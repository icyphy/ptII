/*
 * 
 */
package ptdb.common.dto;


///////////////////////////////////////////////////////////////
//// GetFirstLevelParentsTask

/**
 * Task to fetch the first level parents for the given model. First level 
 * parents are models that have an immediate reference to the given model. 
 * 
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class GetFirstLevelParentsTask extends Task{

    
    
    /**
     * Construct an instance of this class and set the model for which the first 
     * level parents need to be fetched.
     * @param model Model for which the first level parents need to be fetched.
     */
    public GetFirstLevelParentsTask(XMLDBModel model) {
        this._model = model;
    }
    
    
    //////////////////////////////////////////////////////////////////////
    ////		public methods 					////
    
    /**
     * Return the model for which the first level parents need to be fetched.
     * @return Model for which the first level parents need to be fetched.
     * @see #setModel
     */
    public XMLDBModel getModel() {
        return _model;
    }
    
    /**
     * Set the model for which the first level parents need to be fetched.
     * @param model Model for which the first level parents need to be fetched.
     * @see #getModel
     */
    public void setModel(XMLDBModel model) {
        this._model = model;
    }
    
    //////////////////////////////////////////////////////////////////////
    ////		private variables				////
    /* Model for which the first level parents need to be fetched. */
    XMLDBModel _model;
}
