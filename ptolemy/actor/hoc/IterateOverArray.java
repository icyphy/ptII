/* An aggregation of actors.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (neuendor@eecs.berkeley.edu)
 */

package ptolemy.actor.hoc;

//import ptolemy.kernel.*;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.*;

import ptolemy.actor.*;
import ptolemy.graph.*;
import ptolemy.data.*;
import ptolemy.data.type.*;
import ptolemy.actor.IODependence.IOInformation;
import ptolemy.graph.DirectedGraph;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;



//////////////////////////////////////////////////////////////////////////
//// IterateOverArray
/**

A composite actor that takes arrays as inputs and maps actors inside
over the array inputs.

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
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is a composite actor with clones of the ports of the
     *  original actor, the contained actors, and the contained relations.
     *  The ports of the returned actor are not connected to anything.
     *  The connections of the relations are duplicated in the new composite,
     *  unless they cross levels, in which case an exception is thrown.
     *  The local director is cloned, if there is one.
     *  The executive director is not cloned.
     *  NOTE: This will not work if there are level-crossing transitions.
     *
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException If the actor contains
     *   level crossing transitions so that its connections cannot be cloned,
     *   or if one of the attributes cannot be cloned.
     *  @return A new IterateOverArray.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        IterateOverArray newObject = (IterateOverArray)super.clone(workspace);
        return newObject;
    }

    /** If this actor is opaque, transfer any data from the input ports
     *  of this composite to the ports connected on the inside, and then
     *  invoke the fire() method of its local director.
     *  The transfer is accomplished by calling the transferInputs() method
     *  of the local director (the exact behavior of which depends on the
     *  domain).  If the actor is not opaque, throw an exception.
     *  This method is read-synchronized on the workspace, so the
     *  fire() method of the director need not be (assuming it is only
     *  called from here).  After the fire() method of the director returns,
     *  send any output data created by calling the local director's
     *  transferOutputs method.
     *
     *  @exception IllegalActionException If there is no director, or if
     *   the director's fire() method throws it, or if the actor is not
     *   opaque.
     */
    public void fire() throws IllegalActionException {
        if (_debugging) {
            _debug("Called fire()");
        }
        try {
            workspace().getReadAccess();
            if (!isOpaque()) {
                throw new IllegalActionException(this,
                        "Cannot fire a non-opaque actor.");
            }

            // Don't use the local director to transfer inputs.
            Iterator inputPorts = inputPortList().iterator();
            while (inputPorts.hasNext() && !_stopRequested) {
                IOPort p = (IOPort)inputPorts.next();
                for (int i = 0; i < p.getWidth(); i++) {
                    // NOTE: This is not compatible with certain cases
                    // in PN, where we don't want to block on a port
                    // if nothing is connected to the port on the
                    // inside.
                    try {
                        if (p.isKnown(i)) {
                            if (p.hasToken(i)) {
                                Token t = p.get(i);
                                if (_debugging) _debug(getName(),
                                        "transferring input from "
                                        + getName());
                                ArrayToken arrayToken = (ArrayToken)t;
                                for(int j = 0; j < arrayToken.length(); j++) {
                                    p.sendInside(i, arrayToken.getElement(j));
                                }
                            }
                        }
                    } catch (NoTokenException ex) {
                        // this shouldn't happen.
                        throw new InternalErrorException(this, ex, null);
                    }
                }
            }
            
            if (_stopRequested) return;
            while (!_stopRequested && getDirector().prefire()) {
                getDirector().fire();
                if (!getDirector().postfire()) break;
            }           
            if (_stopRequested) return;
            // Use the local director to transfer outputs.
            Iterator outports = outputPortList().iterator();
            while (outports.hasNext() && !_stopRequested) {
                IOPort p = (IOPort)outports.next();
                for (int i = 0; i < p.getWidthInside(); i++) {
                    try {
                        ArrayList list = new ArrayList();
                        while(p.isKnownInside(i) && p.hasTokenInside(i)) {
                            Token t = p.getInside(i);
                            list.add(t);
                        }
                        if(list.size() != 0) {
                            Token[] tokens = (Token[])list.toArray(new Token[list.size()]);
                            p.send(i, new ArrayToken(tokens));
                        }
                    } catch (NoTokenException ex) {
                        throw new InternalErrorException(this, ex, null);
                    }
                }
            }
        } finally {
            workspace().doneReading();
        }
    }

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
    ////                         protected variables               ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
