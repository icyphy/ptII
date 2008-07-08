/* Causality interface where all outputs depend on all inputs.

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
import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;


//////////////////////////////////////////////////////////////////////////
//// DefaultCausalityInterface

/**
 This class provides causality interfaces for actor networks as described
 in the paper "Causality Interfaces for Actor Networks" by Ye Zhou and
 Edward A. Lee, ACM Transactions on Embedded Computing Systems (TECS),
 April 2008, as available as <a href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-148.pdf">
 Technical Report No. UCB/EECS-2006-148</a>,
 November 16, 2006.  Specifically, this class represents a simple
 default causality interface where every output port depends on every
 input port.
 <p>
 Causality interfaces represent dependencies between input and output ports
 of an actor and can be used to perform scheduling or static analysis
 on actor models.

 @author Edward A. Lee
 @version $Id: DefaultCausalityInterface.java 47513 2007-12-07 06:32:21Z cxh $
 @since Ptolemy II 7.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class DefaultCausalityInterface implements CausalityInterface {
    
    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     */
    public DefaultCausalityInterface(Actor actor, Dependency defaultDependency) {
        _actor = actor;
        _defaultDependency = defaultDependency;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return a collection of the ports in this actor that depend on
     *  or are depended on by the specified port. A port X depends
     *  on a port Y if X is an output and Y is an input and
     *  getDependency(X,Y) returns something not equal to
     *  the oPlusIdentity() of the default dependency specified
     *  in the constructor.
     *  <p>
     *  This base class presumes (but does not check) that the
     *  argument is a port contained by the associated actor.
     *  If the actor is an input, then it returns a collection of
     *  all the outputs. If the actor is output, then it returns
     *  a collection of all the inputs.
     *  <p>
     *  Derived classes may override this, but they may need to
     *  also override {@link #getDependency(IOPort, IOPort)}
     *  and {@link #equivalentPorts(IOPort)} to be consistent.
     *  @param port The port to find the dependents of.
     */
    public Collection<IOPort> dependentPorts(IOPort port) {
        // FIXME: This does not support ports that are both input and output.
        // Should it?
        if (port.isOutput()) {
            return _actor.inputPortList();
        } else {
            return _actor.outputPortList();
        }
    }
    
    /** Return a collection of the ports in this actor that are
     *  in the same equivalence class. This base class
     *  returns a collection of all the input ports of the
     *  associated actor (although it is not checked, this
     *  method presumes that the specified actor is an
     *  input port contained by the associated actor).
     *  If derived classes override this, they may also
     *  need to override {@link #getDependency(IOPort,IOPort)}
     *  and {@link #dependentPorts(IOPort)} to be consistent.
     *  The returned result should always include the specified input port.
     *  <p>
     *  An equivalence class is defined as follows.
     *  If ports X and Y each have a dependency not equal to the
     *  default depenency's oPlusIdentity(), then they
     *  are in an equivalence class. That is,
     *  there is a causal dependency. They are also in
     *  the same equivalence class if there is a port Z
     *  in an equivalence class with X and in an equivalence
     *  class with Y. Otherwise, they are not in the same
     *  equivalence class. If there are no
     *  output ports, then all the input ports
     *  are in a single equivalence class.
     *  @param input The port to find the equivalence class of.
     */
    public Collection<IOPort> equivalentPorts(IOPort input) {
        return _actor.inputPortList();
    }
    
    /** Return the actor for which this is a dependency.
     *  @return The actor for which this is a dependency.
     */
    public Actor getActor() {
        return _actor;
    }

    /** Return the dependency between the specified input port
     *  and the specified output port.  This base class returns
     *  the default dependency if the first port is an input
     *  port owned by this actor and the second one is an output
     *  port owned by this actor. Otherwise, it returns the
     *  additive identity of the dependency.
     *  <p>
     *  Derived classes should override this method to provide
     *  actor-specific dependency information. If they do so,
     *  then they may also need to override {@link #equivalentPorts(IOPort)}
     *  and {@link #dependentPorts(IOPort)} to be consistent.
     *  @return The dependency between the specified input port
     *   and the specified output port.
     */
    public Dependency getDependency(IOPort input, IOPort output) {
        if (input.isInput()
                && input.getContainer() == _actor
                && output.isOutput()
                && output.getContainer() == _actor) {
            return _defaultDependency;
        }
        return _defaultDependency.oPlusIdentity();
    }
    
    /** Return a description of the causality interfaces.
     *  @return A description of the causality interfaces.
     */
    public String toString() {
        StringBuffer result = new StringBuffer();
        Iterator inputPorts = _actor.inputPortList().iterator();
        while (inputPorts.hasNext()) {
            IOPort inputPort = (IOPort)inputPorts.next();
            result.append(inputPort.getName());
            result.append(" has output dependencies as follows:\n");
            Iterator outputPorts = _actor.outputPortList().iterator();
            while (outputPorts.hasNext()) {
                IOPort outputPort = (IOPort)outputPorts.next();
                result.append("   ");
                result.append(outputPort.getName());
                result.append(": ");
                result.append(getDependency(inputPort, outputPort));
                result.append("\n");
            }
        }
        return result.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////
    
    /** The associated actor. */
    protected Actor _actor;
    
    /** The default dependency of an output port on an input port. */
    protected Dependency _defaultDependency;
}
