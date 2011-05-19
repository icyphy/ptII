/* A data structure that holds a timestamp microstep pair.

@Copyright (c) 2008-2009 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.domains.ptides.kernel;

import ptolemy.actor.util.Time;

/**
 *  A timestamp and a microstamp that represent a Tag.
 *
 *  @author Jia Zou
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Yellow (jiazou)
 *  @Pt.AcceptedRating Red (jiazou)
 *
 */
public class Tag implements Comparable {

    /** Construct a Tag.
     *  @param timestamp The timestamp.
     *  @param microstep The microstep.
     */
    public Tag(Time timestamp, int microstep) {
        this.timestamp = timestamp;
        this.microstep = microstep;
    }

    /** Construct an empty Tag.
     */
    public Tag() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /** The timestamp. */
    public Time timestamp;

    /** The microstep. */
    public int microstep;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compare a tag with another.
     *  @param other The object to be compared.
     *  @return The result of the comparison, which is a lexicographical order
     *  with the timestamp as the first element and microstep as the second.
     */
    public int compareTo(Object other) {
        Tag tag2 = (Tag) other;
        if (timestamp.compareTo(tag2.timestamp) == 1) {
            return 1;
        } else if (timestamp.compareTo(tag2.timestamp) == -1) {
            return -1;
        } else {
            if (microstep > tag2.microstep) {
                return 1;
            } else if (microstep < tag2.microstep) {
                return -1;
            } else {
                return 0;
            }
        }
    }
    
    // 	FIXME: FindBugs: "Eq: Class defines compareTo(...) and
    // 	uses Object.equals() (EQ_COMPARETO_USE_OBJECT_EQUALS)"

    // 	"This class defines a compareTo(...) method but inherits its
    // 	equals() method from java.lang.Object. Generally, the value of
    // 	compareTo should return zero if and only if equals returns
    // 	true. If this is violated, weird and unpredictable failures
    // 	will occur in classes such as PriorityQueue. In Java 5 the
    // 	PriorityQueue.remove method uses the compareTo method, while
    // 	in Java 6 it uses the equals method."
    
    // 	"From the JavaDoc for the compareTo method in the Comparable interface:"

    // 	    "It is strongly recommended, but not strictly
    // 	    required that (x.compareTo(y)==0) ==
    // 	    (x.equals(y)). Generally speaking, any class that
    // 	    implements the Comparable interface and violates
    // 	    this condition should clearly indicate this
    // 	    fact. The recommended language is "Note: this
    // 	    class has a natural ordering that is inconsistent
    // 	    with equals.""

    /** Return a string representation of this class.
     *  @return a string representation of this class.
     */ 
    public String toString() {
        return "timestamp = " + timestamp.getDoubleValue() +
            ", microstep = " + microstep;
    }
}
