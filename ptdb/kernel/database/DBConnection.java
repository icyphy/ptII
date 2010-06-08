package ptdb.kernel.database;

import java.util.ArrayList;

import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;

//////////////////////////////////////////////////////////////////////////
//// DBConnection
/**
 *  Interface for XML Database connections.
 *  
 *  @author Ashwini Bijwe
 *  
 *  @version $Id$
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (abijwe)
 *  @Pt.AcceptedRating Red (abijwe)
 *   
 */
public interface DBConnection {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /**
     * Abort the connection to the database and rollback the transaction
     * @throws DBConnectionException
     */
    public void abortConnection() throws DBConnectionException;

    /**
     * Close the connection to the database and commit the transaction
     * @throws DBConnectionException
     */
    public void closeConnection() throws DBConnectionException;

    /**
     * Get the attributes defined from the database 
     * @param task - Define the criteria to get the attribute   
     * @return
     * @throws DBConnectionException
     */
    public ArrayList executeGetAttributesTask(GetAttributesTask task)
            throws DBExecutionException;

    /**
     * 
     * @param task
     *          The task to be completed.  In this case, GetModelsTask. 
     *          This will tell the DB layer to return the specified model.
     * @return XMLDBModel
     *          This object will be populated with the model's MoML string
     * @throws DBConnectionException
     */
    public XMLDBModel executeGetModelsTask(GetModelsTask task)
            throws DBExecutionException;

}
