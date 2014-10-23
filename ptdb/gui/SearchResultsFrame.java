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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.ImageObserver;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.CircularDependencyException;
import ptdb.kernel.bl.load.LoadManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// SearchResultsFrame

/**
 * An extended JFrame displaying all search results in a scroll panel.
 * Each search result is contained in a SearchResultPanel.  This is also an observer of
 * the SearchResultsBuffer, where it can get search results on the fly by calling getResults().
 * The _cancelButton will notify the search classes that the search has been canceled.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

@SuppressWarnings("serial")
public class SearchResultsFrame extends JFrame implements Observer,
PTDBBasicFrame {

    /**
     * Construct a panel associated with a search result.
     *
     * @param model
     *        The model into which search results would be imported.
     * @param frame
     *        The frame from which this frame was opened.  It is here to allow repainting.
     *
     * @param configuration
     *        The configuration under which an effigy of models would be generated.
     */
    public SearchResultsFrame(NamedObj model, JFrame frame,
            Configuration configuration) {

        _configuration = configuration;
        _cancelObservable = new CancelObservable();
        _containerModel = model;
        _sourceFrame = frame;
        _configuration = configuration;

        String title = "Search Results";

        setTitle(title);

        JPanel outerPanel = new JPanel();
        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        outerPanel.setAlignmentX(LEFT_ALIGNMENT);
        add(outerPanel);

        _innerPanel = new JPanel();
        _innerPanel.setLayout(new BoxLayout(_innerPanel, BoxLayout.Y_AXIS));
        _innerPanel.setAlignmentX(LEFT_ALIGNMENT);
        _innerPanel.setMinimumSize(getMaximumSize());

        JLabel _label = new JLabel(title + ":");
        _label.setFont(new Font("Title", Font.BOLD, 24));
        _label.setAlignmentX(LEFT_ALIGNMENT);
        outerPanel.add(_label);

        _scrollPane = new JScrollPane(_innerPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);

        _scrollPane.setLayout(new ScrollPaneLayout());
        _scrollPane.setPreferredSize(new Dimension(800, 200));
        _scrollPane.setAlignmentX(LEFT_ALIGNMENT);

        outerPanel.add(_scrollPane);

        _resultPanelList = new ArrayList();

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setPreferredSize(new Dimension(600, 50));
        buttonPanel.setAlignmentX(LEFT_ALIGNMENT);
        outerPanel.add(buttonPanel);

        _loadByRefButton = new JButton("Import By Reference");
        buttonPanel.add(_loadByRefButton);

        _loadByValButton = new JButton("Import By Value");
        buttonPanel.add(_loadByValButton);

        _cancelButton = new JButton("Stop Search");
        //        _cancelButton.setEnabled(false);
        buttonPanel.add(_cancelButton);

        _loadByRefButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                _importByReference();

            }

        });

        _loadByValButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                _importByValue();

            }

        });

        _cancelButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent event) {

                _cancelObservable.notifyObservers();

                _cancelButton.setEnabled(false);

            }

        });

        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.setPreferredSize(new Dimension(ImageObserver.WIDTH, 50));

        outerPanel.add(statusPanel);

        _statusTextField = new JTextField();

        _statusTextField.setBorder(javax.swing.BorderFactory
                .createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        _statusTextField.setEditable(false);
        _statusTextField.setSize(statusPanel.getSize());

        _statusTextField.setFont(new Font("", Font.BOLD, 12));
        statusPanel.add(_statusTextField, BorderLayout.SOUTH);

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add new search result in the scroll pane.
     *
     * @param model
     *        The model that will be displayed as search results.
     */
    public void addSearchResult(XMLDBModel model) {

        SearchResultPanel newResultPanel = new SearchResultPanel(model,
                _configuration);
        _resultPanelList.add(newResultPanel);
        _innerPanel.add(newResultPanel);
        _innerPanel.validate();
        _scrollPane.validate();
        repaint();

    }

    /**
     * Close this frame.
     */

    @Override
    public void closeFrame() {
        dispose();
    }

    /** Display new search results in the scroll pane.
     *
     * @param modelList
     *        The list of models that will be displayed as search results.
     */
    public void display(List<XMLDBModel> modelList) {

        for (XMLDBModel model : modelList) {

            addSearchResult(model);

        }

    }

    /** Register an observer to allow notification upon canceling by the user.
     *
     * @param buffer
     *        The observer.  Only added if it is an instance of SearchResultBuffer.
     */
    public void registerCancelObserver(Observer buffer) {

        if (buffer instanceof SearchResultBuffer) {

            _cancelObservable.addObserver(buffer);

        }

    }

    /** Implement the update method for Observer interface.
     *  Call display to display search results.
     *
     * @param buffer
     *        The observer.  Only handled if it is an instance of SearchResultBuffer.
     * @param arg
     *        Option argument.  This is unused, but included by Java conventions.
     */
    @Override
    public void update(Observable buffer, Object arg) {

        if (buffer instanceof SearchResultBuffer) {
            ArrayList<XMLDBModel> results = ((SearchResultBuffer) buffer)
                    .getResults();

            if (results != null && results.size() > 0) {
                display(results);
            } else {
                if (_resultPanelList.size() == 0) {
                    // No result found.
                    //                    JOptionPane.showMessageDialog(this, "No result found.");

                    _statusTextField.setText("No result found.");
                    _statusTextField.setForeground(Color.RED);
                } else {
                    // Searching is done.
                    //                    JOptionPane.showMessageDialog(this, "Search is done.");
                    _statusTextField.setText("Search is done.");
                    _statusTextField.setForeground(Color.BLACK);
                }
                _cancelButton.setEnabled(false);
            }

        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _importByReference() {

        ArrayList<String> modelNames = new ArrayList();

        for (SearchResultPanel panel : _resultPanelList) {

            modelNames.addAll(panel.getSelections());

        }
        if (modelNames.size() > 0) {
            boolean importDone = false;
            for (String modelName : modelNames) {

                try {

                    Entity modelToImport = LoadManager.importModel(modelName,
                            true, _containerModel);

                    if (modelToImport != null) {

                        MoMLChangeRequest change = new MoMLChangeRequest(this,
                                _containerModel, modelToImport.exportMoML());

                        change.setUndoable(true);
                        _containerModel.requestChange(change);
                        importDone = true;

                    } else {

                        throw new Exception();

                    }

                } catch (CircularDependencyException e) {

                    JOptionPane.showMessageDialog(this, "Importing this "
                            + "model by reference will result in a circular "
                            + "dependency.", "Import Error",
                            JOptionPane.INFORMATION_MESSAGE, null);

                } catch (Exception e) {

                    MessageHandler.error("Cannot import the model - '"
                            + modelName + "'.", e);

                }

            }
            if (importDone) {
                _sourceFrame.toFront();
                //                JOptionPane.showMessageDialog(_sourceFrame, "Import complete!");
                _statusTextField.setText("Import complete!");
                _statusTextField.setForeground(Color.BLACK);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select at least one model to import.");
        }
    }

    private void _importByValue() {

        ArrayList<String> modelNames = new ArrayList();

        for (SearchResultPanel panel : _resultPanelList) {

            modelNames.addAll(panel.getSelections());

        }
        if (modelNames.size() > 0) {
            for (String modelName : modelNames) {

                try {

                    Entity modelToImport = LoadManager.importModel(modelName,
                            false, _containerModel);

                    if (modelToImport != null) {

                        MoMLChangeRequest change = new MoMLChangeRequest(this,
                                _containerModel, modelToImport.exportMoML());

                        change.setUndoable(true);
                        _containerModel.requestChange(change);

                    } else {

                        throw new Exception();

                    }

                } catch (Exception e) {

                    MessageHandler.error("Cannot import the specified model. ",
                            e);

                }

            }

            _sourceFrame.toFront();
            //            JOptionPane.showMessageDialog(_sourceFrame, "Import complete!");
            _statusTextField.setText("Import complete!");
            _statusTextField.setForeground(Color.BLACK);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select atleast one model to import.");
        }
    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private JScrollPane _scrollPane;
    private JButton _loadByRefButton;
    private JButton _loadByValButton;
    private JButton _cancelButton;
    private ArrayList<SearchResultPanel> _resultPanelList;
    private CancelObservable _cancelObservable;
    JPanel _innerPanel;
    private NamedObj _containerModel;
    private JFrame _sourceFrame;
    private Configuration _configuration;
    private javax.swing.JTextField _statusTextField;

}
