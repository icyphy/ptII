/*
@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptdb.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.IllegalNameException;
import ptdb.common.exception.SearchCriteriaParseException;
import ptdb.common.util.Utilities;
import ptdb.kernel.bl.search.SearchCriteriaManager;
import ptdb.kernel.bl.search.SearchManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.actor.gt.MalformedStringException;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// SimpleSearchFrame

/**
 * An extended JFrame used for performing a simple database search based on
 * the model name and attributes.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 *
 */
@SuppressWarnings("serial")
public class SimpleSearchFrame extends JFrame implements PTDBBasicFrame {

    /**
     * Construct a SimpleSearchFrame. Add swing Components to the frame. Add
     * listeners for the "Search" and "Cancel" buttons.
     *
     * @param model
     *      The model into which search results would be imported.
     * @param frame
     *      The editing frame from which the simple search window will
     *      open.
     * @param configuration
     *      The configuration under which models from the database will
     *      be loaded.
     * @param tableau The tableau of the window that opens this frame.
     *
     */
    public SimpleSearchFrame(NamedObj model, JFrame frame,
            Configuration configuration, Tableau tableau) {

        super("Search Database");

        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        _containerModel = model;

        _sourceFrame = frame;
        _configuration = configuration;

        _attributesListPanel = new AttributesSearchListPanel(new NamedObj());
        _saveLocation = null;
        _searchCriteria = new SearchCriteria();
        _baseTableau = tableau;

        _initSimpleSearchFrame();
        _addMenus();

        _topPanel = new JPanel();
        _bottomPanel = new JPanel();

        _attributesListPanel.setAlignmentX(LEFT_ALIGNMENT);
        _topPanel.setAlignmentX(LEFT_ALIGNMENT);
        _bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        _attributesListPanel.setAlignmentY(TOP_ALIGNMENT);
        _topPanel.setAlignmentY(TOP_ALIGNMENT);
        _bottomPanel.setAlignmentY(TOP_ALIGNMENT);

        _topPanel.setLayout(new BoxLayout(_topPanel, BoxLayout.Y_AXIS));

        _topPanel.setBorder(BorderFactory.createEmptyBorder());

        JButton cancel_Button;
        JButton advancedSearchButton;

        _searchButton = new JButton("Search");
        cancel_Button = new JButton("Cancel");
        advancedSearchButton = new JButton("Add Graph Pattern Search");

        _searchButton.setMnemonic(KeyEvent.VK_ENTER);
        cancel_Button.setMnemonic(KeyEvent.VK_ESCAPE);

        _searchButton.setActionCommand("Search");
        cancel_Button.setActionCommand("Cancel");
        advancedSearchButton.setActionCommand("Add Graph Pattern Search");

        _searchButton.setHorizontalTextPosition(SwingConstants.CENTER);
        cancel_Button.setHorizontalTextPosition(SwingConstants.CENTER);

        advancedSearchButton.setHorizontalAlignment(SwingConstants.RIGHT);

        _searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                try {

                    // If the form is in an invalid state, do not continue;
                    if (!_isValid()) {

                        return;

                    }

                    _simpleSearch();

                } catch (NameDuplicationException e) {

                    MessageHandler.error("The search cannot be performed now "
                            + "due to a NameDuplicationException.", e);

                } catch (IllegalActionException e) {

                    MessageHandler.error("The search cannot be performed now "
                            + "due to an IllegalActionException.", e);

                } catch (DBConnectionException e) {

                    MessageHandler.error("The search cannot be performed now "
                            + "due to a DBConnectionException.", e);

                } catch (DBExecutionException e) {

                    MessageHandler.error("The search cannot be performed now "
                            + "due to a DBExecutionException.", e);

                } catch (MalformedStringException e) {
                    MessageHandler.error("The search cannot be performed now "
                            + "due to a MalformedStringException.", e);
                }

            }
        });

        cancel_Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                //                setVisible(false);
                if (_patternMatchframe != null) {
                    _patternMatchframe.dispose();
                }

                dispose();
            }

        });

        advancedSearchButton
        .addActionListener(new OpenPatternSearchFrameAction(tableau));

        addWindowListener(new WindowListener() {

            @Override
            public void windowOpened(WindowEvent e) {
                // Do nothing.
            }

            @Override
            public void windowIconified(WindowEvent e) {
                // Do nothing.
            }

            @Override
            public void windowDeiconified(WindowEvent e) {
                // Do nothing.
            }

            @Override
            public void windowDeactivated(WindowEvent e) {
                // Do nothing.
            }

            @Override
            public void windowClosing(WindowEvent e) {

                closeFrame();

                //                if (_patternMatchframe != null) {
                //                    _patternMatchframe.dispose();
                //                }
                //
                //                dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {

                closeFrame();
                //
                //                if (_patternMatchframe != null) {
                //                    _patternMatchframe.dispose();
                //                }
                //
                //                dispose();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // Do nothing.
            }
        });

        // Add the action listener to model name text field.
        _attributesListPanel.getNameTextField().addKeyListener(
                new KeyListener() {

                    @Override
                    public void keyTyped(KeyEvent e) {
                        // Do nothing.
                    }

                    @Override
                    public void keyReleased(KeyEvent e) {
                        // Do nothing.
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {

                        /*
                         * If the enter button is pressed, perform the search
                         *  action.
                         */
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {

                            _searchButton.getActionListeners()[0]
                                    .actionPerformed(null);
                        }
                    }
                });

        _topPanel.add(_attributesListPanel);
        _bottomPanel.add(_searchButton);
        _bottomPanel.add(cancel_Button);
        _bottomPanel.add(advancedSearchButton);
        add(_topPanel);
        add(_bottomPanel);

        validate();
        repaint();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Perform the action of clicking the search button on this frame.
     *
     * @param event The ActionEvent for this click.
     */
    public void clickSearchButton(ActionEvent event) {
        _searchButton.doClick();
    }

    /**
     * Close this frame.
     */

    @Override
    public void closeFrame() {

        _ptdbContainedFramesManager.closeContainedFrames();

        dispose();
    }

    /** Get an indication if the panel has been modified.
     *  True if it has, false if it hasn't.
     *
     * @return
     *         An indication if the panel has been modified.
     *
     * @see #setModified(boolean)
     *
     */
    public boolean isModified() {

        if (_patternMatchframe != null) {
            return _attributesListPanel.isModified()
                    || _patternMatchframe.isModified();
        } else {
            return _attributesListPanel.isModified();
        }

    }

    /** Set the panel to modified or unmodified.
     *
     * @param modified True to set to modified.  False to set to unmodified.
     *
     * @see #isModified
     *
     */
    public void setModified(boolean modified) {

        _attributesListPanel.setModified(modified);
        if (_patternMatchframe != null) {
            _patternMatchframe.setModified(modified);
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /**
     * Add menus to the menu bar.
     */
    protected void _addMenus() {

        setJMenuBar(_menuBar);

        _fileMenu = new JMenu("File");
        _fileMenu.setMnemonic(KeyEvent.VK_F);
        _menuBar.add(_fileMenu);

        GUIUtilities.addHotKey(getRootPane(), _newSearchCriteriaAction);
        GUIUtilities.addMenuItem(_fileMenu, _newSearchCriteriaAction);

        GUIUtilities.addHotKey(createRootPane(), _openSearchCriteriaAction);
        GUIUtilities.addMenuItem(_fileMenu, _openSearchCriteriaAction);

        GUIUtilities.addHotKey(createRootPane(), _saveSearchCriteriaAction);
        GUIUtilities.addMenuItem(_fileMenu, _saveSearchCriteriaAction);

        GUIUtilities.addHotKey(createRootPane(), _saveAsSearchCriteriaAction);
        GUIUtilities.addMenuItem(_fileMenu, _saveAsSearchCriteriaAction);

        GUIUtilities.addHotKey(getRootPane(), _exitSearchCriteriaAction);
        GUIUtilities.addMenuItem(_fileMenu, _exitSearchCriteriaAction);

    }

    /**
     * Exit the window.  If the search criteria was changed, give the user
     * an opportunity to save it.
     */
    protected void _exit() {

        if (isModified()) {

            Object[] options = { "Yes", "No", "Cancel" };
            int n = JOptionPane.showOptionDialog(this,
                    "Would you like to save the search criteria? ",
                    "Save Search Criteria?", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

            if (n == JOptionPane.YES_OPTION) {

                _save();

            } else if (n == JOptionPane.CANCEL_OPTION) {

                return;

            }
        }

        dispose();

    }

    /**
     * Establish all event listeners.
     */
    protected void _initSimpleSearchFrame() {

        _newSearchCriteriaAction = new NewSearchCriteriaAction();
        _openSearchCriteriaAction = new OpenSearchCriteriaAction();
        _saveSearchCriteriaAction = new SaveSearchCriteriaAction();
        _saveAsSearchCriteriaAction = new SaveAsSearchCriteriaAction();
        _exitSearchCriteriaAction = new ExitSearchCriteriaAction();

    }

    /**
     * Create new search criteria.  If the currently opened search criteria is
     * changed, give the user an opportunity to save it.
     */
    protected void _new() {

        if (isModified()) {

            Object[] options = { "Yes", "No", "Cancel" };
            int n = JOptionPane.showOptionDialog(this,
                    "Would you like to save the search criteria? ",
                    "Save Search Criteria?", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

            if (n == JOptionPane.YES_OPTION) {

                _save();

            } else if (n == JOptionPane.CANCEL_OPTION) {

                return;

            }
        }

        _topPanel.removeAll();
        _attributesListPanel = new AttributesListPanel(new NamedObj());
        _topPanel.add(_attributesListPanel);
        validate();
        repaint();

        // Renew the pattern search frame.
        if (_patternMatchframe != null) {
            _patternMatchframe.dispose();
            _patternMatchframe = null;
        }

        _saveLocation = null;
        _searchCriteria = new SearchCriteria();
        setModified(false);

    }

    /**
     * Open a previously saved search criteria file.  If the currently opened
     * search criteria was changed, give the user an opportunity to save it.
     */
    protected void _open() {

        JFileChooser chooser = new JFileChooser();

        FileFilter filter = new SearchCriteriaFileFilter(
                "Model Search Criteria (*.xml)");
        chooser.setFileFilter(filter);
        chooser.setDialogTitle("Open");

        int returnVal = chooser.showOpenDialog(this);

        if (returnVal == JFileChooser.APPROVE_OPTION) {

            try {

                if (isModified()) {

                    Object[] options = { "Yes", "No", "Cancel" };
                    int n = JOptionPane.showOptionDialog(this,
                            "Would you like to save the search criteria? ",
                            "Save Search Criteria?",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[2]);

                    if (n == JOptionPane.YES_OPTION) {

                        _save();

                    } else if (n == JOptionPane.CANCEL_OPTION) {

                        return;

                    }
                }

                try {

                    _searchCriteria = SearchCriteriaManager.open(chooser
                            .getSelectedFile().getCanonicalPath(),
                            _configuration);

                } catch (SearchCriteriaParseException e1) {

                    MessageHandler.error(
                            "Cannot retrieve the search criteria.", e1);

                }

                _topPanel.removeAll();
                _attributesListPanel = new AttributesListPanel(new NamedObj());
                _topPanel.add(_attributesListPanel);
                validate();
                repaint();

                if (_searchCriteria.getModelName() != null) {
                    _attributesListPanel.setModelName(_searchCriteria
                            .getModelName());
                }

                if (_searchCriteria.getAttributes() != null) {

                    for (Attribute attribute : _searchCriteria.getAttributes()) {
                        _attributesListPanel
                        .addAttribute((StringParameter) attribute);
                    }

                }

                if (_searchCriteria.getPatternMoML() != null) {
                    // Open the pattern model in the advanced search frame.

                    if (_patternMatchframe != null) {
                        // Close the previous opened pattern search frame.
                        _patternMatchframe.dispose();
                        _patternMatchframe = null;
                    }

                    URL toRead = getClass().getClassLoader().getResource(
                            "ptolemy.actor.gt.controller.Match");

                    try {

                        EffigyFactory effigyFactory = new EffigyFactory(
                                _configuration.workspace());

                        PtolemyEffigy.Factory ptolemyEffigyFactory = new PtolemyEffigy.Factory(
                                effigyFactory, "new effigy factory");

                        PtolemyEffigy effigy = (PtolemyEffigy) ptolemyEffigyFactory
                                .createEffigy(_configuration.getDirectory(),
                                        null, toRead);

                        CompositeEntity compositeEntity = new TransformationRule(
                                effigy, "DB Search Pattern");

                        _patternMatchframe = new GraphPatternSearchEditor(
                                compositeEntity, new Tableau(effigy,
                                        "DBSearchframe"),
                                        ((ActorGraphDBTableau) _baseTableau)
                                        .getGtLibrary(), _containerModel,
                                        _sourceFrame, SimpleSearchFrame.this);

                        String moml = _searchCriteria.getPatternMoML();

                        _patternMatchframe.updatePattern(moml);

                        if (!_patternMatchframe.isPatternEmpty()) {
                            _patternMatchframe.repaint();

                            _patternMatchframe.pack();
                            _patternMatchframe
                            .setLocationRelativeTo(SimpleSearchFrame.this);
                            _patternMatchframe.setVisible(true);
                        }

                    } catch (Exception e2) {

                        MessageHandler.error(
                                "Failed to open pattern match search editor.",
                                e2);
                    }

                }

                _saveLocation = chooser.getSelectedFile().getCanonicalPath();
                setModified(false);

            } catch (IOException e) {

                MessageHandler.error("Cannot read from the selected file.", e);

            }
        }

    }

    /**
     * Save the currently opened search criteria.
     */
    protected void _save() {

        if (_saveLocation == null) {

            _saveAs();

        } else {
            _searchCriteria = new SearchCriteria();

            try {

                if (!_attributesListPanel.getModelName().trim().isEmpty()) {

                    _searchCriteria.setModelName(_attributesListPanel
                            .getModelName());
                }

                if (_attributesListPanel.getAttributeCount() > 0) {

                    ArrayList<Attribute> attributesToSearch = _attributesListPanel
                            .getAttributes();
                    _searchCriteria.setAttributes(attributesToSearch);
                }

                // Fetch the search criteria from the pattern match window.
                if (_patternMatchframe != null
                        && !_patternMatchframe.isPatternEmpty()) {

                    _searchCriteria.setPatternMoML(_patternMatchframe
                            .getPatternMoML());
                }

                SearchCriteriaManager.save(_searchCriteria, _saveLocation);

                setModified(false);

                JOptionPane.showMessageDialog(this,
                        "Search Criteria is saved successfully!");

            } catch (IllegalActionException e) {

                MessageHandler.error("Cannot save the search criteria.", e);

            } catch (IOException e) {

                MessageHandler.error("Cannot save the search criteria.", e);

            }

        }

    }

    /**
     * Save the currently opened search criteria to a new location.
     */
    protected void _saveAs() {

        try {
            _searchCriteria = new SearchCriteria();

            if (!_attributesListPanel.getModelName().trim().isEmpty()) {

                _searchCriteria.setModelName(_attributesListPanel
                        .getModelName());
            }

            if (_attributesListPanel.getAttributeCount() > 0) {

                ArrayList<Attribute> attributesToSearch = _attributesListPanel
                        .getAttributes();
                _searchCriteria.setAttributes(attributesToSearch);
            }

            // Fetch the search criteria from the pattern match window.
            if (_patternMatchframe != null
                    && !_patternMatchframe.isPatternEmpty()) {

                _searchCriteria.setPatternMoML(_patternMatchframe
                        .getPatternMoML());
            }

            JFileChooser chooser = new JFileChooser();

            FileFilter filter = new SearchCriteriaFileFilter(
                    "Model Search Criteria (*.xml)");
            chooser.setFileFilter(filter);
            chooser.setDialogTitle("Save As");

            if (_saveLocation != null) {

                chooser.setSelectedFile(new File(_saveLocation));

            }

            boolean saveComplete = false;

            while (!saveComplete) {

                if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) {

                    return;

                } else {

                    File filename = chooser.getSelectedFile();
                    String name = filename.getName();
                    if (!name.endsWith(".xml")) {

                        filename = new File(filename.getParent(), name + ".xml");

                    }

                    if (filename.exists()
                            && !filename.getCanonicalFile().toString()
                            .equals(_saveLocation)) {

                        Object[] options = { "Yes", "No" };
                        int n = JOptionPane.showOptionDialog(this,
                                filename.toString() + " already exists.\n"
                                        + "Do you want to replace it?",
                                        "Overwrite File?", JOptionPane.YES_NO_OPTION,
                                        JOptionPane.WARNING_MESSAGE, null, options,
                                        options[1]);

                        if (n == JOptionPane.YES_OPTION) {

                            saveComplete = true;

                        } else {

                            saveComplete = false;

                        }

                    } else {

                        saveComplete = true;

                    }
                    if (saveComplete) {

                        try {

                            SearchCriteriaManager.save(_searchCriteria,
                                    filename.getCanonicalPath());

                            _saveLocation = chooser.getSelectedFile()
                                    .getCanonicalPath();

                            setModified(false);

                            JOptionPane.showMessageDialog(this,
                                    "Search Criteria is saved successfully!");

                        } catch (IllegalActionException e) {

                            MessageHandler.error(
                                    "Cannot save the search criteria.", e);

                        } catch (IOException e) {

                            MessageHandler.error(
                                    "Cannot save the search criteria.", e);

                        }

                    }
                }
            }

        } catch (IOException e) {

            MessageHandler.error("Cannot save to the selected file.", e);

        } catch (IllegalActionException e) {

            MessageHandler.error("Cannot save to the selected file.", e);
            //        } catch (NameDuplicationException e) {
            //            MessageHandler.error("Cannot save to the selected file.", e);
            //        } catch (MalformedStringException e) {
            //            MessageHandler.error("Cannot save to the selected file.", e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The file menu for this frame. */
    protected JMenu _fileMenu;

    /** The menu bar for this frame. */
    protected JMenuBar _menuBar = new JMenuBar();

    /** The location where the currently opened search criteria is saved. */
    protected String _saveLocation;

    /** The action for opening search criteria. */
    protected Action _openSearchCriteriaAction;

    /** The action for saving search criteria. */
    protected Action _saveSearchCriteriaAction;

    /** The action for saving search criteria to a new location. */
    protected Action _saveAsSearchCriteriaAction;

    /** The action for exiting the frame. */
    protected Action _exitSearchCriteriaAction;

    /** The action for creating new search criteria. */
    protected Action _newSearchCriteriaAction;

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /**
     * Validate whether the entire search criteria is enough. In order to
     * narrow down the search, at least one of the attribute, model name,
     * port, or component entity search criteria needs to be set in the
     * search criteria.
     *
     * @param searchCriteria The search criteria to be check.
     * @return true - the search criteria input by the user is enough.<br>
     *          false - the search criteria input by the user is not enough.
     */
    private boolean _isSearchCriteriaEnough(SearchCriteria searchCriteria) {

        if ((searchCriteria.getAttributes() == null || searchCriteria
                .getAttributes().size() == 0)

                && (searchCriteria.getDBGraphSearchCriteria() == null || (searchCriteria
                        .getDBGraphSearchCriteria().getPortsList() == null || searchCriteria
                        .getDBGraphSearchCriteria().getPortsList().isEmpty())
                        && (searchCriteria.getDBGraphSearchCriteria()
                                .getComponentEntitiesList() == null || searchCriteria
                                .getDBGraphSearchCriteria()
                                .getComponentEntitiesList().isEmpty()))

                                && (searchCriteria.getModelName() == null || searchCriteria
                                .getModelName().trim().isEmpty())) {

            return false;
        } else {
            return true;
        }

    }

    /**
     * Validate whether the search criteria put in this simple search frame
     * is valid or not. These search criteria includes the model name and
     * attributes.
     *
     * @return true - if the search criteria are valid.<br>
     *          false - if the search criteria are invalid.
     */
    private boolean _isValid() {

        if (_attributesListPanel.getAttributeCount() == 0
                && _attributesListPanel.getModelName().trim().isEmpty()) {
            //
            //            JOptionPane.showMessageDialog(this,
            //                    "You must enter the Model Name or "
            //                            + "select attributes on which to search.",
            //                    "Search Error", JOptionPane.INFORMATION_MESSAGE, null);
            //
            //            return false;

            // As long as there is the pattern matching criteria, this situation
            // can also be possible.
            return true;

        }

        if (_attributesListPanel.getModelName().length() > 0) {
            try {
                Utilities.checkModelName(_attributesListPanel.getModelName());
            } catch (IllegalNameException e) {
                JOptionPane.showMessageDialog(this,
                        "The model name should only "
                                + "contain letters and numbers.",
                                "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

                return false;
            }

        }

        //Duplicates are now allowed.  This allows OR searches.
        /*
        if (_attributesListPanel.containsDuplicates()) {

            JOptionPane.showMessageDialog(this,
                    "The search criteria cannot contain more"
                            + " than one instance " + "of the same attribute.",
                    "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }
         */

        if (!_attributesListPanel.allAttributeNamesSet()) {

            JOptionPane.showMessageDialog(this,
                    "You must specify a name for all attributes.",
                    "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        if (!_attributesListPanel.allAttributeValuesSet()) {

            JOptionPane.showMessageDialog(this,
                    "You must specify a value for all attributes.",
                    "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        return true;
    }

    private void _simpleSearch() throws DBConnectionException,
    DBExecutionException, NameDuplicationException,
    IllegalActionException, MalformedStringException {

        _attributesListPanel.regroup();

        final SearchCriteria searchCriteria = new SearchCriteria();

        if (!_attributesListPanel.getModelName().trim().isEmpty()) {

            searchCriteria.setModelName(_attributesListPanel.getModelName());
        }

        if (_attributesListPanel.getAttributeCount() > 0) {

            ArrayList<Attribute> attributesToSearch = _attributesListPanel
                    .getAttributes();
            searchCriteria.setAttributes(attributesToSearch);
        }

        // Fetch the search criteria from the pattern match window.
        if (_patternMatchframe != null) {
            _patternMatchframe.fetchSearchCriteria(searchCriteria);
        }

        // Validate whether the search criteria is enough.
        if (_isSearchCriteriaEnough(searchCriteria)) {

            final SearchResultsFrame searchResultsFrame = new SearchResultsFrame(
                    _containerModel, _sourceFrame, _configuration);

            final SearchResultBuffer searchResultBuffer = new SearchResultBuffer();

            _ptdbContainedFramesManager.addContainedFrame(searchResultsFrame);

            // Register the search result frame as the observer of the search
            // result buffer. Once there is any update in the buffer, the
            // search result frame will be notified.
            searchResultBuffer.addObserver(searchResultsFrame);

            // Register the search result buffer as the observer of the search
            // result frame. Once the user invokes the canceling search in the
            // search result frame, the buffer will be notified and stop.
            searchResultsFrame.registerCancelObserver(searchResultBuffer);

            // Show the search result frame.
            searchResultsFrame.pack();
            searchResultsFrame.setLocationRelativeTo(this);
            searchResultsFrame.setVisible(true);

            new Thread(new Runnable() {

                @Override
                public void run() {
                    // Call the Search Manager to trigger the search.
                    SearchManager searchManager = new SearchManager();
                    try {
                        searchManager
                        .search(searchCriteria, searchResultBuffer);

                    } catch (DBConnectionException e1) {
                        searchResultsFrame.setVisible(false);
                        searchResultsFrame.dispose();
                        MessageHandler.error("Cannot perform the search now.",
                                e1);

                    } catch (DBExecutionException e2) {
                        searchResultsFrame.setVisible(false);
                        searchResultsFrame.dispose();
                        MessageHandler.error("Cannot perform the search now.",
                                e2);
                    }
                }
            }).start();

        } else {
            JOptionPane.showMessageDialog(this,
                    "In order to narrow the search, please specify "
                            + "search criteria.  At least one of "
                            + "attribute, model name, port or" + " component"
                            + " entity needs to be set in the search "
                            + "criteria.", "Not Enough Search Criteria",
                            JOptionPane.INFORMATION_MESSAGE, null);

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private AttributesListPanel _attributesListPanel;
    private Configuration _configuration;
    private NamedObj _containerModel;
    private GraphPatternSearchEditor _patternMatchframe;
    private JButton _searchButton;
    private JFrame _sourceFrame;
    private SearchCriteria _searchCriteria;
    private JPanel _topPanel = new JPanel();
    private JPanel _bottomPanel = new JPanel();
    private Tableau _baseTableau;
    private PTDBContainedFramesManager _ptdbContainedFramesManager = new PTDBContainedFramesManager();

    ///////////////////////////////////////////////////////////////////
    //// OpenPatternSearchFrameAction

    private class OpenPatternSearchFrameAction extends AbstractAction {

        public OpenPatternSearchFrameAction(Tableau tableau) {
            super("Pattern Search");
            _tableau = tableau;
            //            putValue("tooltip", "Pattern Search");
            //            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_P));
        }

        ///////////////////////////////////////////////////////////////
        ////            public methods                          //////

        @Override
        public void actionPerformed(ActionEvent e) {

            if (_patternMatchframe == null) {
                URL toRead = getClass().getClassLoader().getResource(
                        "ptolemy.actor.gt.controller.Match");

                try {

                    EffigyFactory effigyFactory = new EffigyFactory(
                            _configuration.workspace());

                    PtolemyEffigy.Factory ptolemyEffigyFactory = new PtolemyEffigy.Factory(
                            effigyFactory, "new effigy factory");

                    Effigy effigy = ptolemyEffigyFactory.createEffigy(
                            _configuration.getDirectory(), null, toRead);

                    CompositeEntity compositeEntity = new TransformationRule(
                            effigy, "transformation rule");

                    _patternMatchframe = new GraphPatternSearchEditor(
                            compositeEntity, new Tableau(effigy,
                                    "DBSearchframe"),
                                    ((ActorGraphDBTableau) _tableau).getGtLibrary(),
                                    _containerModel, _sourceFrame,
                                    SimpleSearchFrame.this);

                    _ptdbContainedFramesManager
                    .addContainedFrame(_patternMatchframe);

                } catch (Exception e2) {

                    MessageHandler.error(
                            "Failed to open pattern match search editor.", e2);
                }
            }

            _patternMatchframe.pack();
            _patternMatchframe.setLocationRelativeTo(SimpleSearchFrame.this);
            _patternMatchframe.setVisible(true);
        }

        private Tableau _tableau;
    }

    ///////////////////////////////////////////////////////////////////
    ////                private inner classes                      ////

    private class OpenSearchCriteriaAction extends AbstractAction {

        public OpenSearchCriteriaAction() {
            super("Open...");

            putValue("tooltip", "Open...");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_O));

        }

        ///////////////////////////////////////////////////////////////
        ////            public methods                          //////

        @Override
        public void actionPerformed(ActionEvent e) {

            _open();

        }
    }

    private class SaveSearchCriteriaAction extends AbstractAction {

        public SaveSearchCriteriaAction() {
            super("Save");

            putValue("tooltip", "Save");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_S));

        }

        ///////////////////////////////////////////////////////////////
        ////            public methods                          //////

        @Override
        public void actionPerformed(ActionEvent e) {

            _save();

        }
    }

    private class SaveAsSearchCriteriaAction extends AbstractAction {

        public SaveAsSearchCriteriaAction() {
            super("Save As...");

            putValue("tooltip", "Save As...");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_A));

        }

        ///////////////////////////////////////////////////////////////
        ////            public methods                          //////

        @Override
        public void actionPerformed(ActionEvent e) {

            _saveAs();

        }
    }

    private class ExitSearchCriteriaAction extends AbstractAction {

        public ExitSearchCriteriaAction() {
            super("Exit");

            putValue("tooltip", "Exit");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_X));

        }

        ///////////////////////////////////////////////////////////////
        ////            public methods                          //////

        @Override
        public void actionPerformed(ActionEvent e) {

            _exit();

        }
    }

    private class NewSearchCriteriaAction extends AbstractAction {

        public NewSearchCriteriaAction() {
            super("New");

            putValue("tooltip", "New");
            putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_N));

        }

        ///////////////////////////////////////////////////////////////
        ////            public methods                          //////

        @Override
        public void actionPerformed(ActionEvent e) {

            _new();

        }
    }

    /** Filter that returns true if the selected file is an XML file. */
    private static class SearchCriteriaFileFilter extends FileFilter {

        public SearchCriteriaFileFilter(String description) {

            _description = description;

        }

        /** Filter that returns true if the selected file is an XML file.
         *  @param pathname The pathname to be checked
         *  @return true if the pathname ends with .xml.
         */
        @Override
        public boolean accept(File pathname) {
            return pathname.getName().endsWith(".xml");
        }

        @Override
        public String getDescription() {
            return _description;
        }

        private String _description;

    }

}
