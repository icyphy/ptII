/* A subclass of Thread to be used for optimizing reader-writer mechanism in
   Ptolemy II.

 Copyright (c) 1997-2000 The Regents of the University of California.
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

@ProposedRating Green (lmuliadi@eecs.berkeley.edu)
@AcceptedRating Green (liuj@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.Serializable;
import java.util.Hashtable;

//////////////////////////////////////////////////////////////////////////
////
/** PtolemyThread
PtolemyThread extends Thread by adding a <i>readDepth</i> field. This field is
used for counting the number of read accesses this thread has gotten from the
workspace.
<p>
In general, if a thread will ever need a read or write access from the
workspace (by calling the getReadAccess() or getWriteAccess() method) then
it should use the PtolemyThread class. One advantage is that it provides a
more efficient access to the readDepth field, as otherwise the workspace has
to store the readDepth fields for the thread in a hash table.
<p>
For flexibility, one can still use the reader-writer mechanism in the Workspace
class just by using the Thread class. Obviously, some efficiency will be lost
as the workspace has to store and then search the readDepth fields in a hash
table. (as was mentioned in the previous paragraph)

@author Lukito Muliadi
@version $Id$
*/
public class PtolemyThread extends Thread {

    /** Construct a new PtolemyThread object. This constructor has the
     *  same effect as PtolemyThread(null, null, <i>gname</i>), where
     *  <i>gname</i> is a newly generated name. Automatically generated
     *  names are of the form "Thread-"+n, where n is an integer.
     */
    public PtolemyThread() {
        super();
    }

    /** Construct a new PtolemyThread object. This constructor has the same
     *  effect as PtolemyThread(null, target, <i>gname</i>), where
     *  <i>gname</i> is a newly generated name. Automatically generated
     *  names are of the form "Thread-"+n, where n is an integer.
     *  @param target The object whose run method is called.
     */
    public PtolemyThread(Runnable target) {
        super(target);
    }

    /** Construct a new PtolemyThread object. This constructor has the
     *  same effect as PtolemyThread(null, target, name)
     *  @param target The object whose run method is called.
     *  @param name The name of the new thread.
     *
     */
    public PtolemyThread(Runnable target, String name) {
        super(target, name);
    }

    /** Construct a new PtolemyThread object. This constructor has the
     *  same effect as PtolemyThread(null, null, name)
     *  @param name The name of the new thread.
     */
    public PtolemyThread(String name) {
        super(name);
    }

    /** Construct a new PtolemyThread object. This constructor has the
     *  same effect as PtolemyThread(group, target, gname), where gname is a
     *  newly generated name. Automatically generated names are of
     *  the form "Thread-"+n, where n is an integer.
     *  @param group The thread group
     *  @param target The object whose run method is called.
     */
    public PtolemyThread(ThreadGroup group, Runnable target) {
        super(group, target);
    }

    /** Construct a new PtolemyThread object so that it has target as
     *  its run object, has the specified name as its name, and belongs
     *  to the thread group referred to by group.
     *  @param group The thread group.
     *  @param target The object whose run method is called.
     *  @param name The name of the new thread.
     *  @exception SecurityException If the superclass constructor throws it.
     *
     */
    public PtolemyThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
    }

    /** Construct a new PtolemyThread object. This constructor has the same
     *  effect as PtolemyThread(group, null, name).
     *  @param group The thread group.
     *  @param name The name of the new thread.
     */
    public PtolemyThread(ThreadGroup group, String name) {
        super(group, name);
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the read depth field.
     * @return The read depth field.
     */
    public int getReadDepth() {
        return readDepth;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         variables                         ////
    /** readDepth the number of read permissions this thread holds.
     *  This field is made 'package friendly' because only the Workspace class
     *  should access this field.
     */
    // FIXME: This should really be 'package friendly'.
    // After the bug in JavaScope got fixed, remove the word 'public' (lmuliadi)
    public int readDepth;
}
