/* An actor that writes input data to the specified file.

@Copyright (c) 1998-1999 The Regents of the University of California.
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
@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib;

import ptolemy.kernel.util.*;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.actor.*;
import java.awt.*;

/** A general-purpose printer.  This actor reads tokens from any number
 *  of input channels and prints their string values to the specified
 *  output file.  If no file name is given, then the values are printed
 *  to the standard output.
 *  Note changes to the filename
 *  parameter during execution are ignored until the next execution.
 *  <p>
 *  The input type is StringToken.  Since any other type of token
 *  can be converted to a StringToken, this imposes no constraints
 *  on the types of the upstream actors.
 *
 *  @author  Yuhong Xiong, Edward A. Lee
 *  @version $Id$
 */
public class FileWrite extends TypedAtomicActor {

    public FileWrite(TypedCompositeActor container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(StringToken.class);

        filename = new Parameter(this, "filename", new StringToken(""));
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type StringToken. */
    public TypedIOPort input;

    /** The name of the file to write to. */
    public Parameter filename;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports.
     *  @param ws The workspace for the new object.
     *  @return A new actor.
     */
    public Object clone(Workspace ws) {
        try {
            FileWrite newobj = (FileWrite)super.clone(ws);
            newobj.input = (TypedIOPort)newobj.getPort("input");
            newobj.filename = (Parameter)newobj.getAttribute("filename");
            return newobj;
        } catch (CloneNotSupportedException ex) {
            // Errors should not occur here...
            throw new InternalErrorException(
                    "Clone failed: " + ex.getMessage());
        }
    }

    /** Read at most one token from each input channel and write its
     *  string value to the specified file.  Each value is terminated
     *  with a newline character.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException {
        // FIXME: This currently ignores the filename parameter and
        // just writes to stdout.
        int width = input.getWidth();
        for (int i = 0; i < width; i++) {
            if (input.hasToken(i)) {
                StringToken token = (StringToken)input.get(i);
                String value = token.stringValue();
                System.out.println(value + "\n");
            }
        }
    }

    /** Open the specified file, if any.  Note changes to the filename
     *  parameter during execution are ignored until the next execution.
     *  @exception IllegalActionException If the file cannot be opened,
     *   or if the parent class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        // FIXME: implement this.
    }

    /** Close the specified file, if any.
     */
    public void wrapup() {
        // FIXME: implement this.
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
}

