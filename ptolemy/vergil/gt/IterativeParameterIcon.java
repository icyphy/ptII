/*

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
package ptolemy.vergil.gt;

import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingConstants;

import ptolemy.actor.gt.IterativeParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.XMLIcon;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.LabelFigure;

//////////////////////////////////////////////////////////////////////////
//// IterativeParameterIcon

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class IterativeParameterIcon extends XMLIcon {

    /**
     *  @param container
     *  @param name
     *  @exception IllegalActionException
     *  @exception NameDuplicationException
     */
    public IterativeParameterIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    /**
     *  @param workspace
     *  @param name
     *  @exception IllegalActionException
     */
    public IterativeParameterIcon(Workspace workspace, String name)
            throws IllegalActionException {
        super(workspace, name);
    }

    @Override
    public Figure createBackgroundFigure() {
        IterativeParameter parameter = (IterativeParameter) getContainer();

        StringBuffer text = new StringBuffer("for (");
        text.append(parameter.getName());
        text.append(" = ");
        text.append(parameter.initial.getExpression());
        text.append("; ");
        text.append(parameter.constraint.getExpression());
        text.append("; ");
        text.append(parameter.getName());
        text.append(" = ");
        text.append(parameter.next.getExpression());
        text.append(')');

        LabelFigure label = new LabelFigure(text.toString(), _LABEL_FONT, 1.0,
                SwingConstants.CENTER);
        label.setFillPaint(Color.blue);
        Rectangle2D bounds = label.getBounds();
        Figure background = new BasicRectangle(bounds.getMinX() - 5,
                bounds.getMinY() - 5, bounds.getWidth() + 10,
                bounds.getHeight() + 10, _BACKGROUND_COLOR, 1);
        CompositeFigure figure = new CompositeFigure(background);
        figure.add(label);
        return figure;
    }

    private static final Color _BACKGROUND_COLOR = new Color(1.0f, 1.0f, 0.8f,
            0.8f);

    private static final Font _LABEL_FONT = new Font("SansSerif", Font.PLAIN,
            12);
}
