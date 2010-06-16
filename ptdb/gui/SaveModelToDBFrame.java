package ptdb.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

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

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.kernel.DBAttribute;
import ptdb.kernel.bl.save.SaveModelManager;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.Variable;
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
////SaveModelToDBFrame

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
    public SaveModelToDBFrame(NamedObj model, JFrame frame) {

        super("Save Model to Database");

        setBounds(100, 100, 500, 300);
        setResizable(false);

        _modelToSave = model;
        _initialModelName = model.getName();
        _rollbackModel = model.exportMoML();

        try {

            _modelClone = (NamedObj) _modelToSave.clone();

        } catch (Exception e) {
        }

        _aList = new HashMap();
        _AttDelete = new HashMap();

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

        outerPanel.setLayout(new BoxLayout(outerPanel, BoxLayout.Y_AXIS));
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
        modelNamePanel
                .setLayout(new BoxLayout(modelNamePanel, BoxLayout.X_AXIS));
        innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.Y_AXIS));
        _tabbedPane.setLayout(new BoxLayout(_tabbedPane, BoxLayout.Y_AXIS));
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

        //TODO Populate list of possible attributes.
        _aList.put("DBAttribute1", "Text");
        _aList.put("DBAttribute2", "Boolean");
        _aList.put("DBAttribute3", "List");

        // Add existing attributes.
        for (Object a : model.attributeList()) {

            if (a instanceof DBAttribute) {

                // We only show the Attribute if it is in the list returned
                // from the DB.
                if (_aList.containsKey(((DBAttribute) a).getName())) {

                    JPanel modelDeletePanel = new JPanel();
                    ModelAttributePanel modelAttPanel = new ModelAttributePanel(
                            _aList);
                    JButton deleteButton = new JButton("Delete");

                    modelAttPanel.setAttributeName(((DBAttribute) a).getName());
                    System.out.println(modelAttPanel.getAttributeName());
                    modelAttPanel.setValue(((DBAttribute) a).getExpression());
                    System.out.println(modelAttPanel.getValue());

                    deleteButton.setActionCommand("Delete");
                    deleteButton
                            .setHorizontalTextPosition(SwingConstants.CENTER);

                    modelDeletePanel.add(modelAttPanel);
                    modelDeletePanel.add(deleteButton);

                    _AttDelete.put(deleteButton, modelDeletePanel);

                    _attListPanel.add(modelDeletePanel);

                    deleteButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {

                            _attListPanel.remove((JPanel) _AttDelete.get(event
                                    .getSource()));
                            _attListPanel.remove((JButton) event.getSource());
                            repaint();

                        }

                    });

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
                ModelAttributePanel modelAttPanel = new ModelAttributePanel(
                        _aList);
                JButton deleteButton = new JButton("Delete");

                modelAttPanel.setAttributeName("");

                deleteButton.setActionCommand("Delete");
                deleteButton.setHorizontalTextPosition(SwingConstants.CENTER);

                modelDeletePanel.add(modelAttPanel);
                modelDeletePanel.add(deleteButton);

                _AttDelete.put(deleteButton, modelDeletePanel);

                _attListPanel.add(modelDeletePanel);

                deleteButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {

                        _attListPanel.remove((JPanel) _AttDelete.get(event
                                .getSource()));
                        _attListPanel.remove((JButton) event.getSource());
                        repaint();

                    }

                });
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

                    if (_saveModel())
                        setVisible(false);

                } catch (NameDuplicationException e) {

                    JOptionPane
                            .showMessageDialog(
                                    (Component) event.getSource(),
                                    "The entered name will result in a "
                                            + "duplicate "
                                            + "model name.  Please enter a different name.",
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
                                    "Could not save specified attributes.  "
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

    }

    ///////////////////////////////////////////////////////////////////
    //                    private methods                          ////

    private boolean _isValid() {

        if (_nameText.getText().isEmpty()) {

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

    private boolean _saveModel() throws Exception {

        try {

            _modelToSave.setName(_nameText.getText());

            if (_modelToSave.getAttribute("DBModel") == null) {

                _isNew = true;

                Variable dbModel = new Variable(_modelToSave.workspace());
                dbModel.setContainer(_modelToSave);
                dbModel.setName("DBModel");

            } else if (_modelToSave.getName().equals(_initialModelName)) {

                _isNew = true;

            } else {

                _isNew = false;

            }

            ArrayList<DBAttribute> attributesList = new ArrayList();

            for (Object a : _modelToSave.attributeList()) {

                if (a instanceof DBAttribute) {

                    attributesList.add((DBAttribute) a);

                }

            }

            // Delete all existing DBAttributes.
            for (DBAttribute attribute : attributesList) {

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

                            //System.out.println(((ModelAttributePanel) componentArray2[j]).getAttributeName());
                            //System.out.println(((ModelAttributePanel) componentArray2[j]).getValue());

                            DBAttribute attributeToAdd = new DBAttribute(
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

        } catch (NameDuplicationException e) {

            throw e;

        } catch (IllegalActionException e) {

            throw e;

        }

        XMLDBModel xmlModel = new XMLDBModel(_modelToSave.getName());
        xmlModel.setModel(_modelToSave.exportMoML());
        xmlModel.setIsNew(_isNew);

        SaveModelManager saveModelManager = new SaveModelManager();

        try {

            if (saveModelManager.save(xmlModel)) {

                JOptionPane.showMessageDialog(this,
                        "The model was successfully saved.", "Success",
                        JOptionPane.INFORMATION_MESSAGE, null);
                return true;

            } else {

                JOptionPane.showMessageDialog(this,
                        "A problem occurred while saving.", "Save Error",
                        JOptionPane.INFORMATION_MESSAGE, null);

            }

        } catch (DBConnectionException e1) {

            _rollbackModel();
            throw e1;

        } catch (DBExecutionException e1) {

            _rollbackModel();
            throw e1;

        } catch (IllegalArgumentException e1) {

            _rollbackModel();
            throw e1;

        }

        return false;

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
    private boolean _isNew = true;
    private String _rollbackModel;
    private NamedObj _modelClone;

}
