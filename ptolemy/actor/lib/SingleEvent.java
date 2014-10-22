/* An actor that produces an event at the specified time.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.actor.lib;

import ptolemy.actor.Director;
import ptolemy.actor.SuperdenseTimeDirector;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// SingleEvent

/**
 This actor produces an event with the specified value at the
 specified time.  In its initialize() method, it requests a firing
 at the specified time.  If the <i>time</i>
 parameter changes before that time is reached, then the event is
 effectively canceled.  No event will be produced.
 <p>
 If used with a director that implements SuperdenseTimeDirector, then
 this actor will produce its output event at microstep 1.
 If it is fired at microstep 0 at the specified model time,
 then it requests a refiring at the current time.
 This ensures piecewise continuity.
 <p>
 Note that you do not really need this actor. The
 {@link DiscreteClock} actor can produce any finite sequence of
 events by setting is <i>period</i> to Infinity.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (liuj)
 */
public class SingleEvent extends TypedAtomicActor {
    /** Construct an actor with the specified container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SingleEvent(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        time = new Parameter(this, "time", new DoubleToken(0.0));
        time.setTypeEquals(BaseType.DOUBLE);
        value = new Parameter(this, "value", new BooleanToken(true));
        output.setTypeSameAs(value);
    }

    ///////////////////////////////////////////////////////////////////
    ////                      ports and parameters                 ////

    /** The output port.  The type of this port is linked to the type
     *  of the <i>value</i> parameter.
     */
    public TypedIOPort output = null;

    /** The time at which to produce the output.  This has type double,
     *  with default value 0.0.
     *  If the value is negative, then no output will be produced.
     */
    public Parameter time;

    /** The value produced at the output.  This can have any type,
     *  and it defaults to a boolean token with value <i>true</i>.
     */
    public Parameter value;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then links the type of the <i>value</i> parameter
     *  to the output.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        SingleEvent newObject = (SingleEvent) super.clone(workspace);
        newObject.output.setTypeSameAs(newObject.value);
        return newObject;
    }

    /** If the current time matches the value of the <i>time</i> parameter,
     *  then produce an output token with value given by the <i>value</i>
     *  parameter.
     */
    @Override
    public void fire() throws IllegalActionException {
        double eventTimeValue = ((DoubleToken) time.getToken()).doubleValue();
        Director director = getDirector();
        int microstep = 1;
        if (director instanceof SuperdenseTimeDirector) {
            microstep = ((SuperdenseTimeDirector) director).getIndex();
        }
        Time eventTime = new Time(director, eventTimeValue);
        Time currentTime = director.getModelTime();
        if (currentTime.equals(eventTime)) {
            if (microstep >= 1) {
                // If an output has been produced previously, then
                // produce it again only if the microstep matches.
                if (_outputProduced < 0 || _outputProduced == microstep) {
                    output.send(0, value.getToken());
                    _outputProduced = microstep;
                } else {
                    output.sendClear(0);
                }
            } else {
                // Request a refiring at the next microstep.
                // This ensures that the output is piecewise continuous.
                // This doesn't really need to be postponed to postfire()
                // because it is generally harmless to make this request more
                // than once.
                _fireAt(currentTime);
                // Send clear to the output, since we know it is absent.
                output.sendClear(0);
            }
        } else {
            // Send clear to the output, since we know it is absent.
            output.sendClear(0);
        }

        super.fire();
    }

    /** Request firing at the time given by the <i>time</i> parameter.
     *  If the time is negative, then do nothing.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        _outputProduced = -1;

        double eventTime = ((DoubleToken) time.getToken()).doubleValue();

        if (eventTime >= 0.0) {
            _fireAt(eventTime);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Indicator that the event has been produced at a microstep. */
    private int _outputProduced;
}
