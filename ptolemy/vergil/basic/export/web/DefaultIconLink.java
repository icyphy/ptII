/* Interface for parameters that provide web export content.

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

import java.util.List;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;


///////////////////////////////////////////////////////////////////
//// DefaultIconLink
/**
 * A parameter specifying default hyperlink to associate
 * with icons in model. Putting this attribute into a model causes
 * the icons of entities, attributes, or both, to be assigned a
 * default hyperlink to the URI given by <i>linkTarget</i>.
 * This will replace any configuration default link that
 * includes the same objects, and
 * targets the same instanceOf possibilities.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DefaultIconLink extends IconLink {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public DefaultIconLink(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        include = new StringParameter(this, "include");
        include.addChoice("Entities");
        include.addChoice("Attributes");
        include.addChoice("All");
        include.setExpression("Entities");

        instancesOf = new StringParameter(this, "instancesOf");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If non-empty (the default), specifies a class name.
     *  Only entities or attributes (depending on <i>include</i>)
     *  implementing the specified
     *  class will be assigned the control defined by this
     *  DefaultIconLink parameter.
     */
    public StringParameter instancesOf;

    /** Specification of whether to provide the link for
     *  Attributes, Entities, or both. This is either "Entities" (the
     *  default), "Attributes", or "All".
     */
    public StringParameter include;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Override the base class to define an href attribute to associate with
     *  the area of the image map corresponding to its container.
     *
     *  @param exporter  The web exporter to which to write content.
     *  @exception IllegalActionException If evaluating the value
     *   of this parameter fails.
     */
    protected void _provideAttributes(WebExporter exporter)
        throws IllegalActionException{

        boolean entities = false, attributes = false;
        String includeValue = include.stringValue().toLowerCase();
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
                objects = ((CompositeEntity)container).entityList();
            } else {
                try {
                    Class restrict = Class.forName(instances);
                    objects = ((CompositeEntity)container).entityList(restrict);
                } catch (ClassNotFoundException e) {
                    throw new IllegalActionException(this,
                            "No such class: " + instances);
                }
            }
            for (NamedObj object : objects) {
                _provideEachAttribute(exporter, object);
            }
        }
        if (attributes) {
            if (instances.trim().equals("")) {
                objects = ((CompositeEntity)container).attributeList();
            } else {
                try {
                    Class restrict = Class.forName(instances);
                    objects = ((CompositeEntity)container).attributeList(restrict);
                } catch (ClassNotFoundException e) {
                    throw new IllegalActionException(this,
                            "No such class: " + instances);
                }
            }
            for (NamedObj object : objects) {
                _provideEachAttribute(exporter, object);
            }
        }
    }

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class defines an href attribute to associate with
     *  the area of the image map corresponding to its container.
     *
     *  @param exporter The exporter.
     *  @param object The object.
     *  @exception IllegalActionException If evaluating the value
     *   of this parameter fails.
     */
    protected void _provideEachAttribute(WebExporter exporter, NamedObj object)
            throws IllegalActionException {

        WebAttribute webAttribute;

        if (object != null) {
            // Last argument specifies to overwrite any previous value defined.
            if (!stringValue().trim().equals("")) {

                // Create link attribute and add to exporter.
                // Content should only be added once (onceOnly -> true).
                webAttribute =
                    WebAttribute.createWebAttribute(getContainer(),
                            "hrefWebAttribute", "href");
                webAttribute.setExpression(stringValue());
                exporter.defineAttribute(webAttribute, true);

                String targetValue = linkTarget.stringValue();
                if (!targetValue.trim().equals("")) {
                    if (targetValue.equals("_lightbox")) {
                        // Strangely, the class has to be "iframe".
                        // I don't understand why it can't be "lightbox".

                        // Create class attribute and add to exporter.
                        // Content should only be added once (onceOnly -> true).
                        webAttribute = WebAttribute
                             .createWebAttribute(getContainer(),
                                     "classWebAttribute", "class");
                        webAttribute.setExpression("iframe");
                        exporter.defineAttribute(webAttribute, true);
                    } else {
                        // Create target attribute and add to exporter.
                        // Content should only be added once (onceOnly -> true).
                        webAttribute = WebAttribute.
                            createWebAttribute(getContainer(),
                                    "targetWebAttribute", "target");
                        webAttribute.setExpression(targetValue);
                        exporter.defineAttribute(webAttribute, true);
                    }
                }
            }
        }
    }
}
