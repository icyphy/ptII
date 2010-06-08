package ptdb.common.dto;

/**
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (abijwe)
 * @Pt.AcceptedRating Red (abijwe)
 * DTO to encapsulate the parameters required for creating an XML DB connection
 * @author abijwe
 *
 */
public class DBConnectionParameters {

    /**
     * Creates a DBConnectionParameters object with the given parameters
     * @param url - Url/Path for the location where the database file is present 
     * @param containerName - Name of the container for the XML Database
     * @param isTransactionRequired - boolean that specifies whether the transaction management is required for the connection that is created
     */
    public DBConnectionParameters(String url, String containerName,
            boolean isTransactionRequired) {
        this._url = url;
        this._containerName = containerName;
        this._isTransactionRequired = isTransactionRequired;
    }

    /**
     * Returns the set container name for the given XML database
     * @return
     */
    public String getContainerName() {
        return _containerName;
    }

    /**
     * Returns the set URL/path of the location where the XML database file is present
     * @return
     */
    public String getUrl() {
        return _url;
    }

    /**
     * Returns true if the connection is to be created with transaction and false if it is to be created without transaction.
     * @return
     */
    public boolean isTransactionRequired() {
        return _isTransactionRequired;
    }

    /**
     * Sets the container name parameter to the given value
     * @param containerName - Name of the container for the given XML database
     */
    public void setContainerName(String containerName) {
        _containerName = containerName;
    }

    /**
     * Sets the transaction required parameter to the given value 
     * @param isTransactionRequired - boolean that specifies whether the transaction management is required for the connection that is created
     */
    public void setIsTransactionRequired(boolean isTransactionRequired) {
        _isTransactionRequired = isTransactionRequired;
    }

    /**
     * Sets the URL parameter to the given value
     * @param url - Location where the database file is present 
     */
    public void setUrl(String url) {
        _url = url;
    }

    /*
     public String getUserName() {
        return _userName;
    }

    public void setUserName(String userName) {
        this._userName = userName;
    }

    public String getPassword() {
        return _password;
    }

    public void setPassword(String password) {
        this._password = password;
    }
    */

    private String _containerName;
    private boolean _isTransactionRequired;
    private String _url;
    /*
    private String _userName;
    private String _password;
    */

}
