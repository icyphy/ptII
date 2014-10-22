/* An attribute that creates an editor to configure its container.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// EditorFactory

/**
 This is an attribute that can create an editor for interactively
 configuring its container. If you place an instance of this class
 inside a Ptolemy II object, then when a user double clicks on the
 object, instead of
 the default behavior (which is to edit parameters), the createEditor()
 method of this class is called.
 <p>
 This differs from EditorPaneFactory
 in that it is responsible for every aspect of creating the editor.
 Thus, it has to create a top-level window, rather than just a pane
 to insert in a top-level window.
 <p>
 In this base class, the createEditor() method is abstract.
 Derived classes must override this method to present a user
 interface for configuring the object.
 For example, a digital filter actor could present a filter
 design interface.  A plotter actor could present a panel for
 configuring a plot.  A file reader actor could present a file
 browser.

 @see EditorPaneFactory
 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public abstract class EditorFactory extends Attribute implements Editable {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public EditorFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an editor for configuring the container.
     *  This editor will have no parent window.
     */
    @Override
    public void createEditor() {
        createEditor(getContainer(), null);
    }

    /** Create an editor for configuring the specified object.
     *  This editor will have no parent window.
     *  @param object The object to configure.
     */
    @Override
    public void createEditor(NamedObj object) {
        createEditor(object, null);
    }

    /** Create an editor for configuring the specified object with the
     *  specified parent window.
     *  @param object The object to configure.
     *  @param parent The parent window, or null if there is none.
     */
    @Override
    public abstract void createEditor(NamedObj object, Frame parent);
}
