package ptdb.common.dto;

/**
 *
 * @author Yousef
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yousef)
 * @Pt.AcceptedRating Red (yousef)
 *
 * This class represent the create model task that represent a request to create a new model in the database
 * It is used as a data transfer object and hold the model as XMLDBModel object with its getter and setter methods
 *
 */
public class CreateModelTask extends Task {

    /**
     * return the model that is to be created in the database
     */
    public XMLDBModel getXMLDBModel() {
        return _m_Model;
    }

    /**
     * set the model to be created
     * @param p_XMLDBModel the model to be created to the database.
     */
    public void setXMLDBModel(XMLDBModel p_XMLDBModel) {
        _m_Model = p_XMLDBModel;
    }

    /**
     * variable to hold the model in XMLDBModel object
     */
    private XMLDBModel _m_Model = null;

}
