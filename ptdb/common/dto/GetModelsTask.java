package ptdb.common.dto;

/**
 * @author Yousef
 * @version 1.0
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (Yousef)
 * @Pt.AcceptedRating Red (Yousef)
 * 
 * This class represent the get models task that represent a request to fetch a model from the database
 * It is used as a data transfer object and hold the model name with its getter and setter method
 *
*/
public class GetModelsTask extends Task {

    /**
     * return the model name that is needed to to fetch the model from the database
     */
    public String getModelName() {
        return _m_strModelName;

    }

    /**
     * set the model name
     * @param p_strModelName the name of the model that need to be fetched from the database
     */
    public void setModelName(String p_strModelName) {
        _m_strModelName = p_strModelName;
    }

    /**
     * variable to hold the model name
     */
    private String _m_strModelName = null;

}
