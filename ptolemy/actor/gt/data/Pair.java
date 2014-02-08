/* Generic pair data structure.

@Copyright (c) 2007-2009 The Regents of the University of California.
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
package ptolemy.actor.gt.data;

//////////////////////////////////////////////////////////////////////////
//// Pair

/**
 Generic pair data structure.

 @param <E1> Type of the first element.
 @param <E2> Type of the second element.
 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
@SuppressWarnings("serial")
public class Pair<E1, E2> extends Tuple<Object> {

    /** Construct a pair with two elements.
     *
     *  @param first The first element.
     *  @param second The second element.
     */
    public Pair(E1 first, E2 second) {
        super(first, second);
    }

    /** Get the first element.
     *
     *  @return The first element.
     *  @see #setFirst(Object)
     */
    public E1 getFirst() {
        return (E1) get(0);
    }

    /** Get the second element.
     *
     *  @return The second element.
     *  @see #setSecond(Object)
     */
    public E2 getSecond() {
        return (E2) get(1);
    }

    /** Set the two elements in this pair.
     *
     *  @param first The first element.
     *  @param second The second element.
     */
    public void set(E1 first, E2 second) {
        set(0, first);
        set(1, second);
    }

    /** Set the first element in this pair.
     *
     *  @param first The first element.
     *  @see #getFirst()
     */
    public void setFirst(E1 first) {
        set(0, first);
    }

    /** Set the second element in this pair.
     *
     *  @param second The first element.
     *  @see #getSecond()
     */
    public void setSecond(E2 second) {
        set(1, second);
    }
}
