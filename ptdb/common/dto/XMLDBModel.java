package ptdb.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * The Model that need to be stored or retrieved from the database.
 * 
 * <p>
 * It is used as a data transfer object and hold 4 information about the model:
 * <br>- Model name.
 * <br>- Model content in xml format.
 * <br>- Does the model exist in the database or it is a new model.
 * <br>- List of parents for the current model.
 * <br>Each of the 4 information has its own getter and setter methods.
 * </p>
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */
public class XMLDBModel {

    
    /**
     * Default constructor and should be removed once the pointing to it is fixed
     * to use the constructor below.
     */
    public XMLDBModel() {
        //TODO: Remove this method as soon as the code pointing to it is modified.
    }
    
    
    /**
     * Construct a XMLDBModel instance
     * with the given model name.
     *
     * @param Name for the given model.
     */
    public XMLDBModel(String modelName) {
        this._modelName = modelName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Add the given parent list to the model's
     * parent list.
     * @param List of parents to be added.
     */
    public void addParentList(List<XMLDBModel> list) {
        if (_listParents == null) {
            _listParents = new ArrayList<List<XMLDBModel>>();
        }

        _listParents.add(list);
    }

    /**
     * Return True or false based on if the model is new or it exists in the database.
     *@return True or false based on if the model is new or it exists in the database.
     */
    public boolean getIsNew() {
        return _isNew;
    }

    /**
     * Return the model content.
     * @return A string representation of the model content.
     */
    public String getModel() {
        return _modelContent;
    }

    /**
     * Return the model name.
     * @return The model name.
     */
    public String getModelName() {
        return _modelName;
    }

    /**
     * Return the parents for the current model.
     * @return List of parents models for the current model.
     */
    public List<List<XMLDBModel>> getParents() {
        return _listParents;
    }

    /**
     * Set the isNew variable which indicates if the model is in the database or
     * it is new model.
     * @param True or false value to set the isNew member variable.
     */
    public void setIsNew(boolean isNew) {
        _isNew = isNew;
    }

    /**
     * Set the model name.
     * @param The model name.
     */
    public void setModelName(String modelName) {
        _modelName = modelName;
    }

    /**
     * Set the model content.
     * @param The model content in xml format.
     */
    public void setModel(String modelContent) {
        _modelContent = modelContent;
    }

    /**
     * Set the parents for the current model
     * @param List of parents for this model.
     */
    public void setParents(List<List<XMLDBModel>> listParents) {
        _listParents = listParents;
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     * True or false value to indicate whether the model is in the database or
     * a new model.
     */
    private boolean _isNew;

    /**
     * List of all the parents for the current model.
     */
    private List<List<XMLDBModel>> _listParents;

    /**
     * The content of the model in a string.
     */
    private String _modelContent;

    /**
     * Model name.
     */
    private String _modelName;

}
