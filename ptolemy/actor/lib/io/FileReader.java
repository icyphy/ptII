/* An actor that outputs a string read from a text file or URL.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (reviewmoderator@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.io;

import ptolemy.actor.TypedIOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Workspace;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.CompositeEntity;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;

import java.io.BufferedReader;

//////////////////////////////////////////////////////////////////////////
//// FileReader
/**
This actor reads a file or URL and outputs the whole file
as a string.  The file or URL is specified using any form
acceptable to FileAttribute.

//Note: this actor was originally designed to read a model
//file and output a moml string for the MobileModel actor
// to load the model dynamically. So it is supposed to
// triggered to fire at a proper time for one time.
@see LineReader

@author Yang Zhao
@version $Id$
@since Ptolemy II 2.0
*/
public class FileReader extends TypedAtomicActor{

    /** Construct an actor in the specified workspace with
     *  no container and an empty string as a name. You can then change
     *  the name with setName(). If the workspace argument is null, then
     *  use the default workspace.
     *  @param workspace The workspace that will list the actor.
     */
    public FileReader(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
        trigger = new TypedIOPort(this, "trigger", true, false);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
        //modelURL = new Parameter(this, "modelURL", new StringToken(""));
        //modelURL.setTypeEquals(BaseType.STRING);
        fileOrURL = new FileAttribute(this, "fileOrURL");
    }

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
        trigger = new TypedIOPort(this, "trigger", true, false);
        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.STRING);
        //modelURL = new Parameter(this, "modelURL", new StringToken(""));
        //modelURL.setTypeEquals(BaseType.STRING);
        fileOrURL = new FileAttribute(this, "fileOrURL");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////
    /** The input and output ports.
     *
     */
    public TypedIOPort trigger, output;

    /** The file name or URL from which to read.  This is a string with
     *  any form accepted by FileAttribute.
     *  @see FileAttribute
     */
    public FileAttribute fileOrURL;

    //used to use an URL to specify the file to read.
    //public Parameter modelURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    /** Output the data read from the file or URL as a string.
     *  @exception IllegalActionException If there is no director.
     */
    public void fire() throws IllegalActionException  {
        if (_debugging) {
            _debug("Invoking fire");
        }
        //just consumme the token.
        for (int i = 0; i < trigger.getWidth(); i++) {
            if (trigger.hasToken(i)) {
                trigger.get(i);
            }
        }
	//trigger.get(0);
        try {
            BufferedReader reader;
            // Ignore if the fileOrUL is blank.
            if (fileOrURL.getExpression().trim().equals("")) {
                reader = null;
            } else {
                reader = fileOrURL.openForReading();
            }
            StringBuffer momlBuffer = new StringBuffer();
            String newline = System.getProperty("line.separator");
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                momlBuffer = momlBuffer.append(line);
                momlBuffer = momlBuffer.append(newline);
            }
            fileOrURL.close();
            String momlString = momlBuffer.toString();
            output.broadcast(new StringToken(momlString));
        } catch(Exception ex) {
            throw new IllegalActionException(this, ex.getMessage());
        }
    }

    /** Return true if there is token at the <i>trigger<i> input.
     *  Otherwise, return false.
     *  @exception IllegalActionException If the superclass throws it.
     */
    public boolean prefire() throws IllegalActionException {
        if (_debugging) {
            _debug("Invoking prefire");
        }
        if (trigger.hasToken(0)) {
            return true;
        }
        return false;
    }

}
