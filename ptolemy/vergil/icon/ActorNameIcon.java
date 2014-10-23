/* An icon that renders the value of an attribute of the container.

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
package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import ptolemy.data.expr.SingletonParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;

///////////////////////////////////////////////////////////////////
//// ActorNameIcon

/**
 An icon that displays the parameter <i>_displayedName</i>.

 This differs from {@link ptolemy.vergil.icon.BoxedValueIcon} in that
 it uses the _displayedName parameter and hides the name of the
 container.

 @author Elaine Cheong
 @version $Id$
 @since Ptolemy II 5.2
 @Pt.ProposedRating Red (celaine)
 @Pt.AcceptedRating Red (celaine)
 */
public class ActorNameIcon extends BoxedValueIcon {

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
    public ActorNameIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        // Set the name of the parameter that will contain the displayed name.
        this.attributeName.setExpression("_displayedName");

        displayWidth.setExpression("20");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new background figure.  This overrides the base class
     *  to with a different display height and hides the name of the
     *  container if it exists.
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
            height = Math.floor(stringBounds.getHeight()) + 20;

            // If the container name exists, hide it.
            if (displayString.trim().length() > 0) {
                try {
                    SingletonParameter hideName = new SingletonParameter(
                            this.getContainer(), "_hideName");
                    hideName.setExpression("true");
                } catch (Exception ex) {
                    if (!_printedMessage) {
                        _printedMessage = true;
                        System.out
                        .println("Failed to create the background figure. "
                                + ex);
                    }
                }
            }
        }

        return new BasicRectangle(0, 0, width, height, Color.white, 1);
    }

    /** True if we have printed a message about failing to create
     *  background figures.
     */
    private boolean _printedMessage;
}
