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
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
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

        dx = new TypedIOPort(this, "dx", false, true);
        dx.setTypeEquals(BaseType.DOUBLE);

        dxFile = new FileParameter(this, "dxFile");

        dy = new TypedIOPort(this, "dy", false, true);
        dy.setTypeEquals(BaseType.DOUBLE);

        dyFile = new FileParameter(this, "dyFile");

        dtheta = new TypedIOPort(this, "dz", false, true);
        dtheta.setTypeEquals(BaseType.DOUBLE);

        dthetaFile = new FileParameter(this , "dthetaFile");

        heading = new TypedIOPort(this, "heading", true, false);
        heading.setTypeEquals(BaseType.DOUBLE);

        filesAreCompressed =
            new Parameter(this, "filesAreCompressed", new BooleanToken(false));
        filesAreCompressed.setTypeEquals(BaseType.BOOLEAN);

        functionFile = new FileParameter(this, "functionFile");

        functionValue = new TypedIOPort(this, "functionValue", false, true);
        functionValue.setTypeEquals(BaseType.DOUBLE);

        writeOutData =
            new Parameter(this, "writeOutData", new BooleanToken(false));
        writeOutData.setTypeEquals(BaseType.BOOLEAN);
        // Hide the writeOutData parameter.
        writeOutData.setVisibility(Settable.EXPERT);

        x = new TypedIOPort(this, "x", true, false);
        x.setTypeEquals(BaseType.DOUBLE);

        y = new TypedIOPort(this, "y", true, false);
        y.setTypeEquals(BaseType.DOUBLE);

        // This flag will become true after initialize() is first
        // called, to avoid reloading the surface function.
        _alreadyInitialized = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and FileParameters                  ////

    /** Output gradiant value for the x coordinate.  The type of this
     *  port is double.
     */
    public TypedIOPort dx;

    /** The location of the dx file. */
    public FileParameter dxFile;

    /** Output gradiant value for the y coordinate.  The type of this
     *  port is double.
     */
    public TypedIOPort dy;

    /** The location of the dy file. */
    public FileParameter dyFile;

    /** Output gradiant value theta.  The type of this
     *  port is double.
     */   
    public TypedIOPort dtheta;

    /** The location of the dtheta file. */
    public FileParameter dthetaFile;

    /** A boolean parameter that by default is set to true if the files
     *  are compressed.
     */
    public Parameter filesAreCompressed;

    /** The location of the function file. */
    public FileParameter functionFile;

    /** Output functionValue of type double. */
    public TypedIOPort functionValue;

    /** Input heading angle. The type of this port is double. */
    public TypedIOPort heading;

    /** A boolean parameter that determines whether the input data files
     *  are written out in the 'other format'.
     *  
     *  <p>This parameter is only visible in expert mode.
     *
     *  <p>The initial value of this parameters is false, which means
     *  that no action is taken.  

     *  <p> This actor reads data files that can be either compressed
     *  or uncompressed, as per the <i>filesAreCompressed</i>
     *  parameter.  

     *  <p>If this parameter is true, and the input files are in uncompressed
     *  format, then the input files are written out in compressed format
     *  in the current directory with ".out" appended to the base file name 
     *  of each file.  For example, if <i>dxFile</i> is set to
     *  "surfaces/softwall.final.gradx.data", then the output file will be
     *  called "softwall.final.gradx.data.out".
     *
     *  <p>If this parameter is true, and the input files are in compressed
     *  format, then the input files are written out in compressed format
     *  with a ".out" appended to the base file name of each file.
     */   
    public Parameter writeOutData;

    /** Input x position.  The type of this port is double. */
    public TypedIOPort x;

    /** Current y position.  The type of this port is double. */
    public TypedIOPort y;


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

        //String ptII, functionName, dxName, dyName, dthetaName;

        // Make the file names relative to the correct path.
        // functionName = functionFile.asFile().getAbsolutePath();
        // dxName = dxFile.asFile().getAbsolutePath();
        // dyName = dyFile.asFile().getAbsolutePath();
        // dthetaName = dthetaFile.asFile().getAbsolutePath();

        boolean compressed =
            ((BooleanToken)filesAreCompressed.getToken()).booleanValue();

        boolean write =
            ((BooleanToken)writeOutData.getToken()).booleanValue();

        _surfaceFunction = new ThreeDFunction(functionFile, compressed, write);
        _xGradientFunction = new ThreeDFunction(dxFile, compressed, write);
        _yGradientFunction = new ThreeDFunction(dyFile, compressed, write);
        _thetaGradientFunction =
            new ThreeDFunction(dthetaFile, compressed, write);
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


