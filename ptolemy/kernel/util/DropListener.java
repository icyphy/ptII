/* Interface for objects that wish to be informed of drops.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// DropListener
/**
A DropListener is an interface implemented by objects that are
interested in being informed when a user interface is used to
change the contents of an object.  In particular, no notification
will occur if the change is made while constructing a model reading
a file, for instance. Only interactive changes made through a visual
editor are expected to trigger notifications.

@author Edward A. Lee
@version $Id$
@since Ptolemy II 3.1
@see ChangeRequest
*/
public interface DropListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a drop. This is called when a user interface drops
     *  an object into an object implementing this interface. The
     *  call actually occurs when the change request is queued, so
     *  the listener (which implements this method) should react
     *  from within a change request itself, so as to ensure that
     *  the reaction occurs after the drop has been completed.
     */
    public void dropped();
}
