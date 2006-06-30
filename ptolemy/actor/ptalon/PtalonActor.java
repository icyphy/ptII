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
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedList;
import ptolemy.kernel.util.Settable;


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
     *  FIXME: There is an issue with persistence that has yet to be
     *  solved for this actor.  In particular, if I create an
     *  instance of this actor and then save it...
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
        _fileSet = false;
        _methods = new ParameterMethodList();
        _parameters = new ParameterValues();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
        
    
    /**
     * Add an actor with the given class name.  The class name should
     * include the full class path.  The actorName parameter may only
     * be a prefix of the generated actor.  This avoid name conflicts.
     * 
     * @param className The class name.
     * @param actorName The desired name for the actor.
     * @return The generated actor.
     * @throws IllegalActionExcepiton If it is generated in creating
     * the actor.
     */
    public ComponentEntity addActor(String className) throws IllegalActionException {
        try {
            StringTokenizer tokenizer = new StringTokenizer(className, "\"");
            className = tokenizer.nextToken();
            tokenizer = new StringTokenizer(className, ".");
            String actorName = "";
            while (tokenizer.hasMoreElements()) {
                actorName = tokenizer.nextToken();
            }
            String displayName = uniqueName(actorName);
            Object[] args = new Object[] {this, displayName};
            Constructor cons;
            Class actorClass = Class.forName(className); 
            Class[] argClasses = new Class[] {CompositeEntity.class, String.class};
            cons = actorClass.getConstructor(argClasses);
            ComponentEntity p = (ComponentEntity) cons.newInstance(args);
            List portList = p.portList();
            TypedIOPort atomicPort;
            TypedIOPort newPort;
            TypedIORelation r;
            for (int j = portList.size() - 1; j >= 0; j--) {
                if (!(portList.get(j) instanceof TypedIOPort)) {
                    continue;
                }
                atomicPort = (TypedIOPort) portList.get(j);
                String portName = p.getName() + "_" + atomicPort.getName();
                newPort = new TypedIOPort(this, portName);
                r = new TypedIORelation(this, uniqueName("relation"));
                if (atomicPort.isMultiport()) {
                    newPort.setMultiport(true);
                    r.setWidth(0);
                }
                newPort.setTypeEquals(atomicPort.getType());
                atomicPort.link(r);
                newPort.link(r);
            }
            List attributeList = p.attributeList();
            Parameter atomicParam;
            Parameter newParam;
            for (int j = 0; j < attributeList.size(); j++){
                if (!(attributeList.get(j) instanceof Parameter)) {
                    continue;
                }
                atomicParam = (Parameter) attributeList.get(j);
                String paramName = p.getName() + "_" + atomicParam.getName();
                newParam = new Parameter(this, paramName);
                newParam.setExpression(atomicParam.getExpression());
                atomicParam.setExpression(newParam.getName());
            }
            return p;
        } catch(Exception e) {
            if (!(e instanceof IllegalActionException)) {
                throw new IllegalActionException(e.getMessage());
            } else {
                throw (IllegalActionException) e;
            }
        }
    }
    
    /**
     * Return the addActor method as a Method object.
     * @return The addActor method.
     */
    public static Method addActorMethod() {
        try {
            Class thisClass = Class.forName("ptolemy.actor.ptalon");
            Class stringClass = Class.forName("java.lang.String");
            Method addActorMethod = thisClass.getMethod("addActor", stringClass);
        } catch (Exception e) {
        }
        return null;
    }
    
    /**
     * Add an atomic actor to this actor with the specified className.
     * If the className is in use, this will do nothing and return null.
     * otherwise, it will return the generated TypedAtomicActor.
     * @param className The class of the actor.
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
                if (atomicPort.isMultiport()) {
                    newPort.setMultiport(true);
                    r.setWidth(0);
                }
                atomicPort.link(r);
                newPort.link(r);
            }
            List attributeList = p.attributeList();
            Parameter atomicParam;
            Parameter newParam;
            for (int j = 0; j < attributeList.size(); j++){
                if (!(attributeList.get(j) instanceof Parameter)) {
                    continue;
                }
                atomicParam = (Parameter) attributeList.get(j);
                newParam = new Parameter(this, atomicParam.getName());
                newParam.setExpression(atomicParam.getExpression());
                atomicParam.setExpression(newParam.getName());
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
            return p;
        } catch(NameDuplicationException e) {
            //Do nothing.  Just give up.
        }
        return null;
    }
    
    /**
     * Add a parameter and an associated method to this Ptalon actor.
     * @param name The name of the parameter.
     * @param method The method to call for this parameter.
     */
    public void addParameterMethod(String name, Method method) {
        _methods.addPair(name, method);
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
            return p;
        } catch(NameDuplicationException e) {
            //Do nothing, just give up.
        }
        return null;
    }

    /** React to a change in an attribute.  This method is called by
     *  a contained attribute when its value changes.  This initally responds
     *  to changes in the <i>ptalonCode</i> parameter.  Later it responds
     *  to changes in parameters specified in the Ptalon code itself.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    public void attributeChanged(Attribute att) throws IllegalActionException {
        if (att == ptalonCodeLocation) {
            File inputFile = ptalonCodeLocation.asFile();
            if ((inputFile == null) || (_fileSet))  {
                return;
            }
            PtalonLexer lex;
            PtalonRecognizer rec;
            try {
                FileReader reader = new FileReader(inputFile);
                lex = new PtalonLexer(reader);
                rec = new PtalonRecognizer(lex);
                rec.actor_definition(this);
                ptalonCodeLocation.setExpression("");
                ptalonCodeLocation.setContainer(null);
            } catch (Exception e) {
                throw new IllegalActionException(e.getMessage());
            }
        } else if ((att instanceof Parameter) && !(att instanceof FileParameter)) {
            try {
                String expression = ((Parameter) att).getExpression();
                String name;
                if (expression.equals("")) {
                    return;
                }
                for (int i=0; i < _methods.size(); i++) {
                    name = att.getName();
                    if (!(expression.equals(_parameters.getValue(name)))) {
                        Method method = _methods.getMethod(name);
                        if (method != null) {
                            method.invoke(this, expression);
                            _parameters.setValue(name, expression);
                            ((Parameter)att).setVisibility(Settable.NONE);
                            return;
                        }
                    }
                }
            } catch(Exception e) {
                if (e instanceof IllegalActionException) {
                    throw (IllegalActionException) e;
                }
                else {
                    throw new IllegalActionException(e.getMessage());
                }
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
    ////                       private variables                   ////
    
    /**
     * True if the file has already been set.
     */
    private boolean _fileSet;
    
    private ParameterMethodList _methods;
    
    private ParameterValues _parameters;

}
