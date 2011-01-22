/*
 CADirector

 Copyright (c) 2010 The University of Florida

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
import ptolemy.actor.lib.Const;
import ptolemy.actor.lib.gui.Display;
import ptolemy.actor.lib.gui.MatrixViewer;
import ptolemy.actor.lib.SetVariable;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.ca.lib.CA2DConvolution;
import ptolemy.domains.ca.lib.gui.CAViewer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NameDuplicationException;


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
 * <i>xLocation</i>: the x location in the grid (the 2D matrix) that is
 * currently being updated.
 * </p>
 * <p>
 * <i>yLocation</i>: the y location in the grid (the 2D matrix) that is
 * currently being updated.
 * </p>
 * <p>
 * <i>currentValue</i>: the current value in the grid at location (x_current,
 * y_current).
 * </p>
 * <p>
 * <i>newValue</i>: the updated value in the grid at location (x_current,
 * y_current).
 * </p>
 * <p>
 * <i>neighbor1</i>: the value of the top-left neighbor of grid location
 * (current_x, current_y).
 * </p>
 * <p>
 * <i>neighbor_2</i>: the value of the top neighbor of grid location
 * (current_x, current_y).
 * </p>
 * <p>
 * <i>neighbor_3</i>: the value of the top-right neighbor of grid location
 * (current_x, current_y).
 * </p>
 * <p>
 * <i>neighbor_4</i>: the value of the left neighbor of grid location
 * (current_x, current_y).
 * </p>
 * <p>
 * <i>neighbor_5</i>: the value of the right neighbor of grid location
 * (current_x, current_y).
 * </p>
 * <p>
 * <i>neighbor_6</i>: the value of the bottom-left neighbor of grid location
 * (current_x, current_y).
 * </p>
 * <p>
 * <i>neighbor_7</i>: the value of the bottom neighbor of grid location
 * (current_x, current_y).
 * </p>
 * <p>
 * <i>neighbor_8</i>: the value of the bottom-right neighbor of grid location
 * (current_x, current_y).
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
 *  @since Ptolemy II 8.1
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
    ////                      public methods                       ////

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
    public void fire() throws IllegalActionException {
        Nameable container = getContainer();
        if (container instanceof TypedCompositeActor) {
            TypedCompositeActor composite = (TypedCompositeActor) container
;
            Variable vxl = (Variable) composite.getAttribute("x_location");
            Variable vyl = (Variable) composite.getAttribute("y_location");
            Variable vcv = (Variable) composite.getAttribute("current_value");
            Variable vn1 = (Variable) composite.getAttribute("neighbor_1");
            Variable vn2 = (Variable) composite.getAttribute("neighbor_2");
            Variable vn3 = (Variable) composite.getAttribute("neighbor_3");
            Variable vn4 = (Variable) composite.getAttribute("neighbor_4");
            Variable vn5 = (Variable) composite.getAttribute("neighbor_5");
            Variable vn6 = (Variable) composite.getAttribute("neighbor_6");
            Variable vn7 = (Variable) composite.getAttribute("neighbor_7");
            Variable vn8 = (Variable) composite.getAttribute("neighbor_8");

            int i = currentY;
            int j = currentX;

            vxl.setToken(new IntToken(i));
            vyl.setToken(new IntToken(j));
            vcv.setToken(new DoubleToken(cells[i][j]));

            int previousRow = i - 1;
            if (previousRow < 0)
                previousRow = matrixSize - 1;

            int nextRow = i + 1;
            if (nextRow >= matrixSize)
                nextRow = 0;

            int previousColumn = j - 1;
            if (previousColumn < 0)
                previousColumn = matrixSize - 1;

            int nextColumn = j + 1;
            if (nextColumn >= matrixSize)
                nextColumn = 0;

            vn1.setToken(new DoubleToken(cells[previousRow][previousColumn]));
            vn2.setToken(new DoubleToken(cells[previousRow][j]));
            vn3.setToken(new DoubleToken(cells[previousRow][nextColumn]));

            vn4.setToken(new DoubleToken(cells[i][previousColumn]));
            vn5.setToken(new DoubleToken(cells[i][nextColumn]));

            vn6.setToken(new DoubleToken(cells[nextRow][previousColumn]));
            vn7.setToken(new DoubleToken(cells[nextRow][j]));
            vn8.setToken(new DoubleToken(cells[nextRow][nextColumn]));

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
                    Variable v = (Variable) c.value;
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
    public void preinitialize() throws IllegalActionException {

        matrixSize = ((IntToken) paramSize.getToken()).intValue();
        double[][] initialMatrix = new double[matrixSize][matrixSize];
        boolean isRandom = ((BooleanToken) paramRandom.getToken())
                .booleanValue();

        if (isRandom) {
            Random generator = new Random();
            for (int i = 0; i < matrixSize; i++) {
                for (int j = 0; j < matrixSize; j++) {
                    double d = generator.nextDouble();
                    if (d > 0.5) {
                        initialMatrix[i][j] = 1.0;
                    } else {
                        initialMatrix[i][j] = 0.0;
                    }
                }
            }
        } else {
            for (int i = 0; i < matrixSize; i++) {
                for (int j = 0; j < matrixSize; j++) {
                    initialMatrix[i][j] = 0.0;
                }
            }

            double[][] valueMatrix = ((DoubleMatrixToken) paramInitialMatrix
                    .getToken()).doubleMatrix();

            for (int i = 0; i < valueMatrix.length; i++) {
                if (valueMatrix[i].length == 3) {
                    int xi = (int) valueMatrix[i][0];
                    int yi = (int) valueMatrix[i][1];
                    initialMatrix[yi][xi] = valueMatrix[i][2];
                }
            }
        }

        cells = new double[matrixSize][matrixSize];
        newCells = new double[matrixSize][matrixSize];

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                cells[i][j] = initialMatrix[i][j];
                newCells[i][j] = initialMatrix[i][j];
            }
        }

        Variable matrixVar = (Variable) (((TypedCompositeActor) getContainer())
                .getAttribute("matrix"));
        matrixVar.setToken(new DoubleMatrixToken(cells));

        iter = ((IntToken) paramIterations.getToken()).intValue();

        firstFire = true;
        currentX = 0;
        currentY = 0;

        _setInitMatrix();
        super.preinitialize();
    }

    /**
     * Update the current x and y locations of the grid.  It 
     * also updates the iteration count when necessary.  If all matrix cells 
     * have been updated for this iteration, then _iterate() is called.
     * 
     * @exception IllegalActionException If an exception occurs in the
     * base class.
     */
    public boolean postfire() throws IllegalActionException {
        super.postfire();

        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Variable matrixVar = (Variable) container.getAttribute("matrix");
        matrixVar.setToken(new DoubleMatrixToken(cells));

        Variable vnv = (Variable) container.getAttribute("new_value");
        DoubleToken dt = (DoubleToken) vnv.getToken();
        newCells[currentY][currentX] = dt.doubleValue();

        if (++currentX == matrixSize) {
            currentX = 0;
            if (++currentY == matrixSize) {
                return _iterate();
            }
        }
        return true;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                    protected methods                      ////
    
    /**
     * This is a helper method called in the constructor to initialize
     * the parameters.
     * 
     * @exception IllegalActionException
     *                If the parameters names are invalid.
     * @exception NameDuplicationException
     *                If parameters already exist with a specified name.
     */
    protected void _initParameters() throws IllegalActionException,
            NameDuplicationException {

        paramIterations = new Parameter(this, "iterations");
        paramIterations.setExpression("10");
        paramIterations.setTypeEquals(BaseType.INT);

        paramDelay = new Parameter(this, "delay (in sec)");
        paramDelay.setExpression("0.5");
        paramDelay.setTypeEquals(BaseType.DOUBLE);

        paramRandom = new Parameter(this, "Randomize matrix");
        paramRandom.setTypeEquals(BaseType.BOOLEAN);
        paramRandom.setExpression("false");

        paramSize = new Parameter(this, "Matrix size");
        paramSize.setTypeEquals(BaseType.INT);
        paramSize.setExpression("10");

        // sample matrix values
        double[][] darray = new double[1][3];
        darray[0][0] = 0;
        darray[0][1] = 0;
        darray[0][2] = 0.0;

        paramInitialMatrix = new Parameter(this,
                "initial values ([x1,y1,value1;x2,y2,value2])",
                new DoubleMatrixToken(darray));
    }
    
    /**
     * This method is called every time the entire matrix has been updated.
     * It enforces the user-defined delay, calls _showMatrix(), and checks
     * if the iteration limit has been reached.
     * 
     * @return boolean false is the iteration limit has been reached and 
     * execution should stop, true otherwise.
     */
    protected boolean _iterate() {
        try {
            Thread.sleep((int) (1000.0 * ((DoubleToken) paramDelay.getToken())
                    .doubleValue()));
        } catch (Exception e) {
        }

        for (int i = 0; i < matrixSize; i++) {
            for (int j = 0; j < matrixSize; j++) {
                cells[i][j] = newCells[i][j];
            }
        }
        try {
            _showMatrix();
        } catch (Exception e) {
        }

        iter--;
        currentX = currentY = 0;
        if (iter <= 0) {
            return false;
        } else {

            return true;
        }
    }

    /**
     * This is a helper method called in preinitialize() to set the initial
     * matrix for the CAViewer (the actor that visualizes the grid).
     */
    protected void _setInitMatrix() {
        TypedCompositeActor container = (TypedCompositeActor) getContainer();
        Iterator actorList = container.entityList().iterator();

        Actor actorMatrixViewer = null;
        Actor actorConst = null;

        while (actorList.hasNext()) {
            Actor actor = (Actor) actorList.next();

            if (actor instanceof CAViewer) {
                CAViewer caViewer = (CAViewer) actor;
                caViewer.setMatrix(cells);
            }

        }
    }

    /**
     * This is a helper method called in _iterate() that sets the current 
     * matrix for any MatrixViewer, Const with a DoubleMatrixToken, or 
     * CAViewer actors.
     */
    protected void _showMatrix() throws IllegalActionException {
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
                Variable v = (Variable) c.value;
                if (v.getToken() instanceof DoubleMatrixToken) {
                    actorConst = actor;
                }
            } else if (actor instanceof CAViewer) {
                CAViewer caViewer = (CAViewer) actor;
                caViewer.setMatrix(cells);
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
    ////                   protected variables                     ////
    /**
     * The current matrix.
     */
    protected double[][] cells;
    
    /**
     * The x location of the current matrix cell being updated.
     */
    protected int currentX;
    
    /**
     * The y location of the current matrix cell being updated.
     */
    protected int currentY;
    
    /**
     * True if the director has not been fired for a given iteration,
     * false otherwise.
     */
    protected boolean firstFire;
    
    /**
     * The current iteration of the director.
     */
    protected int iter;
    
    /**
     *  The size of one dimension of the square matrix.
     */
    protected int matrixSize;
    
    /**
     *  A temporary matrix to hold the updated values.
     */
    protected double[][] newCells;
    
    /**
     *  The delay parameter.
     */
    protected Parameter paramDelay;
    
    /**
     * The initial matrix parameter.
     */
    protected Parameter paramInitialMatrix;
    
    /**
     * The iterations parameter.
     */
    protected Parameter paramIterations;
    
    /**
     *  The random parameter.
     */
    protected Parameter paramRandom;
    
    /**
     * The matrix size parameter.
     */
    protected Parameter paramSize;
}
