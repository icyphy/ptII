/* Interface representing dependencies between ports of an associated actor.

 Copyright (c) 2008-2013 The Regents of the University of California.
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

import ptolemy.actor.Actor;
import ptolemy.actor.IOPort;
import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// CausalityInterface

/**
 This interface defines a causality interfaces for actor networks as
 described in the paper "Causality Interfaces for Actor Networks" by
 Ye Zhou and Edward A. Lee, ACM Transactions on Embedded Computing
 Systems (TECS), April 2008, as available as <a
 href="http://www.eecs.berkeley.edu/Pubs/TechRpts/2006/EECS-2006-148.pdf">
 Technical Report No. UCB/EECS-2006-148</a>, November 16, 2006.
 Causality interfaces represent dependencies between input and output
 ports of an actor and can be used to perform scheduling or static
 analysis on actor models.  Implementers of this interface must ensure
 consistency between the methods {@link #dependentPorts(IOPort)},
 {@link #equivalentPorts(IOPort)}, and {@link #getDependency(IOPort, IOPort)}.

 @see Dependency

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public interface CausalityInterface {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Set the dependency that the specified output port has
     *  on the specified input port to represent a time
     *  delay with the specified value and superdense time index.
     *  Implementations of this method should be adaptive to the type
     *  of Dependency provided by the director. For example, if the
     *  director provides a BooleanDependency, then an implementation
     *  of this method should define the dependency to be oTimesIdentity if
     *  the timeDelay is 0.0 and the index is 0, and otherwise it should
     *  be oPlusIdentity.
     *  @param input The input port.
     *  @param output The output port with a time delay dependency on the
     *   input port.
     *  @param timeDelay The time delay.
     *  @param index The superdense time index.
     */
    public void declareDelayDependency(IOPort input, IOPort output,
            double timeDelay, int index);

    /** Return a collection of the ports in this actor that depend on
     *  or are depended on by the specified port. A port Y depends
     *  on a port X if Y is an output and X is an input and
     *  getDependency(X,Y) returns the oTimesIdentity.
     *  The argument port should be contained by the same actor
     *  returned by getActor().
     *  @param port The port to find the dependents of.
     *  @exception IllegalActionException If the dependency
     *   cannot be determined.
     *  @return a collection of ports in this actor that depend
     *   on or are depended on by the specified port.
     */
    public Collection<IOPort> dependentPorts(IOPort port)
            throws IllegalActionException;

    /** Return a collection of input ports in the associated actor that are
     *  in the same equivalence class as the specified input port.
     *  An equivalence class is defined as follows.
     *  If input ports X and Y each have a dependency equal to the
     *  default dependency's oTimesIdentity to any common output port
     *  or to the state of the associated actor, then they
     *  are in an equivalence class. That is,
     *  there is a causal dependency. They are also in
     *  the same equivalence class if there is a port Z
     *  in an equivalence class with X and in an equivalence
     *  class with Y. Otherwise, they are not in the same
     *  equivalence class.
     *  @param input The port to find the equivalence class of.
     *  @exception IllegalActionException If the equivalent ports
     *   cannot be determined.
     *  @return a collection of input ports that are in the same equivalence
     *  class as the specified input port.
     */
    public Collection<IOPort> equivalentPorts(IOPort input)
            throws IllegalActionException;

    /** Return the associated actor.
     *  @return The actor for which this is a dependency.
     */
    public Actor getActor();

    /** Return the default dependency.
     *  @return The default dependency.
     */
    public Dependency getDefaultDependency();

    /** Return the dependency between the specified input port
     *  and the specified output port.
     *  @param input The specified input port.
     *  @param output The specified output port.
     *  @return The dependency between the specified input port
     *   and the specified output port.
     *  @exception IllegalActionException If the dependency
     *   cannot be determined.
     */
    public Dependency getDependency(IOPort input, IOPort output)
            throws IllegalActionException;

    /** Remove the dependency that the specified output port has
     *  on the specified input port, meaning that the dependency
     *  is set to oPlusIdentity. If there is no
     *  defined dependency between the two ports, then this
     *  method will have no effect.
     *  @param inputPort The input port.
     *  @param outputPort The output port that does not depend on the
     *   input port.
     */
    public void removeDependency(IOPort inputPort, IOPort outputPort);
}
