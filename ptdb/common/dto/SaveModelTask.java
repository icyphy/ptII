package ptdb.common.dto;

/**
 * @author Yousef
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (Yousef)
 * @Pt.AcceptedRating Red (Yousef)
 *
 * This class represent the save model task that represent a request to save/update the model in the database
 * It is used as a data transfer object and hold the model as XMLDBModel object with its getter and setter methods
 */
public class SaveModelTask extends Task {

    /**
     * return the model that is to be saved in the database
     */
    public XMLDBModel getXMLDBModel() {
        return _m_Model;

    }

    /**
     * set the model to be saved
     * @param p_XMLDBModel the model to be saved to the database.
     */
    public void setXMLDBModel(XMLDBModel p_XMLDBModel) {
        _m_Model = p_XMLDBModel;
    }

    /**
     * variable to hold the model in XMLDBModel object
     */
    private XMLDBModel _m_Model = null;

}
