/* This class manages the Ptoelmy II domains and actor packages .

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
*/

package ptolemy.system;

import java.io.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.actor.Director;
import ptolemy.actor.Actor;
import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// PtolemySystem
/**
This class provides information about the Ptolemy system.
The User Interface can use this information for creating and displaying
domains and actors. A "configuration" can be read from a default place
or an input stream, and saved to the default place or an output stream.
The configuration has the grammar:
<P>
configuration: domain_definitions<BR>
domain_definitions: domain_definition
                  | domain_definitions domain_definition<BR>
domain_definition: domain_classname
                 | domain_classname actor_packages<BR>
domain_classname: <B>Domain</B> <I>classname</I><BR>
actor_packages: actor_package
              | actor_packages actor_package<BR>
actor_package: <B>ActorPackage</B> <I>package</I>
<P>

The domains are created after reading each domain_classname, then all the
actor_packages are added to this domain.

Actors are created by giving its domain and its name. The actor name
may come from the actor icons or be directly specified by the user.
The actors are searched in the actor packages of the given domain.
If no actor with the given name is found, the compatible domains (specified
by a complete partial order of domains) is asked. If still not found,
the domain polymorphic actor packages are searched.


@author Jie Liu, Lukito Muliadi
@version $Id$
*/
public class PtolemySystem {
    /** Null constructor. The configuration file is loaded from some
     *  default place.
     */
    public PtolemySystem() {
    }

    /** Construct the PtolemySystem with a input stream. The configuration
     *  information is read from the stream.
     *  @param inputstream The stream from which the configuration is read.
     */
    public PtolemySystem(InputStream inputstream) {
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Add an actor package for the specified domain. If the domain is null
     *  the actor package is considered a domain polymorphic actor
     *  package. The domain is added to the domain list if it's
     *  not already there.
     * @param domain The domain this package of actors belongs to.
     * @param actorpackage The full package name.
     */
    public void addActorPackage(Domain domain, String actorpackage) {
        // if ! (domain exists) addDomain(domain)
        // domain.addActorPackage(actorpackage);
    }

    /** Add a domain to the domain list. The list can be saved by calling
     *  saveConfiguration() method. If the domain is already on the domain
     *  list, do nothing.
     * @param domain The added domain.
     */
    public void addDomain(Domain domain) {
    }

    /** Return a new actor with the specified name that is compatible with
     *  the specified domain.
     *  The actor is searched through the list of actor packages for
     *  the given domain. The first one that matches the name will be
     *  created and returned. If no actor is found in that domain,
     *  the compatible domains will be searched, and then the domain
     *  polymorphic actors. If the actor is still not found,
     *  a RuntimeException -- ClassNotFoundException will be thrown.
     * @param domain The domain from which the actor is asked.
     * @param actorName The name of the actor.
     * @return The new actor object if there is one.
     */
    public Actor createActor(Domain domain, String actorName) {
        return null;
    }

    /** Return the actor packages array for the specified domain in there
     *  creation orders. This is also the order of searching actors.
     *  If the domain is null, then the actor package of the domain
     *  polymorphic actors will be returned. If the domain is not on the
     *  domain list, returns null.
     * @param domain The domain requested.
     * @return The actor packages in a array.
     */
    public String[] getActorPackages(Domain domain) {
        //if (domain == null) return PolyDomain.getActorPackages()
        //if (domain exist) return domain.getActorPackages()
        //if (!domain exist) return null;
        return null;
    }

    /** Return a new domain object with the specified name. The name of
     *  a domain is the full class name of the Java class. This domain
     *  class is searched in the CLASSPATH of the system. If no matched
     *  class is found, then a ClassNotFoundException will be thrown.
     *  @param domainname The full class name of the domain.
     *  @return The domain that has the name.
     */
    public Domain createDomain(String domainname) {
        return null;
    }

    /** Return an enumeration of all available domains.
     *  @return The available domains.
     */
    public Enumeration getDomains() {
        return null;
    }

    /** Return the (static) version of Ptolemy II.
     */
    public String getVersion() {
        return null;
    }

    /** Load the configuration from the default place.
     */
    public void loadConfiguration() {
    }

    /** Load the configuration from an InputStream. The domains are
     *  created for each domain configuration.
     * @param inputstream The input stream to read the configuration.
     */
    public void loadConfiguration(InputStream inputstream) {
    }

    /** Remove the specified actor path from the list in the specified
     *  domain. If the domain is not in the domain list, or the
     *  actorpackage is unknown, then an IllegalActionException is thrown.
     *  @param domain The specified domain
     *  @param actorpackage The String of actor package.
     *  @exception IllegalActionException If the domain is not on the list
     *        or the actor package is unknow.
     */
    public void removeActorPackage(Domain domain, String actorpackage)
        throws IllegalActionException {
    }

    /** Remove the specified domain from the domain list. If the domain is
     *  not on the domain list, then an IllegalActionException will be
     *  thrown.
     * @param domain The domain asked to remove.
     * @exception IllegalActionException If the domain is not on the domain
     *       list.
     */
    public void removeDomain(Domain domain) throws IllegalActionException{
    }

    /** Save the current configuration, including domains and actor packages
     *  to the default place.
     */
    public void saveConfiguration(){
    }

    /** Save the current configuration to the specified output stream.
     *  If the output stream is null, then save to the default place,
     *  @param outputstream The specified output stream.
     */
    public void saveConfiguration(OutputStream outputstream) {
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // A list of known domains.
    private LinkedList _domains;
}
