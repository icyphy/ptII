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

import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.kernel.bl.save.AttributesManager;
import ptdb.kernel.bl.save.SaveModelManager;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.SingletonConfigurableAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.icon.ValueIcon;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;

///////////////////////////////////////////////////////////////////
//// SaveModelToDBFrame

/**
 * An extended JFrame used for saving a model to the database. Additionally, the
 * user can manage model attributes prior to saving. This associates saved
 * attributes to those that the user selects for model searches.
 * 
 * @author Lyle Holsinger
 * @since Ptolemy II 8.1
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 * 
 */

public class SaveModelToDBFrame extends JFrame {

    /**
     * Construct a SaveModelToDBFrame. Add swing Components to the frame. Add a
     * listener for the "+" button, which adds a ModelAttributePanel to the
     * tabbed pane. Add a listener for the Save button to call _saveModel().
     * 
     * @param model The model that is being saved to the database.
     * @param frame The frame from which the save form was opened. Passed to the
     * object to allow repainting if attribute modifications occur.
     */
    public SaveModelToDBFrame(NamedObj model) {

        super("Save Model to Database");

        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        _modelToSave = model;
        _initialModelName = model.getName();

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
        _nameText = new JTextField(model.getName());
        _attListPanel = new JPanel();
        _scrollPane = new JScrollPane(_attListPanel,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        nameLabel.setAlignmentX(LEFT_ALIGNMENT);
        modelNamePanel.setAlignmentX(LEFT_ALIGNMENT);
        innerPanel.setAlignmentX(LEFT_ALIGNMENT);
        _nameText.setAlignmentX(LEFT_ALIGNMENT);
        _tabbedPane.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        outerPanel.setAlignmentX(LEFT_ALIGNMENT);
        _attListPanel.setAlignmentX(LEFT_ALIGNMENT);
        _scrollPane.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        nameLabel.setAlignmentY(TOP_ALIGNMENT);
        modelNamePanel.setAlignmentY(TOP_ALIGNMENT);
        innerPanel.setAlignmentY(TOP_ALIGNMENT);
        _nameText.setAlignmentY(TOP_ALIGNMENT);
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
        _nameText.setPreferredSize(new Dimension(100, 20));
        nameLabel.setPreferredSize(new Dimension(70, 20));

        topPanel.setBorder(BorderFactory.createEmptyBorder());
        nameLabel.setBorder(new EmptyBorder(2, 2, 2, 2));

        modelNamePanel.add(nameLabel);
        modelNamePanel.add(_nameText);
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

        JButton save_Button;
        JButton add_Button;
        JButton cancel_Button;

        add_Button = new JButton("+");
        save_Button = new JButton("Save");
        cancel_Button = new JButton("Cancel");

        add_Button.setMnemonic(KeyEvent.VK_PLUS);
        save_Button.setMnemonic(KeyEvent.VK_ENTER);
        cancel_Button.setMnemonic(KeyEvent.VK_ESCAPE);

        add_Button.setActionCommand("+");
        save_Button.setActionCommand("Save");
        cancel_Button.setActionCommand("Cancel");

        add_Button.setHorizontalTextPosition(SwingConstants.CENTER);
        save_Button.setHorizontalTextPosition(SwingConstants.CENTER);
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

        save_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                // If the form is in an invalid state, do not continue;
                if (!_isValid()) {

                    return;

                }

                try {

                    _saveModel();

                } catch (NameDuplicationException e) {

                    JOptionPane
                            .showMessageDialog((Component) event.getSource(),
                                    "The entered name will result in a "
                                            + "duplicate " + "model name.  "
                                            + "Please enter a different name.",
                                    "Save Error",
                                    JOptionPane.INFORMATION_MESSAGE, null);

                } catch (IllegalActionException e) {

                    JOptionPane
                            .showMessageDialog((Component) event.getSource(),
                                    "Saving this model will result in "
                                            + "incorrect or "
                                            + "inconsistent data.  "
                                            + "Please cancel and try again.",
                                    "Save Error",
                                    JOptionPane.INFORMATION_MESSAGE, null);

                } catch (Exception e) {

                    JOptionPane
                            .showMessageDialog((Component) event.getSource(),
                                    "Could not save the model.  "
                                            + "Please cancel and try again.",
                                    "Save Error",
                                    JOptionPane.INFORMATION_MESSAGE, null);
                }

            }
        });

        cancel_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                try {

                    _rollbackModel();
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
        bottomPanel.add(save_Button);
        bottomPanel.add(cancel_Button);
        outerPanel.add(topPanel);
        outerPanel.add(bottomPanel);
        add(outerPanel);
        validate();
        repaint();

    }

    ///////////////////////////////////////////////////////////////////
    //                    private methods                          ////

    private void _commitSave(boolean isNew) throws Exception {

        _updateDisplayedModel();

        xmlModel = new XMLDBModel(_modelToSave.getName());
        xmlModel.setModel(_modelToSave.exportMoML());
        xmlModel.setIsNew(isNew);

        SaveModelManager saveModelManager = new SaveModelManager();

        try {

            if (saveModelManager.save(xmlModel)) {

                JOptionPane.showMessageDialog(this,
                        "The model was successfully saved.", "Success",
                        JOptionPane.INFORMATION_MESSAGE, null);

                setVisible(false);

            } else {

                JOptionPane.showMessageDialog(this,
                        "A problem occurred while saving.", "Save Error",
                        JOptionPane.INFORMATION_MESSAGE, null);

            }

        } catch (DBConnectionException exception) {

            _rollbackModel();
            exception.printStackTrace();
            throw exception;

        } catch (DBExecutionException exception) {

            _rollbackModel();
            exception.printStackTrace();
            throw exception;

        } catch (IllegalArgumentException exception) {

            _rollbackModel();
            exception.printStackTrace();
            throw exception;

        } catch (ModelAlreadyExistException exception) {

            Object[] options = { "Yes", "No", "Cancel" };
            int n = JOptionPane.showOptionDialog(this,
                    "A model with the given name "
                            + "already exists in the database.  "
                            + "Would you like to overwrite it? ",
                    "Model Exists", JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE, null, options, options[2]);

            if (n == JOptionPane.YES_OPTION) {

                saveModelManager = null;
                _commitSave(false);

            } else {

                _rollbackModel();

            }

        }

    }

    private boolean _isNew() throws NameDuplicationException,
            IllegalActionException {

        boolean isNew = true;

        // It is not new if the model has the DBModel tag 
        // and the model name is still the same.
        if (_modelToSave.getAttribute("DBModel") != null
                && _modelToSave.getName().equals(_initialModelName)) {

            isNew = false;

        }

        return isNew;
    }

    private boolean _isValid() {

        if (_nameText.getText().length() == 0) {

            JOptionPane.showMessageDialog(this, "You must enter a Model Name.",
                    "Save Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        if (_nameText.getText().contains(".")) {

            JOptionPane.showMessageDialog(this,
                    "The model name should not contain '.'.", "Save Error",
                    JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        ArrayList<String> attributes = new ArrayList();

        // Get a list of all attributes we have displayed.
        Component[] componentArray1 = _attListPanel.getComponents();

        for (int i = 0; i < componentArray1.length; i++) {

            if (componentArray1[i] instanceof JPanel) {

                Component[] componentArray2 = ((JPanel) componentArray1[i])
                        .getComponents();

                for (int j = 0; j < componentArray2.length; j++) {

                    if (componentArray2[j] instanceof ModelAttributePanel) {

                        attributes
                                .add(((ModelAttributePanel) componentArray2[j])
                                        .getAttributeName());

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
                        "The model cannot contain more" + " than one instance "
                                + "of the same attribute.", "Save Error",
                        JOptionPane.INFORMATION_MESSAGE, null);
                return false;

            }

        }

        return true;

    }

    private void _rollbackModel() throws Exception {

        //TODO

    }

    private void _saveModel() throws Exception {

        try {

            _modelToSave.setName(_nameText.getText());

            if (_initialModelName != null && _initialModelName.length() > 0) {

                if (!_modelToSave.getName().equals(_initialModelName)) {

                    Object[] options = { "Yes", "No", "Cancel" };
                    int n = JOptionPane.showOptionDialog(this,
                            "You have given the model a new name.  "
                                    + "Do you want to save a new copy?",
                            "Model Name Changed",
                            JOptionPane.YES_NO_CANCEL_OPTION,
                            JOptionPane.QUESTION_MESSAGE, null, options,
                            options[2]);

                    if (n != JOptionPane.YES_OPTION) {

                        return;

                    }
                }
            }

            _commitSave(_isNew());

        } catch (DBConnectionException exception) {

            throw exception;

        } catch (DBExecutionException exception) {

            throw exception;

        } catch (IllegalArgumentException exception) {

            throw exception;

        }

    }

    private void _updateDisplayedModel() throws Exception {

        try {

            if (_modelToSave.getAttribute("DBModel") == null) {

                StringParameter dbModelParam = new StringParameter(
                        _modelToSave, "DBModel");
                dbModelParam.setExpression("TRUE");

            }

            ArrayList<StringParameter> attributesList = new ArrayList();

            for (Object a : _modelToSave.attributeList()) {

                if (a instanceof StringParameter) {

                    attributesList.add((StringParameter) a);

                }

            }

            // Delete all existing DBAttributes.
            for (StringParameter attribute : attributesList) {

                attribute.setContainer(null);

            }

            // Get all attributes we have displayed.
            Component[] componentArray1 = _attListPanel.getComponents();

            for (int i = 0; i < componentArray1.length; i++) {

                if (componentArray1[i] instanceof JPanel) {

                    Component[] componentArray2 = ((JPanel) componentArray1[i])
                            .getComponents();

                    for (int j = 0; j < componentArray2.length; j++) {

                        if (componentArray2[j] instanceof ModelAttributePanel) {

                            StringParameter attributeToAdd = new StringParameter(
                                    _modelToSave,
                                    ((ModelAttributePanel) componentArray2[j])
                                            .getAttributeName());
                            attributeToAdd
                                    .setExpression(((ModelAttributePanel) componentArray2[j])
                                            .getValue());
                            attributeToAdd.setContainer(_modelToSave);

                            SingletonAttribute sa = new SingletonAttribute(
                                    attributeToAdd.workspace());
                            sa.setContainer(attributeToAdd);
                            sa.setName("_hideName");

                            ValueIcon vi = new ValueIcon(attributeToAdd,
                                    "_icon");
                            vi.setContainer(attributeToAdd);

                            ColorAttribute ca = new ColorAttribute(vi, "_color");
                            ca.setContainer(vi);
                            ca.setExpression("{1.0, 0.0, 0.0, 1.0}");

                            SingletonConfigurableAttribute sca = new SingletonConfigurableAttribute(
                                    attributeToAdd.workspace());
                            sca.setContainer(attributeToAdd);
                            sca.configure(null, attributeToAdd.getSource(),
                                    "<svg><text x=\"20\" "
                                            + "style=\"font-size:14; "
                                            + "font-family:SansSerif; "
                                            + "fill:blue\" " + "y=\"20\">Hello"
                                            + "</text></svg>");

                            VisibleParameterEditorFactory vpef = new VisibleParameterEditorFactory(
                                    attributeToAdd, "_editorFactory");
                            vpef.setContainer(attributeToAdd);

                            // TODO Figure out how to place in unique location.
                            double[] xy = { 250, 170 };
                            Location l = new Location(attributeToAdd,
                                    "_location");
                            l.setLocation(xy);

                            try {

                                MoMLChangeRequest change = new MoMLChangeRequest(
                                        this, null, _modelToSave.exportMoML());
                                change.setUndoable(true);
                                _modelToSave.requestChange(change);
                            } catch (Exception e) {
                                throw e;
                            }

                        }

                    }

                }

            }

        } catch (NameDuplicationException exception) {

            throw exception;

        } catch (IllegalActionException exception) {

            throw exception;

        }

    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////

    private JPanel _attListPanel;
    private HashMap _aList;
    private JScrollPane _scrollPane;
    private HashMap _AttDelete;
    private JTabbedPane _tabbedPane;
    private NamedObj _modelToSave;
    private JTextField _nameText;
    private String _initialModelName;
    private XMLDBModel xmlModel;

}
