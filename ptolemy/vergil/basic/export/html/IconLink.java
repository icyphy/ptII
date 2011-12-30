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

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


///////////////////////////////////////////////////////////////////
//// IconLink
/**
 * Attribute specifying a URI to link to from an icon.
 * Using the <i>linkTarget</i> parameter, you can control how the
 * link is displayed.
 * <p>
 * To use this, drag it onto an icon in your model. Then double
 * click on that icon to set the URL to link to. The <i>linkTarget</i>
 * parameter specifies whether the link should be opened in a
 * new browser window (the default), in the same browser window,
 * in a lightbox, etc.
 * <p>
 * Note that this attribute can be used in combination with
 * {@link LiveLink}. The latter provides a hyperlink that works
 * within Vergil, whereas this attribute provides a hyperlink
 * that works in an exported HTML page.  For example,
 * LiveLink might be used to provide a hyperlink to another
 * model (a pointer to its MoML file), so that double clicking
 * on the container of the LiveLink attribute opens the other
 * model. If that container also contains an instance of
 * IconLink, then when the model is exported to a web page,
 * the container's icon can become a link to the exported page
 * for the other model.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class IconLink extends WebContent implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public IconLink(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        _icon.setIconText("L");
        displayText.setExpression("http://ptolemy.org");
        height.setExpression("1");

        linkTarget = new LinkTarget(this, "linkTarget");
        // Note that the default value and choices are set
        // in the above constructor call.
        
        setExpression("http://ptolemy.org");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////
    
    /** Parameter specifying the target for the link.
     *  The possibilities are:
     *  <ul>
     *  <li><b>_lightbox</b>: Open in a lightbox-style popup frame.
     *  <li><b>_blank</b>: Open in a new window or tab.
     *  <li><b>_self</b>: Open in the same frame as it was clicked.
     *  <li><b>_parent</b>: Open in the parent frameset.
     *  <li><b>_top</b>: Open in the full body of the window.
     *  <li><b><i>framename</i></b>: Open in a named frame.
     *  </ul>
     *  The default is "_lightbox".
     */
    public LinkTarget linkTarget;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class provides only outside content, so this method
     *  does nothing.
     *  @param exporter The exporter.
     *  @throws IllegalActionException If a subclass throws it.
     */
    public void provideContent(WebExporter exporter) throws IllegalActionException {
        // This class does not provide content.
    }

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class defines an href attribute to associate with
     *  the area of the image map corresonding to its container.
     *  @throws IllegalActionException If evaluating the value
     *   of this parameter fails.
     */
    public void provideOutsideContent(WebExporter exporter) throws IllegalActionException {
        NamedObj container = getContainer();
        if (container != null) {
            // Last argument specifies to overwrite any previous value defined.
            if (!stringValue().trim().equals("")) {
                exporter.defineAreaAttribute(container, "href", stringValue(), true);
                String targetValue = linkTarget.stringValue();
                if (!targetValue.trim().equals("")) {
                    if (targetValue.equals("_lightbox")) {
                        // Strangely, the class has to be "iframe".
                        // I don't understand why it can't be "lightbox".
                        exporter.defineAreaAttribute(container, "class", "iframe", true);
                    } else {
                        exporter.defineAreaAttribute(container, "target", targetValue, true);
                    }
                }
            }
        }
    }
}
