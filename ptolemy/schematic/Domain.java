/* The domain class in Ptolemy II.

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
@ProposedRating red (liuj@eecs.berkeley.edu)

*/

package ptolemy.schematic;

import java.io.*;
import ptolemy.actor.Director;
import ptolemy.actor.Actor;
import java.util.Enumeration;
import collections.HashedMap;

//////////////////////////////////////////////////////////////////////////
//// PtolemyDomain
/**
A domain is an aggregation of actor and directors. This class manages the
actor package list and the director list. This base class provide methods
that support creating actors, create directors, get available directors
etc. The derived class should provide a list of the director's full class
name. The actor
packages are added at run time through addActorPackage() method, which
is generally called by PtolemySystem.

@author  Jie Liu, Lukito Muliadi
@version $Id$
*/
public class Domain extends XMLElement {

    /** Construct a Ptolemy Domain with an empty string
     *  name.  At the time of construction it has no actor packages and
     *  no directors.
     */
    public Domain() {
        super("domain");
        _actorpackages = (HashedMap) new HashedMap();
        _directors = (HashedMap) new HashedMap();
        _description = new XMLElement("description");
        setName("");
    }

    /** Construct a Ptolemy Domain with the given attributes.
     *  At the time of construction it has no actor packages and no directors
     *
     *  @param attributes a HashedMap from attribute name to attribute value.
     */
    public Domain(HashedMap attributes) {
        super("domain",attributes);
        _actorpackages = (HashedMap) new HashedMap();
        _directors = (HashedMap) new HashedMap();
        _description = new XMLElement("description");
        if(!hasAttribute("name")) setName("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return an enumeration of the names of the actor packages.
     *  @return an Enumartion of String
     */
    public Enumeration actorPackageNames(){
        return _actorpackages.keys();
    }

   /**
     * Add an actor package for the this domain.   All the classes in this
     * java package are assumed to implement the Actor interface.
     * @param name A unique Identifier for this package.
     * @param package The full package name.
     */
    public void addActorPackage(String name, String packagename) {
        XMLElement e = new XMLElement("actorpackage");
        e.setAttribute("name", name);
        e.setAttribute("packagename", packagename);
        addChildElement(e);
        _actorpackages.putAt(name, e);
    }

    /**
     * Add a director to this domain.
     */
    public void addDirector(String name, String classname) {
        XMLElement e = new XMLElement("director");
        e.setAttribute("name", name);
        e.setAttribute("classname", classname);
        addChildElement(e);
        _directors.putAt(name, e);
    }

    /**
     * Test if this domain contains the given actorpackage.
     */
    public boolean containsActorPackage (String name) {
        return _actorpackages.includesKey(name);
    }

     /**
     * Test if this domain contains the given director.
     */
    public boolean containsDirector (String name) {
        return _directors.includesKey(name);
    }

   /**
     * Create a new actor in this domain that matches the given
     * actor class name. The actor class is searched through the
     * known actor packages. If no actor is found, then throw
     * a ClassNotFoundException.
     *
     * @param actorname The name of the actor.
     * @return The new actor.
     */
    public Actor createActor(String actorname) {
        return null;
    }

    /** Create a new director from the given director name.
     *  The name of a director is its full class name.
     *  If the director is not found, a ClassNotFoundException is thrown.
     *  @param directorname The director's full class name.
     *  @return The new Director.
     */
    public Director createDirector(String directorname) {
        return null;
    }

    /** Return the available Director names in an Enumeration of String.
     *  The name of a director is its full class name.
     *  @return The enumeration of director class names.
     */
    public Enumeration directorNames() {
        return _directors.keys();
    }

    /** Return the description of this domain.
     *  @return The description of this domain.
     */
    public String getDescription() {
        return _description.getPCData();
    }

    /** Return the name of the domain.
     *  @return The name of the domain.
     */
    public String getName() {
        return getAttribute("name");
    }

    /** Remove the specified actor package.
     *  @param actorpackage The requested actor package String.
     *  @exception IllegalActionException If the specified actor package
     *       is not in this domain.
     */
    public void removeActorPackage(String actorpackage) {
        XMLElement e = (XMLElement) _actorpackages.at(actorpackage);
        removeChildElement(e);
        _actorpackages.removeAt(actorpackage);
    }

    /**
     * set the Description of this domain
     */
    public void setDescription(String description) {
        _description.setPCData(description);
    }

    /**
     * set the name of this domain.
     */
    public void setName(String name) {
        setAttribute("name",name);
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The list of the director's full names
    private HashedMap _directors;
    // The list of actor packages
    private HashedMap _actorpackages;
    // The description of this domain
    private XMLElement _description;
}
