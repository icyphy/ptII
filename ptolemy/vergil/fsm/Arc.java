/* An object representing an arc between two states.

 Copyright (c) 2000-2005 The Regents of the University of California.
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
package ptolemy.vergil.fsm;

import ptolemy.kernel.ComponentRelation;

//////////////////////////////////////////////////////////////////////////
//// Arc

/**
 Instances of this class represent an arc between two states in a state
 machine visualization.  To see how this class is used and links are made
 using this class, see ArcModel in FSMGraphModel.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 @see FSMGraphModel.ArcModel
 */
public class Arc {
    /** Return the head of this link.   This will be the icon of a state.
     *  @return The head of this link
     *  @see #setHead(Object)
     */
    public Object getHead() {
        return _head;
    }

    /** Return the relation that this link represents.  This should always
     *  be an instance of Transition.
     *  @return The relation that represents this link
     *  @see #setRelation(ComponentRelation)
     */
    public ComponentRelation getRelation() {
        return _relation;
    }

    /** Return the tail of this link.   This will be the icon of a state.
     *  @return The tail of this link.
     *  @see #setTail(Object)
     */
    public Object getTail() {
        return _tail;
    }

    /** Set the head of this link.  This will be the icon of a state.
     *  @param head The head of this link.
     *  @see #getHead()
     */
    public void setHead(Object head) {
        _head = head;
    }

    /** Set the relation that this link represents.  This should always
     *  be an instance of Transition.
     *  @param relation The relation of this link.
     *  @see #getRelation()
     */
    public void setRelation(ComponentRelation relation) {
        _relation = relation;
    }

    /** Set the tail of this link.  This will be the icon of a state.
     *  @param tail The Tail of this link.
     *  @see #getTail()
     */
    public void setTail(Object tail) {
        _tail = tail;
    }

    /** Return a string representation of this link.
     */
    public String toString() {
        return "Arc(" + _head + ", " + _tail + ", " + _relation + ")";
    }

    private Object _head;

    private Object _tail;

    private ComponentRelation _relation;
}
