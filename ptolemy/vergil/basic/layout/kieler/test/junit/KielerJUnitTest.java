/* JUnit test the Kieler Layout mechanism

 Copyright (c) 2011 The Regents of the University of California.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

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

package ptolemy.vergil.basic.layout.kieler.test.junit;

import static org.junit.Assert.assertArrayEquals;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;
import javax.swing.SwingUtilities;

import ptolemy.actor.Manager;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.Effigy;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.moml.MoMLParser;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.undo.RedoChangeRequest;
import ptolemy.kernel.undo.UndoChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.FileUtilities;
import ptolemy.util.test.Diff;
import ptolemy.vergil.basic.layout.KielerLayoutAction;



///////////////////////////////////////////////////////////////////
//// KielerJUnitTest
/** 
 * Test out Kieler by open models, stripping the graphical elements,
 * using Kieler to layout the graph and then doing undo and redo.
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */ 
public class KielerJUnitTest {


    /**
     * Test the layout facility by reading in a model, stripping
     * out the graphical elements, laying out the model, comparing
     * the new results with the known good results and then doing
     * undo and redo.
     * @param modelFileName The file name of the test model. 
     * @exception Exception If the file name cannot be read or laid out.
     */
    public void layoutTest(final String modelFileName) throws Exception {

        // FIXME: this seem wrong:  The inner classes are in different
        // threads and can only access final variables.  However, we
        // use an array as a final variable, but we change the value
        // of the element of the array.  Is this thread safe?
        final NamedObj[] model = new NamedObj[1];


        // Open the model.
        Runnable openModelAction = new Runnable() {
                public void run() {
                    try {
                        model[0] = openModel(modelFileName);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
        SwingUtilities.invokeAndWait(openModelAction);
        String nongraphicalMoML = model[0].exportMoML();

        sleep();

        // Layout the model.
        Runnable layoutModelAction = new Runnable() {
                public void run() {
                    try {
                        layoutModel(model[0], modelFileName);
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
        SwingUtilities.invokeAndWait(layoutModelAction);

        // Loop through undo and redo
        String laidOutMoML = model[0].exportMoML();
        for(int i = 1; i <= 2; i ++) {
            Runnable undoAction = new Runnable() {
                    public void run() {
                        try {
                            undo(model[0]);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };
            SwingUtilities.invokeAndWait(undoAction);

            sleep();

            String undoMoML = model[0].exportMoML();
            if (_debug || !nongraphicalMoML.equals(undoMoML)) {
                System.out.println("Difference between nonGraphical MoML"
                        + " and the exported MoML after Kieler Layout and then undo:");
                System.out.println(Diff.diff(nongraphicalMoML,
                                undoMoML));
            }

            assertArrayEquals(nongraphicalMoML.getBytes(),
                    undoMoML.getBytes());


            Runnable redoAction = new Runnable() {
                    public void run() {
                        try {
                            redo(model[0]);
                        } catch (Exception ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                };
            SwingUtilities.invokeAndWait(redoAction);
            String redoMoML = model[0].exportMoML();
            if (_debug || !laidOutMoML.equals(redoMoML)) {
                System.out.println("Difference between laid out MoML"
                        + " and the exported MoML after Kieler Layout and then undo, then redo:");
                System.out.println(Diff.diff(laidOutMoML, redoMoML));
            }

            assertArrayEquals(laidOutMoML.getBytes(),
                    redoMoML.getBytes());

            sleep();
        }

        // Close the model.
        Runnable closeAction = new Runnable() {
                public void run() {
                    try {
                        if (_debug) {
                            System.out.println("About to close " + model[0].getName());
                        }
                        Effigy effigy = Configuration.findEffigy(model[0].toplevel());
                        // Avoid being prompted for save.
                        effigy.setModified(false);
                        //
                        System.setProperty("ptolemy.ptII.doNotExit", "true");
                        effigy.closeTableaux();
                        ((TypedCompositeActor)model[0]).setContainer(null);
                        MoMLParser.purgeAllModelRecords();
                        //Configuration.closeAllTableaux();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            };
        SwingUtilities.invokeAndWait(closeAction);
        sleep();
    }
        
    public NamedObj openModel(String modelFileName) {
        NamedObj model = null;
        try {
            // Read in the model, using moml.filter.RemoveGraphicalClasses.
            List filters = new LinkedList();
            filters.add(new RemoveGraphicalClasses());
            MoMLParser.setMoMLFilters(filters);
            System.out.print(modelFileName + " ");
            // Conver the file name to a canonical file name so that
            // this test may be run from any directory or from within Eclipse.
            File canonicalModelFile = FileUtilities.nameToFile(modelFileName, null);
            String canonicalModelFileName = canonicalModelFile.getCanonicalPath();

            // FIXME: are we in the right thread?
            ConfigurationApplication application = 
                new ConfigurationApplication( new String[] {
                            // Need to display a frame or Kieler fails.
                            //"ptolemy/actor/gui/test/testConfiguration.xml",
                            "ptolemy/configs/full/configuration.xml", 
                            canonicalModelFileName});
            

            // Find the first TypedCompositeActor, skipping the
            // Configuration etc.
            StringBuffer names = new StringBuffer();
            Iterator models = application.models().iterator();
            while (models.hasNext()) {
                model = (NamedObj) models.next();
                if (names.length() > 0) {
                    names.append(", ");
                }
                names.append(model.getFullName());
                if (model instanceof TypedCompositeActor) {
                    if (_debug) {
                        System.out.println("openModel(" + modelFileName + ")\n"
                                + model.getName());
                    }
                    break;
                }
            }
            if ( !(model instanceof TypedCompositeActor)) {
                throw new Exception("Failed to find a TypedComposite.  Models were: " 
                        + names);
            }

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return model;
    }

    public void layoutModel(NamedObj model, String modelFileName) throws Exception {
        try {
            String nongraphicalMoML = model.exportMoML();

            // Invoke the Kieler layout mechanism.
            new KielerLayoutAction().doAction(model);
            
            // Export the model and compare it with the original.
            String laidOutMoML = model.exportMoML();
            File canonicalModelFile = FileUtilities.nameToFile(modelFileName, null);
            String canonicalModelFileName = canonicalModelFile.getCanonicalPath();
            byte [] originalMoMLBytes = FileUtilities.binaryReadURLToByteArray(canonicalModelFile.toURI().toURL());

            if (_debug || !new String(originalMoMLBytes).equals(laidOutMoML)) {
                System.out.println("Difference between " + canonicalModelFileName
                        + " and the exported MoML after Kieler Layout:");
                System.out.println(Diff.diff(new String(originalMoMLBytes),
                                laidOutMoML));
            }

            // System.out.println("Original MoML:");
            // System.out.println(new String(originalMoMLBytes));

            // System.out.println("Laid out MoML:");
            // System.out.println(new String(laidOutMoML));

            assertArrayEquals(laidOutMoML.getBytes(), originalMoMLBytes);

            // TODO: Invoke the crufty Ptolemy layout mechanism and export.
            // To do this, we need to either get the controller or refactor
            // the krufty Ptolemy layout mechanism so we have a 
            // KruftyPtolemyLayoutAction. 

            // TODO: Invoke the Kieler layout mechanism again and export.

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /** Sleep the current thread.
     */   
    public void sleep() {
        try {
            Thread.sleep(1000);
        } catch (Throwable ex) {
            //Ignore
        }
    }

    /** Undo the last operation on the model. */
    public void undo(NamedObj model) {
        try {
            Manager manager = new Manager(model.workspace(), "KJUT");
            // Invoke undo and compare against the Ptolemy layout.
            // See ptolemy/moml/test/UndoEntity.tcl
            UndoChangeRequest undo = new UndoChangeRequest(model, model);
            manager.requestChange(undo);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    /** Redo the last operation on the model. */
    public void redo(NamedObj model) {
        try {
            Manager manager = new Manager(model.workspace(), "KJUT");
            // Invoke redo and compare against the Kieler layout.
            RedoChangeRequest redo = new RedoChangeRequest(model, model);
            manager.requestChange(redo);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    
    /** 
     * Test the layout facility by reading in a models, stripping
     * out the graphical elements, laying out the models, comparing
     * the new results with the known good results and then doing
     * undo and redo.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */ 
    @org.junit.Test 
    public void runConstDisplay() throws Exception {
        layoutTest("$CLASSPATH/ptolemy/vergil/basic/layout/kieler/test/junit/models/ConstDisplay.xml");
    }

    /* Test the layout of the ConstConstDisplay model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */ 
    @org.junit.Test 
    public void runConstConstDisplay() throws Exception {
        layoutTest("$CLASSPATH/ptolemy/vergil/basic/layout/kieler/test/junit/models/ConstConstDisplay.xml");
    }

    /* Test the layout of the modulation model.
     * @exception Exception If there is a problem reading or laying
     * out a model.
     */ 
    @org.junit.Test 
    public void runModulation() throws Exception {
        layoutTest("$CLASSPATH/ptolemy/moml/demo/modulation.xml");
    }

    /** Test the Kieler layout facility.
     *  @exception args Not used.
     */
    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main("ptolemy.vergil.basic.layout.kieler.test.junit.KielerJUnitTest");
    }

    // private static String _eol = System.getProperty("line.separator");

    /** Set to true for more messages. */
    private final boolean _debug = false;
}

