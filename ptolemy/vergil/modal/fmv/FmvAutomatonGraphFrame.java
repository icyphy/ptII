/* The graph frame for FMVAutomaton (FSM supporting verification using formal methods).

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
package ptolemy.vergil.modal.fmv;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JOptionPane;

import ptolemy.actor.gui.Tableau;
import ptolemy.domains.modal.kernel.fmv.FmvAutomaton;
import ptolemy.gui.ComponentDialog;
import ptolemy.gui.JFileChooserBugFix;
import ptolemy.gui.Query;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.LibraryAttribute;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.modal.FSMGraphFrame;
import ptolemy.vergil.modal.FSMGraphModel;
import ptolemy.verification.kernel.MathematicalModelConverter.FormulaType;
import diva.graph.GraphPane;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// FmvAutomatonGraphFrame

/**
 * This is a graph editor frame for Ptolemy FmvAutomaton models. Given a
 * composite entity and a tableau, it creates an editor and populates the menus
 * and toolbar. This overrides the base class to associate with the editor an
 * instance of FmvAutomatonGraphController.
 *
 * @deprecated ptolemy.de.lib.TimedDelay is deprecated, use ptolemy.actor.lib.TimeDelay.
 * @author Chihhong Patrick Cheng Contributor: Edward A. Lee
 * @version $Id: FmvAutomatonGraphFrame.java,v 1.9 2008/01/29 07:51:58 patrickj
 *          Exp $
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (patrickj)
 * @Pt.AcceptedRating Red ()
 */
@Deprecated
@SuppressWarnings("serial")
public class FmvAutomatonGraphFrame extends FSMGraphFrame {

    /**
     * Construct a frame associated with the specified model. After constructing
     * this, it is necessary to call setVisible(true) to make the frame appear.
     * This is typically done by calling show() on the controlling tableau. This
     * constructor results in a graph frame that obtains its library either from
     * the model (if it has one) or the default library defined in the
     * configuration.
     *
     * @see Tableau#show()
     * @param entity
     *                The model to put in this frame.
     * @param tableau
     *                The tableau responsible for this frame.
     */
    public FmvAutomatonGraphFrame(CompositeEntity entity, Tableau tableau) {
        this(entity, tableau, null);
    }

    /**
     * Construct a frame associated with the specified model. After constructing
     * this, it is necessary to call setVisible(true) to make the frame appear.
     * This is typically done by calling show() on the controlling tableau. This
     * constructor results in a graph frame that obtains its library either from
     * the model (if it has one), or the <i>defaultLibrary</i> argument (if it
     * is non-null), or the default library defined in the configuration.
     *
     * @see Tableau#show()
     * @param entity
     *                The model to put in this frame.
     * @param tableau
     *                The tableau responsible for this frame.
     * @param defaultLibrary
     *                An attribute specifying the default library to use if the
     *                model does not have a library.
     */
    public FmvAutomatonGraphFrame(CompositeEntity entity, Tableau tableau,
            LibraryAttribute defaultLibrary) {
        super(entity, tableau, defaultLibrary);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Create a new graph pane. Note that this method is called in constructor
     * of the base class, so it must be careful to not reference local variables
     * that may not have yet been created.
     *
     * @param entity
     *                The object to be displayed in the pane (which must be an
     *                instance of CompositeEntity).
     * @return The pane that is created.
     */
    @Override
    protected GraphPane _createGraphPane(NamedObj entity) {
        _controller = new FmvAutomatonGraphController(_directory);
        _controller.setConfiguration(getConfiguration());
        _controller.setFrame(this);

        // NOTE: The cast is safe because the constructor accepts
        // only CompositeEntity.

        final FSMGraphModel graphModel = new FSMGraphModel(
                (CompositeEntity) entity);
        return new GraphPane(_controller, graphModel);
    }

    /**
     * Create the menus that are used by this frame. It is essential that
     * _createGraphPane() be called before this.
     */
    @Override
    protected void _addMenus() {
        super._addMenus();

        _fmvMenu = new JMenu("Verification");
        _fmvMenu.setMnemonic(KeyEvent.VK_V);
        _menubar.add(_fmvMenu);

        GUIUtilities.addMenuItem(_fmvMenu, _translateSmvAction);
        GUIUtilities.addMenuItem(_fmvMenu, _invokeNuSMVAction);

        GUIUtilities.addToolBarButton(_toolbar, _invokeNuSMVAction);
        // diva.gui.GUIUtilities.addMenuItem(menu, _translateSmvAction);
        // diva.gui.GUIUtilities.addMenuItem(menu, _invokeNuSMVAction);
        // diva.gui.GUIUtilities.addToolBarButton(toolbar,_invokeNuSMVAction );

    }

    /** The case menu. */
    protected JMenu _fmvMenu;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // The action for composing with another Fmv automaton.
    private InvokeNuSMVAction _invokeNuSMVAction = new InvokeNuSMVAction();
    private TranslateSmvAction _translateSmvAction = new TranslateSmvAction();

    // The directory of the current model.
    private File _directory;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    ///////////////////////////////////////////////////////////////////
    // // TranslateSmvAction

    /** An action to perform format translation to .smv file. */
    public class TranslateSmvAction extends AbstractAction {
        /** Create a new action to perform composition. */
        public TranslateSmvAction() {
            super("Translate into .SMV file");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/modal/fmv/img/nusmv.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/modal/fmv/img/nusmv.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/modal/fmv/img/nusmv.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/modal/fmv/img/nusmv.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Translate into .SMV file");
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    Integer.valueOf(KeyEvent.VK_T));
        }

        /**
         * Compose with another Fmv automaton by first opening a file chooser
         * dialog and then composing with the specified model. The specified
         * model and the composition result are shown in two new Fmv automaton
         * graph frames.
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            Query query = new Query();
            query.addLine("formula", "Temporal formula", "");
            String[] possibleChoice = new String[2];
            possibleChoice[0] = "CTL";
            possibleChoice[1] = "LTL";
            query.addRadioButtons("choice", "Formula Type", possibleChoice,
                    "CTL");
            query.addLine("span", "Size of span", "0");
            ComponentDialog dialog = new ComponentDialog(null, "Input Formula",
                    query);

            String pattern = "";
            FormulaType finalChoice;
            int span = 0;
            if (dialog.buttonPressed().equals("OK")) {
                pattern = query.getStringValue("formula");
                String choice = query.getStringValue("choice");
                if (choice.equals("CTL")) {
                    finalChoice = FormulaType.CTL;
                } else {
                    finalChoice = FormulaType.LTL;
                }
                span = Integer.parseInt(query.getStringValue("span"));
                // Retrieve the Fmv Automaton
                FmvAutomaton model = (FmvAutomaton) getModel();
                // StringBuffer = model

                StringBuffer fmvFormat = new StringBuffer("");
                FileWriter smvFileWriter = null;
                try {

                    fmvFormat.append(model.convertToSMVFormat(pattern,
                            finalChoice, span));
                    // Avoid white boxes in file chooser, see
                    // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3801
                    JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
                    Color background = null;
                    try {
                        background = jFileChooserBugFix.saveBackground();
                        JFileChooser fileSaveDialog = new JFileChooser();
                        // SMVFileFilter filter = new SMVFileFilter();
                        // fileSaveDialog.setFileFilter(filter);
                        fileSaveDialog.setDialogType(JFileChooser.SAVE_DIALOG);
                        fileSaveDialog
                        .setDialogTitle("Convert Ptolemy model into .smv file");
                        if (_directory != null) {
                            fileSaveDialog.setCurrentDirectory(_directory);
                        } else {
                            // The default on Windows is to open at user.home, which
                            // is
                            // typically an absurd directory inside the O/S
                            // installation.
                            // So we use the current directory instead.
                            // FIXME: Could this throw a security exception in an
                            // applet?
                            String cwd = StringUtilities
                                    .getProperty("user.dir");

                            if (cwd != null) {
                                fileSaveDialog
                                .setCurrentDirectory(new File(cwd));
                            }
                        }

                        int returnValue = fileSaveDialog
                                .showOpenDialog(FmvAutomatonGraphFrame.this);

                        if (returnValue == JFileChooser.APPROVE_OPTION) {
                            _directory = fileSaveDialog.getCurrentDirectory();

                            File smvFile = fileSaveDialog.getSelectedFile()
                                    .getCanonicalFile();

                            if (smvFile.exists()) {
                                String queryString = "Overwrite "
                                        + smvFile.getName() + "?";
                                int selected = JOptionPane.showOptionDialog(
                                        null, queryString, "Overwrite?",
                                        JOptionPane.YES_NO_OPTION,
                                        JOptionPane.QUESTION_MESSAGE, null,
                                        null, null);
                                if (selected == 0) {
                                    try {
                                        smvFileWriter = new FileWriter(smvFile);
                                        smvFileWriter.write(fmvFormat
                                                .toString());
                                    } finally {
                                        if (smvFileWriter != null) {
                                            smvFileWriter.close();
                                        }
                                    }
                                }
                            }
                        }
                    } finally {
                        jFileChooserBugFix.restoreBackground(background);
                    }

                } catch (IOException ex) {
                    MessageHandler.error("IO exception:\n" + ex.getMessage());
                } catch (IllegalActionException ex) {
                    MessageHandler
                    .error("Failed to perform the conversion process:\n"
                            + ex.getMessage());
                }

                try {
                    if (smvFileWriter != null) {
                        smvFileWriter.close();
                    }
                } catch (Exception ex) {
                    MessageHandler
                    .error("Failed to perform the file closing process:\n"
                            + ex.getMessage());
                }

            }

        }

    }

    ///////////////////////////////////////////////////////////////////
    // // InvokeNuSMVAction

    /** An action to perform format translation to .smv file. */
    public class InvokeNuSMVAction extends AbstractAction {
        /** Create a new action to perform composition. */
        public InvokeNuSMVAction() {
            super("Invoke NuSMV");

            GUIUtilities.addIcons(this, new String[][] {
                    { "/ptolemy/vergil/modal/fmv/img/nusmv.gif",
                        GUIUtilities.LARGE_ICON },
                        { "/ptolemy/vergil/modal/fmv/img/nusmv.gif",
                            GUIUtilities.ROLLOVER_ICON },
                            { "/ptolemy/vergil/modal/fmv/img/nusmv.gif",
                                GUIUtilities.ROLLOVER_SELECTED_ICON },
                                { "/ptolemy/vergil/modal/fmv/img/nusmv.gif",
                                    GUIUtilities.SELECTED_ICON } });

            putValue("tooltip", "Invoke NuSMV");
            putValue(diva.gui.GUIUtilities.MNEMONIC_KEY,
                    Integer.valueOf(KeyEvent.VK_I));
        }

        /**
         * Choose the specified .smv file and later invoke NuSMV to perform
         * verification. Redirect the consol and show the result in a popout
         * window.
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            JFileChooser fileDialog = new JFileChooser();
            fileDialog
            .setDialogTitle("Select one .smv file to perform verification");
            if (_directory != null) {
                fileDialog.setCurrentDirectory(_directory);
            } else {
                // The default on Windows is to open at user.home, which is
                // typically an absurd directory inside the O/S installation.
                // So we use the current directory instead.
                // FIXME: Could this throw a security exception in an applet?
                String cwd = StringUtilities.getProperty("user.dir");

                if (cwd != null) {
                    fileDialog.setCurrentDirectory(new File(cwd));
                }
            }

            int returnValue = fileDialog
                    .showOpenDialog(FmvAutomatonGraphFrame.this);

            if (returnValue == JFileChooser.APPROVE_OPTION) {
                _directory = fileDialog.getCurrentDirectory();

                String filepath = fileDialog.getSelectedFile()
                        .getAbsolutePath();

                StringBuffer str = new StringBuffer("");
                BufferedReader reader = null;
                try {
                    Runtime rt = Runtime.getRuntime();
                    Process pr = rt.exec("NuSMV " + "\"" + filepath + "\"");
                    InputStreamReader inputStream = new InputStreamReader(
                            pr.getInputStream());
                    reader = new BufferedReader(inputStream);
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        str.append(line + "\n");
                    }

                } catch (IOException ex) {
                    MessageHandler.error("Failed to create debug listener: "
                            + ex);
                } finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (IOException ex) {
                        MessageHandler
                        .error("Failed to create debug listener: " + ex);
                    }
                }

                // StringBuffer str stores the information of the verification.
                Query query = new Query();
                query.setTextWidth(80);
                query.addTextArea("formula", "Verification Results",
                        str.toString());
                ComponentDialog dialog = new ComponentDialog(null, "Terminal",
                        query);

                // String pattern = "";
                if (dialog.buttonPressed().equals("OK")) {
                    // pattern = query.getStringValue("formula");

                }

            }

        }
    }

    ///////////////////////////////////////////////////////////////////
    // // SMVFileFilter
    /** A file filter that accepts files that end with ".smv". */
    protected static class SMVFileFilter extends
    javax.swing.filechooser.FileFilter {

        /**
         * Return true if the file name ends with ".smv".
         *
         * @param file
         *                The file to be checked.
         * @return true if the file is a directory or the file name, when
         *         converted to lower case, ends with ".smv".
         */
        @Override
        public boolean accept(File file) {
            return file.isDirectory()
                    || file.getName().toLowerCase(Locale.getDefault())
                    .endsWith(".smv");
        }

        /**
         * Return the description of this file filter.
         *
         * @return The description of this file filter.
         */
        @Override
        public String getDescription() {
            return "Software Model Verification (.smv) files";
        }
    }

}
