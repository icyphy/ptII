/* Attribute specifying a URI to link to from an icon.

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
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.ValueIcon;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;


///////////////////////////////////////////////////////////////////
//// IconLink
/**
 * Attribute specifying a URI to link to from an icon.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class IconLink extends StringParameter implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public IconLink(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        linkTarget = new LinkTarget(this, "linkTarget");
        
        // Add parameters that ensure this is rendered correctly in Vergil.
        new SingletonAttribute(this, "_hideName");
        new ValueIcon(this, "_icon");
        ConfigurableAttribute smallIcon = new ConfigurableAttribute(this, "_smallIconDescription");
        try {
            smallIcon.configure(null, null,
                    "<svg><text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\">link</text></svg>");
        } catch (Exception e) {
            // Show exception on the console. Should not occur.
            e.printStackTrace();
        }
        new VisibleParameterEditorFactory(this, "_editorFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** Parameter specifying the target for the link.
     *  The possibilities are:
     *  <ul>
     *  <li><b>_iframe</b>: Open in a lightbox-style popup frame.
     *  <li><b>_blank</b>: Open in a new window or tab.
     *  <li><b>_self</b>: Open in the same frame as it was clicked.
     *  <li><b>_parent</b>: Open in the parent frameset.
     *  <li><b>_top</b>: Open in the full body of the window.
     *  <li><b><i>framename</i></b>: Open in a named frame.
     *  </ul>
     *  The default is "_iframe".
     */
    public LinkTarget linkTarget;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a string of the form:
     *  <pre>
     *     href="linkvalue" target="targetvalue" title="containername"
     *  <pre>
     *  where <i>linkvalue</i> is the string value of this parameter,
     *  <i>targetvalue</i> is the value of the <i>linkTarget</i> parameter,
     *  and <i>containername</i> is the name of the container of this object.
     *  @return Text to insert into an anchor or area command in HTML.
     *  @throws IllegalActionException If evaluating the parameter fails.
     */
    public String getContent() throws IllegalActionException {
        return "href=\""
        	+ StringUtilities.escapeString(stringValue())
        	+ "\" "
        	+ linkTarget.getModifier()
        	+ " title=\""
        	+ getContainer().getName()
        	+ "\"";
    }
}
