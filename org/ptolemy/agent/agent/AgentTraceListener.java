/* Listener for incremental agent trace updates during a run. */

package org.ptolemy.agent.agent;

///////////////////////////////////////////////////////////////////
//// AgentTraceListener

/**
 * Receives each {@link AgentTrace.Step} as soon as it is recorded so
 * HTTP handlers can stream progress to the frontend.
 */
public interface AgentTraceListener {

    /** @param step The step that was just appended to the trace. */
    void onStep(AgentTrace.Step step);
}
