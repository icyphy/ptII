/* An attribute that displays documentation.

 Copyright (c) 2003-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_3
 COPYRIGHTENDKEY
 */
package ptolemy.vergil.kernel.attributes;

import java.awt.Color;
import java.awt.Frame;
import java.io.File;
import java.net.URL;

import javax.swing.JFileChooser;

import ptolemy.actor.gui.Configuration;
import ptolemy.actor.gui.ConfigurationApplication;
import ptolemy.actor.gui.EditorFactory;
import ptolemy.actor.gui.TableauFrame;
import ptolemy.data.expr.FileParameter;
import ptolemy.gui.JFileChooserBugFix;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

/**
 <p>This attribute is a visible attribute that displays documentation when
 configured by double clicking on it or by invoking Configure in the context
 menu.
 </p><p>
 The method
 that causes the documentation to be displayed is createEditor, which
 is normally used to configure an attribute. This means that the
 DocumentationAttribute can't be configured. That is, if a
 double-click occurs or the Configure menu item is selected the
 documentation will be displayed, and the normal configure dialog
 will not be offered. Special provisions for "configuring" a
 DocumentationAttribute are described below.
 </p><p>
 The documentation is in a file specified by the FileParameter
 attribute with the name _documentation. The _documentation FileParameter can
 be on any object, including this DocumentationAttribute, in the containment
 hierarchy. (As explained below, the _documentation FileParameter will be
 found on the container of this DocumentationAttribute.) If a
 _documentation FileParameter can not be found a JFileChooser is
 presented. The resulting selection, if there is one, is then used
 to create a _documentation FileParameter on the <i>container</i>.
 There are two reasons for this. First, the documentation most likely
 applies to the container. I.e., this DocumentationAttribute isn't
 being documented, rather, the thing that this DocumentationAttribute
 is an attribute of is being documented. Second, the container most
 likely can be configured in the normal way. Since, the
 _documentation FileParameter will be on the container the
 specification for the file containing the documentation can be
 modified.
 </p>
 <p>Note that if a DocumentationAttribute refers to a pdf file, then it
 may not be possible to view the pdf file in an applet because
 {@link ptolemy.actor.gui.BrowserLauncher} uses reflection to look up
 classes.  The workaround is to use
 ptolemy.vergil.pdfrenderer.PDFRenderer

 @deprecated DocumentationAttribute provides no UI way to edit the URL, one must edit the MoML file by hand. Use ptolemy.vergil.basic.DocAttribute or ptolemy.vergil.pdfrenderer.PDFRenderer instead of using DocumentationAttribute.
 @author Rowland R Johnson
 @version $Id$
 @since Ptolemy II 4.0
 @see ptolemy.vergil.basic.DocAttribute
 @Pt.ProposedRating Red (rowland)
 @Pt.AcceptedRating Red (rowland)
 */
@Deprecated
public class DocumentationAttribute extends VisibleAttribute {
    /** Construct an icon with the attached this attached.
     *  @param container The container.
     *  @param name The name of the factory.
     *  @exception IllegalActionException If the factory is not of an
     *   acceptable attribute for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public DocumentationAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-50\" y=\"-20\" width=\"130\" height=\"40\" "
                + "style=\"fill:yellow\"/>" + "<text x=\"-40\" y=\"-5\" "
                + "style=\"font-size:12; font-family:SansSerif; fill:black\">"
                + "Double click to see\ndocumentation.</text></svg>");
        new DocumentationAttributeFactory(this, "_editorFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////
    private static class DocumentationAttributeFactory extends EditorFactory {
        // FindBugs suggests making this class static so as to decrease
        // the size of instances and avoid dangling references.

        public DocumentationAttributeFactory(NamedObj _container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(_container, name);
        }

        /** Display the documentation if there is any. If not, offer a dialog to
         *  let the user specify where the documentation is located.
         *  @param object The object to configure.
         *  @param parent The parent window, or null if there is none.
         */
        @Override
        public void createEditor(NamedObj object, Frame parent) {
            try {
                FileParameter docAttribute = null;
                if (!(parent instanceof TableauFrame)) {
                    throw new InternalErrorException("Frame \"" + parent
                            + "\" is not a TableauFrame");
                }
                Configuration configuration = ((TableauFrame) parent)
                        .getConfiguration();
                NamedObj documentedObject = object;

                while (documentedObject != null) {
                    docAttribute = (FileParameter) documentedObject
                            .getAttribute("_documentation", FileParameter.class);

                    if (docAttribute != null) {
                        break;
                    }

                    documentedObject = documentedObject.getContainer();
                }

                if (docAttribute != null) {
                    URL doc = ConfigurationApplication.specToURL(docAttribute
                            .getExpression());
                    configuration.openModel(doc, doc, doc.toExternalForm());
                } else {
                    NamedObj container = object.getContainer();

                    if (container == null) {
                        container = object;
                    }

                    // Avoid white boxes in file chooser, see
                    // http://bugzilla.ecoinformatics.org/show_bug.cgi?id=3801
                    JFileChooserBugFix jFileChooserBugFix = new JFileChooserBugFix();
                    Color background = null;
                    try {
                        background = jFileChooserBugFix.saveBackground();
                        JFileChooser fileDialog = new JFileChooser();
                        fileDialog
                        .setDialogTitle("Select a documentation file.");

                        //File _directory = null;

                        String cwd = StringUtilities.getProperty("user.dir");

                        if (cwd != null) {
                            fileDialog.setCurrentDirectory(new File(cwd));
                        }

                        if (fileDialog.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                            // FIXME: why is this ignored?
                            //_directory = fileDialog.getCurrentDirectory();

                            String fileName = fileDialog.getSelectedFile()
                                    .getAbsolutePath();

                            docAttribute = new FileParameter(container,
                                    "_documentation");
                            docAttribute.setExpression(fileName);
                        }
                    } finally {
                        jFileChooserBugFix.restoreBackground(background);
                    }
                }
            } catch (Throwable throwable) {
                throw new InternalErrorException(object, throwable,
                        "Cannot access Documentation");
            }
        }
    }
}
