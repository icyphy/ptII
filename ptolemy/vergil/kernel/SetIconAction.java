/* An action to set the icon of an Entity.

   Copyright (c) 2002-2003 The Regents of the University of California.
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

   @ProposedRating Red (cxh@eecs.berkeley.edu)
   @AcceptedRating Red (cxh@eecs.berkeley.edu)
 */

package ptolemy.vergil.kernel;

import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.toolbox.FigureAction;


//////////////////////////////////////////////////////////////////////////
//// SetIconAction
/**
   An action to set the icon of an Entity.
   An icon can be a gif, svg, or xml file.

   @author  Original Author Unknown, Contributor: Christopher Hylands
   @version $Id$
   @since Ptolemy II 2.1
 */
public class SetIconAction extends FigureAction {
    /**
     */
    public SetIconAction() {
        super("Set Icon");
    }

    /** Determine the target Ptolemy II object and prompt the user
     *  for a new icon.   The icon file extension should be
     *  gif, svg or xml.
     *
     *  @param e The event.
     */
    public void actionPerformed(ActionEvent e) {

        // Figure out what entity.
        super.actionPerformed(e);
        NamedObj object = getTarget();
        String path = null;
        StringAttribute imagePath =
            (StringAttribute)object.getAttribute("_imagePath");
        if (imagePath != null) {
            path = imagePath.getExpression();
        }
        JFileChooser iconChooser = new JFileChooser();
        iconChooser.setDialogTitle("Select an Image");
        ImageFileFilter filter = new ImageFileFilter();
        iconChooser.setFileFilter(filter);
        // If the image has been previously set, set the selected file
        // to the previously set file path.
        if (path != null) {
            iconChooser.setSelectedFile(new File(path));
        } else if (_directory != null) {
            iconChooser.setCurrentDirectory(_directory);
        } else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // FIXME: This will throw a security exception in an applet?
            String cwd = System.getProperty("user.dir");
            if (cwd != null) {
                iconChooser.setCurrentDirectory(new File(cwd));
            }
        }

        if (iconChooser.showOpenDialog(null)
                == JFileChooser.APPROVE_OPTION) {
            try {
                File file = iconChooser.getSelectedFile().getAbsoluteFile();
                // Make sure this file is not bogus.
                if (!file.exists()) {
                    throw new NullPointerException("No such file");
                }

                String extension = getExtension(file);
                StringBuffer moml = new StringBuffer(
                        "<group>\n<property name=\"_imagePath\" "
                        + "class=\"ptolemy.kernel.util.StringAttribute\" "
                        + "value=\"" + file.getCanonicalPath() + "\"/>\n"
                        + "<property name=\"_iconDescription\""
                        + " class=\"ptolemy.kernel.util."
                        + "SingletonConfigurableAttribute\""
                        + ">\n <configure> ");

                if (extension.equalsIgnoreCase("svg")
                        || extension.equalsIgnoreCase("xml")) {
                    // read in the svg from a file
                    BufferedReader reader = new BufferedReader(
                            new FileReader(file));

                    String temp = new String();
                    while ((temp = reader.readLine()) != null) {
                        moml.append(temp);
                    }
                    moml.append(" </configure>\n</property>\n</group> ");
                    ChangeRequest request =
                        new MoMLChangeRequest(object, object,
                                moml.toString(), null);
                    object.requestChange(request);
                }
                else
                    {// insert the gif into svg markup
                        moml.append (
                                "<svg>\n<rect x=\"0\" y=\"0\" width=\"60\" "
                                + "height=\"40\" style=\"fill:white\"></rect>"
                                + "<image x=\"0\" y=\"0\" width=\"60\" "
                                + "height=\"40\" xlink:href=\"file:"
                                + file.getCanonicalPath()
                                + "\"></image>\n</svg>\n"
                                + " </configure>\n</property>\n</group>");

                        ChangeRequest request =
                            new MoMLChangeRequest(object, object,
                                    moml.toString(), null);
                        object.requestChange(request);
                    }
                _directory = iconChooser.getCurrentDirectory();
            } catch (Exception ex) {
                MessageHandler.error("Error reading image file", ex);
            }
        }
    }

    /** Return the extension of a file.  The extension of a file
     *  is the characters after the final period ('.'), if any.
     *  If there file name does not contain a period, then return null.
     *  @param fileOrDirectory The file or directory.
     *  @return the extension.
     */
    public static String getExtension(File fileOrDirectory) {
        String fileOrDirectoryName = fileOrDirectory.getName();
        int dotIndex = fileOrDirectoryName.lastIndexOf('.');
        if (dotIndex == -1) {
            return null;
        }
        return fileOrDirectoryName.substring(dotIndex + 1);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     protected variables                   ////

    protected static File _directory = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////


    /** Display gif, svg, or xml files */
    class ImageFileFilter extends FileFilter {

        /** Accept all directories and all gif, svg, or xml files.
         *  @param file The file to be checked.
         *  @return true if the file is a directory, or if
         *  the extension ends with gif, svg, or xml.
         */
        public boolean accept(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory()) {
                return true;
            }

            String extension = getExtension(fileOrDirectory);
            if (extension != null) {
                if (extension.equalsIgnoreCase("gif")
                        || extension.equalsIgnoreCase("svg")
                        || extension.equalsIgnoreCase("xml")) {
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        /**  The description of this filter */
        public String getDescription() {
            return "Supported Image Formats (gif, svg, xml)";
        }
    }
}
