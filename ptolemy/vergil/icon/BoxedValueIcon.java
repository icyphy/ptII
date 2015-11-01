/* An icon that renders the value of an attribute of the container.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.vergil.icon;

import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;

///////////////////////////////////////////////////////////////////
//// BoxedValueIcon

/**
 An icon that displays the value of an attribute of the container in a box
 that resizes according to the width of the attribute value.

 <p>If the value is long, then the value is truncated and ends with "...".
 See {@link ptolemy.util.StringUtilities#truncateString(String, int, int)}.
 This is done so as to avoid Consts with overly long icons.</p>

 <p>The attribute is assumed to be an instance of Settable, and its name
 is given by the parameter <i>attributeName</i>.</p>

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class BoxedValueIcon extends AttributeValueIcon {
    // See http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5607
    // for problems with long Consts.

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public BoxedValueIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        boxColor = new ColorAttribute(this, "boxColor");
        boxColor.setExpression("{1.0, 1.0, 1.0, 1.0}");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Color of the box. This defaults to white. */
    public ColorAttribute boxColor;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new background figure.  This overrides the base class
     *  to draw a box around the value display, where the width of the
     *  box depends on the value.
     *  @return A new figure.
     */
    @Override
    public Figure createBackgroundFigure() {
        String displayString = _displayString();
        double width = 60;
        double height = 30;

        if (displayString != null) {
            // Measure width of the text.  Unfortunately, this
            // requires generating a label figure that we will not use.
            LabelFigure label = new LabelFigure(displayString, _labelFont, 1.0,
                    SwingConstants.CENTER);
            Rectangle2D stringBounds = label.getBounds();

            // NOTE: Padding of 20. Quantize the height so that
            // snap to grid still works.
            width = Math.floor(stringBounds.getWidth()) + 20;
            height = Math.floor(stringBounds.getHeight()) + 10;

            if (width < 40) {
                // Too small. Annoying.
                width = 40;
            }
            if (height < 20) {
                height = 20;
            }
        }

        return new BasicRectangle(0, 0, width, height, boxColor.asColor(), 1);
    }
}
