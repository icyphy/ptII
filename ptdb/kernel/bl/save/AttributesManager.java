package ptdb.kernel.bl.save;

import java.util.List;

import ptdb.common.dto.CreateAttributeTask;
import ptdb.common.dto.DeleteAttributeTask;
import ptdb.common.dto.UpdateAttributeTask;
import ptdb.common.dto.XMLDBAttribute;
import ptdb.common.dto.GetAttributesTask;
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
 * @author Yousef Alsaeed, Alek Wang
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
     * Save a new user defined attribute into the database.    
     * 
     * @param attribute The attribute to be saved in the database.  It 
     * contains the information to be stored for that attribute. 
     * @return The instance of the newly created attribute. 
     * @exception DBConnectionException Thrown from the database layer if the
     *  database layer fails to create a connection to the database.
     * @exception DBExecutionException Thrown from the database layer if the
     *  database layer fails to execute the operations in the database. 
     */
    public XMLDBAttribute createAttribute(XMLDBAttribute attribute)
            throws DBConnectionException, DBExecutionException {
        
        XMLDBAttribute updatedAttribute;
        try {
            // create a DB connection 
            _dbConnection = DBConnectorFactory.getSyncConnection(true);

            // create the CreateAttributeTask
            CreateAttributeTask createAttributeTask = new CreateAttributeTask(
                    attribute);

            // execute 
            updatedAttribute = _dbConnection
                    .executeCreateAttributeTask(createAttributeTask);

            _dbConnection.commitConnection();

        } catch (DBExecutionException e) {
            if (_dbConnection != null) {
                _dbConnection.abortConnection();
            }
            throw e;
        } finally {
            if (_dbConnection != null) {
                _dbConnection.closeConnection();
            }
        }
        
        return updatedAttribute;
    }

    /**
     * Delete an existing attribute from the database. 
     * 
     * @param attribute The attribute to be deleted from the database.
     * @exception DBConnectionException Thrown from the database layer if the
     *  database layer fails to create a connection to the database.
     * @exception DBExecutionException Thrown from the database layer if the
     *  database layer fails to execute the operations in the database.
     */
    public void deleteAttribute(XMLDBAttribute attribute)
            throws DBConnectionException, DBExecutionException {
        try {
            // create a DB connection 
            _dbConnection = DBConnectorFactory.getSyncConnection(true);

            // create the DeleteAttributeTask
            DeleteAttributeTask deleteAttributeTask = new DeleteAttributeTask(
                    attribute);

            // execute 
            _dbConnection.executeDeleteAttributeTask(deleteAttributeTask);

            // commit the connection
            _dbConnection.commitConnection();

        } catch (DBExecutionException e) {
            if (_dbConnection != null) {
                _dbConnection.abortConnection();
            }
            throw e;

        } finally {
            if (_dbConnection != null) {
                _dbConnection.closeConnection();
            }
        }

    }

    /**
     * Call to the database and retrieve the list attributes stored there.
     * 
     * @return The list of attributes stored in the database.
     * 
     * @exception DBExecutionException Thrown if the operation fails.
     * @exception DBConnectionException Thrown if the db layer fails to create
     * the connection.
     * 
     */
    public List<XMLDBAttribute> getDBAttributes() throws DBExecutionException,
            DBConnectionException {

        List<XMLDBAttribute> attributesList = null;

        try {
            _dbConnection = DBConnectorFactory.getSyncConnection(false);

            if (_dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection from the database.");
            }

            GetAttributesTask task = new GetAttributesTask();

            attributesList = _dbConnection.executeGetAttributesTask(task);

        } catch (DBExecutionException e) {
            if (_dbConnection != null) {
                _dbConnection.abortConnection();
            }
            throw new DBExecutionException("Failed to fetch the attributes - "
                    + e.getMessage(), e);

        } finally {
            if (_dbConnection != null) {
                _dbConnection.closeConnection();
            }
        }
        return attributesList;
    }

    /**
     * Update an existing attribute in the database with the new information.
     * 
     * @param attribute The attribute to be updated in the database. It
     *  contains the new information to be stored for that attribute. 
     * @exception DBConnectionException Thrown from the database layer if the 
     *  database layer fails to create a connection to the database.
     * @exception DBExecutionException Thrown from the database layer if the
     *  database layer fails to execute the operations in the database.
     */
    public void updateAttribute(XMLDBAttribute attribute)
            throws DBConnectionException, DBExecutionException {
        try {
            // create a DB connection 
            _dbConnection = DBConnectorFactory.getSyncConnection(true);

            // create the UpdateAttributeTask
            UpdateAttributeTask updateAttributeTask = new UpdateAttributeTask(
                    attribute);

            // execute 
            _dbConnection.executeUpdateAttributeTask(updateAttributeTask);

            // commit the connection
            _dbConnection.commitConnection();

        } catch (DBExecutionException e) {
            if (_dbConnection != null) {
                _dbConnection.abortConnection();
            }

            throw e;

        } finally {
            if (_dbConnection != null) {
                _dbConnection.closeConnection();
            }

        }

    }

    //////////////////////////////////////////////////////////////////////
    ////                public variables                              ////

    private DBConnection _dbConnection;

}
