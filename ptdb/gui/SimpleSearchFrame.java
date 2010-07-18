/*
@Copyright (c) 2010 The Regents of the University of California.
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
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import ptdb.common.dto.SearchCriteria;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.Utilities;
import ptdb.kernel.bl.search.SearchManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.actor.gt.TransformationRule;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.EffigyFactory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// SimpleSearchFrame

/**
 * An extended JFrame used for performing a simple database search based on
 * the model name and attributes. 
 * 
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 * 
 */

public class SimpleSearchFrame extends JFrame {

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

        super("Save Model to Database");

        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        _containerModel = model;

        _sourceFrame = frame;
        _configuration = configuration;
        _attributesListPanel = new AttributesListPanel(new NamedObj());

        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();

        _attributesListPanel.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        _attributesListPanel.setAlignmentY(TOP_ALIGNMENT);
        topPanel.setAlignmentY(TOP_ALIGNMENT);
        bottomPanel.setAlignmentY(TOP_ALIGNMENT);

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

        topPanel.setBorder(BorderFactory.createEmptyBorder());

        JButton cancel_Button;
        JButton advancedSearchButton;

        _searchButton = new JButton("Search");
        cancel_Button = new JButton("Cancel");
        advancedSearchButton = new JButton("Open Advanced Search...");

        _searchButton.setMnemonic(KeyEvent.VK_ENTER);
        cancel_Button.setMnemonic(KeyEvent.VK_ESCAPE);

        _searchButton.setActionCommand("Search");
        cancel_Button.setActionCommand("Cancel");
        advancedSearchButton.setActionCommand("Open Advanced Search...");

        _searchButton.setHorizontalTextPosition(SwingConstants.CENTER);
        cancel_Button.setHorizontalTextPosition(SwingConstants.CENTER);

        advancedSearchButton.setHorizontalAlignment(SwingConstants.RIGHT);

        _searchButton.addActionListener(new ActionListener() {
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

                }

            }
        });

        cancel_Button.addActionListener(new ActionListener() {
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
                if (_patternMatchframe != null) {
                    _patternMatchframe.dispose();
                }
                
                dispose();
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if (_patternMatchframe != null) {
                    _patternMatchframe.dispose();
                }
                
                dispose();
            }

            @Override
            public void windowActivated(WindowEvent e) {
                // Do nothing.    
            }
        });

        topPanel.add(_attributesListPanel);
        bottomPanel.add(_searchButton);
        bottomPanel.add(cancel_Button);
        bottomPanel.add(advancedSearchButton);
        add(topPanel);
        add(bottomPanel);

        validate();
        repaint();

    }

    ///////////////////////////////////////////////////////////////////
    ////                  public methods                           ////

    /**
     * Perform the action of clicking the search button on this frame. 
     * 
     * @param event The ActionEvent for this click. 
     */
    public void clickSearchButton(ActionEvent event) {
        _searchButton.doClick();
    }

    ///////////////////////////////////////////////////////////////////
    ////                  private methods                          ////

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

                && (searchCriteria.getDBGraphSearchCriteria() == null || ((searchCriteria
                        .getDBGraphSearchCriteria().getPortsList() == null || searchCriteria
                        .getDBGraphSearchCriteria().getPortsList().isEmpty()) && (searchCriteria
                        .getDBGraphSearchCriteria().getComponentEntitiesList() == null || searchCriteria
                        .getDBGraphSearchCriteria().getComponentEntitiesList()
                        .isEmpty())))

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

            if (!Utilities.checkAttributeModelName(_attributesListPanel
                    .getModelName())) {
                JOptionPane.showMessageDialog(this,
                        "The model name should only "
                                + "contain letters and numbers.",
                        "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

                return false;

            }
        }

        if (_attributesListPanel.containsDuplicates()) {

            JOptionPane.showMessageDialog(this,
                    "The search criteria cannot contain more"
                            + " than one instance " + "of the same attribute.",
                    "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

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
            IllegalActionException {

        SearchCriteria searchCriteria = new SearchCriteria();

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

            SearchResultsFrame searchResultsFrame = new SearchResultsFrame(
                    _containerModel, _sourceFrame, _configuration);

            SearchResultBuffer searchResultBuffer = new SearchResultBuffer();
            searchResultBuffer.addObserver(searchResultsFrame);

            // Show the search result frame.
            searchResultsFrame.pack();
            searchResultsFrame.setVisible(true);

            // Call the Search Manager to trigger the search.
            SearchManager searchManager = new SearchManager();
            try {
                searchManager.search(searchCriteria, searchResultBuffer);

            } catch (DBConnectionException e1) {
                searchResultsFrame.setVisible(false);
                searchResultsFrame.dispose();
                MessageHandler.error("Cannot perform the search now.", e1);

            } catch (DBExecutionException e2) {
                searchResultsFrame.setVisible(false);
                searchResultsFrame.dispose();
                MessageHandler.error("Cannot perform the search now.", e2);
            }

            //            setVisible(false);

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
    ////                  private variables                        ////

    private AttributesListPanel _attributesListPanel;
    private Configuration _configuration;
    private NamedObj _containerModel;
    private GraphPatternSearchEditor _patternMatchframe;
    private JButton _searchButton;
    private JFrame _sourceFrame;

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

                } catch (Exception e2) {

                    MessageHandler.error(
                            "Failed to open pattern match search editor.", e2);
                }
            }

            _patternMatchframe.pack();
            _patternMatchframe.centerOnScreen();
            _patternMatchframe.setVisible(true);
        }

        private Tableau _tableau;
    }

}
