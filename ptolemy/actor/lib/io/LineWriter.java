/* An actor that writes the value of string tokens to a file, one per line.

@Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (liuj@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.io;

import java.io.File;
import java.io.PrintWriter;

import ptolemy.actor.lib.Sink;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.util.MessageHandler;

//////////////////////////////////////////////////////////////////////////
//// LineWriter
/**
This actor reads string-valued input tokens and writes them,
one line at a time, to a specified file.  It does not
include any enclosing quotation marks in the output.
If you need the enclosing quotation marks, use ExpressionWriter.
<p>
The file is specified by the <i>fileName</i> attribute
using any form acceptable to FileParameter.
<p>
If the <i>append</i> attribute has value <i>true</i>,
then the file will be appended to. If it has value <i>false</i>,
then if the file exists, the user will be queried for permission
to overwrite, and if granted, the file will be overwritten.
<p>
If the <i>confirmOverwrite</i> parameter has value <i>false</i>,
then this actor will overwrite the specified file if it exists
without asking.  If <i>true</i> (the default), then if the file
exists, then this actor will ask for confirmation before overwriting.

@see FileParameter
@see ExpressionWriter
@author  Edward A. Lee
@version $Id$
@since Ptolemy II 2.2
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
        input.setMultiport(false);

        fileName = new FileParameter(this, "fileName");
        fileName.setExpression("System.out");

        append = new Parameter(this, "append");
        append.setTypeEquals(BaseType.BOOLEAN);
        append.setToken(BooleanToken.FALSE);

        confirmOverwrite = new Parameter(this, "confirmOverwrite");
        confirmOverwrite.setTypeEquals(BaseType.BOOLEAN);
        confirmOverwrite.setToken(BooleanToken.TRUE);

        _attachText("_iconDescription", "<svg>\n"
                + "<rect x=\"-25\" y=\"-20\" "
                + "width=\"50\" height=\"40\" "
                + "style=\"fill:white\"/>\n"
                + "<polygon points=\"-15,-10 -12,-10 -8,-14 -1,-14 3,-10"
                + " 15,-10 15,10, -15,10\" "
                + "style=\"fill:red\"/>\n"
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
     *  any form accepted by FileParameter.  The default value is
     *  "System.out".
     *  @see FileParameter
     */
    public FileParameter fileName;

    /** If <i>false</i>, then overwrite the specified file if it exists
     *  without asking.  If <i>true</i> (the default), then if the file
     *  exists, ask for confirmation before overwriting.
     */
    public Parameter confirmOverwrite;

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
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileName) {
            // Do not close the file if it is the same file.
            String newFileName = ((StringToken)fileName.getToken()).stringValue();
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
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        LineWriter newObject = (LineWriter)super.clone(workspace);
        newObject._writer = null;
        return newObject;
    }

    /** Read an input string token and write it to the file.
     *  If there is no input, do nothing.
     *  If the file is not open for writing then open it. If the file
     *  does not exist, then create it.  If the file already exists,
     *  then query the user for overwrite, unless the <i>append</i>
     *  parameter has value <i>true</i>.
     *  @exception IllegalActionException If the file cannot be opened
     *   or created, or if the user refuses to overwrite an existing file.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token token = input.get(0);
            if (_writer == null) {
                // Open the file.
                File file = fileName.asFile();
                boolean appendValue
                    = ((BooleanToken)append.getToken()).booleanValue();
                boolean confirmOverwriteValue
                    = ((BooleanToken)confirmOverwrite.getToken())
                    .booleanValue();
                // Don't ask for confirmation in append mode, since there
                // will be no loss of data.
                if (file.exists() && !appendValue && confirmOverwriteValue) {
                    // Query for overwrite.
                    // FIXME: This should be called in the event thread!
                    // There is a chance of deadlock since it is not.
                    if (!MessageHandler.yesNoQuestion(
                            "OK to overwrite " + file + "?")) {
                        throw new IllegalActionException(this,
                                "Please select another file name.");
                    }
                }
                _writer = new PrintWriter(
                        fileName.openForWriting(appendValue), true);
            }
            _writeToken(token);
        }
        return super.postfire();
    }

    /** Close the writer if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
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
     */
    protected void _writeToken(Token token) {
        // In this base class, the cast is safe.
        _writer.println(((StringToken)token).stringValue());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected members                 ////

    /** The current writer. */
    protected PrintWriter _writer;

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    /** Previous value of fileName parameter. */
    private String _previousFileName;

}
