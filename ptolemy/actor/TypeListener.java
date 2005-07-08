/* An interface implemented by objects that are interested in being kept
 informed about type changes in a Typeable object.

 Copyright (c) 1998-2005 The Regents of the University of California.
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
package ptolemy.actor;

//////////////////////////////////////////////////////////////////////////
//// TypeListener

/**
 An interface implemented by objects that are interested in being kept
 informed about type changes in a Typeable object.
 If the Typeable object is a TypedIOPort, the listeners register their
 interest through the addTypeListener() method, and are informed of the
 changes by receiving instances of TypeEvent in the typeChanged() method.

 @author Yuhong Xiong
 @version $Id$
 @since Ptolemy II 0.2
 @Pt.ProposedRating Green (yuhong)
 @Pt.AcceptedRating Green (cxh)
 @see TypeEvent
 @see ptolemy.actor.TypedIOPort#addTypeListener
 @see ptolemy.actor.TypedIOPort#removeTypeListener
 */
public interface TypeListener {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to the fact that the type of a Typeable is changed.
     *  @param event The type change event.
     */
    public void typeChanged(TypeEvent event);
}
