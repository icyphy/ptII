/* An actor that writes input data to the specified file.

@Copyright (c) 1998-2003 The Regents of the University of California.
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
@ProposedRating Yellow (yuhong@eecs.berkeley.edu)
@AcceptedRating Yellow (mudit@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

import java.io.IOException;

/**
This actor reads tokens from any number of input channels and writes
their string values to the specified output file.  If no file name
is given, then the values are written to the standard output.

@deprecated This actor is deprecated. Use ExpressionWriter instead.

@author  Yuhong Xiong, Edward A. Lee
@version $Id$
@since Ptolemy II 0.4
*/
public class FileWriter extends ptolemy.actor.lib.Writer {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FileWriter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        filename = new Parameter(this, "filename");
        filename.setExpression("");
        filename.setTypeEquals(BaseType.STRING);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The name of the file to write to. This parameter contains
     *  a StringToken.  By default, it contains an empty string, which
     *  is interpreted to mean that output should be directed to the
     *  standard output.
     */
    public Parameter filename;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>filename</i>, then close
     *  the current file (if there is one) and open the new one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>filename</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == filename) {
            try {
                StringToken filenameToken = (StringToken)filename.getToken();
                if (filenameToken == null) {
                    setWriter(null);
                } else {
                    String newFilename = filenameToken.stringValue();
                    if (newFilename.equals("")) {
                        setWriter(null);
                    } else {
                        java.io.FileWriter writer
                            = new java.io.FileWriter(newFilename);
                        setWriter(writer);
                    }
                }
            } catch (IOException ex) {
                throw new IllegalActionException(this, ex,
                        "attributeChanged(" + 
                        attribute.getName() + ") failed");
            }
        }
    }

    /** Open the specified file, if any.
     *  @exception IllegalActionException If the file cannot be opened,
     *   or if the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        attributeChanged(filename);
    }

    /** Close the file, if there is one.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public void wrapup() throws IllegalActionException {
        super.wrapup();
        // To get the file to close.
        setWriter(null);
    }
}
