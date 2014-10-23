/* An actor that does back off after a frame transmission.

 Copyright (c) 2004-2006 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.wireless.lib.network.mac;

import java.util.Random;

import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.RecordToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// Backoff

/**
 This actor does the back off after a frame transmission. It randomly
 choose a slot from the back off window and wait for that amount of
 time before trying to access the medium.
 @author Yang Zhao
 @version Backoff.java,v 1.14 2004/04/22 19:46:18 ellen_zh Exp
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (ellen_zh)
 @Pt.AcceptedRating Red (pjb2e)
 */
public class Backoff extends MACActorBase {
    /** Construct an actor with the specified name and container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the empty string.
     *  This constructor write-synchronizes on the workspace.
     *  @param container The container.
     *  @param name The name of the actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Backoff(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create and configure the ports.
        fromDataPump = new TypedIOPort(this, "fromDataPump", true, false);
        getBackoff = new TypedIOPort(this, "getBackoff", true, false);
        BKDone = new TypedIOPort(this, "BKDone", false, true);

        fromDataPump.setTypeEquals(BaseType.GENERAL);
        getBackoff.setTypeEquals(BaseType.GENERAL);
        BKDone.setTypeEquals(BaseType.GENERAL);

        seed = new Parameter(this, "seed", new LongToken(0));
        seed.setTypeEquals(BaseType.LONG);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The input port for channel status message.
     *  This has undeclared type.
     */
    public TypedIOPort fromDataPump;

    /** The input port for backoff message from the Protocol control block.
     */
    public TypedIOPort getBackoff;

    /** The output port that produces messages that
     *  indicate the backoff is done.
     */
    public TypedIOPort BKDone;

    /** The seed that controls the random number generation.
     *  A seed of zero is interpreted to mean that no seed is specified,
     *  which means that each execution of the model could result in
     *  distinct data. For the value 0, the seed is set to
     *  System.currentTimeMillis() + hashCode(), which means that
     *  with extremely high probability, two distinct actors will have
     *  distinct seeds.  However, current time may not have enough
     *  resolution to ensure that two subsequent executions of the
     *  same model have distinct seeds.
     *
     *  This parameter contains a LongToken, initially with value 0.
     */
    public Parameter seed;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        Backoff newObject = (Backoff) super.clone(workspace);
        return newObject;
    }

    /** Override the base class to declare that the <i>BKDone</i>
     *  output port does not depend on the <i>getBackoff</i>
     *  of <i>fromDataPump</i> input ports in a firing.
     *  @exception IllegalActionException If the causality interface
     *  cannot be computed.
     */
    @Override
    public void declareDelayDependency() throws IllegalActionException {
        // Declare that output does not immediately depend on the input,
        // though there is no lower bound on the time delay.
        _declareDelayDependency(getBackoff, BKDone, 0.0);
        _declareDelayDependency(fromDataPump, BKDone, 0.0);
    }

    /** The main function
     *  @exception IllegalActionException If an error occurs reading
     *   or writing inputs or outputs.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        int ct = 0;
        Director director = getDirector();
        _currentTime = director.getModelTime();

        int kind = whoTimeout();

        // if a timer is processed, should not consume the message token
        // kind = -1 means no timer event.
        if (kind == -1) {
            if (getBackoff.hasToken(0)) {
                _inputMessage = (RecordToken) getBackoff.get(0);
            } else if (fromDataPump.hasToken(0)) {
                _inputMessage = (RecordToken) fromDataPump.get(0);
            }

            if (_inputMessage != null) {
                _messageType = ((IntToken) _inputMessage.get("kind"))
                        .intValue();
            }
        }

        switch (_state) {
        case No_Backoff:

            switch (_messageType) {
            case Backoff:
                _setAttribute(_mBkIP, new BooleanToken(true));
                _cnt = ((IntToken) _inputMessage.get("cnt")).intValue();

                if (_cnt < 0) {
                    int ccw = ((IntToken) _inputMessage.get("ccw")).intValue();
                    _slotCnt = _generateRandom(ccw);
                } else {
                    _slotCnt = _cnt;
                }

                if (_status == Idle) {
                    _startBackoff();
                } else {
                    _cnt = 1; //backoff done will return with -1; another backoff will follow
                    _state = Channel_Busy;
                }

                break;

            case Idle:
                _status = Idle;
                break;

            case Busy:
                _status = Busy;
                break;
            }

            break;

        case Channel_Busy:

            switch (_messageType) {
            // modify standard here
            case Idle:
                _startBackoff();
                break;

                // end modification
            case Cancel:
                _backoffDone(_slotCnt);
                break;
            }

            break;

        case Channel_Idle:

            // modify standard here
            if (kind == BackoffTimeOut) {
                if (_cnt == 0) {
                    ct = -2;
                } else {
                    ct = -1;
                }

                _backoffDone(ct);
            }

            switch (_messageType) {
            // modify standard here
            case Busy:
                _slotCnt -= (int) (_currentTime.subtract(_backoffStartTime)
                        .getDoubleValue() * 1e6 / _aSlotTime);
                cancelTimer(_BackoffTimer);
                _state = Channel_Busy;
                _status = Busy;
                break;

                // end modification
            case Cancel:
                _backoffDone(_slotCnt);
                break;
            }

            break;
        }

        _inputMessage = null;
        _messageType = UNKNOWN;
    }

    /** Initialize the private variables.
     *  @exception IllegalActionException If thrown by the base class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        long sd = ((LongToken) seed.getToken()).longValue();

        if (sd != 0) {
            _random.setSeed(sd);
        } else {
            _random.setSeed(System.currentTimeMillis() + hashCode());
        }

        _inputMessage = null;

        //_message = null;
        _messageType = UNKNOWN;
        _status = Busy;
        _state = No_Backoff;
        _backoffStartTime = new Time(getDirector());

        NamedObj macComposite = getContainer().getContainer();

        if (macComposite.getAttribute("mBkIP") != null) {
            _mBkIP = macComposite.getAttribute("mBkIP");
        } else {
            _mBkIP = null;
            throw new IllegalActionException("the MAC compositor "
                    + "dosen't contain a parameter named mBkIP");
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    private int _generateRandom(int ccw) {
        double r = _random.nextDouble();
        return (int) Math.ceil(ccw * r);
    }

    private void _backoffDone(int cnt) throws IllegalActionException {
        Token[] value = { new IntToken(BkDone), new IntToken(cnt) };
        BKDone.send(0, new RecordToken(BackoffDoneMsgFields, value));
        _setAttribute(_mBkIP, new BooleanToken(false));
        _state = No_Backoff;
    }

    private void _startBackoff() throws IllegalActionException {
        _backoffStartTime = _currentTime;
        _BackoffTimer = setTimer(BackoffTimeOut,
                _currentTime.add(_slotCnt * _aSlotTime * 1e-6));
        _state = Channel_Idle;
        _status = Idle;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    //Define the states of the inside FSM.
    private static final int No_Backoff = 0;

    private static final int Channel_Busy = 1;

    private static final int Channel_Idle = 2;

    private int _state = 0;

    private int _slotCnt;

    private int _cnt;

    private int _status;

    private Timer _BackoffTimer;

    // timer types
    private static final int BackoffTimeOut = 0;

    private Time _backoffStartTime;

    private RecordToken _inputMessage;

    private int _messageType;

    private Time _currentTime;

    protected Random _random = new Random();
}
