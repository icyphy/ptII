/* This class manages the Ptoelmy II domains and actor packages .

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
*/

package ptolemy.schematic;

import java.io.*;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.actor.Director;
import ptolemy.actor.Actor;
import java.util.Enumeration;
import collections.HashedMap;

//////////////////////////////////////////////////////////////////////////
//// DomainLibrary
/** 
This class provides information about the domains and actors
in Ptolemy installation. This information can be queried
and extended by clients such as user interfaces and configuration
tools. The information is persistently stored in an XML
file.

The specification for the domain library is expressed in XML and looks 
something like the following:
<pre>
<domainlibrary name="Dataflow" version="1.0">
<domain name="Static Dataflow">
<actorpackage name="SDF default" package="ptolemy.domains.sdf.lib"/>
<director name="Multirate" class="ptolemy.domains.sdf.kernel.SDFDirector"/>
<director name="Discrete Timed" class="ptolemy.domains.sdf.kernel.DTDirector"/>
</domain>
<domain name="Continuous Time">
<actorpackage name="CT default" package="ptolemy.domains.ct.lib"/>
<director name="Runge-Kutta" class="ptolemy.domains.ct.kernel.CTRKDirector"/>
</domain>
</domainlibrary>
</pre>

The domains are created after reading each domain_classname, then all the
actor_packages are added to this domain. 

<p>
Actors are created by giving its domain and its name. The actor name
may come from the actor icons or be directly specified by the user.
The actors are searched in the actor packages of the given domain.
If no actor with the given name is found, the compatible domains (specified
by a complete partial order of domains) is asked. If still not found,
the domain polymorphic actor packages are searched.


@author Jie Liu, Lukito Muliadi, John Reekie
@version $Id$
*/
public class DomainLibrary extends XMLElement{

    /** 
     * Create a new DomainLibrary object
     */	
    public DomainLibrary() {
        super("domainlibrary");
        _domains = (HashedMap) new HashedMap();
        setName("");
        setVersion("");
    }

    /** 
     * Construct a DomainLibrary object with the specified attributes.
     *
     * @param attributes a HashedMap from attribute name to attribute value.
     */
    public DomainLibrary(HashedMap attributes) {
        super("domainlibrary",attributes);
        _domains = (HashedMap) new HashedMap();
        if(!hasAttribute("name")) setName("");
        if(!hasAttribute("version")) setVersion("");
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         public methods                         ////

    /** Add a domain to the domain list. 
     * @param domain The added domain.
     */	
    public void addDomain(Domain domain) {
        String name=domain.getName();
        _domains.putAt(name,domain);
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

    /** Return an enumeration of the known domains. Each entry
     * in the returned enumeration is the name of a domain contained
     * in this domainlibrary.
     *
     *  @return An Enumaration of String
     */
    public Enumeration domains() {
        return _domains.keys();
    }

    /** 
     * Get the single instance of this class. 
     */	
    public static DomainLibrary getInstance() {
        if (_instance == null) {
            _instance = new DomainLibrary();
        }
        return _instance;
    }

   /** Return a new domain object with the specified name. 
     *  @param domainname The identifier of a domain.
     *  @return The domain that has the name.
     */	
    public Domain getDomain(String domainname) {
        return (Domain)_domains.at(domainname);
    }

    /**
     * Return the name of this library.
     */
    public String getName() {
        return getAttribute("name");
    }

    /** Return the version of this library.
     */
    public String getVersion() {
        return getAttribute("version");
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

    /** 
     * Remove the specified domain from the domain list. 
     *
     * @exception IllegalActionException If no domain with the given name
     * exists
     */	
    public void removeDomain(String domainname) throws IllegalActionException {
        _domains.removeAt(domainname);
    }

    /**
     * Set the global instance of this object.   This should be set once,
     * and once only!!!!  
     */
    public void setInstance(DomainLibrary d) {
        if(_instance==null) _instance = d;
        else throw new RuntimeException("Instance of DomainLibrary class has" +
                " already been set.");
    }

    /** 
     * Set the short name of this domainlibrary
     */
    public void setName(String s) {
        setAttribute("name", s);
    }

    /** Set the string that represents the version of this domainlibrary.
     */
    public void setVersion(String s) {
        setAttribute("version",s);
    }


    ////////////////////////////////////////////////////////////////////////
    ////                         private variables                      ////

    // A list of known domains.
    private HashedMap _domains;    

    // The single instance
    private static DomainLibrary _instance = null;
}
