/* An EntityLibrary stores Entity templates.

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
import java.util.Enumeration;
import collections.*;
import java.io.*;

//////////////////////////////////////////////////////////////////////////
//// EntityLibrary
/**
An EntityLibrary is the hierarchical object for organizing Entity templates. 
An EntityLibrary contains a set of entity templates, and a set of other
EntityLibraries called sublibraries.  

@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class EntityLibrary extends PTMLObject {

    /** 
     * Create an EntityLibrary object with the name "Entitylibrary".
     * The library will have an empty string for the description and version.
     */
    public EntityLibrary() {
        this("EntityLibrary");
    }

    /** 
     * Create an EntityLibrary object with the given name.
     * The library will have an empty string for the description and version.
     */
    public EntityLibrary(String name) {
        super(name);
        _sublibraries = (NamedList) new NamedList();
        _entities = (NamedList) new NamedList();
   }

    /**
     * Add an Entity to this library
     *  @exception IllegalActionException If the entity has no name.
     *  @exception NameDuplicationException If the name of the entity
     *  coincides with the name of another entity contained in this library.
     */
    public void addEntity(EntityTemplate e)
        throws IllegalActionException, NameDuplicationException {
        _entities.append(e);
    }

    /**
     * Add a sublibrary to this library.
     *  @exception IllegalActionException If the sublibrary has no name.
     *  @exception NameDuplicationException If the name of the sublibrary
     *  coincides with the name of another sublibrary
     *  contained in this library.
     */
    public void addSubLibrary(EntityLibrary library)
        throws IllegalActionException, NameDuplicationException {
        _sublibraries.append(library);
    }

    /**
     * Test if the library contains an Entity with the given name
     */
    public boolean containsEntity(EntityTemplate entity) {
        return _entities.includes(entity);
    }

    /**
     * Test if the library contains the sublibrary
     */
    public boolean containsSubLibrary(EntityLibrary lib) {
        return _sublibraries.includes(lib);
    }

    /**
     * Get the Entity that is stored in this EntityLibrary with the specified
     * type signature
     */
    // public Entity getEntity(EntityType e) {
    //    return (Entity) _Entities.at(e);
    //}
    //    public Entity getEntity(String s) {
    //    return (Entity) _Entities.at(s);
    // }

    /** 
     * return the URL of the given sublibrary.
     */
    //public EntityLibrary getSubLibrary(String name) {
    //    return (EntityLibrary) _sublibraries.at(name);
    //}

    /** Return the version of this library.
     */
    //public String getVersion() {
    //    return _version;
    //}

    /**
     * Return the Entitys that are contained in this Entity library.
     *
     * @return an enumeration of Entity
     */
    public Enumeration entities() {
        return _entities.elements();
    }

   /**
     * Remove an Entity from this EntityLibrary
     */
    public void removeEntity(EntityTemplate entity) {
        _sublibraries.remove(entity);
    }

    /**
     * Remove a sublibrary from this EntityLibrary
     */
    public void removeSubLibrary(EntityLibrary lib) {
        _sublibraries.remove(lib);
    }

    /** Set the string that represents the version of this library.
     */
    //public void setVersion(String s) {
    //    _version = s;
    //}

    /**
     * Return the names of subLibraries of this EntityLibrary.   These names
     * are URL Strings which can be passed to other EntityLibrary objects
     * for parsing.
     * @return an Enumeration of Strings
     */
    public Enumeration subLibraries() {
        return _sublibraries.elements();
    }

    /**
     * Return a string this representing Entity.
     */
    public String toString() {
        Enumeration els = subLibraries();
        String str = getName() + "({";
        while(els.hasMoreElements()) {
            EntityLibrary il = (EntityLibrary) els.nextElement();
	    str += il.toString();
        }
        str += "}{";
        els = entities();
         while(els.hasMoreElements()) {
            EntityTemplate entity = (EntityTemplate) els.nextElement();
	    str += "\n" + entity.toString();
        }        
        return str + "})";
    }

    private NamedList _sublibraries;
    private NamedList _entities;
}

