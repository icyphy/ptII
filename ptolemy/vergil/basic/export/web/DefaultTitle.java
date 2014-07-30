/* Interface for parameters that provide web export content.

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

import java.util.List;
import java.util.Locale;

import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// DefaultTitle
/**
 * A parameter specifying default title to associate
 * with a model and with components in the model.
 * By default, this attribute uses the model name
 * and component names as the title.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DefaultTitle extends WebContent implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public DefaultTitle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _icon.setIconText("T");
        displayText
        .setExpression("Default title to give to icons in the model.");

        showTitleInHTML = new Parameter(this, "showTitleInHTML");
        showTitleInHTML.setExpression("true");
        showTitleInHTML.setTypeEquals(BaseType.BOOLEAN);

        include = new StringParameter(this, "include");
        include.addChoice("Entities");
        include.addChoice("Attributes");
        include.addChoice("All");
        include.addChoice("None");
        include.setExpression("Entities");

        instancesOf = new StringParameter(this, "instancesOf");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If non-empty (the default), specifies a class name.
     *  Only entities or attributes (depending on <i>include</i>)
     *  implementing the specified
     *  class will be assigned the title defined by this
     *  DefaultTitle parameter.
     */
    public StringParameter instancesOf;

    /** Specification of whether to provide the title for
     *  Attributes, Entities, or both. This is either "Entities" (the
     *  default), "Attributes", "All", or "None".
     */
    public StringParameter include;

    /** If set to true, then the title given by this parameter
     *  will be shown in the HTML prior to the image of the model
     *  (as well as in the image of the model, if it is visible
     *  when the export to web occurs). This is a boolean that
     *  defaults to true.
     */
    public Parameter showTitleInHTML;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** A title is of type text/html.
     *
     * @return The string text/html
     */
    @Override
    public String getMimeType() {
        return "text/html";
    }

    /** Return true, since new title content should overwrite old title content.
     *
     * @return True, since new title content should overwrite old title content.
     */
    @Override
    public boolean isOverwriteable() {
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Provide content to the specified web exporter to be
     *  included in a web page. This class provides a default title for the
     *  web page and for each object
     *  as specified by <i>include</i> and <i>instancesOf</i>.
     *
     *  @param exporter  The web exporter to which to write content.
     *  @exception IllegalActionException If something is wrong with the web
     *  content or the object already has an attribute with the same name as the
     *  the created WebAttribute
     */
    @Override
    protected void _provideAttributes(WebExporter exporter)
            throws IllegalActionException {

        WebAttribute webAttribute;

        // Set the title for the model object.
        String titleValue = stringValue();
        if (titleValue == null || titleValue.equals("")) {
            // Use the model name as the default title.
            titleValue = toplevel().getDisplayName();
        }

        // FIXME:  Refactor so we don't need this method
        exporter.setTitle(titleValue,
                ((BooleanToken) showTitleInHTML.getToken()).booleanValue());

        // Create a WebAttribute for title and add to exporter.
        // Content should only be added once (onceOnly -> true).
        /* title attribute is now used for displaying parameter table.
        webAttribute = WebAttribute.createWebAttribute(getContainer(),
                "titleWebAttribute", "title");
        webAttribute.setExpression(titleValue);
        exporter.defineAttribute(webAttribute, true);
         */

        boolean entities = false, attributes = false;
        String includeValue = include.stringValue().toLowerCase(
                Locale.getDefault());
        if (includeValue.equals("all")) {
            entities = true;
            attributes = true;
        } else if (includeValue.equals("entities")) {
            entities = true;
        } else if (includeValue.equals("attributes")) {
            attributes = true;
        }
        List<NamedObj> objects;
        String instances = instancesOf.stringValue();
        NamedObj container = getContainer();
        if (entities && container instanceof CompositeEntity) {
            if (instances.trim().equals("")) {
                objects = ((CompositeEntity) container).entityList();
            } else {
                try {
                    Class restrict = Class.forName(instances);
                    objects = ((CompositeEntity) container)
                            .entityList(restrict);
                } catch (ClassNotFoundException e) {
                    throw new IllegalActionException(this, "No such class: "
                            + instances);
                }
            }
            /* title attribute is now used for displaying parameter table.
            for (NamedObj object : objects) {
                // Create a WebAttribute for each object's title and add to
                // exporter.   Content should only be added once
                // (onceOnly -> true).
                webAttribute = WebAttribute.createWebAttribute(object,
                        "titleWebAttribute", "title");
                webAttribute.setExpression(object.getDisplayName());
                exporter.defineAttribute(webAttribute, true);
            }
             */
        }
        if (attributes) {
            if (instances.trim().equals("")) {
                objects = ((CompositeEntity) container).attributeList();
            } else {
                try {
                    Class restrict = Class.forName(instances);
                    objects = ((CompositeEntity) container)
                            .attributeList(restrict);
                } catch (ClassNotFoundException e) {
                    throw new IllegalActionException(this, "No such class: "
                            + instances);
                }
            }
            for (NamedObj object : objects) {
                // Create a WebAttribute for each object's title and add to
                // exporter.   Content should only be added once
                // (onceOnly -> true).
                webAttribute = WebAttribute.createWebAttribute(object,
                        "titleWebAttribute", "title");
                webAttribute.setExpression(object.getDisplayName());
                exporter.defineAttribute(webAttribute, true);
            }
        }
    }

}
