/* A data structure that holds a timestamp microstep pair.

@Copyright (c) 2008-2014 The Regents of the University of California.
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
 *  @since Ptolemy II 10.0
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
    @Override
    public int compareTo(Object other) {
        Tag tag2 = (Tag) other;
        // Call compareTo() only once for performance reasons.
        final int compareToResult = timestamp.compareTo(tag2.timestamp);
        // FindBugs: RV: Bad use of return value.  Be sure not to compare
        // against -1 or 1.
        if (compareToResult > 0) {
            return 1;
        } else if (compareToResult < 0) {
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

    /** Checks if this tag is the same as another.
     *  @param arg0 The object checking against.
     *  @return true if the tags are equal.
     */
    @Override
    public boolean equals(Object arg0) {
        if (!(arg0 instanceof Tag)) {
            return false;
        }
        return compareTo(arg0) == 0;
    }

    /** Hashcode for this class.
     *  @return hashcode for this class.
     */
    @Override
    public int hashCode() {
        return timestamp.hashCode() >>> microstep;
    }

    /** Return a string representation of this class.
     *  @return a string representation of this class.
     */
    @Override
    public String toString() {
        return "timestamp = " + timestamp.getDoubleValue() + ", microstep = "
                + microstep;
    }
}
