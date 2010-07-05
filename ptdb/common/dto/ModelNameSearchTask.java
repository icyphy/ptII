/*
 * 
 */
package ptdb.common.dto;


///////////////////////////////////////////////////////////////
//// ModelNameSearchTask

/**
 * Task to search for models based on model name.
 * 
 * @author Ashwini Bijwe
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 *
 */
public class ModelNameSearchTask extends Task {
    /**
     * Create an instance of ModelNameSearchTask.
     * @param modelName Model name for which the search task is to be created.
     */
    public ModelNameSearchTask(String modelName) {
        this.modelName = modelName;
    }
    //////////////////////////////////////////////////////////////////////
    ////		public methods 					////
    /**
     * Return the model name.
     * @see setModelName
     */
    public String getModelName() {
        return modelName;
    }
    /**
     * Set the given String as the model name.
     * @param modelName String to be set as model name.
     * @see getModelName
     */
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
    
    //////////////////////////////////////////////////////////////////////
    ////		private variables				////
    private String modelName;

   
}
