/* An editor for a transition.

 Copyright (c) 2003-2013 The Regents of the University of California.
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

import java.awt.Component;

import ptolemy.domains.modal.kernel.FSMTransitionParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// TransitionEditorPaneFactory

/**
An editor for a transition. This editor is used by FSMTransitionParameters.

 @author Patricia Derler
 @version $Id: InsideEntityEditorPaneFactory.java 65763 2013-03-07 01:54:37Z cxh $
 @since Ptolemy II 4.0
 @Pt.ProposedRating Red (derler)
 @Pt.AcceptedRating Red (derler)
 */
public class TransitionEditorPaneFactory extends EditorPaneFactory {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TransitionEditorPaneFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to look for an entity contained by
     *  the container and return a configurer for that.
     *  @return A new widget for configuring the container.
     */
    public Component createEditorPane() {
        NamedObj object = getContainer();

        if (object instanceof FSMTransitionParameter &&
                ((FSMTransitionParameter)object).getTransition() != null) {
            return super.createEditorPane(((FSMTransitionParameter)object).getTransition());
        }

        return super.createEditorPane(object);
    }
}
