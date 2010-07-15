package ptdb.common.dto;

/**
 * 
 * A task request to fetch the reference string the database.
 * 
 * <p>The reference string contains an XML skeleton containing all hierarchies 
 * where references to the given model occur in the database.</p>
 * 
 * @author Lyle Holsinger
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (lholsing)
 * @Pt.AcceptedRating Red (lholsing)
 *
 *
 */

public class GetReferenceStringTask {

    /**
     * Construct an instance of the object and set the name of the model
     * for which to retrieve the reference string.
     * 
     * @param modelName the model name to be fetched from the database.
     */
    public GetReferenceStringTask(String modelName){
        _modelName = modelName;        
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return the model name that is needed to be fetched from the database.
     *
     * @return The model name.
     * 
     * @see #setModelName
     */
    public String getModelName() {
        return _modelName;

    }
    
    /**
     * Set the model name to be fetched from the database.
     *
     * @param modelName the name of the model to be fetched from the database.
     * 
     * @see #getModelName
     */
    public void setModelName(String modelName) {
        _modelName = modelName;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The model name. */
    private String _modelName;
    
}
