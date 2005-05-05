/* The currrent state of a checkpoint object.

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

import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// CheckpointState
/**
   The current state of a checkpoint object. A checkpoint object stores its
   complete state in its <tt>CheckpointState</tt> object. Different checkpoint
   objects may share the same state, and hence they represent the same
   checkpoint entity.
   <p>
   When two checkpoint objects are merged, they exchange their states and
   compute the union of the two.

   @author Thomas Feng
   @version $Id$
   @since Ptolemy II 5.1
   @Pt.ProposedRating Red (tfeng)
   @Pt.AcceptedRating Red (tfeng)
*/
public class CheckpointState {

    ///////////////////////////////////////////////////////////////////
    ////                       public methods                      ////

    /** Create a new checkpoint, and return the new timestamp.
     *
     *  @return The new timestamp.
     */
    public long createCheckpoint() {
        return ++_currentTimestamp;
    }

    /** Get the list of all the monitored objects.
     *
     *  @return The list of all the monitored objects.
     */
    public List getMonitoredObjects() {
        return _monitoredObjects;
    }

    /** Get the current timestamp.
     *
     *  @return The current timestamp.
     */
    public long getTimestamp() {
        return _currentTimestamp;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private fields                      ////

    /** The current timestamp. It is static, so different checkpoint states use
     *  different timestamps, and the timestamps are always increasing in the
     *  time line.
     */
    private static long _currentTimestamp = 0;

    /** The list of objects monitored by the checkpoint object.
     */
    private List _monitoredObjects = new LinkedList();
}
