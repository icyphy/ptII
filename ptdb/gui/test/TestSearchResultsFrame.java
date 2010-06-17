package ptdb.gui.test;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import ptdb.common.dto.XMLDBModel;
import ptdb.gui.SearchResultsFrame;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.Entity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////
//// TestSearchResultsFrame

/**
 * Class for testing SearchResultsFrame.  This is a complicated
 * GUI.  The display is somewhat difficult to troubleshoot.
 *
 * @author Lyle Holsinger
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 */

public class TestSearchResultsFrame {

    /** Main class for running SearchResultsFrame in a test capacity.
     *
     * @param args
     *          Command line arguments.
     */
    public static void main(String args[]) {

        JFrame originator = new JFrame();

        try {

            SearchResultsFrame frame = new SearchResultsFrame(getModel(),
                    originator, getConfiguration());
            frame.pack();
            frame.setVisible(true);

            List<List<XMLDBModel>> parentHierarchy = new ArrayList();

            List<XMLDBModel> parentBranch;// = new ArrayList();

            for (int i = 0; i < 5; i++) {

                parentBranch = new ArrayList();

                for (int j = 0; j < 10; j++) {

                    parentBranch.add(new XMLDBModel());
                    parentBranch.get(j).setIsNew(false);
                    parentBranch.get(j).setModelName("model" + j * i);

                }

                parentHierarchy.add(parentBranch);

            }

            parentBranch = new ArrayList();
            parentBranch.add(new XMLDBModel());
            parentBranch.get(0).setIsNew(false);
            parentBranch.get(0).setModelName("LonelyModel");
            parentHierarchy.add(parentBranch);
            
            XMLDBModel searchResult = new XMLDBModel();
            List<XMLDBModel> searchResultList = new ArrayList();

            searchResult.setIsNew(false);
            searchResult.setModelName("model1");
            searchResult.setParents(parentHierarchy);

            
            XMLDBModel searchResult2 = new XMLDBModel();
            searchResult2.setIsNew(false);
            searchResult2.setModelName("model2");
            searchResult2.setParents(null);
            
            searchResultList.add(searchResult);            
            searchResultList.add(searchResult2);

            frame.display(searchResultList);
            

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    private static NamedObj getModel() throws Exception {

        MoMLParser parser = new MoMLParser();

        Configuration configuration = getConfiguration();

        XMLDBModel dbModel = new XMLDBModel();
        dbModel.setIsNew(false);
        dbModel
                .setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                        + "<entity name=\"model1\" class=\"ptolemy.actor.TypedCompositeActor\">"                        + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                        + "</property>"
                        + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\" value=\"{bounds={232, 141, 815, 517}, maximized=false}\">"
                        + "</property>"
                        + "<property name=\"_vergilSize\" class=\"ptolemy.actor.gui.SizeAttribute\" value=\"[600, 400]\">"
                        + "</property>"
                        + "<property name=\"_vergilZoomFactor\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"1.0\">"
                        + "</property>"
                        + "<property name=\"_vergilCenter\" class=\"ptolemy.data.expr.ExpertParameter\" value=\"{300.0, 200.0}\">"
                        + "</property>"
                        + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                        + "<doc>Create a constant sequence.</doc>"
                        + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                        + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                        + "</property>"
                        + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                        + "</property>"
                        + "</property>"
                        + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{150, 150}\">"
                        + "</property>" + "</entity>" + "</entity>");
        //Demonstrate that the name is taken from the MoML.
        dbModel.setModelName("model2");

        PtolemyEffigy returnEffigy = null;

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

        return returnEffigy.getModel();
    }

    private static Configuration getConfiguration() throws Exception {
        MoMLParser parser = new MoMLParser();
        parser.reset();
        String configPath = "ptolemy/configs/ptdb/configuration.xml";

        URL configURL = ConfigurationApplication.specToURL(configPath);
        Configuration configuration = (Configuration) parser.parse(configURL,
                configURL);

        return configuration;
    }
}
