package ptdb.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.ScrollPaneConstants;

import ptdb.common.dto.XMLDBModel;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// SearchResultsFrame

/**
 * An extended JFrame displaying all search results in a scroll panel.
 * Each search result is contained in a SearchResultPanel.  This is also an observer of
 * the SearchResultsBuffer, where it can get search results on the fly by calling getResults().
 * The _cancelButton will notify the search classes that the search has been canceled.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

public class SearchResultsFrame extends JFrame implements Observer {

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

        String title = "Search Results";

        setTitle(title);

        JPanel outerPanel = new JPanel();
        LayoutManager layout = new BoxLayout(outerPanel, BoxLayout.Y_AXIS);
        outerPanel.setLayout(layout);
        outerPanel.setAlignmentX(LEFT_ALIGNMENT);
        add(outerPanel);

        JLabel _label = new JLabel(title + ":");
        _label.setFont(new Font("Title", Font.BOLD, 24));
        _label.setAlignmentX(LEFT_ALIGNMENT);
        outerPanel.add(_label);

        _scrollPane = new JScrollPane(
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
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

        _cancelButton = new JButton("Cancel Search");
        buttonPanel.add(_cancelButton);

        _loadByRefButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                //TODO

            }

        });

        _loadByValButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                //TODO

            }

        });

        _cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent event) {

                _cancelObservable.notifyObservers();

            }

        });

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
        _scrollPane.getViewport().add(newResultPanel);

        repaint();

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
    public void update(Observable buffer, Object arg) {

        if (buffer instanceof SearchResultBuffer) {
            ArrayList<XMLDBModel> results = ((SearchResultBuffer) buffer)
                    .getResults();

            if (results != null && results.size() > 0) {
                display(results);
            } else {
                if (_resultPanelList.size() == 0) {
                    // No result found. 
                    JOptionPane.showMessageDialog(this, "No result found.");
                } else {
                    // Searching is done. 
                    JOptionPane.showMessageDialog(this, "Search is done.");
                }
            }

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
    private Configuration _configuration;

}
