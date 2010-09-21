/* Implement the Export PDF menu choice using iText.

 Copyright (c) 2010 The Regents of the University of California.
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
 COPYRIGHTENDKEY 2
 */

package ptolemy.vergil.basic.itextpdf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import ptolemy.gui.JFileChooserBugFix;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;
import ptolemy.vergil.basic.BasicGraphFrame;

import com.itextpdf.text.Document;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;

import diva.gui.GUIUtilities;

///////////////////////////////////////////////////////////////////
//// ExportPDFAction

/**
 An Action to Export PDF using iText PDF.

 <p>This implementation uses <a href="http://itextpdf.com/#in_browser">iText PDF</a>,
 which is under the
 <a href="http://itextpdf.com/terms-of-use/index.php">Affero General Public License (AGPL)</a>.
 Thus, use of this package is optional.  To add the "Export PDF" menu
 choice to the GraphEditor, add the following to the configuration
 <pre>
  &lt;property name="_exportPDFClassName"
            class="ptolemy.data.expr.StringParameter"
            value="ptolemy.vergil.basic.itextpdf.ExportPDFAction"/&gt;
 </pre>
 {@link ptolemy.vergil.basic.BasicGraphFrame} checks for this parameter
 and adds the "Export PDF" menu choice if the class named by that parameter exists.

 <p>The <code>$PTII/ptolemy/configs/defaultFullConfiguration.xml</code> file
 already has this parameter.  The ptiny configuration does <b>not</b> have
 this parameter so that we have a smaller download and have a non-GPL executable.


 @author  Edward A. Lee, Christopher Brooks
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class ExportPDFAction extends AbstractAction {
    /** Create a new action to export PDF.
     *  @param frame The Frame which to which this action is added.
     */
    public ExportPDFAction(BasicGraphFrame frame) {
        super("Export PDF");
        _frame = frame;
        putValue("tooltip", "Export PDF to a file.");
        putValue(GUIUtilities.MNEMONIC_KEY, Integer.valueOf(KeyEvent.VK_X));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ppublic methods                   ////

    /** Export PDF. */
    public void actionPerformed(ActionEvent e) {
        _exportPDF();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Export PDF to a file.
     *  This uses the iText library at http://itextpdf.com/.
     */
    private void _exportPDF() {
        Dimension size = _frame.getJGraph().getSize();
        Rectangle pageSize = null;
        try {
            pageSize = new Rectangle(size.width, size.height);
        } catch (Throwable ex) {
            // This exception will occur if the iText library is not installed.
            MessageHandler
                    .error(
                            "iText library is not installed. See http://itextpdf.com/.",
                            ex);
            return;
        }
        Document document = new Document(pageSize);
        JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
        Color background = null;
        try {
            background = jFileChooserBugFix.saveBackground();

            JFileChooser fileDialog = _saveAsFileDialog();
            fileDialog.setDialogTitle("Specify a file to write to.");
            LinkedList extensions = new LinkedList();
            extensions.add("pdf");
            extensions.add("PDF");
            fileDialog.addChoosableFileFilter(new ExtensionFileFilter(
                    extensions));

            // FIXME: _directory is protected in BasicGraphFrame
            //if (_directory != null) {
            //    fileDialog.setCurrentDirectory(_directory);
            //} else {
            // The default on Windows is to open at user.home, which is
            // typically an absurd directory inside the O/S installation.
            // So we use the current directory instead.
            // This will throw a security exception in an applet.
            // FIXME: we should support users under applets opening files
            // on the server.
            String currentWorkingDirectory = StringUtilities
                    .getProperty("user.dir");
            if (currentWorkingDirectory != null) {
                fileDialog
                        .setCurrentDirectory(new File(currentWorkingDirectory));
            }
            //}

            int returnVal = fileDialog.showSaveDialog(_frame);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                // FIXME: _directory is protected in BasicGraphFrame
                //_directory = fileDialog.getCurrentDirectory();
                File file = fileDialog.getSelectedFile().getCanonicalFile();

                if (file.getName().indexOf(".") == -1) {
                    // If the user has not given the file an extension, add it
                    file = new File(file.getAbsolutePath() + ".pdf");
                }

                PdfWriter writer = PdfWriter.getInstance(document,
                        new FileOutputStream(file));
                // To ensure Latex compatibility, use earlier PDF version.
                writer.setPdfVersion(PdfWriter.VERSION_1_3);
                document.open();
                PdfContentByte contentByte = writer.getDirectContent();

                PdfTemplate template = contentByte.createTemplate(size.width,
                        size.height);
                Graphics2D graphics = template.createGraphics(size.width,
                        size.height);
                template.setWidth(size.width);
                template.setHeight(size.height);

                Paper paper = new Paper();
                paper.setSize(size.width, size.height);
                paper.setImageableArea(0.0, 0.0, size.width, size.height);
                PageFormat format = new PageFormat();
                format.setPaper(paper);
                _frame.print(graphics, format, 0);
                graphics.dispose();
                contentByte.addTemplate(template, 0, 0);

                // Open the PDF file.
                // FIXME: _read is protected in BasicGraphFrame
                //_read(file.toURI().toURL());
            }
        } catch (Exception e) {
            MessageHandler.error("Export to PDF failed", e);
        } finally {
            jFileChooserBugFix.restoreBackground(background);
        }
        document.close();
    }

    private JFileChooser _saveAsFileDialog() {
        // FIXME: copied from Top.
        JFileChooser fileDialog = new JFileChooser();

        if (_fileFilter != null) {
            fileDialog.addChoosableFileFilter(_fileFilter);
        }

        fileDialog.setDialogTitle("Save as...");
        // FIXME:  _getCurrentDirectory is in Top
        //fileDialog.setCurrentDirectory(_getCurrentDirectory());
        return fileDialog;
    }

    ///////////////////////////////////////////////////////////////////
    ////                    private variables

    BasicGraphFrame _frame;

    /** The FileFilter that determines what files are displayed by
     *  the Open dialog and the Save As dialog
     *  The initial default is null, which causes no FileFilter to be
     *  applied, which results in all files being displayed.
     *  // FIXME: copied from Top.
     */
    protected FileFilter _fileFilter = null;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** File filter that filters out files that do not have one of a
     *  pre-specified list of extensions.
     */
    protected static class ExtensionFileFilter extends FileFilter {

        // FIXME: copied from ptolemy.actor.gui.TableauFrame

        // NetBeans wants this protected.  If it is package visibility,
        // then there are problems accessing it from the same package
        // but a different jar.

        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        /** Construct a file filter that filters out all files that do
         *  not have one of the extensions in the given list.
         *  @param extensions A list of extensions, each of which is
         *   a String.
         */
        public ExtensionFileFilter(List extensions) {
            _extensions = extensions;
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Accept only files with one of the extensions given in the
         *  constructor.
         *  @param fileOrDirectory The file to be checked.
         *  @return True if the file is a directory or has one of the
         *   specified extensions.
         */
        public boolean accept(File fileOrDirectory) {
            if (fileOrDirectory.isDirectory()) {
                return true;
            }

            String fileOrDirectoryName = fileOrDirectory.getName();
            int dotIndex = fileOrDirectoryName.lastIndexOf('.');

            if (dotIndex == -1) {
                return false;
            }

            String extension = fileOrDirectoryName.substring(dotIndex + 1);

            if (extension != null) {
                Iterator extensions = _extensions.iterator();

                while (extensions.hasNext()) {
                    String matchExtension = (String) extensions.next();

                    if (extension.equalsIgnoreCase(matchExtension)) {
                        return true;
                    }
                }
            }

            return false;
        }

        /**  The description of this filter. */
        public String getDescription() {
            StringBuffer result = new StringBuffer();
            Iterator extensions = _extensions.iterator();
            int extensionNumber = 1;
            int size = _extensions.size();

            while (extensions.hasNext()) {
                String extension = (String) extensions.next();
                result.append(".");
                result.append(extension);

                if (extensionNumber < (size - 1)) {
                    result.append(", ");
                } else if (extensionNumber < size) {
                    result.append(" and ");
                }

                extensionNumber++;
            }

            result.append(" files");
            return result.toString();
        }

        ///////////////////////////////////////////////////////////////
        ////                     private variables                 ////
        // The list of acceptable file extensions.
        private List _extensions;
    }

}
