/* Listener interface for decorator events

 Copyright (c) 2014-2015 The Regents of the University of California.
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
package org.ptolemy.ssm;

import ptolemy.data.expr.Parameter;

/** Listener interface for decorator events.
 *
 *  @author Ilge Akkya
 *  @version $Id$
 */
public interface MirrorDecoratorListener {
    /**  The event that is sent by the mirror decorator  and processed
     *  by the listener.
     *  @param eventType The type of the event.
     *  @param p The parameter subject to change.
     */
    public void event(MirrorDecorator ssm, DecoratorEvent eventType, Parameter p);

    /** The event that is sent by the mirror decorator  and processed
     *  by the listener.
     *  @param eventType The type of the event.
     *  @param portName Name of changing port.
     */
    public void event(MirrorDecorator ssm, DecoratorEvent eventType, String portName);

    /** Type of the event. */
    public static enum DecoratorEvent {
        /** Port was added. */
        ADDED_PORT,

        /** Port was removed. */
        REMOVED_PORT,

        /** Parameter was added. */
        ADDED_PARAMETER,

        /** Parameter was removed. */
        REMOVED_PARAMETER,

        /** Parameter was changed. */
        CHANGED_PARAMETER,

        /** PortParameter was added. */
        ADDED_PORT_PARAMETER,

        /** PortParameter was removed. */
        REMOVED_PORT_PARAMETER,

        /** PortParameter was changed. */
        CHANGED_PORT_PARAMETER
    }
}
