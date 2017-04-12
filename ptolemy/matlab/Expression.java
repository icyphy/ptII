/* An actor that evaluates matlab expressions with input ports
 providing variables

 Copyright (c) 1998-2016 The Regents of the University of California and
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

 */
package ptolemy.matlab;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gui.style.CheckBoxStyle;
import ptolemy.actor.gui.style.TextStyle;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.matlab.impl.engine.MatlabEngine;
import ptolemy.matlab.impl.engine.MatlabEngine.ConversionParameters;
import ptolemy.matlab.impl.engine.MatlabEngineFactory;
import ptolemy.matlab.impl.engine.impl.DefaultMatlabEngineFactory;
import ptolemy.matlab.impl.gui.CollectionParameter;

/**
 * On each firing send an expression for evaluation to a Matlab(R)-compatible engine, an instance of 
 * {@link MatlabEngine}. The expression is any valid Matlab expression, e.g.:
 *
 * <pre>
 [out1, out2, ... ] = SomeMatlabFunctionOrExpression( in1, in2, ... );...
 * </pre>
 *
 * <p>
 * The expression may include references to the input port names, current time
 * (<i>time</i>), and a count of the firing (<i>iteration</i>). This is similar
 * to what the non-Matlab <a href="../../ptolemy/actor/lib/Expression.html">Expression</a> supports. To refer
 * to parameters in scope, use $name or ${name} within the expression.
 * </p>
 *
 * <p>
 * The Matlab engine is opened (started) during prefire() by the first Matlab
 * Expression actor. Subsequent open()s simply increment a use count.
 * </p>
 *
 * <p>
 * At the start of {@link #fire()}, <i>clear variables;clear globals</i> commands are
 * sent to Matlab to clear its workspace. This helps detect errors where the
 * Matlab expression refers to a Matlab variable not initialized from the input
 * ports of this actor instance.
 * </p>
 *
 * <p>
 * After the evaluation of the Matlab expression is complete, the {@link #fire()} method
 * iterates through names of output ports and converts Matlab variables with
 * corresponding names to Tokens that are sent to the corresponding output
 * ports. Incorrect expressions might be first detected at this point by not
 * finding the expected variables. If an output port variable is not found in
 * the {@link MatlabEngine} instance used, an exception is thrown. The exception
 * description string might contain the latest <i>stdout</i> of the Matlab engine (it usually
 * describes the error).
 * </p>
 *
 * <p>
 * The {@link #get1x1asScalars} and {@link #getIntegerMatrices} control data
 * conversion (see {@link MatlabEngine} and {@link ConversionParameters}).
 * </p>
 *
 * <p>
 * A Parameter named <i>packageDirectories</i> may be added to this actor to
 * augment the search path of the Matlab engine during the firing of this actor.
 * The value of this parameter should evaluate to a {@link StringToken}, e.g.:
 *
 * <pre>
 * "path1, path2, ..."
 * </pre>
 *
 * <p>
 * containing a comma-separated list of paths to be prepended to the Matlab
 * engine search path before <i>expression</i> is evaluated. The list may
 * contain paths relative to the directory in which Ptolemy II was started, or any
 * directory listed in the current <i>classpath</i> (in that order, first match wins).
 * See {@link ptolemy.data.expr.UtilityFunctions#findFile(String)}. After
 * evaluation, the previous search path is restored.
 * </p>
 *
 * <p>
 * A Parameter named <i>_debugging</i> may be used to turn on debug print
 * statements to stdout from the {@link MatlabEngine} used. An IntToken
 * with a value of 1 turns on standard debug statements, a value of 2
 * may add engine-specific debug statements as well. A value of 0 or the absence of the
 * <i>_debugging</i> parameter yields normal operation.
 * </p>
 *
 * <p>
 * For this actor to work, Matlab or a Matlab-compatible piece of software 
 * must be installed on your local machine. In
 * addition, your environment must be set properly. The
 * <code>$PTII/bin/vergil</code> script may do (part of) this for you. Below are
 * instructions for users, such as Eclipse users, who are not using
 * <code>$PTII/bin/vergil</code>.
 * </p>
 *
 * <p>
 * If you use Mathworks Matlab(R), add the Matlab shared libraries to your the library path. In the examples
 * below <code>$MATLAB</code> should be the location of your Matlab
 * installation. For example, if /usr/bin/matlab is a link:
 *
 * <pre>
 bash-3.2$ which matlab
 /usr/bin/matlab
 bash-3.2$ ls -l /usr/bin/matlab
 lrwxr-xr-x  1 root  wheel  42 Jan 15 20:57 /usr/bin/matlab -&gt; /Applications/MATLAB_R2009b.app/bin/matlab
 bash-3.2$
 * </pre>
 *
 * Then $MATLAB would be /Applications/MATLAB_R2009b.app
 *
 * <dl>
 * <dt>32 Bit Mac (10.5?, Edit ~/.MacOSX/environment.plist)</dt>
 * <dd><code>export DYLD_LIBRARY_PATH=$MATLAB/bin/maci</code></dd>
 * <dt>64 Bit Mac (10.6?, Edit ~/.MacOSX/environment.plist)</dt>
 * <dd><code>export DYLD_LIBRARY_PATH=$MATLAB/bin/maci64</code></dd>
 * <dt>32 Bit Linux (Edit</dt>
 * <dd><code>export LD_LIBRARY_PATH=$MATLAB/bin/gnlx86</code></dd>
 * <dt>64 Bit Linux</dt>
 * <dd><code>export LD_LIBRARY_PATH=$MATLAB/bin/glnxa64</code></dd>
 * <dt>Windows (Start|My Computer|Properties|Advanced Environment Variables| Add
 * the directory that contains matlab.exe to your path)</dt>
 * <dd><code>Be sure that the matlab binary is in your path</code></dd>
 * </dl>
 *
 * @author Zoltan Kemenczy and Sean Simmons, Research in Motion Limited
 * @version $Id$
 * @since Ptolemy II 2.0
 * @Pt.ProposedRating Yellow (zkemenczy)
 * @Pt.AcceptedRating Red (cxh)
 */
public class Expression extends TypedAtomicActor {

	/** The output port. */
	public TypedIOPort output;

	/**
	 * The parameter that is evaluated to produce the output. Typically, this
	 * parameter evaluates an expression involving the inputs. To refer to
	 * parameters in scope within the expression, use $name or ${name}, where
	 * "name" is the name of the parameter.
	 */
	public StringParameter expression;

	/**
	 * If true (checked), 1x1 matrix results are converted to ScalarTokens
	 * instead of a 1x1 MatrixToken, default is <i>true</i>.
	 */
	public Parameter get1x1asScalars;

	/**
	 * If true, all double-valued matrix results are checked to see if all
	 * elements represent integers, and if so, an IntMatrixToken is returned,
	 * default is <i>false</i> for performance reasons.
	 */
	public Parameter getIntegerMatrices;

	/** If true, clear variables and globals before each execution. */
	public Parameter clearEnvironment;

	/**
	 * External Matlab(R)-compatible engine to use.
	 */
	public CollectionParameter engineChoice;

	/**
	 * Filepath to external Matlab-compatible executable engine.
	 */
	public FileParameter engineFilePath;

	/**
	 * Used external Matlab-compatible executable engine, possibly found externally at {@link #engineFilePath}.
	 */
	private transient MatlabEngine engine = null;

	/**
	 * A factory able to produce instances of a requested Matlab-compatible executable engine, used to 
	 * initialize the {@link #engine} field with, provided the factory is able to yield an engine of the type indicated at 
	 * {@link #engineChoice}.
	 */
	private transient MatlabEngineFactory engineFactory = new DefaultMatlabEngineFactory();

	/**
	 * Currently-being-used instance of {@link #engine}.
	 */
	private MatlabEngine.MatlabEngineInstance engineCurrentInstance = null;

	private Variable _iteration;

	private int _iterationCount = 1;

	private String _addPathCommand = null;

	private Token _previousPath = null;

	private transient MatlabEngine.ConversionParameters _dataParameters;

	/** A map of input port names to tokens. */
	private Map<String, Token> _inputTokens = new HashMap<>();

	/**
	 * Construct an actor with the given container and name.
	 *
	 * @param container
	 *            The container.
	 * @param name
	 *            The name of this actor.
	 * @exception IllegalActionException
	 *                If the actor cannot be contained by the proposed
	 *                container.
	 * @exception NameDuplicationException
	 *                If the container already has an actor with this name.
	 */
	@SuppressWarnings({ "unused", "unchecked" })
	public Expression(final CompositeEntity container, final String name)
			throws NameDuplicationException, IllegalActionException {
		super(container, name);

		this.output = new TypedIOPort(this, "output", false, true);
		this.expression = new StringParameter(this, "expression");
		new TextStyle(this.expression, "Matlab expression");

		this._dataParameters = new MatlabEngine.ConversionParameters();

		this.get1x1asScalars = new Parameter(this, "get1x1asScalars",
				new BooleanToken(this._dataParameters.getScalarMatrices));
		new CheckBoxStyle(this.get1x1asScalars, "style");

		this.getIntegerMatrices = new Parameter(this, "getIntegerMatrices",
				new BooleanToken(this._dataParameters.getIntMatrices));
		new CheckBoxStyle(this.getIntegerMatrices, "style");

		this.clearEnvironment = new Parameter(this, "clearEnvironment", new BooleanToken(true));
		this.clearEnvironment.setTypeEquals(BaseType.BOOLEAN);
		this.clearEnvironment.setToken(BooleanToken.TRUE);

		this.engineFactory.initEngineTypes();
		this.engineChoice = new CollectionParameter(this, "engineChoice",
				(Collection<Object>) (Collection<?>) this.engineFactory.getEngineTypes());

		this.engineFilePath = new FileParameter(this, "engineFilePath");

		// _time is not needed, fire() sets a matlab variable directly
		this._iteration = new Variable(this, "_iteration", new IntToken(1));
	}

	/**
	 * Clone the actor into the specified workspace. This calls the base class
	 * and then sets the <code>iteration</code> public member to the parameters
	 * of the new actor.
	 *
	 * @param workspace
	 *            The workspace for the new object.
	 * @return A new actor.
	 * @exception CloneNotSupportedException
	 *                If a derived class contains an attribute that cannot be
	 *                cloned.
	 */
	@Override
	public Object clone(final Workspace workspace) throws CloneNotSupportedException {
		final Expression newObject = (Expression) super.clone(workspace);

		newObject._addPathCommand = null;
		newObject._iteration = (Variable) newObject.getAttribute("_iteration");
		newObject._iterationCount = 1;
		newObject._previousPath = null;
		newObject._inputTokens = new HashMap<>();
		//TODO: should other fields be included?
		return newObject;
	}

	/**
	 * Evaluate the expression and send its result to the output.
	 *
	 * @exception IllegalActionException
	 *                If the evaluation of the expression triggers it, or the
	 *                evaluation yields a null result, or the evaluation yields
	 *                an incompatible type, or if there is no director.
	 */
	@Override
	public void fire() throws IllegalActionException {
		super.fire();
		final Director director = this.getDirector();
		if (director == null) {
			throw new IllegalActionException(this, "No director!");
		}

		try {

			// Read the input ports before acquiring the engine lock since
			// get() may block depending on the director, e.g., PN.
			this.setInputPortsIntoEngine();
			final boolean clearEnvironmentValue = ((BooleanToken) this.clearEnvironment.getToken()).booleanValue();

			final String chosenEngineName = this.engineChoice.getExpression();
			synchronized (this.engineFactory.getEngineSemaphore(chosenEngineName)) {

				if (clearEnvironmentValue) {
					this.engine.evalString(this.engineCurrentInstance, "clear variables;clear globals");
				}

				if (this._addPathCommand != null) {
					this.engine.evalString(this.engineCurrentInstance, this._addPathCommand);
				}

				this.putVariablesIntoEngine(director);
				this.engine.evalString(this.engineCurrentInstance, this.expression.stringValue());
				this.updateOutputPortsFromEngine();

				// Restore previous path if path was modified above
				if (this._previousPath != null) {
					this.engine.put(this.engineCurrentInstance, "previousPath_", this._previousPath);
					this.engine.evalString(this.engineCurrentInstance, "path(previousPath_);");
				}
			}
		} finally {
			// Remove references to any tokens that were read from input ports.
			this._inputTokens.clear();
		}
	}

	/**
	 * Initialize the iteration count to 1.
	 *
	 * @exception IllegalActionException
	 *                If the parent class throws it.
	 */
	@Override
	public void initialize() throws IllegalActionException {
		super.initialize();

		// Check to make sure that the engine has been initialized.
		final String chosenEngineName = this.engineChoice.getExpression();
		final String engineFilePath = this.engineFilePath.getValueAsString();
		synchronized (this.engineFactory.getEngineSemaphore(chosenEngineName)) {
			if (this.engineCurrentInstance == null) {
				this._initializeEngine(chosenEngineName, engineFilePath);
			}
		}

		this._iterationCount = 1;
		this._iteration.setToken(new IntToken(this._iterationCount));

		// Process any additional directories to be added to matlab's
		// path. The list may contain paths relative to the directory in
		// which ptolemy was started or any directory listed in the current
		// classpath (in this order, first match wins). See
		// UtilityFunctions.findFile()
		this._addPathCommand = null; // Assume none
		this._previousPath = null;

		final Parameter packageDirectories = (Parameter) this.getAttribute("packageDirectories");

		if (packageDirectories != null) {
			final StringTokenizer dirs = new StringTokenizer(
					((StringToken) packageDirectories.getToken()).stringValue(), ",");
			final StringBuffer cellFormat = new StringBuffer(512);
			cellFormat.append("{");

			if (dirs.hasMoreTokens()) {
				cellFormat.append("'" + UtilityFunctions.findFile(dirs.nextToken()) + "'");
			}

			while (dirs.hasMoreTokens()) {
				cellFormat.append(",'" + UtilityFunctions.findFile(dirs.nextToken()) + "'");
			}

			cellFormat.append("}");

			if (cellFormat.length() > 2) {
				this._addPathCommand = "addedPath_ = " + cellFormat.toString() + ";addpath(addedPath_{:});";

				synchronized (this.engineFactory.getEngineSemaphore(chosenEngineName)) {
					this.engine.evalString(this.engineCurrentInstance, "previousPath_=path");
					this._previousPath = this.engine.get(this.engineCurrentInstance, "previousPath_");
				}
			}
		}

		this._dataParameters.getScalarMatrices = ((BooleanToken) this.get1x1asScalars.getToken()).booleanValue();
		this._dataParameters.getIntMatrices = ((BooleanToken) this.getIntegerMatrices.getToken()).booleanValue();
	}

	/**
	 * Increment the iteration count.
	 *
	 * @exception IllegalActionException
	 *                If the superclass throws it.
	 */
	@Override
	public boolean postfire() throws IllegalActionException {
		this._iterationCount++;
		this._iteration.setToken(new IntToken(this._iterationCount));
		// This actor never requests termination.
		return super.postfire();
	}

	/**
	 * Return true if all input ports have at least one token.
	 *
	 * @return True if this actor is ready for firing, false otherwise.
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	@Override
	public boolean prefire() throws IllegalActionException {
		@SuppressWarnings("rawtypes")
		final Iterator inputPorts = this.inputPortList().iterator();
		while (inputPorts.hasNext()) {
			final IOPort port = (IOPort) inputPorts.next();
			if (!port.hasToken(0)) {
				return false;
			}
		}
		return super.prefire();
	}

	/**
	 * Open a matlab engine.
	 *
	 * @exception IllegalActionException
	 *                If matlab engine not found.
	 */
	@Override
	public void preinitialize() throws IllegalActionException {
		super.preinitialize();
		this._initializeEngine(this.engineChoice.getExpression(), this.engineFilePath.getValueAsString());
	}
	
	/**
	 * Close matlab engine if it was open.
	 *
	 * @exception IllegalActionException
	 *                Not thrown in this base class.
	 */
	@Override
	public void wrapup() throws IllegalActionException {
		super.wrapup();
		final String chosenEngineName = this.engineChoice.getExpression();
		synchronized (this.engineFactory.getEngineSemaphore(chosenEngineName)) {
			if (this.engine != null) {
				this.engine.close(this.engineCurrentInstance);
			}
		}
		this.engineCurrentInstance = null;
	}
	

	/**
	 * Default type constraints do not apply in this case, since the input type
	 * may be totally unrelated to the output type and cannot be inferred;
	 * return null.
	 *
	 * @return null
	 */
	@Override
	protected Set<Inequality> _defaultTypeConstraints() {
		return null;
	}

	/**
	 * Initialize the Matlab MathworksEngine.
	 *
	 * @param chosenEngineName
	 * @param string
	 * @exception IllegalActionException
	 *                If the connection to the Matlab engine cannot be
	 *                intialized.
	 */
	private void _initializeEngine(final String chosenEngineName, final String engineFilePath)
			throws IllegalActionException {

		synchronized (this.engineFactory.getEngineSemaphore(chosenEngineName)) {
			// The intialization used to occur in preinitialize(), but this
			// meant that the Matlab Expression actor could not be used
			// in a RunComposite because RunComposite invokes
			// _executeInsideModel()
			// which does not invoke preinitialize(). If _executeInsideModel()
			// invokes preinitialize(), then the PortParameters of the
			// RunComposite
			// are constantly reset to the value of the _persistentExpression.
			try {
				this.engine = this.engineFactory.getEngine(chosenEngineName);
			} catch (final Throwable throwable) {
				throw new IllegalActionException(this,throwable,buildEngineErrorMessage(chosenEngineName, throwable));
			}
			this.engine.setDebugLogs(getDebugging());
			this.engineCurrentInstance = this.engine.open(engineFilePath, false);
		}
	}
	

	private void putVariablesIntoEngine(final Director director) throws IllegalActionException {
		try {
			this.engine.put(this.engineCurrentInstance, "time", new DoubleToken(director.getModelTime().getDoubleValue()));
		} catch (final IllegalActionException ex) {
			throw new IllegalActionException(this, ex, "Failed to set the \"time\" variable in the Matlab "
					+ "engine to " + new DoubleToken(director.getModelTime().getDoubleValue()));
		}
		try {
			this.engine.put(this.engineCurrentInstance, "iteration", this._iteration.getToken());
		} catch (final IllegalActionException ex) {
			throw new IllegalActionException(this, ex, "Failed to set the \"iteration\" variable in the Matlab "
					+ "engine to " + this._iteration.getToken());
		}

		for (final Map.Entry<String, Token> entry : this._inputTokens.entrySet()) {
			this.engine.put(this.engineCurrentInstance, entry.getKey(), entry.getValue());
		}
	}
	

	private void setInputPortsIntoEngine() throws NoTokenException, IllegalActionException {
		for (final TypedIOPort port : this.inputPortList()) {
			this._inputTokens.put(port.getName(), port.get(0));
		}
	}
	

	private void updateOutputPortsFromEngine() throws IllegalActionException, NoRoomException {
		@SuppressWarnings("rawtypes")
		final Iterator outputPorts = this.outputPortList().iterator();
		while (outputPorts.hasNext()) {
			final IOPort port = (IOPort) outputPorts.next();
			// FIXME: Handle multiports
			if (port.isOutsideConnected()) {
				port.send(0, this.engine.get(this.engineCurrentInstance, port.getName(), this._dataParameters));
			}
		}
	}
	
	private byte getDebugging() throws IllegalActionException {
		final Parameter debugging = (Parameter) this.getAttribute("_debugging");
		if (debugging != null) {
			final Token t = debugging.getToken();
			if (t instanceof IntToken) {
				return ((byte) ((IntToken) t).intValue());
			}
		}
		return MatlabEngine.NO_DEBUG_LOGS;
	}
	
	private static String buildEngineErrorMessage(final String chosenEngineName, final Throwable throwable) {
		final StringBuffer errorMessage = new StringBuffer("There was a problem invoking the ")
				.append(chosenEngineName)
				.append("interface.\nError details: ")
				.append(throwable.getLocalizedMessage())
				.append("\nIf you are using Mathworks Matlab(R), it should be installed "
					+ "on the local machine and the ptmatlab " + " shared library should be available.\n"
					+ "* Under Linux, you must have the LD_LIBRARY_PATH "
					+ "environment variable set to include the directories "
					+ "that contain libmx.so and libptmatlab.so.\n"
					+ "* Under Mac OS X, you must have the DYLD_LIBRARY_PATH "
					+ "environment variable set to include the directories "
					+ "that contain libmx.dylib and libptmatlab.jnilib.\n"
					+ "* Under Windows, you must have your PATH set to include "
					+ "the Matlab bin/win32 or equivalent directory so that "
					+ "libmex.dll is found and the directory that contains " + "libptmatlab.dll. "
					+ "In addition, if you are running under Windows from "
					+ "the Cygwin bash prompt, then you must start Vergil with "
					+ "the -jni argument: $PTII/bin/vergil -jni. For details, " + "see $PTII/jni/package.html.\n"
					+ "Refer to $PTII/ptolemy/matlab/makefile for more " + "information.");
		return errorMessage.toString();
	}

}
