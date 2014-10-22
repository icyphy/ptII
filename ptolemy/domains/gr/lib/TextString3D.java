/* A GR Shape consisting of a text string.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.domains.gr.lib;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.geom.Line2D;

import javax.media.j3d.Font3D;
import javax.media.j3d.FontExtrusion;
import javax.media.j3d.Node;
import javax.media.j3d.Shape3D;
import javax.media.j3d.Text3D;
import javax.media.j3d.Transform3D;
import javax.media.j3d.TransformGroup;
import javax.vecmath.Vector3d;

import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// TextString3D

/**

 An actor that encapsulates 3D text shapes in the GR domain.
 The <i>text</i> port/parameter gives the text to be displayed.
 The <i>fontSize</i> parameter gives the size of the font.
 The <i>extrusionDepth</i> parameter specifies how deep the 3-D
 rendering of the text should be.
 The <i>alignment</i> parameter gives the alignment of the text
 relative to the position of the object.
 The <i>fontFamily</i> parameter specifies the font family.
 The rest of the parameters are described in the base class.

 @author C. Fong and Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (liuxj)
 */
public class TextString3D extends GRShadedShape {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TextString3D(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        text = new PortParameter(this, "text");
        text.setStringMode(true);
        text.setExpression("Ptolemy");

        fontFamily = new StringParameter(this, "fontFamily");
        fontFamily.setExpression("SansSerif");

        // Get font family names from the Font class in Java.
        // This includes logical font names, per Font class in Java:
        // Dialog, DialogInput, Monospaced, Serif, SansSerif, or Symbol.
        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();

        for (String familie : families) {
            fontFamily.addChoice(familie);
        }

        extrusionDepth = new Parameter(this, "extrusionDepth");
        extrusionDepth.setTypeEquals(BaseType.DOUBLE);
        extrusionDepth.setExpression("0.2");

        fontSize = new Parameter(this, "fontSize");
        fontSize.setTypeEquals(BaseType.DOUBLE);
        fontSize.setExpression("1.0");

        alignment = new StringParameter(this, "alignment");
        alignment.setExpression("center");
        alignment.addChoice("center");
        alignment.addChoice("first");
        alignment.addChoice("last");

        fontFamily.moveToFirst();
        alignment.moveToFirst();
        extrusionDepth.moveToFirst();
        fontSize.moveToFirst();
        text.moveToFirst();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** The alignment of the text. This is a string that defaults to
     *  "center". The recognized values are "center", "first", and
     *  "last". A value of "first" means that the first character
     *  is put on the position of the object, whereas "last" means
     *  that the last character is put on the position. Note that
     *  "left" and "right" would make no sense, since the orientation
     *  is arbitrary.
     */
    public StringParameter alignment;

    /** The depth of the extrusion of the text. This is a double that
     *  defaults to 0.2.
     */
    public Parameter extrusionDepth;

    /** The font family. This is a string that defaults to "SansSerif",
     *  a font that is guaranteed by Java to always be present.
     */
    public StringParameter fontFamily;

    /** The font size. This is a double that defaults to 1.0.
     */
    public Parameter fontSize;

    /** The text to display. */
    public PortParameter text;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>text</i>, then update the displayed
     *  text string.
     *  @param attribute The attribute.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == text && _textGeometry != null) {
            String textValue = ((StringToken) text.getToken()).stringValue();
            _textGeometry.setString(textValue);
        } else if ((attribute == fontFamily || attribute == extrusionDepth)
                && _textGeometry != null && _changesAllowedNow) {
            String fontFamilyValue = fontFamily.stringValue();

            // NOTE: The extrusion can, in principle, follow a more complicated
            // path. However, it's not clear what user interface to provide for this.
            double depth = ((DoubleToken) extrusionDepth.getToken())
                    .doubleValue();
            FontExtrusion extrusion = new FontExtrusion(new Line2D.Double(0.0,
                    0.0, depth, 0.0));

            Font3D font3D = new Font3D(
                    new Font(fontFamilyValue, Font.PLAIN, 1), extrusion);
            _textGeometry.setFont3D(font3D);
        } else if (attribute == alignment && _textGeometry != null
                && _changesAllowedNow) {
            String alignmentValue = alignment.stringValue();
            int align = Text3D.ALIGN_CENTER;

            if (alignmentValue.equals("first")) {
                align = Text3D.ALIGN_FIRST;
            } else if (alignmentValue.equals("last")) {
                align = Text3D.ALIGN_LAST;
            }

            _textGeometry.setAlignment(align);
        } else if (attribute == fontSize) {
            if (_scaleTransform != null) {
                float size = (float) ((DoubleToken) fontSize.getToken())
                        .doubleValue();
                _scaleTransform.setScale(new Vector3d(size, size, 1.0f));

                // The following seems to be needed so the new scale
                // takes effect.
                ((TransformGroup) _containedNode).setTransform(_scaleTransform);
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Override the base class to update the <i>text</i> parameter
     *  from the value given at the port, if any.
     *  @return False if the scene graph has already been
     *   initialized.
     *  @exception IllegalActionException If thrown by the parent class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // NOTE: It won't do to do this in fire() because
        // prefire in the base class usually returns false.
        text.update();
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Create the shape and appearance of the encapsulated text.
     *  @exception IllegalActionException If the value of some parameters
     *   can't be obtained.
     */
    @Override
    protected void _createModel() throws IllegalActionException {
        super._createModel();

        String textValue = ((StringToken) text.getToken()).stringValue();
        String fontFamilyValue = fontFamily.stringValue();

        // NOTE: The extrusion can, in principle, follow a more complicated
        // path. However, it's not clear what user interface to provide for this.
        double depth = ((DoubleToken) extrusionDepth.getToken()).doubleValue();
        FontExtrusion extrusion = new FontExtrusion(new Line2D.Double(0.0, 0.0,
                depth, 0.0));

        Font3D font3D = new Font3D(new Font(fontFamilyValue, Font.PLAIN, 1),
                extrusion);

        _textGeometry = new Text3D(font3D, textValue);
        _textGeometry.setCapability(Text3D.ALLOW_STRING_WRITE);

        String alignmentValue = alignment.stringValue();
        int align = Text3D.ALIGN_CENTER;

        if (alignmentValue.equals("first")) {
            align = Text3D.ALIGN_FIRST;
        } else if (alignmentValue.equals("last")) {
            align = Text3D.ALIGN_LAST;
        }

        _textGeometry.setAlignment(align);

        Shape3D shape = new Shape3D();
        shape.setGeometry(_textGeometry);
        shape.setAppearance(_appearance);

        TransformGroup scaler = new TransformGroup();
        scaler.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
        _scaleTransform = new Transform3D();

        float size = (float) ((DoubleToken) fontSize.getToken()).doubleValue();
        _scaleTransform.setScale(new Vector3d(size, size, 1.0f));
        scaler.setTransform(_scaleTransform);
        scaler.addChild(shape);
        _containedNode = scaler;

        if (_changesAllowedNow) {
            _textGeometry.setCapability(Text3D.ALLOW_FONT3D_WRITE);
            _textGeometry.setCapability(Text3D.ALLOW_ALIGNMENT_WRITE);
        }
    }

    /** Return the encapsulated Java3D node of this 3D actor. The encapsulated
     *  node for this actor is a Java3D Text3D
     *
     *  @return the Java3D Text3D
     */
    @Override
    protected Node _getNodeObject() {
        return _containedNode;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The node. */
    private Node _containedNode;

    /** If changes to the font size are allowed, this is the transform
     *  that applies them.
     */
    private Transform3D _scaleTransform;

    /** The text geometry. */
    private Text3D _textGeometry;
}
