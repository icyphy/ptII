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
    public CompositeEntity createPtolemyModel(Schematic schematic) 
            throws IllegalActionException, NameDuplicationException {
	String containerName = schematic.getName();
	CompositeActor container = new CompositeActor(null, containerName);
	
	/*	Enumeration ports = schematic.ports();
	while(ports.hasMoreElements()) {
	    SchematicPort port = 
		(SchematicPort)ports.nextElement();
	    container.addPort(_createPtolemyPort(port));
	    }*/

	Enumeration entities = schematic.entities();
	while(entities.hasMoreElements()) {
	    SchematicEntity entity = 
		(SchematicEntity)entities.nextElement();
	    _addPtolemyEntity(container, entity);
	}
    /*
	Enumeration relations = schematic.relations();
	while(relations.hasMoreElements()) {
	    SchematicRelation relation = 
		(SchematicRelation)relations.nextElement();
	    container.addRelation(_createPtolemyRelation(relation));
	}
    */
	_addParameters(container, schematic);
	return container;
    }

    private void _addParameters(NamedObj model, PTMLObject schematicObject) {
	try {
	    Enumeration parameters = schematicObject.parameters();
	    while(parameters.hasMoreElements()) {
		SchematicParameter parameter =
		(SchematicParameter)parameters.nextElement();
		Parameter modelParameter = 
		new Parameter(model, parameter.getName());
		Token value = new DoubleToken(parameter.getValue());
		modelParameter.setToken(value);
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private void _addPtolemyEntity(CompositeEntity container, 
				   SchematicEntity entity) {
	try {
	    Class formalArgs[] = new Class[2];
	    formalArgs[0] = Class.forName("ptolemy.kernel.CompositeEntity");
	    formalArgs[1] = Class.forName("java.lang.String");
	    
	    String implementation = entity.getImplementation();
	    
	    //FIXME add support for a schematic.
	    System.out.println("Entity implementation = " + implementation);
	    //	ClassLoader loader = new ClassLoader();
	    Class entityClass = Class.forName(implementation);//loader.loadClass(implementation);
	    Constructor entityConstructor = 
		entityClass.getConstructor(formalArgs);
	    Object actualArgs[] = new Object[2];
	    actualArgs[0] = container;
	    actualArgs[1] = entity.getName();
	    Entity newEntity = 
		(Entity)entityConstructor.newInstance(actualArgs);
	    
	    System.out.println("entity = " + newEntity.description());
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

//    private Port _createPtolemyPort(SchematicPort port) {
	

}

