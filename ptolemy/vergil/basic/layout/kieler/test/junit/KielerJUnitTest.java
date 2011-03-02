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

import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.moml.filter.RemoveGraphicalClasses;
import ptolemy.moml.MoMLParser;
import ptolemy.actor.TypedCompositeActor;
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
    public void layoutTest(String modelFileName) throws Exception {
        NamedObj model = null;
        try {
            // Read in the model, using moml.filter.RemoveGraphicalClasses.
            List filters = new LinkedList();
            filters.add(new RemoveGraphicalClasses());
            MoMLParser.setMoMLFilters(filters);

            ConfigurationApplication application = 
                new ConfigurationApplication( new String[] {
                            // Need to display a frame or Kieler fails.
                            //"ptolemy/actor/gui/test/testConfiguration.xml",
                            "ptolemy/configs/full/configuration.xml", 
                            modelFileName});
            
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
                    break;
                }
            }
            if ( !(model instanceof TypedCompositeActor)) {
                throw new Exception("Failed to find a TypedComposite.  Models were: " 
                        + names);
            }

            // Invoke the Kieler layout mechanism.
            new KielerLayoutAction().doAction(model);
            
            // Export the model and compare it with the original.
            String laidOutMoML = model.exportMoML();
            byte [] originalMoMLBytes = FileUtilities.binaryReadURLToByteArray(new File(modelFileName).toURI().toURL());

            System.out.println("Difference between " + modelFileName
                    + " and the exported MoML after Kieler Layout:");
            System.out.println(Diff.diff(new String(originalMoMLBytes),
                            laidOutMoML));

            //System.out.println(new String(laidOutMoML));
            //System.out.println(new String(originalMoML));

            assertArrayEquals(laidOutMoML.getBytes(), originalMoMLBytes);

            // Invoke the crufty Ptolemy layout mechanism and export.
            // Invoke the Kieler layout mechanism and export.
            // Invoke undo and compare against the Ptolemy layout.
            // Invoke redo and compare against the Kieler layout.

            // Clean up temporary files.
        } finally {
            if (model != null) {
                if (model instanceof TypedCompositeActor) {
                    ((TypedCompositeActor)model).setContainer(null);
                }
            }
        }        
    }

    /** 
     *  
     */ 
    @org.junit.Test 
    public void run() throws Exception {
        layoutTest("ConstDisplay.xml");
    }

    public static void main(String args[]) {
        org.junit.runner.JUnitCore.main("ptolemy.vergil.basic.layout.kieler.test.junit.KielerJUnitTest");
    }

    private static String _eol = System.getProperty("line.separator");
}