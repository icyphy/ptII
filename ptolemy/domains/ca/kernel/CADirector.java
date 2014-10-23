/*
 CADirector

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

package ptolemy.domains.ca.kernel;

import java.util.Iterator;
import java.util.Random;

import ptolemy.actor.Actor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.SetVariable;
import ptolemy.actor.lib.gui.MatrixViewer;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ca.lib.CA2DConvolution;
import ptolemy.domains.ca.lib.gui.CAViewer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.Workspace;

/**
 * A Cellular Automata director.
 * <p>
 * The CADirector is to be used in conjunction with the CA2DConvolution Actor. To use
 * this director, it is highly recommended to start with one of the examples.
 * The director contains a matrix. At each iteration, every entry in the matrix
 * is updated according to the CA2DConvolution actor. This functionality can be useful when
 * simulating cellular automata or other grid-based phenomena.
 * </p>
 * <p>
 * This director assumes the following parameters are added to the canvas:
 * </p>
 * <p>
 * <i>xLocation</i>: the xLocation in the grid (the 2D matrix) that is
 * currently being updated.
 * </p>
 * <p>
 * <i>yLocation</i>: the yLocation in the grid (the 2D matrix) that is
 * currently being updated.
 * </p>
 * <p>
 * <i>currentValue</i>: the currentValue in the grid at location (xLocation,
 * yLocation).
 * </p>
 * <p>
 * <i>newValue</i>: the updated value in the grid at location (xLocation,
 * yLocation).
 * </p>
 * <p>
 * <i>neighbor1</i>: the value of the top-left neighbor of grid location
 * (xLocation, yLocation).
 * </p>
 * <p>
 * <i>neighbor2</i>: the value of the top neighbor of grid location
 * (xLocation, yLocation).
 * </p>
 * <p>
 * <i>neighbor3</i>: the value of the top-right neighbor of grid location
 * (xLocation, yLocation).
 * </p>
 * <p>
 * <i>neighbor4</i>: the value of the left neighbor of grid location
 * (xLocation, yLocation).
 * </p>
 * <p>
 * <i>neighbor5</i>: the value of the right neighbor of grid location
 * (xLocation, yLocation).
 * </p>
 * <p>
 * <i>neighbor6</i>: the value of the bottom-left neighbor of grid location
 * (xLocation, yLocation).
 * </p>
 * <p>
 * <i>neighbor7</i>: the value of the bottom neighbor of grid location
 * (xLocation, yLocation).
 * </p>
 * <p>
 * <i>neighbor8</i>: the value of the bottom-right neighbor of grid location
 * (xLocation, yLocation).
 * </p>
 * <p>
 * The director sets the variables for each entry in the matrix every iteration.
 * The number of high-level <i>iterations</i> (that scan the entire matrix once)
 * can be set as a parameter in the director. Other parameters include:
 * <i>delay</i>, the time delay between iterations; <i>randomize matrix</i>,
 * which determines if the matrix will be populated randomly; <i>matrix size</i>,
 * the dimension of the square matrix; and <i>initial values</i>, the initial
 * values of the matrix with all values not explicitly defined set to 0.0.
 * </p>
 *
 * <p>
 * See also the documentation on the {@link ptolemy.domains.ca.lib.CA2DConvolution} actor
 * and the {@link ptolemy.domains.ca.lib.gui.CAViewer} actor.
 * </p>
 *
 *  @author  Zach Ezzell, Contributor: Christopher Brooks
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (cxh)
 *  @Pt.AcceptedRating Red (cxh)
 */
public class CADirector extends Director {

    /**
     * Construct a new CADirector.
     *
     * @param container The container.
     * @param name The name of the director.
     * @exception IllegalActionException If the name has a period in
     * it, or the director is not compatible with the specified
     * container.
     * @exception NameDuplicationException If the container already
     * contains an entity with the specified name.
     */
    public CADirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _initParameters();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /**
     *  The delay time between iterations in seconds.  The initial default
     *  value is a double with value 0.5, indicating a delay of half a second
     *  between iterations.
     */
    protected Parameter delay;

    /**
     * The initial matrix parameter.
     */
    protected Parameter initialMatrix;

    /**
     * The iterations parameter.
     */
    protected Parameter iterations;

    /**
     *  True if the matrix will be populated with random data.  The default
     *  value is false, indicating that all values in the matrix will be
     *  initially set to 0.0.
     */
    protected Parameter random;

    /**
     * An integer representing the size of the square matrix.  The initial
     * value is 10, indicating a 10 x 10 matrix.
     */
    protected Parameter size;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new object.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        CADirector newObject = (CADirector) super.clone(workspace);
        newObject.delay = (Parameter) newObject.getAttribute("delay");
        newObject.initialMatrix = (Parameter) newObject
                .getAttribute("initialMatrix");
        newObject.iterations = (Parameter) newObject.getAttribute("iterations");
        newObject.random = (Parameter) newObject.getAttribute("random");
        newObject.size = (Parameter) newObject.getAttribute("size");
        return newObject;
    }

    /**
     * Iterate the relevant actors on the canvas for a single grid location.
     *
     * <p>This method is invoked once per location in the matrix per
     * iteration (i.e., Matrix size x iteration count).</p>
     *
     * <p>This method sets the appropriate values on the canvas and
     * iterates the appropriate actors (e.g., the CA2DConvolution actor) and
     * updates grid values accordingly.</p>
     *
     * @exception IllegalActionException If an actor is unable to iterate().
     */
    @Override
    public void fire() throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof TypedCompositeActor) {
            TypedCompositeActor composite = (TypedCompositeActor) container;
            Variable vxl = (Variable) composite.getAttribute("xLocation");
            Variable vyl = (Variable) composite.getAttribute("yLocation");
            Variable vcv = (Variable) composite.getAttribute("currentValue");
            Variable vn1 = (Variable) composite.getAttribute("neighbor1");
            Variable vn2 = (Variable) composite.getAttribute("neighbor2");
            Variable vn3 = (Variable) composite.getAttribute("neighbor3");
            Variable vn4 = (Variable) composite.getAttribute("neighbor4");
            Variable vn5 = (Variable) composite.getAttribute("neighbor5");
            Variable vn6 = (Variable) composite.getAttribute("neighbor6");
            Variable vn7 = (Variable) composite.getAttribute("neighbor7");
            Variable vn8 = (Variable) composite.getAttribute("neighbor8");

            int i = _currentY;
            int j = _currentX;

            vxl.setToken(new IntToken(i));
            vyl.setToken(new IntToken(j));
            vcv.setToken(new DoubleToken(_cells[i][j]));

            int previousRow = i - 1;
            if (previousRow < 0) {
                previousRow = _matrixSize - 1;
            }

            int nextRow = i + 1;
            if (nextRow >= _matrixSize) {
                nextRow = 0;
            }

            int previousColumn = j - 1;
            if (previousColumn < 0) {
                previousColumn = _matrixSize - 1;
            }

            int nextColumn = j + 1;
            if (nextColumn >= _matrixSize) {
                nextColumn = 0;
            }

            vn1.setToken(new DoubleToken(_cells[previousRow][previousColumn]));
            vn2.setToken(new DoubleToken(_cells[previousRow][j]));
            vn3.setToken(new DoubleToken(_cells[previousRow][nextColumn]));

            vn4.setToken(new DoubleToken(_cells[i][previousColumn]));
            vn5.setToken(new DoubleToken(_cells[i][nextColumn]));

            vn6.setToken(new DoubleToken(_cells[nextRow][previousColumn]));
            vn7.setToken(new DoubleToken(_cells[nextRow][j]));
            vn8.setToken(new DoubleToken(_cells[nextRow][nextColumn]));

            Actor actorCA2DConvolution = null;
            Actor actorSetVariable = null;

            Iterator actorList = composite.entityList().iterator();
            while (actorList.hasNext()) {
                Actor actor = (Actor) actorList.next();
                if (actor instanceof CA2DConvolution) {
                    actorCA2DConvolution = actor;
                } else if (actor instanceof SetVariable) {
                    actorSetVariable = actor;
                } else if (actor instanceof MatrixViewer) {
                } else if (actor instanceof Const) {
                    Const c = (Const) actor;
                    Variable v = c.value;
                    if (!(v.getToken() instanceof DoubleMatrixToken)) {
                        actor.iterate(1);
                    }
                }
            }
            actorCA2DConvolution.iterate(1);
            actorSetVariable.iterate(1);
        }
    }

    /**
     * This method performs the initialization step.  This includes
     * creating the matrix according to the user-defined parameters.
     *
     * @exception IllegalActionException
     *              If exception occurs in base class.
     *
     */
    @Override
    public void preinitialize() throws IllegalActionException {

        _matrixSize = ((IntToken) size.getToken()).intValue();
        double[][] initial = new double[_matrixSize][_matrixSize];
        boolean isRandom = ((BooleanToken) random.getToken()).booleanValue();

        if (isRandom) {
            Random generator = new Random();
            for (int i = 0; i < _matrixSize; i++) {
                for (int j = 0; j < _matrixSize; j++) {
                    double d = generator.nextDouble();
                    if (d > 0.5) {
                        initial[i][j] = 1.0;
                    } else {
                        initial[i][j] = 0.0;
                    }
                }
            }
        } else {
            for (int i = 0; i < _matrixSize; i++) {
                for (int j = 0; j < _matrixSize; j++) {
                    initial[i][j] = 0.0;
                }
            }

            double[][] valueMatrix = ((DoubleMatrixToken) initialMatrix
                    .getToken()).doubleMatrix();

            for (double[] element : valueMatrix) {
                if (element.length == 3) {
                    int xi = (int) element[0];
                    int yi = (int) element[1];
                    initial[yi][xi] = element[2];
                }
            }
        }

        _cells = new double[_matrixSize][_matrixSize];
        _newCells = new double[_matrixSize][_matrixSize];

        for (int i = 0; i < _matrixSize; i++) {
            for (int j = 0; j < _matrixSize; j++) {
                _cells[i][j] = initial[i][j];
                _newCells[i][j] = initial[i][j];
            }
        }

        Variable matrixVar = (Variable) ((TypedCompositeActor) getContainer())
                .getAttribute("matrix");
        matrixVar.setToken(new DoubleMatrixToken(_cells));

        _currentIteration = ((IntToken) iterations.getToken()).intValue();

        _currentX = 0;
        _currentY = 0;

        _setInitMatrix();
        super.preinitialize();
    }

    /**
     * Update the current x and yLocations of the grid.  It
     * also updates the iteration count when necessary.  If all matrix _cells
     * have been updated for this iteration, then _iterate() is called.
     *
     * @exception IllegalActionException If an exception occurs in the
     * base class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        super.postfire();

        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Variable matrixVar = (Variable) container.getAttribute("matrix");
        matrixVar.setToken(new DoubleMatrixToken(_cells));

        Variable vnv = (Variable) container.getAttribute("newValue");
        DoubleToken dt = (DoubleToken) vnv.getToken();
        _newCells[_currentY][_currentX] = dt.doubleValue();

        if (++_currentX == _matrixSize) {
            _currentX = 0;
            if (++_currentY == _matrixSize) {
                return _iterate();
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Initialize the parameters.
     *
     * @exception IllegalActionException If the parameters names are
     * invalid.
     * @exception NameDuplicationException If parameters already exist
     * with a specified name.
     */
    protected void _initParameters() throws IllegalActionException,
    NameDuplicationException {

        // FIXME: we need an attributeChanged() method.  What happens
        // if one of these parameters changes while the model is running?

        // Note that the Java variable name and the name in the second
        // parameter must match or this actor will not clone properly and
        // will not be useful in actor oriented classes.  To have the
        // names not match, see NamedObj.setDisplayName().
        iterations = new Parameter(this, "iterations");
        iterations.setExpression("10");
        iterations.setTypeEquals(BaseType.INT);

        delay = new Parameter(this, "delay");
        delay.setExpression("0.5");
        delay.setTypeEquals(BaseType.DOUBLE);

        random = new Parameter(this, "random");
        random.setTypeEquals(BaseType.BOOLEAN);
        random.setExpression("false");

        size = new Parameter(this, "size");
        size.setTypeEquals(BaseType.INT);
        size.setExpression("10");

        // Sample matrix values
        double[][] doubleArray = new double[1][3];
        doubleArray[0][0] = 0;
        doubleArray[0][1] = 0;
        doubleArray[0][2] = 0.0;

        initialMatrix = new Parameter(this, "initialMatrix",
                new DoubleMatrixToken(doubleArray));
    }

    /**
     * Enforce the user-defined delay, call _showMatrix(), and check
     * if the iteration limit has been reached.
     * This method is called every time the entire matrix has been updated.
     *
     * @return boolean false is the iteration limit has been reached and
     * execution should stop, true otherwise.
     * @exception IllegalActionException If the matrix cannot be shown.
     */
    protected boolean _iterate() throws IllegalActionException {
        try {
            Thread.sleep((int) (1000.0 * ((DoubleToken) delay.getToken())
                    .doubleValue()));
        } catch (Throwable throwable) {
            // Ignored.
        }

        for (int i = 0; i < _matrixSize; i++) {
            for (int j = 0; j < _matrixSize; j++) {
                _cells[i][j] = _newCells[i][j];
            }
        }
        try {
            _showMatrix();
        } catch (Exception ex) {
            throw new IllegalActionException(this, ex,
                    "Failed to show the matrix.");
        }

        _currentIteration--;
        _currentX = _currentY = 0;
        if (_currentIteration <= 0) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Set the initial matrix for the CAViewer (the actor that visualizes the grid).
     */
    protected void _setInitMatrix() {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Iterator actorList = container.entityList().iterator();

        //Actor actorMatrixViewer = null;
        //Actor actorConst = null;

        // FIXME: iterating through the actors by type
        // seems odd.  What if we add a new actor type?
        while (actorList.hasNext()) {
            Actor actor = (Actor) actorList.next();
            if (actor instanceof CAViewer) {
                CAViewer caViewer = (CAViewer) actor;
                caViewer.setMatrix(_cells);
            }
        }
    }

    /**
     * Set the current matrix for any MatrixViewer, Const with a
     * DoubleMatrixToken, or CAViewer actors.
     * @exception IllegalActionException If thrown while reading a token or iterating
     * the actors
     */
    protected void _showMatrix() throws IllegalActionException {
        // FIXME: This is kind of an odd way to iterate.  It only
        // iterates certain actors, which means that if we add actors
        // to this model of computation, we need to edit this file.
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Iterator actorList = container.entityList().iterator();
        Actor actorMatrixViewer = null;
        Actor actorConst = null;
        while (actorList.hasNext()) {
            Actor actor = (Actor) actorList.next();
            if (actor instanceof MatrixViewer) {
                actorMatrixViewer = actor;
            } else if (actor instanceof Const) {
                Const c = (Const) actor;
                Variable v = c.value;
                if (v.getToken() instanceof DoubleMatrixToken) {
                    actorConst = actor;
                }
            } else if (actor instanceof CAViewer) {
                CAViewer caViewer = (CAViewer) actor;
                caViewer.setMatrix(_cells);
                caViewer.iterate(1);
            }
        }
        if (actorConst != null) {
            actorConst.iterate(1);
        }
        if (actorMatrixViewer != null) {
            actorMatrixViewer.iterate(1);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /**
     * The current matrix.
     */
    private double[][] _cells;

    /**
     * The current iteration of the director.
     */
    private int _currentIteration;

    /**
     * The xLocation of the current matrix cell being updated.
     */
    private int _currentX;

    /**
     * The yLocation of the current matrix cell being updated.
     */
    private int _currentY;

    /**
     *  The size of one dimension of the square matrix.
     */
    private int _matrixSize;

    /**
     *  A temporary matrix to hold the updated values.
     */
    private double[][] _newCells;
}
