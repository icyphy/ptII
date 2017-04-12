package ptolemy.matlab.itests.impl.backends.mxapi;

import ptolemy.matlab.impl.backends.mxapi.MxEngine;
import ptolemy.matlab.itests.AbstractMatlabEngineTests;

/**
 * Tests for {@link MxEngine}.
 * 
 * @author david
 *
 */
public class MxEngineTests extends AbstractMatlabEngineTests {
	public MxEngineTests() {
		testedEngine = new MxEngine();		
	}
}
