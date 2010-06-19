package ptdb.kernel.bl.save.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Test;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.kernel.bl.load.LoadManager;
import ptdb.kernel.bl.save.SaveModelManager;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.moml.MoMLParser;

///////////////////////////////////////////////////////////////
//// TestSaveRequirementsIntegration

/**
 * JUnit test for integration testing of the Save feature.
 * 
 * 
 * 
 * @author Lyle Holsinger
 * @version $$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating red (lholsing)
 * @Pt.AcceptedRating red (lholsing)
 * 
 */

public class TestSaveRequirementsIntegration {

    @Test
    public void testNullModel() throws Exception {
        
        XMLDBModel dbModel = null;
        
        boolean exceptionThrown = false;
        
        SaveModelManager saveModelManager = new SaveModelManager();

        try{

            saveModelManager.save(dbModel);
            
        } catch (IllegalArgumentException e){

            exceptionThrown = true;
            
        }
        

        assertNotNull(exceptionThrown);
        
    }
    
    @Test
    public void testSavingNewModel() throws Exception {
        
        MoMLParser parser = new MoMLParser();
        parser.reset();
        String configPath = "ptolemy/configs/ptdb/configuration.xml";

        URL configURL = ConfigurationApplication.specToURL(configPath);
        Configuration configuration = (Configuration) parser.parse(configURL,
                configURL);

        PtolemyEffigy effigy = null;
        
        java.util.Date time = new java.util.Date();
        
        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                        + "<entity name=\"" + dbModel.getModelName() + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                        + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
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
        
        SaveModelManager saveModelManager = new SaveModelManager();

        boolean saved = saveModelManager.save(dbModel);
        assertTrue(saved);
        
        effigy = LoadManager.loadModel(dbModel.getModelName(), configuration);
        
        boolean equal = effigy.getModel().getName().equals(dbModel.getModelName());
        
        assertTrue(equal);
        
    }
    
    
    @Test
    public void testUpdatingModel() throws Exception {
        
        MoMLParser parser = new MoMLParser();
        parser.reset();
        String configPath = "ptolemy/configs/ptdb/configuration.xml";

        URL configURL = ConfigurationApplication.specToURL(configPath);
        Configuration configuration = (Configuration) parser.parse(configURL,
                configURL);

        PtolemyEffigy effigy = null;
        
        java.util.Date time = new java.util.Date();
        
        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                        + "<entity name=\"" + dbModel.getModelName() + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                        + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
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
        
        SaveModelManager saveModelManager = new SaveModelManager();

        saveModelManager.save(dbModel);

        // We know that the model is already in the DB, 
        // so test overwriting it.
        dbModel.setIsNew(false);

        boolean saved = saveModelManager.save(dbModel);
        
        assertTrue(saved);
        
        effigy = LoadManager.loadModel(dbModel.getModelName(), configuration);
        
        boolean equal = effigy.getModel().getName().equals(dbModel.getModelName());
        
        assertTrue(equal);
        
    }
    
    @Test
    public void testExceptions() throws Exception {
        
        MoMLParser parser = new MoMLParser();
        parser.reset();
        String configPath = "ptolemy/configs/ptdb/configuration.xml";

        URL configURL = ConfigurationApplication.specToURL(configPath);
        Configuration configuration = (Configuration) parser.parse(configURL,
                configURL);

        PtolemyEffigy effigy = null;
        
        java.util.Date time = new java.util.Date();
        
        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                        + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                        + "<entity name=\"" + dbModel.getModelName() + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                        + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
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
        
        SaveModelManager saveModelManager = new SaveModelManager();

        boolean exceptionThrown = false;
        boolean saved = false;
        
        try{
        
            saved = saveModelManager.save(dbModel);
        
        } catch(DBExecutionException e){
            
            exceptionThrown = true;
            
        }
        
        assertTrue(exceptionThrown);
        assertTrue(!saved);
       
        exceptionThrown = false; 
        saved = false;
        
        dbModel.setIsNew(true);
        saved = saveModelManager.save(dbModel);
        assertTrue(saved);
        saved = false;
        
        try{
            
            saved = saveModelManager.save(dbModel);
        
        } catch(ModelAlreadyExistException e){
            
            exceptionThrown = true;
            
        }
        
        assertTrue(exceptionThrown);
        assertTrue(!saved);
        
        effigy = LoadManager.loadModel(dbModel.getModelName(), configuration);
        
        boolean equal = (effigy != null);
        
        assertTrue(equal);
        
    }
}
