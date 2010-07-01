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
     * Construct a XMLDBModel instance
     * with the given model name.
     *
     * @param modelName the name for the given model.
     */
    public XMLDBModel(String modelName) {
        this._modelName = modelName;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /**
     * Add the given parent list to the model's
     * parent list.
     * @param list a List of parents to be added.
     */
    public void addParentList(List<XMLDBModel> list) {
        if (_listParents == null) {
            _listParents = new ArrayList<List<XMLDBModel>>();
        }

        _listParents.add(list);
    }

    /**
     * Add the given child entity to the model's
     * referenced children list.
     * @param modelName Child entity to be added to the referenced children list.
     */
    public void addReferencedChild(String modelName) {
        if (_listReferencedChildren == null) {
            _listReferencedChildren = new ArrayList<String>();
        }

        _listReferencedChildren.add(modelName);
    }
    /**
     * Return True or false based on if the model is new or it exists in the database.
     * @return True or false based on if the model is new or it exists in the database.
     * 
     * @see #setIsNew
     */
    public boolean getIsNew() {
        return _isNew;
    }

    /**
     * Return the model content.
     * @return A string representation of the model content.
     * 
     * @see #setModel
     */
    public String getModel() {
        return _modelContent;
    }

    /**
     * Return the model name.
     * @return The model name.
     * 
     * @see #setModelName
     */
    public String getModelName() {
        return _modelName;
    }

    /**
     * Return the parents for the current model.
     * @return List of parents models for the current model.
     * 
     * @see #setParents
     */
    public List<List<XMLDBModel>> getParents() {
        return _listParents;
    }

    /**
     * Return the first level referenced children entities  
     * for the current model.
     * 
     * @return List of first level referenced children entities 
     * for the current model.
     * 
     * @see #setReferencedChildren
     */
    public List<String> getReferencedChildren() {
        return _listReferencedChildren;
    }

    /**
     * Set the isNew variable which indicates if the model is in the database or
     * it is new model.
     * @param isNew a boolean True or false value to set the isNew member variable.
     * 
     * @see #getIsNew
     */
    public void setIsNew(boolean isNew) {
        _isNew = isNew;
    }

    /**
     * Set the model name.
     * @param modelName The model name.
     * 
     * @see #getModelName
     */
    public void setModelName(String modelName) {
        _modelName = modelName;
    }

    /**
     * Set the model content.
     * @param modelContent The model content in xml format.
     * 
     * @see #getModel
     */
    public void setModel(String modelContent) {
        _modelContent = modelContent;
    }

    /**
     * Set the parents for the current model.
     * @param listParents List of parents for this model.
     * 
     * @see #getParents
     */
    public void setParents(List<List<XMLDBModel>> listParents) {
        _listParents = listParents;
    }

    
    /**
     * Set the first level referenced children entities  for the current model.
     * @param listChildren List of first level referenced children entities
     * for this model.
     * 
     * @see #getReferencedChildren
     */
    public void setReferencedChildren(List<String> listChildren) {
        _listReferencedChildren = listChildren;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /**
     * True or false value to indicate whether the model is in the database or
     * a new model.
     */
    private boolean _isNew;

    /** List of all the parents for the current model. */
    private List<List<XMLDBModel>> _listParents;

    /** List of all the first level referenced child entities for 
     * the current model. 
     */
    private List<String> _listReferencedChildren;
    
    /** The content of the model in a string. */
    private String _modelContent;

    /** Model name. */
    private String _modelName;

}
