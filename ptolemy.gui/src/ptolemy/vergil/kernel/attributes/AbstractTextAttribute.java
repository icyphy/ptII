/* An abstract attribute for a visible text annotation.

 Copyright (c) 2004-2014 The Regents of the University of California.
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
package ptolemy.vergil.kernel.attributes;

import java.awt.GraphicsEnvironment;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// AbstractTextAttribute

/**
 An abstract base class for text annotations.
 <p>
 @author Edward A. Lee, Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 4.1
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class AbstractTextAttribute extends VisibleAttribute {
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
    public AbstractTextAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

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

        String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();

        for (String familie : families) {
            fontFamily.addChoice(familie);
        }

        bold = new Parameter(this, "bold");
        bold.setExpression("false");
        bold.setTypeEquals(BaseType.BOOLEAN);

        italic = new Parameter(this, "italic");
        italic.setExpression("false");
        italic.setTypeEquals(BaseType.BOOLEAN);

        anchor = new StringParameter(this, "anchor");
        anchor.setExpression("northwest");
        anchor.addChoice("center");
        anchor.addChoice("east");
        anchor.addChoice("north");
        anchor.addChoice("northeast");
        anchor.addChoice("northwest");
        anchor.addChoice("south");
        anchor.addChoice("southeast");
        anchor.addChoice("southwest");
        anchor.addChoice("west");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Indication of which point of the text should be aligned to the
     *  grid. The possible values are "center", "east", "north",
     *  "northeast", "northwest" (the default), "south", "sountheast",
     *  "southwest", or "west".
     */
    public StringParameter anchor;

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

    /** The text color.  This is a string representing an array with
     *  four elements, red, green, blue, and alpha, where alpha is
     *  transparency. The default is "{0.0, 0.0, 0.0, 1.0}", which
     *  represents an opaque black.
     */
    public ColorAttribute textColor;

    /** The text size.  This is an int that defaults to 14.
     */
    public Parameter textSize;

}
