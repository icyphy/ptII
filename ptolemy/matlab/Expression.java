/* An actor that evaluates matlab expressions with input ports
   providing variables

 Copyright (c) 1998-2001 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.matlab;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.expr.Variable;
import ptolemy.data.Token;
import ptolemy.data.ScalarToken;
import ptolemy.data.IntToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.ComplexToken;
import ptolemy.data.MatrixToken;
import ptolemy.data.ComplexMatrixToken;
import ptolemy.data.DoubleMatrixToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.math.Complex;
import ptolemy.matlab.Engine;

import java.util.StringTokenizer;
import java.util.Iterator;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// Expression
/**
On each firing send an expression for evaluation to a matlab engine. The
expression is any valid matlab expression, e.g.:
<pre>
    [out1, out2,... ] = SomeMatlabFunctionOrExpression( in1, in2,... ); ...
</pre>

The expression may include references to the input port names, current
time (<i>time</i>), and a count of the firing (<i>iteration</i>). This
is similar to <a
href="../../ptolemy/actor/lib/Expression.html">Expression</a>. <p>

The matlab engine is opened (started) during prefire() by the first
matlab Expression actor. Subsequent open()s simply increment a use
count.<p>

At the start of fire(), a <i>clear</i> command is sent to matlab to
clear its workspace. This helps detect errors where the matlab
expression refers to a matlab variable not initialized from the input
ports of this actor instance.<p>

After the evaluation of the matlab expression is complete, the fire()
method iterates through names of output ports and converts matlab
variables with corresponding names to Tokens that are sent to the
corresponding output ports. Incorrect expressions are usually first
detected at this point by not finding the expected variables. If an
output port variable is not found in the matlab engine, an exception
is thrown. The exception description string contains the last stdout
of the matlab engine that ususally describes the error.<p>

A Parameter named <i>packageDirectories</i> may be added to this actor
to augment the matlab engine's search path during the firing of this
actor. The value of this parameter should evaluate to a StringToken,
e.g.:
<pre>
    "path1,path2,..."
</pre>

containing a comma-separated list of paths to be prepended to the matlab
engine search path before <i>expression</i> is evaluated. The list may contain paths
relative to the directory in which ptolemy was started, or any directory listed
in the current classpath (in that order, first match wins). See
{@link ptolemy.data.expr.UtilityFunctions#findFile(String)}. After
evaluation, the previous search path is restored.<p>

@author Zoltan Kemenczy and Sean Simmons, Research in Motion Limited
@version $Id$
*/
public class Expression extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Expression(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        expression = new StringAttribute(this, "expression");
        // _time is not needed, fire() sets a matlab variable directly
        _iteration = new Variable(this, "iteration", new IntToken(1));
        matlabEngine = new Engine();
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. */
    public TypedIOPort output;

    /** The parameter that is evaluated to produce the output.
     *  Typically, this parameter evaluates an expression involving
     *  the inputs.
     */
    public StringAttribute expression;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Open a matlab engine
     *  @exception IllegalActionException Thrown if matlab engine not found.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        if (_debugging) matlabEngine.setDebugging((byte)1);
        else matlabEngine.setDebugging((byte)0);
        matlabEngine.open();
    }

    /** Initialize the iteration count to 1.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 1;
        _iteration.setToken(new IntToken(_iterationCount));
    }

    /** Evaluate the expression and send its result to the output.
     *  @exception IllegalActionException If the evaluation of the expression
     *   triggers it, or the evaluation yields a null result, or the evaluation
     *   yields an incompatible type, or if there is no director.
     */
    public void fire() throws IllegalActionException {
        Director director = getDirector();
        if (director == null) {
            throw new IllegalActionException(this, "No director!");
        }
        try {
            // The following clears variables, but preserves any
            // persistent storage created by a function (this usually
            // for speed-up purposes to avoid recalculation on every
            // function call)
            matlabEngine.evalString("clear variables;clear globals");

            // Process any additional directories to be added to matlab's
            // path. The list may containe paths relative to the directory in
            // which ptolemy was started or any directory listed in the current
            // classpath (in this order, first match wins). See
            // UtilityFunctions.findFile()
            Parameter packageDirectories =
		(Parameter)getAttribute("packageDirectories");
            Token previousPath = null;
            if (packageDirectories != null) {
                StringTokenizer dirs = new
                    StringTokenizer((String)
				    ((StringToken)packageDirectories
				     .getToken()).stringValue(),",");
                StringBuffer cellFormat = new StringBuffer(512);
                cellFormat.append("{");
                if (dirs.hasMoreTokens()) {
                    cellFormat.append("'" + UtilityFunctions
				      .findFile(dirs.nextToken()) + "'");
                }
                while (dirs.hasMoreTokens()) {
                    cellFormat.append(",'" + UtilityFunctions
				      .findFile(dirs.nextToken()) + "'");
                }
                cellFormat.append("}");
                if (cellFormat.length() > 2) {
                    matlabEngine.evalString("previousPath_=path");
                    previousPath = matlabEngine.get("previousPath_");
                    matlabEngine.evalString("addedPath_="
					    + cellFormat.toString()
					    + ";addpath(addedPath_{:});");
                }
            }

            matlabEngine.put("time",
			     new DoubleToken(director.getCurrentTime()));
            matlabEngine.put("iteration",
			     _iteration.getToken());
            Iterator inputPorts = inputPortList().iterator();
            while(inputPorts.hasNext()) {
                IOPort port = (IOPort)(inputPorts.next());
                // FIXME: Handle multiports
                if (port.getWidth() > 0 && port.hasToken(0)) {
                    matlabEngine.put(port.getName(), port.get(0));
                }
            }
            matlabEngine.evalString(expression.getExpression());
            Iterator outputPorts = outputPortList().iterator();
            while(outputPorts.hasNext()) {
                IOPort port = (IOPort)(outputPorts.next());
                // FIXME: Handle multiports
                if (port.getWidth() > 0) {
                    port.send(0, matlabEngine.get(port.getName()));
                }
            }
            // Restore previous path if path was modified above
            if (previousPath != null) {
                matlabEngine.put("previousPath_",previousPath);
                matlabEngine.evalString("path(previousPath_);");
            }
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(getFullName()+": "+ex);
        }
    }

    /** Increment the iteration count.
     *  @IllegalActionException If the superclass throws it.
     */
    public boolean postfire() throws IllegalActionException {
        _iterationCount++;
        _iteration.setToken(new IntToken(_iterationCount));
        // This actor never requests termination.
        return true;
    }

    /** Close matlab engine if it was open
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        if (matlabEngine != null) matlabEngine.close();
    }

    private Engine matlabEngine = null;
    private Variable _iteration;
    private int _iterationCount = 1;
}
