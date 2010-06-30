package ptdb.gui;

import java.awt.Component;
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
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.kernel.bl.save.SaveModelManager;
import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
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
     * Construct a SaveModelToDBFrame. Add swing Components to the frame. Add
     * listeners for the "Save" and "Cancel" buttons.
     * 
     * @param model The model that is being saved to the database.
     * 
     */
    public SaveModelToDBFrame(NamedObj model) {

        super("Save Model to Database");

        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        _modelToSave = model;
        _initialModelName = model.getName();
        
        _orignialAttributes = new ArrayList();
        _attributesListPanel = new AttributesListPanel(_modelToSave);
        _tabbedPane = new JTabbedPane();
        
        
        //Create a list of the original attributes
        for(Object attribute : _modelToSave.attributeList()){
            
            if(attribute instanceof StringParameter){
                
                if (((StringParameter) attribute).getName()!="DBReference" && 
                        ((StringParameter) attribute).getName()!="DBModelName" &&
                        _attributesListPanel.isDBAttribute(((StringParameter) attribute).getName())){
                    
                    _orignialAttributes.add((StringParameter) attribute);
                    
                }
                
            }            
            
        }

        JPanel topPanel = new JPanel();
        JPanel bottomPanel = new JPanel();
        
        _attributesListPanel.setAlignmentX(LEFT_ALIGNMENT);
        _tabbedPane.setAlignmentX(LEFT_ALIGNMENT);
        topPanel.setAlignmentX(LEFT_ALIGNMENT);
        bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

        _attributesListPanel.setAlignmentY(TOP_ALIGNMENT);
        _tabbedPane.setAlignmentY(TOP_ALIGNMENT);
        topPanel.setAlignmentY(TOP_ALIGNMENT);
        bottomPanel.setAlignmentY(TOP_ALIGNMENT);

        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        _tabbedPane.setLayout(new BoxLayout(_tabbedPane, BoxLayout.Y_AXIS));

        topPanel.setBorder(BorderFactory.createEmptyBorder());

        _tabbedPane.addTab("Model Info", _attributesListPanel);
        
        _tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
        _tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

        JButton save_Button;
        JButton cancel_Button;

        save_Button = new JButton("Save");
        cancel_Button = new JButton("Cancel");

        save_Button.setMnemonic(KeyEvent.VK_ENTER);
        cancel_Button.setMnemonic(KeyEvent.VK_ESCAPE);

        save_Button.setActionCommand("Save");
        cancel_Button.setActionCommand("Cancel");

        save_Button.setHorizontalTextPosition(SwingConstants.CENTER);
        cancel_Button.setHorizontalTextPosition(SwingConstants.CENTER);

        for (Object stringParameter : _modelToSave.attributeList()) {

            // If the attribute is a StringParameter that is not the
            // reference indication or the model name AND it is one of the
            // attributes configured in the DB, show it in the panel.
            if (stringParameter instanceof StringParameter && 
                ((StringParameter) stringParameter).getName()!="DBReference" && 
                ((StringParameter) stringParameter).getName()!="DBModelName" &&
                _attributesListPanel.isDBAttribute(((StringParameter) 
                      stringParameter).getName())) {
                
                _attributesListPanel.addAttribute((StringParameter) stringParameter);
                
            }
        
        }

        save_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                try {
                    
                    // If the form is in an invalid state, do not continue;
                    if (!_isValid()) {

                        _rollbackModel();
                        
                        return;

                    }
                    
                    _saveModel();

                } catch (NameDuplicationException e) {

                    JOptionPane
                            .showMessageDialog((Component) event.getSource(),
                                    "The entered name will result in a "
                                            + "duplicate " + "model name.  "
                                            + "Please enter a different name.",
                                    "Save Error",
                                    JOptionPane.INFORMATION_MESSAGE, null);
                    _rollbackModel();

                } catch (IllegalActionException e) {

                    JOptionPane
                            .showMessageDialog((Component) event.getSource(),
                                    "Saving this model will result in "
                                            + "incorrect or "
                                            + "inconsistent data.  "
                                            + "Please cancel and try again.",
                                    "Save Error",
                                    JOptionPane.INFORMATION_MESSAGE, null);
                    _rollbackModel();

                } catch (Exception e) {

                    JOptionPane
                            .showMessageDialog((Component) event.getSource(),
                                    "Could not save the model.  "
                                            + "Please cancel and try again.",
                                    "Save Error",
                                    JOptionPane.INFORMATION_MESSAGE, null);
                    _rollbackModel();
                }

            }
        });

        cancel_Button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                _rollbackModel();
                setVisible(false);

            }

        });

        topPanel.add(_tabbedPane);
        bottomPanel.add(save_Button);
        bottomPanel.add(cancel_Button);
        add(topPanel);
        add(bottomPanel);
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

            throw exception;

        } catch (DBExecutionException exception) {

            throw exception;

        } catch (IllegalArgumentException exception) {

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
        if (_modelToSave.getAttribute("DBReference") != null
                && _modelToSave.getName().equals(_initialModelName)) {
            
            isNew = false;

        }

        return isNew;
    }

    private boolean _isValid() throws NameDuplicationException,
        IllegalActionException {

        if (_attributesListPanel.getModelName().length() == 0) {

            JOptionPane.showMessageDialog(this, "You must enter a Model Name.",
                    "Save Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }
        
        if (!_attributesListPanel.getModelName().matches("^[A-Za-z0-9]+$")){
            
            JOptionPane.showMessageDialog(this,
                    "The model name should only contain letters and numbers.", 
                    "Save Error",
                    JOptionPane.INFORMATION_MESSAGE, null);

            return false;
            
        }
        
        if (_attributesListPanel.containsDuplicates()) {
            
            JOptionPane.showMessageDialog(this,
                    "The model cannot contain more" + " than one instance "
                            + "of the same attribute.", "Save Error",
                    JOptionPane.INFORMATION_MESSAGE, null);
            
            return false;
            
        }
        
        if (!_attributesListPanel.allAttributeNamesSet()) {
            
            JOptionPane.showMessageDialog(this,
                    "You must specify a name for all attributes.", "Save Error",
                    JOptionPane.INFORMATION_MESSAGE, null);
            
            return false;
            
        }

        return true;

    }

    private void _rollbackModel() {

        try{

            ArrayList<StringParameter> attributesList = new ArrayList();

            for (Object a : _modelToSave.attributeList()) {

                if (a instanceof StringParameter) {

                    attributesList.add((StringParameter) a);

                }

            }

            // Delete all existing attributes that are in the
            // set of attributes obtained from the DB.
            for (StringParameter attribute : attributesList) {

                if (attribute.getName()!="DBReference" && 
                        attribute.getName()!="DBModelName" &&
                        _attributesListPanel.isDBAttribute(attribute.getName())){
                    
                    attribute.setContainer(null);
                    
                }

            }
            
            for(StringParameter attribute : _orignialAttributes){
                
                attribute.setContainer(_modelToSave);
                
            }

            MoMLChangeRequest change = new MoMLChangeRequest(
                    this, null, _modelToSave.exportMoML());
            change.setUndoable(true);
                            
            _modelToSave.requestChange(change);
            
        } catch (Exception e){}
        

    }

    private void _saveModel() throws Exception {

        try {

            _modelToSave.setName(_attributesListPanel.getModelName());

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

            if (_modelToSave.getAttribute("DBReference") == null) {

                StringParameter dbModelParam = new StringParameter(
                        _modelToSave, "DBReference");
                dbModelParam.setExpression("FALSE");
                dbModelParam.setContainer(_modelToSave);
                
            }
            
            if (_modelToSave.getAttribute("DBModelName") == null) {

                StringParameter dbModelParam = new StringParameter(
                        _modelToSave, "DBModelName");
                dbModelParam.setExpression(_modelToSave.getName());
                dbModelParam.setContainer(_modelToSave);
                
            } else { 
                
                ((StringParameter)_modelToSave.getAttribute("DBModelName"))
                    .setExpression(_modelToSave.getName());
                
            }

            ArrayList<StringParameter> attributesList = new ArrayList();

            for (Object a : _modelToSave.attributeList()) {

                if (a instanceof StringParameter) {

                    attributesList.add((StringParameter) a);

                }

            }

            // Delete all existing attributes that are in the
            // set of attributes obtained from the DB.
            for (StringParameter attribute : attributesList) {

                if (attribute.getName()!="DBReference" && 
                        attribute.getName()!="DBModelName" &&
                        _attributesListPanel.isDBAttribute(attribute.getName())){
                    
                    attribute.setContainer(null);
                    
                }

            }

            // Get all attributes we have displayed.
           for(Attribute attributeToAdd : _attributesListPanel.getAttributes()){

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
                                + "fill:blue\" " + "y=\"20\">"
                                + "</text></svg>");

                VisibleParameterEditorFactory vpef = new VisibleParameterEditorFactory(
                        attributeToAdd, "_editorFactory");
                vpef.setContainer(attributeToAdd);

                // TODO Figure out how to place in unique location.
                double[] xy = { 250, 170 };
                Location l = new Location(attributeToAdd,
                        "_location");
                l.setLocation(xy);

            }
            
            try {

                MoMLChangeRequest change = new MoMLChangeRequest(
                        this, null, _modelToSave.exportMoML());
                change.setUndoable(true);
                                
                _modelToSave.requestChange(change);
                
            } catch (Exception e) {
                throw e;
            }

        } catch (NameDuplicationException exception) {

            throw exception;

        } catch (IllegalActionException exception) {

            throw exception;

        }

    }

    ///////////////////////////////////////////////////////////////////
    //                    private variables                        ////    
    
    private JTabbedPane _tabbedPane;
    private NamedObj _modelToSave;
    private String _initialModelName;
    private AttributesListPanel _attributesListPanel;
    private XMLDBModel xmlModel;
    private ArrayList<StringParameter> _orignialAttributes;
    
}
