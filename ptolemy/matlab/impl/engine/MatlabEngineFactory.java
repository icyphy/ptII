package ptolemy.matlab.impl.engine;

import java.util.Collection;

/**
 * Represents a factory able to produce instances of Matlab (R) engines and its associated semaphore objects.
 * 
 * @author David Guardado barcia
 *
 */
public interface MatlabEngineFactory {
	
	/**
	 * Returns a Matlab(R)-compatible engine of a requested type.
	 * 
	 * @param engineType type of the engine instance to return
	 * @return a Matlab(R)-compatible engine instance
	 */
	AbstractMatlabEngine getEngine(final String engineType);
	
	/**
	 * Returns a semaphore object to be used by a family of Matlab(R)-compatible engines.
	 * @param engineType the type of the family of engines, all of which shall share the returned semaphore object
	 * @return a semaphore object
	 */
	Object getEngineSemaphore(final String engineType);
	
	/**
	 * Registers an engine type (and its class) with this factory, making it thus supported.
	 * @param engineType engine type identifier
	 * @param engineClass the engine class - one extending {@link AbstractMatlabEngine}
	 */
	<T extends AbstractMatlabEngine> void registerEngineType(final String engineType,final Class<T> engineClass);
	
	/**
	 * Returns the engine types supported by this factory.
	 * @return a collection with the engine types supported by this factory
	 */
	public Collection<String> getEngineTypes();

	/**
	 * Initialises the default set of supported engine types.
	 */
	void initEngineTypes();

}