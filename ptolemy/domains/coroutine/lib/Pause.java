package ptolemy.domains.coroutine.lib;

import ptolemy.domains.coroutine.kernel.AtomicContinuationActor;
import ptolemy.domains.coroutine.kernel.ControlEntryToken;
import ptolemy.domains.coroutine.kernel.ControlEntryToken.EntryLocation;
import ptolemy.domains.coroutine.kernel.ControlExitToken;
import ptolemy.domains.coroutine.kernel.ControlExitToken.ExitLocation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

public class Pause extends AtomicContinuationActor {

    public Pause() {
        // TODO Auto-generated constructor stub
    }

    public Pause(Workspace workspace) {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    public Pause(CompositeEntity container, String name)
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

        ControlEntryToken.EntryLocation loc = null;
        ControlExitToken extk = null;

        /**/ if (entry.isInit())   { loc = pauseEntry; }
        else if (entry.isResume()) { loc = _resumeLoc; }
        else if (entry.isEntry())  { loc = entry.getLocation(); }
        else { super.controlEnter(entry); }

        while (true) {
            if (loc == pauseEntry) {
                _currentLoc = resumeEntry;
                extk = ControlExitToken.Suspend();
                break;
            }
            if (loc == resumeEntry) {
                _currentLoc = pauseEntry;
                extk = ControlExitToken.Exit(nextExit);
                break;
            }
            break;
        }

        if (extk != null) return extk;
        else return super.controlEnter(entry);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.AtomicActor#postfire()
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _resumeLoc = _currentLoc;
        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////

    protected void _init() throws
        IllegalActionException, NameDuplicationException {

        addEntryLocation(pauseEntry);
        addEntryLocation(resumeEntry);
        addExitLocation(nextExit);
    }


    final public EntryLocation pauseEntry  = new EntryLocation("pause");
    final public EntryLocation resumeEntry = new EntryLocation("resume");
    final public ExitLocation  nextExit    = new ExitLocation("next");
    private EntryLocation _resumeLoc, _currentLoc;
}
