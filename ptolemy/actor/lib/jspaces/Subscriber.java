/* A subscriber class to the Java Spaces.

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
@AcceptedRating Red (yuhong@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.jspaces;

import ptolemy.actor.lib.Source;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.data.expr.Parameter;
import ptolemy.data.*;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.*;
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
A subscriber to the Java Spaces. This actor register a TokenEntry
of interest to the JavaSpaces. When get notified, it reads the
TokenEntry. Entries are not ordered. New entry will overwrite the 
old one, if the actor is not fired during the time.

If the parameter "blocking" is set to true, the actor will block
when it is fired but it has no receivings from last firing.
Otherwise, it will produce no output.

@author Jie Liu, Yuhong Xiong
@version $Id$
*/

public class Subscriber extends Source implements RemoteEventListener {

    /** Construct an actor with the given container and name.
     *  The output and trigger ports are also constructed.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public Subscriber(TypedCompositeActor container, String name)
            throws NameDuplicationException, IllegalActionException  {
        super(container, name);
    	jspaceName = new Parameter(this, "jspaceName",
                new StringToken("JavaSpaces"));
        jspaceName.setTypeEquals(BaseType.STRING);
        entryName = new Parameter(this, "entryName",
                new StringToken(""));
        entryName.setTypeEquals(BaseType.STRING);
        blocking = new Parameter(this, "blocking",
                new BooleanToken(false));
        blocking.setTypeEquals(BaseType.BOOLEAN);
        defaultToken = new Parameter(this, "defaultToken",
                new DoubleToken(0.0));
        defaultToken.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The Java Space name. The default name is "JavaSpaces" of
     *  type StringToken.
     */
    public Parameter jspaceName;

    /** The name for the subcribed entry. The default value is
     *  an empty string of type StringToken.
     */
    public Parameter entryName;

    /** Indicate whether the actor blocks when it can not read
     *  an entry from the space. The default value is false of
     *  type BooleanToken.
     */
    public Parameter blocking;

    /** The default initial token. If the actor is nonblocking
     *  and there is no matching entry in the space, then this
     *  token will be output. Default value is 0.0 of type
     *  DoubleToken. The token and the type will be override
     *  by the first entry read from the space.
     */
    public Parameter defaultToken;


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
        Subscriber newobj = (Subscriber)super.clone(ws);
        newobj.jspaceName = (Parameter)newobj.getAttribute("jspaceName");
        newobj.entryName = (Parameter)newobj.getAttribute("entryName");
        newobj.blocking = (Parameter)newobj.getAttribute("blocking");
        newobj.defaultToken = (Parameter)newobj.getAttribute("defaultToken");
        return newobj;
    }

    /** update parameters.
     */
    public void attributeChanged(Attribute attr)
        throws IllegalActionException {
        if (attr == blocking) {
            _blocking = ((BooleanToken)blocking.getToken()).booleanValue();
        }
    }

    /** Find the JavaSpaces and retrieve the first token. The type of
     *  the output is infered from the type of the token
     *  @exception IllegalActionException If the space cannot be found.
     */
    public void preinitialize() throws IllegalActionException {
        _entryName = ((StringToken)entryName.getToken()).stringValue();
   
        _lookupThread = Thread.currentThread();
        
        _space = SpaceFinder.getSpace(
                ((StringToken)jspaceName.getToken()).stringValue());
        
        _lookupThread = null;

        // export this object so that the space can call back
        try {
            UnicastRemoteObject.exportObject(this);
        } catch (RemoteException e) {
            //throw new IllegalActionException( this,
            //        "unable to export object. Please check if RMI is OK. " +
            //        e.getMessage());
            System.err.println("Warning: " + e.getMessage());
        }
        //_tokenList = new LinkedList();
        //FIXME: set type by the token read.
        output.setTypeEquals(defaultToken.getToken().getType());
        
        TokenEntry template = new TokenEntry(_entryName, null, null);
        
        // request for notification
        try {
            _eventReg = _space.notify(
                    template, null, this, Lease.FOREVER, null);
            _notificationSeq = _eventReg.getSequenceNumber();
            
        } catch (Exception e) {
            throw new IllegalActionException( this,
                    "error reading from the JavaSpace." +
                    e.getMessage());
        }
    }

    /** Fork a new thread to handle the notify event.
     */
    public void notify(RemoteEvent event) {
        if (_debugging) {
            _debug(getName(), "Get notified from JavaSpaces.");
        }
        NotifyHandler nh = new NotifyHandler(this, event);
        new Thread(nh).start();
    }

    /** Produce the oldest received token. One for each firing.
     *  If there's no token available, then the bahavior depends
     *  on the "blocking" parameter. If blocking is true, the
     *  exexution blocks until there's a token coming in.
     *  Otherwise, the defaultToken is produced.
     */
    public void fire() throws IllegalActionException {
        // make sure no one is writing the token list.
        //System.out.println("Subscriber get fired");
        synchronized(_lock) {
            while(true) {
                if (_lastReadToken == null) {
                    if(_blocking) {
                        try {
                            if (_debugging) {
                                _debug(getName(), " is waiting.");
                            }
                            _lock.wait();
                            if (_debugging) {
                                _debug(getName(), " wakes up.");
                            }
                        } catch (InterruptedException e) {
                            throw new IllegalActionException(this,
                                    "blocking interrupted." +
                                    e.getMessage());
                        }
                    } else {
                        output.send(0, defaultToken.getToken());
                        break;
                    }
                } else {
                    if (_debugging) {
                        _debug(getName(), " outputs a token.");
                    }
                    output.send(0, _lastReadToken);
                    _lastReadToken = null;
                    return;
                }
            }
        }
    }

    /** Kill the lookup thread if it is not returned.
     */
    public void stopFire() {
        if (_lookupThread != null) {
            _lookupThread.interrupt();
        }
        super.stopFire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The current serial number
    private String _entryName;

    // The space to read from.
    private JavaSpace _space;

    // cached value of whether blocks when no data
    private boolean _blocking;

    // The list of tokens that received.
    private Object _lock = new Object();

    // The indicator the last read serial number
    private Token _lastReadToken;

    // Used to identify the event registration
    private EventRegistration _eventReg;

    // Notification sequence number
    private long _notificationSeq;

    // The thread that finds jini.
    private Thread _lookupThread;

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
                    // make sure the actor is not producing outputs.
                    TokenEntry entrytemp = new TokenEntry(_entryName,
                            null, null);
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
                        //System.out.println(getName() +
                        //        " read null from space");
                    } else {
                        //System.out.println(getName() +
                        //        " reads successfully.");
                        
                        _lastReadToken = entry.token;
                    }
                    _lock.notifyAll();
                }
            }
            //Thread.currentThread().yield();
            _notificationSeq = _event.getSequenceNumber();
        }

        //////////////////////////////////////////////////////////////
        ////                     private variables                ////

        // the container
        private TypedAtomicActor _container;

        // the event
        private RemoteEvent _event;
    }
}
