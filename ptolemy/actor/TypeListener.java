/* An interface implemented by objects that are interested in being kept
   informed about changes in the types of ports.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red
*/

package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// TypeListener
/**
An interface implemented by objects that are interested in being kept
informed about changes in the types of ports.
The listeners register their interest in TypedIOPort through the
addTypeListener() method, and are informed of the changes by receiving
instances of the TypeEvent in the typeChanged() method.

@author Yuhong Xiong
@version $Id$
@see TypeEvent
@see ptolemy.actor.TypedIOPort#addTypeListener
@see ptolemy.actor.TypedIOPort#removeTypeListener
*/

public interface TypeListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify that the type of a port is changed.
     *
     * @param event The type change event.
     */
    public void typeChanged(TypeEvent event);
}
