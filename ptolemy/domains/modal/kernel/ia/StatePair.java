/* A pair of states.

 Copyright (c) 1999-2014 The Regents of the University of California.
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
package ptolemy.domains.modal.kernel.ia;

import ptolemy.domains.modal.kernel.State;

///////////////////////////////////////////////////////////////////
//// StatePair

/**
 A pair of states.
 This class is used in the representation of alternating simulation.
 @see InterfaceAutomaton#computeAlternatingSimulation

 @author Yuhong Xiong
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (yuhong)
 @Pt.AcceptedRating Red (yuhong)
 */
public class StatePair {
    /** Construct an instance with the specified states.
     *  @param first The first state in the pair.
     *  @param second The second state in the pair.
     */
    public StatePair(State first, State second) {
        _first = first;
        _second = second;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class method to return true if the specified
     *  object is an instance of StatePair and it contains the same
     *  states as this one.
     *  @param object An object to compare with this one.
     *  @return True if the specified object is an instance of StatePair
     *   and this one contains the same states, false otherwise.
     */
    @Override
    public boolean equals(Object object) {
        if (object instanceof StatePair) {
            return this.first() == ((StatePair) object).first()
                    && this.second() == ((StatePair) object).second();
        }

        return false;
    }

    /** Return the first state in this pair.
     *  @return The first state in this pair.
     */
    public State first() {
        return _first;
    }

    /** Override the base class method to ensure that the pairs that
     *  are equal (according to the equals() method) have the same
     *  hash code.
     *  @return The hash code.
     */
    @Override
    public int hashCode() {
        return _first.hashCode() + _second.hashCode();
    }

    /** Return the second state in this pair.
     *  @return The second state in this pair.
     */
    public State second() {
        return _second;
    }

    /** Return a string representation of this pair. The string contains
     *  the name of the first state, followed by a " - ", followed by
     *  the name of the second state.
     *  @return A string containing the names of the two states separated
     *   by a " - ".
     */
    @Override
    public String toString() {
        return _first.getName() + " - " + _second.getName();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private State _first;

    private State _second;
}
