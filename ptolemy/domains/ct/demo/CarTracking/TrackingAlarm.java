/* An actor that delays the input by the specified amount.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Yellow (cxh@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.CarTracking;

import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;

//////////////////////////////////////////////////////////////////////////
//// TrackingAlarm
/**
Wait for the event for the position of the follower and send
alarm events. The actor has zero delay.

@author Jie Liu
@version $Id$
*/
public class TrackingAlarm extends DEActor {

    /** Construct an actor with the specified container and name.
     *  @param container The composite actor to contain this one.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TrackingAlarm(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        _closeThreshold = 15.0;
        closeThreshold = new Parameter(this, "closeThreshold",
                new DoubleToken(15.0));
        closeThreshold.setTypeEquals(BaseType.DOUBLE);

        _awayThreshold = 40.0;
        awayThreshold = new Parameter(this, "awayThreshold",
                new DoubleToken(10.0));
        awayThreshold.setTypeEquals(BaseType.DOUBLE);
        
        leaderPosition = new TypedIOPort(this, "leaderPosition", true, false);
        leaderPosition.setTypeEquals(BaseType.DOUBLE);

        followerPosition = 
            new TypedIOPort(this, "followerPosition", true, false);
        followerPosition.setTypeEquals(BaseType.DOUBLE);
        
        tooClose = new TypedIOPort(this, "tooClose", false, true);
        tooClose.setTypeEquals(BaseType.BOOLEAN);

        tooFar = new TypedIOPort(this, "tooFar", false, true);
        tooFar.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                       ports and parameters                ////

    /** Threshold for too close. Default value is 10.0;
     */
    public Parameter closeThreshold;

    /** Threshold for too far away. Default value is 50.0;
     */
    public Parameter awayThreshold;

    /** Input port for the leader position.
     */
    public TypedIOPort leaderPosition;

    /** Input port for the follower position.
     */
    public TypedIOPort followerPosition;

    /** Output port for too close.
     */
    public TypedIOPort tooClose;

    /** Output port for too far away.
     */
    public TypedIOPort tooFar;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the attribute is <i>delay</i>, then check that the value
     *  is non-negative.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the delay is negative.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == closeThreshold ) {
            double threshold =
                ((DoubleToken)((Parameter)attribute).getToken()).doubleValue();
            if (threshold < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative threshold.");
            } else {
                _closeThreshold = threshold;
            }
        } else if (attribute == awayThreshold ) {
            double threshold =
                ((DoubleToken)((Parameter)attribute).getToken()).doubleValue();
            if (threshold < 0.0) {
                throw new IllegalActionException(this,
                        "Cannot have negative threshold.");
            } else {
                _awayThreshold = threshold;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the parameter.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has
     *   has an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws) throws CloneNotSupportedException {
        TrackingAlarm newobj = (TrackingAlarm)super.clone(ws);
        newobj.closeThreshold = 
            (Parameter)newobj.getAttribute("closeThreshold");
        newobj.awayThreshold = 
            (Parameter)newobj.getAttribute("awayThreshold");
        newobj.leaderPosition = 
            (TypedIOPort)newobj.getPort("leaderPosition");
        newobj.followerPosition = 
            (TypedIOPort)newobj.getPort("followerPosition");
        newobj.tooClose = 
            (TypedIOPort)newobj.getPort("tooClose");
        newobj.tooFar = 
            (TypedIOPort)newobj.getPort("tooFar");
        return newobj;
    }

    /** Read one token from the input and save it so that the
     *  postfire method can produce it to the output.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        if (leaderPosition.hasToken(0)) {
            _hasLeaderInformation = true;
            _leaderPosition = 
                ((DoubleToken)leaderPosition.get(0)).doubleValue();
        }
        if (followerPosition.hasToken(0)) {
            if (_hasLeaderInformation) {
                _hasLeaderInformation = false;
                double follower = 
                    ((DoubleToken)followerPosition.get(0)).doubleValue();
                if (_leaderPosition - follower < _closeThreshold) {
                    tooClose.send(0, new BooleanToken(true));
                    //System.out.println("TOO CLOSE");
                } else if(_leaderPosition - follower > _awayThreshold) {
                    tooFar.send(0, new BooleanToken(true));
                    //System.out.println("TOOFAR");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // latest inputs for leader.
    private double _leaderPosition;

    // has leader information.
    private boolean _hasLeaderInformation = false;

    //
    private double _closeThreshold;

    // away threshold
    private double _awayThreshold;

}
