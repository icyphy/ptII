/* An actor that evaluates matlab expressions with input ports
   providing variables

 Copyright (c) 1998-2003 The Regents of the University of California and
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

@ProposedRating Yellow (zkemenczy@rim.net)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.matlab;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.data.expr.Variable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.matlab.Engine.ConversionParameters;

//////////////////////////////////////////////////////////////////////////
//// Expression
/**
On each firing send an expression for evaluation to a matlab {@link
Engine}. The expression is any valid matlab expression, e.g.:

<pre>
[out1, out2, ... ] = SomeMatlabFunctionOrExpression( in1, in2, ... );...
</pre>

The expression may include references to the input port names, current
time (<i>time</i>), and a count of the firing (<i>iteration</i>). This
is similar to <a
href="../../ptolemy/actor/lib/Expression.html">Expression</a>.
To refer to parameters in scope, use $name or ${name} within
the expression.
<p>

The matlab engine is opened (started) during prefire() by the first
matlab Expression actor. Subsequent open()s simply increment a use
count.<p>

At the start of fire(), <i>clear variables;clear globals</i> commands are
sent to matlab to clear its workspace. This helps detect errors where the
matlab expression refers to a matlab variable not initialized from the
input ports of this actor instance.<p>

After the evaluation of the matlab expression is complete, the fire()
method iterates through names of output ports and converts matlab
variables with corresponding names to Tokens that are sent to the
corresponding output ports. Incorrect expressions are usually first
detected at this point by not finding the expected variables. If an
output port variable is not found in the matlab {@link Engine}, an
exception is thrown. The exception description string contains the last
stdout of the matlab engine that usually describes the error.<p>

The {@link #get1x1asScalars} and {@link #getIntegerMatrices} control
data conversion (see {@link Engine} and
{@link Engine.ConversionParameters}).<p>

A Parameter named <i>packageDirectories</i> may be added to this actor
to augment the search path of the matlab engine during the firing of this
actor. The value of this parameter should evaluate to a StringToken,
e.g.:

<pre>
    "path1, path2, ..."
</pre>

containing a comma-separated list of paths to be prepended to the matlab
engine search path before <i>expression</i> is evaluated. The list may
contain paths relative to the directory in which ptolemy was started,
or any directory listed in the current classpath (in that order, first
match wins). See {@link ptolemy.data.expr.UtilityFunctions#findFile(String)}.
After evaluation, the previous search path is restored.<p>

A Parameter named <i>_debugging</i> may be used to turn on debug print
statements to stdout from {@link Engine} and the ptmatlab JNI. An IntToken
with a value of 1 turns on Engine debug statements, a value of 2 adds
ptmatlab debug statements as well.  A value of 0 or the absence of the
<i>_debugging</i> parameter yields normal operation.<p>

@author Zoltan Kemenczy and Sean Simmons, Research in Motion Limited
@version $Id$
@since Ptolemy II 2.0
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
            throws NameDuplicationException, IllegalActionException,
            java.lang.Exception  {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        expression = new StringParameter(this, "expression");

        _dataParameters = new Engine.ConversionParameters();

        get1x1asScalars = new Parameter
            (this, "get1x1asScalars",
                    new BooleanToken(_dataParameters.getScalarMatrices));
        new CheckBoxStyle(get1x1asScalars, "style");

        getIntegerMatrices = new Parameter
            (this, "getIntegerMatrices",
                    new BooleanToken(_dataParameters.getIntMatrices));
        new CheckBoxStyle(getIntegerMatrices, "style");

        // _time is not needed, fire() sets a matlab variable directly
        _iteration = new Variable(this, "iteration", new IntToken(1));

    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output port. */
    public TypedIOPort output;

    /** The parameter that is evaluated to produce the output.
     *  Typically, this parameter evaluates an expression involving
     *  the inputs. To refer to parameters in scope within the expression,
     *  use $name or ${name}, where "name" is the name of the parameter.
     */
    public StringParameter expression;

    /** If true (checked), 1x1 matrix results are converted to
        ScalarTokens instead of a 1x1 MatrixToken, default is
        <i>true</i>. */
    public Parameter get1x1asScalars;

    /** If true, all double-valued matrix results are checked to see if
        all elements represent integers, and if so, an IntMatrixToken is
        returned, default is <i>false</i> for performance reasons. */
    public Parameter getIntegerMatrices;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Must specify port types using moml (TypeAttribute) - the default
     *  TypedAtomicActor type constraints do not apply in this case, since the
     *  input type may be totally unrelated to the output type and cannot be
     *  inferred; return an empty list. */
    public List typeConstraintList()  {
        LinkedList result = new LinkedList();
        return result;
    }

    /** Open a matlab engine
     *  @exception IllegalActionException If matlab engine not found.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        try {
            matlabEngine = new Engine();
        } catch (Throwable throwable) {
            // LinkageError is and Error, not an exceptoin
            throw new IllegalActionException(this, throwable,
                    "There was a problem invoking the Ptolemy II Matlab interface"
                    + ".\nThe interface has been tested under Windows and Linux,\n"
                    + "requires that Matlab be installed on the local machine."
                    + "Refer to $PTII/ptolemy/matlab/makefile for more"
                    + "information.");
        }

        // First set default debugging level, then check for more
        matlabEngine.setDebugging((byte)0);
        Parameter debugging = ((Parameter)getAttribute("_debugging"));
        if (debugging != null) {
            Token t = debugging.getToken();
            if (t instanceof IntToken)
                matlabEngine.setDebugging((byte)((IntToken)t).intValue());
        }
        engine = matlabEngine.open();
    }

    /** Initialize the iteration count to 1.
     *  @exception IllegalActionException If the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _iterationCount = 1;
        _iteration.setToken(new IntToken(_iterationCount));

        // Process any additional directories to be added to matlab's
        // path. The list may contain paths relative to the directory in
        // which ptolemy was started or any directory listed in the current
        // classpath (in this order, first match wins). See
        // UtilityFunctions.findFile()

        _addPathCommand = null;         // Assume none
        _previousPath = null;
        Parameter packageDirectories =
            (Parameter)getAttribute("packageDirectories");

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
                _addPathCommand = "addedPath_ = " + cellFormat.toString()
                    + ";addpath(addedPath_{:});";
                synchronized (Engine.semaphore) {
                    matlabEngine.evalString
                        (engine, "previousPath_=path");
                    _previousPath = matlabEngine.get(engine, "previousPath_");
                }
            }
        }
        _dataParameters.getScalarMatrices =
            ((BooleanToken)get1x1asScalars.getToken()).booleanValue();
        _dataParameters.getIntMatrices =
            ((BooleanToken)getIntegerMatrices.getToken()).booleanValue();
    }

    /** Return true if all input ports have at least one token.
     *  @return True if this actor is ready for firing, false otherwise.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    public boolean prefire() throws IllegalActionException {
        Iterator inputPorts = inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort port = (IOPort)(inputPorts.next());
            if (!port.hasToken(0)) {
                return false;
            }
        }
        return true;
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
            synchronized (Engine.semaphore) {
                // The following clears variables, but preserves any
                // persistent storage created by a function (this usually
                // for speed-up purposes to avoid recalculation on every
                // function call)
                matlabEngine.evalString
                    (engine, "clear variables;clear globals");

                if (_addPathCommand != null) {
                    matlabEngine.evalString(engine, _addPathCommand);
                }

                matlabEngine.put(engine, "time",
                        new DoubleToken(director.getCurrentTime()));
                matlabEngine.put(engine, "iteration",
                        _iteration.getToken());
                Iterator inputPorts = inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort port = (IOPort)(inputPorts.next());
                    matlabEngine.put(engine, port.getName(), port.get(0));
                }
                matlabEngine.evalString(engine, expression.stringValue());
                Iterator outputPorts = outputPortList().iterator();
                while (outputPorts.hasNext()) {
                    IOPort port = (IOPort)(outputPorts.next());
                    // FIXME: Handle multiports
                    if (port.getWidth() > 0) {
                        port.send(0, matlabEngine.get
                                (engine, port.getName(), _dataParameters));
                    }
                }
                // Restore previous path if path was modified above
                if (_previousPath != null) {
                    matlabEngine.put(engine, "previousPath_",_previousPath);
                    matlabEngine.evalString
                        (engine, "path(previousPath_);");
                }
            }
        } catch (IllegalActionException ex) {
            throw new IllegalActionException(getFullName() + ": " + ex);
        }
    }

    /** Increment the iteration count.
     *  @exception IllegalActionException If the superclass throws it.
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
        if (matlabEngine != null) matlabEngine.close(engine);
        engine = null;
    }

    private Engine matlabEngine = null;
    long[] engine = null;
    private Variable _iteration;
    private int _iterationCount = 1;
    private String _addPathCommand = null;
    private Token _previousPath = null;
    private ConversionParameters _dataParameters;
}
