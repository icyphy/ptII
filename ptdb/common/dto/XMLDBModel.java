package ptdb.common.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Yousef
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (Yousef)
 * @Pt.AcceptedRating Red (Yousef)
 *
 * This class represent the model that need to be stored or retrieved from the database.
 * It is used as a data transfer object and hold 4 information about the model
 * 1- Model name
 * 2- Model content
 * 3- Does the model exist in the database or it is a new model
 * 4- List of parents for the current model
 * Each of the 4 information has its own getter and setter methods.
 *
 */
public class XMLDBModel {

    /**
     * Construct a XMLDBModel instance.
     */
    public XMLDBModel() {

    }

    /**
     * Construct a XMLDBModel instance
     * with the given model name.
     *
     * @param modelName Name for the given model.
     */
    public XMLDBModel(String modelName) {
        this._m_strModelName = modelName;
    }

    /**
     * Add the given parent list to the model's
     * parent list.
     * @param list List of parents to be added.
     */
    public void addParentList(List<XMLDBModel> list) {
        if (_m_listParents == null) {
            _m_listParents = new ArrayList<List<XMLDBModel>>();
        }

        _m_listParents.add(list);
    }

    /**
     * Return the is new.
     *@return boolean True or false based on if the model is new or it exists in the database.
     */
    public boolean getIsNew() {
        return _m_bIsNew;
    }

    /**
     * Return the model content.
     * @return String - string representation of the model content
     */
    public String getModel() {
        return _m_strModel;
    }

    /**
     * Return the model name
     * @return string - the model name
     */
    public String getModelName() {
        return _m_strModelName;
    }

    /**
     * Return the parents for the current model
     * @return ArrayList<ArrayList<XMLDBModel>> - list of parents models for the current model
     */
    public List<List<XMLDBModel>> getParents() {
        return _m_listParents;
    }

    /**
     * Set the isNew variable
     * @param boolean p_bIsNew - variable that holds true or false to set the isNew member variable
     */
    public void setIsNew(boolean p_bIsNew) {
        _m_bIsNew = p_bIsNew;
    }

    /**
     * Set the model name
     * @param String p_strModelName - the model name to be set.
     */
    public void setModelName(String p_strModelName) {
        _m_strModelName = p_strModelName;
    }

    /**
     * Set the model content
     * @param String p_strModel - this is the model content to be set.
     */
    public void setModel(String p_strModel) {
        _m_strModel = p_strModel;
    }

    /**
     * Set the parents for the current model
     * @param ArrayList<ArrayList<XMLDBModel>> p_listParents - list of parents for that need to be set as parents for the current model.
     */
    public void setParents(List<List<XMLDBModel>> p_listParents) {
        _m_listParents = p_listParents;
    }

    /**
     * variable to hold the check if the model is from the database or it is a new model
     */
    private boolean _m_bIsNew = false;

    /**
     * variable to hold a list of all the parents for the current model
     */
    private List<List<XMLDBModel>> _m_listParents = null;

    /**
     * variable to hold the content of the model in a string.
     */
    private String _m_strModel = null;

    /**
     * variable to hold the model name.
     */
    private String _m_strModelName = null;

}
