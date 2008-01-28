/* An attribute that creates an editor to configure and run a code generator.

 Copyright (c) 2006 The Regents of the University of California.
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
package ptolemy.verification.gui;

import java.awt.Frame;

import ptolemy.actor.gui.EditorFactory;

import ptolemy.util.MessageHandler;
import ptolemy.verification.kernel.CodeGenerator;

import ptolemy.kernel.util.IllegalActionException;

import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// CodeGeneratorGUIFactory

/**
 This is an attribute that creates an editor for configuring and
 running a code generator.  This is designed to be contained by
 an instance of CodeGenerator or a subclass of CodeGenerator.
 It customizes the user interface for "configuring" the code
 generator. This UI will be invoked when you double click on the
 code generator.

 @author Chihhong Patrick Cheng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (patrickj)
 @Pt.AcceptedRating Red ()
 */
public class CodeGeneratorGUIFactory extends EditorFactory {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public CodeGeneratorGUIFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create an editor for configuring the specified object with the
     *  specified parent window.
     *  @param object The object to configure.
     *  @param parent The parent window, or null if there is none.
     */
    public void createEditor(NamedObj object, Frame parent) {
        // This is always used to configure the container, so
        // we just use that.
        CodeGenerator codeGenerator = (CodeGenerator) getContainer();
        try {
            int a = codeGenerator.generateCode();
        } catch (Exception ex) {
            MessageHandler
                    .error("Error: CodeGeneratorGUIFactory.createEditor() clashes: \n"
                            + ex.getMessage());
        }

    }

    // FIXME: Check that the container is an instance of CodeGenerator.
}
