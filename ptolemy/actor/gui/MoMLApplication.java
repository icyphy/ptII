/* A Ptolemy application that can read MoML files.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@AcceptedRating Red (vogel@eecs.berkeley.edu)
*/

package ptolemy.actor.gui;

// Java imports
import java.lang.System;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

// Ptolemy imports
import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedCompositeActor;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

// XML imports
import com.microstar.xml.XmlException;

//////////////////////////////////////////////////////////////////////////
//// MoMLApplication
/**
This application creates a Ptolemy II model given a MoML file name on the
command line, and then executes that model.  The specified file should
define a model derived from CompositeActor.

@author Edward A. Lee
@version $Id$
*/
public class MoMLApplication extends CompositeActorApplication {

    /** Parse the command-line arguments, creating models as specified.
     *  Then execute each model.
     *  @param args The command-line arguments.
     *  @exception Exception If command line arguments have problems.
     */
    public MoMLApplication(String args[]) throws Exception {
        super(args);
        _commandTemplate = "ptolemy [ options ] [file ...]";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a new application with the specified command-line arguments.
     *  @param args The command-line arguments.
     */
    public static void main(String args[]) {
        try {
            MoMLApplication plot = new MoMLApplication(args);
        } catch (Exception ex) {
            System.err.println(ex.toString());
            ex.printStackTrace();
        }

        // If the -test arg was set, then exit after 2 seconds.
        if (_test) {
            try {
                Thread.currentThread().sleep(2000);
            } catch (InterruptedException e) {
            }
            System.exit(0);
        }
    }

    ////////////////////////////////////////////////////////////////////////
    ////                         protected methods                      ////

    /** Parse a command-line argument.
     *  @return True if the argument is understood, false otherwise.
     *  @throws Exception If something goes wrong.
     */
    protected boolean _parseArg(String arg) throws Exception {
        if (super._parseArg(arg)) {
            return true;
        } else if (!arg.startsWith("-")) {
            // Assume the argument is a file name.
            // Attempt to read it.
            InputStream instream;
            URL base;
            try {
                // First argument is null because we are only
                // processing absolute URLs this way.  Relative
                // URLs are opened as ordinary files.
                URL inurl = new URL(null, arg);
                // Strangely, the XmlParser does not want as base the
                // directory containing the file, but rather the file itself.
                base = inurl;
                instream = inurl.openStream();
            } catch (MalformedURLException ex) {
                try {
                    File file = new File(arg);
                    instream = new FileInputStream(file);

                    // Strangely, the XmlParser does not want as base the
                    // directory containing the file, but rather the
                    // file itself.
                    File directory = new File(file.getAbsolutePath());
                    // base = new URL("file", null, directory);
                    base = file.toURL();
                } catch (FileNotFoundException ex2) { 
                    URL inurl = Class.forName("ptolemy.kernel.util.NamedObj").
                        getClassLoader().getResource(arg);
                    instream = inurl.openStream();
                    base = inurl;
                }
            }
            _read(base, instream);
        } else {
            // Argument not recognized.
            return false;
        }
        return true;
    }

    /** Read the specified stream, which is expected to be a MoML file.
     *  @param base The base for relative file references, or null if
     *   there are no relative file references.
     *  @param in The input stream.
     *  @exception IOException If the stream cannot be read.
     */
    protected void _read(URL base, InputStream in) throws IOException {
        MoMLParser parser = new MoMLParser();
        try {
            NamedObj toplevel = parser.parse(base, in);
            if (toplevel instanceof TypedCompositeActor) {
                CompositeActor castTopLevel = (CompositeActor)toplevel;
                add(castTopLevel);
            }
        } catch (Exception ex) {
            if (ex instanceof XmlException) {
                XmlException xmlEx = (XmlException)ex;
                // FIXME: The file reported below is wrong... Why?
                report("MoML exception on line " + xmlEx.getLine()
                        + ", column " + xmlEx.getColumn() + ", in entity:\n"
                        + xmlEx.getSystemId(), ex);
            } else {
                report("Failed to read file:\n", ex);
            }
        }
    }
}
