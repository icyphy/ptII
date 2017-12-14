/* An attribute that creates an editor to configure and run a code generator.

 Copyright (c) 2008-2014 The Regents of the University of California.
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
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.verification.kernel.MathematicalModelConverter;

///////////////////////////////////////////////////////////////////
//// MathematicalModelConverterGUIFactory

/**
 This is an attribute that creates an editor for configuring and
 running a code generator.  This is designed to be contained by
 an instance of CodeGenerator or a subclass of CodeGenerator.
 It customizes the user interface for "configuring" the code
 generator. This UI will be invoked when you double click on the
 code generator.

 @deprecated ptolemy.de.lib.TimedDelay is deprecated, use ptolemy.actor.lib.TimeDelay.
 @author Chihhong Patrick Cheng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (patrickj)
 @Pt.AcceptedRating Red ()
 */
@Deprecated
public class MathematicalModelConverterGUIFactory extends EditorFactory {
    /** Construct a factory with the specified container and name.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public MathematicalModelConverterGUIFactory(NamedObj container, String name)
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
    @Override
    public void createEditor(NamedObj object, Frame parent) {
        // This is always used to configure the container, so
        // we just use that.

        MathematicalModelConverter modelConverter = (MathematicalModelConverter) getContainer();

        Effigy effigy = parent == null ? null
                : ((TableauFrame) parent).getEffigy();

        Tableau tableau;
        try {
            if (effigy == null) {
                tableau = new Tableau(workspace());
                tableau.setName("MathematicalModelConverterGUI");
                tableau.setTitle("MathematicalModelConverterGUI");
            } else {
                tableau = (Tableau) effigy
                        .getEntity("MathematicalModelConverterGUI");
                if (tableau == null) {
                    tableau = new Tableau(effigy,
                            "MathematicalModelConverterGUI");
                }
            }
        } catch (KernelException e) {
            throw new InternalErrorException(e);
        }

        Frame frame = tableau.getFrame();

        if (frame == null) {
            try {
                frame = new MathematicalModelConverterGUI(modelConverter,
                        tableau);
            } catch (KernelException e) {
                throw new InternalErrorException(e);
            }
        }

        // Show the result.
        frame.pack();
        //frame.setSize(800, 350);
        //frame.setResizable(false);
        //frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }
}
