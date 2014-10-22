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
import java.util.Map;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.Executable;
import ptolemy.actor.IOPort;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.sched.FixedPointReceiver;
import ptolemy.data.ArrayToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.coroutine.kernel.ControlEntryToken.EntryLocation;
import ptolemy.domains.coroutine.kernel.ControlExitToken.ExitLocation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

/**
 * @author shaver
@version $Id$
@since Ptolemy II 10.0
 *
 */
public class CoroutineDirector extends Director implements Continuation {

    ///////////////////////////////////////////////////////////////////
    public CoroutineDirector() throws IllegalActionException,
            NameDuplicationException {
        super();
    }

    public CoroutineDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    public CoroutineDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        _init();
    }

    private void _init() throws IllegalActionException,
            NameDuplicationException {
        initContinuationName = new Parameter(this, "initContinuationName");
        initContinuationName.setStringMode(true);

        entryMapping = new Parameter(this, "entryMapping");
        nextMapping = new Parameter(this, "nextMapping");
        exitMapping = new Parameter(this, "exitMapping");

        _receivers = new LinkedList();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.Director#fire()
     */
    @Override
    public void fire() throws IllegalActionException {
        InternalEntryLocation currL = _currentLocation;
        _nextLocation = null;
        _currentControlPath = new LinkedList();
        while (true) {
            if (_debugging) {
                _debug("\nEntering : " + currL.continuation());
            }
            ControlExitToken ex = _enterWith(currL);
            _currentControlPath.add(currL.continuation());
            if (ex == null) {
                // Throw
                break;
            }
            if (ex.isSuspend()) {
                if (_debugging) {
                    _debug("\nSuspending.\n");
                }
                _nextLocation = new InternalEntryLocation(currL.continuation(),
                        ControlEntryToken.Resume());
                break;
            }
            if (ex.isTerminate()) {
                if (_debugging) {
                    _debug("\nTerminating.\n");
                }
                _nextLocation = new InternalEntryLocation(_initContinuation,
                        ControlEntryToken.Init());
                break;
            }
            if (ex.isLocation()) {
                InternalExitLocation iex = _exitFrom(currL, ex);
                if (_debugging) {
                    _debug("\nExiting with location : " + ex.getLocation()
                            + "\n");
                }
                if (iex == null) {
                    break;
                }

                // Add exit logic
                if (_nextMap.containsKey(iex)) {
                    InternalEntryLocation nloc = _nextMap.get(iex);
                    if (_debugging) {
                        _debug("\nMoving to continuation : "
                                + nloc.continuation() + ", with entry : "
                                + nloc.entry() + "\n");
                    }
                    currL = nloc;
                    if (currL == _currentLocation) {
                        if (_debugging) {
                            _debug("\nControl cycle.");
                        }
                        break;
                    }
                    continue;
                } else {
                    if (_debugging) {
                        _debug("\nOff the map.");
                    }
                    break;
                }
            }
        }

        // TODO : I should maintain an ordered list of these rather than doing like this
        CompositeActor compAct = (CompositeActor) getContainer();
        List entities = compAct.deepEntityList();
        for (Object ent : entities) {
            if (!(ent instanceof Executable) || ent instanceof Continuation) {
                continue;
            }
            Executable e = (Executable) ent;
            if (_debugging) {
                _debug("\nFiring : " + e + "\n");
            }
            e.fire();
        }
    }

    private InternalExitLocation _exitFrom(InternalEntryLocation iel,
            ControlExitToken cet) {
        Continuation c = iel.continuation();
        if (!_continuations.contains(c)) {
            return null;
        }
        if (!cet.isLocation()) {
            return null;
        }
        ExitLocation el = cet.getLocation();
        return new InternalExitLocation(c, el);
    }

    private ControlExitToken _enterWith(InternalEntryLocation iel)
            throws IllegalActionException {
        Continuation c = iel.continuation();
        if (!_continuations.contains(c)) {
            return null;
        }
        ControlExitToken cet = c.controlEnter(iel.entry());
        return cet;
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.Director#initialize()
     */
    @Override
    public void initialize() throws IllegalActionException {
        CompositeActor compAct = (CompositeActor) getContainer();
        List entities = compAct.deepEntityList();

        _continuations = new LinkedList();
        for (Object entity : entities) {
            if (!(entity instanceof Continuation)) {
                continue;
            }
            Continuation continuation = (Continuation) entity;
            _continuations.add(continuation);
        }

        _initContinuation = null;
        Token initContName = initContinuationName.getToken();
        if (initContName != null && initContName instanceof StringToken) {
            StringToken stName = (StringToken) initContName;
            ComponentEntity ent = compAct.getEntity(stName.stringValue());
            if (ent != null && ent instanceof Continuation) {
                _initContinuation = (Continuation) ent;
            }
        }
        if (_initContinuation == null) {
            if (_continuations.size() > 0) {
                _initContinuation = _continuations.getFirst();
            } else {
                throw new IllegalActionException(
                        "There is no initial continuation.");
            }
        }

        _currentLocation = new InternalEntryLocation(_initContinuation,
                ControlEntryToken.Resume());

        buildEntryMapFromParameter();
        buildMapFromConnections();
        buildExitMapFromParameter();

        _currentControlPath = new LinkedList();

        super.initialize();

        _resetAllReceivers();
    }

    public void buildMapFromParameter() throws IllegalActionException {
        _nextMap = new HashMap();
        Token nextMappingToken = nextMapping.getToken();
        if (nextMappingToken != null && nextMappingToken instanceof ArrayToken) {
            ArrayToken nextMappingAr = (ArrayToken) nextMappingToken;
            for (Token mapElT : nextMappingAr.arrayValue()) {
                String spec[] = _extractMapSpec(mapElT, 4);
                if (spec == null) {
                    continue;
                }

                Continuation contout = _getContinuation(spec[0]);
                if (contout == null) {
                    continue;
                }

                ExitLocation exit = new ExitLocation(spec[1]);
                if (!contout.exitLocations().contains(exit)) {
                    continue;
                }

                Continuation contin = _getContinuation(spec[2]);
                if (contin == null) {
                    continue;
                }

                ControlEntryToken conET = ControlEntryToken.EntryToken(spec[3]);
                if (conET.isLocation()
                        && !contin.entryLocations().contains(
                                conET.getLocation())) {
                    continue;
                }

                InternalExitLocation inext = new InternalExitLocation(contout,
                        exit);
                InternalEntryLocation inent = new InternalEntryLocation(contin,
                        conET);
                _nextMap.put(inext, inent);
            }
        }

        debugShowMap();
    }

    public void buildEntryMapFromParameter() throws IllegalActionException {
        _entryMap = new HashMap();
        Token entryMappingToken = entryMapping.getToken();
        if (entryMappingToken != null
                && entryMappingToken instanceof ArrayToken) {
            ArrayToken entryMappingAr = (ArrayToken) entryMappingToken;
            for (Token mapElT : entryMappingAr.arrayValue()) {
                String spec[] = _extractMapSpec(mapElT, 3);
                if (spec == null) {
                    continue;
                }

                EntryLocation entry = new EntryLocation(spec[0]);
                if (!_entries.contains(entry)) {
                    continue;
                }

                Continuation contin = _getContinuation(spec[1]);
                if (contin == null) {
                    continue;
                }

                ControlEntryToken conET = ControlEntryToken.EntryToken(spec[2]);
                if (conET.isLocation()
                        && !contin.entryLocations().contains(
                                conET.getLocation())) {
                    continue;
                }

                InternalEntryLocation inent = new InternalEntryLocation(contin,
                        conET);
                _entryMap.put(entry, inent);
            }
        }
    }

    public void buildExitMapFromParameter() throws IllegalActionException {
        _exitMap = new HashMap();
        Token exitMappingToken = exitMapping.getToken();
        if (exitMappingToken != null && exitMappingToken instanceof ArrayToken) {
            ArrayToken exitMappingAr = (ArrayToken) exitMappingToken;
            for (Token mapElT : exitMappingAr.arrayValue()) {
                String spec[] = _extractMapSpec(mapElT, 3);
                if (spec == null) {
                    continue;
                }

                Continuation contout = _getContinuation(spec[0]);
                if (contout == null) {
                    continue;
                }

                ExitLocation exit = new ExitLocation(spec[1]);
                if (!contout.exitLocations().contains(exit)) {
                    continue;
                }

                ExitLocation exitM = new ExitLocation(spec[2]);
                if (!_exits.contains(exitM)) {
                    continue;
                }

                InternalExitLocation inext = new InternalExitLocation(contout,
                        exit);
                _exitMap.put(inext, exitM);
            }
        }
    }

    public void buildMapFromConnections() {
        _nextMap = new HashMap();
        for (Continuation c : _continuations) {
            if (!(c instanceof AtomicContinuationActor)) {
                continue;
            }
            AtomicContinuationActor scact = (AtomicContinuationActor) c;
            for (Object ob : scact.outputPortList()) {
                if (!(ob instanceof TypedIOPort)) {
                    continue;
                }

                TypedIOPort sp = (TypedIOPort) ob;
                ExitLocation xl = scact.getExitLocationFromPort(sp);
                if (xl == null) {
                    continue;
                }

                List<TypedIOPort> dps = sp.connectedPortList();
                if (dps.size() < 1) {
                    continue;
                }

                TypedIOPort dp = dps.get(0);
                NamedObj nobj = dp.getContainer();
                if (!(nobj instanceof AtomicContinuationActor)) {
                    continue;
                }
                AtomicContinuationActor dcact = (AtomicContinuationActor) nobj;

                ControlEntryToken et = dcact.getEntryActionFromPort(dp);
                if (et == null) {
                    continue;
                }

                InternalExitLocation inext = new InternalExitLocation(scact, xl);
                InternalEntryLocation inent = new InternalEntryLocation(dcact,
                        et);
                _nextMap.put(inext, inent);
            }
        }

        debugShowMap();
    }

    public void debugShowMap() {
        if (_debugging) {
            _debug("\nNext Map:\n");
        }
        for (Map.Entry<InternalExitLocation, InternalEntryLocation> ent : _nextMap
                .entrySet()) {
            if (_debugging) {
                _debug("\n    " + ent.getKey() + " => " + ent.getValue());
            }
        }
        if (_debugging) {
            _debug("\n");
        }
    }

    public String[] _extractMapSpec(Token t, int n) {
        if (!(t instanceof ArrayToken)) {
            return null;
        }
        Token[] ts = ((ArrayToken) t).arrayValue();

        if (ts.length < n) {
            return null;
        }
        String spec[] = new String[n];
        for (int k = 0; k < n; ++k) {
            if (!(ts[k] instanceof StringToken)) {
                return null;
            }
            spec[k] = ((StringToken) ts[k]).stringValue();
        }

        return spec;
    }

    public Continuation _getContinuation(String name) {
        for (Continuation c : _continuations) {
            if (!(c instanceof Nameable)) {
                continue;
            }
            if (((Nameable) c).getName().equals(name)) {
                return c;
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.Director#postfire()
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (_nextLocation == null) {
            return false;
        }
        _currentLocation = _nextLocation;

        // Postfire all continuations
        boolean pfret = true;
        for (Continuation c : _currentControlPath) {
            if (!(c instanceof Executable)) {
                continue;
            }
            Executable e = (Executable) c;
            if (_debugging) {
                _debug("\nPostfiring : " + c + "\n");
            }
            pfret = pfret && e.postfire();
        }

        // TODO : I should maintain an ordered list of these rather than doing like this
        CompositeActor compAct = (CompositeActor) getContainer();
        List entities = compAct.deepEntityList();
        for (Object ent : entities) {
            if (!(ent instanceof Executable) || ent instanceof Continuation) {
                continue;
            }
            Executable e = (Executable) ent;
            pfret = pfret && e.postfire();
        }

        _resetAllReceivers();

        return pfret && super.postfire();
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.Director#preinitialize()
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        // TODO Auto-generated method stub
        super.preinitialize();
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#enter(ptolemy.domains.coroutine.kernel.ControlEntryToken)
     */
    @Override
    public ControlExitToken controlEnter(ControlEntryToken entry)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#init()
     */
    @Override
    public ControlExitToken controlInit() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#resume()
     */
    @Override
    public ControlExitToken controlResume() throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#enterAt(ptolemy.domains.coroutine.kernel.ControlEntryToken.EntryLocation)
     */
    @Override
    public ControlExitToken controlEnterAt(
            ControlEntryToken.EntryLocation location)
            throws IllegalActionException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#entryLocations()
     */
    @Override
    public List<EntryLocation> entryLocations() {
        // TODO Auto-generated method stub
        return _entries;
    }

    /* (non-Javadoc)
     * @see ptolemy.domains.coroutine.kernel.Continuation#exitLocations()
     */
    @Override
    public List<ExitLocation> exitLocations() {
        // TODO Auto-generated method stub
        return _exits;
    }

    // TODO: Change the below to the above done to automatically connect ports.
    @Override
    public boolean transferInputs(IOPort port) throws IllegalActionException {
        boolean result = false;
        int insideWidth = port.getWidthInside();
        for (int i = 0; i < port.getWidth(); i++) {
            if (port.isKnown(i)) {
                if (port.hasToken(i)) {
                    result = super.transferInputs(port) || result;
                } else {
                    if (i < insideWidth) {
                        port.sendInside(i, null);
                    }
                }
            }
            // we do not explicit reset the receivers receiving inputs
            // from this port because the fixedpoint director resets the
            // receivers in its prefire() method.
        }
        // If the inside is wider than the outside, send clear on the inside.
        for (int i = port.getWidth(); i < insideWidth; i++) {
            port.sendInside(i, null);
        }
        return result;
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.Director#transferOutputs(ptolemy.actor.IOPort)
     */
    @Override
    public boolean transferOutputs(IOPort port) throws IllegalActionException {
        boolean result = false;
        ((CompositeActor) getContainer()).getExecutiveDirector();
        int outsideWidth = port.getWidth();
        for (int i = 0; i < port.getWidthInside(); i++) {
            if (port.isKnownInside(i)) {
                if (port.hasTokenInside(i)) {
                    result = super.transferOutputs(port) || result;
                } else {
                    // Mark the destination receivers absent, if the destination
                    // receiver has such a notion, and otherwise do nothing.
                    if (i < outsideWidth) {
                        port.send(i, null);
                    }
                }
            }
        }
        // If the outside is wider than the inside, send clear on the outside.
        /* NOTE: The following isn't right!  Need to leave the output unknown in case
         * we are in a modal model. A transition may be wanting to set it.
         * it has to become known only if the environment sets it known
         * by presuming that any unproduced outputs are absent.
         *
        for (int i = port.getWidthInside(); i < outsideWidth; i++) {
            port.send(i, null);
        }
         */
        return result;
    }

    /* (non-Javadoc)
     * @see ptolemy.actor.Director#newReceiver()
     */
    @Override
    public Receiver newReceiver() {
        Receiver receiver = new FixedPointReceiver();
        _receivers.add(receiver);
        return receiver;
    }

    /** Reset all receivers to unknown status and clear out variables used
     *  to track which actors fired in the last iteration.
     */
    protected void _resetAllReceivers() {
        if (_debugging) {
            _debug("    CoroutineDirector is resetting all receivers");
        }

        for (Receiver r : _receivers) {
            if (!(r instanceof FixedPointReceiver)) {
                continue;
            }
            FixedPointReceiver receiver = (FixedPointReceiver) r;
            receiver.reset();
        }
    }

    public class InternalEntryLocation extends EntryLocation {
        InternalEntryLocation(Continuation continuation, ControlEntryToken entry) {
            super("");
            _continuation = continuation;
            _entry = entry;
        }

        public Continuation continuation() {
            return _continuation;
        }

        public ControlEntryToken entry() {
            return _entry;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "" + _continuation + " : " + _entry;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            InternalEntryLocation other = (InternalEntryLocation) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (_continuation == null) {
                if (other._continuation != null) {
                    return false;
                }
            } else if (!_continuation.equals(other._continuation)) {
                return false;
            }
            if (_entry == null) {
                if (other._entry != null) {
                    return false;
                }
            } else if (!_entry.equals(other._entry)) {
                return false;
            }
            return true;
        }

        /** Return the hash code for the InternalEntryLocation object. If two
         *  InternalEntryLocation objects contains the same Continuation and
         *  ControlEntryToken then they have the same hashcode.
         *  @return The hash code for this InternalEntryLocation object.
         */
        @Override
        public int hashCode() {
            // See http://www.technofundo.com/tech/java/equalhash.html
            int hashCode = 31;
            if (_continuation != null) {
                hashCode = 31 * hashCode + _continuation.hashCode();
            }
            if (_entry != null) {
                hashCode = 31 * hashCode + _entry.hashCode();
            }
            return hashCode;
        }

        private Continuation _continuation;
        private ControlEntryToken _entry;

        private CoroutineDirector getOuterType() {
            return CoroutineDirector.this;
        }
    }

    public class InternalExitLocation extends ExitLocation {
        InternalExitLocation(Continuation continuation, ExitLocation location) {
            super("");
            _continuation = continuation;
            _location = location;
        }

        public Continuation continuation() {
            return _continuation;
        }

        public ExitLocation location() {
            return _location;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "" + _continuation + " : " + _location;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!super.equals(obj)) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            InternalExitLocation other = (InternalExitLocation) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (_continuation == null) {
                if (other._continuation != null) {
                    return false;
                }
            } else if (!_continuation.equals(other._continuation)) {
                return false;
            }
            if (_location == null) {
                if (other._location != null) {
                    return false;
                }
            } else if (!_location.equals(other._location)) {
                return false;
            }
            return true;
        }

        /** Return the hash code for the InternalExitLocation object. If two
         *  InternalExitLocation objects contains the same Continuation and
         *  ExitLocation then they have the same hashcode.
         *  @return The hash code for this InternalExitLocation object.
         */
        @Override
        public int hashCode() {
            // See http://www.technofundo.com/tech/java/equalhash.html
            int hashCode = 31;
            if (_continuation != null) {
                hashCode = 31 * hashCode + _continuation.hashCode();
            }
            if (_location != null) {
                hashCode = 31 * hashCode + _location.hashCode();
            }
            return hashCode;
        }

        private Continuation _continuation;
        private ExitLocation _location;

        private CoroutineDirector getOuterType() {
            return CoroutineDirector.this;
        }
    }

    public Parameter initContinuationName;
    public Parameter entryMapping;
    public Parameter nextMapping;
    public Parameter exitMapping;

    ///////////////////////////////////////////////////////////////////

    /*private EntryLocation _next(ExitLocation exloc) {
        return _nextMap.get(exloc);
    }*/

    /* The structure of Coroutine Models is given here:
     * M = (I_M, O_M, Q, q_0, m_I, m_O, L_M, G_M, (+), k, n)
     *     Q     -- _continuations
     *     q_0   -- _initContinuation
     *     m_I   -- _inputMap  + transferInputs [TODO: (1)]
     *     m_O   -- _outputMap + transferOutputs [TODO: (1)]
     *     L_M   -- _entries
     *     G_M   -- _exits
     *     (+)   -- transferOutputs
     *     k     -- _entryMap
     *     n     -- _nextMap + _exitMap
     *
     *     (1) at the present this is done manually.
     */

    /** List of continuations that make up the model. */
    private LinkedList<Continuation> _continuations;

    /** Reference to the initial continuation in {@link #_continuations}. */
    private Continuation _initContinuation;

    /** Map from inputs of the model to those of the continuations : m_I */
    //private HashMap<IOPort, IOPort> _inputMap;

    /** Map from outputs of the model to those of the continuations : m_O */
    //private HashMap<IOPort, IOPort> _outputMap;

    /** Entry Locations of the model : L_M */
    private LinkedList<EntryLocation> _entries;

    /** Exit Locations of the model : G_M */
    private LinkedList<ExitLocation> _exits;

    /** Map from Entry Locations of the model to those of the continuations : k */
    private HashMap<EntryLocation, InternalEntryLocation> _entryMap;

    /** Map from Exit Locations to Entry Locations : n (to internal entries)
     *  This map constitutes the structure of the continuations in
     *  the model such that they form a control flow graph.
     */
    private HashMap<InternalExitLocation, InternalEntryLocation> _nextMap;

    /** Map from Exit Locations of the continuations to those of the model : n (to exits) */
    private HashMap<InternalExitLocation, ExitLocation> _exitMap;

    /* *************************************** */

    private LinkedList<Receiver> _receivers;

    /* *************************************** */

    /** Current location from which the model is resumed. */
    private InternalEntryLocation _currentLocation, _nextLocation;
    private LinkedList<Continuation> _currentControlPath;

}
