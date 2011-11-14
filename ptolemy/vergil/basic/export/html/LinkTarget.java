/* Parameter specifying the target for an HTML link.

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
//// LinkTarget
/**
 * Parameter specifying the target for an HTML link.
 * The parameter offers the following possibilities:
 *  <ul>
 *  <li><b>_lightbox</b>: Open in a lightbox-style popup frame.
 *  <li><b>_blank</b>: Open in a new window or tab.
 *  <li><b>_self</b>: Open in the same frame as it was clicked.
 *  <li><b>_parent</b>: Open in the parent frameset.
 *  <li><b>_top</b>: Open in the full body of the window.
 *  <li><b><i>framename</i></b>: Open in a named frame.
 *  </ul>
 *  The default is "_lightbox".
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class LinkTarget extends StringParameter {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public LinkTarget(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        addChoice("_lightbox");
        addChoice("_blank");
        addChoice("_self");
        addChoice("_top");
        setExpression("_lightbox");
    }
    
    /** Return the modifier to append to an href anchor corresponding
     *  to the selected option. This will be of the form
     *  'class="x"' or 'target="y"', where x is "iframe" if
     *  "_iframe" is selected, and y is whatever is selected otherwise.
     * 
     *  @return The modifier to use in an href anchor.
     *  @throws IllegalActionException If the current value cannot
     *   be evaluated.
     */
    public String getModifier() throws IllegalActionException {
        String value = stringValue();
        if (value.equals("_lightbox")) {
            // Strangely, the class has to be "iframe".
            // I don't understand why it can't be "lightbox".
            return "class=\"iframe\"";
        } else {
            return "target=\"" + value + "\"";
        }
    }
}
