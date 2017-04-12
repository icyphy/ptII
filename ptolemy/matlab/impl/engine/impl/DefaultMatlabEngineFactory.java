/**
 * 
 */
package ptolemy.matlab.impl.engine.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import ptolemy.matlab.impl.backends.jmathlib.JMLEngine;
import ptolemy.matlab.impl.backends.mxapi.MxEngine;
import ptolemy.matlab.impl.engine.AbstractMatlabEngine;
import ptolemy.matlab.impl.engine.MatlabEngineFactory;

/**
 * Registers and gets Matlab(R)-compatible engine implementations and its semaphores.
 * 
 * @author David Guardado Barcia
 *
 */
public final class DefaultMatlabEngineFactory implements MatlabEngineFactory {
	
	private final  Map<String,Class<? extends AbstractMatlabEngine>> engineClasses = 
			new HashMap<String, Class<? extends AbstractMatlabEngine>>();

	private final  Map<String,Object> semaphores = new HashMap<String,Object>();

	@Override
	public AbstractMatlabEngine getEngine(final String engineType) {
		final Class<? extends AbstractMatlabEngine> engineClass = engineClasses.get(engineType);
		if (engineClass != null) {
			try {
				return engineClass.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				//TODO: replace this.
				System.out.println(e);
				return getDefaultEngine();
			}
		}
		return getDefaultEngine();
	}

	@Override
	public Object getEngineSemaphore(final String engineType) {
		return semaphores.get(engineType);
	}

	@Override
	public <T extends AbstractMatlabEngine> void registerEngineType(final String engineType, final Class<T> engineClass) {
		this.engineClasses.put(engineType, engineClass);
		this.semaphores.put(engineType, new Object());
	}
	
	private static AbstractMatlabEngine getDefaultEngine() {
		return new MxEngine();
	}

	@Override
	public Collection<String> getEngineTypes() {
		return this.semaphores.keySet();
	}
	
	/**
	 * Initialises this class' supported engine types.
	 * <p>New Matlab engine implementations may extend this method's body to register their own types.</p>
	 */
	@Override
	public void initEngineTypes() {
		this.registerEngineType(MxEngine.ENGINE_NAME, MxEngine.class);
		this.registerEngineType(JMLEngine.ENGINE_NAME, JMLEngine.class);
	}

}