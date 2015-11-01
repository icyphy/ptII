/* An icon for a Ptera time advance event.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.vergil.ptera;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import ptolemy.domains.ptera.kernel.TimeAdvanceEvent;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.RoundedRectangle;

///////////////////////////////////////////////////////////////////
//// TimeAdvanceEventIcon

/**
  An icon for a Ptera time advance event.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TimeAdvanceEventIcon extends EventIcon {

    /** Create a new icon with the given name in the given container.
     *  The container is required to implement Settable, or an exception
     *  will be thrown.
     *  @param container The container for this attribute.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If thrown by the parent
     *  class or while setting an attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TimeAdvanceEventIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a Figure.
     *  @return The figure.
     */
    @Override
    public Figure createFigure() {
        CompositeFigure figure = (CompositeFigure) super.createFigure();
        Rectangle2D bounds = figure.getBounds();

        TimeAdvanceEvent event = (TimeAdvanceEvent) getContainer();
        LabelFigure label = new LabelFigure(event.getTimeAdvanceText(), _FONT,
                2.0, SwingConstants.CENTER);
        Rectangle2D labelBounds = label.getBounds();
        RoundedRectangle background = new RoundedRectangle(0, 0,
                labelBounds.getWidth() + 12, labelBounds.getHeight() + 6,
                Color.white, 1.0f, 6.0f, 6.0f);
        Rectangle2D backgroundBounds = background.getBounds();

        figure.add(background);
        figure.add(label);

        background.translate(bounds.getMaxX() - backgroundBounds.getCenterX()
                - 5, bounds.getMinY() - backgroundBounds.getCenterY());
        label.translateTo(bounds.getMaxX() - 5, bounds.getMinY());
        return figure;
    }

    private static final Font _FONT = new Font("SansSerif", Font.PLAIN, 9);
}
