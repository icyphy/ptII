/* An attribute for a visible text annotation.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.vergil.kernel.attributes;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.vergil.icon.TextIcon;

//////////////////////////////////////////////////////////////////////////
//// TextAttribute
/**
This is an attribute that is rendered as text annotation.
<p>
@author Edward A. Lee
@version $Id$
*/
public class TextAttribute extends Attribute {

    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TextAttribute(NamedObj container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // Hide the name.
        new Attribute(this, "_hideName");

        _icon = new TextIcon(this, "_icon");
        _icon.setPersistent(false);
        
        // Don't use StringParameter here because variable
        // substitution would be strange.
        text = new StringAttribute(this, "text");
        text.setExpression("Double click to edit text.");
        TextStyle style = new TextStyle(text, "_style");
        style.height.setExpression("20");
        style.width.setExpression("80");
        
        textSize = new Parameter(this, "textSize");
        textSize.setExpression("14");
        textSize.setTypeEquals(BaseType.INT);
        textSize.addChoice("9");
        textSize.addChoice("10");
        textSize.addChoice("11");
        textSize.addChoice("12");
        textSize.addChoice("14");
        textSize.addChoice("18");
        textSize.addChoice("24");
        textSize.addChoice("32");
        
        textColor = new ColorAttribute(this, "textColor");
        textColor.setExpression("{0.0, 0.0, 1.0, 1.0}");

        // Get font family names from the Font class in Java.
        // This includes logical font names, per Font class in Java:
        // Dialog, DialogInput, Monospaced, Serif, SansSerif, or Symbol.
        fontFamily = new StringParameter(this, "fontFamily");
        fontFamily.setExpression("SansSerif");
        String[] families = GraphicsEnvironment
            .getLocalGraphicsEnvironment()
            .getAvailableFontFamilyNames();
        for(int i = 0; i < families.length; i++) {
            fontFamily.addChoice(families[i]);
        }
        
        bold = new Parameter(this, "bold");
        bold.setExpression("false");
        bold.setTypeEquals(BaseType.BOOLEAN);

        italic = new Parameter(this, "italic");
        italic.setExpression("false");
        italic.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** A boolean indicating whether the font should be bold.
     *  This defaults to false.
     */
    public Parameter bold;
    
    /** The font family. This is a string that defaults to "SansSerif".
     */
    public StringParameter fontFamily;

    /** A boolean indicating whether the font should be italic.
     *  This defaults to false.
     */
    public Parameter italic;
    
    /** The text.  This is a string that defaults to
     *  "Double click to edit text."
     */
    public StringAttribute text;

    /** The text color.  This is a string representing an array with
     *  four elements, red, green, blue, and alpha, where alpha is
     *  transparency. The default is "{0.0, 0.0, 0.0, 1.0}", which
     *  represents an opaque black.
     */
    public ColorAttribute textColor;

    /** The text size.  This is an int that defaults to 14.
     */
    public Parameter textSize;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a changes in the attributes by changing the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if ((attribute == fontFamily || attribute == textSize
                || attribute == bold || attribute == italic)
                && !_inAttributeChanged) {
            try {
                // Prevent redundant actions here... When we evaluate the
                // _other_ atribute here (whichever one did _not_ trigger
                // this call, it will likely trigger another call to
                // attributeChanged(), which will result in this action
                // being performed twice.
                _inAttributeChanged = true;
                int sizeValue = ((IntToken) textSize.getToken()).intValue();
                String familyValue = fontFamily.stringValue();
                int styleValue = Font.PLAIN;
                if (((BooleanToken)bold.getToken()).booleanValue()) {
                    styleValue = styleValue | Font.BOLD;
                }
                if (((BooleanToken)italic.getToken()).booleanValue()) {
                    styleValue = styleValue | Font.ITALIC;
                }
                Font fontValue = new Font(familyValue, styleValue, sizeValue);
                _icon.setFont(fontValue);
            } finally {
                _inAttributeChanged = false;
            }
        } else if (attribute == textColor) {
            Color colorValue = textColor.asColor();
            _icon.setTextColor(colorValue);
        } else if (attribute == text) {
            _icon.setText(text.getExpression());
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       protected members                   ////

    /** The text icon. */
    protected TextIcon _icon;
    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////

    private boolean _inAttributeChanged = false;
}
