/* The domain class in Ptolemy II.

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
@ProposedRating red (liuj@eecs.berkeley.edu)

*/

package ptolemy.system;

import java.io.*;
import ptolemy.actor.Director;
import ptolemy.actor.Actor;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PtolemyDomain
/**
A domain is an aggregation of actor and directors. This class manage the
actor package list and the director list. This base class provide methods
that support creating actors, create directors, get available directors
etc. The derived class should provide a list of the director's full class
name. The actor
packages are added at run time through addActorPackage() method, which
is generally called by PtolemySystem.

@author  Jie Liu, Lukito Muliadi
@version $Id$
*/
public class Domain {
    /** Construct a Ptolemy Domain with an empty string
     *  name.  At the time of construction it has no actors packages.
     */
    public Domain() {
        this(null);
    }

    /** Construct a Ptolemy Domain with a name. If the argument is null
     *  then the name of the director is an empty string.
     *  At the time of construction it has no actor packages.
     *  @param name The name of the domain.
     */
    public Domain(String name) {
        if(name == null) {
            _name = "";
        }else {
            _name = name;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add an actor package for the this domain.
     * @param actorpackage The full package name.
     */
    public void addActorPackage(String actorpackage) {
        // actorpackagelist.add(actirpackage)
    }

    /** Create a new actor in this domain that matches the given
     *  actor class name. The actor class is searched through the
     *  known actor packages. If no actor is found, then throw
     *  a ClassNotFoundException.
     *  @param actorname The name of the actor.
     *  @return The new actor.
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

    /** Return the actor packages in an array of String.
     *  @return the actor packages of this domain.
     */
    public String[] getActorPackages(){
        return null;
    }

    /** Return the available Director names in an Enumeration of String.
     *  The name of a director is its full class name.
     *  @return The enumeration of director class names.
     */
    public Enumeration getDirectorNames() {
        return null;
    }

    /** Return the description of this domain.
     *  @return The description of this domain.
     */
    public String getDescription() {
        return null;
    }

    /** Return the name of the domain.
     *  @return The name of the domain.
     */
    public String getName() {
        return _name;
    }

    /** Remove the specified actor package.
     *  @param actorpackage The requested actor package String.
     *  @exception IllegalActionException If the specified actor package
     *       is not in this domain.
     */
    public void removeActorPackage(String actorpackage) {
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // The name of the Domain.
    private String _name;
    // The list of the director's full names
    private LinkedList _directornames;
    // The list of actor packages
    private LinkedList _actorpackages;
}
