/* Parameter specifying an event type for an area action.

 Copyright (c) 2011-2012 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.web;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// AreaEventType
/**
 * A parameter used to specify the event type actions associated
 * with an area in an HTML image map. This parameter offers the following events:
 *  <ul>
 *  <li><b>onblur</b>: Command to be run when an element loses focus.
 *  <li><b>onclick</b>: Command to be run on a mouse click.
 *  <li><b>ondblclick</b>: Command to be run on a mouse double-click.
 *  <li><b>onfocus</b>: Command to be run when an element gets focus.
 *  <li><b>onmousedown</b>: Command to be run when mouse button is pressed.
 *  <li><b>onmousemove</b>: Command to be run when mouse pointer moves.
 *  <li><b>onmouseout</b>: Command to be run when mouse pointer moves out of an element.
 *  <li><b>onmouseover</b>: Command to be run when mouse pointer moves over an element.
 *  <li><b>onmouseup</b>: Command to be run when mouse button is released.
 *  <li><b>onkeydown</b>: Command to be run when a key is pressed.
 *  <li><b>onkeypress</b>: Command to be run when a key is pressed and released.
 *  <li><b>onkeyup</b>: Command to be run when a key is released.
 *  </ul>
 *  These are the events supported by the HTML area tag.
 *  The default value is "onmouseover". It is not clear how
 *  these areas get the focus, so the focus and key commands do not
 *  appear to be useful.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class AreaEventType extends StringParameter {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public AreaEventType(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        addChoice("onblur");
        addChoice("onclick");
        addChoice("ondblclick");
        addChoice("onfocus");
        addChoice("onmousedown");
        addChoice("onmousemove");
        addChoice("onmouseout");
        addChoice("onmouseover");
        addChoice("onmouseup");
        addChoice("onkeydown");
        addChoice("onkeypress");
        addChoice("onkeyup");
        setExpression("onmouseover");
    }
}
