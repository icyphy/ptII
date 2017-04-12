package ptolemy.matlab.impl.backends.jmathlib;

import java.util.HashMap;

import jmathlib.core.interpreter.ErrorLogger;
import jmathlib.core.interpreter.JMathLibException;
import jmathlib.core.interpreter.Variable;
import jmathlib.core.tokens.DataToken;
import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObject;
import ptolemy.matlab.impl.backends.jmathlib.adapters.JMLObjectAdapterFactory;
import ptolemy.matlab.impl.engine.AbstractMatlabEngine;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ObjectAdapterFactory;

/**
 * @author david
 *
 */
public class JMLEngine extends AbstractMatlabEngine {

	public final static String ENGINE_NAME = "JMathLib";
	
	private final HashMap<String,JMLEngineInstance> instances = new HashMap<String,JMLEngineInstance>();

	public JMLEngine() {
		super(ENGINE_NAME);
		ErrorLogger.setDebug(super.getDebugLogsFlag() != NO_DEBUG_LOGS);
	}

	/**
	 * @see ptolemy.matlab.impl.engine.MatlabEngine#open(java.lang.String, boolean)
	 */
	@Override
	public MatlabEngineInstance open(String startCmd, boolean needOutput) throws IllegalActionException {
		final JMLEngineInstance instance = new JMLEngineInstance(DEFAULT_OUTPUT_BUFFER_SIZE);
		this.instances.put(instance.id, instance);
		return instance;
	}

	/**
	 * @see ptolemy.matlab.impl.engine.MatlabEngine#close(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance)
	 */
	@Override
	public void close(MatlabEngineInstance instance) throws IllegalActionException {
		if (this.instances.containsKey(instance.id)) {
			this.instances.remove(instance.id);
		} else {
			throw buildInvalidInstanceException(instance);			
		}
	}
	
	/**
	 * @see ptolemy.matlab.impl.engine.MatlabEngine#evalString(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String)
	 */
	@Override
	public void evalString(MatlabEngineInstance instance, String evalStr) throws IllegalActionException {
		final JMLEngineInstance jmInstance = this.instances.get(instance.id);
		if (jmInstance != null) {
			try {
				jmInstance.getInterpreter().executeExpression(evalStr);				
			} catch (final JMathLibException e) {
				throw new IllegalActionException("failed to evaluate expression: <<" + evalStr + ">>. Error: " + e.getMessage());
			}
		} else {
			throw buildInvalidInstanceException(instance);
		}
	}

	/**
	 * @see ptolemy.matlab.impl.engine.MatlabEngine#getOutput(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance)
	 */
	@Override
	public StringToken getOutput(MatlabEngineInstance instance) throws IllegalActionException {
		final JMLEngineInstance jmInstance = this.instances.get(instance.id);
		if (jmInstance != null) {
			final JMLEngineOutputBuffer engineOutput = (JMLEngineOutputBuffer) jmInstance.getInterpreter().getOutputPanel();
			return adaptOutputBuffer(engineOutput);
		}
		throw buildInvalidInstanceException(instance);			
	}

	/**
	 * @see ptolemy.matlab.impl.engine.AbstractMatlabEngine#putMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String, ptolemy.matlab.impl.engine.adaption.MatlabObject)
	 */
	@Override
	protected void putMatlabObject(MatlabEngineInstance engInstance, String name, MatlabObject matlabObject)
			throws IllegalActionException {
		final JMLEngineInstance jmlInstance = (JMLEngineInstance) engInstance;
		final JMLObject jmlObject = (JMLObject) matlabObject;
		final Variable jmlVariable = jmlInstance.interpreter.globals.createVariable(name);
		jmlVariable.assign(jmlObject.getJmlToken());
	}

	/**
	 * @see ptolemy.matlab.impl.engine.AbstractMatlabEngine#getMatlabObject(ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance, java.lang.String)
	 */
	@Override
	protected MatlabObject getMatlabObject(MatlabEngineInstance engInstance, String name) throws IllegalActionException {
		final JMLEngineInstance jmlInstance = (JMLEngineInstance) engInstance;
		final Variable jmlVariable = jmlInstance.getInterpreter().globals.getVariable(name);
		if (jmlVariable == null) {
			throw new IllegalActionException(logHeader(engInstance.id) + " invalid variable name: " + name);
		}
		return new JMLObject((DataToken)jmlVariable.getData(),name);
	}

	/**
	 * @see ptolemy.matlab.impl.engine.AbstractMatlabEngine#releaseMatlabObject(ptolemy.matlab.impl.engine.adaption.MatlabObject, java.lang.String)
	 */
	@Override
	protected void releaseMatlabObject(MatlabObject matlabObject, String name) {
		// Nothing to do here.
	}

	/**
	 * @param engineOutput
	 * @return
	 */
	protected static StringToken adaptOutputBuffer(final JMLEngineOutputBuffer engineOutput) {
		final StringBuffer stringBuffer = new StringBuffer();
		for (String line: engineOutput) {
			stringBuffer.append(line + "\n");
		}
		return new StringToken(stringBuffer.toString());
	}

	@Override
	protected ObjectAdapterFactory getObjectAdapterFactory() {
		return new JMLObjectAdapterFactory();
	}
	
}


