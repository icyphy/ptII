/* An actor containing a PetriNet composite actor. */

 Copyright (c) 2001 The Regents of the University of California.
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
@ProposedRating Red (yukewang@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/


package ptolemy.domains.petrinet.kernel;

import ptolemy.kernel.CompositeEntity;
import ptolemy.actor.TypedActor;

import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.ComponentRelation;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.InvalidStateException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.Workspace;
import ptolemy.actor.Director;
import ptolemy.actor.Manager;
import ptolemy.actor.Receiver;
import ptolemy.actor.IOPort;
import ptolemy.actor.IORelation;
import ptolemy.actor.Actor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.Token;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Variable;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Typeable;
import ptolemy.data.type.BaseType;
import ptolemy.graph.Inequality;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// PetrinetActor
/**
A Petri net Actor

@author  Yuke Wang
@version $Id$
*/
public class PetriNetActor extends TypedCompositeActor implements TypedActor   {

    public PetriNetActor() {
        super();
        getMoMLInfo().className =
            "ptolemy.domains.petrinet.kernel.PetriNetActor";
    }

    public PetriNetActor(Workspace workspace) {
	super(workspace);
        getMoMLInfo().className =
            "ptolemy.domains.petrinet.kernel.PetriNetActor";
    }

    public PetriNetActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        getMoMLInfo().className =
            "ptolemy.domains.petrinet.kernel.PetriNetActor";
    }


    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////




    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////




    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        PetriNetActor newObject = (PetriNetActor)super.clone(workspace);
        return newObject;
    }



    public ComponentRelation newRelation(String name)
            throws NameDuplicationException {
        try {
            _workspace.getWriteAccess();

            TypedIORelation rel = new TypedIORelation(this, name);
            return rel;

        } catch (IllegalActionException ex) {
            throw new InternalErrorException(
                    "TypedCompositeActor.newRelation: Internal error: "
                    + ex.getMessage());
        }

        finally {
            _workspace.doneWriting();
        }
    }


    public void fire() throws IllegalActionException {


    }

    public Director getDirector() {
        CompositeEntity container = (CompositeEntity)getContainer();
        if (container instanceof CompositeActor) {
            return ((CompositeActor)container).getDirector();
        }
        return null;
    }

    /** Return the executive director (same as getDirector()).
     *  @return The executive director.
     */
    public Director getExecutiveDirector() {
        return getDirector();
    }

    public Manager getManager() {
	try {
	    _workspace.getReadAccess();
	    CompositeEntity container = (CompositeEntity)getContainer();
	    if (container instanceof CompositeActor) {
		return ((CompositeActor)container).getManager();
	    }
	    return null;
	} finally {
	    _workspace.doneReading();
	}
    }


    public void initialize() throws IllegalActionException {
        getDirector().initialize(this);
    }


    public List inputPortList() {
        if(_inputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                // Update the cache.
                LinkedList inports = new LinkedList();
                Iterator ports = portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if (p.isInput()) {
                        inports.add(p);
                    }
                }
                _cachedInputPorts = inports;
                _inputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }
        return _cachedInputPorts;
    }





    public int iterate(int count) throws IllegalActionException {
	int n = 0;
	while (n++ < count) {
	    if (prefire()) {
		fire();
		if(!postfire()) return STOP_ITERATING;
	    } else {
                return NOT_READY;
	    }
	}
	return COMPLETED;
    }

    public Receiver newReceiver() throws IllegalActionException {
        Director dir = getDirector();
        if (dir == null) {
            throw new IllegalActionException(this,
                    "Cannot create a receiver without a director.");
        }
        return dir.newReceiver();
    }

    public List outputPortList() {
        if(_outputPortsVersion != _workspace.getVersion()) {
            try {
                _workspace.getReadAccess();
                _cachedOutputPorts = new LinkedList();
                Iterator ports = portList().iterator();
                while(ports.hasNext()) {
                    IOPort p = (IOPort)ports.next();
                    if( p.isOutput()) {
                        _cachedOutputPorts.add(p);
                    }
                }
                _outputPortsVersion = _workspace.getVersion();
            } finally {
                _workspace.doneReading();
            }
        }
        return _cachedOutputPorts;
    }


    public boolean postfire() throws IllegalActionException {

        return true;
    }


    public boolean prefire() throws IllegalActionException {

        return true;
    }


    public void preinitialize() throws IllegalActionException {


    }


    public void stopFire() {
    }

    public void terminate() {
        try {
            wrapup();
        }
        catch (IllegalActionException e) {
            // Just ignore everything and terminate.
        }
    }


    public List typeConstraintList() {
        try {
            _workspace.getReadAccess();

            List result = new LinkedList();
            Iterator inPorts = inputPortList().iterator();
            while (inPorts.hasNext()) {
                TypedIOPort inport = (TypedIOPort)inPorts.next();
                boolean isUndeclared = inport.getTypeTerm().isSettable();
                if (isUndeclared) {
                    // inport has undeclared type
                    Iterator outPorts = outputPortList().iterator();
                    while (outPorts.hasNext()) {
                        TypedIOPort outport =
                            (TypedIOPort)outPorts.next();

                        isUndeclared = outport.getTypeTerm().isSettable();
                        if (isUndeclared && inport != outport) {
                            // outport also has undeclared type
                            Inequality ineq = new Inequality(
                                    inport.getTypeTerm(),
                                    outport.getTypeTerm());
                            result.add(ineq);
                        }
                    }
                }
            }

            // Collect constraints from contained Typeables.
            Iterator ports = portList().iterator();
            while (ports.hasNext()) {
                Typeable port = (Typeable)ports.next();
                result.addAll(port.typeConstraintList());
            }

            Iterator attrib = attributeList(Typeable.class).iterator();
            while (attrib.hasNext()) {
                Typeable att = (Typeable)attrib.next();
                result.addAll(att.typeConstraintList());
            }

            // Collect constraints from all transitions.
            Iterator trs = relationList().iterator();
            while (trs.hasNext()) {
                Relation tr = (Relation)trs.next();
                attrib = tr.attributeList(Typeable.class).iterator();
                while (attrib.hasNext()) {
                    Typeable att = (Typeable)attrib.next();
                    result.addAll(att.typeConstraintList());
                }
            }

            return result;

        } finally {
            _workspace.doneReading();
        }
    }




    public void wrapup() throws IllegalActionException {
        //Iterator inputPorts = inputPortList().iterator();
        //while (inputPorts.hasNext()) {
        //    TypedIOPort inport = (TypedIOPort)inputPorts.next();
        //    _removeInputVariables(inport);
        //}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Cached lists of input and output ports.
    private transient long _inputPortsVersion = -1;
    private transient LinkedList _cachedInputPorts;
    private transient long _outputPortsVersion = -1;
    private transient LinkedList _cachedOutputPorts;

    // Stores for each state a map from input ports to boolean flags
    // indicating whether a channel is connected to an output port
    // of the refinement of the state.
    private Map _connectionMaps = null;

    // Version of the connection maps.
    private long _connectionMapsVersion = -1;

    // The map from input ports to boolean flags indicating whether a
    // channel is connected to an output port of the refinement of the
    // current state.
    private Map _currentConnectionMap = null;


    // Version of the reference to the initial state.
    private long _initialStateVersion = -1;



}
