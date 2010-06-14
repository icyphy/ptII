package ptdb.kernel.bl.load;

import java.net.URI;
import java.util.List;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBConnectionException;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.moml.MoMLParser;

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

public class LoadManagerInterface {

    /** Given a model name, return a PtolemyEffigy objects.
     *
     * @param name
     *          The model name.
     * @param configuration
     *          The configuration used to create the effigy.
     * @return
     *          A PtolemyEffigy object that the
     *          GUI can display.
     * @exception DBConnectionException
     * @exception Exception
     */
    public PtolemyEffigy loadModel(String name, Configuration configuration)
            throws Exception {

        //Instantiate a LoadModelManager then pass the names array.
        LoadModelManager lmm = new LoadModelManager();
        XMLDBModel dbModel = lmm.load(name);

        //Convert the XMLDBModel into an effigy.
        return getEffigy(dbModel, configuration);

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
     */
    private PtolemyEffigy getEffigy(XMLDBModel dbModel,
            Configuration configuration) throws Exception {

        PtolemyEffigy returnEffigy = null;

        MoMLParser parser = new MoMLParser();

        Entity entity = new Entity();
        parser.reset();

        entity = (Entity) parser.parse(dbModel.getModel());

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

}
