/* An actor that implements a resettable timer.

 Copyright (c) 1998-2003 The Regents of the University of California.
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

package ptolemy.domains.fsm.demo.ABP;

import ptolemy.actor.IODependence;
import ptolemy.actor.IODependenceOfAtomicActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.DoubleToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.domains.de.kernel.DEIOPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// DETimer
/**
The input sets the time interval before the next expire.

@author Xiaojun Liu
@version $Id$
@since Ptolemy II 0.4
*/
public class DETimer extends TypedAtomicActor {

    /** Constructor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DETimer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        expired = new DEIOPort(this, "expired", false, true);
        expired.setTypeEquals(BaseType.GENERAL);
        set = new DEIOPort(this, "set", true, false);
        set.setTypeEquals(BaseType.DOUBLE);
//        set.delayTo(expired);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reset the timer if there is a token in port set. Otherwise send
     *  a token to port expire if the current time agrees with the time
     *  the timer is set to expire.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        DEDirector dir = (DEDirector)getDirector();
        double now = dir.getCurrentTime();

        if (set.hasToken(0)) {
            // reset timer
            double delay = ((DoubleToken)set.get(0)).doubleValue();
            if (delay > 0.0) {
                _expireTime = now + delay;
                dir.fireAt(this, now + delay);
            } else {
                // disable timer
                _expireTime = -1.0;
            }

            //System.out.println("Reset DETimer " + this.getFullName() +
            //        " to expire at " + _expireTime);

        } else if (Math.abs(now - _expireTime) < 1e-14) {
            // timer expires
            expired.broadcast(_outToken);

            //System.out.println("DETimer " + this.getFullName() +
            //        " expires at " + getCurrentTime());

        }

    }

    /** Initialize the timer.
     *  @exception IllegalActionException If the initialize() of the parent
     *   class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _expireTime = -1.0;
    }

    /** Create an IODependence attribute for this actor.
     *  @exception IllegalActionException thrown by super class or
     *  the IODependence constructor.
     */
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        try {
            IODependenceOfAtomicActor ioDependence = 
                new IODependenceOfAtomicActor(this, "_IODependence");
        } catch (NameDuplicationException e) {
            // because the IODependence attribute is not persistent,
            // and it is only created once in the preinitialize method,
            // there should be no NameDuplicationException thrown.
        }
    }
    
    /** Explicitly declare which inputs and outputs are not dependent.
     *  
     */
    public void removeDependencies() throws IllegalActionException {
        IODependenceOfAtomicActor ioDependence = (IODependenceOfAtomicActor) 
                        this.getAttribute(
                        "_IODependence", IODependence.class);
        ioDependence.removeDependence(set, expired);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** @serial Set port. */
    public DEIOPort set;
    /** @serial Expired port. */
    public DEIOPort expired;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** @serial So we don't need to create the token every time the
     *  timer expires.
     */
    private static final Token _outToken = new Token();

    /** @serial The time to expire.*/
    private double _expireTime = -1.0;

}
