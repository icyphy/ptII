package ptdb.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import ptdb.common.dto.SearchCriteria;
import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.bl.save.AttributesManager;
import ptdb.kernel.bl.search.SearchManager;
import ptdb.kernel.bl.search.SearchResultBuffer;
import ptolemy.actor.gui.Configuration;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
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
     * Construct a SimpleSearchFrame. Add swing Components to the frame. Add a
     * listener for the "+" button, which adds a ModelAttributePanel to the
     * tabbed pane. Add a listener for the Search button to call 
     * _simpleSearch().
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
        _aList = new HashMap();
        _AttDelete = new HashMap();

        setPreferredSize(new Dimension(760, 400));
        
        JPanel outerPanel = new JPanel();
        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        JPanel innerPanel = new JPanel();
        JPanel modelNamePanel = new JPanel();
        JLabel nameLabel = new JLabel("Model Name");

        _tabbedPane = new JTabbedPane();
        _modelName = new JTextField(model.getName());
        _attListPanel = new JPanel();
        _scrollPane = new JScrollPane(_attListPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        modelNamePanel.setAlignmentX(LEFT_ALIGNMENT);
        innerPanel.setAlignmentX(LEFT_ALIGNMENT);
        _modelName.setAlignmentX(LEFT_ALIGNMENT);
        _tabbedPane.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        outerPanel.setAlignmentX(LEFT_ALIGNMENT);
        _attListPanel.setAlignmentX(LEFT_ALIGNMENT);
        _scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        nameLabel.setAlignmentY(TOP_ALIGNMENT);
        modelNamePanel.setAlignmentY(TOP_ALIGNMENT);
        innerPanel.setAlignmentY(TOP_ALIGNMENT);
        _modelName.setAlignmentY(TOP_ALIGNMENT);
        _tabbedPane.setAlignmentY(TOP_ALIGNMENT);
        topPanel.setAlignmentY(TOP_ALIGNMENT);
        outerPanel.setAlignmentY(TOP_ALIGNMENT);
        _attListPanel.setAlignmentY(TOP_ALIGNMENT);
        _scrollPane.setAlignmentY(TOP_ALIGNMENT);
        bottomPanel.setAlignmentY(TOP_ALIGNMENT);

        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        _tabbedPane.setLayout(new BoxLayout(_tabbedPane, BoxLayout.Y_AXIS));
        modelNamePanel
                .setLayout(new BoxLayout(modelNamePanel, BoxLayout.X_AXIS));
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        _attListPanel.setLayout(new BoxLayout(_attListPanel, BoxLayout.Y_AXIS));
        _scrollPane.setLayout(new ScrollPaneLayout());
        _scrollPane.setPreferredSize(new Dimension(500, 300));

        modelNamePanel.setMaximumSize(new Dimension(300, 20));
        _modelName.setPreferredSize(new Dimension(100, 20));
        nameLabel.setPreferredSize(new Dimension(70, 20));

        topPanel.setBorder(BorderFactory.createEmptyBorder());
        nameLabel.setBorder(new EmptyBorder(2, 2, 2, 2));

        modelNamePanel.add(nameLabel);
        modelNamePanel.add(_modelName);
        innerPanel.add(modelNamePanel);
        innerPanel.add(_scrollPane);
        _tabbedPane.addTab("Model Info", innerPanel);

        _tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        _tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        try {
            
            AttributesManager attributeManager = new AttributesManager();
            List <XMLDBAttribute> xmlAttList = new ArrayList();
            xmlAttList = attributeManager.getDBAttributes();
            
            for(XMLDBAttribute a : xmlAttList){
                
                _aList.put(a.getAttributeName(), a);
                
            }
        
        } catch(DBExecutionException e){
            
            JOptionPane
            .showMessageDialog((Component) this,
                    "Could not retrieve attributes from the database.",
                    "Save Error",
                    JOptionPane.INFORMATION_MESSAGE, null);
            
        } catch(DBConnectionException e){
         
            JOptionPane
            .showMessageDialog((Component) this,
                    "Could not retrieve attributes from the database.",
                    "Save Error",
                    JOptionPane.INFORMATION_MESSAGE, null);
            
        }

        // Add existing attributes.
        for (Object a : model.attributeList()) {

            if (a instanceof StringParameter) {

                // We only show the Attribute if it is in the list returned
                // from the DB.
                if (_aList.containsKey(((StringParameter) a).getName())) {

                    JPanel modelDeletePanel = new JPanel();
                    modelDeletePanel
                        .setLayout(new BoxLayout(modelDeletePanel, BoxLayout.X_AXIS));
                    modelDeletePanel.setAlignmentX(LEFT_ALIGNMENT);
                    modelDeletePanel.setAlignmentY(TOP_ALIGNMENT);
                    
                    ModelAttributePanel modelAttPanel = new ModelAttributePanel(
                            _aList);
                    modelAttPanel.setValue(((StringParameter) a).getExpression());
                    JButton deleteButton = new JButton("Delete");
                    deleteButton.setAlignmentY(TOP_ALIGNMENT);

                    modelAttPanel.setAttributeName(((StringParameter) a).getName());
                    modelAttPanel.setValue(((StringParameter) a).getExpression());

                    deleteButton.setActionCommand("Delete");
                    deleteButton
                            .setHorizontalTextPosition(SwingConstants.CENTER);

                    modelDeletePanel.add(modelAttPanel);
                    modelDeletePanel.add(deleteButton);

                    _AttDelete.put(deleteButton, modelDeletePanel);
                    
                    _attListPanel.add(modelDeletePanel);
                    _attListPanel.setMaximumSize(getMinimumSize());
                    
                    deleteButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {

                            _attListPanel.remove((JPanel) _AttDelete.get(event
                                    .getSource()));
                            _attListPanel.remove((JButton) event.getSource());
                            repaint();

                        }

                    });

                    validate();
                    repaint();

                }

            }

        }

        JButton search_Button;
        JButton add_Button;
        JButton cancel_Button;

        add_Button = new JButton("+");
        search_Button = new JButton("Search");
        cancel_Button = new JButton("Cancel");

        add_Button.setMnemonic(KeyEvent.VK_PLUS);
        search_Button.setMnemonic(KeyEvent.VK_ENTER);
        cancel_Button.setMnemonic(KeyEvent.VK_ESCAPE);

        add_Button.setActionCommand("+");
        search_Button.setActionCommand("Save");
        cancel_Button.setActionCommand("Cancel");

        add_Button.setHorizontalTextPosition(SwingConstants.CENTER);
        search_Button.setHorizontalTextPosition(SwingConstants.CENTER);
        cancel_Button.setHorizontalTextPosition(SwingConstants.CENTER);

        add_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                JPanel modelDeletePanel = new JPanel();
                modelDeletePanel
                    .setLayout(new BoxLayout(modelDeletePanel, BoxLayout.X_AXIS));
                modelDeletePanel.setAlignmentX(LEFT_ALIGNMENT);
                modelDeletePanel.setAlignmentY(TOP_ALIGNMENT);
                
                ModelAttributePanel modelAttPanel = new ModelAttributePanel(
                        _aList);
                JButton deleteButton = new JButton("Delete");
                deleteButton.setAlignmentY(TOP_ALIGNMENT);

                modelAttPanel.setAttributeName("");

                deleteButton.setActionCommand("Delete");
                deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);

                modelDeletePanel.add(modelAttPanel);
                modelDeletePanel.add(deleteButton);

                _AttDelete.put(deleteButton, modelDeletePanel);

                _attListPanel.add(modelDeletePanel);
                _attListPanel.setMaximumSize(getMinimumSize());

                deleteButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {

                        _attListPanel.remove((JPanel) _AttDelete.get(event
                                .getSource()));
                        _attListPanel.remove((JButton) event.getSource());

                        validate();
                        repaint();

                    }

                });

                validate();
                repaint();
            }
        });

        search_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                // If the form is in an invalid state, do not continue;
                if (!_isValid()) {

                    return;

                }

                try {

                    _simpleSearch();

                } catch (DBConnectionException e) {

                    //TODO

                } catch (DBExecutionException e) {

                    //TODO

                }

            }
        });

        cancel_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                try {

                    setVisible(false);

                } catch (Exception e) {

                    JOptionPane.showMessageDialog(
                            (Component) event.getSource(),
                            "Could not roll back the model.", "Save Error",
                            JOptionPane.INFORMATION_MESSAGE, null);

                }

            }

        });

        topPanel.add(_tabbedPane);
        bottomPanel.add(add_Button);
        bottomPanel.add(search_Button);
        bottomPanel.add(cancel_Button);
        outerPanel.add(topPanel);
        outerPanel.add(bottomPanel);
        add(outerPanel);
        validate();
        repaint();

    }
    
    ///////////////////////////////////////////////////////////////////
    //                    private methods                          ////
    
    private boolean _isValid(){
        
        ArrayList<String> attributes = new ArrayList();

        // Get a list of all attributes we have displayed.
        Component[] componentArray1 = _attListPanel.getComponents();

        for (int i = 0; i < componentArray1.length; i++) {

            if (componentArray1[i] instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) componentArray1[i])
                        .getComponents();

                for (int j = 0; j < componentArray2.length; j++) {

                    if (componentArray2[j] instanceof ModelAttributePanel) {
                        
                        if(((ModelAttributePanel) componentArray2[j])
                                .getAttributeName().length() == 0) {

                            JOptionPane.showMessageDialog(this, 
                                        "All attributes " +
                                        "must contain a value.",
                                        "Serach Error", 
                                        JOptionPane.INFORMATION_MESSAGE, null);
        
                            return false;
        
                        } 
                        
                        attributes
                            .add(((ModelAttributePanel) componentArray2[j])
                                .getAttributeName());
                        
                        if(((ModelAttributePanel) componentArray2[j])
                                        .getValue().length() == 0) {

                            JOptionPane.showMessageDialog(this, 
                                        "All attributes " +
                            		"must contain a value.",
                                        "Serach Error", 
                                        JOptionPane.INFORMATION_MESSAGE, null);

                            return false;

                        } 
                        

                    }

                }

            }

        }

        // Check for duplicate attributes.
        HashSet set = new HashSet();
        for (int i = 0; i < attributes.size(); i++) {

            boolean val = set.add(attributes.get(i));
            if (val == false) {

                JOptionPane.showMessageDialog(this,
                        "The search criteria cannot contain more" + 
                        " than one instance " +
                        "of the same attribute.", "Search Error",
                        JOptionPane.INFORMATION_MESSAGE, null);
                return false;

            }

        }
        
        
        if (attributes.size() == 0 && _modelName.getText().length()==0) {

            JOptionPane.showMessageDialog(this, 
                        "You must enter a Model Name or " +
            		"select attributes on which to seach.",
                        "Save Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }
        
        if(_modelName.getText().length()>0){
            if (!_modelName.getText().matches("^[A-Za-z0-9]+$")){
                
                JOptionPane.showMessageDialog(this,
                        "The model name should only contain letters and numbers.", 
                        "Search Error",
                        JOptionPane.INFORMATION_MESSAGE, null);
    
                return false;
                
            }
        }
        
        return true;
    }
    
    private void _simpleSearch() 
        throws DBConnectionException, DBExecutionException {
        
        SearchResultsFrame searchResultsFrame = new SearchResultsFrame(
                _containerModel, _sourceFrame, _configuration);
        
        SearchResultBuffer searchResultBuffer = new SearchResultBuffer();
        searchResultBuffer.addObserver(searchResultsFrame);
        
        SearchCriteria searchCriteria = new SearchCriteria();

        ArrayList<Attribute> attributesList = new ArrayList<Attribute>();
        
        // Get all attributes we have displayed.
        Component[] componentArray1 = _attListPanel.getComponents();

        for (int i = 0; i < componentArray1.length; i++) {

            if (componentArray1[i] instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) componentArray1[i])
                        .getComponents();

                for (int j = 0; j < componentArray2.length; j++) {

                    if (componentArray2[j] instanceof ModelAttributePanel) {
                        
                        try {
                            
                            StringParameter attributeToAdd = new StringParameter(
                                    _containerModel, ((ModelAttributePanel) 
                                            componentArray2[j])
                                                .getAttributeName());
                            
                            attributeToAdd.setExpression
                                (((ModelAttributePanel) 
                                        componentArray2[j]).getValue());
                            
                            attributesList.add(attributeToAdd);
                            
                        } catch (Exception e){    }
                    }
                }
            }
        }
        
        
        try {
            
            StringParameter attributeToAdd = new StringParameter(
                    _containerModel, "DBModelName");
            
            attributeToAdd.setExpression
                (_modelName.getText());
            
            attributesList.add(attributeToAdd);
            
        } catch (Exception e){  }
        
        searchCriteria.setAttributes(attributesList);
        
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

    private JPanel _attListPanel;
    private HashMap _aList;
    private JScrollPane _scrollPane;
    private HashMap _AttDelete;
    private JTabbedPane _tabbedPane;
    private JTextField _modelName;        
    private NamedObj _containerModel;
    private JFrame _sourceFrame;
    private Configuration _configuration;
    
}
