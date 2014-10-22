/* Cellular Automata Viewer

 Copyright (c) 2010-2014 The University of Florida

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

import java.awt.Container;

import javax.swing.JPanel;

import ptolemy.actor.gui.AbstractPlaceableActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.TokenEffigy;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * Cellular Automata Viewer.
 *
 * <p>The CAViewer is a display actor to be used in conjunction with
 * the CADirector.  The CAViewer can be placed on the canvas and will
 * render the grid as defined by the Director's parameters and
 * possibly the output of the CA2DConvolution actor.  This actor is
 * the currently the only way to visualize cellular automata or other
 * 2D grid based phenomena employing the CADirectory.</p>
 *
 *  @author  Zach Ezzell, Contributor: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class CAViewer extends AbstractPlaceableActor {

    /**
     * Construct a new CAViewer.
     *
     * @param entity The container
     * @param name The name of the CAViewer.
     * @exception IllegalActionException If the name has a period in it.
     * @exception NameDuplicationException If the container already
     * contains an entity with the specified name.
     */
    public CAViewer(CompositeEntity entity, String name)
            throws IllegalActionException, NameDuplicationException {
        super(entity, name);
        matrix = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     *   Initialize the viewer.
     *
     *   @exception IllegalActionException If the superclass throws
     *   the exception.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _initialized = false;
    }

    /**
     * Specify the container in which the data should be displayed.
     *
     *  @param container The container in which to place the pane that
     *  will contain the CATableau or null to specify no container.
     */
    @Override
    public void place(Container container) {

        _container = container;

        if (_container != null && _pane != null) {
            _container.remove(_pane);
            _container = null;
        }

        if (_frame != null) {
            _frame.dispose();
            _frame = null;
        }

        if (container == null) {
            try {
                _tableau.setContainer(null);
            } catch (KernelException ex) {
                throw new InternalErrorException(ex);
            }
            _tableau = null;
            _effigy = null;
            _pane = null;
        }
    }

    /**
     * Show the tableau containing the visualization of the matrix.
     *
     * @exception IllegalActionException If the base class throws an
     * exception.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        super.postfire();

        if (!_initialized) {
            _initialized = true;
            _openWindow();
        }
        if (_tableau != null) {
            _frame.setVisible(true);
            _frame.toFront();
        }

        return false;
    }

    /**
     *  Set the matrix to be rendered as a grid.
     *
     * @param matrix The matrix to be rendered.
     */
    public void setMatrix(double[][] matrix) {
        this.matrix = matrix;
        if (_tableau != null) {
            _tableau.setMatrix(matrix);
            _tableau.show();
            _frame.repaint();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Open the window, first initializing it if necessary.  It is
     * modeled after _openWindow() is the Display actor.
     *
     * @exception IllegalActionException If the top level effigy
     * cannot be found or the token effigy cannot be created.
     */
    protected void _openWindow() throws IllegalActionException {
        if (_container == null) {
            Effigy containerEffigy = Configuration.findEffigy(toplevel());

            if (containerEffigy == null) {
                throw new IllegalActionException(this,
                        "Cannot find effigy for top level: "
                                + toplevel().getFullName());
            }
            try {
                if (_tableau != null) {
                    _effigy.clear();
                    _tableau.show();
                } else {
                    _effigy = new TokenEffigy(containerEffigy,
                            containerEffigy.uniqueName("tokenEffigy"));
                    _effigy.identifier.setExpression(getFullName());
                    _tableau = new CATableau(_effigy, "tokenTableau");
                    _tableau.setMatrix(matrix);
                    _frame = _tableau.mainFrame;
                    setFrame(_frame);
                    _frame.pack();
                    _tableau.show();
                }
            } catch (Throwable throwable) {
                throw new IllegalActionException(this, null, throwable,
                        "Error creating effigy and tableau");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    /**
     * True if the actor has been initialized.
     */
    protected boolean _initialized;

    /**
     * The matrix to be rendered as a grid.
     */
    protected double[][] matrix;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The Java Container that contains the visualization.
     */
    private Container _container = null;

    /**
     * The generic effigy to be used for the window.
     */
    private TokenEffigy _effigy = null;

    /**
     *  The JPanel to be used in the Container.
     */
    private JPanel _pane = null;

    /**
     * The custom tableau for the window.
     */
    private CATableau _tableau = null;
}
