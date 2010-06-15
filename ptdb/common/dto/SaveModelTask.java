package ptdb.common.dto;

/**
 *
 * A task request to save/update the model in the database.
 * 
 * <p>It is used as a data transfer object that hold the model as XMLDBModel 
 * object with its getter and setter methods.</p>
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */
public class SaveModelTask extends Task {

    
    /**
     * Default nullary constructor.
     * <p>Should be removed once the code using it
     * is fixed to use the constructor with the XMLDBModel parameter.</p>
     */
    public SaveModelTask() {
        //FIXME: Remove this method as soon as the code pointing to it is modified.
    }
    
    /**
     * Construct an instance of this class and set the model to be saved in the 
     * database.
     * @param The model to be saved in the database.
     */
    public SaveModelTask(XMLDBModel xmlDBModel) {
        _xmlDBModel = xmlDBModel;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    
    /**
     * Return the model to be saved in the database.
     * @return The model to be saved in the database.
     */
    public XMLDBModel getXMLDBModel() {
        return _xmlDBModel;

    }

    /**
     * Set the model to be saved in the database.
     * @param The model to be saved to the database.
     */
    public void setXMLDBModel(XMLDBModel xmlDBModel) {
        _xmlDBModel = xmlDBModel;
    }

    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    /** The model in XMLDBModel object format. */
    private XMLDBModel _xmlDBModel;

}
