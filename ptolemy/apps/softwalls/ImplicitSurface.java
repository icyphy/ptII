/* Actor for calculating the value of f, df/dx, df/dy, and df/dtheta

 Copyright (c) 2003-2004 The Regents of the University of California.
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
@ProposedRating Red (acataldo@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.apps.softwalls;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.data.expr.FileParameter;

import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// ImplicitSurface
/**
This takes state input from the aircraft, and outputs information on
the implicit surface function, which represents the reachable set.
This function is positive outside the reacable set, negative inside
the reachable set, and 0 on the boundary of the reacable set.  Its
value represents its distance to the reachable set, and, for positive
values, the negative gradient points toward the shortest path the the
reachable set.  This outputs the function value, the x gradient, the y
gradient, and the theta gradient, given the current x, y, and theta
value.

The function file should be stored in
$PTII/ptolemy/apps/softwalls/surfaces.

@author Adam Cataldo
@version $Id$
@since Ptolemy II 2.0.1
*/
public class ImplicitSurface extends TypedAtomicActor {
    /** Constructs an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public ImplicitSurface(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create and configure ports
        x = new TypedIOPort(this, "x", true, false);
        x.setTypeEquals(BaseType.DOUBLE);

        y = new TypedIOPort(this, "y", true, false);
        y.setTypeEquals(BaseType.DOUBLE);

        heading = new TypedIOPort(this, "heading", true, false);
        heading.setTypeEquals(BaseType.DOUBLE);

        functionValue = new TypedIOPort(this, "functionValue", false, true);
        functionValue.setTypeEquals(BaseType.DOUBLE);

        dx = new TypedIOPort(this, "dx", false, true);
        dx.setTypeEquals(BaseType.DOUBLE);

        dy = new TypedIOPort(this, "dy", false, true);
        dy.setTypeEquals(BaseType.DOUBLE);

        dtheta = new TypedIOPort(this, "dz", false, true);
        dtheta.setTypeEquals(BaseType.DOUBLE);

        // Create and configure FileParameters
        functionFile = new FileParameter(this, "functionFile");
        dxFile = new FileParameter(this, "dxFile");
        dyFile = new FileParameter(this, "dyFile");
        dthetaFile = new FileParameter(this , "dthetaFile");

        // This flag will become true after initialize() is first
        // called, to avoid reloading the surface function.
        _alreadyInitialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and FileParameters                  ////

    /** The location of the function file. */
    public FileParameter functionFile;

    /** The location of the dx file. */
    public FileParameter dxFile;

    /** The location of the dy file. */
    public FileParameter dyFile;

    /** The location of the dtheta file. */
    public FileParameter dthetaFile;

    /** Input x position.  The type of this port is double. */
    public TypedIOPort x;

    /** Current y position.  The type of this port is double. */
    public TypedIOPort y;

    /** Input heading angle. The type of this port is double. */
    public TypedIOPort heading;

    /** Output functionValue of type double. */
    public TypedIOPort functionValue;

    /** Output gradiant value for the x coordinate.  The type of this
     *  port is double.
     */   
    public TypedIOPort dx;

    /** Output gradiant value for the y coordinate.  The type of this
     *  port is double.
     */   
    public TypedIOPort dy;

    /** Output gradiant value theta.  The type of this
     *  port is double.
     */   
    public TypedIOPort dtheta;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Overrides the base class to output the implicit surface
     *  function value.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void fire() throws IllegalActionException {
        double x1, x2, x3;
        double f, xGrad, yGrad, thetaGrad;

        // Get current function and gradient information.
        x1 = ((DoubleToken)x.get(0)).doubleValue();
        x2 = ((DoubleToken)y.get(0)).doubleValue();
        x3 = ((DoubleToken)heading.get(0)).doubleValue();
        f = _surfaceFunction.getValue(x1, x2, x3);
        xGrad = _xGradientFunction.getValue(x1, x2, x3);
        yGrad = _yGradientFunction.getValue(x1, x2, x3);
        thetaGrad = _thetaGradientFunction.getValue(x1, x2, x3);

        // Send values out of ports.
        functionValue.send(0, new DoubleToken(f));
        dx.send(0, new DoubleToken(xGrad));
        dy.send(0, new DoubleToken(yGrad));
        dtheta.send(0, new DoubleToken(thetaGrad));
    }

    /** Loads the implicit surface function and gradient function.
     *  @exception IllegalActionException If the super class throws
     *  it, or if an exception is generated by creating the
     *  functions.
     */

    public void initialize() throws IllegalActionException {
        super.initialize();
        // Prevent reinitialization.
        if (_alreadyInitialized) {
            return;
        } else {
            _alreadyInitialized = true;
        }

        String ptII, functionName, dxName, dyName, dthetaName;

        // Make the file names relative to the correct path.
        functionName = functionFile.asFile().getAbsolutePath();
        dxName = dxFile.asFile().getAbsolutePath();
        dyName = dyFile.asFile().getAbsolutePath();
        dthetaName = dthetaFile.asFile().getAbsolutePath();

        // FIXME: The above code doesn't work... Hardwire in the names.
        //_surfaceFunction = new ThreeDFunction(functionName);
        //_xGradientFunction = new ThreeDFunction(dxName);
        //_yGradientFunction = new ThreeDFunction(dyName);
        //_thetaGradientFunction = new ThreeDFunction(dthetaName);

        _surfaceFunction = ThreeDFunction.read(functionName);
        _xGradientFunction = ThreeDFunction.read(dxName);
        _yGradientFunction = ThreeDFunction.read(dyName);
        _thetaGradientFunction = ThreeDFunction.read(dthetaName);
    }

//     /** Clears the the implicit surface function and gradient function.
//      *  @exception IllegalActionException If the super class throws
//      *  it.
//      */
//     public void wrapup() throws IllegalActionException {
//         _surfaceFunction = null;
//         _xGradientFunction = null;
//         _yGradientFunction = null;
//         _thetaGradientFunction = null;
//     }

    ///////////////////////////////////////////////////////////////////
    ////                      private variables                    ////

    // Implicit Surface Function and its gradient functions
    private ThreeDFunction _surfaceFunction;
    private ThreeDFunction _xGradientFunction;
    private ThreeDFunction _yGradientFunction;
    private ThreeDFunction _thetaGradientFunction;

    // Used to stop actor from reloading each time.
    private boolean _alreadyInitialized;

}


