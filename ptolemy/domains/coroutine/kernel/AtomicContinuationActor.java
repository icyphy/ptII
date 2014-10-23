/*
Below is the copyright agreement for the Ptolemy II system.

Copyright (c) 1995-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the above
copyright notice and the following two paragraphs appear in all copies
of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

Ptolemy II includes the work of others, to see those copyrights, follow
the copyright link on the splash page or see copyright.htm.
 */
/**
 *
 */
package ptolemy.domains.coroutine.kernel;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.NoRoomException;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.ObjectType;
import ptolemy.domains.coroutine.kernel.ControlEntryToken.EntryLocation;
import ptolemy.domains.coroutine.kernel.ControlExitToken.ExitLocation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * @author shaver
@version $Id$
@since Ptolemy II 10.0
 *
 */
public abstract class AtomicContinuationActor extends TypedAtomicActor
implements Continuation {

    public AtomicContinuationActor() {
        super();
    }

    public AtomicContinuationActor(Workspace workspace) {
        super(workspace);
    }

    public AtomicContinuationActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    private void _init() throws IllegalActionException,
    NameDuplicationException {
        _entries = new LinkedList();
        _exits = new LinkedList();

        _entryPoints = new HashMap();
        _entryPointsR = new HashMap();
        _exitPoints = new HashMap();
        _exitPointsR = new HashMap();

        // Creating control ports
        _entryPort = new TypedIOPort(this, "entryPort", true, false);
        _exitPort = new TypedIOPort(this, "exitPort", false, true);
        //

        BaseType t = BaseType.GENERAL;
        _initPoint = new TypedIOPort(this, "Init", true, false);
        _initPoint.setTypeEquals(t);
        _resumePoint = new TypedIOPort(this, "Resume", true, false);
        _resumePoint.setTypeEquals(t);

        _attachText("_iconDescription", "<svg>\n"
                + "<circle cx=\"0\" cy=\"0\" r=\"20\" style=\"fill:white\"/>\n"
                + "</svg>\n");
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#enter(ptolemy.domains.coroutine.kernel.ControlToken)
     */
    @Override
    public ControlExitToken controlEnter(ControlEntryToken entry)
            throws IllegalActionException {
        return null;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#init()
     */
    @Override
    public ControlExitToken controlInit() throws IllegalActionException {
        return controlEnter(ControlEntryToken.Init());
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#resume()
     */
    @Override
    public ControlExitToken controlResume() throws IllegalActionException {
        return controlEnter(ControlEntryToken.Resume());
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#enterAt(ptolemy.domains.coroutine.kernel.ControlEntryToken.EntryLocation)
     */
    @Override
    public ControlExitToken controlEnterAt(
            ControlEntryToken.EntryLocation location)
                    throws IllegalActionException {
        return controlEnter(ControlEntryToken.Enter(location));
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#entryLocation()
     */
    @Override
    public List<EntryLocation> entryLocations() {
        return _entries;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#exitLocation()
     */
    @Override
    public List<ExitLocation> exitLocations() {
        return _exits;
    }

    public void addEntryLocation(EntryLocation l)
            throws IllegalActionException, NameDuplicationException {
        if (_entryPoints.containsKey(l)) {
            return;
        }
        _entries.add(l);
        TypedIOPort lp = new TypedIOPort(this, l.name, true, false);
        lp.setTypeEquals(new ObjectType());

        _entryPoints.put(l, lp);
        _entryPointsR.put(lp, l);
    }

    public void addExitLocation(ExitLocation l) throws IllegalActionException,
    NameDuplicationException {
        if (_exitPoints.containsKey(l)) {
            return;
        }
        _exits.add(l);
        TypedIOPort lp = new TypedIOPort(this, l.name, false, true);
        lp.setTypeEquals(new ObjectType());

        _exitPoints.put(l, lp);
        _exitPointsR.put(lp, l);
    }

    public ControlEntryToken getEntryActionFromPort(TypedIOPort p) {
        if (p == _initPoint) {
            return ControlEntryToken.Init();
        }
        if (p == _resumePoint) {
            return ControlEntryToken.Resume();
        }
        if (!_entryPointsR.containsKey(p)) {
            return null;
        }
        EntryLocation el = _entryPointsR.get(p);
        return ControlEntryToken.Enter(el);
    }

    public ExitLocation getExitLocationFromPort(TypedIOPort p) {
        if (!_exitPointsR.containsKey(p)) {
            return null;
        }
        ExitLocation xl = _exitPointsR.get(p);
        return xl;
    }

    ///////////////////////////////////////////////////////////////////

    /* (non-Javadoc)
     * @see ptolemy.actor.AtomicActor#fire()
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        ControlEntryToken entk = ControlEntryToken.Resume();
        if (_entryPort.getWidth() >= 1 && _entryPort.isKnown()) {
            try {
                Token entryT = _entryPort.get(0);
                if (entryT instanceof ControlEntryToken) {
                    entk = (ControlEntryToken) entryT;
                } else if (entryT instanceof IntToken) {
                    List<ControlEntryToken.EntryLocation> entries = entryLocations();
                    int edex = ((IntToken) entryT).intValue();
                    if (entries != null && edex >= 2
                            && edex < entries.size() + 2) {
                        entk = ControlEntryToken.Enter(entries.get(edex - 2));
                    } else if (edex == 1) {
                        entk = ControlEntryToken.Resume();
                    } else if (edex == 0) {
                        entk = ControlEntryToken.Init();
                    }

                }
            } catch (NoTokenException e) {
                entk = ControlEntryToken.Resume();
            }
        }

        ControlExitToken extk = controlEnter(entk);
        try {
            _exitPort.send(0, extk);
        } catch (NoRoomException e) {
            e.printStackTrace();
        }
    }

    ///////////////////////////////////////////////////////////////////

    protected TypedIOPort _entryPort;
    protected TypedIOPort _exitPort;

    protected TypedIOPort _initPoint;
    protected TypedIOPort _resumePoint;

    protected HashMap<EntryLocation, TypedIOPort> _entryPoints;
    protected HashMap<TypedIOPort, EntryLocation> _entryPointsR;
    protected HashMap<ExitLocation, TypedIOPort> _exitPoints;
    protected HashMap<TypedIOPort, ExitLocation> _exitPointsR;

    private LinkedList<EntryLocation> _entries;
    private LinkedList<ExitLocation> _exits;

}
