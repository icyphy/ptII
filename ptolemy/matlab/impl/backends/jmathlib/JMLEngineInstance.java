package ptolemy.matlab.impl.backends.jmathlib;

import java.util.UUID;

import jmathlib.core.interpreter.Interpreter;
import ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance;

public class JMLEngineInstance extends MatlabEngineInstance {
	
	public final Interpreter interpreter;

	public JMLEngineInstance(final int outputBufferSize) {
		id = UUID.randomUUID().toString();
		interpreter = new Interpreter(true);
		interpreter.setOutputPanel(new JMLEngineOutputBuffer(outputBufferSize));
		interpreter.throwErrorsB = true;
	}

	public Interpreter getInterpreter() {
		return interpreter;
	}
	
}