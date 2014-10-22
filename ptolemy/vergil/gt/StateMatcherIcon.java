/*

@Copyright (c) 2007-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

package ptolemy.vergil.gt;

import java.awt.Color;
import java.util.Iterator;

import javax.swing.Icon;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.modal.StateIcon;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;
import diva.canvas.toolbox.RoundedRectangle;
import diva.gui.toolbox.FigureIcon;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class StateMatcherIcon extends StateIcon {

    /**
     * @param container
     * @param name
     * @exception NameDuplicationException
     * @exception IllegalActionException
     */
    public StateMatcherIcon(NamedObj container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    @Override
    public Figure createBackgroundFigure() {
        _spacingValue = 0.0;
        Figure figure = super.createBackgroundFigure();
        if (figure instanceof RoundedRectangle) {
            ((RoundedRectangle) figure).setFillPaint(Color.LIGHT_GRAY);
        } else if (figure instanceof BasicRectangle) {
            ((BasicRectangle) figure).setFillPaint(Color.LIGHT_GRAY);
        }
        return figure;
    }

    @Override
    public Figure createFigure() {
        CompositeFigure figure = (CompositeFigure) super.createFigure();
        Iterator<?> subfigures = figure.figures();
        while (subfigures.hasNext()) {
            Figure subfigure = (Figure) subfigures.next();
            if (subfigure instanceof LabelFigure) {
                ((LabelFigure) subfigure).setFillPaint(Color.RED);
            }
        }
        return figure;
    }

    @Override
    public Icon createIcon() {
        if (_iconCache != null) {
            return _iconCache;
        }

        Figure figure = new RoundedRectangle(0, 0, 20, 10, Color.LIGHT_GRAY,
                2.0f, 5.0, 5.0);
        _iconCache = new FigureIcon(figure, 20, 15);
        return _iconCache;
    }
}
