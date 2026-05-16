/* Decide which agent strategy to run for a given user message.

 Copyright (c) 2024-2026 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY
 */
package org.ptolemy.agent.agent;

import org.ptolemy.agent.session.PtolemySession;

import ptolemy.actor.CompositeActor;
import ptolemy.kernel.ComponentEntity;

///////////////////////////////////////////////////////////////////
//// AgentRouter

/**
 Heuristic mode classifier for the agent. Decides whether a given
 user message + current session state should run as:

 <ul>
 <li>{@link Mode#PIPELINE} — full plan-then-build-then-refactor flow.
 Used when starting from an empty model AND/OR the user message
 explicitly asks for a new model.</li>
 <li>{@link Mode#SINGLE} — one-pass agent with the full tool set.
 Used for tweaks ("change Scale1.factor to 5"), targeted refactors
 ("wrap these three into a composite"), or follow-up edits where a
 plan-then-build dance would just waste budget.</li>
 <li>{@link Mode#CHAT} — pure-prose response with no tool calls. Used
 for greetings, questions about the existing model, or messages that
 reference no concrete edit at all.</li>
 </ul>

 <p>The classifier is intentionally heuristic so it never adds an
 extra LLM round-trip just to decide what to do. Callers can always
 override the decision with the {@code mode} field on the chat
 request body.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class AgentRouter {

    /** Possible execution strategies. */
    public enum Mode {
        /** Full plan + build + refactor pipeline. */
        PIPELINE,
        /** One agent, full toolset. */
        SINGLE,
        /** Pure conversational reply, no tools. */
        CHAT
    }

    private AgentRouter() {
    }

    /** Heuristic English + Chinese classifier.
     *  @param session The session under edit (may have a model or not).
     *  @param message The raw user message.
     *  @return A {@link Mode}. PIPELINE only when starting (mostly) from
     *      scratch AND the user clearly wants new logic; SINGLE for
     *      edits/tweaks/targeted refactors; CHAT for short non-edit
     *      messages on an existing model.
     */
    public static Mode classify(PtolemySession session, String message) {
        String raw = message == null ? "" : message;
        String m = raw.toLowerCase().trim();
        int entities = _countTopLevelEntities(session);
        boolean emptyModel = entities == 0;

        // Bare greetings as a complete utterance.
        if (m.equals("hi") || m.equals("hello") || m.equals("hey")
                || m.equals("你好") || m.equals("您好") || m.equals("嗨")
                || m.equals("thanks") || m.equals("thank you")
                || m.equals("谢谢") || m.equals("ok") || m.equals("好的")) {
            return Mode.CHAT;
        }

        // Strong "build new" signals.
        boolean buildIntent = _containsAny(m,
                "build a ", "build an ", "create a ", "create an ",
                "implement a ", "implement an ", "design a ", "design an ",
                "develop a ", "develop an ", "make a ", "make an ",
                "model a ", "model an ", "rebuild", "start over",
                "from scratch",
                "搭建", "搭一", "构建", "实现", "建模", "建一个",
                "做一个", "做个", "开发", "重新搭", "重建",
                "重新建模", "从头开始");

        // "Edit / tweak / parameter change" signals.
        boolean tweakIntent = _containsAny(m,
                "change ", "set ", "update ", "modify ", "tweak ",
                "rename ", "delete ", "remove ", "disconnect",
                "increase", "decrease", "add a recorder", "add a "
                        + "const", "add another", "add the ",
                "swap ", "replace ",
                "改成", "改为", "修改", "调整", "设置", "把 ", "删除",
                "删 ", "去掉", "增加", "重连", "替换", "换成");

        // Targeted refactor (no new logic, only restructuring).
        boolean refactorIntent = _containsAny(m,
                "refactor", "wrap into", "group ", "encapsulate",
                "组成 composite", "封装", "分组", "重构", "组合",
                "包成");
        // Model diagnosis / optimization requests should run tools even
        // when phrased as a question.
        boolean optimizeIntent = _containsAny(m,
                "optimiz", "diagnos", "health check", "what is wrong",
                "validate model", "validate the model", "check model",
                "unconnected", "disconnected", "orphan",
                "optimize model", "cleanup model",
                "优化", "诊断", "校验", "检查模型", "模型问题",
                "还有什么问题", "未连接", "孤立", "断开", "分块",
                "组件过多", "过多组件", "递归");

        // Conversational signals: questions about the model, greetings.
        boolean endsWithQuestion = m.endsWith("?") || m.endsWith("？");
        boolean startsWithQuestionWord = _startsWithAny(m,
                "what ", "what's ", "whats ", "how ", "why ",
                "when ", "where ", "is ", "are ", "does ", "do ",
                "can ", "could ", "would ", "should ",
                "什么", "怎么", "为什么", "如何", "是否", "能不能");
        boolean conversational = endsWithQuestion
                || startsWithQuestionWord
                || _containsAny(m,
                        "what does", "what is", "what's", "whats",
                        "explain", "describe", "how does", "why does",
                        "show me", "list ", "tell me",
                        "thanks", "thank you", "hi ", "hello", "hey",
                        "什么", "解释", "说明一下", "是什么", "为什么",
                        "怎么", "如何", "谢谢", "你好");

        // ----- Decision tree -----
        if (emptyModel && buildIntent) {
            return Mode.PIPELINE;
        }
        if (emptyModel && !buildIntent && !tweakIntent
                && conversational) {
            return Mode.CHAT;
        }
        if (emptyModel) {
            // Empty model + edit-like words = the user is probably
            // describing what to build. Use pipeline.
            return Mode.PIPELINE;
        }

        // ----- Non-empty model from here -----

        if (buildIntent && message != null && message.length() > 60) {
            // "rebuild as ...", "build a much bigger ..." with a
            // substantial new spec → run the whole pipeline again.
            return Mode.PIPELINE;
        }
        if (refactorIntent || tweakIntent) {
            return Mode.SINGLE;
        }
        if (optimizeIntent) {
            return Mode.SINGLE;
        }
        // Conversational on an existing model — questions, status
        // checks, anything ending in '?'. The conversational flag is
        // now strong enough to drive the decision on its own.
        if (conversational && !tweakIntent && !buildIntent) {
            return Mode.CHAT;
        }
        // Default for ambiguous messages on an existing model: SINGLE.
        return Mode.SINGLE;
    }

    private static boolean _startsWithAny(String haystack,
            String... prefixes) {
        if (haystack == null) {
            return false;
        }
        for (String p : prefixes) {
            if (haystack.startsWith(p)) {
                return true;
            }
        }
        return false;
    }

    private static boolean _containsAny(String haystack, String... needles) {
        if (haystack == null) {
            return false;
        }
        for (String n : needles) {
            if (haystack.contains(n)) {
                return true;
            }
        }
        return false;
    }

    private static int _countTopLevelEntities(PtolemySession session) {
        if (session == null) {
            return 0;
        }
        CompositeActor top = session.toplevel();
        if (top == null) {
            return 0;
        }
        int count = 0;
        for (Object obj : top.entityList()) {
            if (!(obj instanceof ComponentEntity)) {
                continue;
            }
            ComponentEntity ent = (ComponentEntity) obj;
            String name = ent.getName();
            if (name != null && name.startsWith("__recorder__")) {
                continue; // injected probes don't count
            }
            count++;
        }
        return count;
    }
}
