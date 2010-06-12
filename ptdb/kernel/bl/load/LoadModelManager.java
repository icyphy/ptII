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
    public XMLDBModel load(String name) throws DBConnectionException,
            DBExecutionException {

        XMLDBModel returnModel = null;

        DBConnection connection = DBConnectorFactory.getSyncConnection(false);

        try {

            GetModelsTask getModelsTask = new GetModelsTask();
            getModelsTask.setModelName(name);
            returnModel = connection.executeGetModelsTask(getModelsTask);

            connection.commitConnection();

        } catch (DBExecutionException dbEx) {

            connection.abortConnection();
            throw dbEx;

        } finally {

            if (connection != null) {

                connection.closeConnection();

            }

        }

        return returnModel;

    }

}
