/**
 *
 */
package ptolemy.domains.coroutine.lib;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.coroutine.kernel.AtomicContinuationActor;
import ptolemy.domains.coroutine.kernel.ControlEntryToken;
import ptolemy.domains.coroutine.kernel.ControlEntryToken.EntryLocation;
import ptolemy.domains.coroutine.kernel.ControlExitToken;
import ptolemy.domains.coroutine.kernel.ControlExitToken.ExitLocation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * @author shaver
 *
 */
public class Counter extends AtomicContinuationActor {

    public Counter() {
        super();
    }

    public Counter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    public Counter(Workspace workspace) {
        super(workspace);
    }

    /////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.ContinuationActor#enter(ptolemy.domains.coroutine.kernel.ControlEntryToken)
     */
    @Override
    public ControlExitToken controlEnter(ControlEntryToken entry)
        throws IllegalActionException {

        ControlEntryToken.EntryLocation loc = null;
        ControlExitToken extk = null;

        Token alarmPToken = alarmP.getToken();
        if (alarmPToken instanceof IntToken) {
            _alarm = ((IntToken)alarmPToken).intValue();
        }

        /**/ if (entry.isInit())   { loc = resetEntry; }
        else if (entry.isResume()) { loc = _resumeLoc; }
        else if (entry.isEntry())  { loc = entry.getLocation(); }
        else { super.controlEnter(entry); }

        _currentCount = _count;
        while (true) {
            if (loc == resetEntry) {
                _currentCount = 0;
                _currentLoc = tickEntry;
                extk = ControlExitToken.Suspend();
                break;
            }
            if (loc == tickEntry) {
                _currentCount += 1;
                if (_currentCount >= _alarm && _alarm != 0) {
                    _currentLoc = tickEntry;
                    extk = ControlExitToken.Exit(alarmExit);
                    break;
                }
                else {
                    _currentLoc = tickEntry;
                    extk = ControlExitToken.Suspend();
                    break;
                }
            }
            if (loc == setEntry) {
                try {
                    Token alarmT = _alarmIn.get(0);
                    if (alarmT instanceof IntToken) {
                        IntToken alarmIntT = (IntToken)alarmT;
                        _alarm = alarmIntT.intValue();
                        alarmP.setToken(new IntToken(_alarm));
                    }
                } catch (NoTokenException e) {
                    e.printStackTrace();
                }
                loc = tickEntry;
                continue;
            }
            break;
        }

        try {
            _countOut.send(0, new IntToken(_currentCount));
        } catch (NoRoomException e) {
            e.printStackTrace();
        }

        if (extk != null) return extk;
        else return super.controlEnter(entry);
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.AtomicActor#initialize()
     */
    @Override
    public void initialize() throws IllegalActionException {
        _alarm = 0;
        _count = _currentCount = 0;
        _resumeLoc = _currentLoc = resetEntry;
        super.initialize();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.AtomicActor#postfire()
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        _count = _currentCount;
        _resumeLoc = _currentLoc;
        return super.postfire();
    }


    public TypedIOPort _alarmIn;
    public TypedIOPort _countOut;

    ///////////////////////////////////////////////////////

    protected void _init() throws
        IllegalActionException, NameDuplicationException {
        _alarm = 0;
        _count = _currentCount = 0;

        addEntryLocation(resetEntry);
        addEntryLocation(setEntry);
        addEntryLocation(tickEntry);
        addExitLocation(alarmExit);

        _alarmIn  = new TypedIOPort(this, "AlarmIn",  true,  false);
        _countOut = new TypedIOPort(this, "CountOut", false, true);

        _resumeLoc = _currentLoc = resetEntry;

        alarmP = new Parameter(this, "alarm");

    }

    public Parameter alarmP;

    private int _count, _currentCount;
    private int _alarm;

    final public EntryLocation resetEntry = new EntryLocation("reset");
    final public EntryLocation tickEntry  = new EntryLocation("tick");
    final public EntryLocation setEntry   = new EntryLocation("set");
    final public ExitLocation  alarmExit  = new ExitLocation("alarm");

    private EntryLocation _resumeLoc, _currentLoc;

}






