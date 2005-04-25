/* The interface of rollbackable objects.

Copyright (c) 2005 The Regents of the University of California.
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

package ptolemy.backtrack;

//////////////////////////////////////////////////////////////////////////
//// Rollbackable
/**
   The interface of rollbackable objects.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public interface Rollbackable {

    /** Commit changes up to the given timestamp, but not including changes
     *  made at timestamp and afterward.
     *
     *  @param timestamp The timestamp.
     */
    public void $COMMIT(long timestamp);

    /** Get the checkpoint object that monitors this rollbackable object.
     *
     *  @return The checkpoint object.
     */
    public Checkpoint $GET$CHECKPOINT();

    /** Set the checkpoint object of this rollbackable object. A merge
     *  operation is performed on the current checkpoint object and the new
     *  checkpoint object, so that the two checkpoint objects monitor the same
     *  set of objects when this function returns.
     *
     *  @param checkpoint The new checkpoint object.
     *  @return This rollbackable object itself.
     *  @see Checkpoint#setCheckpoint(Checkpoint)
     */
    public Object $SET$CHECKPOINT(Checkpoint checkpoint);

    /** Restore a previous state to all the private fields of this rollbackable
     *  object.
     *
     *  @param timestamp The timestamp taken at the time when the previous
     *   state was recorded.
     *  @param trim Whether to delete the records used for the rollback.
     *  @see Checkpoint#rollback(long, boolean)
     */
    public void $RESTORE(long timestamp, boolean trim);
}
