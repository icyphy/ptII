/* An actor that outputs a string read from a text file or URL.

 Copyright (c) 2003 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.io;

import java.io.BufferedReader;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.Source;
import ptolemy.data.StringToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// FileReader
/**
This actor reads a file or URL and outputs the entire file
as a single string.  The file or URL is specified using any form
acceptable to FileParameter.

@see FileParameter
@see LineReader

@author Yang Zhao (contributor: Edward A. Lee)
@version $Id$
@since Ptolemy II 3.0
*/
public class FileReader extends Source {

    /** Construct an actor with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public FileReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        output.setTypeEquals(BaseType.STRING);
        
        fileOrURL = new FileParameter(this, "fileOrURL");
        
        fileOrURLPort = new TypedIOPort(this, "fileOrURL", true, false);
        fileOrURLPort.setTypeEquals(BaseType.STRING);
        
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
    ////                         public variables                  ////
    
    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileParameter.
     *  @see FileParameter
     */
    public FileParameter fileOrURL;

    /** An input port for optionally providing a file name. This has
     *  type string.
     */
    public TypedIOPort fileOrURLPort;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    
    /** Output the data read from the file or URL as a string.
     *  @exception IllegalActionException If there is no director or
     *   if reading the file triggers an exception.
     */
    public void fire() throws IllegalActionException  {
        super.fire();
        
        // If the fileOrURL input port is connected and has data, then
        // get the file name from there.
        if (fileOrURLPort.getWidth() > 0) {
            if (fileOrURLPort.hasToken(0)) {
                String name =
                    ((StringToken)fileOrURLPort.get(0)).stringValue();
                // Using setExpression() rather than setToken() allows
                // the string to refer to variables defined in the
                // scope of this actor.
                fileOrURL.setExpression(name);
            }
        }

        try {
            BufferedReader reader = fileOrURL.openForReading();
            StringBuffer lineBuffer = new StringBuffer();
            String newline = System.getProperty("line.separator");
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                lineBuffer = lineBuffer.append(line);
                lineBuffer = lineBuffer.append(newline);
            }
            fileOrURL.close();
            output.broadcast(new StringToken(lineBuffer.toString()));
        } catch(Exception ex) {
            throw new IllegalActionException(this, ex.getMessage());
        }
    }
}
