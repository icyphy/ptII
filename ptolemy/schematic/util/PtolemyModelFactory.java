/* A Factory to create a ptolemy model from a schematic.

 Copyright (c) 1998-1999 The Regents of the University of California.
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
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic.util;

import ptolemy.kernel.util.*;
import ptolemy.kernel.*;
import ptolemy.actor.*;
import java.util.*;
import collections.*;
import java.io.*;
import ptolemy.schematic.xml.*;
import ptolemy.data.expr.Parameter;
import ptolemy.data.*;
import java.net.*;
import java.lang.reflect.*;
//////////////////////////////////////////////////////////////////////////
//// PtolemyModelFactory
/**
An PtolemyModelFactory supports the creation of useful objects (IconLibraries, 
DomainLibraries, etc.) from XMLElements that represent the root elements
of the correspinding PTML file.

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class PtolemyModelFactory {
    /** 
     * Create a Ptolemy model using based on the given schematic.  
     * @return A composite actor.
     * @exception IllegalActionException If the schematic is not well formed.
     * @exception NameDuplicationException If the schematic, or an object
     * contained within the schematic contains two objects with the same name.
     */
    public TypedCompositeActor createPtolemyModel(Schematic schematic) 
            throws IllegalActionException, NameDuplicationException {
	System.out.println("Creating Ptolemy model for :\n" + schematic);
	
	String containerName = schematic.getName();
        System.out.println("name = " + containerName);
	TypedCompositeActor container = new TypedCompositeActor();
        container.setName(containerName);
	
	/*	Enumeration ports = schematic.ports();
	while(ports.hasMoreElements()) {
	    SchematicPort port = 
		(SchematicPort)ports.nextElement();
	    container.addPort(_createPtolemyPort(port));
	    }*/
        System.out.println("creating entities");
	Enumeration entities = schematic.entities();
	while(entities.hasMoreElements()) {
	    SchematicEntity entity = 
		(SchematicEntity)entities.nextElement();
	    _addPtolemyEntity(container, entity);
	}

	System.out.println("Creating relations");
	Enumeration relations = schematic.relations(); 
	while(relations.hasMoreElements()) { 
	    SchematicRelation relation =  
		(SchematicRelation)relations.nextElement(); 
	    _addPtolemyRelation(container, relation);
	} 
    
        System.out.println("creating parameters");
	_addParameters(container, schematic);

	Manager manager = new Manager("manager");
	container.setManager(manager);
	// FIXME get director from domain library.
	Director director = new ptolemy.domains.sdf.kernel.SDFDirector();
	container.setDirector(director);
	director.setName("director");
	Parameter iterations = (Parameter)director.getAttribute("iterations");
	iterations.setToken(new IntToken(3));

	return container;
    }

    private void _addParameters(NamedObj model, PTMLObject schematicObject)
            throws IllegalActionException, NameDuplicationException {
        Enumeration parameters = schematicObject.parameters();
        while(parameters.hasMoreElements()) {
            SchematicParameter parameter =
		(SchematicParameter)parameters.nextElement();
            // If a parameter with the given name already exists, then
            // use that, otherwise create a new parameter
            Attribute foundAttribute = 
                model.getAttribute(parameter.getName());
            Parameter modelParameter = null;
            if((foundAttribute != null) && 
                    (foundAttribute instanceof Parameter)) {
                modelParameter = (Parameter) foundAttribute;
            } else {
                modelParameter = 
                    new Parameter(model, parameter.getName());
            }
            
            // Create a token representing the value of the parameter.
            // Use reflection to get the Token class and its string 
            // constructor.
            Token valueToken = null;
            try {
                Class tokenClass = Class.forName(parameter.getType());
                Class formalArgs[] = new Class[1];
                formalArgs[0] = Class.forName("java.lang.String");
                Constructor tokenConstructor = 
                    tokenClass.getConstructor(formalArgs);
                Object actualArgs[] = new Object[1];
                actualArgs[0] = parameter.getValue();
                valueToken = 
                    (Token)tokenConstructor.newInstance(actualArgs);
            } catch (Exception ex) {
                throw new IllegalActionException(
                        "Error creating parameter value: " + ex);
            }
            
            modelParameter.setToken(valueToken);
        }
    }

    private void _addPtolemyEntity(TypedCompositeActor container, 
            SchematicEntity entity) 
            throws IllegalActionException, NameDuplicationException {
    
        String implementation = entity.getImplementation();
        if(implementation == null) {
            throw new IllegalActionException("entity cannot be " +
                    "instantiated, it has no implementation.");
        }
        System.out.println("Entity implementation = " + implementation);
        
        Entity newEntity = null;
        //FIXME add support for a schematic.
        //FIXME add heuristics for determining what the implementation is.
        //if(implementation is a class) {
        // implementation is a hierarchical class name.  Use reflection to
        // find the constructor and create the new actor.  
        // All actors MUST implement the two argument constructor that
        // takes a container and a name.
        try {
            Class entityClass = Class.forName(implementation);
            Class formalArgs[] = new Class[2];
            formalArgs[0] = Class.forName("ptolemy.actor.TypedCompositeActor");
            formalArgs[1] = Class.forName("java.lang.String");
            Constructor entityConstructor = 
                entityClass.getConstructor(formalArgs);
            Object actualArgs[] = new Object[2];
            actualArgs[0] = container;
            actualArgs[1] = entity.getName();
            newEntity = 
                (Entity)entityConstructor.newInstance(actualArgs);	    
        } catch (Exception ex) {
            throw new IllegalActionException("Error creating actor: " + ex);
        }
        //}	    
	entity.setSemanticObject(newEntity);
        _addParameters(newEntity, entity);
    }

    private void _addPtolemyRelation(TypedCompositeActor container, 
            SchematicRelation relation) 
	throws IllegalActionException, NameDuplicationException {
	TypedIORelation newRelation =
	    new TypedIORelation(container, relation.getName());
	Enumeration links = relation.links();
	while(links.hasMoreElements()) {
	    SchematicLink link = (SchematicLink) links.nextElement();
	    _addPtolemyLink(newRelation, link.getTo());
	    _addPtolemyLink(newRelation, link.getFrom());
	}
	relation.setSemanticObject(newRelation);
    }
 
    /** Find the port in the model that corresponds to the given terminal
     *  and link it to the relation.   This method assumes that the port
     *  already exists in the model.
     */
    private void _addPtolemyLink(TypedIORelation modelRelation, 
				 SchematicTerminal terminal) 
	throws IllegalActionException {
	Nameable terminalContainer = terminal.getContainer();	    
	if(terminalContainer instanceof SchematicRelation) {
	    //Ignore.  This is graphical info only.
	} else if(terminalContainer instanceof SchematicEntity) {
	    SchematicEntity entity = (SchematicEntity) terminalContainer;
            TerminalMap terminalMap = entity.getTerminalMap();
	    String portName =
		terminalMap.getPort(terminal.getName());
	    // crap...  how do you get the port.
	    Entity modelEntity = (Entity) entity.getSemanticObject();
	    TypedIOPort modelPort = 
		(TypedIOPort) modelEntity.getPort(portName);
	    modelPort.link(modelRelation);
	}
    }

//    private Port _createPtolemyPort(SchematicPort port) {
	

}

