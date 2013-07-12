/* Pair is a container for a pair of values.

 Copyright (c) 2012-2013 The Regents of the University of California.
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
package ptolemy.domains.metroII.kernel;

/**
 * Pair is a container for a pair of values, which may be of different types (F and S). 
 * The individual values can be accessed through its public methods.
 *
 * @author Liangpeng Guo
 * @version $Id$
 * @since Ptolemy II 9.1
 * @Pt.ProposedRating Red (glp)
 * @Pt.AcceptedRating Red (glp)
 *
 */
public class Pair<F, S> {
    /**
     * Construct a Pair with two values.
     * @param first First value
     * @param second Second value
     */
    public Pair(F first, S second) {
        this.first = first;
        this.second = second;
    }
    
    /**
     * Set the first element
     * @param first Value to be assigned to the first element
     */
    public void setFirst(F first) {
        this.first = first;
    }

    /**
     * Set the second element
     * @param second Value to be assigned to the second element
     */
    public void setSecond(S second) {
        this.second = second;
    }

    /**
     * Get the first element
     * @return The first element in the pair.
     */
    public F getFirst() {
        return first;
    }

    /**
     * Get the second element
     * @return The second element in the pair.
     */
    public S getSecond() {
        return second;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /**
     * First element of the pair
     */
    private F first;
    
    /**
     * Second element of the pair
     */
    private S second; 

}
