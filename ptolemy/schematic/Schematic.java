/* A Schematic represents a PtII design

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.schematic;

import java.util.Enumeration;
import collections.HashedMap;

//////////////////////////////////////////////////////////////////////////
//// Schematic
/**

A schematic represents a PtolemyII schematic. Schematic objects
are constructed from (and written to) XML files by an instance
of SchematicParser. A schematic objects contains information
about entities, relations, and parameters, in a form that gives
the client enough information about the system to construct
and run a simulation, or to display and edit the visual schematic
(with the help of other classes such as Icon and IconLibrary).


@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class Schematic extends SchematicElement {

    /** 
     * Create a new Schematic object.
     */
    public Schematic () {
        super("schematic");
        entities = (HashedMap) new HashedMap();
        relations = (HashedMap) new HashedMap();
        description = new XMLElement("description");
        addChildElement(description);
        setVersion("");
   }

    /** 
     * Create a new Schematic object with the specified attributes
     */
    public Schematic (HashedMap attributes) {
        super("schematic", attributes);
        entities = (HashedMap) new HashedMap();
        relations = (HashedMap) new HashedMap();
        description = new XMLElement("description");
        addChildElement(description);
        if(!hasAttribute("version")) setVersion("");
   }

    /**
     * Add a new entity to this schematic. The name
     * of the entity must be unique in this schematic.
     */
    public void addEntity (SchematicEntity entity) {
        entities.putAt(entity.getName(),entity);
        addChildElement(entity);
    }

    /**
     * Add a new relation to this schematic. The name
     * of the relation must be unique in this schematic.
     */
    public void addRelation (SchematicRelation relation) {
        relations.putAt(relation.getName(),relation);
        addChildElement(relation);
    }

    /**
     * Test if there is an entity with the given name in the
     * schematic.
     */
    public boolean containsEntity (String name) {
        return entities.includesKey(name);
    }

    /**
     * Test if there is an relation with the given name in the
     * schematic.
     */
    public boolean containsRelation (String name) {
        return relations.includesKey(name);
    }

     /**
     * Return an enumeration over the entities in this
     * schematic.
     */
    public Enumeration entityNames () {
        return entities.keys();
    }

    /** 
     * Return a long description string of this Schematic.
     */
    public String getDescription() {
        return description.getPCData();
    }

    /**
     * Return the schematic entity that has the given name.
     * Throw an exception if there is no entity with the
     * given name in this schematic.
     */
    public SchematicEntity getEntity (String name) {
        return (SchematicEntity) entities.at(name);
    }

     /**
     * Return the schematic relation that has the given name.
     * Throw an exception if there is no relation with the
     * given name in this schematic.
     */
    public SchematicRelation getRelation (String name) {
        return (SchematicRelation) relations.at(name);
    }

    /** Return the version of this schematic.
     */
    public String getVersion() {
        return getAttribute("version");
    }

    /**
     * Return an enumeration over the relations in this
     * schematic.
     */
    public Enumeration relationNames () {
        return relations.keys();
    }

     /**
     * Remove the schemtic entity with the given name.
     * Throw an exception if the entity does not exist
     * in this schematic.
     */
    public void removeEntity(String name) {
        SchematicEntity e = (SchematicEntity) entities.at(name);
        removeChildElement(e);
        entities.removeAt(name);
    }

    /**
     * Remove the schematic relation with the given name.
     * Throw an exception if the relation does not exist
     * in this schematic.
     */
    public void removeRelation(String name) {
        SchematicRelation e = (SchematicRelation) relations.at(name);
        removeChildElement(e);
        entities.removeAt(name);
    }

    /** 
     * Set the string that contains the long description of this library.
     */
    public void setDescription(String s) {
        description.setPCData(s);
    }   

    /** Set the string that represents the version of this schematic.
     */
    public void setVersion(String s) {
        setAttribute("version",s);
    }

    /**
     * Take an arbitrary XMLElement and figure out what type it is, then
     * figure out what semantic meaning that has within this XMLElement.
     * This is primarily used by the parser to keep the semantic structures
     * within an XMLElement consistant with the childElements.
     */
    void applySemanticsToChild(XMLElement e) {
        if(e instanceof SchematicEntity) {
            // if it's a Port, then just add it to the list of ports.
            entities.putAt(
                    ((SchematicPort) e).getName(), e);
        } else if(e instanceof SchematicRelation) {
            // if a relation, remove the old one and install the new one.
            relations.putAt(
                    ((SchematicRelation) e).getName(), e);
        } else if(e instanceof SchematicParameter) {
            // if a parameter, remove the old one and install the new one.
            parameters.putAt(
                    ((SchematicParameter) e).getName(), e);
        } else if(e.getElementType().equals("description")) {
            // if a description, remove the old one and install the new one.
            removeChildElement(description);
            description = e;
        }
    }

    protected XMLElement description;
    protected HashedMap entities;
    protected HashedMap relations;

}

