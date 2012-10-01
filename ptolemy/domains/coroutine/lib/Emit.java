package ptolemy.domains.coroutine.lib;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.Token;
import ptolemy.domains.coroutine.kernel.AtomicContinuationActor;
import ptolemy.domains.coroutine.kernel.ControlEntryToken;
import ptolemy.domains.coroutine.kernel.ControlExitToken;
import ptolemy.domains.coroutine.kernel.ControlExitToken.ExitLocation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class Emit extends AtomicContinuationActor {

    public Emit() {
        super();
    }

    public Emit(Workspace workspace) {
        super(workspace);
    }

    public Emit(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    ///////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.ContinuationActor#enter(ptolemy.domains.coroutine.kernel.ControlEntryToken)
     */
    @Override
    public ControlExitToken controlEnter(ControlEntryToken entry)
            throws IllegalActionException {

        int insize  = _inputs.getWidth(),
            outsize = _outputs.getWidth(),
            maxsize = insize < outsize ? insize : outsize;

        try {
            for (int k = 0; k < maxsize; ++k) {
                Token inToken = _inputs.get(k);
                _outputs.send(k, inToken);
            }
        }
        catch (NoRoomException e) {
            e.printStackTrace();
        }
        catch (NoTokenException e) {
            e.printStackTrace();
        }

        return ControlExitToken.Exit(nextExit);
    }

    public TypedIOPort _inputs;
    public TypedIOPort _outputs;

    ///////////////////////////////////////////////////////////////////

    protected void _init() throws
            IllegalActionException, NameDuplicationException {

        addExitLocation(nextExit);

        _inputs  = new TypedIOPort(this, "Inputs",  true, false);
        _outputs = new TypedIOPort(this, "Outputs", false, true);
    }

    final public ExitLocation nextExit = new ExitLocation("next");

}
