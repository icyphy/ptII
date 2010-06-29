package ptdb.common.dto;

import java.util.ArrayList;

///////////////////////////////////////////////////////////////
////RemoveModelsTask

/**
 * 
 * A task request to remove a list of models from the database.
 * 
 * <p>It is used as a data transfer object that hold a list of models as 
 * XMLDBModel objects with its getter and setter methods.</p>
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class RemoveModelsTask extends Task {


    /**
     * Construct an object from this class and set the models list.
     * @param modelsList The models list to be removed from the database.
     */
    public RemoveModelsTask(ArrayList<XMLDBModel> modelsList) {
        this._modelsList = modelsList;
    }

    //////////////////////////////////////////////////////////////////////
    ////		public methods 					  ////
    
    /**
     * Return the list of models to be deleted from the database.
     * @return The list of models to be deleted from the database.
     * 
     * @see #setModelsList
     */
    public ArrayList<XMLDBModel> getModelsList() {
        return _modelsList;
    }
    
    /**
     * Set the list of models to be deleted from the database.
     * @param modelsList List of models to be deleted from the database.
     * 
     * @see #getModelsList
     */
    public void setModelsList(ArrayList<XMLDBModel> modelsList) {
        this._modelsList = modelsList;
    }
    
    //////////////////////////////////////////////////////////////////////
    ////		private variables				  ////
    
    
    /** List of models to be deleted. */
    ArrayList<XMLDBModel> _modelsList = null;

}
