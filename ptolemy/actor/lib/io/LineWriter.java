/* An actor that writes the value of string tokens to a file, one per line.

@Copyright (c) 1998-2002 The Regents of the University of California.
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

import ptolemy.actor.lib.Sink;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.gui.MessageHandler;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

//////////////////////////////////////////////////////////////////////////
//// LineWriter
/**
This actor reads string-valued input tokens and writes them,
one line at a time, to a specified file.  It does not include
include any enclosing quotation marks.  If you need the enclosing
quotation marks, use ExpressionWriter.  The file is
specified using any form acceptable to FileAttribute.

@see FileAttribute
@see ExpressionWriter
@author  Edward A. Lee
@version $Id$
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

        fileName = new FileAttribute(this, "fileName");
        fileName.setExpression("System.out");

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

    /** The file name to which to write.  This is a string with
     *  any form accepted by FileAttribute.  The default value is
     *  "System.out".
     *  @see FileAttribute
     */
    public FileAttribute fileName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>fileName</i> and there is an
     *  open file being written, then close that file.  The new one will
     *  be opened or created when it is next written to.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>fileName</i> and the previously
     *   opened file cannot be closed.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == fileName) {
            fileName.close();
            _writer = null;
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
     *  If the file is not open for writing then open it. If the file
     *  does not exist, then create it.  If the file already exists,
     *  then query the user for overwrite. If there is no input, do nothing.
     *  @exception IllegalActionException If the file cannot be opened
     *   or created, or if the user refuses to overwrite an existing file.
     */
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            StringToken token = (StringToken)input.get(0);
            if (_writer == null) {
                // Open the file.
                File file = fileName.asFile();
                if (file.exists()) {
                    // Query for overwrite.
                    if (!MessageHandler.yesNoQuestion(
                            "OK to overwrite " + file + "?")) {
                        throw new IllegalActionException(this,
                                "Please select another file name.");
                    }
                }
                _writer = new PrintWriter(fileName.openForWriting());
            }
            _writer.println(token.stringValue());
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
    ////                         protected members                 ////

    /** The current writer. */
    protected PrintWriter _writer;
}
