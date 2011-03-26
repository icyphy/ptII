/* Export a model as an image.

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

package ptolemy.vergil.basic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.Effigy;
import ptolemy.actor.gui.Tableau;
import ptolemy.actor.lib.image.ImageReader;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.BasicModelErrorHandler;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.util.FileUtilities;
import ptolemy.util.test.Diff;
import ptolemy.vergil.basic.BasicGraphFrame;
import ptolemy.vergil.basic.PtolemyLayoutAction;
import ptolemy.vergil.basic.layout.KielerLayoutAction;

///////////////////////////////////////////////////////////////////
//// ExportImage
/** 
 * Export a model as a image.
 *
 * The default is to export a .gif file with the same name as the model.
 * 
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ExportImage {
    /** Export an image of a model to a file.
     *  The image is written to a file with the same name as the model.
     *
     *  @param formatName The file format of the file to be generated.
     *  One of "GIF", "gif", "PNG", "png".
     *  @param modelFileName A Ptolemy model in MoML format.
     *  The string may start with $CLASSPATH, $HOME or other formats
     *  suitable for {@link ptolemy.util.FileUtilities.nameToFile(String, URI)}.
     *  @param run True if the model should be run first.
     *  @exception Exception Thrown if there is a problem reading the model
     *  or exporting the image.
     */
    public void exportImage(final String formatName, final String modelFileName, final boolean run)
            throws Exception {
       // FIXME: this seem wrong:  The inner classes are in different
        // threads and can only access final variables.  However, we
        // use an array as a final variable, but we change the value
        // of the element of the array.  Is this thread safe?
        // Perhaps we should make this a field?
        final TypedCompositeActor[] model = new TypedCompositeActor[1];

        /////
        // Open the model.
        // FIXME: Refactor this and KielerLayoutJUnitTest to a common class.
        Runnable openModelAction = new Runnable() {
            public void run() {
                try {
                    model[0] = ConfigurationApplication.openModel(modelFileName);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        };
        SwingUtilities.invokeAndWait(openModelAction);
        _sleep();

       _basicGraphFrame = BasicGraphFrame.getBasicGraphFrame(model[0]);


       if (run) {
           // Optionally run the model.
           Runnable runAction = new Runnable() {
                   public void run() {
                       try {
                           Manager manager = model[0].getManager();
                           if (manager == null) {
                               manager = new Manager(model[0].workspace(), "MyManager");
                               ((TypedCompositeActor)model[0]).setManager(manager);
                           }
                           ((TypedCompositeActor)model[0]).setModelErrorHandler(new BasicModelErrorHandler());
                           manager.execute();
                       } catch (Exception ex) {
                           ex.printStackTrace();
                           throw new RuntimeException(ex);
                       }
                   }
               };
           SwingUtilities.invokeAndWait(runAction);
           _sleep();
       }

       // Export images
       Runnable exportImageAction = new Runnable() {
               public void run() {
                   try {
                       File imageFile = new File(model[0].getName()
                               + "." + formatName.toLowerCase());
                       OutputStream out = null;
                       try {
                           out = new FileOutputStream(imageFile);
                           // Export the image.
                           _basicGraphFrame.getJGraph().exportImage(out, formatName);
                       } finally {
                           if (out != null) {
                               try {
                                   out.close();
                               } catch (IOException ex) {
                                   ex.printStackTrace();
                               }
                           }
                       }
                   } catch (Exception ex) {
                       ex.printStackTrace();
                       throw new RuntimeException(ex);
                   }
               }
           };
       SwingUtilities.invokeAndWait(exportImageAction);
       _sleep();

        /////
        // Close the model.
        Runnable closeAction = new Runnable() {
            public void run() {
                try {
                    ConfigurationApplication.closeModelWithoutSavingOrExiting(model[0]);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            };
        SwingUtilities.invokeAndWait(closeAction);
        _sleep();
    }

    /** Export a model as an image.
     *  
     *  <p>Note that the a graphical display must be present, this
     *  code displays the model and executes.  To use in a headless
     *  environment under Linux, install Xvfb.</p>
     *
     *  <p>Usage:
     *  <br/> To save a gif:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.ExportImage model.xml
     *  </pre>
     *  <p>or, to save a png:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.ExportImage png model.xml
     *  </pre>
     *  <p>or, to run the model and then save a png:</p>
     *  <pre>
     *   java -classpath $PTII ptolemy.vergil.basic.ExportImage -run png model.xml
     *  </pre>
     * 
     *  @exception args If there is 1 argument, then it names a 
     *  Ptolemy MoML file and the model is exported as a .gif file.
     *  If there are two arguments, then the first argument names
     *  a format, current formats are GIF, gif, PNG and png and
     *  the second argument names a Ptolemy MoML file.
     */
    public static void main(String args[]) {
        String usage ="Usage: java -classpath $PTII "
                    + "ptolemy.vergil.basic.ExportImage "
                    + "[-run] [GIF|gif|PNG|png] model.xml";
        if (args.length == 0 || args.length > 3) {
            // FIXME: we should get the list of acceptable format names from
            // BasicGraphFrame
            System.err.println("Wrong number of arguments");
            System.err.println(usage);
            System.exit(3);
        }
        String formatName = "GIF";
        boolean run = false;
        String modelFileName = null;
        if (args.length == 1) {
            modelFileName = args[0];
        } else {
            if (args.length == 2) {
                if (args[0].equals("-run")) {
                    run = true;
                } else {
                    formatName = args[0].toUpperCase();
                }
                modelFileName = args[1];
            } else {
                if (args[0].equals("-run")) {
                    run = true;
                    formatName = args[1].toUpperCase();
                } else {
                    formatName = args[0].toUpperCase();
                    if (args[1].equals("-run")) {
                        run = true;
                    } else {
                        System.err.println("Don't understand "
                                + args[0] + " "
                                + args[1] + " "
                                + args[2] + " ");
                        System.err.println(usage);
                        System.exit(4);
                    }
                }
                modelFileName = args[2];
            }
        }
        try {
            new ExportImage().exportImage(formatName, modelFileName, run);
        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(5);
        }
    }
 
    /** Sleep the current thread, which is usually not the Swing Event
     *  Dispatch Thread.
     */
    protected static void _sleep() {
        try {
            Thread.sleep(1000);
        } catch (Throwable ex) {
            //Ignore
        }
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    /** The BasicGraphFrame of the model. */
    private BasicGraphFrame _basicGraphFrame;
}
