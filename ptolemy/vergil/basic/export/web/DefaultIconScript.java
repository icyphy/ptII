/* Interface for parameters that provide web export content.

 Copyright (c) 2011-2013 The Regents of the University of California.
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
//// DefaultIconScript
/**
 * A parameter specifying default JavaScript actions to associate
 * with icons in model. Putting this attribute into a model causes
 * the icons of entities, attributes, or both, to be assigned a
 * default action of type given by <i>eventType</i>, where the
 * action is defined by the value of this parameter.
 * This will replace any configuration default that targets
 * the same event type, includes the same objects, and
 * targets the same instanceOf possibilities.
 * <p>
 * A typical use of this would be to set its string value
 * to something like "foo(args)" where foo is a JavaScript function
 * defined in the <i>script</i> parameter.
 * You can also provide HTML text to insert into the start or
 * end sections of the container's web page.
 * </p>
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DefaultIconScript extends IconScript {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the superclass throws it.
     */
    public DefaultIconScript(NamedObj container, String name)
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
     *  DefaultIconScript parameter.
     */
    public StringParameter instancesOf;

    /** Specification of whether to provide the default behavior for
     *  Attributes, Entities, or both. This is either "Entities" (the
     *  default), "Attributes", or "All".
     */
    public StringParameter include;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This class provides default content for each object
     *  as specified by <i>include</i> and <i>instancesOf</i>.
     *
     *  @param exporter The web exporter to add content to
     *  @exception IllegalActionException If a subclass throws it.
     */
    protected void _provideAttributes(WebExporter exporter)
            throws IllegalActionException {
        WebAttribute webAttribute;

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
            for (NamedObj object : objects) {
                if (object != null) {
                    // TODO:  Enable multiple eventTypes
                    String eventTypeValue = eventType.stringValue();
                    if (!eventTypeValue.trim().equals("")) {
                        // Create WebAttribute for event and add to exporter.
                        // Content should only be added once (onceOnly -> true).
                        webAttribute = WebAttribute
                                .createWebAttribute(getContainer(),
                                        eventTypeValue + "WebAttribute",
                                        eventTypeValue);
                        webAttribute.setExpression(stringValue());
                        exporter.defineAttribute(webAttribute, true);

                        _provideDefaultAttributes(object, exporter);
                    }
                }
            }
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
                // Do not generate events for WebAttributes and WebElements
                if (object != null && !(object instanceof WebAttribute)
                        && !(object instanceof WebElement)) {
                    // TODO:  Enable multiple eventTypes
                    String eventTypeValue = eventType.stringValue();
                    if (!eventTypeValue.trim().equals("")) {
                        // Create WebAttribute for event and add to exporter.
                        // Content should only be added once (onceOnly -> true).
                        webAttribute = WebAttribute
                                .createWebAttribute(getContainer(),
                                        eventTypeValue + "WebAttribute",
                                        eventTypeValue);
                        webAttribute.setExpression(stringValue());
                        exporter.defineAttribute(webAttribute, true);

                        _provideDefaultAttributes(object, exporter);
                    }
                }
            }
        }
    }

    /** Return attributes for default events, e.g. onmouseover().  If an
     *  attribute is already defined for this event, do nothing.
     *  Returns null in this class.  Derived classes should override.
     *
     * @param exporter The WebExporter to add content to
     * @param object  The NamedObj to generate default events for
     * @exception IllegalActionException If there is a problem creating the content
     * or if there is a name duplication with the created attributes
     */
    protected void _provideDefaultAttributes(NamedObj object,
            WebExporter exporter) throws IllegalActionException {
    }

}
