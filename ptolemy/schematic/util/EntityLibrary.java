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
import java.util.StringTokenizer;
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
     * Search for an entity template with the given hierarchical name in 
     * this library and all its deeply contained sublibraries.
     * @return The found entity template
     * @exception IllegalActionException If no entity template with the given
     * name is found.
     */
    public EntityTemplate findEntityTemplate(String dottedName)
            throws IllegalActionException {
        StringTokenizer tokens = new StringTokenizer(dottedName, ".");
        EntityLibrary temp = this;
        int count = tokens.countTokens();
        
        int i;
        for(i = 0; i < (count - 1); i++) 
            temp = temp.getSubLibrary((String) (tokens.nextElement()));
        
        return temp.getEntity((String) (tokens.nextElement()));
    }

    /**
     * Get the Entity that is stored directly 
     * in this EntityLibrary with the given name.
     */
    public EntityTemplate getEntity(String name)
            throws IllegalActionException {
        Enumeration allEntities = entities();
        while(allEntities.hasMoreElements()) {
            EntityTemplate entity = (EntityTemplate) allEntities.nextElement();
            if(name.equals(entity.getName()))
                return entity;
        }
        throw new IllegalActionException("Entity does not exist with " +
                "the name " + name);
    }
    
    /**
     * Get the Entity that is stored in this EntityLibrary with the specified
     * type signature
     */
    public EntityLibrary getSubLibrary(String name) 
        throws IllegalActionException {
        Enumeration allLibraries = subLibraries();
        while(allLibraries.hasMoreElements()) {
            EntityLibrary library = (EntityLibrary) allLibraries.nextElement();
            if(name.equals(library.getName()))
                return library;
        }
        throw new IllegalActionException(
                "Entity library does not exist with " +
                "the name " + name);
    }

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

    /** Return a description of the object.  Lines are indented according to
     *  to the level argument using the protected method _getIndentPrefix().
     *  Zero, one or two brackets can be specified to surround the returned
     *  description.  If one is specified it is the the leading bracket.
     *  This is used by derived classes that will append to the description.
     *  Those derived classes are responsible for the closing bracket.
     *  An argument other than 0, 1, or 2 is taken to be equivalent to 0.
     *  This method is read-synchronized on the workspace.
     *  @param indent The amount of indenting.
     *  @param bracket The number of surrounding brackets (0, 1, or 2).
     *  @return A description of the object.
     */
    protected String _description(int indent, int bracket) {
        String result = "";
        if(bracket == 0) 
            result += super._description(indent, 0);
        else 
            result += super._description(indent, 1);
	result += " sublibraries {\n";
	Enumeration sublibraries = subLibraries();
        while (sublibraries.hasMoreElements()) {
            EntityLibrary p = (EntityLibrary) sublibraries.nextElement();
            result += p._description(indent + 1, 2);
        }
	result += _getIndentPrefix(indent) + "} entites {\n";
	Enumeration entities = entities();
        while (entities.hasMoreElements()) {
            EntityTemplate p = (EntityTemplate) entities.nextElement();
            result += p._description(indent + 1, 2) + "\n";
        }
        result += _getIndentPrefix(indent) + "}";
        if (bracket == 2) result += "}";
        return result;
    }

    private NamedList _sublibraries;
    private NamedList _entities;
}

