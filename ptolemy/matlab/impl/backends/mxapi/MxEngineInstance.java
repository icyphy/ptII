package ptolemy.matlab.impl.backends.mxapi;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.engine.MatlabEngine;

public class MxEngineInstance extends MatlabEngine.MatlabEngineInstance {

	private final static String separator = ":";

	public MxEngineInstance(final long[] ptmatlabId) {
		this.id = String.valueOf(ptmatlabId[0]) + separator + String.valueOf(ptmatlabId[1]);
	}

	public static long[] asPtmatlabId(final MatlabEngine.MatlabEngineInstance i) throws IllegalActionException {
		final String[] chunks = i.id.split(separator);
		if (chunks == null || chunks.length != 2) {
			throw new IllegalActionException("invalid Mathworks instance");
		}
		try {
			long[] ptmatlabId = new long[2];
			ptmatlabId[0] = Long.valueOf(chunks[0]);
			ptmatlabId[1] = Long.valueOf(chunks[1]);
			return ptmatlabId;
		} catch (NumberFormatException e) {
			throw new IllegalActionException("invalid Mathworks instance: id is invalid (" + e.getMessage() + ")" );			
		}
	}
}