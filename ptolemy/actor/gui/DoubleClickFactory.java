/* An attribute that handles double-clicks for its container.

 Copyright (c) 2007 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.awt.Frame;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// DoubleClickFactory

/**
 This is an attribute that handles double-clicks for its container.
 If you place an instance of this class inside a Ptolemy II object,
 then when the user double clicks on the object, instead of the
 default behavior (which is to edit parameters), the invoke() method
 of this class is called.
 <p>
 In this base class, the invoke() method is abstract.
 Derived classes must override this method to present user-defined
 code.

 @see EditorFactory
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public abstract class DoubleClickFactory extends Attribute {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DoubleClickFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an editor for configuring the container.
     *  This editor will have no parent window.
     */
    public void invoke() {
        invoke(getContainer(), null);
    }

    /** Create an editor for configuring the specified object.
     *  This editor will have no parent window.
     *  @param object The object to configure.
     */
    public void invoke(NamedObj object) {
        invoke(object, null);
    }

    /** Create an editor for configuring the specified object with the
     *  specified parent window.
     *  @param object The object to configure.
     *  @param parent The parent window, or null if there is none.
     */
    public abstract void invoke(NamedObj object, Frame parent);
}
