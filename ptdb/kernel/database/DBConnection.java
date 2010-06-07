package ptdb.kernel.database;

import java.util.ArrayList;

import ptdb.common.dto.GetAttributesTask;
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
    public ArrayList executeGetAttributesTask(GetAttributesTask task) throws DBExecutionException;
    
}
