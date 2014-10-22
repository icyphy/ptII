/* An actor that writes the value of string tokens to a file, one per line.

 @Copyright (c) 2002-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY
 */
package ptolemy.actor.lib.io;

import java.io.File;
import java.io.PrintWriter;

import ptolemy.actor.lib.Sink;
import ptolemy.actor.parameters.FilePortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

///////////////////////////////////////////////////////////////////
//// LineWriter

/**
 <p>This actor reads string-valued input tokens and writes them,
 one line at a time, to a specified file.  It does not
 include any enclosing quotation marks in the output.
 If you need the enclosing quotation marks, precede this
 actor with TokenToExpression.</p>
 <p>
 The file is specified by the <i>fileName</i> attribute
 using any form acceptable to {@link FileParameter}.</p>
 <p>
 If the <i>append</i> attribute has value <i>true</i>,
 then the file will be appended to. If it has value <i>false</i>,
 then if the file exists, the user will be queried for permission
 to overwrite, and if granted, the file will be overwritten.</p>
 <p>
 If the <i>confirmOverwrite</i> parameter has value <i>false</i>,
 then this actor will overwrite the specified file if it exists
 without asking.  If <i>true</i> (the default), then if the file
 exists, then this actor will ask for confirmation before overwriting.</p>

 @see FileParameter
 @see ExpressionWriter
 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.2
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (liuj)
 */
public class LineWriter extends Sink {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public LineWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input.setTypeEquals(BaseType.STRING);
        new SingletonParameter(input, "_showName").setToken(BooleanToken.TRUE);

        fileName = new FilePortParameter(this, "fileName");
        fileName.setExpression("System.out");
        new SingletonParameter(fileName.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        append = new Parameter(this, "append");
        append.setTypeEquals(BaseType.BOOLEAN);
        append.setToken(BooleanToken.FALSE);

        confirmOverwrite = new Parameter(this, "confirmOverwrite");
        confirmOverwrite.setTypeEquals(BaseType.BOOLEAN);
        confirmOverwrite.setToken(BooleanToken.TRUE);

        endOfLineCharacter = new Parameter(this, "endOfLineCharacter");
        endOfLineCharacter.setTypeEquals(BaseType.STRING);
        // Default value is null.

        alwaysFlush = new Parameter(this, "alwaysFlush");
        alwaysFlush.setTypeEquals(BaseType.BOOLEAN);
        alwaysFlush.setToken(BooleanToken.FALSE);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-25\" y=\"-20\" " + "width=\"50\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polygon points=\"-15,-10 -12,-10 -8,-14 -1,-14 3,-10"
                + " 15,-10 15,10, -15,10\" " + "style=\"fill:red\"/>\n"
                + "</svg>\n");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** If <i>true</i>, then append to the specified file.  If <i>false</i>
     *  (the default), then overwrite any preexisting file after asking
     *  the user for permission.
     */
    public Parameter append;

    /** The file name to which to write.  This is a string with
     *  any form accepted by FilePortParameter.  The default value is
     *  "System.out".
     *  @see FilePortParameter
     */
    public FilePortParameter fileName;

    /** If <i>false</i>, then overwrite the specified file if it exists
     *  without asking.  If <i>true</i> (the default), then if the file
     *  exists, ask for confirmation before overwriting.
     */
    public Parameter confirmOverwrite;

    /** End of line character to use. This is a string
     *  that defaults to null, which results in the current
     *  platform's standard end-of-line character being used.
     *  If an empty string is specified,
     *  then no end of line character is used after each
     *  output written to the file.
     */
    public Parameter endOfLineCharacter;

    /** If <i>true</i>, flush output after each line is written. If
     *  <i>false</i> (the default), the output may not be written until the
     *  stream is closed during wrapup().
     */
    public Parameter alwaysFlush;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>fileName</i> and there is an
     *  open file being written, then close that file.  The new file will
     *  be opened or created when it is next written to.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>fileName</i> and the previously
     *   opened file cannot be closed.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileName) {
            // Do not close the file if it is the same file.
            String newFileName = ((StringToken) fileName.getToken())
                    .stringValue();

            if (_previousFileName != null
                    && !newFileName.equals(_previousFileName)) {
                _previousFileName = newFileName;
                fileName.close();
                _writer = null;
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        LineWriter newObject = (LineWriter) super.clone(workspace);
        newObject._writer = null;
        return newObject;
    }

    /** Read an input string token from each input
     *  channel and write it to the file, one line per token.
     *  If there is no input, do nothing.
     *  If the file is not open for writing then open it. If the file
     *  does not exist, then create it.  If the file already exists,
     *  then query the user for overwrite, unless the <i>append</i>
     *  parameter has value <i>true</i>.
     *  @exception IllegalActionException If the file cannot be opened
     *   or created, or if the user refuses to overwrite an existing file.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        fileName.update();
        for (int i = 0; i < input.getWidth(); i++) {
            if (input.hasToken(i)) {
                Token token = input.get(i);

                if (_writer == null) {
                    // File has not been opened.
                    boolean appendValue = ((BooleanToken) append.getToken())
                            .booleanValue();

                    String fileNameValue = fileName.stringValue();

                    // If previousFileName is null, we have never opened a file.
                    if (_previousFileName == null) {
                        _previousFileName = fileNameValue;
                    }
                    if (!fileNameValue.equals("System.out")) {
                        // Only check for append and overwrite if the
                        // fileName is not "System.out"
                        // Open the file.
                        File file = fileName.asFile();
                        boolean confirmOverwriteValue = ((BooleanToken) confirmOverwrite
                                .getToken()).booleanValue();

                        // Don't ask for confirmation in append mode, since there
                        // will be no loss of data.
                        if (file != null && file.exists() && !appendValue
                                && confirmOverwriteValue) {
                            // Query for overwrite.
                            // FIXME: This should be called in the event thread!
                            // There is a chance of deadlock since it is not.
                            if (!MessageHandler
                                    .yesNoQuestion("OK to overwrite " + file
                                            + "?")) {
                                throw new IllegalActionException(this,
                                        "Please select another file name.");
                            }
                        }
                    }

                    _writer = new PrintWriter(
                            fileName.openForWriting(appendValue), true);
                }
                _writeToken(token);
            }
        }
        return super.postfire();
    }

    /** Read the value of alwaysFlush parameter.
     *  @exception IllegalActionException If there is an error reading the
     *  alwaysFlush parameter.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        _flushValue = ((BooleanToken) alwaysFlush.getToken()).booleanValue();
    }

    /** Close the writer if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        fileName.close();
        _writer = null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Write the specified token to the current writer.
     *  This is protected so that derived classes can modify the
     *  format in which the token is written.
     *  @param token The token to write.
     *  @exception IllegalActionException Not thrown in this base class.
     */
    protected void _writeToken(Token token) throws IllegalActionException {
        String eol = "\n";
        Token eolToken = endOfLineCharacter.getToken();
        if (eolToken != null) {
            eol = ((StringToken) eolToken).stringValue();
        }
        // In this base class, the cast is safe.
        _writer.print(((StringToken) token).stringValue() + eol);

        if (_flushValue) {
            _writer.flush();
        }

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** If true, flush the writer after every write. */
    protected boolean _flushValue;

    /** The current writer. */
    protected PrintWriter _writer;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Previous value of fileName parameter. */
    private String _previousFileName;
}
