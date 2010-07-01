package ptdb.kernel.bl.save;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ptdb.common.dto.CreateModelTask;
import ptdb.common.dto.SaveModelTask;
import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.common.exception.XMLDBModelParsingException;
import ptdb.common.util.DBConnectorFactory;
import ptdb.common.util.Utilities;
import ptdb.kernel.database.DBConnection;

///////////////////////////////////////////////////////////////////
//// SaveModelManager

/**
 * The business layer of the save to database function.
 * 
 * <p> 
 * It is responsible for:
 * 
 * <br>- Get the XMLDBModel which is the Ptolemy model wrapped in a special Data
 * Transfer Object (dto).
 * 
 * <br>- Wrap the model object in another dto (SaveModelTask or CreateModelTask)
 * to either create it in the database or save the modification made on it.
 * 
 * <br>- Get a database connection and run the execute method on the database
 * connection object to perform the saving or creation of the model. 
 * </p>
 * 
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (Yousef)
 * @Pt.AcceptedRating Red (Yousef)
 * 
 */
public class SaveModelManager {

    ///////////////////////////////////////////////////////////////////
    ////                public methods                            ////

    /**
     * Save the changes of an existing model in the database or create a new
     * model in the database.
     * 
     * @param xmlDBModel The model object that is required to be saved or
     * created in the database.
     * 
     * @return A boolean indicator of weather the operation was successful or
     * not.
     * 
     * @exception DBConnectionException Thrown if there is a database connection error.
     * @exception DBExecutionException Thrown if the execution failed.
     * @exception IllegalArgumentException Thrown if the parameters were not right.
     * @exception ModelAlreadyExistException Thrown if the model being created already exists.
     * @throws XMLDBModelParsingException Thrown if the model is parsed incorrectly.
     * 
     */
    public boolean save(XMLDBModel xmlDBModel) throws DBConnectionException,
            DBExecutionException, IllegalArgumentException,
            ModelAlreadyExistException, XMLDBModelParsingException {

        boolean isSuccessful = false;

        DBConnection dbConnection = null;

        try {

            if (xmlDBModel == null) {
                throw new IllegalArgumentException(
                        "Failed while attempting to save."
                                + " The XMLDBModel to be saved is null");
            }
            xmlDBModel = populateChildModelsList(xmlDBModel);
            dbConnection = DBConnectorFactory.getSyncConnection(true);

            if (dbConnection == null) {
                throw new DBConnectionException(
                        "Unable to get synchronous connection from the database");
            }

            if (xmlDBModel.getIsNew()) {

                CreateModelTask createModelTask = new CreateModelTask(
                        xmlDBModel);

                dbConnection.executeCreateModelTask(createModelTask);

                isSuccessful = true;

                dbConnection.commitConnection();

            } else {

                SaveModelTask saveModelTask = new SaveModelTask(xmlDBModel);

                dbConnection.executeSaveModelTask(saveModelTask);

                isSuccessful = true;

                dbConnection.commitConnection();

            }

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

        return isSuccessful;

    }

    /**
     * Populate the referenced child models list and update the model XML by 
     * replacing the referenced models with place holder.  
     * 
     * @param model Model with references to be resolved.
     * 
     * @return The updates model containing the list of child models and 
     * updated content.
     * 
     * @throws XMLDBModelParsingException If thrown while parsing the XML. 
     */
    public XMLDBModel populateChildModelsList(XMLDBModel model)
            throws XMLDBModelParsingException {
        
        if(model.getModel() == null) {
            return model;
        }
        
        Document modelDocument = (Document) Utilities
                .parseXML(model.getModel());
        /*
         * First level nodes.
         */
        NodeList entityList = modelDocument.getFirstChild().getChildNodes();

        boolean isChanged = false;

        if (entityList != null) {

            for (int i = 0; i < entityList.getLength(); i++) {

                Node entity = entityList.item(i);

                if (!"entity".equals(entity.getNodeName())) {
                    continue;
                }

                /* Get all first-level nodes inside the given entity. */
                NodeList parameterList = entity.getChildNodes();

                String referencedModelName = null;
                boolean isReferenced = false;
                int noOfParametersFound = 0;

                /* Get value for the DBReference and DBModelName properties.*/
                for (int j = 0; j < parameterList.getLength(); j++) {

                    Node parameter = parameterList.item(j);

                    if ("property".equals(parameter.getNodeName())) {

                        String name = Utilities.getValueForAttribute(parameter,
                                "name");

                        if ("DBModelName".equals(name)) {

                            referencedModelName = Utilities
                                    .getValueForAttribute(parameter, "value");

                            noOfParametersFound++;

                        } else if ("DBReference".equals(name)) {

                            String value = Utilities.getValueForAttribute(
                                    parameter, "value");
                            isReferenced = "TRUE".equals(value);

                            noOfParametersFound++;
                        }

                        if (noOfParametersFound == 2) {
                            break;
                        }
                    }
                }

                if (isReferenced && referencedModelName != null) {

                    /*
                     * As we are considering only "entity" nodes, we can be 
                     * sure that the type conversion will not fail.
                     */
                    Element entityElement = (Element) entity.cloneNode(false);
                    NodeList childNodesList = entity.getChildNodes();

                    /*
                     * Create an entity node with the required properties and 
                     * replace the current referenced entity.
                     */
                    for (int k = 0; k < childNodesList.getLength(); k++) {
                        Node childNode = childNodesList.item(k);

                        if ("property".equals(childNode.getNodeName())) {

                            String name = Utilities.getValueForAttribute(
                                    childNode, "name");

                            if (name != null
                                    && (name.startsWith("_")
                                            || "DBReference".equals(name) || "DBModelName"
                                            .equals(name))) {
                                entityElement.appendChild(childNode);
                            }
                        }
                    }
                    
                    entityElement.setAttribute("DBModelName",
                            referencedModelName);
                    modelDocument.getFirstChild().replaceChild(entityElement, entity);

                    model.addReferencedChild(referencedModelName);
                    isChanged = true;
                }
            }
        }

        /* Update model content only if the model has changed. */
        if (isChanged) {

            String newModelContent = Utilities
                    .getDocumentXMLString(modelDocument);
            model.setModel(newModelContent);

        }

        return model;
    }
}
