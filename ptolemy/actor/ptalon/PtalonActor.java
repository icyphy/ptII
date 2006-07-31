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

import antlr.CommonAST;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Constructor;
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
        _ptalonParameters = new ArrayList<PtalonParameter>();
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
        
    
    /**
     * Add an actor with the class name specified in the given 
     * PtalonParameter.  This is given as the only element of an
     * array to be used with the flexible executeMethods method
     * of PtalonParameter, where it will be called.
     * The class name should include the full class path.  The actorName parameter may only
     * be a prefix of the generated actor.  This avoid name conflicts.
     * 
     * @param paramter A length 1 array of parameters
     * @throws IllegalActionExcepiton If it is generated in creating
     * the actor.
     */
    public void addActor(PtalonObject[] param) throws IllegalActionException {
        if (param.length != 1) {
            throw new IllegalActionException(
                    "addActor expects a length one array of parameters.");
        }
        try {
            PtalonParameter parameter = (PtalonParameter)param[0];
            String className = parameter.stringValue(); 
            StringTokenizer tokenizer = new StringTokenizer(className, ".");
            String actorName = "";
            while (tokenizer.hasMoreElements()) {
                actorName = tokenizer.nextToken();
            }
            String displayName = uniqueName(actorName);
            parameter.setActorName(displayName);
            Object[] args = new Object[] {this, displayName};
            Constructor cons;
            Class actorClass = Class.forName(className); 
            Class[] argClasses = new Class[] {CompositeEntity.class, String.class};
            cons = actorClass.getConstructor(argClasses);
            ComponentEntity p = (ComponentEntity) cons.newInstance(args);
//            List portList = p.portList();
//            TypedIOPort atomicPort;
//            PtalonPort newPort;
//            TypedIORelation r;
//            for (int j = portList.size() - 1; j >= 0; j--) {
//                if (!(portList.get(j) instanceof TypedIOPort)) {
//                    continue;
//                }
//                atomicPort = (TypedIOPort) portList.get(j);
//                String portName = p.getName() + "_" + atomicPort.getName();
//                newPort = new PtalonPort(this, portName);
//                r = new TypedIORelation(this, uniqueName("relation"));
//                if (atomicPort.isMultiport()) {
//                    newPort.setMultiport(true);
//                    r.setWidth(0);
//                }
//                newPort.setTypeEquals(atomicPort.getType());
//                atomicPort.link(r);
//                newPort.link(r);
//            }
//            List attributeList = p.attributeList();
//            Parameter atomicParam;
//            Parameter newParam;
//            for (int j = 0; j < attributeList.size(); j++){
//                if (!(attributeList.get(j) instanceof Parameter)) {
//                    continue;
//                }
//                atomicParam = (Parameter) attributeList.get(j);
//                String paramName = p.getName() + "_" + atomicParam.getName();
//                newParam = new Parameter(this, paramName);
//                newParam.setExpression(atomicParam.getExpression());
//                atomicParam.setExpression(newParam.getName());
//            }
        } catch(Exception e) {
            if (!(e instanceof IllegalActionException)) {
                throw new IllegalActionException(this, e, e.getMessage());
            } else {
                throw (IllegalActionException) e;
            }
        }
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
            PtalonPort newPort;
            TypedIORelation r;
            for (int j = portList.size() - 1; j >= 0; j--) {
                if (!(portList.get(j) instanceof TypedIOPort)) {
                    continue;
                }
                atomicPort = (TypedIOPort) portList.get(j);
                newPort = new PtalonPort(this, atomicPort.getName()); 
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
            throw new IllegalActionException(this, e, e.getMessage());
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
    public PtalonParameter addParameter(String name) throws IllegalActionException {
        try {
            PtalonParameter p = new PtalonParameter(this, name);
            return p;
        } catch(NameDuplicationException e) {
            //Do nothing.  Just give up.
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
    public PtalonPort addPort(String name, String flow) throws IllegalActionException {
        try {
            PtalonPort p;
            if (flow.equals(INPUT)) {
                p = new PtalonPort(this, name, true, false);
            } else if (flow.equals(OUTPUT)) {
                p = new PtalonPort(this, name, false, true);
            } else {
                p = new PtalonPort(this, name, true, true);
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
    public PtalonRelation addRelation(String name) throws IllegalActionException {
        try {
            PtalonRelation p = new PtalonRelation(this, name);
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
            _initializePtalonCodeLocation();
        } else if (att instanceof PtalonParameter) {
            _respondToParameterUpdate((PtalonParameter) att);
        } else {
            super.attributeChanged(att);
        }
    }
    

    /**
     * Connect loose ports for an actor with the class name specified
     * in the given PtalonParameter.  This is given as the only element of an
     * array to be used with the flexible executeMethods method
     * of PtalonParameter, where it will be called.
     * The class name should include the full class path.  The actorName parameter may only
     * be a prefix of the generated actor.  This avoid name conflicts.
     * 
     * @param paramter A length 1 array of parameters
     * @throws IllegalActionExcepiton If it is generated in connecting
     * the ports.
     */
    public void connectLoosePorts(PtalonObject[] param) throws IllegalActionException {
        if (param.length != 1) {
            throw new IllegalActionException(
                    "addActor expects a length one array of parameters.");
        }
        try {
            PtalonParameter parameter = (PtalonParameter)param[0];
            String displayName = parameter.getActorName();
            Object[] args = new Object[] {this, displayName};
            ComponentEntity p = getEntity(parameter.getActorName());
            List portList = p.portList();
            TypedIOPort port, newPort;
            TypedIORelation r;
            List relations;
            for (Object obj : portList) {
                if (!(obj instanceof TypedIOPort)) {
                    continue;
                }
                port = (TypedIOPort) obj;
                relations = port.linkedRelationList();
                if (relations.isEmpty()) {
                  String portName = p.getName() + "_" + port.getName();
                  newPort = new PtalonPort(this, portName);
                  r = new TypedIORelation(this, uniqueName("relation"));
                  if (port.isMultiport()) {
                      newPort.setMultiport(true);
                      r.setWidth(0);
                  }
                  newPort.setTypeEquals(port.getType());
                  port.link(r);
                  newPort.link(r);                    
                }
            }
        } catch(Exception e) {
            if (!(e instanceof IllegalActionException)) {
                throw new IllegalActionException(this, e, e.getMessage());
            } else {
                throw (IllegalActionException) e;
            }
        }
    }

    /**
     * Connect two PtalonPorts together.
     * @param ports A length three array consisting of
     * 1) A PtalonParameter in contained in this composite, corresponding to an actor.
     * 2) A PtalonParamter not in this class, whose value is a port name for the contained actor.
     * 3) A PtalonPort contained in this actor to connect two.
     * @throws IllegalActionException If it is generated in creating
     * the actor.
     */
    public void connectPorts(PtalonObject[] ports) throws IllegalActionException {
        try {
            TypedIORelation r = new TypedIORelation(this, uniqueName("relation"));
            PtalonParameter actorParam = (PtalonParameter) ports[0];
            PtalonParameter portParam = (PtalonParameter) ports[1];
            ComponentEntity actor = getEntity(actorParam.getActorName());
            TypedIOPort p = (TypedIOPort) actor.getPort(portParam.getExpression());
            PtalonPort q = (PtalonPort) ports[2];
            p.link(r);
            q.link(r);
        } catch(IllegalActionException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }        
    }
    
    /**
     * Connect a PtalonPort and a PtalonRelation.
     * @param ports A length three array consisting of
     * 1) A PtalonParameter in contained in this composite, corresponding to an actor.
     * 2) A PtalonParamter not in this class, whose value is a port name for the contained actor.
     * 3) A PtalonRelation contained in this actor to connect two.
     * @throws IllegalActionException If it is generated in creating
     * the actor.
     */
    public void linkToRelation(PtalonObject[] ports) throws IllegalActionException {
        try {
            PtalonParameter actorParam = (PtalonParameter) ports[0];
            PtalonParameter portParam = (PtalonParameter) ports[1];
            ComponentEntity actor = getEntity(actorParam.getActorName());
            TypedIOPort p = (TypedIOPort) actor.getPort(portParam.getExpression());
            PtalonRelation r = (PtalonRelation) ports[2];
            p.link(r);
        } catch(IllegalActionException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }        
    }
    
    /**
     * Propagage parameters for an actor with the class name specified
     * in the given PtalonParameter up to this actor.  This is given as the only element of an
     * array to be used with the flexible executeMethods method
     * of PtalonParameter, where it will be called.
     * The class name should include the full class path.  The actorName parameter may only
     * be a prefix of the generated actor.  This avoid name conflicts.
     * 
     * @param paramter A length 1 array of parameters
     * @throws IllegalActionExcepiton If it is generated in connecting
     * the parameters.
     */
    public void propagateParameters(PtalonObject[] param) throws IllegalActionException {
        if (param.length != 1) {
            throw new IllegalActionException(
                    "addActor expects a length one array of parameters.");
        }
        try {
            PtalonParameter parameter = (PtalonParameter)param[0];
            String displayName = parameter.getActorName();
            Object[] args = new Object[] {this, displayName};
            ComponentEntity p = getEntity(parameter.getActorName());
            List portList = p.portList();
            List attributeList = p.attributeList();
            Parameter atomicParam;
            Parameter newParam;
            for (int j = 0; j < attributeList.size(); j++){
                if (!(attributeList.get(j) instanceof Parameter)) {
                    continue;
                }
                atomicParam = (Parameter) attributeList.get(j);
                if (atomicParam.getVisibility().equals(Settable.NONE)) {
                    continue;
                }
                String paramName = p.getName() + "_" + atomicParam.getName();
                newParam = new Parameter(this, paramName);
                newParam.setExpression(atomicParam.getExpression());
                atomicParam.setExpression(newParam.getName());
            }
        } catch(Exception e) {
            if (!(e instanceof IllegalActionException)) {
                throw new IllegalActionException(this, e, e.getMessage());
            } else {
                throw (IllegalActionException) e;
            }
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
     * Collect all the PtalonParameters created into the
     * _ptalonParameters list.
     */
    private void _collectPtalonParameters() {
        List attributes = attributeList();
        Attribute attribute;
        PtalonParameter parameter;
        for (int i = 0; i < attributes.size(); i++) {
            attribute = (Attribute) attributes.get(i);
            if (attribute instanceof PtalonParameter) {
                parameter = (PtalonParameter) attribute;
                if (!parameter.getVisibility().equals(Settable.NONE)) {
                    _ptalonParameters.add(parameter);
                }
            }
        }
    }
    
    /**
     * This helper method is used to begin the Ptalon compiler
     * if the ptalonCodeLocation attribute has been updated.
     * @throws IllegalActionException If any exception is thrown.
     */
    private void _initializePtalonCodeLocation() throws IllegalActionException {
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
            rec.actor_definition();
            CommonAST ast = (CommonAST) rec.getAST();
            PtalonWalker walker = new PtalonWalker();
            walker.actor_definition(ast, this);
            ptalonCodeLocation.setExpression("");
            ptalonCodeLocation.setContainer(null);
            _collectPtalonParameters();
        } catch (Exception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }
    }
    
    /**
     * Invoke the methods that have been associated with each
     * Ptalon actor.
     * @throws IllegalActionException If there is any problem
     * invoking one of the specified mehtods.
     */
    private void _invokeMethods() throws IllegalActionException {
        for (int i = 0; i < _ptalonParameters.size(); i++) {
            _ptalonParameters.get(i).executeMethods(this);
            _ptalonParameters.get(i).setVisibility(Settable.NONE);
        }
    }
    
    /**
     * Returns true if all the PtalonParamters have values and
     * are therefore ready to have their methods called.  Their
     * methods will populate this actor with a hierarchical network of
     * interconnected actors.
     * @return True when all parameters are ready. 
     */
    private boolean _readyToCallMethods() {
        PtalonParameter p;
        for (int i = 0; i < _ptalonParameters.size(); i++) {
            p = _ptalonParameters.get(i);
            if (!(p.hasValue())) {
                return false;
            } else if (p.getVisibility().equals(Settable.NONE)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * This helper method is used to process the update of a
     * Ptalon parameter.
     * @param att The parameter to process.
     * @throws IllegalActionException If any exception is called
     * in trying to execute methods corresponding to a PtalonParameter.
     */
    private void _respondToParameterUpdate(PtalonParameter att) throws IllegalActionException {
        try {
            String expression = att.getExpression();
            if (expression.equals("")) {
                return;
            }
            att.setHasValue(true);
            if (_readyToCallMethods()) {
                _invokeMethods();
            }
        } catch (Exception e) {
            throw new IllegalActionException(this, e, e.getMessage());
        }
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                       private variables                   ////
    
    /**
     * True if the file has already been set.
     */
    private boolean _fileSet;
    
    /**
     * List of all PtalonParameters associated with this actor.
     */
    private ArrayList<PtalonParameter> _ptalonParameters;
    

}
