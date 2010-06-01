package ptolemy.domains.sdf.optimize;

import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

public abstract class SharedBufferTransformer extends Transformer implements BufferingProfile {

    private boolean _nextIterationExclusive;

    @Override
    public void initialize() throws IllegalActionException {
        // default to copying firing
        this._nextIterationExclusive = false;
        super.initialize();
    }

    public SharedBufferTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }
    
    public void fire() throws IllegalActionException {
        if(this._nextIterationExclusive){
            this.fireExclusive();
        } else {
            this.fireCopying();
        }
    }

    public int sharedBuffers() {
        return 1;
    }

    public int exclusiveBuffers() {
        return 0;
    }

    public int sharedExecutionTime() {
        return 1;
    }

    public int exclusiveExecutionTime() {
        return 2;
    }

    protected abstract void fireExclusive() throws IllegalActionException;

    protected abstract void fireCopying() throws IllegalActionException;
 
    public int iterate(int iterationCount, boolean fireExclusive)
        throws IllegalActionException {
        this._nextIterationExclusive  = fireExclusive;
        int result = super.iterate(iterationCount);
        // default to copying firing
        this._nextIterationExclusive = false;
        return result;
    }
}
