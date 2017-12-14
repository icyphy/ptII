/* A representative of a text file.

 Copyright (c) 1998-2016 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Locale;

import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;

import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// TextEffigy

/**
 An effigy for a text file.  If the ptolemy.user.texteditor property
 is set to "emacs", then {@link ExternalTextEffigy} is used as an Effigy,
 otherwise this class is used as an Effigy.

 @author Edward A. Lee, contributor Zoltan Kemenczy
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Red (neuendor)
 @Pt.AcceptedRating Red (neuendor)
 */
public class TextEffigy extends Effigy {
    /** Create a new effigy in the specified workspace with an empty string
     *  for its name.
     *  @param workspace The workspace for this effigy.
     */
    public TextEffigy(Workspace workspace) {
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
    public TextEffigy(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the syntax style to use for files with the given extension.
     *  @param extension The file extension.
     *  @return A syntax style, or none if the extension is not recognized.
     */
    public static String extensionToSyntaxStyle(String extension) {
        extension = extension.trim().toLowerCase();
        switch (extension) {
        // The returned strings are defined in
        // org.fife.ui.rsyntaxtextarea.SyntaxConstants
        // but we don't want a hard dependence on an external
        // library, so we have replicate those strings here.
        case "c":
            return "text/c";
        case "clj":
            return "text/clojure";
        case "cpp":
            return "text/cpp";
        case "cs":
            return "text/cs";
        case "css":
            return "text/css";
        case "dtd":
            return "text/dtd";
        case "f":
        case "f90":
            return "text/fortran";
        case "groovy":
        case "gvy":
        case "gy":
            return "text/groovy";
        case "h":
            return "text/cpp";
        case "htm":
        case "html":
            return "text/html";
        case "java":
            return "text/java";
        case "js":
        case "javascript":
            return "text/javascript";
        case "json":
            return "text/json";
        case "jsp":
            return "text/jsp";
        case "tex":
        case "latex":
            return "text/latex";
        case "mk":
            return "text/makefile";
        case "pl":
            return "text/perl";
        case "php":
            return "text/php";
        case "properties":
            return "text/properties";
        case "py":
        case "python":
            return "text/python";
        case "rby":
        case "ruby":
            return "text/ruby";
        case "scala":
            return "text/scala";
        case "sh":
            return "text/unix";
        case "sql":
            return "text/sql";
        case "tcl":
            return "text/tcl";
        case "txt":
            return "text/plain";
        case "vb":
            return "text/vb";
        case "bat":
            return "text/bat";
        case "xml":
            return "text/xml";
        default:
            return null;
        }
    }

    /** Return the document that this is an effigy of.
     *  @return The document, or null if none has been set.
     *  @see #setDocument(Document)
     */
    public Document getDocument() {
        return _doc;
    }

    /** Return the syntax style for the document, if one has been identified,
     *  and null otherwise.
     *  @return A syntax style or null.
     */
    public String getSyntaxStyle() {
        return _syntaxStyle;
    }

    /** Override the base class to compare the current text in the document
     *  against the original text.
     *  @return True if the data has been modified.
     */
    @Override
    public boolean isModified() {
        if (_originalText == null) {
            if (_doc.getLength() > 0) {
                return true;
            } else {
                return false;
            }
        }
        try {
            if (_originalText.equals(_doc.getText(0, _doc.getLength()))) {
                return false;
            }
        } catch (BadLocationException e) {
            // This should not happen.
            return true;
        }
        return true;
    }

    /** Create a new effigy in the given container containing the specified
     *  text.  The new effigy will have a new instance of
     *  DefaultStyledDocument associated with it.
     *  @param container The container for the effigy.
     *  @param text The text to insert in the effigy.
     *  @return A new instance of SyntaxTextEffigy.
     *  @exception Exception If the text effigy cannot be
     *   contained by the specified container, or if the specified
     *   text cannot be inserted into the document.
     */
    public static TextEffigy newTextEffigy(CompositeEntity container,
            String text) throws Exception {
        return newTextEffigy(container, text, null);
    }

    /** Create a new effigy in the given container containing the specified
     *  text.  The new effigy will have a new instance of
     *  DefaultStyledDocument associated with it.
     *  @param container The container for the effigy.
     *  @param text The text to insert in the effigy.
     *  @param syntaxStyle The style of the text, for highlighting.
     *   This can be one of the styles defined in org.fife.ui.rsyntaxtextarea.SyntaxConstants,
     *   if that is installed,
     *   or null or an empty string for plain text. If the style is not recognized, then
     *   plain text will be assumed.
     *  @return A new instance of SyntaxTextEffigy.
     *  @exception Exception If the text effigy cannot be
     *   contained by the specified container, or if the specified
     *   text cannot be inserted into the document.
     */
    public static TextEffigy newTextEffigy(CompositeEntity container,
            String text, String syntaxStyle) throws Exception {
        // Create a new effigy.
        TextEffigy effigy = new TextEffigy(container,
                container.uniqueName("effigy"));
        if (syntaxStyle == null || syntaxStyle.trim().equals("")) {
            syntaxStyle = "text/plain";
        }
        effigy._syntaxStyle = syntaxStyle;
        Document doc = _createDocument(syntaxStyle);
        effigy.setDocument(doc);

        if (text != null) {
            doc.insertString(0, text, null);
        }
        effigy._originalText = text;
        return effigy;
    }

    /** Create a new effigy in the given container by reading the specified
     *  URL. If the specified URL is null, then create a blank effigy.
     *  If the extension of the URL is one of several extensions used by
     *  binary formats, the file is not opened and this returns null.
     *  The new effigy will have a new instance of
     *  DefaultStyledDocument associated with it.
     *  @param container The container for the effigy.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.  This is ignored in this
     *   class.
     *  @param in The input URL, or null if there is none.
     *  @return A new instance of SyntaxTextEffigy.
     *  @exception Exception If the URL cannot be read, or if the data
     *   is malformed in some way.
     */
    public static TextEffigy newTextEffigy(CompositeEntity container, URL base,
            URL in) throws Exception {

        // Check the extension: if it looks like a binary file do not open.
        // Do not open KAR files,
        // see http://bugzilla.ecoinformatics.org/show_bug.cgi?id=5280#c1
        // For other extensions, determine the syntax style (a MIME type).
        //
        String syntaxStyle = "text/plain";
        if (in != null) {
            String extension = EffigyFactory.getExtension(in)
                    .toLowerCase(Locale.getDefault());
            String syntaxStyleFromExtension = extensionToSyntaxStyle(extension);
            if (syntaxStyleFromExtension != null) {
                syntaxStyle = syntaxStyleFromExtension;
            } else if (extension.equals("jar") || extension.equals("kar")
            // TODO: find a better way to check for binary files.
                    || extension.equals("gz") || extension.equals("tar")
                    || extension.equals("zip")) {
                return null;
            }
        }

        // Create a new effigy.
        TextEffigy effigy = new TextEffigy(container,
                container.uniqueName("effigy"));
        Document doc = _createDocument(syntaxStyle);
        effigy.setDocument(doc);
        effigy._syntaxStyle = syntaxStyle;

        if (in != null) {
            // A URL has been given.  Read it.
            BufferedReader reader = null;

            try {
                try {
                    InputStream inputStream = null;

                    try {
                        inputStream = in.openStream();
                    } catch (NullPointerException npe) {
                        throw new IOException(
                                "Failed to open '" + in + "', base: '" + base
                                        + "' : openStream() threw a "
                                        + "NullPointerException");
                    } catch (Exception ex) {
                        IOException exception = new IOException(
                                "Failed to open '" + in + "\", base: \"" + base
                                        + "\"");
                        exception.initCause(ex);
                        throw exception;
                    }

                    reader = new BufferedReader(
                            new InputStreamReader(inputStream));

                    // openStream throws an IOException, not a
                    // FileNotFoundException
                } catch (IOException ex) {
                    try {
                        // If we are running under WebStart, and try
                        // view source on a .html file that is not in
                        // ptsupport.jar, then we may end up here,
                        // so we look for the file as a resource.
                        URL jarURL = ptolemy.util.ClassUtilities
                                .jarURLEntryResource(in.toString());
                        reader = new BufferedReader(
                                new InputStreamReader(jarURL.openStream()));

                        // We were able to open the URL, so update the
                        // original URL so that the title bar accurately
                        // reflects the location of the file.
                        in = jarURL;
                    } catch (Throwable throwable) {
                        try {
                            // Hmm.  Might be Eclipse, where sadly the
                            // .class files are often in a separate directory
                            // than the .java files.  So, we look at the CLASSPATH
                            // and for each element that names a directory, traverse
                            // the parents directories and look for adjacent directories
                            // that contain a "src" directory.  For example if
                            // the classpath contains "kepler/ptolemy/target/classes/",
                            // then we will find kepler/ptolemy/src and return it
                            // as a URL.  See also Configuration.createPrimaryTableau()

                            URL sourceURL = ptolemy.util.ClassUtilities
                                    .sourceResource(in.toString());
                            reader = new BufferedReader(new InputStreamReader(
                                    sourceURL.openStream()));

                            // We were able to open the URL, so update the
                            // original URL so that the title bar accurately
                            // reflects the location of the file.
                            in = sourceURL;

                        } catch (Throwable throwable2) {
                            // Looking for the file as a resource did not work,
                            // so we rethrow the original exception.
                            throw ex;
                        }
                    }
                }

                String line = reader.readLine();

                while (line != null) {
                    // Translate newlines to Java form.
                    doc.insertString(doc.getLength(), line + "\n", null);
                    line = reader.readLine();
                }
            } finally {
                if (reader != null) {
                    reader.close();
                }
            }

            // Check the URL to see whether it is a file,
            // and if so, whether it is writable.
            if (in.getProtocol().equals("file")) {
                String filename = in.getFile();
                File file = new File(filename);

                try {
                    if (!file.canWrite()) {
                        effigy.setModifiable(false);
                    }
                } catch (SecurityException ex) {
                    // We are in an applet or sandbox.
                    effigy.setModifiable(false);
                }
            } else {
                effigy.setModifiable(false);
            }

            effigy.uri.setURL(in);
        } else {
            // No document associated.  Allow modifications.
            effigy.setModifiable(true);
        }
        effigy._originalText = doc.getText(0, doc.getLength());
        return effigy;
    }

    /** Set the document that this is an effigy of.
     *  @param document The document
     *  @see #getDocument()
     */
    public void setDocument(Document document) {
        _doc = document;
    }

    @Override
    public void setModified(boolean modified) {
        super.setModified(modified);
        if (!modified) {
            // If someone is indicating that this is no longer modified, then reset
            // the _originalText to equal the current text.
            try {
                _originalText = _doc.getText(0, _doc.getLength());
            } catch (Exception ex) {
                // Should not occur. Ignore. Worst case is an extra prompt to apply.
            }
        }
    }

    /** Write the text of the document to the specified file.
     *  @param file The file to write to.
     *  @exception IOException If the write fails.
     */
    @Override
    public void writeFile(File file) throws IOException {
        if (_doc != null) {
            java.io.FileWriter fileWriter = null;

            try {
                fileWriter = new java.io.FileWriter(file);

                try {
                    fileWriter.write(_doc.getText(0, _doc.getLength()));
                } catch (BadLocationException ex) {
                    throw new IOException(
                            "Failed to get text from the document: " + ex);
                }
            } finally {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected method                  ////

    /** Create a syntax document, if possible, and otherwise a plain
     *  document.
     *  @param syntaxStyle The syntax style.
     *  @return A new document.
     */
    protected static Document _createDocument(String syntaxStyle) {
        Document doc = null;
        try {
            // Attempt to create a styled document.
            // Use reflection here to avoid a hard dependency on an external package.
            Class docClass = Class
                    .forName("org.fife.ui.rsyntaxtextarea.RSyntaxDocument");
            Constructor docClassConstructor = docClass
                    .getConstructor(String.class);
            doc = (Document) docClassConstructor
                    .newInstance(new Object[] { syntaxStyle });
        } catch (Throwable ex) {
            // Ignore and use default text editor.
            System.out.println("Note: failed to open syntax-directed editor: "
                    + ex.getMessage());
        }
        if (doc == null) {
            doc = new DefaultStyledDocument();
        }
        return doc;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** The document associated with this effigy. */
    private Document _doc;

    /** The original text, to determine whether it has been modified. */
    private String _originalText;

    /** The syntax style, if one has been identified. */
    private String _syntaxStyle;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** A factory for creating new effigies.
     */
    public static class Factory extends EffigyFactory {
        /** Create a factory with the given name and container.
         *  @param container The container.
         *  @param name The name.
         *  @exception IllegalActionException If the container is incompatible
         *   with this entity.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public Factory(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            try {
                String editorPreference = ".";

                try {
                    editorPreference = System
                            .getProperty("ptolemy.user.texteditor", ".");
                } catch (SecurityException security) {
                    // Ignore, we are probably running in a sandbox
                    // or applet.
                }

                Class effigyClass;

                if (editorPreference.equals("emacs")) {
                    effigyClass = Class
                            .forName("ptolemy.actor.gui.ExternalTextEffigy");
                } else {
                    effigyClass = Class.forName("ptolemy.actor.gui.TextEffigy");
                }

                _newTextEffigyURL = effigyClass.getMethod("newTextEffigy",
                        new Class[] { CompositeEntity.class, URL.class,
                                URL.class });
            } catch (ClassNotFoundException ex) {
                throw new IllegalActionException(ex.toString());
            } catch (NoSuchMethodException ex) {
                throw new IllegalActionException(ex.toString());
            }
        }

        ///////////////////////////////////////////////////////////////
        ////                     public methods                    ////

        /** Return true, indicating that this effigy factory is
         *  capable of creating an effigy without a URL being specified.
         *  @return True.
         */
        @Override
        public boolean canCreateBlankEffigy() {
            return true;
        }

        /** Create a new effigy in the given container by reading the specified
         *  URL. If the specified URL is null, then create a blank effigy.
         *  The extension of the URL is not
         *  checked, so this will open any file.  Thus, this factory
         *  should be last on the list of effigy factories in the
         *  configuration.
         *  The new effigy will have a new instance of
         *  DefaultStyledDocument associated with it.
         *  @param container The container for the effigy.
         *  @param base The base for relative file references, or null if
         *   there are no relative file references.  This is ignored in this
         *   class.
         *  @param in The input URL.
         *  @return A new instance of TextEffigy.
         *  @exception Exception If the URL cannot be read, or if the data
         *   is malformed in some way.
         */
        @Override
        public Effigy createEffigy(CompositeEntity container, URL base, URL in)
                throws Exception {
            // Create a new effigy.
            try {
                return (Effigy) _newTextEffigyURL.invoke(null,
                        new Object[] { container, base, in });
            } catch (java.lang.reflect.InvocationTargetException ex) {
                throw (Exception) ex.getCause();
                // Uncomment this for debugging
                // throw new java.lang.reflect.InvocationTargetException(ex,
                // " Invocation of method failed!. Method was: "
                // + _newTextEffigyURL
                // + "\nwith arguments( container = " + container
                // + " base = " + base + " in = " + in + ")");
            }
        }

        private Method _newTextEffigyURL;
    }
}
