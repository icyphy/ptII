package ptdb.kernel.bl.load;

import java.net.URI;
import java.util.List;
import java.util.ArrayList;

import ptdb.common.dto.XMLDBModel;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ModelDirectory;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.moml.MoMLParser;

/**
* This is the business layer is used by the GUI to pass models to load.
* It uses the LoadManager to interface with the database to get MoML strings. 
*
* @author Lyle Holsinger
* @since Ptolemy II 8.1
* @version $Id$
* @Pt.ProposedRating red (lholsing)
* @Pt.AcceptedRating red (lholsing)
*/

public class LoadManagerInterface {

    /** Use a given list of models to generate PtolemyEffigy objects 
     *  returned to the GUI.
     *  
     * @param names
     *          List of model names.
     * @param configuration
     *          The configuration used to create the effigies.
     * @param directory
     *          The directory used to create the effigies.
     * @return
     *          An ArrayList of PtolemyEffigy objects that the
     *          GUI can display.
     * @throws DBConnectionException
     * @throws Exception
     */
    public ArrayList<PtolemyEffigy> loadModels(String[] names,
            Configuration configuration) throws Exception {

        //We instantiate a LoadModelManager then pass the names array.
        LoadModelManager lmm = new LoadModelManager();
        ArrayList<XMLDBModel> dbModels = lmm.load(names);

        //Convert ArrayList of XMLDBModels into an ArrayList of effigies.
        return getEffigies(dbModels, configuration);

    }

    /** Generate effigies from XMLDBModel objects.
     * 
     * @param dbModels
     *          ArayList of XMLDBModel objects containing their MoML.
     * @param configuration
     *          Configuration used to create the effigies.
     * @param directory
     *          Directory used to create the effigies.
     * @return
     *          ArrayList of PtolemyEffigies.
     * @throws Exception
     */
    private ArrayList<PtolemyEffigy> getEffigies(
            ArrayList<XMLDBModel> dbModels, Configuration configuration)
            throws Exception {

        ArrayList<PtolemyEffigy> effigyList = new ArrayList<PtolemyEffigy>();

        MoMLParser parser = new MoMLParser();

        for (int i = 0; i < dbModels.size(); i++) {

            Entity entity = new Entity();
            parser.reset();

            entity = (Entity) parser.parse(dbModels.get(i).getModel());

            effigyList.add(new PtolemyEffigy(configuration.workspace()));

            effigyList.get(i).setModel(entity);

            // Look to see whether the model has a URIAttribute.
            List attributes = entity.attributeList(URIAttribute.class);

            if (attributes.size() > 0) {

                // The entity has a URI, which was probably
                // inserted by MoMLParser.
                URI uri = ((URIAttribute) attributes.get(0)).getURI();

                // Set the URI and identifier of the effigy.
                effigyList.get(i).uri.setURI(uri);
                effigyList.get(i).identifier.setExpression(uri.toString());

                // Put the effigy into the directory
                effigyList.get(i).setName(
                        configuration.getDirectory().uniqueName(
                                entity.getName()));
                effigyList.get(i).setContainer(configuration.getDirectory());

            }

        }

        return effigyList;

    }

}
