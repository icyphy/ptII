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
//import ptolemy.actor.lib.Source;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.data.type.BaseType;
import ptolemy.actor.lib.jspaces.util.SpaceFinder;

import net.jini.space.JavaSpace;
import net.jini.core.lease.Lease;
import net.jini.core.event.*;
import java.rmi.server.*;
import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Iterator;

//////////////////////////////////////////////////////////////////////////
//// Subscriber
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
                new StringToken("JaveSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);
        entryName = new Parameter(this, "entryName", 
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);
        
        correct = new TypedIOport(this, "correct", false, true);
        correct.setMultiport(false);
        correct.setTypeEquals(BaseType.BOOLEAN);

        force = new TypedIOport(this, "force", false, true);
        force.setMultiport(false);
        force.setTypeEquals(BaseType.DOUBLE);

        velocity = new TypedIOport(this, "velocity", false, true);
        velocity.setMultiport(false);
        velocity.setTypeEquals(BaseType.DOUBLE);

        position = new TypedIOport(this, "position", false, true);
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
        TokenEntry entrytemp = new TokenEntry(_entryName, 
                null, null);
        TokenEntry entry;
        boolean ready = false;
        while(!ready) { 
            try {
                entry = (TokenEntry)_space.readIfExists(
                        entrytemp, null, Long.MAX_VALUE);
            }catch (Exception e) {
                throw new IllegalActionException(this,
                        "error reading space." +
                        e.getMessage());
            }
            if(entry == null) {
                System.err.println("The publisher is not ready. Try again...");
                Thread.sleep(1000);
            } else {
                ready = true;
            }
        }
        ArrayToken array = (ArrayToken)entry.token;
        _lastTimeStamp = ((DoubleToken)array.getElement(0)).doubleValue();
        _lastForce = ((DoubleToken)array.getElement(1)).doubleValue();
        _lastVelocity = ((DoubleToken)array.getElement(2)).doubleValue();
        _lastPosition = ((DoubleToken)array.getElement(3)).doubleValue();

        // request for notification
        _eventReg = _space.notify(
                template, null, this, Lease.FOREVER, null);
        _notificationSeq = _eventReg.getSequenceNumber();
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
        correct.send(0, new BooleanToken(_correct));
        force.send(0, new DoubleToken(_lastForce));
        velocity.send(0, new DoubleToken(_lastVelocity));
        position.send(0, new DoubleToken(_lastPosition));
    }

    /** Check whether the newly comed set of data is correct, 
     *  if there is any.
     */
    public boolean postfire() throws IllegalActionException {
        if(_newData) {
            // grab a lock so that the the set of data is consistent.
            synchronized(_lock) {
                // do the sanity check.
                double timeInterval = _currentTimeStamp - _lastTimeStamp;
                double fovermiu = _lastForce/_miu;
                double expt = Math.exp(-_miu * timeInterval)
                double computedVelocity = 
                    (_lastVelocity - fovermiu) * expt + fovermiu;
                double computedPosition = _lastPosition +
                    (1.0/_miu)*(_lastVelocity - fovermiu) * (1.0 - expt) +
                    fovermiu * timeInterval;
                if(Math.abs(_currentVelocity - computedVelocity) < _eps &&
                        Math.abs(_currentPosition - computedPosition) < _eps) {
                    _correct = true;
                } else {
                    _correct = false;
                }
                _lastForce = _currentForce;
                _lastVelocity = _currentVelocity;
                _lastPosition = _currentPosition;
            }
        }
    }                    
            
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // The current serial number
    private String _entryName;

    // The space to read from.
    private JavaSpace _space;
    
    // The last sampling time
    private double _lastTimeStamp;

    // The last sample of force.
    private double _lastForce;

    // The last sample of velocity.
    private double _lastVelocity;

    // The last sample of position.
    private double _lastPosition;

    // The current sampling time
    private double _currentTimeStamp;

    // The current sample of force.
    private double _currentForce;

    // The current sample of velocity.
    private double _currentVelocity;

    // The current sample of position.
    private double _currentPosition;

    // Indicating whether there's new data came in.
    private boolean _newData;

    // The correctness of the current outputs, which are the last set of 
    // subscribed data.
    private boolean _correct;

    // The lock that the access of local variables are synchronized on.
    private Object _lock;



    // The list of tokens that received.
    private LinkedList _tokenList;

    // The indicator the last read serial number
    private LastRead _lastRead;
    
    // Used to identify the event registration
    private EventRegistration _eventReg;
    
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
                synchronized(_lastRead) {
                    boolean finished = false;
                    // make sure the actor is not producing outputs.
                    synchronized(_tokenList) {
                        while(!finished) {
                            System.out.println(getName() + 
                                    " is trying to read entry: " +
                                   ( _lastRead.getSerialNumber()+1));
                            TokenEntry entrytemp = new TokenEntry(_entryName, 
                                new Long(_lastRead.getSerialNumber()+1), null);
                            TokenEntry entry;
                            try{
                                entry = (TokenEntry)_space.readIfExists(
                                        entrytemp, null, Long.MAX_VALUE);
                            } catch (Exception e) {
                                throw new InvalidStateException(_container,
                                        "error reading space." +
                                        e.getMessage());
                            }
                            if(entry == null) {
                                System.out.println(getName() + 
                                        " read null from space");
                                /* check min indecies
                                IndexEntry indexmin ;
                                try {
                                   indexmin = (IndexEntry)_space.read(
                                        _minTemplate, null, Long.MAX_VALUE);
                                } catch (Exception e) {
                                    throw new InvalidStateException(_container,
                                            "error reading space." +
                                            e.getMessage());
                                }
                                if(_indexmin != null && 
                                _lastRead.getSerialNumber() >= 
                                        indexmin.position.longValue()) {
                                    finished = true;
                                } else {
                                    _lastRead.setSerialNumber(
                                            indexmin.position.longValue());
                                            }*/
                                finished = true;
                            } else {
                                System.out.println(getName() + 
                                        " reads successfully.");
                                _lastRead.increment();
                                System.out.println(getName() + 
                                        " locally stores entry: " +
                                        _lastRead.getSerialNumber());
                                Token token = entry.token;
                                _tokenList.addLast(token);
                            }
                        }
                        _tokenList.notifyAll();
                    }
                }
            }   
            Thread.currentThread().yield();
        }
        
        //////////////////////////////////////////////////////////////
        ////                     private variables                ////
        
        // the container
        private TypedAtomicActor _container;
        
        // the event
        private RemoteEvent _event;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner class                       ////
    /** A index for the last read token entry serial number.
     *  An instance of this class also serves as the lock for 
     *  reading token entries from the JavaSpaces.
     */
    public class LastRead {
        
        /** Construct LastRead with a given serial number.
         *  @param initserailnumber The initial serial number.
         */
        public LastRead(long initserialnumber) {
            _lastSerialNumber = initserialnumber;
        }
        
        //////////////////////////////////////////////////////////////
        ////                     public methods                   ////
        
        /** Return the serial number of the last token entry being 
         *  read.
         *  @return The last read serail number.
         */
        public long getSerialNumber() {
            return _lastSerialNumber;
        }

        /** Increase the serial number by one.
         */
        public void increment() {
            _lastSerialNumber ++;
        }

        /** Set the serial number.
         */
        public void setSerialNumber(long number) {
            _lastSerialNumber = number;
        }


        
        //////////////////////////////////////////////////////////////
        ////                     private variables                ////
        
        // the last read serial number
        private long _lastSerialNumber;
    }

}
