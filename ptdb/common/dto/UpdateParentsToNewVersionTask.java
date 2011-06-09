/*
 * 
 */
package ptdb.common.dto;

import java.util.ArrayList;


///////////////////////////////////////////////////////////////
//// UpdateParentsToNewVersionTask

/**
 * Task to update the selected parents to the new version of the model.
 * 
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class UpdateParentsToNewVersionTask extends Task {


    //////////////////////////////////////////////////////////////////////
    ////		public methods 					////
    
    /**
     * Return the new model that was created.
     * @return The new model that was created.
     * @see #setNewModel 
     */
    public XMLDBModel getNewModel() {
        return _newModel;
    }
    
    /**
     * Return the old model that was being updated.
     * @return The old model that was being updated.
     * @see #setOldModel
     */
    public XMLDBModel getOldModel() {
        return _oldModel;
    }
    
    /**
     * Return the list of selected first level parents' model names.
     * @return The list of selected first level parents' model names.
     * @see #setParentsList
     */
    public ArrayList<String> getParentsList() {
        return _parentsList;
    }
    
    /** 
     * Set the new model that was created.
     * @param newModel The new model that was created.
     * @see #getNewModel
     */
    public void setNewModel(XMLDBModel newModel) {
        _newModel = newModel;
    }
    
    /**
     * Set the old model that was being updated. 
     * @param oldModel The old model that was being updated.
     * @see #getOldModel
     */
    public void setOldModel(XMLDBModel oldModel) {
        _oldModel = oldModel;
    }

    /**
     * Set the list of selected first level parents' model names.
     * @param parentsList The list of selected first level parents' model names.
     * @see #getParentsList
     */
    public void setParentsList(ArrayList<String> parentsList) {
        _parentsList = parentsList;
    }

    //////////////////////////////////////////////////////////////////////
    ////		private variables				////
    /* The new model that was created. */
    XMLDBModel _newModel;
    /* The old model that was being updated. */
    XMLDBModel _oldModel;
    /* The list of selected first level parents' model names. */
    ArrayList<String> _parentsList;
}
