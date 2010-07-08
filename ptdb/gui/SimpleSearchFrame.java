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
import java.util.ArrayList;

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
import ptdb.kernel.bl.search.SearchManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.actor.gui.Configuration;
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
     *      
     */
    public SimpleSearchFrame(NamedObj model, 
            JFrame frame, Configuration configuration) {

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

        JButton search_Button;
        JButton cancel_Button;

        search_Button = new JButton("Search");
        cancel_Button = new JButton("Cancel");

        search_Button.setMnemonic(KeyEvent.VK_ENTER);
        cancel_Button.setMnemonic(KeyEvent.VK_ESCAPE);

        search_Button.setActionCommand("Search");
        cancel_Button.setActionCommand("Cancel");

        search_Button.setHorizontalTextPosition(SwingConstants.CENTER);
        cancel_Button.setHorizontalTextPosition(SwingConstants.CENTER);

        search_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                try {
                    
                    // If the form is in an invalid state, do not continue;
                    if (!_isValid()) {
                        
                        return;

                    }

                    _simpleSearch();

                } catch (NameDuplicationException e) {

                    MessageHandler.error("The search cannot be performed now " +
                            "due to a NameDuplicationException.", e);

                } catch (IllegalActionException e) {

                    MessageHandler.error("The search cannot be performed now " +
                            "due to an IllegalActionException.", e);

                } catch (DBConnectionException e) {
                    
                    MessageHandler.error("The search cannot be performed now " +
                            "due to a DBConnectionException.", e);

                } catch (DBExecutionException e) {

                    MessageHandler.error("The search cannot be performed now " +
                            "due to a DBExecutionException.", e);

                }

            }
        });

        cancel_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                setVisible(false);

            }

        });

        topPanel.add(_attributesListPanel);
        bottomPanel.add(search_Button);
        bottomPanel.add(cancel_Button);
        add(topPanel);
        add(bottomPanel);
        
        validate();
        repaint();

    }
    
    ///////////////////////////////////////////////////////////////////
    //                    private methods                          ////
    
    private boolean _isValid() throws NameDuplicationException,
        IllegalActionException {
        
        if (_attributesListPanel.getAttributeCount() == 0 &&
                _attributesListPanel.getModelName().length()==0) {

            JOptionPane.showMessageDialog(this, 
                        "You must enter the Model Name or " +
                        "select attributes on which to search.",
                        "Search Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }
        
        
        if(_attributesListPanel.getModelName().length()>0){
            if (!_attributesListPanel.getModelName().matches("^[A-Za-z0-9]+$")){
                
                JOptionPane.showMessageDialog(this,
                        "The model name should only " +
                        "contain letters and numbers.", 
                        "Search Error",
                        JOptionPane.INFORMATION_MESSAGE, null);
    
                return false;
                
            }
        }
        
        if (_attributesListPanel.containsDuplicates()) {
            
            JOptionPane.showMessageDialog(this,
                    "The search criteria cannot contain more" + 
                    " than one instance " + 
                    "of the same attribute.", "Search Error",
                    JOptionPane.INFORMATION_MESSAGE, null);
            
            return false;
            
        }
        
        if (!_attributesListPanel.allAttributeNamesSet()){
            
            JOptionPane.showMessageDialog(this,
                    "You must specify a name for all attributes.", 
                    "Search Error",
                    JOptionPane.INFORMATION_MESSAGE, null);
    
            return false;
            
        }
            
        if (!_attributesListPanel.allAttributeValuesSet()){
            
            JOptionPane.showMessageDialog(this,
                    "You must specify a value for all attributes.", 
                    "Search Error",
                    JOptionPane.INFORMATION_MESSAGE, null);
    
            return false;
            
        }

        return true;
    }
    
    private void _simpleSearch() 
        throws DBConnectionException, DBExecutionException, 
        NameDuplicationException, IllegalActionException {
        
        SearchResultsFrame searchResultsFrame = new SearchResultsFrame(
                _containerModel, _sourceFrame, _configuration);
        
        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();
        searchResultBuffer.addObserver(searchResultsFrame);
        
        SearchCriteria searchCriteria = new SearchCriteria();
        
        
        if (!_attributesListPanel.getModelName().equals("")){
            
            searchCriteria.setModelName(_attributesListPanel.getModelName());
        }
        
        if(_attributesListPanel.getAttributeCount() > 0){
        
            ArrayList<Attribute> attributesToSearch = 
                _attributesListPanel.getAttributes();
            searchCriteria.setAttributes(attributesToSearch);
        }
        
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
        setVisible(false);
    }
    
    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private NamedObj _containerModel;
    private JFrame _sourceFrame;
    private Configuration _configuration;
    private AttributesListPanel _attributesListPanel;
    
}
