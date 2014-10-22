/* Sieve filtering out all multiples of a given number for
 the Sieve of Eratosthenes.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.domains.csp.lib;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.domains.csp.kernel.CSPActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// CSPSieve

/**
 Used in the Sieve of Eratosthenes demo. This actor represents a
 sieve process which filters out all multiples of a particular number.
 The first time the filter encounters a number it cannot filter it
 creates a new process to filter out all multiples of that number and
 appends it to the string of filters already created. This actor is a good
 illustration of how to perform changes to the topology.
 <p>
 @author Neil Smyth
 @version @$Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Red (nsmyth)
 @Pt.AcceptedRating Red (cxh)
 */
public class CSPSieve extends CSPActor {
    /** Construct a DDESink with the specified container and name.
     *  This method calls the super class constructor and creates
     *  the necessary ports.
     *  @param container The container of this actor.
     *  @param name The name of this actor.
     *  @param prime The prime this sieve is filtering out.
     *  @exception NameDuplicationException If more than one port
     *   with the same name is added to this actor, or if the entity
     *   containing this actor already contains an actor with the name
     *   of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     */
    public CSPSieve(TypedCompositeActor container, String name, int prime)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        _prime = prime;
        input = new TypedIOPort(this, "input", true, false);
        output = new TypedIOPort(this, "output", false, true);

        input.setTypeEquals(BaseType.GENERAL);
        output.setTypeEquals(BaseType.GENERAL);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The input port.
     */
    public TypedIOPort input;

    /** The output port.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Reads one Token from it's input port and writes this token to
     *  it's output ports. Needs to read one token for every output
     *  port.
     *  @exception IllegalActionException If an error occurs during
     *   executing the process.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        Token data;
        boolean isLargestPrime = true;
        int lastSeen = 0;

        while (true) {
            //System.out.println("Sieve getting data");
            data = input.get(0);
            lastSeen = ((IntToken) data).intValue();

            //System.out.println("Sieve got data:" + data.toString());
            if (lastSeen % _prime != 0) {
                // is it the next prime?
                if (isLargestPrime) {
                    // yes - make and queue the topologyChange

                    /* JFIXME
                     TopologyChangeRequest t = _makeChangeRequest(lastSeen);
                     getDirector().queueTopologyChangeRequest(t);
                     //System.out.println(getName() +
                     //     ":Queued TopologyChange");
                     */
                    _waitForDeadlock();

                    //System.out.println(getName() +": change succeeded?");
                    isLargestPrime = false;
                } else {
                    output.send(0, data);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /*  Create and return a new TopologyChangeRequest object that
     *  adds a new sieve.
     *  @param value The prime the new filter should sieve.
     *  FIXME
     private TopologyChangeRequest _makeChangeRequest(final int value) {
     TopologyChangeRequest request = new TopologyChangeRequest(this) {
     public void constructEventQueue() {
     System.out.println("TopologyRequest event q being constructed!");
     TypedCompositeActor container =
     (TypedCompositeActor)getContainer();
     CSPSieve newSieve = null;
     ComponentRelation newRelation = null;
     try {
     newSieve = new CSPSieve(container, value + "_sieve", value);
     // If we use a 1-1 relation this needs to change.
     newRelation = new IORelation(container, "R" + value);
     } catch (NameDuplicationException ex) {
     throw new InvalidStateException("11Cannot create " +
     "new sieve.");
     } catch (IllegalActionException ex) {
     throw new InvalidStateException("Cannot create " +
     "new sieve.");
     }

     queueEntityAddedEvent(container, newSieve);
     queueRelationAddedEvent(container, newRel);
     queuePortLinkedEvent(newRel, output);
     queuePortLinkedEvent(newRel, newSieve.getPort("input"));
     }
     };
     return request;
     }
     */

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    /** The prime this sieve is filtering out. */
    private int _prime;
}
