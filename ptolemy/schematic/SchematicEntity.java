/* A SchematicEntity is an entity stored in a schematic

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
//// SchematicEntity
/**

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicEntity extends SchematicElement { 

    /** 
     * Create a new SchematicEntity object wtih no set attributes.
     */
    public SchematicEntity () {
        super("entity");
        entitytype = new EntityType();
        addChildElement(entitytype);
    }

    /**
     * Create a new SchematicEntity object with the given attributes and an 
     * unspecified entitytype.
     *
     * @param attributes a HashedMap from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public SchematicEntity (HashedMap attributes) {
        super("entity", attributes);
        entitytype = new EntityType();
        addChildElement(entitytype);
    }

    /**
     * Create a new SchematicEntity object with the given attributes and the
     * specified entity type.
     *
     * @param attributes a HashedMap from a String specifying the name of
     * an attribute to a String specifying the attribute's value.
     */
    public SchematicEntity (HashedMap attributes, EntityType et) {
        super("entity", attributes);
        entitytype = et;
        addChildElement(entitytype);
    }

    /**
     * Get the object that describes this entity. This object
     * can be used to instantiate an actor given certain
     * other information (such as the domain) or to look up
     * icons in an icon library.
     */
    public EntityType getEntityType () {
        return entitytype;
    }

    EntityType entitytype;
}

