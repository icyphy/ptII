/* An actor that iterates a contained actor over input arrays.

 Copyright (c) 1997-2003 The Regents of the University of California.
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
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
 */

package ptolemy.actor.hoc;

//import ptolemy.kernel.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.FiringEvent;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.QueueReceiver;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.Token;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// IterateOverArray
/**
This actor iterates a contained actor over input arrays.

FIXME: details.

@author Edward A. Lee, Steve Neuendorffer
@version $Id$
*/
public class IterateOverArray extends TypedCompositeActor {

    /** Create an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *  You should set a director before attempting to execute it.
     *
     *  @param container The container actor.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public IterateOverArray(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        getMoMLInfo().className = "ptolemy.actor.hoc.IterateOverArray";
        new IterateDirector(this, uniqueName("IterateDirector"));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    // Return the type constraints on all connections starting from
    // the specified source port to all the ports in a group of
    // destination ports.  If a source port or a destination port is a
    // port of this composite, then the port is forced to be an array
    // type and the proper constraint on the element type of the array
    // is made.
    protected List _typeConstraintsFromTo(TypedIOPort sourcePort,
            List destinationPortList) {
        List result = new LinkedList();

        boolean srcUndeclared = sourcePort.getTypeTerm().isSettable();
        Iterator destinationPorts = destinationPortList.iterator();
        while (destinationPorts.hasNext()) {
            TypedIOPort destinationPort = (TypedIOPort)destinationPorts.next();
            boolean destUndeclared =
                destinationPort.getTypeTerm().isSettable();

            // FIXME:
            if (srcUndeclared || destUndeclared) {
                if(sourcePort.getContainer().equals(this) ||
                        destinationPort.getContainer().equals(this)) {
                    continue;
                }
                // At least one of the source/destination ports does
                // not have declared type, form type constraint.
                Inequality ineq = new Inequality(sourcePort.getTypeTerm(),
                        destinationPort.getTypeTerm());
                result.add(ineq);
                System.out.println("ineq = " + ineq);
            }
        }
        return result;
    }

    // Check types from a source port to a group of destination ports,
    // assuming the source port is connected to all the ports in the
    // group of destination ports.  Return a list of instances of
    // Inequality that have type conflicts.
    protected List _checkTypesFromTo(TypedIOPort sourcePort,
            List destinationPortList) {
        List result = new LinkedList();
        
        boolean isUndeclared = sourcePort.getTypeTerm().isSettable();
        if (!isUndeclared) {
            // sourcePort has a declared type.
            Type srcDeclared = sourcePort.getType();
            Iterator destinationPorts = destinationPortList.iterator();
            while (destinationPorts.hasNext()) {
                TypedIOPort destinationPort =
                    (TypedIOPort)destinationPorts.next();
                isUndeclared = destinationPort.getTypeTerm().isSettable();

                if (!isUndeclared) {
                    // FIXME
                    if(sourcePort.getContainer().equals(this) ||
                            destinationPort.getContainer().equals(this)) {
                        continue;
                    } 
                    // both source/destination ports are declared,
                    // check type
                    Type destDeclared = destinationPort.getType();
                    int compare = TypeLattice.compare(srcDeclared,
                            destDeclared);
                    if (compare == CPO.HIGHER || compare == CPO.INCOMPARABLE) {
                        Inequality inequality = new Inequality(
                                sourcePort.getTypeTerm(),
                                destinationPort.getTypeTerm());
                        result.add(inequality);
                    }
                }
            }
        }
        return result;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    ///////////////////////////////////////////////////////////////////
    //// IterateDirector
    
    /**
     *  FIXME.
     */
    private class IterateDirector extends Director {

        /** Create a new instance of the director for IterateOverArray.
         *  @param container The container for the director.
         *  @param name The name of the director.
         *  @throws IllegalActionException Should not be thrown.
         *  @throws NameDuplicationException Should not be thrown.
         */
        public IterateDirector(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            setPersistent(false);
        }

        /** Invoke an iteration on all of the deeply contained actors of the
         *  container of this director repeatedly until they all return false
         *  in prefire or any one return false in postfire. The contained
         *  actors are interated in the order reported by the entityList()
         *  method of the container.
         *  @exception IllegalActionException If any called method of one
         *  of the associated actors throws it.
         */
        public void fire() throws IllegalActionException {
            Nameable container = getContainer();
            Iterator actors = ((CompositeActor)container)
                    .deepEntityList().iterator();
            int iterationCount = 1;
            while (actors.hasNext() && !_stopRequested) {
                Actor actor = (Actor)actors.next();
                if (_debugging) {
                    _debug(new FiringEvent(this, actor,
                            FiringEvent.BEFORE_ITERATE,
                            iterationCount));
                }
                boolean postfireReturns = true;
                int result = Actor.COMPLETED;
                while (result != Actor.NOT_READY) {
                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.BEFORE_ITERATE,
                                iterationCount));
                    }
                    result = actor.iterate(1);
                    if (_debugging) {
                        _debug(new FiringEvent(this, actor,
                                FiringEvent.AFTER_ITERATE,
                                iterationCount));
                    }
                    // FIXME: Perhaps should return if there is no more input data,
                    // irrespective of return value of perfire() of the actor, which
                    // may not be reliable.
                    if (result == Actor.STOP_ITERATING) {
                        postfireReturns = true;
                        if (_debugging) {
                            _debug("Actor requests halt: "
                                    + ((Nameable)actor).getFullName());
                        }
                        break;
                    }
                }
            }
        }

        /** Delegate by calling fireAt() on the director of the container's
         *  container.
         *  @param actor The actor requesting firing.
         *  @param time The time at which to fire.
         */
        public void fireAt(Actor actor, double time)
                throws IllegalActionException {
            Director director = IterateOverArray.this.getExecutiveDirector();
            if (director != null) {
                director.fireAt(actor, time);
            }
        }

        /** Delegate by calling fireAtCurrentTime() on the director
         *  of the container's container.
         *  @param actor The actor requesting firing.
         *  @param time The time at which to fire.
         */
        public void fireAtCurrentTime(Actor actor)
                throws IllegalActionException {
            Director director = IterateOverArray.this.getExecutiveDirector();
            if (director != null) {
                director.fireAtCurrentTime(actor);
            }
        }
        
        // FIXME: Other methods that need to be delegated?
        
        /** Return a new instance of QueueReceiver.
         *  @return A new instance of QueueReceiver.
         *  @see QueueReceiver
         */
        public Receiver newReceiver() {
            // TODO Auto-generated method stub
            return new QueueReceiver();
        }

        /** Transfer data from an input port of the
         *  container to the ports it is connected to on the inside.
         *  This method extracts tokens from the input array and
         *  provides them sequentially to the corresponding ports
         *  of the contained actor.
         *  @exception IllegalActionException Should not be thrown.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         */
        public boolean transferInputs(IOPort port)
                throws IllegalActionException {

            boolean result = false;
            for (int i = 0; i < port.getWidth(); i++) {
                // NOTE: This is not compatible with certain cases
                // in PN, where we don't want to block on a port
                // if nothing is connected to the port on the
                // inside.
                try {
                    if (port.isKnown(i)) {
                        if (port.hasToken(i)) {
                            Token t = port.get(i);
                            if (_debugging) {
                                _debug(getName(),
                                        "transferring input from "
                                        + port.getName());
                            }
                            ArrayToken arrayToken = (ArrayToken)t;
                            for(int j = 0; j < arrayToken.length(); j++) {
                                port.sendInside(i, arrayToken.getElement(j));
                            }
                            result = true;
                        }
                    }
                } catch (NoTokenException ex) {
                    // this shouldn't happen.
                    throw new InternalErrorException(this, ex, null);
                }
            }
            return result;
        }

        /** Transfer data from the inside receivers of an output port of the
         *  container to the ports it is connected to on the outside.
         *  This method packages the available tokens into a single array.
         *  @exception IllegalActionException Should not be thrown.
         *  @param port The port to transfer tokens from.
         *  @return True if at least one data token is transferred.
         *  @see IOPort#transferOutputs
         */
        public boolean transferOutputs(IOPort port)
                throws IllegalActionException {
            boolean result = false;
            for (int i = 0; i < port.getWidthInside(); i++) {
                try {
                    ArrayList list = new ArrayList();
                    while(port.isKnownInside(i) && port.hasTokenInside(i)) {
                        Token t = port.getInside(i);
                        list.add(t);
                    }
                    if(list.size() != 0) {
                        Token[] tokens = (Token[])list.toArray(new Token[list.size()]);
                        if (_debugging) {
                            _debug(getName(),
                                    "transferring output to "
                                    + port.getName());
                        }
                        port.send(i, new ArrayToken(tokens));
                    }
                    result = true;
                } catch (NoTokenException ex) {
                    throw new InternalErrorException(this, ex, null);
                }
            }
            return result;
        }
    }
}
