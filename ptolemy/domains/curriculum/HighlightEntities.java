/* An editor factory for an object that has a fileOrURL parameter.

 Copyright (c) 2006-2014 The Regents of the University of California.
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
package ptolemy.domains.curriculum;

import java.awt.Frame;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.icon.BoxedValueIcon;

///////////////////////////////////////////////////////////////////
//// HighlightEntities

/**
 An attribute that highlights entities with a specified color when you
 double click on its icon, or when you invoke Configure. To edit its
 parameters, hold the Alt key while double clicking.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.2
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class HighlightEntities extends Attribute {

    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public HighlightEntities(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Define an icon that displays the label.
        BoxedValueIcon _icon = new BoxedValueIcon(this, "_icon");
        _icon.attributeName.setExpression("label");
        _icon.displayWidth.setExpression("40");

        // Attribute that specifies what to do on double click.
        new HighlightIcons(this, "_highlightIcons");

        entityNames = new Parameter(this, "entityNames");
        entityNames.setTypeEquals(new ArrayType(BaseType.STRING));

        // NOTE: The name of the this parameter violates the
        // naming conventions so that it is recorgnized as a highlight
        // color for this icon.
        _highlightColor = new ColorAttribute(this, "_highlightColor");
        // Yellow default.
        _highlightColor.setExpression("{1.0, 1.0, 0.0, 1.0}");

        label = new StringParameter(this, "label");
        label.setExpression("HighlightEntities");

        SingletonParameter hideName = new SingletonParameter(this, "_hideName");
        hideName.setToken(BooleanToken.TRUE);
        hideName.setVisibility(Settable.EXPERT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                          parameters                       ////

    /** List of names of the entities to highlight.
     *  This is an array of strings that defaults to
     *  null, which is interpreted to mean to clear
     *  all highlights.
     */
    public Parameter entityNames;

    /** Highlight color. This defaults to yellow.
     */
    public ColorAttribute _highlightColor;

    /** Label to put in the icon for this object.
     *  This is a string that defaults to "HighlightEntities".
     */
    public StringParameter label;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** Attribute that defines the action performed on
     *  "configure" command.
     */
    public class HighlightIcons extends EditorFactory {
        /**
         * Construct an attribute that defines the action performed
         * on a "configure" command.
         * @param container The container for this attribute.
         * @param name The name of this attribute.
         * @exception IllegalActionException If thrown by the parent class.
         * @exception NameDuplicationException If thrown by the parent class.
         */
        public HighlightIcons(NamedObj container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
        }

        @Override
        public void createEditor(NamedObj object, Frame parent) {
            // Perform the highlighting.
            try {
                NamedObj container = HighlightEntities.this.getContainer();
                if (container instanceof CompositeEntity) {
                    ArrayToken names = (ArrayToken) entityNames.getToken();
                    if (names != null && names.length() > 0) {
                        StringBuffer moml = new StringBuffer("<group>");
                        for (int i = 0; i < names.length(); i++) {
                            String name = ((StringToken) names.getElement(i))
                                    .stringValue();
                            ComponentEntity entity = ((CompositeEntity) container)
                                    .getEntity(name);
                            if (entity != null) {
                                moml.append("<entity name=\"");
                                moml.append(name);
                                moml.append("\">");
                                moml.append(_highlightColor.exportMoML());
                                moml.append("</entity>");
                            }
                        }
                        moml.append("</group>");
                        container.requestChange(new MoMLChangeRequest(this,
                                container, moml.toString()));
                    } else {
                        // Clear all highlights.
                        StringBuffer moml = new StringBuffer("<group>");
                        for (Object entity : ((CompositeEntity) container)
                                .entityList()) {
                            if (((ComponentEntity) entity)
                                    .getAttribute("_highlightColor") != null) {
                                moml.append("<entity name=\"");
                                moml.append(((ComponentEntity) entity)
                                        .getName());
                                moml.append("\">");
                                moml.append("<deleteProperty name=\"_highlightColor\"/>");
                                moml.append("</entity>");
                            }
                        }
                        moml.append("</group>");
                        container.requestChange(new MoMLChangeRequest(this,
                                container, moml.toString()));
                    }
                }
            } catch (IllegalActionException e1) {
                MessageHandler.error("Failed to set highlight colors", e1);
            }
        }
    }
}
