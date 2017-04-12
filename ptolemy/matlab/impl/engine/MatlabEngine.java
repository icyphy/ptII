package ptolemy.matlab.impl.engine;

import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.Expression;
import ptolemy.matlab.impl.backends.mxapi.MxEngine;
import ptolemy.matlab.impl.engine.impl.ObjectAdapter;
import ptolemy.matlab.impl.utils.LoggingContext;

/**
 * Represents an engine able to satisfy computation requests issued to an external Matlab (R) 
 * or Matlab(R)-compatible program. See {@link AbstractMatlabEngine} for the mandatory abstract 
 * implementation that clients shall extend.
 * 
 *  <p>The bulk of the work done by implementing classes is assumed to be the conversion between
 PtolemyII Tokens and matlab variables. See {@link ObjectAdapter} for details 
 about how Matlab (R) types are mapped into PtolemyII types and the other way around.<p>

 @author Zoltan Kemenczy and Sean Simmons, Research in Motion Limited.
 @author David Guardado Barcia
 *
 */
public interface MatlabEngine extends LoggingContext {
	
	public static final byte NO_DEBUG_LOGS = 0;
	public static final byte DEBUG_LOGS = 1;
	
	/**
	 * Represents an engine instance within the set of all instances 
	 * of a given type.
	 * 
	 * @author David Guardado Barcia
	 *
	 */
	public static abstract class MatlabEngineInstance {
		public String id;
	}
	
    /** Data conversion parameters. These affect 
     * how Matlab objects are converted into PtolemyII objects and 
     * the other way around. */
    public static class ConversionParameters {
    	
        /** If true (default), 1x1 matrices are returned as
         * appropriate ScalarToken.*/
        public boolean getScalarMatrices = true;

        /** If true, double matrices where all elements represent
         * integers are returned as IntMatrixTokens (default false).*/
        public boolean getIntMatrices = false;
        
    }
	
    /**
     * Sets the semaphore object shared between all engine instances of the same type as this one.
     * @see MatlabEngineFactory#getEngineSemaphore(String)
     */
	public void setSemaphore(final Object object);
	
	/** Enable/disable debug-level logs.
	 * 
	 * @param d Non-zero to enable debug statements, zero to disable.
	 */
	void setDebugLogs(byte d);

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
	MatlabEngineInstance open() throws IllegalActionException;

	/** Open a connection to the default matlab engine installed on
	 * this host with specified output buffering.
	 * @param needOutput selects whether the output should be buffered
	 * or not.
	 * @return long[2] retval engine handle; retval[0] is the real
	 * engine handle, retval[1] is a pointer to the engine output
	 * buffer; both should be preserved and passed to subsequent engine
	 * calls.
	 * @exception IllegalActionException If the matlab engine open is
	 * unsuccessful.  This will typically occur if ptmatlab (.dll)
	 * cannot be located or if the matlab bin directory is not in the
	 * path.
	 * @see #open(String, boolean)
	 */
	MatlabEngineInstance open(boolean needOutput) throws IllegalActionException;

	/** Open a connection to a matlab engine.<p>
	 * For more information, see the matlab engine API reference engOpen()
	 * @param startCmd hostname or command to use to start the engine.
	 * @param needOutput selects whether the output should be buffered
	 * or not.
	 * @return long[2] retval engine handle; retval[0] is the real
	 * engine handle, retval[1] is a pointer to the engine output
	 * buffer; both should be preserved and passed to subsequent engine
	 * calls.
	 * @exception IllegalActionException If the matlab engine open is
	 * unsuccessful.  This will typically occur if ptmatlab (.dll)
	 * cannot be located or if the matlab bin directory is not in the
	 * path.
	 * @see #getOutput(long[])
	 */
	MatlabEngineInstance open(String startCmd, boolean needOutput) throws IllegalActionException;

	/** Close a connection to a matlab engine.
	 * This will also close the matlab engine if this instance was the last
	 * user of the matlab engine.
	 * <p>
	 * For more information, see matlab engine API reference engClose()
	 * @param eng An array of longs with length 2. eng[0] is the real
	 * engine handle, eng[1] is a pointer to the engine output
	 * buffer.
	 * @return The value returned by calling engClose() in the
	 * Matlab interface.
	 * @throws IllegalActionException 
	 */
	void close(MatlabEngineInstance eng) throws IllegalActionException;

	/** Send a string for evaluation to the matlab engine.
	 * @param eng An array of two longs; eng[0] is the real
	 * engine handle, eng[1] is a pointer to the engine output
	 * buffer.
	 * @param evalStr string to evaluate.
	 * @return The value returned by the ptmatlabEngEvalString() native method.
	 * @exception IllegalActionException If the matlab engine is not opened.
	 */
	void evalString(MatlabEngineInstance eng, String evalStr) throws IllegalActionException;

	/** Return a Token from the matlab engine using default
	 * {@link MxEngine.ConversionParameters} values.
	 * @param eng An array of longs with length 2. eng[0] is the real
	 * engine handle, eng[1] is a pointer to the engine output
	 * buffer.
	 * @param name Matlab variable name used to initialize the returned Token
	 * @return PtolemyII Token.
	 * @exception IllegalActionException If the matlab engine is not opened, or
	 * if the matlab variable was not found in the engine. In this case, the
	 * matlab engine's stdout is included in the exception message.
	 * @see Expression
	 */
	Token get(MatlabEngineInstance eng, String name) throws IllegalActionException;

	/** Return a Token from the matlab engine using specified
	 * {@link MxEngine.ConversionParameters} values.
	 * @param eng An array of longs with length 2. eng[0] is the real
	 * engine handle, eng[1] is a pointer to the engine output
	 * buffer.
	 * @param name Matlab variable name used to initialize the returned Token
	 * @param par The ConversionParameter to use.
	 * @return PtolemyII Token.
	 * @exception IllegalActionException If the matlab engine is not opened, or
	 * if the matlab variable was not found in the engine. In this case, the
	 * matlab engine's stdout is included in the exception message.
	 * @see Expression
	 */
	Token get(MatlabEngineInstance eng, String name, ConversionParameters par) throws IllegalActionException;

	/** Get last matlab stdout.
	 * @param eng An array of longs with length 2. eng[0] is the real
	 * engine handle, eng[1] is a pointer to the engine output
	 * buffer.
	 * @return PtolemyII StringToken
	 * @throws IllegalActionException 
	 */
	StringToken getOutput(MatlabEngineInstance eng) throws IllegalActionException;

	/** Create a matlab variable using name and a Token.
	 * @param eng An array of longs with length 2. eng[0] is the real
	 * engine handle, eng[1] is a pointer to the engine output
	 * buffer.
	 * @param name matlab variable name.
	 * @param t Token to provide value.
	 * @return The result of calling engPutArray() in the Matlab
	 * C library.
	 * @exception IllegalActionException If the engine is not opened.
	 * @see MxEngine
	 */
	void put(MatlabEngineInstance eng, String name, Token t) throws IllegalActionException;


}