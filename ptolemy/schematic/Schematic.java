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
import collections.LinkedList;

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
        ;
    }

    /**
     * Add a new entity to this schematic. The name
     * of the entity must be unique in this schematic.
     */
    public void addEntity (SchematicEntity entity) {
        ;
    }

    /**
     * Add a new relation to this schematic. The name
     * of the relation must be unique in this schematic.
     */
    public void addRelation (SchematicRelation relation) {
        ;
    }

    /**
     * Test if there is an entity with the given name in the
     * schematic.
     */
    public boolean containsEntity (String name) {
        return false;
    }

    /**
     * Test if there is an relation with the given name in the
     * schematic.
     */
    public boolean containsRelation (String name) {
        return false;
    }

     /**
     * Return an enumeration over the entities in this
     * schematic.
     */
    public Enumeration entities () {
        return null;
    }

    /**
     * Return the schematic entity that has the given name.
     * Throw an exception if there is no entity with the
     * given name in this schematic.
     */
    public SchematicEntity getEntity (String name) {
        return null;
    }

     /**
     * Return the schematic relation that has the given name.
     * Throw an exception if there is no relation with the
     * given name in this schematic.
     */
    public SchematicRelation getRelation (String name) {
        return null;
    }

    /**
     * Return an enumeration over the relations in this
     * schematic.
     */
    public Enumeration relations () {
        return null;
    }

     /**
     * Remove the schemtic entity with the given name.
     * Throw an exception if the entity does not exist
     * in this schematic.
     */
    public void removeEntity(String name) {
        ;
    }

    /**
     * Remove the schematic relation with the given name.
     * Throw an exception if the relation does not exist
     * in this schematic.
     */
    public void removeRelation(String name) {
        ;
    }
}

