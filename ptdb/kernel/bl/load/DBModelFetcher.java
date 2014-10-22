/*
@Copyright (c) 2010-2014 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.

                                                PT_COPYRIGHT_VERSION_2
                                                COPYRIGHTENDKEY


 */
package ptdb.kernel.bl.load;

import java.util.ArrayList;

import ptdb.common.dto.GetModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// DBModelFetcher

/**
 * This is the business layer that interfaces with the database for retrieving
 * models.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

public class DBModelFetcher {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Given a model id representing the model to load, return
     *  an XMLDBModel object that contains the MoML.
     *
     * @param id
     *          The id of the model to be loaded.
     *
     * @return An XMLDBModel object from the Database,
     *         containing its MoML string.
     *
     * @exception DBConnectionException
     *          Thrown if there a problem with the database connection.
     *
     * @exception DBExecutionException
     *          Thrown if there is a problem executing the database task.
     *
     */
    public static XMLDBModel loadUsingId(String id)
            throws DBConnectionException, DBExecutionException {

        GetModelTask getModelTask = new GetModelTask(null, id);
        return load(getModelTask);
    }

    /** Given a model name representing the model to load, return
     *  an XMLDBModel object that contains the MoML.
     *
     * @param name
     *          The name of the model to be loaded.
     *
     * @return An XMLDBModel object from the Database,
     *         containing its MoML string.
     *
     * @exception DBConnectionException
     *          Thrown if there a problem with the database connection.
     *
     * @exception DBExecutionException
     *          Thrown if there is a problem executing the database task.
     *
     */
    public static XMLDBModel load(String name) throws DBConnectionException,
            DBExecutionException {

        GetModelTask getModelTask = new GetModelTask(name);
        return load(getModelTask);
    }

    /**
     * Given a GetModelTask representing the model(name or id) to load, return
     * an XMLDBModel object that contains the MoML.
     *
     * @param getModelTask GetModelTask that contains either the id or the model
     * name.
     * @return An XMLDBModel object from the Database, containing its MoML string.
     * @exception DBConnectionException Thrown if there a problem with the database
     * connection.
     * @exception DBExecutionException Thrown if there is a problem executing the
     * database task.
     */
    private static XMLDBModel load(GetModelTask getModelTask)
            throws DBConnectionException, DBExecutionException {

        XMLDBModel returnModel = null;

        DBConnection connection = DBConnectorFactory.getSyncConnection(false);

        try {

            returnModel = connection.executeGetCompleteModelTask(getModelTask);

        } catch (DBExecutionException dbEx) {
            throw dbEx;
        } finally {
            if (connection != null) {
                connection.closeConnection();
            }
        }
        return returnModel;
    }

    /** Given an ArrayList of XMLDBModel objects that are not populated with
     * MoML strings, query the database to obtain the MoML and then return
     * the revised ArrayList.
     *
     * @param modelList
     *          An ArrayList of XMLDBModel objects
     *          without associated MoML strings.
     *
     * @return An ArrayList of XMLDBModel objects
     *         populated with their respective MoML strings.
     *         An empty list is returned if no objects could be added.
     *
     * @exception DBConnectionException
     *          Thrown if there a problem with the database connection.
     *
     * @exception DBExecutionException
     *          Thrown if there is a problem executing the database task.
     *
     */
    public static ArrayList<XMLDBModel> load(ArrayList<XMLDBModel> modelList)
            throws DBConnectionException, DBExecutionException {

        ArrayList<XMLDBModel> returnList = new ArrayList();

        DBConnection connection = DBConnectorFactory.getSyncConnection(false);

        try {

            for (XMLDBModel model : modelList) {

                XMLDBModel resultModel;
                GetModelTask getModelTask = new GetModelTask(
                        model.getModelName());
                resultModel = connection
                        .executeGetCompleteModelTask(getModelTask);

                if (resultModel != null) {

                    returnList.add(resultModel);

                }

            }

        } catch (DBExecutionException dbEx) {
            throw dbEx;
        } finally {
            if (connection != null) {
                connection.closeConnection();
            }
        }

        return returnList;
    }
}
