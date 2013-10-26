/*
 CATableau

 Copyright (c) 2010-2013 The University of Florida

 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF FLORIDA BE LIABLE TO ANY PARTY
 FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
 ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 THE UNIVERSITY OF FLORIDA HAS BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF FLORIDA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
 PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
 FLORIDA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

  PT_COPYRIGHT_VERSION_2
  COPYRIGHTENDKEY
 */

package ptolemy.domains.ca.lib.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.List;

import javax.swing.JPanel;

import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * This class is used by the CAViewer to render a
 * two-dimensional grid of cells that are either on or off.
 *
 *  @author  Zach Ezzell, Contributor: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class CATableau extends Tableau {

    /**
     * Construct a new tableau for the model represented by the given effigy.
     *
     * @param container The container.
     * @param name The name of the tableau.
     * @exception IllegalActionException If the container does not
     * accept this entity (this should not occur).
     * @exception NameDuplicationException If the name coincides with
     * an attribute already in the container.
     */
    public CATableau(Effigy container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        createFrame(null);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * In this class, do nothing because the CADirector communicates
     * with the CAViewer directly.
     *
     * @param token The token to append.
     * @exception IllegalActionException If the token is not
     * acceptable (not thrown in this base class).
     */
    public void append(Token token) throws IllegalActionException {
    }

    /**
     * In this class, do nothing because the CADirector communicates
     * with the CAViewer directly.
     *
     * @param list A list of tokens.
     * @exception IllegalActionException If the tokens are not
     * acceptable (not thrown in this base class).
     */
    public void append(List list) throws IllegalActionException {
        if (mainFrame != null) {
            // FIXME: what happens here?
        }
    }

    /** This functions sets the matrix for the CAMatrixViewer to display.
     *
     * @param matrix The matrix to display.
     */
    public void setMatrix(double[][] matrix) {
        if (matrixViewer != null) {
            matrixViewer.setMatrix(matrix);
        }
    }

    /**
     * Create a frame to hold the CAMatrixViewer.
     *
     * @param frame The frame to use, or null if none is specified.
     *
     * @exception IllegalActionException If the frame cannot be created.
     */
    public void createFrame(TableauFrame frame) throws IllegalActionException {
        if (frame == null) {
            frame = new TableauFrame(this, null);
        }
        setFrame(frame);
        frame.setTableau(this);
        mainFrame = frame;
        JPanel panel = new JPanel();
        matrixViewer = new CAMatrixViewer();
        panel.setBackground(Color.BLACK);
        panel.add(matrixViewer);
        mainFrame.getContentPane().add(panel, BorderLayout.CENTER);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * The TableauFrame to hold the CAMatrixViewer.
     */
    public TableauFrame mainFrame;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     *  The CAMatrixViewer that renders the two-dimensional grid.
     */
    private CAMatrixViewer matrixViewer;
}

/**
 * The CAMatrixViewer is a type of Java Component that renders a grid of
 * cells within a matrix (a two-dimensional array of doubles).  A grid
 * cell is rendered as 'on' if the value of the cell is greater than zero
 * and 'off' otherwise. This class is not public and currently only used
 * as part of a CATableau which, in turn, is used as part of a CADisplay.
 *
 *  @author  Zach Ezzell, Contributor: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
class CAMatrixViewer extends Component {

    /**
     * Construct a new CAMatrixViewer and initialize the matrix to
     * size [1][1] and set the only value to 1.0;
     */
    public CAMatrixViewer() {
        super();
        matrix = new double[1][1];
        matrix[0][0] = 1.0;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the minimum size of this component.
     * @return The minimum size.
     */
    public Dimension getMinimumSize() {
        int minimumWidth = 200;
        int minimumHeight = 200;
        Container container = this.getParent();
        if (container != null) {
            minimumWidth = container.getWidth() - 10;
            minimumHeight = container.getHeight() - 10;
            if (minimumWidth < minimumHeight) {
                minimumHeight = minimumWidth;
            } else {
                minimumWidth = minimumHeight;
            }
        }
        return new Dimension(minimumWidth, minimumHeight);
    }

    /**
     * Return the preferred size.
     * The preferred size of this component is the minimum
     * size.
     * @return the preferred size.
     */
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    /**
     * Paint the current matrix as a grid.  If the
     * value in a cell is greater than 0.0, then the cell is
     * "filled-in."
     *
     * @param graphics The java Graphics object that is required to
     * paint the Component.
     *
     */
    public void paint(Graphics graphics) {

        super.paint(graphics);

        Color gray = new Color(100, 100, 100);
        Color darkGray = new Color(40, 40, 40);
        Color shadow = new Color(120, 120, 120);
        Color cellColor = new Color(225, 225, 225);
        graphics.setColor(gray);
        graphics.fillRect(0, 0, this.getWidth(), this.getHeight());

        if (matrix != null) {

            int rowCount = this.matrix.length;
            int columnCount = this.matrix[0].length;

            float rowWidth = (float) this.getWidth() / (float) rowCount;
            float columnWidth = (float) this.getHeight() / (float) columnCount;

            int borderSize = (int) (rowWidth / 6.0);
            // create shadow trianlge
            int[] xPoints = new int[] { 0, (int) columnWidth, (int) columnWidth };
            int[] yPoints = new int[] { (int) rowWidth, 0, (int) rowWidth };

            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    int x1 = (int) (j * columnWidth);
                    int y1 = (int) (i * rowWidth);

                    int x2 = (int) columnWidth;
                    int y2 = (int) rowWidth;
                    if (matrix[i][j] > 0.0) {

                        graphics.translate(x1, y1);

                        graphics.setColor(Color.WHITE);
                        graphics.fillRect(0, 0, x2, y2);
                        graphics.setColor(shadow);
                        graphics.fillPolygon(xPoints, yPoints, 3);
                        graphics.setColor(cellColor);
                        graphics.fillRect(borderSize, borderSize, x2
                                - borderSize * 2, y2 - borderSize * 2);

                        graphics.translate(-x1, -y1);
                    }
                }
            }

            graphics.setColor(darkGray);
            graphics.drawRect(0, 0, this.getWidth() - 1, this.getHeight() - 1);
            for (int i = 1; i <= columnCount; i++) {
                int x = (int) (i * columnWidth);
                graphics.drawLine(x, 0, x, this.getHeight());
            }

            for (int i = 1; i <= rowCount; i++) {
                int y = (int) (i * rowWidth);
                graphics.drawLine(0, y, this.getWidth(), y);
            }
        }
    }

    /**
     * Set the matrix to display.
     *
     * @param matrix The matrix to display.
     *
     */
    public void setMatrix(double[][] matrix) {
        this.matrix = matrix;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The matrix to display.
     */
    private double[][] matrix;
}
