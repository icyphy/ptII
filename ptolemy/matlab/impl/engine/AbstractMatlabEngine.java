package ptolemy.matlab.impl.engine;

import java.util.logging.Level;
import java.util.logging.Logger;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.matlab.impl.engine.adaption.MatlabObject;
import ptolemy.matlab.impl.engine.adaption.ObjectAdapterFactory;
import ptolemy.matlab.impl.engine.impl.ObjectAdapter;

/**
 *  Abstract, partial implementation of {@link MatlabEngine}. It governs the transformations between 
 *  Ptolemy II and Matlab types and provides with additional services, such as logging.
 *  
 *  <p>Every engine implementation shall extend this class for their functionality to be 
 *  available to PtolemyII final users. See the {@link ptolemy.matlab.impl.backends} for details 
 *  and example implementations.</p>
 *  
 *  @author Zoltan Kemenczy and Sean Simmons, Research in Motion Limited.
 *  @author David Guardado
 */
public abstract class AbstractMatlabEngine implements MatlabEngine {

	/** Output buffer (allocated for each opened instance) size. */
	protected static final int DEFAULT_OUTPUT_BUFFER_SIZE = 2048;

	private byte debug;
	private  final Logger logger;
	protected final String name;
	protected Object semaphore;

 	public AbstractMatlabEngine(final String name) {
		this(name,NO_DEBUG_LOGS);
	}

	public AbstractMatlabEngine(final String name, final byte debug) {
		this.name = name;
		this.logger = Logger.getLogger(ROOT_LOGGING_CATEGORY);
		if (debug != NO_DEBUG_LOGS) {
			this.logger.setLevel(Level.ALL);
		} else {
			this.logger.setLevel(Level.SEVERE);
		}
		this.setSemaphore(new Object());
	}

	@Override
	public final void setSemaphore(final Object object) {
		if (this.semaphore == null) {
			this.semaphore = object;			
		}
	}

	@Override
	public final MatlabEngine.MatlabEngineInstance open() throws IllegalActionException {
		return open(null, true);
	}

	@Override
	public final MatlabEngine.MatlabEngineInstance open(boolean needOutput) throws IllegalActionException {
		return open(null, needOutput);
	}

	@Override
	public final Token get(MatlabEngine.MatlabEngineInstance eng, String name) throws IllegalActionException {
		return get(eng, name, new ConversionParameters());
	}

	@Override
	public final Token get(MatlabEngine.MatlabEngineInstance engInstance, String name, ConversionParameters conversionParameters) throws IllegalActionException {

		synchronized (semaphore) {
			final MatlabObject matlabObject = getMatlabObject(engInstance, name);
			final Token token = buildObjectAdapter(conversionParameters).adapt(matlabObject,null);
			releaseMatlabObject(matlabObject, name);
			logDebug(logHeader() + "get(" + name + ") = " + token.toString());
			return token;
		}

	}

	@Override
	public final void put(final MatlabEngine.MatlabEngineInstance engInstance, String name, Token token)
			throws IllegalActionException {

		synchronized (semaphore) {
			if (name == null) {
				throw new NullPointerException(); // Do it here, while we can.
			}
			if (name.isEmpty()) {
				logDebug(logHeader(engInstance.id) + "put(" + name + ", " + token.toString() + ")");
				throw new IllegalActionException(logHeader(engInstance.id) + " variables' names cannot be empty");
			}
			logDebug(logHeader(engInstance.id) + "put(" + name + ", " + token.toString() + ")");
			final MatlabObject matlabObject = buildObjectAdapter().adapt(token,name);
			putMatlabObject(engInstance, name, matlabObject);
		}

	}

	@Override
	public final void setDebugLogs(byte d) {
		debug = d;
	}

	public final byte getDebugLogsFlag() {
		return debug;
	}

	public final void log(final String message) {
		logger.log(Level.INFO,message);
	}

	public final void logDebug(final String message) {
		logger.log(Level.FINE,message);
	}

	@Override
	public final String logHeader() {
		return this.name + " -> ";
	}

	@Override
	public final String logHeader(final String id) {
		return logHeader() + id;
	}

	@Override
	public final String logHeader(final long id) {
		return logHeader() + id;
	}

	@Override
	public final String logHeader(final int id) {
		return logHeader() + id;
	}

	protected final IllegalActionException buildInvalidInstanceException(MatlabEngineInstance instance) {
		return  new IllegalActionException(logHeader() + " invalid engine instance id: " + instance.id);
	}

	protected abstract void putMatlabObject( MatlabEngineInstance engInstance, String name, MatlabObject matlabObject) throws IllegalActionException;
	protected abstract MatlabObject getMatlabObject(MatlabEngineInstance engInstance, String name) throws IllegalActionException ;
	protected abstract void releaseMatlabObject(MatlabObject matlabObject, String name);
	protected abstract ObjectAdapterFactory getObjectAdapterFactory();

	private ObjectAdapter buildObjectAdapter() {
		return buildObjectAdapter(null);
	}

	private ObjectAdapter buildObjectAdapter(final ConversionParameters conversionParams) {
		final ObjectAdapter objectAdapter = new ObjectAdapter(conversionParams);
		objectAdapter.setAdapterFactory(getObjectAdapterFactory());
		return objectAdapter;
	}
	
}