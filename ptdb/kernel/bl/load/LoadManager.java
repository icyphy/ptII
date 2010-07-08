/*
@Copyright (c) 2010 The Regents of the University of California.
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

import java.net.URI;
import java.util.List;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptdb.common.exception.DBExecutionException;
import ptolemy.data.expr.DBParameter;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
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
    */
   public  static Entity importModel(String name, boolean byReference
           , NamedObj container)
           throws DBConnectionException, DBExecutionException, Exception {

       XMLDBModel dbModel = DBModelFetcher.load(name);
       
       if(dbModel == null) return null;
       
       Entity returnEntity = _getEntity(dbModel);
       
       if(byReference){

           if (returnEntity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) == null) {

               String referenceTag = "<property name=\"" + XMLDBModel.DB_REFERENCE_ATTR + "\" " +
               		"class=\"ptolemy.data.expr.DBParameter\" " +
               		"value=\"TRUE\"></property>";
               
               MoMLChangeRequest change = new MoMLChangeRequest(null,
                       returnEntity, referenceTag);
                   
               change.setUndoable(true);
               returnEntity.requestChange(change);
               
           } else {
           
               ((DBParameter) returnEntity
                   .getAttribute(XMLDBModel.DB_REFERENCE_ATTR)).setExpression("TRUE");
 
           }
       
       } else {
           
           if (returnEntity.getAttribute(XMLDBModel.DB_REFERENCE_ATTR) == null) {

               String referenceTag = "<property name=\"" + XMLDBModel.DB_REFERENCE_ATTR + "\" " +
                        "class=\"ptolemy.data.expr.DBParameter\" " +
                        "value=\"FALSE\"></property>";
               
               MoMLChangeRequest change = new MoMLChangeRequest(null,
                       returnEntity, referenceTag);
                   
               change.setUndoable(true);
               returnEntity.requestChange(change);
                   
           } else {
           
               ((DBParameter) returnEntity
                   .getAttribute(XMLDBModel.DB_REFERENCE_ATTR)).setExpression("FALSE");
 
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
