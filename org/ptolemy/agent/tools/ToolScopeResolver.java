/* Shared helper for resolving nested composite scopes by path.
 *
 * Copyright (c) 2024-2026 The Regents of the University of California.
 * All rights reserved.
 *
 * Permission is hereby granted, without written agreement and without
 * license or royalty fees, to use, copy, modify, and distribute this
 * software and its documentation for any purpose, provided that the above
 * copyright notice and the following two paragraphs appear in all copies
 * of this software.
 *
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
 * FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 * PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 * CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 * ENHANCEMENTS, OR MODIFICATIONS.
 *
 * PT_COPYRIGHT_VERSION_2
 * COPYRIGHTENDKEY
 */
package org.ptolemy.agent.tools;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;

///////////////////////////////////////////////////////////////////
//// ToolScopeResolver

/** Utility methods for nested {@code parent} path handling. */
final class ToolScopeResolver {

    private ToolScopeResolver() {
    }

    /** Resolve a scope path like {@code "Plant/Controller/Inner"} from
     *  the given top-level composite.
     *  @param top Top-level scope.
     *  @param parentPath Empty for top-level, otherwise slash-separated
     *      composite names.
     *  @return The resolved composite scope.
     *  @throws IllegalArgumentException If any segment is missing or not
     *      a composite.
     */
    static CompositeEntity resolve(CompositeEntity top, String parentPath) {
        if (top == null) {
            throw new IllegalArgumentException("no model loaded");
        }
        String path = parentPath == null ? "" : parentPath.trim();
        if (path.isEmpty()) {
            return top;
        }
        CompositeEntity current = top;
        String[] segments = path.split("/");
        for (String segment : segments) {
            String name = segment == null ? "" : segment.trim();
            if (name.isEmpty()) {
                continue;
            }
            ComponentEntity child = current.getEntity(name);
            if (!(child instanceof CompositeEntity)) {
                throw new IllegalArgumentException(
                        "no composite named '" + path + "' in scope");
            }
            current = (CompositeEntity) child;
        }
        return current;
    }

    /** Wrap an inner MoML fragment inside nested entity tags according to
     *  the given parent path.
     *  @param parentPath Empty for top-level.
     *  @param inner Inner MoML fragment.
     *  @return MoML fragment ready for applyChange.
     */
    static String wrapInParent(String parentPath, String inner) {
        String path = parentPath == null ? "" : parentPath.trim();
        if (path.isEmpty()) {
            return inner;
        }
        String[] segments = path.split("/");
        String wrapped = inner;
        for (int i = segments.length - 1; i >= 0; i--) {
            String segment = segments[i] == null ? "" : segments[i].trim();
            if (segment.isEmpty()) {
                continue;
            }
            wrapped = "<entity name=\"" + AddEntityTool.escape(segment)
                    + "\">" + wrapped + "</entity>";
        }
        return wrapped;
    }
}
