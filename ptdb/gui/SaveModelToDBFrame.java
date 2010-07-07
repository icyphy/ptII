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
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.util.Utilities;
import ptdb.kernel.bl.save.SaveModelManager;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;

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
                
                if (((StringParameter) attribute).getName()!= XMLDBModel.DB_REFERENCE_ATTR && 
                        ((StringParameter) attribute).getName()!=XMLDBModel.DB_MODEL_ID_ATTR &&
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
                ((StringParameter) stringParameter).getName()!= XMLDBModel.DB_REFERENCE_ATTR && 
                ((StringParameter) stringParameter).getName()!=XMLDBModel.DB_MODEL_ID_ATTR &&
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

                    MessageHandler.error("The model cannot be saved now " +
                            "due to a NameDuplicationException.", e);

                    _rollbackModel();

                } catch (IllegalActionException e) {

                    MessageHandler.error("The model cannot be saved now " +
                            "due to an IllegalActionException.", e);
                    
                    _rollbackModel();

                } catch (Exception e) {

                    MessageHandler.error("The model cannot be saved now " +
                            "due to an Exception.", e);
                    
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

    private void _commitSave(boolean isNew, String id) throws Exception {

        _updateDisplayedModel();

        xmlModel = new XMLDBModel(_modelToSave.getName());
        xmlModel.setModel(_modelToSave.exportMoML());
        xmlModel.setIsNew(isNew);
        xmlModel.setModelId(id);
        
        SaveModelManager saveModelManager = new SaveModelManager();

        try {

            String modelId = saveModelManager.save(xmlModel);
            
            if (modelId != null) {

                JOptionPane.showMessageDialog(this,
                        "The model was successfully saved.", "Success",
                        JOptionPane.INFORMATION_MESSAGE, null);

                if (_modelToSave.getAttribute(XMLDBModel.DB_MODEL_ID_ATTR) == null) {
                    
                    StringParameter dbModelParam = new StringParameter(
                            _modelToSave, XMLDBModel.DB_MODEL_ID_ATTR);
                    dbModelParam.setExpression(modelId);
                    dbModelParam.setContainer(_modelToSave);
                    
                } else if(!((StringParameter)
                        _modelToSave.getAttribute(XMLDBModel.DB_MODEL_ID_ATTR)).getExpression()
                        .equals(modelId)){
                    
                    ((StringParameter)
                            _modelToSave.getAttribute(XMLDBModel.DB_MODEL_ID_ATTR))
                            .setExpression(modelId);
                    
                }
                
                try {

                    MoMLChangeRequest change = new MoMLChangeRequest(
                            this, null, _modelToSave.exportMoML());
                    change.setUndoable(true);
                                    
                    _modelToSave.requestChange(change);
                    
                } catch (Exception e) {
                    throw e;
                }
                
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
                _commitSave(false, null);

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
        if (_modelToSave.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null
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
        
        if (!_attributesListPanel.allAttributeValuesSet()){
            
            JOptionPane.showMessageDialog(this,
                    "You must specify a value for all attributes.", 
                    "Save Error",
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

                if (attribute.getName()!=XMLDBModel.DB_REFERENCE_ATTR && 
                        attribute.getName()!=XMLDBModel.DB_MODEL_ID_ATTR &&
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
            
            String id = null;
            
            if (_modelToSave.getAttribute(XMLDBModel.DB_MODEL_ID_ATTR) != null){

                id = ((StringParameter)_modelToSave.getAttribute(XMLDBModel.DB_MODEL_ID_ATTR))
                    .getExpression();
            
            }

            _commitSave(_isNew(), id);

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

            if (_modelToSave.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) == null) {

                StringParameter dbModelParam = new StringParameter(
                        _modelToSave, XMLDBModel.DB_REFERENCE_ATTR);
                dbModelParam.setExpression("FALSE");
                dbModelParam.setContainer(_modelToSave);
                
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

                if (attribute.getName()!= XMLDBModel.DB_REFERENCE_ATTR && 
                        attribute.getName()!= XMLDBModel.DB_MODEL_ID_ATTR &&
                        _attributesListPanel.isDBAttribute(attribute.getName())){
                    
                    attribute.setContainer(null);
                    
                }

            }

            // Get all attributes we have displayed.
           for(Attribute attributeToAdd : _attributesListPanel.getAttributes()){

               attributeToAdd.setContainer(_modelToSave);

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
