/* Interface for parameters that provide web export content.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.html;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


///////////////////////////////////////////////////////////////////
//// IconScript
/**
 * A parameter associating a JavaScript with an icon in model.
 * This class assumes that the icon becomes an area in an image map
 * on an HTML page. This parameter provides a way to specify a
 * script to execute when that area in the image map is the target
 * of some UI event, such as mouse movement or clicking or keyboard
 * action.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class IconScript extends StringParameter implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public IconScript(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        eventType = new AreaEventType(this, "eventType");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** Event type to respond to by executing this script.
     *  This script will be run when the icon corresponding to the
     *  container of this parameter gets one of the following events:
     *  <ul>
     *  <li><b>onblur</b>: Script to be run when an element loses focus.
     *  <li><b>onclick</b>: Script to be run on a mouse click.
     *  <li><b>ondblclick</b>: Script to be run on a mouse double-click.
     *  <li><b>onfocus</b>: Script to be run when an element gets focus.
     *  <li><b>onmousedown</b>: Script to be run when mouse button is pressed.
     *  <li><b>onmousemove</b>: Script to be run when mouse pointer moves.
     *  <li><b>onmouseout</b>: Script to be run when mouse pointer moves out of an element.
     *  <li><b>onmouseover</b>: Script to be run when mouse pointer moves over an element.
     *  <li><b>onmouseup</b>: Script to be run when mouse button is released.
     *  <li><b>onkeydown</b>: Script to be run when a key is pressed.
     *  <li><b>onkeypress</b>: Script to be run when a key is pressed and released.
     *  <li><b>onkeyup</b>: Script to be run when a key is released.
     *  </ul>
     *  These are the events supported by the HTML area tag.
     *  The default is "onmouseover".
     */
    public AreaEventType eventType;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the content to export to the web.
     *  @return Content to export to the web.
     *  @throws IllegalActionException If evaluating the parameter fails.
     */
    public String getContent() throws IllegalActionException {
        
        // FIXME: invoke writeText(stringValue()) by default?
        // How to ensure that writeText is defined?  Need to require
        // a header script, but need to make sure it doesn't appear ore than once.
        return stringValue();
    }
}
