package ptdb.kernel.bl.save;

import java.util.List;

import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////
//// AttributesManager

/**
 * Manage the attributes and work as a link between the GUI and the database
 * layer.
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 * 
 */
public class AttributesManager {

    //////////////////////////////////////////////////////////////////////
    ////		public methods 				      ////

    /**
     * Call to the database and retrieve the list attributes stored there.
     * 
     * @return The list of attributes stored in the database.
     * 
     * @exception DBExecutionException Thrown if the operation fails.
     * @exception DBConnectionException Thrown if the connection fails.
     * 
     */
    public List<XMLDBAttribute> getDBAttributes() throws DBExecutionException,
            DBConnectionException {
        
        DBConnection dbConnection = null;
        List<XMLDBAttribute> attributesList = null;

        try {
            dbConnection = DBConnectorFactory.getSyncConnection(false);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection from the database.");
            }

            GetAttributesTask task = new GetAttributesTask();

            attributesList = dbConnection.executeGetAttributesTask(task);

        } catch (DBExecutionException e) {
            if (dbConnection != null) {
                dbConnection.abortConnection();
            }
            throw new DBExecutionException("Failed to save the model - "
                    + e.getMessage(), e);

        } finally {
            if (dbConnection != null) {
                dbConnection.closeConnection();
            }
        }
        return attributesList;
    }

}
