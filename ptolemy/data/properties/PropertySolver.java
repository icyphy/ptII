package ptolemy.data.properties;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelException;

public interface PropertySolver {

    /**
     * Resolve the property values for the given top-level entity.
     * @param topLevel The given top level entity.
     */
    public abstract void resolveProperties(CompositeEntity topLevel)
            throws KernelException;
    
    /**
     * Return the property helper for the given object. 
     * @param object The given object.
     * @return The property helper for the object.
     * @throws IllegalActionException Thrown if the helper cannot
     *  be found or instantiated.
     */
    public abstract PropertyHelper getHelper(Object object) 
            throws IllegalActionException;
}
