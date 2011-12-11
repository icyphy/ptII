/* Interface for parameters that provide web export content.

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

package ptolemy.vergil.basic.export.html;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ConfigurableAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.icon.ValueIcon;
import ptolemy.vergil.toolbox.VisibleParameterEditorFactory;


///////////////////////////////////////////////////////////////////
//// CopyJavaScriptFiles
/**
 * A parameter which, when inserted into a model, causes the
 * export to web to produce a standalone web page that makes
 * no reference to the ptolemy.org website. This is accomplished
 * by copying all required JavaScript and image files into
 * the target directory for the exported web page.
 * If the property usePtWebsite is true, then the copy
 * is not performed since the webpage will reference the files
 * at http://ptolemy.org/ in any case.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class CopyJavaScriptFiles extends Attribute implements WebExportable {

    /** Create an instance of this parameter.
     *  @param container The container.
     *  @param name The name.
     *  @throws IllegalActionException If the superclass throws it.
     *  @throws NameDuplicationException If the superclass throws it.
     */
    public CopyJavaScriptFiles(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
                
        // Add parameters that ensure this is rendered correctly in Vergil.
        new SingletonAttribute(this, "_hideName");
        new ValueIcon(this, "_icon");
        ConfigurableAttribute smallIcon = new ConfigurableAttribute(this, "_smallIconDescription");
        try {
            smallIcon.configure(null, null,
                    "<svg><text x=\"20\" style=\"font-size:14; font-family:SansSerif; fill:blue\" y=\"20\">copy</text></svg>");
        } catch (Exception e) {
            // Show exception on the console. Should not occur.
            e.printStackTrace();
        }
        new VisibleParameterEditorFactory(this, "_editorFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Copy the required files.
     *  @throws IllegalActionException If a subclass throws it.
     */
    public void provideContent(WebExporter exporter) throws IllegalActionException {
        boolean usePtWebsite = Boolean.valueOf(StringUtilities.getProperty("ptolemy.ptII.exportHTML.usePtWebsite"));
        if (usePtWebsite == true) {
            // Copy is not necessary.
            return;
        }
        // Copy Javascript source files into destination directory,
        // if they are available. The files are under an MIT license,
        // which is compatible with the Ptolemy license.
        // For jquery, we could use a CDS (content delivery service) instead
        // of copying the file.
        String jsDirectoryName = "$CLASSPATH/ptolemy/vergil/basic/export/html/javascript/";
        File jsDirectory = FileUtilities.nameToFile(jsDirectoryName, null);
        // We assume that if the directory exists, then the files exist.
        if (jsDirectory.isDirectory()) {
            // Copy files into the "javascript" directory.
            File directory = exporter.getExportDirectory();
            File jsTargetDirectory = new File(directory, "javascript");
            if (jsTargetDirectory.exists() && !jsTargetDirectory.isDirectory()) {
                File jsBackupDirectory = new File(directory, "javascript.bak");
                if (!jsTargetDirectory.renameTo(jsBackupDirectory)) {
                    // It is ok to ignore this.
                    System.out.println("Failed to rename \"" + jsTargetDirectory 
                            + "\" to \"" + jsBackupDirectory + "\"");
                }
            }
            if (!jsTargetDirectory.exists() && !jsTargetDirectory.mkdir()) {
                MessageHandler
                .message("Warning: Cannot find required JavaScript, CSS, and image files"
                        + " for lightbox effect implemented by the fancybox"
                        + " package. Perhaps your Ptolemy II"
                        + " installation does not include them.");
            } else {
                // Copy css, JavaScript, and image files.
                for (String filename : ExportHTMLAction.FILENAMES) {
                    try {
                        URL lightboxFile = FileUtilities
                                .nameToURL(
                                        jsDirectoryName + filename,
                                        null, null);
                        FileUtilities.binaryCopyURLToFile(lightboxFile, new File(
                                jsTargetDirectory, filename));
                    } catch (IOException e) {
                        throw new IllegalActionException(this, e, "Failed to copy required files for standalone web export.");
                    }
                }
            }
        }
    }
    
    /** There is no outside content, so do nothing.
     *  @throws IllegalActionException If a subclass throws it.
     */
    public void provideOutsideContent(WebExporter exporter) throws IllegalActionException {
    }
}
