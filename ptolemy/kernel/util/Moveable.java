/* Interface for objects that can can be moved in a list of similar objects.

Copyright (c) 1997-2004 The Regents of the University of California.
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

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// Moveable
/**
   This is an interface for objects that can be moved in a list of
   similar objects.

   @author Edward A. Lee
   @version $Id$
   @since Ptolemy II 4.1
   @Pt.ProposedRating Green (eal)
   @Pt.AcceptedRating Red (eal)
*/

public interface Moveable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Move this object down by one in the list of similar
     *  objects in its container. If the object has no
     *  container, or if it is already last on the list,
     *  then it is not moved.
     *  @return The index of this object prior to moving it,
     *   or -1 if it is not moved.
     */
    public int moveDown();
    
    /** Move this object to the first position in the list of
     *  similar objects in its container.  If the object has no
     *  container, or if it is already first on the list,
     *  then it is not moved.
     *  @return The index of this object prior to moving it,
     *   or -1 if it is not moved.
     */
    public int moveToFirst();

    /** Move this object to the specified position in the list of
     *  similar objects in its container, where 0 is the first position.
     *  If the object has no container, or if it is already at the specified
     *  position, then it is not moved.
     *  @param index The position to which to move the object.
     *  @return The index of this object prior to moving it,
     *   or -1 if it is not moved.
     *  @exception IndexOutOfBoundsException If the index is out of bounds.
     */
    public int moveToIndex(int index) throws IndexOutOfBoundsException;

    /** Move this object to the last position in the list of
     *  similar objects in its container.  If the object has no
     *  container, or if it is already last on the list,
     *  then it is not moved.
     *  @return The index of this object prior to moving it,
     *   or -1 if it was not moved.
     */
    public int moveToLast();

    /** Move this object up by one in the list of similar
     *  objects in its container.  If the object has no
     *  container, or if it is already first on the list,
     *  then it is not moved.
     *  @return The index of this object prior to moving it,
     *   or -1 if it was not moved.
     */
    public int moveUp();
}
