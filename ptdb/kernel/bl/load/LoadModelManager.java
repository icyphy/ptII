package ptdb.kernel.bl.load;

import java.util.ArrayList;

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

    /**Give an array of names representing models to load, return
     * an array of XMLDBModel objects that contains the MoML.
     * 
     * @param names
     * @return An array of XMLDBModel objects from the Database, 
     *          each containing their MoML string.
     * @throws DBConnectionException
     */
    public ArrayList<XMLDBModel> load(String[] names)
            throws DBConnectionException {

        ArrayList<XMLDBModel> modelList = new ArrayList<XMLDBModel>();

        DBConnection conn = DBConnectorFactory.getSyncConnection(false);

        try {

            for (int i = 0; i < names.length; i++) {

                GetModelsTask gmt = new GetModelsTask();
                gmt.setModelName(names[i]);
                modelList.add(conn.executeGetModelsTask(gmt));

            }

            conn.closeConnection();

        } catch (DBExecutionException dbEx) {

            conn.abortConnection();

        }

        return modelList;

    }
}
