package ptolemy.domains.coroutine.kernel;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class AccumLUB extends TypedAtomicActor {

    public AccumLUB() {
        super();
    }

    public AccumLUB(Workspace workspace) {
        super(workspace);
    }

    public AccumLUB(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.AtomicActor#fire()
     */
    // TODO This assumes consistency. I should check for it.
    @Override
    public void fire() throws IllegalActionException {
        int width = _ins.getWidth();
        for (int i = 0; i < width; ++i) {
            if (!_ins.isKnown(i)) continue;
            _out.broadcast(_ins.hasToken(i) ? _ins.get(i) : null);
            break;
        }
    }

    private void _init()
            throws IllegalActionException, NameDuplicationException {
        _ins = new TypedIOPort(this, "Ins", true, false);
        _out = new TypedIOPort(this, "Out", false, true);
        _ins.setMultiport(true);
    }

    public TypedIOPort _ins;
    public TypedIOPort _out;


}
