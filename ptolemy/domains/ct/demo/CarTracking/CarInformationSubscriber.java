/* A subscriber class that also checks the car model.

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

@ProposedRating Red (liuj@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.domains.ct.demo.CarTracking;

import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.lib.jspaces.TokenEntry;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.type.BaseType;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;

import net.jini.space.JavaSpace;
import net.jini.core.lease.Lease;
import net.jini.core.event.*;
import net.jini.core.transaction.TransactionException;
import java.rmi.server.*;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// CarInformationSubscriber
/**
A subscriber to the JavaSpaces for car information and do a sanity
check. It outputs whether the data subscribed is reliable, and 
the data recieved which are the force, velocity and position of
another car.

@author Jie Liu
@version $Id$
*/

public class CarInformationSubscriber extends TypedAtomicActor
    implements RemoteEventListener {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public CarInformationSubscriber(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    	jspaceName = new Parameter(this, "jspaceName", 
                new StringToken("JavaSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);
        entryName = new Parameter(this, "entryName", 
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);
        
        correct = new TypedIOPort(this, "correct", false, true);
        correct.setMultiport(false);
        correct.setTypeEquals(BaseType.BOOLEAN);

        force = new TypedIOPort(this, "force", false, true);
        force.setMultiport(false);
        force.setTypeEquals(BaseType.DOUBLE);

        velocity = new TypedIOPort(this, "velocity", false, true);
        velocity.setMultiport(false);
        velocity.setTypeEquals(BaseType.DOUBLE);

        position = new TypedIOPort(this, "position", false, true);
        position.setMultiport(false);
        position.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Output for the correctness of subscribed data. Default is true.
     */
    public TypedIOPort correct;

    /** Output for the force. Default is 0.0.
     */
    public TypedIOPort force;
    
    /** Output for the velocity. Default is 0.0.
     */
    public TypedIOPort velocity;

    /** Output for the position. Default is 0.0.
     */
    public TypedIOPort position;

    /** The Java Space name. The default name is "JavaSpaces" of 
     *  type StringToken.
     */
    public Parameter jspaceName;

    /** The name for the subcribed entry. The default value is
     *  an empty string of type StringToken.
     */
    public Parameter entryName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then sets the <code>output</code>
     *  variable to equal the new port.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace ws)
	    throws CloneNotSupportedException {
	try {
	    CarInformationSubscriber newobj =
                (CarInformationSubscriber)super.clone(ws);
	    newobj.jspaceName = (Parameter)newobj.getAttribute("jspaceName");
            newobj.entryName = (Parameter)newobj.getAttribute("entryName");
            newobj.correct = (TypedIOPort)newobj.getPort("correct");
            newobj.force = (TypedIOPort)newobj.getPort("force");
            newobj.velocity = (TypedIOPort)newobj.getPort("velocity");
            newobj.position = (TypedIOPort)newobj.getPort("position");
	    return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }
            
    /** Find the JavaSpaces and retrieve the first token. The type of
     *  the output is infered from the type of the token
     *  @exception IllegalActionException If the space cannot be found.
     */
    public void preinitialize() throws IllegalActionException {
        _entryName = ((StringToken)entryName.getToken()).toString();
        _space = SpaceFinder.getSpace(
                ((StringToken)jspaceName.getToken()).toString());

        // export this object so that the space can call back
        try {
            UnicastRemoteObject.exportObject(this);
        } catch (RemoteException e) {
            //throw new IllegalActionException( this,
            //        "unable to export object. Please check if RMI is OK. " +
            //        e.getMessage());
            System.err.println("Warning: " + e.getMessage());
        }
                
        // read the current data in the JavaSpaces, and use
        // it as the initial condition
        TokenEntry entryTemplate = new TokenEntry(_entryName, 
                null, null);
        TokenEntry entry;
        boolean ready = false;
        while(!ready) { 
            try {
                entry = (TokenEntry)_space.readIfExists(
                        entryTemplate, null, Long.MAX_VALUE);
            }catch (Exception e) {
                throw new IllegalActionException(this,
                        "error reading space." +
                        e.getMessage());
            }
            if(entry == null) {
                System.err.println("The publisher is not ready. Try again...");
                try {
                    Thread.sleep(5000);
                } catch(InterruptedException ex) {
                    throw new IllegalActionException(this,
                            "sleep interrupted. " + ex.getMessage());
                }
            } else {
                ready = true;
                _lastData = (ArrayToken)entry.token;
            }
        }
        // request for notification
        try {
            _eventReg = _space.notify(
                    entryTemplate, null, this, Lease.FOREVER, null);
            _notificationSeq = _eventReg.getSequenceNumber();
        } catch (RemoteException re) {
            throw new IllegalActionException(this,
                    "failed registering for notification. " + re.getMessage());
        } catch (TransactionException te) {
            throw new IllegalActionException(this,
                    "failed registering for notification. " + te.getMessage());
        }
    }

    /** Fork a new thread to handle the notify event.
     */
    public void notify(RemoteEvent event) {
        NotifyHandler nh = new NotifyHandler(this, event);
        new Thread(nh).start();
    }
    
    /** Always output the last set of subscribed data.
     *  The new data only takes effect after postfire.
     */
    public void fire() throws IllegalActionException {
        //System.out.println("Correct = " + _correct);
        correct.send(0, new BooleanToken(_correct));
        force.send(0, _lastData.getElement(1));
        velocity.send(0, _lastData.getElement(2));
        position.send(0, _lastData.getElement(3));
    }
    
    /** Check whether the newly comed set of data is correct, 
     *  if there is any.
     */
    public boolean postfire() throws IllegalActionException {
        if(_hasNewData) {
            //System.out.println("check for correctness");
            // grab a lock so that the the set of data is consistent.
            double lastTimeStamp =
                ((DoubleToken)_lastData.getElement(0)).doubleValue();
            //System.out.println("last time stamp " + lastTimeStamp);
            double lastF = 
                ((DoubleToken)_lastData.getElement(1)).doubleValue();
            //System.out.println("last force " + lastF);
            double lastV = 
                ((DoubleToken)_lastData.getElement(2)).doubleValue();
            //System.out.println("last velocity " + lastV);
            double lastP = 
                ((DoubleToken)_lastData.getElement(3)).doubleValue();
            //System.out.println("last position " + lastP);
            
            synchronized(_lock) {
                // do the sanity check.
                double currentPosition =
                    ((DoubleToken)_currentData.getElement(3)).doubleValue();
                if (currentPosition >= lastP) {
                    double currentTimeStamp = 
                        ((DoubleToken)_currentData.getElement(0)).doubleValue();
                    double timeInterval = currentTimeStamp - lastTimeStamp;
                    //System.out.println("time interval: " + timeInterval);
                    double fovermiu = lastF/_miu;
                    double expt = Math.exp((-1.0) * _miu * timeInterval);
                    double computedVelocity = 
                        (lastV - fovermiu) * expt + fovermiu;
                    //System.out.println("computed v: " + computedVelocity);
                    double computedPosition = lastP +
                        (1.0/_miu)*(lastV - fovermiu) * (1.0 - expt) +
                        fovermiu * timeInterval;
                    //System.out.println("computed p: " + computedPosition);
                    double currentVelocity = 
                        ((DoubleToken)_currentData.getElement(2)).doubleValue();
                    //System.out.println("read velocity: " +currentVelocity);
                    
                    //System.out.println("read position: " +currentPosition);
                    if(Math.abs(currentVelocity - computedVelocity) < _eps &&
                            Math.abs(currentPosition - computedPosition) <
                            _eps) {
                        _correct = true;
                    } else {
                        _correct = false;
                    }
                    _lastData = _currentData;
                    _hasNewData = false;
                } else {
                    // New data are definitely wrong. Don't even update them.
                    _correct = false;
                    _hasNewData = false;
                }
            }
        }
        //System.out.println("CORRECT" + _correct); 
        correct.send(0, new BooleanToken(_correct));
        return true;
    }                    
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // The entry name.
    private String _entryName;
    
    // The space to read from.
    private JavaSpace _space;
    
    // Indicating whether there's new data came in.
    private boolean _hasNewData;
    
    // The correctness of the current outputs, which are the last set of 
    // subscribed data.
    private boolean _correct = true;

    // The lock that the access of local variables are synchronized on.
    private Object _lock = new Object();
  
    // Last set of data.
    private ArrayToken _lastData;

    // Current set of data.
    private ArrayToken _currentData;

    // Used to identify the event registration
    private EventRegistration _eventReg;
    
    // Used to identify notification.
    private long _notificationSeq;
    
    // Constants in the model
    // Friction coefficient
    private final double _miu = 0.5;
    
    // error tolerance.
    private final double _eps = 1;

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    
    public class NotifyHandler implements Runnable {
        
        /** construct the notify handler
         */
        public NotifyHandler(TypedAtomicActor container, RemoteEvent event) {
            _container = container;
            _event = event;
        }

        //////////////////////////////////////////////////////////////
        ////                     public methods                   ////
                
        /** Read the entry token from the javaspaces.
         */
        public void run() {
            // check if it is the right notification
            if (_event.getSource().equals(_eventReg.getSource()) &&
                    _event.getID() == _eventReg.getID() && 
                    _event.getSequenceNumber() > _notificationSeq) {
                // grab a lock and read all new entries.
                synchronized(_lock) {
                    TokenEntry entryTemplate = new TokenEntry(_entryName, 
                            null, null);
                    TokenEntry entry;
                    try {
                        entry = (TokenEntry)_space.readIfExists(
                                entryTemplate, null, 100);
                    } catch (Exception e) {
                        throw new InvalidStateException(_container,
                                "error reading from space." +
                                e.getMessage());
                    }
                    if(entry == null) {
                        System.out.println(getName() + 
                                " read null from space");
                    }
                    _currentData = (ArrayToken)entry.token;
                    _hasNewData = true;
                }
            }
        }
        
        //////////////////////////////////////////////////////////////
        ////                     private variables                ////
        
        // the container
        private TypedAtomicActor _container;
        
        // the event
        private RemoteEvent _event;
    }
}
