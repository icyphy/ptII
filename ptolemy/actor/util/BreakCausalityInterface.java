/* Causality interface where no output depends on any input.

 Copyright (c) 2003-2006 The Regents of the University of California.
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
package ptolemy.actor.util;

import java.util.Collection;
import java.util.LinkedList;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;


//////////////////////////////////////////////////////////////////////////
//// BreakCausalityInterface

/**
 This class provides a causality interface
 where no output port depends on any input port.
 That is, the dependency of any output port on any
 input port is the oPlusIdentity() of the specified
 default dependency.

 @see Dependency
 
 @author Edward A. Lee
 @version $Id: BreakCausalityInterface.java 47513 2007-12-07 06:32:21Z cxh $
 @since Ptolemy II 7.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class BreakCausalityInterface extends DefaultCausalityInterface {
    
    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     */
    public BreakCausalityInterface(Actor actor, Dependency defaultDependency) {
        super(actor, defaultDependency);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a collection of the ports in this actor that depend on
     *  or are depended on by the specified port. This method
     *  returns a collection containing only the specified port.
     *  <p>
     *  Derived classes may override this, but they may need to
     *  also override {@link #getDependency(IOPort, IOPort)}
     *  and {@link #equivalentPorts(IOPort)} to be consistent.
     *  @param port The port to find the dependents of.
     */
    public Collection<IOPort> dependentPorts(IOPort port) {
        LinkedList<IOPort> result = new LinkedList<IOPort>();
        result.add(port);
        return result;
    }
    
    /** Return a collection of the ports in this actor that are
     *  in the same equivalence class. This method
     *  returns a collection containing only the specified port.
     *  <p>
     *  If derived classes override this, they may also
     *  need to override {@link #getDependency(IOPort,IOPort)}
     *  and {@link #dependentPorts(IOPort)} to be consistent.
     *  The returned result should always include the specified input port.
     *  @param inputPort The port to find the equivalence class of.
     */
    public Collection<IOPort> equivalentPorts(IOPort input) {
        LinkedList<IOPort> result = new LinkedList<IOPort>();
        result.add(input);
        return result;
    }
    
    /** Return the dependency between the specified input port
     *  and the specified output port.  This method returns
     *  the oPlusIdentity() of the default dependency given
     *  in the constructor.
     *  <p>
     *  Derived classes should override this method to provide
     *  actor-specific dependency information. If they do so,
     *  then they may also need to override {@link #equivalentPorts(IOPort)}
     *  and {@link #dependentPorts(IOPort)} to be consistent.
     *  @return The dependency between the specified input port
     *   and the specified output port.
     */
    public Dependency getDependency(IOPort input, IOPort output) {
        return _defaultDependency.oPlusIdentity();
    }
}
