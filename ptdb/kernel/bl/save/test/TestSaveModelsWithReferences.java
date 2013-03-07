/*
@Copyright (c) 2010-2013 The Regents of the University of California.
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
package ptdb.kernel.bl.save.test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import ptdb.common.dto.XMLDBModel;
import ptdb.common.exception.DBExecutionException;
import ptdb.common.exception.ModelAlreadyExistException;
import ptdb.kernel.bl.save.SaveModelManager;

///////////////////////////////////////////////////////////////////
////TestSaveModelsWithReferences

/**
 * JUnit test for saving/updating models with and without references and getting new
 * model Id after each successful save operation when new save is done and the same
 * model Id after each successful update operation.
 *
 *
 * @author Yousef Alsaeed
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (yalsaeed)
 * @Pt.AcceptedRating Red (yalsaeed)
 *
 */

public class TestSaveModelsWithReferences {

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    /**
     * Test the save with null model passed.
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testNullModel() throws Exception {

        XMLDBModel dbModel = null;

        boolean exceptionThrown = false;

        SaveModelManager saveModelManager = new SaveModelManager();

        try {

            saveModelManager.save(dbModel);

        } catch (IllegalArgumentException e) {

            exceptionThrown = true;

        }

        assertTrue(exceptionThrown);

    }

    /**
     * Test saving a new model in the database which does not have references.
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testSavingNewModel_NoReferences() throws Exception {

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        String modelId = saveModelManager.save(dbModel);
        assertTrue(modelId.contains(dbModel.getModelName()));

        //System.out.println(modelId);
    }

    /**
     * Test the saving of a new model which has references in it.
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testSavingNewModel_WithReferences() throws Exception {

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"FALSE\">"
                + "</property>"
                + "<property name=\"author\" class=\"ptolemy.data.expr.StringParameter\" value=\"test_ref\">"
                + "<property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">"
                + "</property>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.ValueIcon\">"
                + "<property name=\"_color\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"{1.0, 0.0, 0.0, 1.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "<configure>"
                + "<svg>"
                + "<text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\"/>"
                + "</svg>"
                + "</configure>"
                + "</property>"
                + "<property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.VisibleParameterEditorFactory\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{250.0, 170.0}\">"
                + "</property>"
                + "</property>"
                + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{110, 85}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"Display\" class=\"ptolemy.actor.lib.gui.Display\">"
                + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\">"
                + "</property>"
                + "<property name=\"_paneSize\" class=\"ptolemy.actor.gui.SizeAttribute\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{500, 110}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"modeltest\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\""
                + XMLDBModel.DB_MODEL_ID_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"modeltest_1278356855859\">"
                + "</property>"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"TRUE\">"
                + "</property>"
                + "<property name=\"author\" class=\"ptolemy.data.expr.StringParameter\" value=\"test\">"
                + "<property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">"
                + "</property>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.ValueIcon\">"
                + "<property name=\"_color\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"{1.0, 0.0, 0.0, 1.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "<configure>"
                + "<svg>"
                + "<text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\"/>"
                + "</svg>"
                + "</configure>"
                + "</property>"
                + "<property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.VisibleParameterEditorFactory\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{250.0, 170.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"DBModelName\" class=\"ptolemy.data.expr.StringParameter\" value=\"modeltest\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[300.0, 95.0]\">"
                + "</property>"
                + "<port name=\"port\" class=\"ptolemy.actor.TypedIOPort\">"
                + "<property name=\"input\"/>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[25.0, 150.0]\">"
                + "</property>"
                + "</port>"
                + "<port name=\"port2\" class=\"ptolemy.actor.TypedIOPort\">"
                + "<property name=\"output\"/>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{580.0, 200.0}\">"
                + "</property>"
                + "</port>"
                + "<entity name=\"Const2\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{125, 200}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"AddSubtract\" class=\"ptolemy.actor.lib.AddSubtract\">"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[275.0, 125.0]\">"
                + "</property>"
                + "</entity>"
                + "<relation name=\"relation2\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation3\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<link port=\"port\" relation=\"relation\"/>"
                + "<link port=\"port2\" relation=\"relation3\"/>"
                + "<link port=\"Const2.output\" relation=\"relation2\"/>"
                + "<link port=\"AddSubtract.plus\" relation=\"relation2\"/>"
                + "<link port=\"AddSubtract.plus\" relation=\"relation\"/>"
                + "<link port=\"AddSubtract.output\" relation=\"relation3\"/>"
                + "</entity>"
                + "<relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation2\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<link port=\"Const.output\" relation=\"relation\"/>"
                + "<link port=\"Display.input\" relation=\"relation2\"/>"
                + "<link port=\"modeltest.port\" relation=\"relation\"/>"
                + "<link port=\"modeltest.port2\" relation=\"relation2\"/>"
                + "</entity>");

        //System.out.println(dbModel.getModel());

        SaveModelManager saveModelManager = new SaveModelManager();

        String modelId = saveModelManager.save(dbModel);
        assertTrue(modelId.contains(dbModel.getModelName()));

        //System.out.println(modelId);
    }

    /**
     * Test updating an existing model in the database which does not have references in it.
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testUpdateModel_NoReferences() throws Exception {

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        String modelId = saveModelManager.save(dbModel);

        dbModel.setIsNew(false);

        String newModelId = saveModelManager.save(dbModel);

        assertTrue(modelId.equals(newModelId));

        //System.out.println(modelId);
    }

    /**
     * Test updating a model in the database which has references in it.
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testUpdateModel_WithReferences() throws Exception {

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"FALSE\">"
                + "</property>"
                + "<property name=\"author\" class=\"ptolemy.data.expr.StringParameter\" value=\"test_ref\">"
                + "<property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">"
                + "</property>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.ValueIcon\">"
                + "<property name=\"_color\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"{1.0, 0.0, 0.0, 1.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "<configure>"
                + "<svg>"
                + "<text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\"/>"
                + "</svg>"
                + "</configure>"
                + "</property>"
                + "<property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.VisibleParameterEditorFactory\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{250.0, 170.0}\">"
                + "</property>"
                + "</property>"
                + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{110, 85}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"Display\" class=\"ptolemy.actor.lib.gui.Display\">"
                + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\">"
                + "</property>"
                + "<property name=\"_paneSize\" class=\"ptolemy.actor.gui.SizeAttribute\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{500, 110}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"modeltest\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\""
                + XMLDBModel.DB_MODEL_ID_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"modeltest_1278356855859\">"
                + "</property>"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"TRUE\">"
                + "</property>"
                + "<property name=\"author\" class=\"ptolemy.data.expr.StringParameter\" value=\"test\">"
                + "<property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">"
                + "</property>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.ValueIcon\">"
                + "<property name=\"_color\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"{1.0, 0.0, 0.0, 1.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "<configure>"
                + "<svg>"
                + "<text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\"/>"
                + "</svg>"
                + "</configure>"
                + "</property>"
                + "<property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.VisibleParameterEditorFactory\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{250.0, 170.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"DBModelName\" class=\"ptolemy.data.expr.StringParameter\" value=\"modeltest\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[300.0, 95.0]\">"
                + "</property>"
                + "<port name=\"port\" class=\"ptolemy.actor.TypedIOPort\">"
                + "<property name=\"input\"/>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[25.0, 150.0]\">"
                + "</property>"
                + "</port>"
                + "<port name=\"port2\" class=\"ptolemy.actor.TypedIOPort\">"
                + "<property name=\"output\"/>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{580.0, 200.0}\">"
                + "</property>"
                + "</port>"
                + "<entity name=\"Const2\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{125, 200}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"AddSubtract\" class=\"ptolemy.actor.lib.AddSubtract\">"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[275.0, 125.0]\">"
                + "</property>"
                + "</entity>"
                + "<relation name=\"relation2\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation3\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<link port=\"port\" relation=\"relation\"/>"
                + "<link port=\"port2\" relation=\"relation3\"/>"
                + "<link port=\"Const2.output\" relation=\"relation2\"/>"
                + "<link port=\"AddSubtract.plus\" relation=\"relation2\"/>"
                + "<link port=\"AddSubtract.plus\" relation=\"relation\"/>"
                + "<link port=\"AddSubtract.output\" relation=\"relation3\"/>"
                + "</entity>"
                + "<relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation2\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<link port=\"Const.output\" relation=\"relation\"/>"
                + "<link port=\"Display.input\" relation=\"relation2\"/>"
                + "<link port=\"modeltest.port\" relation=\"relation\"/>"
                + "<link port=\"modeltest.port2\" relation=\"relation2\"/>"
                + "</entity>");

        SaveModelManager saveModelManager = new SaveModelManager();

        String modelId = saveModelManager.save(dbModel);

        dbModel.setIsNew(false);

        String newModelId = saveModelManager.save(dbModel);

        assertTrue(modelId.equals(newModelId));

        //System.out.println(modelId);
    }

    /**
     * Test updating a model which does not exist in the database and
     * does not have references.
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testUpdatingModelNotInDB_NoReferences() throws Exception {

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        String modelId = "";

        try {

            modelId = saveModelManager.save(dbModel);

        } catch (DBExecutionException e) {

            exceptionThrown = true;

        }

        assertTrue(exceptionThrown);
        assertTrue(modelId.length() == 0);

    }

    /**
     * Test updating a model which does not exist in the database and has references in it.
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testUpdatingModelNotInDB_WithReferences() throws Exception {

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(false);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"FALSE\">"
                + "</property>"
                + "<property name=\"author\" class=\"ptolemy.data.expr.StringParameter\" value=\"test_ref\">"
                + "<property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">"
                + "</property>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.ValueIcon\">"
                + "<property name=\"_color\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"{1.0, 0.0, 0.0, 1.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "<configure>"
                + "<svg>"
                + "<text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\"/>"
                + "</svg>"
                + "</configure>"
                + "</property>"
                + "<property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.VisibleParameterEditorFactory\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{250.0, 170.0}\">"
                + "</property>"
                + "</property>"
                + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{110, 85}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"Display\" class=\"ptolemy.actor.lib.gui.Display\">"
                + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\">"
                + "</property>"
                + "<property name=\"_paneSize\" class=\"ptolemy.actor.gui.SizeAttribute\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{500, 110}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"modeltest\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\""
                + XMLDBModel.DB_MODEL_ID_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"modeltest_1278356855859\">"
                + "</property>"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"TRUE\">"
                + "</property>"
                + "<property name=\"author\" class=\"ptolemy.data.expr.StringParameter\" value=\"test\">"
                + "<property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">"
                + "</property>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.ValueIcon\">"
                + "<property name=\"_color\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"{1.0, 0.0, 0.0, 1.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "<configure>"
                + "<svg>"
                + "<text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\"/>"
                + "</svg>"
                + "</configure>"
                + "</property>"
                + "<property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.VisibleParameterEditorFactory\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{250.0, 170.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"DBModelName\" class=\"ptolemy.data.expr.StringParameter\" value=\"modeltest\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[300.0, 95.0]\">"
                + "</property>"
                + "<port name=\"port\" class=\"ptolemy.actor.TypedIOPort\">"
                + "<property name=\"input\"/>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[25.0, 150.0]\">"
                + "</property>"
                + "</port>"
                + "<port name=\"port2\" class=\"ptolemy.actor.TypedIOPort\">"
                + "<property name=\"output\"/>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{580.0, 200.0}\">"
                + "</property>"
                + "</port>"
                + "<entity name=\"Const2\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{125, 200}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"AddSubtract\" class=\"ptolemy.actor.lib.AddSubtract\">"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[275.0, 125.0]\">"
                + "</property>"
                + "</entity>"
                + "<relation name=\"relation2\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation3\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<link port=\"port\" relation=\"relation\"/>"
                + "<link port=\"port2\" relation=\"relation3\"/>"
                + "<link port=\"Const2.output\" relation=\"relation2\"/>"
                + "<link port=\"AddSubtract.plus\" relation=\"relation2\"/>"
                + "<link port=\"AddSubtract.plus\" relation=\"relation\"/>"
                + "<link port=\"AddSubtract.output\" relation=\"relation3\"/>"
                + "</entity>"
                + "<relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation2\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<link port=\"Const.output\" relation=\"relation\"/>"
                + "<link port=\"Display.input\" relation=\"relation2\"/>"
                + "<link port=\"modeltest.port\" relation=\"relation\"/>"
                + "<link port=\"modeltest.port2\" relation=\"relation2\"/>"
                + "</entity>");

        SaveModelManager saveModelManager = new SaveModelManager();

        boolean exceptionThrown = false;

        String modelId = "";

        try {

            modelId = saveModelManager.save(dbModel);

        } catch (DBExecutionException e) {

            exceptionThrown = true;

        }

        assertTrue(exceptionThrown);
        assertTrue(modelId.length() == 0);

    }

    /**
     * Test saving a model as a new model with a name while another model with the same
     * name already exists in the database. The model does not have references.
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testSaveNewModelAreadyExists_NoReferences() throws Exception {

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
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

        dbModel.setIsNew(true);

        String newModelId = "";

        boolean exceptionThrown = false;

        try {
            newModelId = saveModelManager.save(dbModel);

        } catch (ModelAlreadyExistException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        assertTrue(newModelId.length() == 0);
    }

    /**
     * Test saving a model as a new model in the database while another model with
     * the same name already exist in the database. The model contains references in it.
     *
     * @exception Exception Thrown if the test fails and the exception was not handled.
     */
    @Test
    public void testSaveNewModelAlreadyExists_WithReferences() throws Exception {

        java.util.Date time = new java.util.Date();

        XMLDBModel dbModel = new XMLDBModel(String.valueOf(time.getTime()));
        dbModel.setIsNew(true);
        dbModel.setModel("<?xml version=\"1.0\" standalone=\"no\"?>"
                + "<!DOCTYPE entity PUBLIC \"-//UC Berkeley//DTD MoML 1//EN\" \"http://ptolemy.eecs.berkeley.edu/xml/dtd/MoML_1.dtd\">"
                + "<entity name=\""
                + dbModel.getModelName()
                + "\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"FALSE\">"
                + "</property>"
                + "<property name=\"author\" class=\"ptolemy.data.expr.StringParameter\" value=\"test_ref\">"
                + "<property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">"
                + "</property>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.ValueIcon\">"
                + "<property name=\"_color\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"{1.0, 0.0, 0.0, 1.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "<configure>"
                + "<svg>"
                + "<text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\"/>"
                + "</svg>"
                + "</configure>"
                + "</property>"
                + "<property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.VisibleParameterEditorFactory\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{250.0, 170.0}\">"
                + "</property>"
                + "</property>"
                + "<entity name=\"Const\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{110, 85}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"Display\" class=\"ptolemy.actor.lib.gui.Display\">"
                + "<property name=\"_windowProperties\" class=\"ptolemy.actor.gui.WindowPropertiesAttribute\">"
                + "</property>"
                + "<property name=\"_paneSize\" class=\"ptolemy.actor.gui.SizeAttribute\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{500, 110}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"modeltest\" class=\"ptolemy.actor.TypedCompositeActor\">"
                + "<property name=\""
                + XMLDBModel.DB_MODEL_ID_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"modeltest_1278356855859\">"
                + "</property>"
                + "<property name=\"_createdBy\" class=\"ptolemy.kernel.attributes.VersionAttribute\" value=\"8.1.devel\">"
                + "</property>"
                + "<property name=\""
                + XMLDBModel.DB_REFERENCE_ATTR
                + "\" class=\"ptolemy.data.expr.StringConstantParameter\" value=\"TRUE\">"
                + "</property>"
                + "<property name=\"author\" class=\"ptolemy.data.expr.StringParameter\" value=\"test\">"
                + "<property name=\"_hideName\" class=\"ptolemy.kernel.util.SingletonAttribute\">"
                + "</property>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.ValueIcon\">"
                + "<property name=\"_color\" class=\"ptolemy.actor.gui.ColorAttribute\" value=\"{1.0, 0.0, 0.0, 1.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"\" class=\"ptolemy.kernel.util.SingletonConfigurableAttribute\">"
                + "<configure>"
                + "<svg>"
                + "<text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\"/>"
                + "</svg>"
                + "</configure>"
                + "</property>"
                + "<property name=\"_editorFactory\" class=\"ptolemy.vergil.toolbox.VisibleParameterEditorFactory\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{250.0, 170.0}\">"
                + "</property>"
                + "</property>"
                + "<property name=\"DBModelName\" class=\"ptolemy.data.expr.StringParameter\" value=\"modeltest\">"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[300.0, 95.0]\">"
                + "</property>"
                + "<port name=\"port\" class=\"ptolemy.actor.TypedIOPort\">"
                + "<property name=\"input\"/>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[25.0, 150.0]\">"
                + "</property>"
                + "</port>"
                + "<port name=\"port2\" class=\"ptolemy.actor.TypedIOPort\">"
                + "<property name=\"output\"/>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{580.0, 200.0}\">"
                + "</property>"
                + "</port>"
                + "<entity name=\"Const2\" class=\"ptolemy.actor.lib.Const\">"
                + "<doc>Create a constant sequence.</doc>"
                + "<property name=\"_icon\" class=\"ptolemy.vergil.icon.BoxedValueIcon\">"
                + "<property name=\"attributeName\" class=\"ptolemy.kernel.util.StringAttribute\" value=\"value\">"
                + "</property>"
                + "<property name=\"displayWidth\" class=\"ptolemy.data.expr.Parameter\" value=\"60\">"
                + "</property>"
                + "</property>"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"{125, 200}\">"
                + "</property>"
                + "</entity>"
                + "<entity name=\"AddSubtract\" class=\"ptolemy.actor.lib.AddSubtract\">"
                + "<property name=\"_location\" class=\"ptolemy.kernel.util.Location\" value=\"[275.0, 125.0]\">"
                + "</property>"
                + "</entity>"
                + "<relation name=\"relation2\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation3\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<link port=\"port\" relation=\"relation\"/>"
                + "<link port=\"port2\" relation=\"relation3\"/>"
                + "<link port=\"Const2.output\" relation=\"relation2\"/>"
                + "<link port=\"AddSubtract.plus\" relation=\"relation2\"/>"
                + "<link port=\"AddSubtract.plus\" relation=\"relation\"/>"
                + "<link port=\"AddSubtract.output\" relation=\"relation3\"/>"
                + "</entity>"
                + "<relation name=\"relation\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<relation name=\"relation2\" class=\"ptolemy.actor.TypedIORelation\">"
                + "</relation>"
                + "<link port=\"Const.output\" relation=\"relation\"/>"
                + "<link port=\"Display.input\" relation=\"relation2\"/>"
                + "<link port=\"modeltest.port\" relation=\"relation\"/>"
                + "<link port=\"modeltest.port2\" relation=\"relation2\"/>"
                + "</entity>");

        SaveModelManager saveModelManager = new SaveModelManager();

        saveModelManager.save(dbModel);

        dbModel.setIsNew(true);

        String newModelId = "";

        boolean exceptionThrown = false;

        try {
            newModelId = saveModelManager.save(dbModel);

        } catch (ModelAlreadyExistException e) {
            exceptionThrown = true;
        }

        assertTrue(exceptionThrown);
        assertTrue(newModelId.length() == 0);

    }
}
