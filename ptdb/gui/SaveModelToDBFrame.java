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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.dto.XMLDBModelWithReferenceChanges;
import ptdb.common.exception.CircularDependencyException;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.IllegalNameException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.exception.UnSavedParentModelsException;
import ptdb.common.util.Utilities;
import ptdb.kernel.bl.load.LoadManager;
import ptdb.kernel.bl.save.SaveModelManager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.expr.StringConstantParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
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
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 *
 */
@SuppressWarnings("serial")
public class SaveModelToDBFrame extends JFrame implements PTDBBasicFrame {

    /**
     * Construct a SaveModelToDBFrame. Add swing Components to the frame. Add
     * listeners for the "Save" and "Cancel" buttons.
     *
     * @param model The model that is being saved to the database.
     * @param source The source frame.  Used to set modified to false upon
     *          successful save.
     *
     */
    public SaveModelToDBFrame(NamedObj model, ActorGraphDBFrame source) {

        super("Save Model to Database");

        setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        _source = source;
        _modelToSave = model;
        _initialModelName = model.getName();
        _orignialAttributes = new ArrayList();
        _attributesListPanel = new AttributesListPanel(_modelToSave);
        _tabbedPane = new JTabbedPane();

        try {
            if (!_isNew()) {
                _xmlModel = new XMLDBModel(_modelToSave.getName());
                _xmlModel.setModelId(Utilities.getIdFromModel(_modelToSave));
            }
        } catch (NameDuplicationException e1) {
            // skip
        } catch (IllegalActionException e1) {
            // skip
        }

        //Create a list of the original attributes
        for (Object attribute : _modelToSave.attributeList()) {

            if (attribute instanceof StringParameter) {

                if (!((StringParameter) attribute).getName().equals(
                        XMLDBModel.DB_REFERENCE_ATTR)
                        && !((StringParameter) attribute).getName().equals(
                                XMLDBModel.DB_MODEL_ID_ATTR)
                                && _attributesListPanel
                                .isDBAttribute(((StringParameter) attribute)
                                        .getName())) {

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

        _saveButton = new JButton("Save");
        _cancelButton = new JButton("Cancel");
        _nextButton = new JButton("Next >>");

        _saveButton.setMnemonic(KeyEvent.VK_ENTER);
        _cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
        _nextButton.setMnemonic(KeyEvent.VK_RIGHT);

        _saveButton.setActionCommand("Save");
        _cancelButton.setActionCommand("Cancel");
        _nextButton.setActionCommand("Next");

        _saveButton.setHorizontalTextPosition(SwingConstants.CENTER);
        _cancelButton.setHorizontalTextPosition(SwingConstants.CENTER);
        _nextButton.setHorizontalTextPosition(SwingConstants.CENTER);

        for (Object stringParameter : _modelToSave.attributeList()) {

            // If the attribute is a StringParameter that is not the
            // reference indication or the model name AND it is one of the
            // attributes configured in the DB, show it in the panel.
            if (stringParameter instanceof StringParameter
                    && !((StringParameter) stringParameter).getName().equals(
                            XMLDBModel.DB_REFERENCE_ATTR)
                            && !((StringParameter) stringParameter).getName().equals(
                                    XMLDBModel.DB_MODEL_ID_ATTR)
                                    && _attributesListPanel
                                    .isDBAttribute(((StringParameter) stringParameter)
                                            .getName())) {

                _attributesListPanel
                .addAttribute((StringParameter) stringParameter);

            }

        }

        _saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                _save();

            }
        });

        _cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {

                _rollbackModel();
                setVisible(false);

                if (_parentValidateFrame != null) {
                    _parentValidateFrame.setVisible(false);
                }
            }

        });

        _nextButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (_parentValidateFrame == null) {
                    _parentValidateFrame = new ParentValidateFrame(
                            SaveModelToDBFrame.this);
                }

                _parentValidateFrame.pack();
                _parentValidateFrame
                .setLocationRelativeTo(SaveModelToDBFrame.this);
                _parentValidateFrame.setVisible(true);

                // Hide the first frame.
                setVisible(false);

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

                        if (!_attributesListPanel.getNameTextField().getText()
                                .equals(_initialModelName)) {
                            // Disable the next button if the user has changed
                            // the model name.
                            _nextButton.setEnabled(false);
                            _saveButton.setEnabled(true);

                        } else {
                            _setButtons();
                        }
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {

                        /*
                         * If the enter button is pressed, perform the action
                         *  based on the enabled button.
                         */
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            if (_saveButton.isEnabled()) {

                                _saveButton.getActionListeners()[0]
                                        .actionPerformed(null);

                            } else if (_nextButton.isEnabled()) {

                                _nextButton.getActionListeners()[0]
                                        .actionPerformed(null);
                            }
                        }
                    }
                });

        topPanel.add(_tabbedPane);
        bottomPanel.add(_saveButton);
        bottomPanel.add(_cancelButton);
        bottomPanel.add(_nextButton);

        add(topPanel);
        add(bottomPanel);
        validate();
        repaint();

        // The next button should be disabled if there is no parent of
        // the saving model.
        // The save button should be disabled if there are parents of the
        // saving model.
        _setButtons();

        this.pack();

        _attributesListPanel.setModelNameFocus();

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Close this window.
     */

    @Override
    public void closeFrame() {
        if (_parentValidateFrame != null) {
            _parentValidateFrame.dispose();
        }

        dispose();
    }

    ///////////////////////////////////////////////////////////////////
    //                    private methods                          ////

    /**
     * Save the model to the database, together with the references changes
     * to its parent models.
     *
     * @param isNew Whether this is a new model.
     * @param id The id of this model.
     * @param parentsMaintainOldVersion The list of parents names that do not want
     * to reflect the changes to the submodel they have.
     * @param newVersionName The new name of this saving model, to have those
     * parent models maintaining the old reference.
     * @exception Exception Thrown if errors occur during the saving.
     */
    private void _commitSave(boolean isNew, String id,
            ArrayList<String> parentsMaintainOldVersion, String newVersionName)
                    throws Exception {

        String newName = _attributesListPanel.getModelName();
        _modelToSave.setName(newName);
        _updateDisplayedModel();

        if (isNew || id == null) {

            // If the Model ID is in the MoML, remove it.
            if (_modelToSave.getAttribute(XMLDBModel.DB_MODEL_ID_ATTR) != null) {

                _modelToSave.getAttribute(XMLDBModel.DB_MODEL_ID_ATTR)
                .setContainer(null);

                try {

                    MoMLChangeRequest change = new MoMLChangeRequest(this,
                            null, _modelToSave.exportMoML());
                    change.setUndoable(true);

                    _modelToSave.requestChange(change);

                } catch (Exception e) {
                    _resetValues();
                    throw e;
                }
            }

        }

        if (_xmlModel == null) {
            _xmlModel = new XMLDBModel(_modelToSave.getName());
        }
        _xmlModel.setModelName(_modelToSave.getName());
        _xmlModel.setModel(_modelToSave.exportMoML());
        _xmlModel.setIsNew(isNew);
        _xmlModel.setModelId(id);

        XMLDBModelWithReferenceChanges xmlDBModelWithReferenceChanges = new XMLDBModelWithReferenceChanges(
                _xmlModel, parentsMaintainOldVersion, newVersionName);

        try {

            String modelId = _saveModelManager
                    .saveWithParents(xmlDBModelWithReferenceChanges);

            if (modelId != null) {

                JOptionPane.showMessageDialog(this,
                        "The model was successfully saved.", "Success",
                        JOptionPane.INFORMATION_MESSAGE, null);

                // Update the MoMl of the saving model.
                if (_modelToSave.getAttribute(XMLDBModel.DB_MODEL_ID_ATTR) == null) {

                    StringConstantParameter dbModelParam = new StringConstantParameter(
                            _modelToSave, XMLDBModel.DB_MODEL_ID_ATTR);
                    dbModelParam.setExpression(modelId);
                    dbModelParam.setContainer(_modelToSave);

                } else if (!((StringParameter) _modelToSave
                        .getAttribute(XMLDBModel.DB_MODEL_ID_ATTR))
                        .getExpression().equals(modelId)) {

                    ((StringParameter) _modelToSave
                            .getAttribute(XMLDBModel.DB_MODEL_ID_ATTR))
                            .setExpression(modelId);

                }

                try {

                    MoMLChangeRequest change = new MoMLChangeRequest(this,
                            null, _modelToSave.exportMoML());
                    change.setUndoable(true);

                    _modelToSave.requestChange(change);
                    _source.setTitle(_xmlModel.getModelName());

                    try {
                        _source.updateDBModelHistory(_xmlModel.getModelName(),
                                false);

                    } catch (IOException e) {
                        // Ignore if recent files are not updated.
                    }

                } catch (Exception e) {
                    throw e;
                }

                //                // Update the parent models that are opened already.
                //                // Update those parents with unchanged sub model first.
                //                if (parentsMaintainOldVersion != null
                //                        && parentsMaintainOldVersion.size() > 0) {
                //
                //                    XMLDBModel newVersionModel = DBModelFetcher
                //                            .load(newVersionName);
                //
                //                    for (String parentName : parentsMaintainOldVersion) {
                //
                //                        if (_source.getConfiguration().getDirectory()
                //                                .getEntity(parentName) != null) {
                //                            // This parent model is opened.
                //
                //                            PtolemyEffigy parentModelEffigy = (PtolemyEffigy) _source
                //                                    .getConfiguration().getDirectory()
                //                                    .getEffigy(parentName);
                //
                //                            boolean modifiedFlag = parentModelEffigy
                //                                    .isModified();
                //
                //                            for (Object entity : ((CompositeEntity) parentModelEffigy
                //                                    .getModel()).entityList()) {
                //
                //                                ComponentEntity componentEntity = (ComponentEntity) entity;
                //
                //                                if (Utilities.getIdFromModel(componentEntity) != null
                //                                        && Utilities.getIdFromModel(
                //                                                componentEntity)
                //                                                .equals(modelId)) {
                //                                    // Update the original model id to the new
                //                                    // version model id.
                //                                    StringParameter modelIdAttribute = (StringParameter) componentEntity
                //                                            .getAttribute(XMLDBModel.DB_MODEL_ID_ATTR);
                //
                //                                    modelIdAttribute
                //                                            .setExpression(newVersionModel
                //                                                    .getModelId());
                //                                    componentEntity.setName(parentModelEffigy
                //                                            .getModel().uniqueName(
                //                                                    newVersionModel
                //                                                            .getModelName()));
                //
                //                                    String momlString = componentEntity
                //                                            .exportMoML();
                //                                    //                                    componentEntity.setContainer(null);
                //                                    if (componentEntity instanceof CompositeEntity) {
                //                                        CompositeEntity compositeEntity = (CompositeEntity) componentEntity;
                //                                        compositeEntity.removeAllEntities();
                //                                    }
                //
                //                                    // Update the MoML of the opened parent
                //                                    MoMLChangeRequest change = new MoMLChangeRequest(
                //                                            null, parentModelEffigy.getModel(),
                //                                            momlString);
                //
                //                                    change.setUndoable(true);
                //                                    parentModelEffigy.getModel().requestChange(
                //                                            change);
                //
                //                                }
                //                            }
                //
                //                            // If that model hasn't changed, set the changed
                //                            // to false.
                //                            if (!modifiedFlag) {
                //                                parentModelEffigy.setModified(false);
                //                            }
                //
                //                        }
                //
                //                    }
                //                }

                //                // Update the parent models that want to maintain the reference.
                //                if (_parentValidateFrame != null) {
                //                    ArrayList<String> parentsModelsMaintainReferences = _parentValidateFrame
                //                            ._getParentsMaintainReferences();
                //                    if (parentsModelsMaintainReferences != null
                //                            && parentsModelsMaintainReferences.size() > 0) {
                //
                //                        // fetch the saved sub model from the database, to
                //                        // update the information in the parent model.
                //                        XMLDBModel savedModel = DBModelFetcher
                //                                .loadUsingId(modelId);
                //
                //                        MoMLParser parser = new MoMLParser();
                //                        parser.resetAll();
                //
                //                        Entity savedSubModel = (Entity) parser.parse(savedModel
                //                                .getModel());
                //
                //                        StringParameter referenceAttribute = null;
                //
                //                        if (savedSubModel
                //                                .getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null) {
                //
                //                            referenceAttribute = (StringParameter) savedSubModel
                //                                    .getAttribute(XMLDBModel.DB_REFERENCE_ATTR);
                //
                //                        } else {
                //                            referenceAttribute = new StringParameter(
                //                                    savedSubModel, XMLDBModel.DB_REFERENCE_ATTR);
                //
                //                        }
                //
                //                        referenceAttribute.setExpression("TRUE");
                //
                //                        for (String parentName : parentsModelsMaintainReferences) {
                //                            if (_source.getConfiguration().getDirectory()
                //                                    .getEntity(parentName) != null) {
                //                                // This parent model is opened.
                //
                //                                PtolemyEffigy parentModelEffigy = (PtolemyEffigy) _source
                //                                        .getConfiguration().getDirectory()
                //                                        .getEffigy(parentName);
                //
                //                                boolean modifiedFlag = parentModelEffigy
                //                                        .isModified();
                //
                //                                for (Object entity : ((CompositeEntity) parentModelEffigy
                //                                        .getModel()).entityList()) {
                //
                //                                    ComponentEntity componentEntity = (ComponentEntity) entity;
                //
                //                                    if (Utilities
                //                                            .getIdFromModel(componentEntity) != null
                //                                            && Utilities.getIdFromModel(
                //                                                    componentEntity).equals(
                //                                                    modelId)) {
                //                                        // Update the moml of that sub model.
                //                                        savedSubModel.setName(componentEntity
                //                                                .getName());
                //
                //                                        // Set the location attribute.
                //                                        Location location = (Location) savedSubModel
                //                                                .getAttribute("_location");
                //                                        if (location == null) {
                //                                            location = new Location(
                //                                                    savedSubModel, "_location");
                //                                        }
                //
                //                                        location
                //                                                .setExpression(((Location) componentEntity
                //                                                        .getAttribute("_location"))
                //                                                        .getExpression());
                //
                //                                        String newMoml = savedSubModel
                //                                                .exportMoML();
                //
                //                                        if (componentEntity instanceof CompositeEntity) {
                //                                            CompositeEntity compositeEntity = (CompositeEntity) componentEntity;
                //                                            compositeEntity.removeAllEntities();
                //                                        }
                //
                //                                        MoMLChangeRequest change = new MoMLChangeRequest(
                //                                                null, parentModelEffigy
                //                                                        .getModel(), newMoml);
                //
                //                                        change.setUndoable(true);
                //                                        parentModelEffigy.getModel()
                //                                                .requestChange(change);
                //
                //                                    }
                //                                }
                //
                //                                // If that model hasn't changed, set the changed
                //                                // to false.
                //                                if (!modifiedFlag) {
                //                                    parentModelEffigy.setModified(false);
                //                                }
                //
                //                            }
                //                        }
                //                    }
                //                }
                ArrayList<String> openedParents = null;

                if (_parentValidateFrame != null) {
                    openedParents = new ArrayList<String>();

                    for (String openedParentModelName : _parentValidateFrame
                            ._getOpenedParents()) {

                        openedParents.add(openedParentModelName);
                    }
                }

                _source.setModified(false);

                if (_parentValidateFrame != null) {
                    _parentValidateFrame.dispose();
                }
                dispose();

                // Reload the opened parent models.
                if (openedParents != null) {

                    for (String openedParentModelName : openedParents) {

                        // Close the opened parent models first.
                        PtolemyEffigy parentModelEffigy = (PtolemyEffigy) _source
                                .getConfiguration().getDirectory()
                                .getEffigy(openedParentModelName);
                        parentModelEffigy.closeTableaux();

                        parentModelEffigy.setContainer(null);

                        // Reload the model from the Database.
                        PtolemyEffigy updatedParentModelEffigy = LoadManager
                                .loadModel(openedParentModelName,
                                        _source.getConfiguration());

                        if (updatedParentModelEffigy != null) {

                            updatedParentModelEffigy.showTableaux();

                        }
                    }

                }

                // Display the saved sub model to the front.
                if (_source.getConfiguration().getDirectory()
                        .getEffigy(_xmlModel.getModelName()) != null) {
                    _source.getConfiguration().getDirectory()
                    .getEffigy(_xmlModel.getModelName()).showTableaux();
                }

            } else {

                JOptionPane.showMessageDialog(this,
                        "A problem occurred while saving.", "Save Error",
                        JOptionPane.INFORMATION_MESSAGE, null);

            }

        } catch (DBConnectionException exception) {
            _resetValues();
            throw exception;

        } catch (DBExecutionException exception) {
            _resetValues();
            throw exception;

        } catch (IllegalArgumentException exception) {
            _resetValues();
            throw exception;

        } catch (ModelAlreadyExistException exception) {
            _resetValues();
            JOptionPane.showMessageDialog(this,
                    "A model with the new version name already"
                            + " exists in the database. Please use"
                            + " another name.");
            _rollbackModel();
        }

    }

    /**
     * Check whether the given model has parents.
     *
     * @return true - if there are parents of the given model.<br>
     *  False - if there is no parent for the given model.
     * @exception DBExecutionException Thrown if error happens during the
     *   execution of this operation in the db layer.
     * @exception DBConnectionException Thrown if db connection cannot be
     * obtained.
     */
    private boolean _hasParents() throws DBConnectionException,
    DBExecutionException {

        if (_hasParentFlag == false) {
            return false;
        }

        if (_hasParentFlag == true && _parentModels == null) {
            // Has not verified whether the saving model has parents yet.
            if (Utilities.getIdFromModel(_modelToSave) != null) {
                _parentModels = _saveModelManager
                        .getFirstLevelParents(_xmlModel);

                // In the case of no parents, set the has parent flag to false.
                if (_parentModels == null || _parentModels.size() == 0) {
                    _hasParentFlag = false;
                    _parentModels = null;
                    return false;
                }

                return true;
            } else {
                // New Model
                return false;
            }

        }

        return true;

    }

    private boolean _isNew() throws NameDuplicationException,
    IllegalActionException {

        boolean isNew = true;

        // It is not new if the model has the DBModel tag
        // and the model name is still the same.
        String newName = _attributesListPanel.getModelName();
        if (_modelToSave.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) != null
                && newName.equals(_initialModelName)) {

            isNew = false;

        }

        return isNew;
    }

    private boolean _isValid() throws NameDuplicationException,
    IllegalActionException, HeadlessException, IllegalNameException {

        if (_attributesListPanel.getModelName().length() == 0) {

            JOptionPane.showMessageDialog(this, "You must enter a Model Name.",
                    "Save Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        if (!Utilities.checkModelName(_attributesListPanel.getModelName())) {

            JOptionPane.showMessageDialog(this,
                    "The model name should only contain letters and numbers.",
                    "Save Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        if (_attributesListPanel.containsDuplicates()) {

            JOptionPane.showMessageDialog(this, "The model cannot contain more"
                    + " than one instance " + "of the same attribute.",
                    "Save Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        if (!_attributesListPanel.allAttributeNamesSet()) {

            JOptionPane.showMessageDialog(this,
                    "You must specify a name for all attributes.",
                    "Save Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        if (!_attributesListPanel.allAttributeValuesSet()) {

            JOptionPane.showMessageDialog(this,
                    "You must specify a value for all attributes.",
                    "Save Error", JOptionPane.INFORMATION_MESSAGE, null);

            return false;

        }

        return true;

    }

    /**
     * Reset the model name to the old name in the NamedObj.
     */
    private void _resetValues() {
        try {
            _modelToSave.setName(_initialModelName);
        } catch (IllegalActionException e) {

        } catch (NameDuplicationException e) {

        }
    }

    private void _rollbackModel() {

        try {

            ArrayList<StringParameter> attributesList = new ArrayList();

            for (Object a : _modelToSave.attributeList()) {

                if (a instanceof StringParameter) {

                    attributesList.add((StringParameter) a);

                }

            }

            // Delete all existing attributes that are in the
            // set of attributes obtained from the DB.
            for (StringParameter attribute : attributesList) {

                if (!attribute.getName().equals(XMLDBModel.DB_REFERENCE_ATTR)
                        && !attribute.getName().equals(
                                XMLDBModel.DB_MODEL_ID_ATTR)
                                && _attributesListPanel.isDBAttribute(attribute
                                        .getName())) {

                    attribute.setContainer(null);

                }

            }

            for (StringParameter attribute : _orignialAttributes) {

                attribute.setContainer(_modelToSave);

            }

            MoMLChangeRequest change = new MoMLChangeRequest(this, null,
                    _modelToSave.exportMoML());
            change.setUndoable(true);

            _modelToSave.requestChange(change);

        } catch (Throwable throwable) {
        } // Intentionally, we do nothing.

    }

    /**
     * Perform the saving of the model.
     */
    private void _save() {
        try {

            // If the form is in an invalid state, do not continue;
            if (!_isValid()) {

                _rollbackModel();

                return;
            }

            // Verify the data in the second parent validation frame.
            if (_parentValidateFrame != null) {
                if (!_parentValidateFrame._isValid()) {

                    _rollbackModel();
                    return;
                }
            }

            _saveModel();

        } catch (NameDuplicationException e) {

            MessageHandler.error("The model cannot be saved now "
                    + "due to a NameDuplicationException.", e);

            _rollbackModel();

        } catch (IllegalActionException e) {

            MessageHandler.error("The model cannot be saved now "
                    + "due to an IllegalActionException.", e);

            _rollbackModel();

        } catch (CircularDependencyException e) {

            JOptionPane.showMessageDialog(this, "Saving this model as it is "
                    + "will result in a circular dependency.  Examine "
                    + "the referenced models to determine the cause.",
                    "Save Error", JOptionPane.INFORMATION_MESSAGE, null);

            _rollbackModel();

        } catch (UnSavedParentModelsException e) {

            JOptionPane.showMessageDialog(this, e.getMessage(), "Save Error",
                    JOptionPane.INFORMATION_MESSAGE, null);

            _rollbackModel();

        } catch (Exception e) {

            MessageHandler.error("The model cannot be saved now "
                    + "due to an Exception.", e);

            _rollbackModel();

        }

    }

    private void _saveModel() throws Exception {

        ArrayList<String> unchangedParents = null;
        String newVersionName = null;

        try {
            String newName = _attributesListPanel.getModelName();

            if (_initialModelName != null && _initialModelName.length() > 0) {

                if (!newName.equals(_initialModelName)) {

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

                } else {
                    // Save the parent models that are chosen to maintain the old model.

                    if (_parentValidateFrame != null
                            && _parentValidateFrame._hasParentsWithNewVersion()) {

                        unchangedParents = _parentValidateFrame
                                ._getParentsWithNewVersion();
                        newVersionName = _parentValidateFrame
                                ._getNewVersionName();
                    }
                }
            }

            _commitSave(_isNew(), Utilities.getIdFromModel(_modelToSave),
                    unchangedParents, newVersionName);

        } catch (DBConnectionException exception) {

            throw exception;

        } catch (DBExecutionException exception) {

            throw exception;

        } catch (IllegalArgumentException exception) {

            throw exception;

        }

    }

    private void _setButtons() {

        try {
            if (_hasParents()) {
                _nextButton.setEnabled(true);

                if (_parentValidateFrame == null) {
                    _saveButton.setEnabled(false);
                } else {
                    _saveButton.setEnabled(true);
                }

            } else {
                _nextButton.setEnabled(false);
                _saveButton.setEnabled(true);
            }
        } catch (DBConnectionException e1) {
            JOptionPane.showMessageDialog(this,
                    "Cannot fetch the parent models for this model.");
            _nextButton.setEnabled(false);
            _saveButton.setEnabled(false);
        } catch (DBExecutionException e1) {
            JOptionPane.showMessageDialog(this,
                    "Cannot fetch the parent models for this model.");
            _nextButton.setEnabled(false);
            _saveButton.setEnabled(false);
        }

    }

    private void _updateDisplayedModel() throws Exception {

        try {

            if (_modelToSave.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) == null) {

                StringConstantParameter dbModelParam = new StringConstantParameter(
                        _modelToSave, XMLDBModel.DB_REFERENCE_ATTR);
                dbModelParam.setExpression("FALSE");
                dbModelParam.setContainer(_modelToSave);
                dbModelParam.setVisibility(Settable.NONE);

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

                if (attribute.getName().equals(XMLDBModel.DB_REFERENCE_ATTR)
                        && attribute.getName().equals(
                                XMLDBModel.DB_MODEL_ID_ATTR)
                                && _attributesListPanel.isDBAttribute(attribute
                                        .getName())) {

                    attribute.setContainer(null);

                }

            }

            // Get all attributes we have displayed.
            for (Attribute attributeToAdd : _attributesListPanel
                    .getAttributes()) {

                attributeToAdd.setContainer(_modelToSave);

            }

            try {

                MoMLChangeRequest change = new MoMLChangeRequest(this, null,
                        _modelToSave.exportMoML());
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

    private AttributesListPanel _attributesListPanel;

    private JButton _cancelButton;

    private boolean _hasParentFlag = true;

    private String _initialModelName;

    private NamedObj _modelToSave;

    private JButton _nextButton;

    private ArrayList<StringParameter> _orignialAttributes;

    private ArrayList<XMLDBModel> _parentModels;

    private ParentValidateFrame _parentValidateFrame;

    private JButton _saveButton;

    private SaveModelManager _saveModelManager = new SaveModelManager();

    private ActorGraphDBFrame _source;

    private JTabbedPane _tabbedPane;

    private XMLDBModel _xmlModel;

    ///////////////////////////////////////////////////////////////////
    ////                    private inner classes                  ////

    private class ParentValidateFrame extends JFrame {

        /**
         * Construct the parent validate frame.
         *
         * @param firstFrameOfSave The first frame of the saving wizard.
         */
        public ParentValidateFrame(SaveModelToDBFrame firstFrameOfSave) {

            super(firstFrameOfSave.getTitle());

            setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

            setPreferredSize(firstFrameOfSave.getSize());

            _firstFrameOfSave = firstFrameOfSave;

            JPanel topPanel = new JPanel();
            topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));

            JPanel mainPanel = new JPanel();

            JTabbedPane tabbedPane = new JTabbedPane();

            tabbedPane.setAlignmentX(LEFT_ALIGNMENT);

            tabbedPane.setAlignmentY(TOP_ALIGNMENT);

            tabbedPane.setLayout(new BoxLayout(tabbedPane, BoxLayout.Y_AXIS));

            tabbedPane.addTab("Validate Parent Models", mainPanel);

            tabbedPane.setMnemonicAt(0, KeyEvent.VK_1);
            tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
            tabbedPane.setPreferredSize(new Dimension(800, 500));

            _modelNamePanel = new JPanel();
            _parentsPanel = new JPanel();
            _bottomPanel = new JPanel();

            _modelNamePanel.setMaximumSize(new Dimension(800, 30));

            _modelNamePanel.setBorder(BorderFactory.createEtchedBorder());

            _modelNamePanel.setLayout(new BoxLayout(_modelNamePanel,
                    BoxLayout.X_AXIS));

            _parentsPanel.setLayout(new BoxLayout(_parentsPanel,
                    BoxLayout.Y_AXIS));

            JPanel explanationPanel = new JPanel();
            explanationPanel.setAlignmentX(LEFT_ALIGNMENT);
            explanationPanel.setAlignmentY(TOP_ALIGNMENT);

            explanationPanel.setLayout(new BoxLayout(explanationPanel,
                    BoxLayout.Y_AXIS));

            mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

            JScrollPane parentsScrollPane = new JScrollPane(_parentsPanel,
                    ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                    ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

            parentsScrollPane.setAlignmentX(LEFT_ALIGNMENT);
            parentsScrollPane.setAlignmentY(TOP_ALIGNMENT);
            parentsScrollPane.setLayout(new ScrollPaneLayout());
            parentsScrollPane.setPreferredSize(new Dimension(800, 500));

            mainPanel.add(explanationPanel);

            mainPanel.add(_modelNamePanel);
            mainPanel.add(parentsScrollPane);
            mainPanel.setPreferredSize(new Dimension(800, 500));

            JLabel explanationLabel = new JLabel(
                    "The following models are the parent models for this"
                            + " saving model.");
            JLabel explanationLabel2 = new JLabel(
                    "If you do not want to reflect"
                            + " the change to some parent models, please uncheck"
                            + " them.");

            explanationLabel.setAutoscrolls(true);

            explanationPanel.add(explanationLabel);
            explanationPanel.add(explanationLabel2);
            explanationPanel.add(new JSeparator());

            _modelNamePanel.setAlignmentX(LEFT_ALIGNMENT);
            _parentsPanel.setAlignmentX(LEFT_ALIGNMENT);
            _bottomPanel.setAlignmentX(LEFT_ALIGNMENT);

            _modelNamePanel.setAlignmentY(TOP_ALIGNMENT);
            _parentsPanel.setAlignmentY(TOP_ALIGNMENT);
            _bottomPanel.setAlignmentY(TOP_ALIGNMENT);

            _newModelNameLable = new JLabel(
                    "Please input the new name to replace this model for these parents: ");

            // paint the hidden field for inputting the new model name
            _newModelNameTextField = new JTextField();

            _modelNamePanel.add(_newModelNameLable);
            _modelNamePanel.add(_newModelNameTextField);

            // set the visibility to false at the beginning
            _showModelNameField(false);

            // Get the list of parent models from _parentModels of the parent
            // frame SaveModelToDBFrame, paint the models one by one in the
            // frame, order by name.
            _parentModelsPanels = new ArrayList<ParentModelItemPanel>();

            Collections.sort(_firstFrameOfSave._parentModels);

            for (XMLDBModel parentModel : _firstFrameOfSave._parentModels) {

                ParentModelItemPanel parentModelItemPanel = new ParentModelItemPanel(
                        parentModel.getModelName(),
                        _firstFrameOfSave._source.getConfiguration(), this);

                _parentModelsPanels.add(parentModelItemPanel);
                _parentsPanel.add(parentModelItemPanel);

            }

            _previousButton = new JButton("<< Previous");
            _previousButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    setVisible(false);
                    _firstFrameOfSave.setVisible(true);

                    _firstFrameOfSave._saveButton.setEnabled(true);

                }
            });

            // Add the save button
            _saveButton = new JButton("Save");

            _saveButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    // Add the action listener to call the save button in the
                    // previous frame, when this button is clicked.

                    _firstFrameOfSave._saveButton.setEnabled(true);
                    _firstFrameOfSave._saveButton.doClick();

                }
            });

            // Add the previous button
            _cancelButton = new JButton("Cancel");

            _cancelButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    // Add the action listener to call the cancel button in the
                    // previous frame, when this button is clicked.

                    _firstFrameOfSave._cancelButton.doClick();

                }
            });

            topPanel.add(tabbedPane);

            _bottomPanel.add(_previousButton);
            _bottomPanel.add(_saveButton);
            _bottomPanel.add(_cancelButton);

            add(topPanel);
            add(_bottomPanel);

            validate();
            repaint();

        }

        ///////////////////////////////////////////////////////////////////
        ////                  private methods                          ////

        private String _getNewVersionName() {

            return _newModelNameTextField.getText();
        }

        private ArrayList<String> _getParentsWithNewVersion() {
            ArrayList<String> parents = null;

            if (_parentModelsPanels != null) {
                parents = new ArrayList<String>();

                for (ParentModelItemPanel parentModelItemPanel : _parentModelsPanels) {
                    if (!parentModelItemPanel.isSelected()) {
                        parents.add(parentModelItemPanel.getParentModelName());
                    }
                }
            }

            return parents;
        }

        private ArrayList<String> _getOpenedParents() {

            ArrayList<String> parents = new ArrayList<String>();

            for (ParentModelItemPanel parentModelPanel : _parentModelsPanels) {
                String parentModelName = parentModelPanel.getParentModelName();

                // If the parent model is opened.
                if (_source.getConfiguration().getDirectory()
                        .getEntity(parentModelName) != null) {

                    parents.add(parentModelName);

                }
            }

            return parents;
        }

        //        private ArrayList<String> _getParentsMaintainReferences() {
        //
        //            ArrayList<String> parents = null;
        //
        //            if (_parentModelsPanels != null) {
        //                parents = new ArrayList<String>();
        //
        //                for (ParentModelItemPanel parentModelItemPanel : _parentModelsPanels) {
        //                    if (parentModelItemPanel.isSelected()) {
        //                        parents.add(parentModelItemPanel.getParentModelName());
        //                    }
        //                }
        //            }
        //
        //            return parents;
        //        }

        /**
         * Check whether there is any parent being chosen to have a new
         * version model name for the saving model, which means that parent
         *  model has its check box unchecked.
         *
         * @return True - when there are some parent models chosen.<br>
         *   False if there is no model chosen.
         */
        private boolean _hasParentsWithNewVersion() {

            // For each checkbox of the parents models
            // check whether there is any check box unchecked
            for (ParentModelItemPanel parentModelItemPanel : _parentModelsPanels) {
                if (!parentModelItemPanel.isSelected()) {
                    return true;
                }
            }

            return false;
        }

        /**
         * Validate whether the information in this frame is valid.
         *
         * @return True - if the data is valid.<br>
         *       False - if some data is invalid.
         * @exception IllegalNameException Thrown if the new version name is
         * illegal.
         * @exception UnSavedParentModelsException Thrown if this model has
         * some unsaved parents opening there.
         */
        private boolean _isValid() throws IllegalNameException,
        UnSavedParentModelsException {

            Collection<NamedObj> unSavedModels = new ArrayList<NamedObj>();
            boolean hasUnsavedParent = false;

            // Verify whether there is any unsaved parent model opening there.
            for (String parentModelName : _getOpenedParents()) {

                PtolemyEffigy parentModelEffigy = (PtolemyEffigy) _source
                        .getConfiguration().getDirectory()
                        .getEffigy(parentModelName);

                // If the parent model is unsaved.
                if (parentModelEffigy.isModified()) {
                    unSavedModels.add(parentModelEffigy.getModel());
                    hasUnsavedParent = true;
                }

            }

            if (hasUnsavedParent) {
                throw new UnSavedParentModelsException(unSavedModels,
                        "There are parent model(s) contain(s) unsaved "
                                + "changes, please save these parent models "
                                + "first.");
            }

            // Verify whether the new name for the new version of model meets
            // the name convention requirement.
            if (_hasParentsWithNewVersion()) {

                Utilities.checkModelName(_newModelNameTextField.getText());

                return true;
            }

            return true;

        }

        private void _showModelNameField(boolean isShown) {

            _newModelNameTextField.setText("");

            _newModelNameLable.setVisible(isShown);
            _newModelNameTextField.setVisible(isShown);
            _modelNamePanel.setVisible(isShown);
        }

        private void _unCheckParentModel() {

            if (_hasParentsWithNewVersion()) {
                if (!_modelNamePanel.isVisible()) {
                    _showModelNameField(true);
                }
            } else {
                if (_modelNamePanel.isVisible()) {
                    _showModelNameField(false);
                }
            }

            validate();
            repaint();

        }

        ///////////////////////////////////////////////////////////////////
        ////                  private variables                        ////

        private JPanel _bottomPanel;

        private JButton _cancelButton;

        private SaveModelToDBFrame _firstFrameOfSave;

        private JPanel _modelNamePanel;

        private JLabel _newModelNameLable;

        private JTextField _newModelNameTextField;

        private ArrayList<ParentModelItemPanel> _parentModelsPanels;

        private JPanel _parentsPanel;

        private JButton _previousButton;

        private JButton _saveButton;

    }

    private static class ParentModelItemPanel extends JPanel {

        /**
         * Construct the ParentModelItemPanel.
         *
         * @param parentModelName The name of the parent model.
         * @param configuration The configuration.
         * @param parentValidateFrame The frame contains this panel.
         */
        public ParentModelItemPanel(String parentModelName,
                Configuration configuration,
                ParentValidateFrame parentValidateFrame) {

            super();

            _modelName = parentModelName;
            _parentFrame = parentValidateFrame;
            _configuration = configuration;

            GroupLayout layout = new GroupLayout(this);

            setLayout(layout);
            setAlignmentX(LEFT_ALIGNMENT);
            setAlignmentY(TOP_ALIGNMENT);

            _modelNameButton = new JButton("<html><u>" + parentModelName
                    + "</html></u>");

            _modelNameButton.setForeground(Color.BLUE);
            _modelNameButton.setMaximumSize(getMinimumSize());

            _modelNameButton.setAlignmentX(LEFT_ALIGNMENT);

            _modelNameButton.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent event) {

                    try {

                        PtolemyEffigy effigy = LoadManager.loadModel(
                                _modelName, _configuration);

                        if (effigy != null) {

                            effigy.showTableaux();

                        } else {

                            JOptionPane.showMessageDialog(
                                    ParentModelItemPanel.this,
                                    "The specified model could "
                                            + "not be found in the database.",
                                            "Load Error",
                                            JOptionPane.INFORMATION_MESSAGE, null);

                        }

                    } catch (Exception e) {

                        MessageHandler.error(
                                "Cannot load the specified model. ", e);

                    }

                }

            });

            // For each model, paint one checkbox along with it, checked by
            // default.
            _checkBox = new JCheckBox();
            _checkBox.setSelected(true);

            _checkBox.setAlignmentX(LEFT_ALIGNMENT);

            _checkBox.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    _parentFrame._unCheckParentModel();

                }
            });

            layout.setHorizontalGroup(layout.createSequentialGroup()
                    .addComponent(_checkBox).addComponent(_modelNameButton));

            layout.setVerticalGroup(layout.createParallelGroup()
                    .addComponent(_checkBox).addComponent(_modelNameButton));

        }

        public String getParentModelName() {

            return _modelName;
        }

        public boolean isSelected() {

            return _checkBox.isSelected();
        }

        private JCheckBox _checkBox;

        private Configuration _configuration;

        private String _modelName;

        private JButton _modelNameButton;

        private ParentValidateFrame _parentFrame;

    }

}
