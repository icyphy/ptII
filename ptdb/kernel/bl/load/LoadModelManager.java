package ptdb.kernel.bl.load;

import ptdb.common.dto.GetModelsTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
////LoadModelManager

/**
* This is the business layer that interfaces with the database for retrieving
* Models. 
*
* @author Lyle Holsinger
* @since Ptolemy II 8.1
* @version $Id$
* @Pt.ProposedRating red (lholsing)
* @Pt.AcceptedRating red (lholsing)
*/

public class LoadModelManager {

    ///////////////////////////////////////////////////////////////////
    ////                   public methods                         ////

    /** Give a model name representing the model to load, return
     *  an XMLDBModel object that contains the MoML.
     * 
     * @param name
     * @return An XMLDBModel object from the Database, 
     *         containing its MoML string.
     * @exception DBConnectionException
     * @exception DBExecutionException
     */
    public XMLDBModel load(String name)
            throws DBConnectionException, DBExecutionException {

        XMLDBModel returnModel = null;

        DBConnection conn = DBConnectorFactory.getSyncConnection(false);

        try {

            GetModelsTask gmt = new GetModelsTask();
            gmt.setModelName(name);
            returnModel = conn.executeGetModelsTask(gmt);
            
            conn.commitConnection();

        } catch (DBExecutionException dbEx) {

            conn.abortConnection();
            throw dbEx;
            
        } finally {

            if (conn != null) {
                
                conn.closeConnection();
            
            }

        }

        return returnModel;

    }
}
