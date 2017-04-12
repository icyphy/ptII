/* Matlab MathworksEngine Interface

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
package ptolemy.matlab.impl.backends.mxapi;

import ptolemy.data.StringToken;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.backends.mxapi.adapters.MxObject;
import ptolemy.matlab.impl.backends.mxapi.adapters.MxObjectAdapterFactory;
import ptolemy.matlab.impl.backends.mxapi.jni.JniPtMatlab;
import ptolemy.matlab.impl.engine.AbstractMatlabEngine;
import ptolemy.matlab.impl.engine.MatlabEngine;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ObjectAdapterFactory;

/**
 A bridge with Mathworks' Mx API methods that calls them throughout
 a {@link PtMatlab} implementation.
 
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

 @author Zoltan Kemenczy and Sean Simmons, Research in Motion Limited.
 @author David Guardado Barcia
 */
public class MxEngine extends AbstractMatlabEngine {
	
	public static final String ENGINE_NAME = "MxEngine";

	/** Copy of a common error message. */
	static String ERROR_MSG_NOT_OPENED = " invalid engine instance (no such instance found open)";

	final PtMatlab ptMatlab;

	/** Construct an instance of the Mathworks Matlab (R) engine interface.
	 * The Matlab engine is not activated at this time.
	 * <p>
	 * Ptmatlab.dll is loaded by the system library loader the
	 * first time this class is loaded.
	 * @see #open()
	 */
	public MxEngine() {
		super(ENGINE_NAME);
		ptMatlab = new JniPtMatlab();
	}

	/** Opens a connection to a matlab engine installed on
	 * this host with its output buffered.
	 * 
	 * @return long[2] retval engine handle; retval[0] is the real
	 * engine handle, retval[1] is a pointer to the engine output
	 * buffer; both should be preserved and passed to subsequent engine
	 * calls.
	 * 
	 * @exception IllegalActionException If the matlab engine open is
	 * unsuccessful.  This will typically occur if ptmatlab (.dll)
	 * cannot be located or if the matlab bin directory is not in the
	 * path.
	 * 
	 * @see #open(String, boolean)
	 */
	@Override
	public MatlabEngine.MatlabEngineInstance open(String startCmd, boolean needOutput)
			throws IllegalActionException {

		long[] matlabInstance = new long[2];

		synchronized (semaphore) {
			matlabInstance[0] = ptMatlab.ptmatlabEngOpen(startCmd);
			if (matlabInstance[0] == PtMatlab.INVALID_POINTER) {
				throw new IllegalActionException(
						logHeader() + " .open("
								+ startCmd
								+ ") : can't find Matlab engine. "
								+ "The PATH for this process is \""
								+ getPathSystemVariable()
								+ "\". Try starting "
								+ "\"matlab\" by hand from a shell to verify that "
								+ "Matlab is set up properly and the license is "
								+ "correct.\n"
								+ "Under Windows, try running \"matlab /regserver\", "
								+ "the Matlab C API communicates with Matlab via COM, "
								+ "and apparently the COM interface is not "
								+ "automatically registered when Matlab is "
								+ "installed.\n"
								+ "Under Mac OS X, 'matlab' must be in the PATH, "
								+ "it may be easiest to create a link from /usr/bin/matlab "
								+ "to the location of the matlab script:\n "
								+ "sudo ln -s /Applications/MATLAB_R2011a.app/bin/matlab /usr/bin/matlab\n"
								+ "Under Linux and other types of UNIX, csh must be "
								+ "installed in /bin/csh.");
			}

			if (needOutput) {
				matlabInstance[1] = ptMatlab.ptmatlabEngOutputBuffer(matlabInstance[0],DEFAULT_OUTPUT_BUFFER_SIZE);
			}

			if (getDebugLogsFlag() > 0) {
				System.out.println(logHeader(matlabInstance[0]) + " -> open(\"" + startCmd + "\") suceeded");
			}
		}

		return new MxEngineInstance(matlabInstance);
	}

	/* (non-Javadoc)
	 * @see ptolemy.matlab.MatlabSharedBridge#close(long[])
	 */
	@SuppressWarnings("null")
	@Override
	public void close(MatlabEngine.MatlabEngineInstance engInstance) throws IllegalActionException {
		
		long[] eng = MxEngineInstance.asPtmatlabId(engInstance);
		synchronized (semaphore) {
			if (eng == null || eng[0] == 0) {
				throw new IllegalActionException(logHeader(eng[0]) + ERROR_MSG_NOT_OPENED);
			}			
			if (PtMatlab.SUCCESS !=  ptMatlab.ptmatlabEngClose(eng[0], eng[1])) {
				final String errorMessage = logHeader(eng[0]) + " -> .close(" + eng[0] + ") failed";
				System.out.println(errorMessage);
				throw new IllegalActionException(errorMessage);
			} else if (getDebugLogsFlag() > 0) {
				System.out.println(logHeader(eng[0]) + " -> .close(" + eng[0] + ") succeeded");
			}
		}

	}

	/* (non-Javadoc)
	 * @see ptolemy.matlab.MatlabSharedBridge#evalString(long[], java.lang.String)
	 */
	@SuppressWarnings("null")
	@Override
	public void evalString(MatlabEngine.MatlabEngineInstance engInstance, String evalStr)
			throws IllegalActionException {
		
		long[] eng = MxEngineInstance.asPtmatlabId(engInstance);
		synchronized (semaphore) {
			if (eng == null || eng[0] == 0) {
				throw new IllegalActionException(logHeader(eng[0]) + ERROR_MSG_NOT_OPENED);
			}
			if (PtMatlab.SUCCESS != ptMatlab.ptmatlabEngEvalString(eng[0], evalStr)) {
				final String errorMessage = logHeader(eng[0]) + " -> evalStr(" + evalStr + ") failed";
				System.out.println(errorMessage);
				throw new IllegalActionException(errorMessage);
			} else if (getDebugLogsFlag() > 0) {
				System.out.println(logHeader(eng[0]) + " -> evalStr(" + evalStr + ") succeeded");
			}
		}

	}

	/* (non-Javadoc)
	 * @see ptolemy.matlab.MatlabSharedBridge#getOutput(long[])
	 */
	@SuppressWarnings("null")
	@Override
	public StringToken getOutput(MatlabEngine.MatlabEngineInstance engInstance) throws IllegalActionException {
		long[] eng = MxEngineInstance.asPtmatlabId(engInstance);
		String outputString = "";
		synchronized (semaphore) {
			if (eng == null || eng[0] == 0) {
				throw new IllegalActionException(logHeader(eng[0]) + ERROR_MSG_NOT_OPENED);
			}
			outputString = ptMatlab.ptmatlabGetOutput(eng[1], DEFAULT_OUTPUT_BUFFER_SIZE);
			if (null == outputString) {
				final String errorMessage = logHeader(eng[0]) + " -> getOutput(" + eng[1]+ ") failed";
				System.out.println(errorMessage);
				throw new IllegalActionException(errorMessage);
			} else if (getDebugLogsFlag() > 0) {
				System.out.println(logHeader(eng[0]) + " -> getOutput(" + eng[1]+ ") succeeded; output = <<" + outputString + ">>");
			}
		}
		return new StringToken(outputString);
	}

	@SuppressWarnings("null")
	@Override
	protected void putMatlabObject( MatlabEngineInstance engInstance, String name, MatlabObject matlabObject) throws IllegalActionException {
		
		final MxObject mxObject = (MxObject) matlabObject;
		long[] eng = MxEngineInstance.asPtmatlabId(engInstance);
		if (eng == null || eng[0] == 0) {
			throw new IllegalActionException(logHeader(eng[0]) + ERROR_MSG_NOT_OPENED);
		}
		try {
			if (PtMatlab.SUCCESS != ptMatlab.ptmatlabEngPutArray(eng[0], name, mxObject.getMxArray())) {
				final String errorMessage = logHeader(eng[0]) + " -> putArray() failed";
				System.out.println(errorMessage);
				throw new IllegalActionException(errorMessage);
			} else if (getDebugLogsFlag() > 0) {
				System.out.println(logHeader(eng[0]) + " -> putArray() succeeded");
			}			
		} finally {
			ptMatlab.ptmatlabDestroy(mxObject.getMxArray(), name);			
		}
		
	}

	@SuppressWarnings("null")
	@Override
	protected MatlabObject getMatlabObject(MatlabEngineInstance engInstance, String name) throws IllegalActionException {
		long[] eng = MxEngineInstance.asPtmatlabId(engInstance);
		if (eng == null || eng[0] == 0) {
			throw new IllegalActionException(logHeader(eng[0]) + ERROR_MSG_NOT_OPENED);
		}
		long ma = ptMatlab.ptmatlabEngGetArray(eng[0], name);
		if (ma == 0) {
			throw new IllegalActionException(this.name + " -> " + eng[0] +" -> get(" + name + "): can't find matlab variable \"" + name
					+ "\"\n" + getOutput(engInstance).stringValue());
		}
		return new MxObject(ptMatlab,ma,name);
	}

	@Override
	protected void releaseMatlabObject(MatlabObject matlabObject, final String name) {
		MxObject mxObject = (MxObject) matlabObject;
		ptMatlab.ptmatlabDestroy(mxObject.getMxArray(), name);
	}

	@Override
	protected ObjectAdapterFactory getObjectAdapterFactory() {
		return new MxObjectAdapterFactory(ptMatlab);
	}

	private static String getPathSystemVariable() {
		String Path = "";
		try {
			Path = System.getenv("PATH");
		} catch (Throwable throwable) {
			Path = throwable.toString();
		}
		return Path;
	}

}

