/* A source that emits a trigger signal periodically.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.domains.tm.lib;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;

//////////////////////////////////////////////////////////////////////////
//// PeriodicTrigger
/**
This actor produces a ramp at 2 Hz.
@author Edward A. Lee
@version $Id$
@since Ptolemy II 2.0
*/
public class PeriodicTrigger extends TypedAtomicActor {

    public PeriodicTrigger(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.GENERAL);
        period = new Parameter(this, "period", new LongToken(1000));
        period.setTypeEquals(BaseType.LONG);

    }

    ///////////////////////////////////////////////////////////////////
    ////                  Ports and Parameters                      ////

    /** The output port.
     */
    public TypedIOPort output;

    /** The execution period, in terms of Hz. Default is 2.0.
     */
    public Parameter period;

    ///////////////////////////////////////////////////////////////////
    ////                          Public Methods                     ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>output</code>
     *  variable to equal the new port.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        PeriodicTrigger newObject = (PeriodicTrigger)super.clone(workspace);
        try {
            newObject.period.setTypeEquals(BaseType.LONG);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(getName() + ": clone failed.");
        }
        return newObject;
    }

    /** Once the period is updated, calculate the execution period.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == period) {
            long periodValue = ((LongToken)period.getToken()).longValue();
            if (periodValue < 100) {
                throw new IllegalActionException(this,
                        "does not support period lower than 100, value was:"
                        + periodValue);
            }
            _period = periodValue;
        }
    }

    public void initialize() throws IllegalActionException {
        Trigger trigger = new Trigger(this);
        _triggerThread = new Thread(trigger);
        _triggerThread.start();
    }

    public boolean postfire() throws IllegalActionException {
        //System.out.println(getName() + " output token.");
        output.broadcast(new Token());
        return true;
    }

    public void stopFire() {
        if (_triggerThread != null) {
            _triggerThread.interrupt();
        }
    }


    // Inner class
    public class Trigger implements Runnable {

        public Trigger(Actor container) {
            _container = container;
        }

        public void run() {
            while (true) {
                try {
                    Thread.sleep(_period);
                } catch (InterruptedException ex) {
                    break;
                }
                try {
                    _container.iterate(1);
                } catch (IllegalActionException ex) {
                    throw new InvalidStateException((NamedObj)_container,
                            "IllegalActionException at execution" + ex.getMessage());
                }
            }
        }

        Actor _container;
    }

    private long _period;
    private Thread _triggerThread;
}
