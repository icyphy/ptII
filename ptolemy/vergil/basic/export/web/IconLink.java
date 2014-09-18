/* Attribute specifying a URI to link to from an icon.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

import ptolemy.actor.gui.LiveLink;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// IconLink
/**
 * Attribute specifying a URI to link to from an icon when a model
 * is exported to a web page.
 * Using the <i>linkTarget</i> parameter, you can control how the
 * link is displayed.
 * In contrast, {@link LiveLink} provides a link to a model
 * in Vergil (instead of in an exported web page).
 * <p>
 * To use this, drag it onto an icon in your model. Then double
 * click on that icon to set the URL to link to. The <i>linkTarget</i>
 * parameter specifies whether the link should be opened in a
 * new browser window (the default), in the same browser window,
 * in a lightbox, etc.
 * <p>
 * Note that this attribute can be used in combination with
 * {@link LinkToOpenTableaux}. The latter provides a hyperlink that works
 * within Vergil, whereas this attribute provides a hyperlink
 * that works in an exported HTML page.  For example,
 * LinkToOpenTableaux might be used to provide a hyperlink to another
 * model (a pointer to its MoML file), so that double clicking
 * on the container of the LiveLink attribute opens the other
 * model. If that container also contains an instance of
 * IconLink, then when the model is exported to a web page,
 * the container's icon can become a link to the exported page
 * for the other model.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class IconLink extends WebContent implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
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

    /** A link is of type text/html.
     *
     * @return The string text/html
     */
    @Override
    public String getMimeType() {
        return "text/html";
    }

    /** Return true, since new content should overwrite old.
     *
     * @return True, since new content should overwrite old
     */
    @Override
    public boolean isOverwriteable() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class defines an href attribute to associate with
     *  the area of the image map corresponding to its container.
     *
     *  @param exporter  The web exporter to write content to
     *  @exception IllegalActionException If evaluating the value
     *   of this parameter fails, or creating a web attribute fails.
     */
    @Override
    protected void _provideAttributes(WebExporter exporter)
            throws IllegalActionException {

        WebAttribute webAttribute;

        NamedObj container = getContainer();
        if (container != null) {
            // Last argument specifies to overwrite any previous value defined.
            if (!stringValue().trim().equals("")) {

                // Create link attribute and add to exporter.
                // Content should only be added once (onceOnly -> true).
                webAttribute = WebAttribute.createWebAttribute(getContainer(),
                        "hrefWebAttribute", "href");
                webAttribute.setExpression(stringValue());
                exporter.defineAttribute(webAttribute, true);
            }

            String targetValue = linkTarget.stringValue();
            if (!targetValue.trim().equals("")) {
                if (targetValue.equals("_lightbox")) {
                    // Strangely, the class has to be "iframe".
                    // I don't understand why it can't be "lightbox".

                    // Create class attribute and add to exporter.
                    // Content should only be added once (onceOnly -> true).
                    webAttribute = WebAttribute.appendToWebAttribute(
                            getContainer(), "classWebAttribute", "class",
                            "iframe");
                    exporter.defineAttribute(webAttribute, true);
                } else {

                    // Create target attribute and add to exporter.
                    // Content should only be added once (onceOnly -> true).
                    webAttribute = WebAttribute.createWebAttribute(
                            getContainer(), "targetWebAttribute", "target");
                    webAttribute.setExpression(targetValue);
                    exporter.defineAttribute(webAttribute, true);
                }
            }
        }
    }
}
