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

import javax.swing.SwingUtilities;

import ptolemy.actor.Manager;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.PtolemyEffigy;
import ptolemy.kernel.util.BasicModelErrorHandler;

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
     *  @param save True if the model should be saved after being run.
     *  @exception Exception Thrown if there is a problem reading the model
     *  or exporting the image.
     */
    public void exportImage(final String formatName, final String modelFileName,
            final boolean run, final boolean save)
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
                    ex.printStackTrace();
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
                           System.out.println("Running " + model[0].getFullName());
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

       if (save) {
           // Optionally save the model.
           // Sadly, running the DOPCenter.xml model does not seem to update the
           // graph.  So, we run it and save it and then open it again.
           Runnable saveAction = new Runnable() {
                   public void run() {
                       try {
                           System.out.println("Saving " + model[0].getFullName());
                           ((PtolemyEffigy)(_basicGraphFrame.getTableau().getContainer())).writeFile(new File(modelFileName));
                       } catch (Exception ex) {
                           ex.printStackTrace();
                           throw new RuntimeException(ex);
                       }
                   }
               };
           SwingUtilities.invokeAndWait(saveAction);
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
                           System.out.println("Exported " + imageFile.getCanonicalPath());
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
                    ex.printStackTrace();
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
                    + "[-run] [-save] [GIF|gif|PNG|png] model.xml";
        if (args.length == 0 || args.length > 4) {
            // FIXME: we should get the list of acceptable format names from
            // BasicGraphFrame
            System.err.println("Wrong number of arguments");
            System.err.println(usage);
            System.exit(3);
        }
        String formatName = "GIF";
        boolean run = false;
        boolean save = false;
        String modelFileName = null;
        if (args.length == 1) {
            modelFileName = args[0];
        } else {
            // FIXME: this is a lame way to process arguments.
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals("-run")) {
                    run = true;
                } else if (args[i].equals("-save")) {
                    save = true;
                } else if (args[i].toUpperCase().equals("GIF")
                        || args[i].toUpperCase().equals("PNG")) {
                    formatName = args[i].toUpperCase();
                } else {
                    modelFileName = args[i];
                }
            }
        }                        
        try {
            new ExportImage().exportImage(formatName, modelFileName, run, save);
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
