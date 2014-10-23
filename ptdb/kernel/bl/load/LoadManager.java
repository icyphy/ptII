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
import java.util.List;

import ptdb.common.dto.GetReferenceStringTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.CircularDependencyException;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.common.util.Utilities;
import ptdb.kernel.database.DBConnection;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.expr.StringConstantParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// LoadManager

/**
 * The business layer is used by the GUI to pass models to load.
 * It uses the LoadManager to interface with the database to get MoML strings.
 *
 * @author Lyle Holsinger
 * @since Ptolemy II 10.0
 * @version $Id$
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

public class LoadManager {

    /** Given a model name, return an Entity object.
     *
     * @param name
     *          The model name.  An alphanumeric
     *          string without special characters.
     *          If no model with the given name is found, return null.
     * @param byReference
     *          Indication that the model should be included by reference.
     * @param container
     *          The NamedObj that will contain this imported model.
     *          It is used here to obtain a unique name.
     * @return
     *          An Entity object that the GUI can display.
     * @exception DBConnectionException
     *          Thrown by DBModelFetcher if a problem occurs with the
     *          database connection.
     * @exception DBExecutionException
     *          Thrown by DBModelFetcher if a problem while executing a
     *          command.
     * @exception Exception
     *          Thrown if a problem occurs creating an effigy from the MoML.
     * @exception CircularDependencyException
     *          Thrown if an import would result in a circular dependency.
     */
    public static Entity importModel(String name, boolean byReference,
            NamedObj container) throws DBConnectionException,
            DBExecutionException, Exception, CircularDependencyException {

        XMLDBModel dbModel = DBModelFetcher.load(name);

        if (dbModel == null) {
            return null;
        }

        // Make sure that all references in the model are by value.
        if (!byReference) {

            String modelContent = dbModel.getModel();

            String trueReference = "property name=\""
                    + XMLDBModel.DB_REFERENCE_ATTR + "\" "
                    + "class=\"ptolemy.data.expr.StringConstantParameter\" "
                    + "value=\"TRUE\"";

            String falseReference = "property name=\""
                    + XMLDBModel.DB_REFERENCE_ATTR + "\" "
                    + "class=\"ptolemy.data.expr.StringConstantParameter\" "
                    + "value=\"FALSE\"";

            modelContent = modelContent.replaceAll(trueReference,
                    falseReference);

            trueReference = "property class=\"ptolemy.data.expr.StringConstantParameter\" "
                    + "name=\""
                    + XMLDBModel.DB_REFERENCE_ATTR
                    + "\" "
                    + "value=\"TRUE\"";

            falseReference = "property class=\"ptolemy.data.expr.StringConstantParameter\" "
                    + "name=\""
                    + XMLDBModel.DB_REFERENCE_ATTR
                    + "\" "
                    + "value=\"FALSE\"";

            modelContent = modelContent.replaceAll(trueReference,
                    falseReference);

            dbModel.setModel(modelContent);

        }

        Entity returnEntity = _getEntity(dbModel);

        if (byReference) {

            if (_circularDependencyExists(name, container.getName())) {

                throw new CircularDependencyException("This import would "
                        + "result in a circular dependency.");

            } else {

                if (returnEntity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) == null) {

                    String referenceTag = "<property name=\""
                            + XMLDBModel.DB_REFERENCE_ATTR
                            + "\" "
                            + "class=\"ptolemy.data.expr.StringConstantParameter\" "
                            + "value=\"TRUE\"></property>";

                    MoMLChangeRequest change = new MoMLChangeRequest(null,
                            returnEntity, referenceTag);

                    change.setUndoable(true);
                    returnEntity.requestChange(change);

                } else {

                    ((StringConstantParameter) returnEntity
                            .getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                            .setExpression("TRUE");

                }

            }

        } else {

            if (returnEntity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) == null) {

                String referenceTag = "<property name=\""
                        + XMLDBModel.DB_REFERENCE_ATTR
                        + "\" "
                        + "class=\"ptolemy.data.expr.StringConstantParameter\" "
                        + "value=\"FALSE\"></property>";

                MoMLChangeRequest change = new MoMLChangeRequest(null,
                        returnEntity, referenceTag);

                change.setUndoable(true);
                returnEntity.requestChange(change);

            } else {

                ((StringConstantParameter) returnEntity
                        .getAttribute(XMLDBModel.DB_REFERENCE_ATTR))
                        .setExpression("FALSE");

            }

        }

        // Make the entity name unique within container.
        returnEntity.setName(container.uniqueName(returnEntity.getName()));

        return returnEntity;

    }

    /** Given a model name, return a PtolemyEffigy objects.
     *
     * @param name
     *          The model name.  An alphanumeric
     *          string without special characters.
     *          If no model with the given name is found, return null.
     * @param configuration
     *          The configuration used to create the effigy.
     * @return
     *          A PtolemyEffigy object that the
     *          GUI can display.
     * @exception DBConnectionException
     *          Thrown by DBModelFetcher if a problem occurs with the
     *          database connection.
     * @exception DBExecutionException
     *          Thrown by DBModelFetcher if a problem while executing a
     *          command.
     * @exception Exception
     *          Thrown if a problem occurs creating an effigy from the MoML.
     */
    public static PtolemyEffigy loadModel(String name,
            Configuration configuration) throws DBConnectionException,
            DBExecutionException, Exception {

        XMLDBModel dbModel = DBModelFetcher.load(name);

        return createEffigy(dbModel, configuration);

    }

    /** Given a model id, return a PtolemyEffigy objects.
     *
     * @param id
     *          The model id.  An alphanumeric
     *          string without special characters.
     *          If no model with the given name is found, return null.
     * @param configuration
     *          The configuration used to create the effigy.
     * @return
     *          A PtolemyEffigy object that the
     *          GUI can display.
     * @exception DBConnectionException
     *          Thrown by DBModelFetcher if a problem occurs with the
     *          database connection.
     * @exception DBExecutionException
     *          Thrown by DBModelFetcher if a problem while executing a
     *          command.
     * @exception Exception
     *          Thrown if a problem occurs creating an effigy from the MoML.
     */
    public static PtolemyEffigy loadModelUsingId(String id,
            Configuration configuration) throws DBConnectionException,
            DBExecutionException, Exception {

        XMLDBModel dbModel = DBModelFetcher.loadUsingId(id);

        return createEffigy(dbModel, configuration);

    }

    /**
     * Get the list of models for the given page number.
     *
     * @param pageNumber Page number of the page whose models need to be fetched.
     * @return List of models from the given page.
     * @exception DBConnectionException If thrown while connecting to the database.
     * @exception DBExecutionException If thrown while executing query in the
     * database.
     */
    public List<XMLDBModel> getAllModelsFromDatabase(int pageNumber)
            throws DBConnectionException, DBExecutionException {
        List<XMLDBModel> modelsList = new ArrayList<XMLDBModel>();
        if (!_isFetched) {
            getAllModelsFromDatabase();
        }

        if (_numberOfPages > 0) {
            int endIndex = pageNumber * NO_OF_ITEMS_PER_PAGE;
            int startIndex = endIndex - NO_OF_ITEMS_PER_PAGE;
            if (endIndex > _allModelsList.size()) {
                endIndex = _allModelsList.size();
            }
            for (int i = startIndex; i < endIndex; i++) {
                modelsList.add(_allModelsList.get(i));
            }
        }

        return modelsList;
    }

    /** Fetch and store locally the list of all models in the database.
     *
     * @exception DBConnectionException If thrown while connecting to the
     * database.
     * @exception DBExecutionException  If thrown while executing the
     * query in the database.
     *
     */
    public void getAllModelsFromDatabase() throws DBConnectionException,
    DBExecutionException {
        DBConnection conn = DBConnectorFactory.getSyncConnection(false);
        _allModelsList = conn.executeGetListOfAllModels();
        if (_allModelsList != null) {
            _numberOfPages = _allModelsList.size() / NO_OF_ITEMS_PER_PAGE + 1;
        } else {
            _numberOfPages = 0;
        }
        _isFetched = true;
    }

    /** Get the total number of models in the database.
     *
     * @return Total number of models in the database.
     * @exception DBConnectionException If thrown while connecting to the
     * database.
     * @exception DBExecutionException  If thrown while executing the
     * query in the database.
     */
    public int getTotalNumberOfModels() throws DBConnectionException,
    DBExecutionException {
        if (!_isFetched) {
            getAllModelsFromDatabase();
        }
        return _allModelsList != null ? _allModelsList.size() : 0;
    }

    /** Get the total number of pages required to display the
     * models list.
     *
     * @return Total number of pages in the models list.
     * @exception DBConnectionException If thrown while connecting to the
     * database.
     * @exception DBExecutionException  If thrown while executing the
     * query in the database.
     */
    public int getNoOfPages() throws DBConnectionException,
    DBExecutionException {
        if (!_isFetched) {
            getAllModelsFromDatabase();
        }
        return _numberOfPages;
    }

    /**
     * Create a Ptolemy Effigy from the given XMLDBModel.
     *
     * @param dbModel Model that needs to be converted into Ptolemy Effigy.
     * @param configuration The configuration used to create the effigy.
     * @return  A PtolemyEffigy object that the GUI can display.
     * @exception Exception Thrown if a problem occurs creating an effigy from the
     * MoML.
     */
    private static PtolemyEffigy createEffigy(XMLDBModel dbModel,
            Configuration configuration) throws Exception {

        if (dbModel == null) {
            return null;
        }

        PtolemyEffigy returnEffigy = _getEffigy(dbModel, configuration);
        return returnEffigy;
    }

    /** Check if a circular dependency would result from importing a child
     * model.
     *
     * @param modelName
     *          The name of the model being imported.
     * @param containerName
     *          The name of the container into which the model is being
     *          imported.
     * @return
     *          Indication if the import results in a circular dependency.
     *          True if it does.  False if it does not.
     * @exception DBConnectionException
     *          Thrown if a problem occurs with the database connection.
     * @exception DBExecutionException
     *          Thrown if a problem occurs getting the reference string for
     *          modelName.
     */
    private static boolean _circularDependencyExists(String modelName,
            String containerName) throws DBConnectionException,
            DBExecutionException {

        boolean returnValue = false;

        DBConnection connection = DBConnectorFactory.getSyncConnection(false);

        try {

            GetReferenceStringTask getReferenceStringTask = new GetReferenceStringTask(
                    modelName);
            String referenceString = connection
                    .executeGetReferenceStringTask(getReferenceStringTask);

            if (referenceString == null) {

                throw new DBExecutionException("Model References could not "
                        + "be retrieved in the database.  "
                        + "Rebuild the Reference file.");
            }

            if (Utilities.modelReferenceExists(containerName, referenceString)) {

                returnValue = true;

            }

        } catch (DBExecutionException dbEx) {
            throw dbEx;
        } finally {
            if (connection != null) {
                connection.closeConnection();
                connection = null;
            }
        }

        return returnValue;

    }

    /** Generate an effigy from an XMLDBModel object.
     *
     * @param dbModel
     *          XMLDBModel object containing the model's MoML.
     * @param configuration
     *          Configuration used to create the effigy.
     * @return
     *         PtolemyEffigy.
     * @exception Exception
     *          Thrown if a problem occurs parsing MoML
     */
    private static PtolemyEffigy _getEffigy(XMLDBModel dbModel,
            Configuration configuration) throws Exception {

        PtolemyEffigy returnEffigy = null;

        Entity entity = _getEntity(dbModel);

        // If the model is already open, bring it to the front.
        // Otherwise, a new PtolemyEffigy is created.
        if (configuration.getDirectory().getEntity(entity.getName()) != null) {

            return (PtolemyEffigy) configuration.getDirectory().getEffigy(
                    entity.getName());

        }

        returnEffigy = new PtolemyEffigy(configuration.workspace());
        returnEffigy.setModel(entity);

        returnEffigy.setName(configuration.getDirectory().uniqueName(
                entity.getName()));
        returnEffigy.setContainer(configuration.getDirectory());

        returnEffigy.identifier.setExpression(returnEffigy.getName());

        return returnEffigy;

    }

    /** Generate an entity from an XMLDBModel object.
     *
     * @param dbModel
     *          XMLDBModel object containing the model's MoML.
     * @return
     *         Entity.
     * @exception Exception
     *          Thrown if a problem occurs parsing MoML
     *
     */
    private static Entity _getEntity(XMLDBModel dbModel) throws Exception {

        MoMLParser parser = new MoMLParser();

        parser.resetAll();

        Entity entity = (Entity) parser.parse(dbModel.getModel());

        return entity;

    }

    /** List of all the XMLDBModel names in the database. */
    private List<XMLDBModel> _allModelsList;

    /** Number of results displayed per page on the models list page. */
    public static int NO_OF_ITEMS_PER_PAGE = 25;

    /** Number of pages that can filled with the given number of results. */
    private int _numberOfPages;

    /** Boolean that describes if the list of models has been fetched or not. */
    private boolean _isFetched;
}
