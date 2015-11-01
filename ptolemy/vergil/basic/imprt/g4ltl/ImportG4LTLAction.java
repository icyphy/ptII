/* Implement the Import G4LTL menu choice.

   Copyright (c) 2013-2014 The Regents of the University of California.
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
   COPYRIGHTENDKEY 2
 */

package ptolemy.vergil.basic.imprt.g4ltl;

import g4ltl.SolverUtility;
import g4ltl.Version;
import g4ltl.utility.ResultLTLSynthesis;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ptolemy.gui.JFileChooserBugFix;
import ptolemy.gui.PtFileChooser;
import ptolemy.gui.Top;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.vergil.basic.BasicGraphFrame;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// ImportG4LTLAction

/**
   Import an FSMActor using LTL synthesis (G4LTL).

   <p>"G4LTL is a standalone tool and a Java library for automatically
   generating controllers realizing linear temporal logic (LTL).</p>

   <p>See <a href="http://www6.in.tum.de/~chengch/g4ltl/#in_browser">http://www6.in.tum.de/~chengch/g4ltl/</a></p>

   <p>This class uses classes defined in $PTII/lib/g4ltl.jar.  See
   $PTII/lib/g4ltl-license.htm.</p>

   <p>This package is optional.  To add the "Import G4LTL" menu choice
   to the GraphEditor, add the following to the configuration:</p>
   <pre>
   &lt;property name="_importActionClassNames"
   class="ptolemy.data.expr.Parameter"
   value="{&amp;quot;&quot;ptolemy.vergil.basic.imprt.g4ltl.ImportG4LTLAction&amp;quot;}"/&gt;
   </pre>
   <p>{@link ptolemy.vergil.basic.BasicGraphFrame} checks for this
   parameter and adds the "Import G4LTL" menu choice if the class named
   by that parameter exists.</p>

   <p>The <code>$PTII/ptolemy/configs/defaultFullConfiguration.xml</code> file
   already has this parameter.  The ptiny configuration does <b>not</b> have
   this parameter so that we have a smaller download.</p>

   @author Chihhong (Patrick) Cheng (Fortiss), Christopher Brooks. Based on ExportPDFAction by Edward A. Lee
   @version $Id$
   @since Ptolemy II 10.0
   @Pt.ProposedRating Red (cxh)
   @Pt.AcceptedRating Red (cxh)
 */
@SuppressWarnings("serial")
public class ImportG4LTLAction extends AbstractAction {
    // This package is called "imprt" because "import" is a Java keyword.

    /** Create a new action to import an FSMActor from LTL synthesis.
     *  @param frame  The frame to which the import action is to be added.
     */
    public ImportG4LTLAction(Top frame) {
        super("Import FSMActor using synthesis");
        _frame = frame;
        putValue("tooltip", "Import an FSMActor using LTL synthesis (G4LTL)");
        putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_M));
    }

    /**
     * Import a library by first opening a file chooser dialog and then
     * importing the specified library.
     * See {@link ptolemy.actor.gui.UserActorLibrary#openLibrary(ptolemy.actor.gui.Configuration, File)}
     * for information on the file format.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        _importFromLTLSynthesis();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Import the design by invoking LTL synthesis via G4LTL.
     */
    private void _importFromLTLSynthesis() {
        JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
        Color background = null;
        PtFileChooser ptFileChooser = null;
        ImageIcon icon = new ImageIcon(
                "ptolemy/vergil/basic/imprt/g4ltl/G4LTL.gif");

        try {
            Class basicGraphFrameClass = null;
            try {
                basicGraphFrameClass = Class
                        .forName("ptolemy.vergil.basic.BasicGraphFrame");
            } catch (Throwable throwable) {
                throw new InternalErrorException(null, throwable,
                        "Could not find ptolemy.vergil.basic.BasicGraphFrame?");
            }
            if (basicGraphFrameClass == null) {
                throw new InternalErrorException(null, null,
                        "Could not find ptolemy.vergil.basic.BasicGraphFrame!");
            } else if (!basicGraphFrameClass.isInstance(_frame)) {
                throw new InternalErrorException("Frame " + _frame
                        + " is not a BasicGraphFrame?");
            }

            BasicGraphFrame basicGraphFrame = (BasicGraphFrame) _frame;

            background = jFileChooserBugFix.saveBackground();
            ptFileChooser = new PtFileChooser(null,
                    "Select a design specification file.",
                    JFileChooser.OPEN_DIALOG);

            ptFileChooser.setCurrentDirectory(basicGraphFrame
                    .getLastDirectory());

            int returnVal = ptFileChooser.showDialog(null, "Import");

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                basicGraphFrame.setLastDirectory(ptFileChooser
                        .getCurrentDirectory());

                File file = null;
                ResultLTLSynthesis result = null;
                try {
                    file = ptFileChooser.getSelectedFile().getCanonicalFile();

                    // Step 1: Invoke the synthesis engine to generate a string in MoML format.

                    SolverUtility solver = new SolverUtility();

                    Object[] optionsTechnique = { "CoBuechi", "Buechi" };

                    int optionTechnique = JOptionPane.showOptionDialog(null,
                            "Specify synthesis techniques", "G4LTL@Ptolemy II",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, icon,
                            optionsTechnique, optionsTechnique[0]);

                    int unrollSteps = 0;
                    if (optionTechnique == 0) {
                        Object[] possibilities = { "1", "2", "3", "4", "5", "6" };
                        String s = (String) JOptionPane.showInputDialog(null,
                                "Enter the number of unroll steps",
                                "G4LTL@Ptolemy II", JOptionPane.PLAIN_MESSAGE,
                                icon, possibilities, "1");
                        if (s != null && s.length() > 0) {
                            unrollSteps = Integer.parseInt(s);
                        }
                    }

                    result = G4LTL.synthesizeFromFile(solver, file,
                            optionTechnique, unrollSteps, true);

                    if (result.isStrategyFound() == false) {
                        // Try to see if a counter-strategy exists

                        Object[] options = { "Yes", "No" };
                        int option = JOptionPane.showOptionDialog(null,
                                "G4LTL unable to find strategy.\n"
                                        + "Perform counter-strategy finding?",
                                        "G4LTL@Ptolemy II", JOptionPane.YES_NO_OPTION,
                                        JOptionPane.PLAIN_MESSAGE, icon, options,
                                        options[0]);

                        if (option == 0) {
                            result = G4LTL.synthesizeFromFile(solver, file,
                                    optionTechnique, unrollSteps, false);
                        }

                        JOptionPane.showMessageDialog(null,
                                result.getMessage1(), "G4LTL@Ptolemy II",
                                JOptionPane.DEFAULT_OPTION, icon);
                        return;
                    } else {
                        if (!Version.BSD_VERSION) {
                            if (!result.getMessage2().equals("")) {
                                // Generate the multiplexer
                                G4LTL.updateModel(result.getMessage2(),
                                        basicGraphFrame.getModel());
                            }
                        }
                    }
                    G4LTL.updateModel(result.getMessage1(),
                            basicGraphFrame.getModel());
                } catch (Exception ex) {
                    basicGraphFrame.report(new IllegalActionException(
                            basicGraphFrame.getModel(), ex,
                            "Error reading input file \"" + file + "\"."));
                }
            }
        } finally {
            jFileChooserBugFix.restoreBackground(background);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private variables

    /** The top-level window of the contents to be exported. */
    Top _frame;
}
