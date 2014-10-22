/* A representative of a text file contained in an external text editor.

 Copyright (c) 1998-2014 The Regents of the University of California and
 Research in Motion Limited.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA OR RESEARCH IN MOTION
 LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS
 SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA
 OR RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
 SUCH DAMAGE.

 THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
 SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION
 LIMITED HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY

 */
package ptolemy.actor.gui;

import java.io.File;
import java.io.FileWriter;
import java.net.MalformedURLException;
import java.net.URL;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// ExternalTextEffigy

/**
 An external EDITOR-based effigy for a text file (see {@link
 ExternalTextTableau}).

 @author Zoltan Kemenczy, Research in Motion Limited
 @version $Id$
 @since Ptolemy II 2.2
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class ExternalTextEffigy extends TextEffigy {
    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public ExternalTextEffigy(Workspace workspace) {
        super(workspace);
    }

    /** Create a new effigy in the given directory with the given name.
     *  @param container The directory that contains this effigy.
     *  @param name The name of this effigy.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public ExternalTextEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the argument is the <i>identifier</i> parameter, then tell
     *  the external editor to finally open the file specified by the
     *  identifier (as opposed to at newTextEffigy(container, text) time
     *  at which the document file is not yet specified. This greatly
     *  simplifies the interaction with the external text editor: instead
     *  of first telling it to create a text buffer with some name and
     *  no file attached, the buffer, its associated file name, and any
     *  text saved by newTextEffigy(container, text) is given to the
     *  text editor in one transaction. NOTE: This depends on
     *  TextEditorTableau.createTableau(effigy) setting the identifier
     *  expression after newTextEffigy(container, text).
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        // Let Effigy handle it first
        super.attributeChanged(attribute);

        // Now do the external-text-specific stuff
        if (attribute == identifier) {
            URL url;

            try {
                url = new URL(identifier.getExpression());

                File file = new File(url.getFile());
                String path = file.getAbsolutePath().replace('\\', '/');
                showContent(path);
            } catch (MalformedURLException ex) {
                // just ignore. This happens when the tableau sets
                // "Unnamed" arbitrarily
            }
        }
    }

    /** Create a new effigy in the given container containing the specified
     *  text.  The new effigy will have a new instance of
     *  DefaultStyledDocument associated with it.
     *  @param container The container for the effigy.
     *  @param text The text to insert in the effigy.
     *  @return A new instance of TextEffigy.
     *  @exception Exception If the text effigy cannot be
     *   contained by the specified container, or if the specified
     *   text cannot be inserted into the document.
     */
    public static TextEffigy newTextEffigy(CompositeEntity container,
            String text) throws Exception {
        // Create a new effigy.
        ExternalTextEffigy effigy = new ExternalTextEffigy(container,
                container.uniqueName("effigy"));

        // Cheat: we'll get the text off the container at
        // show(Content)-time. This get's around the problem of stale
        // text after the model is updated and answers YES to the
        // question regarding moml viewing in
        // TextEditorTableau.createTableau()...
        effigy.setUseContainerMoML(true);
        return effigy;
    }

    /** Create a new ExternalTextEffigy.
     *  @param container The container for the effigy.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.  This is ignored in this
     *   class.
     *  @param in The input URL, or null if there is none.
     *  @return A new instance of TextEffigy.
     *  @exception Exception If the URL cannot be read, or if the data
     *   is malformed in some way.
     */
    public static TextEffigy newTextEffigy(CompositeEntity container, URL base,
            URL in) throws Exception {
        ExternalTextEffigy effigy = new ExternalTextEffigy(container,
                container.uniqueName("effigy"));

        // A URL has been given.  Read it.
        // Note: Here the text editor would be given the in URL to
        // open. However, to simplify the interaction with the external
        // text editor, the handling of 1) opening existing text
        // URLs, and 2) effigies of moml files where we want the current
        // moml content of an already open PtolemyEffigy (its moml file
        // is not read), the opening of the file is delayed until its
        // identifier attribute is updated.
        return effigy;
    }

    /** Pass the modifiable flag onto the external text editor. */
    @Override
    public void setModifiable(boolean flag) {
        super.setModifiable(flag);
    }

    /** Signal the external text editor to (re)display its buffer
     associated with this effigy. */
    public void show() {
        showContent(_pathName);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    // Set private useContainerMoML attribute
    private void setUseContainerMoML(boolean useContainerMoML) {
        _useContainerMoML = useContainerMoML;
    }

    private void showContent(String path) {
        try {
            File tmpFile = null;
            String todo;

            if (_useContainerMoML) {
                // Open the file from storage, erase the buffer, then set
                // the current content from the MoML content of the
                // container
                String text = ((PtolemyEffigy) getContainer()).getModel()
                        .exportMoML();
                tmpFile = File.createTempFile("effigy", "");

                String tmpFilePathName = tmpFile.getAbsolutePath().replace(
                        '\\', '/');
                FileWriter writer = null;

                try {
                    writer = new FileWriter(tmpFile);
                    writer.write(text);
                } finally {
                    if (writer != null) {
                        writer.close();
                    }
                }

                todo = "gnudoit (find-file (symbol-name '" + path + "))"
                        + "(setq buffer-read-only nil)" + "(erase-buffer)"
                        + "(insert-file-contents " + "    (symbol-name '"
                        + tmpFilePathName + "))"
                        + "(set-buffer-modified-p nil)"
                        + "(setq buffer-read-only t)" + "(buffer-name)";
            } else {
                // Reading file content from storage
                todo = "gnudoit (find-file (symbol-name '" + path + "))"
                        + "(buffer-name)";
            }

            Process process = Runtime.getRuntime().exec(todo);

            // After many simplifcations, at this point, _bufferName is
            // not really needed anymore, but I'll keep its code as a
            // comment of how to read gnudoit results back from emacs
            // (An optimization would first query emacs for a buffer
            // named by _bufferName content and then simply switch to
            // that buffer as opposed to always re-creating it.)
            //- BufferedInputStream result =
            //-    new BufferedInputStream(process.getInputStream());
            process.waitFor();

            //- byte[] buffer = new byte[result.available()];
            //- result.read(buffer, 0, buffer.length);
            // Delete any linefeeds and carriage returns.
            //- int i = buffer.length -1;
            //- for (; buffer[i] == '\r' || buffer[i] == '\n'; i--);
            //- _bufferName = new String(buffer, 0, i + 1);
            _pathName = path;

            if (tmpFile != null) {
                if (!tmpFile.delete()) {
                    throw new InternalErrorException("Failed to delete \""
                            + tmpFile + "\"?");
                }
            }
        } catch (Throwable throwable) {
            throw new RuntimeException(getFullName(), throwable);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    private String _pathName;

    private boolean _useContainerMoML;
}
