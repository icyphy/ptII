package ptolemy.domains.coroutine.lib;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.coroutine.kernel.AtomicContinuationActor;
import ptolemy.domains.coroutine.kernel.ControlEntryToken;
import ptolemy.domains.coroutine.kernel.ControlExitToken;
import ptolemy.domains.coroutine.kernel.ControlExitToken.ExitLocation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class Conditional extends AtomicContinuationActor {

    public Conditional() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Conditional(Workspace workspace) {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    public Conditional(CompositeEntity container, String name)
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

        ControlExitToken extk = null;
        int insize = _inputs.getWidth();

        Boolean val = true;

        try {
            for (int k = 0; k < insize; ++k) {
                Token inToken = _inputs.get(k);
                if (inToken instanceof BooleanToken) {
                    val = val && ((BooleanToken)inToken).booleanValue();
                }
            }
            extk = ControlExitToken.Exit(val ? trueExit : falseExit);
        }
        catch (NoRoomException e) {
            e.printStackTrace();
        }
        catch (NoTokenException e) {
            e.printStackTrace();
        }

        if (extk != null) return extk;
        else return super.controlEnter(entry);
    }

    public TypedIOPort _inputs;

    ///////////////////////////////////////////////////////////////////

    protected void _init() throws
        IllegalActionException, NameDuplicationException {

        addExitLocation(trueExit);
        addExitLocation(falseExit);

        _inputs = new TypedIOPort(this, "Inputs",  true, false);
        _inputs.setTypeEquals(BaseType.BOOLEAN);
    }

    final public ExitLocation trueExit  = new ExitLocation("true");
    final public ExitLocation falseExit = new ExitLocation("false");

}
