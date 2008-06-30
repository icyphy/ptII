/* Interface representing a dependency between ports.

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

import java.util.Iterator;

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;


//////////////////////////////////////////////////////////////////////////
//// CausalityInterface

/**
 This class provides causality interfaces for actor networks as described
 in the paper "Causality Interfaces for Actor Networks" by Ye Zhou and
 Edward A. Lee, ACM Transactions on Embedded Computing Systems (TECS),
 April 2008, as available as <a href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-148.pdf">
 Technical Report No. UCB/EECS-2006-148</a>,
 November 16, 2006.
 <p>
 Causality interfaces represent dependencies between input and output ports
 of an actor and can be used to perform scheduling or static analysis
 on actor models.

 @author Edward A. Lee
 @version $Id: CausalityInterface.java 47513 2007-12-07 06:32:21Z cxh $
 @since Ptolemy II 7.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class CausalityInterface {
    
    /** Construct a causality interface for the specified actor.
     *  @param actor The actor for which this is a causality interface.
     *  @param defaultDependency The default dependency of an output
     *   port on an input port.
     */
    public CausalityInterface(Actor actor, Dependency defaultDependency) {
        _actor = actor;
        _defaultDependency = defaultDependency;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the dependency between the specified input port
     *  and the specified output port.  This base class returns
     *  the default dependency if the first port is an input
     *  port owned by this actor and the second one is an output
     *  port owned by this actor. Otherwise, it returns the
     *  additive identity of the dependency.
     *  Derived classes should override this method to provide
     *  actor-specific dependency information.
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
            result.append(" has output depenencies as follows:\n");
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
