/* An aggregation of typed actors, specified by a Ptalon model.

 Copyright (c) 1997-2006 The Regents of the University of California.
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
package ptolemy.actor.ptalon;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import ptolemy.actor.Actor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
////PtalonActor

/**
A TypedCompositeActor is an aggregation of typed actors.  A PtalonActor
is a TypedCompositeActor whose aggregation is specified by a Ptalon
model in an external file.  This file is specified in a FileParameter, 
and it is loaded during initialization.
<p>

@author Adam Cataldo
@Pt.ProposedRating Red (acataldo)
@Pt.AcceptedRating Red (acataldo)
*/

public class PtalonActor extends TypedCompositeActor {

    /** Construct a PtalonActor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PtalonActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        setClassName("ptolemy.actor.ptalon.PtalonActor");
        ptalonCodeLocation = new FileParameter(this, "ptalonCodeLocation");
        _initializeLists();
        _fileName = "";
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
        
    /**
     * Add an atomic actor to this actor with the specified className.
     * If the className is in use, this will do nothing and return null.
     * otherwise, it will return the generated TypedAtomicActor.
     * @param className The desired class name for the actor.
     * @param actorName The desired display name of the actor.
     * @return The created actor or null.
     * @exception IllegalActionException If generated in trying to
     * create the new actor.
     */
    public TypedAtomicActor addAtomicActor(String className, String actorName) throws IllegalActionException {
        try {
            String displayName = uniqueName(actorName);
            Object[] args = new Object[] {this, displayName};
            Constructor cons;
            Class actorClass = Class.forName(className); 
            Class[] argClasses = new Class[] {CompositeEntity.class, String.class};
            cons = actorClass.getConstructor(argClasses);
            TypedAtomicActor p = (TypedAtomicActor) cons.newInstance(args);
            _actors.add(p);
            List portList = p.portList();
            TypedIOPort atomicPort;
            TypedIOPort newPort;
            TypedIORelation r;
            for (int j = portList.size() - 1; j >= 0; j--) {
                if (!(portList.get(j) instanceof TypedIOPort)) {
                    continue;
                }
                atomicPort = (TypedIOPort) portList.get(j);
                newPort = new TypedIOPort(this, atomicPort.getName());
                r = new TypedIORelation(this, uniqueName("relation"));
                atomicPort.link(r);
                newPort.link(r);
            }
            return p;
        } catch(Exception e) {
            throw new IllegalActionException(e.getMessage());
        }
    }

    /**
     * Add a parameter to this actor with the specified name.
     * If the name is in use, this will do nothing and return null.
     * otherwise, it will return the generated Parameter.
     * @param name The desired name for the parameter.
     * @return The created parameter or null.
     * @exception IllegalActionException If generated in trying to
     * create the new parameter.
     */
    public Parameter addParameter(String name) throws IllegalActionException {
        try {
            Parameter p = new Parameter(this, name);
            _parameters.add(p);
            return p;
        } catch(NameDuplicationException e) {
            //Do nothing, just give up.
        }
        return null;
    }
    
    /**
     * Add a port to this actor with the specified name.
     * If the name is in use, this will do nothing and return null.
     * otherwise, it will return the generated Parameter.  The
     * <i>flow</i> parameter is one of:
     *    PtalonActor.INPUT
     *    PtalonActor.OUTPUT
     *    PtalonActor.BIDIRECTIOAL (Default for bad flow value.)
     * @param name The desired name for the port.
     * @param flow The desired flow type for the parameter.
     * @return The created port or null.
     * @exception IllegalActionException If generated in trying to
     * create the new port.
     */
    public TypedIOPort addPort(String name, String flow) throws IllegalActionException {
        try {
            TypedIOPort p;
            if (flow.equals(INPUT)) {
                p = new TypedIOPort(this, name, true, false);
            } else if (flow.equals(OUTPUT)) {
                p = new TypedIOPort(this, name, false, true);
            } else {
                p = new TypedIOPort(this, name, true, true);
            }
            _ports.add(p);
            return p;
        } catch(NameDuplicationException e) {
            //Do nothing, just give up.
        }
        return null;
    }
    
    
    /**
     * Add a relation to this actor with the specified name.
     * If the name is in use, this will do nothing and return null.
     * otherwise, it will return the generated TypedIORelation.
     * @param name The desired name for the reltation.
     * @return The created relation or null.
     * @exception IllegalActionException If generated in trying to
     * create the new relation.
     */
    public TypedIORelation addRelation(String name) throws IllegalActionException {
        try {
            TypedIORelation p = new TypedIORelation(this, name);
            _relations.add(p);
            return p;
        } catch(NameDuplicationException e) {
            //Do nothing, just give up.
        }
        return null;
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  This only responds
     *  to changes in the <i>ptalonCode</i> parameter.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute att) throws IllegalActionException {
        if (att == ptalonCodeLocation) {
            File inputFile = ptalonCodeLocation.asFile();
            if (inputFile == null) {
                return;
            }
            PtalonLexer lex;
            PtalonRecognizer rec;
            try {
                String newFileName = inputFile.getCanonicalPath();
                if (newFileName.equals(_fileName)) {
                    return;
                }
                FileReader reader = new FileReader(inputFile);
                lex = new PtalonLexer(reader);
                rec = new PtalonRecognizer(lex);
                _cleanUp();
                rec.actor_definition(this);
                _fileName = newFileName;
            } catch (Exception e) {
                throw new IllegalActionException(e.getMessage());
            }
        } else {
            super.attributeChanged(att);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                       public variables                    ////
        
    /**
     * Strings to denote bidirectional flow
     */
    public static String BIDIRECTIONAL = "bidirectional"; 

    /**
     * Strings to denote input flow
     */
    public static String INPUT = "input"; 

    /**
     * Strings to denote output flow
     */
    public static String OUTPUT = "output"; 
    
    /**
     * The location of the Ptalon code.
     */
    public FileParameter ptalonCodeLocation;
    
        
    ///////////////////////////////////////////////////////////////////
    ////                        private methods                    ////
    
    /**
     * This is used to clean up any parameters, ports, relations,
     * and actors that have been created by an earlier Ptalon model.
     */
    private void _cleanUp() throws NameDuplicationException, IllegalActionException {
        Parameter p;
        for (int i= _parameters.size()-1; i >= 0; i--) {
            p = _parameters.remove(i);
            p.setContainer(null);
        }
        TypedIOPort port;
        for (int i= _ports.size()-1; i >= 0; i--) {
            port = _ports.remove(i);
            port.setContainer(null);
        }
        TypedIORelation r;
        for (int i= _relations.size()-1; i >= 0; i--) {
            r = _relations.remove(i);
            r.setContainer(null);
        }
        TypedAtomicActor a;
        for (int i= _actors.size()-1; i >= 0; i--) {
            a = _actors.remove(i);
            a.setContainer(null);
        }        
    }
    
    /**
     * Initialize the lists of ports, paramters,
     * relations, and actors of this actor.
     *
     */
    private void _initializeLists() {
        _actors = new ArrayList<TypedAtomicActor>();
        _relations = new ArrayList<TypedIORelation>();
        _parameters = new ArrayList<Parameter>();
        _ports = new ArrayList<TypedIOPort>();
        List entities = entityList();
        for (int i = 0; i < entities.size(); i++) {
            if (entities.get(i) instanceof TypedAtomicActor) {
                _actors.add((TypedAtomicActor) entities.get(i));
            }
        }
        List relations
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////
    
    /**
     * A list of the actors added to this actor when an
     * attribute is changed.
     */
    private ArrayList<TypedAtomicActor> _actors;

    /**
     * This string is used to make sure that the filename
     * actually changed before doing anything in attributeChanged.
     */
    private String _fileName;
    
    /**
     * A list of the relations added to this actor when an
     * attribute is changed.
     */
    private ArrayList<TypedIORelation> _relations;
    
    /**
     * A list of the parameters added to this actor when an
     * attribute is changed.
     */
    private ArrayList<Parameter> _parameters;

    /**
     * A list of the port added to this actor when an
     * attribute is changed.
     */
    private ArrayList<TypedIOPort> _ports;
    
   
}
