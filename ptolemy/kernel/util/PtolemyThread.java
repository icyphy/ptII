/* A subclass of Thread to be used for optimizing reader-writer mechanism in
   PtolemyII.

 Copyright (c) 1997-1998 The Regents of the University of California.
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

@ProposedRating Yellow (lmuliadi@eecs.berkeley.edu)
@AcceptedRating ??? (???@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

import java.io.Serializable;
import java.util.Hashtable;
import collections.LinkedList;
import collections.CollectionEnumeration;

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
table. (as was metioned in the previous paragraph)

@author Lukito Muliadi
@version $Id$
*/
public class PtolemyThread extends Thread {

    // FIXME: What kind of constructors should I provide here ? Should I
    // provide all constructors from Thread class ?

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


    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    // readDepth the number of read permissions this thread holds.
    // FIXME: Is this the right level of permission ?? Should it be private
    // and then provide public interface for accessing it ? Note that method
    // calls are more expensive than field accesses.
    // If we want public interface, (at least) these methods are needed:
    // incr(), decr(), isZero()
    public int readDepth;


    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

}
