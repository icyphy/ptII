package ptdb.kernel.database;

import java.util.ArrayList;

import ptdb.common.dto.GetAttributesTask;
import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;

/**
 * 
 * @author wini
 *
 */
public interface DBConnection {

    /**
     * @throws DBConnectionException 
     * 
     */
    public void closeConnection() throws DBConnectionException;

    /**
     * @throws DBConnectionException 
     * 
     */
    public void abortConnection() throws DBConnectionException;

    /**
     * 
     * @param task
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
