package ptdb.kernel.bl.load;

import java.net.URI;
import java.util.List;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////////
//// LoadManager

/**
* The business layer is used by the GUI to pass models to load.
* It uses the LoadManager to interface with the database to get MoML strings.
*
* @author Lyle Holsinger
* @since Ptolemy II 8.1
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
    */
   public  static Entity importModel(String name, boolean byReference)
           throws DBConnectionException, DBExecutionException, Exception {

       XMLDBModel dbModel = DBModelFetcher.load(name);
       
       if(dbModel == null) return null;
       
       Entity returnEntity = _getEntity(dbModel);
       
       if(byReference){

           if (returnEntity.getAttribute("DBReference") == null) {

               String referenceTag = "<property name=\"DBReference\" " +
               		"class=\"ptolemy.data.expr.StringParameter\" " +
               		"value=\"TRUE\"></property>";
               
               MoMLChangeRequest change = new MoMLChangeRequest(null,
                       returnEntity, referenceTag);
                   
               change.setUndoable(true);
               returnEntity.requestChange(change);
               
           } else {
           
               ((StringParameter) returnEntity
                   .getAttribute("DBReference")).setExpression("TRUE");
 
           }
       
       } else {
           
           if (returnEntity.getAttribute("DBReference") == null) {

               String referenceTag = "<property name=\"DBReference\" " +
                        "class=\"ptolemy.data.expr.StringParameter\" " +
                        "value=\"FALSE\"></property>";
               
               MoMLChangeRequest change = new MoMLChangeRequest(null,
                       returnEntity, referenceTag);
                   
               change.setUndoable(true);
               returnEntity.requestChange(change);
                   
           } else {
           
               ((StringParameter) returnEntity
                   .getAttribute("DBReference")).setExpression("FALSE");
 
           }
       
       }
       
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
    public  static PtolemyEffigy loadModel(String name, Configuration configuration)
            throws DBConnectionException, DBExecutionException, Exception {

        XMLDBModel dbModel = DBModelFetcher.load(name);
        
        if(dbModel == null) return null;
        
        PtolemyEffigy returnEffigy = _getEffigy(dbModel, configuration);
        
        return returnEffigy;

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

        returnEffigy = new PtolemyEffigy(configuration.workspace());
        returnEffigy.setModel(entity);

        // Look to see whether the model has a URIAttribute.
        List attributes = entity.attributeList(URIAttribute.class);

        if (attributes.size() > 0) {

            // The entity has a URI, which was probably
            // inserted by MoMLParser.
            URI uri = ((URIAttribute) attributes.get(0)).getURI();

            // Set the URI and identifier of the effigy.
            returnEffigy.uri.setURI(uri);
            returnEffigy.identifier.setExpression(uri.toString());

            // Put the effigy into the directory
            returnEffigy.setName(configuration.getDirectory().uniqueName(
                    entity.getName()));
            returnEffigy.setContainer(configuration.getDirectory());

        }

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
    
        Entity entity = new Entity();
        parser.reset();
    
        entity = (Entity) parser.parse(dbModel.getModel());
    
        return entity;

    }
    
}
