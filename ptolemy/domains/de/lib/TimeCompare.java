/* A DE actor to compare the time stamps of events at its two input ports, and
 output the difference.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
package ptolemy.domains.de.lib;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEActor;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// TimeCompare

/**
 A DE actor to compare the time stamps of events at its two input ports, and
 output the difference. Every time an event can be processed either at input
 port <code>input1</code> or at <code>input2</code>, the event is consumed. The
 value that the event carries is insignificant, but the time stamp of the event
 is recorded in a local list. Time stamps received at the two input ports are
 stored in two different lists. Every time when both lists have data, the
 difference between the top elements of the lists is obtained, and is sent to
 the output port. This done by subtracting the time stamp of every top event in
 the list for <code>input2</code> with the time stamp of every top event in the
 list for <code>input1</code>.
 <p>
 This actor could potentially consume an infinite amount of memory if the
 arrival rates of events at the two input ports are different, because one of
 the lists keeps growing.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TimeCompare extends DEActor {

    /** Construct an actor with the specified container and name.
     *  This is protected because there is no reason to create an instance
     *  of this class, but derived classes will want to invoke the
     *  constructor of the superclass.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimeCompare(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        nonnegative = new Parameter(this, "nonnegative");
        nonnegative.setTypeEquals(BaseType.BOOLEAN);
        nonnegative.setToken(BooleanToken.FALSE);

        input1 = new TypedIOPort(this, "input1", true, false);
        input2 = new TypedIOPort(this, "input2", true, false);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DOUBLE);
    }

    /** Clone this actor into the specified workspace. The new actor is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a new actor with the same ports as the original, but
     *  no connections and no container.  A container must be set before
     *  much can be done with this actor.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If cloned ports cannot have
     *   as their container the cloned entity (this should not occur), or
     *   if one of the attributes cannot be cloned.
     *  @return A new ComponentEntity.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TimeCompare newObject = (TimeCompare) super.clone(workspace);
        newObject._input1TimeStamps = new LinkedList<Double>();
        newObject._input2TimeStamps = new LinkedList<Double>();
        return newObject;
    }

    /** Fire this actor once. If there are events at its input ports, they are
     *  immediately consumed, and their time stamps are recorded in a list. If
     *  the two internal lists for the two input signals both have data, then
     *  outputs are sent to the output port, which are the difference between
     *  the time stamps in the two lists.
     *
     *  @exception IllegalActionException If thrown when trying to consume input
     *  events or produce output events.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        Double currentTime = ((DEDirector) getDirector()).getModelTime()
                .getDoubleValue();

        while (input1.hasNewToken(0)) {
            input1.get(0);
            _input1TimeStamps.add(currentTime);
        }

        while (input2.hasNewToken(0)) {
            input2.get(0);
            _input2TimeStamps.add(currentTime);
        }

        boolean nonnegative = ((BooleanToken) this.nonnegative.getToken())
                .booleanValue();
        Iterator<Double> input1Iterator = _input1TimeStamps.iterator();
        Iterator<Double> input2Iterator = _input2TimeStamps.iterator();
        while (input1Iterator.hasNext() && input2Iterator.hasNext()) {
            double input1 = input1Iterator.next();
            double input2 = input2Iterator.next();
            input2Iterator.remove();
            double difference = input2 - input1;

            if (nonnegative) {
                while (difference < 0.0 && input2Iterator.hasNext()) {
                    input2 = input2Iterator.next();
                    input2Iterator.remove();
                    difference = input2 - input1;
                }
                if (difference >= 0.0) {
                    input1Iterator.remove();
                    output.send(0, new DoubleToken(difference));
                }
            } else {
                input1Iterator.remove();
                output.send(0, new DoubleToken(difference));
            }
        }
    }

    /** Initialize this actor.
     *
     *  @exception IllegalActionException Never thrown.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _input1TimeStamps.clear();
        _input2TimeStamps.clear();
    }

    /** Return ture if this actor can fire. This actor can fire if prefire() of
     *  the superclass returns true, and either of the two input ports, or both,
     *  have token.
     *
     *  @return true if this actor can fire.
     *  @exception IllegalActionException If thrown when trying to decide
     *  whether the input ports have token or not.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        return super.prefire() && (input1.hasToken(0) || input2.hasToken(0));
    }

    /** The first input port. */
    public TypedIOPort input1;

    /** The second input port. */
    public TypedIOPort input2;

    /** A boolean parameter to decide whether inputs at input2 should be ignored
    if they lead to negative outputs. */
    public Parameter nonnegative;

    /** The output port to which difference values are sent. */
    public TypedIOPort output;

    /** The list to store the time stamps received at input1 but have never been
    compared. */
    private List<Double> _input1TimeStamps = new LinkedList<Double>();

    /** The list to store the time stamps received at input2 but have never been
    compared. */
    private List<Double> _input2TimeStamps = new LinkedList<Double>();
}
