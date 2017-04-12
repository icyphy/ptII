/**
 * Contains contributed implementations of {@link ptolemy.matlab.impl.engine.AbstractMatlabEngine} and its 
 *  collaborator classes.
 * 
 * <p>Each one of such contributions is expected to include the following artifacts:</p>
 * <ul>
 * <li>An implementation of {@link ptolemy.matlab.impl.engine.AbstractMatlabEngine}.</li>
 * <li>An implementation of {@link ptolemy.matlab.impl.engine.MatlabEngine.MatlabEngineInstance}.</li>
 * <li>An implementation of {@link ptolemy.matlab.impl.engine.adaption.MatlabObject}.</li>
 * <li>An implementation of {@link ptolemy.matlab.impl.engine.adaption.ObjectAdapterFactory}.</li>
 * </ul>
 * 
 * <p>Contributions are to be registered with PtolemyII via the {@link ptolemy.matlab.impl.engine.impl.DefaultMatlabEngineFactory} class, by 
 * extending the body of the {@link ptolemy.matlab.impl.engine.impl.DefaultMatlabEngineFactory#initEngineTypes()} method.</p>
 * 
 * @author David Guardado barcia
 *
 */
package ptolemy.matlab.impl.backends;