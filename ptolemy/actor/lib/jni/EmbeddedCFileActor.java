/* An actor that executes compiled embedded C code.  It takes in a file
 parameter that specifies the file where the code is located.

 Copyright (c) 2008-2010 The Regents of the University of California.
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
package ptolemy.actor.lib.jni;

import java.io.BufferedReader;
import java.io.IOException;

import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 The embeddedCFileActor executes compiled embedded C Code.

 <p>This actor extends the EmbeddedCActor and has most of the same
 functionality.  The only difference is that a file specifying the C
 code can be passed into the actor as a parameter to avoid having
 multiple instances of the same code in different copies of the
 EmbeddedCActor.

 @author Christine Avanessians, Edward Lee, and Man-Kit Leung
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (cavaness)
 @Pt.AcceptedRating Red (cavaness)
 */

public class EmbeddedCFileActor extends EmbeddedCActor {

    /** Construct an actor with the given container and name.
     *  Invoke the super class constructor and create the
     *  <i>codeBlockFile</i> file parameter and initialize
     *  it to the empty string.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception NameDuplicationException If the container already
     *   has an actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    public EmbeddedCFileActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        codeBlockFile = new FileParameter(this, "codeBlockFile");
        codeBlockFile.setExpression("");

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Set the embeddedCCode parameter in the EmbeddedCActor to
     * contain the contents of the file specified by codeBlockFile.
     * @exception IllegalActionException If there is a problem reading
     * the code block file.
     */
    public void changeEmbeddedCCode() throws IllegalActionException {
        BufferedReader reader = null;

        try {
            reader = codeBlockFile.openForReading();
            if (reader == null) {
                throw new IllegalActionException("Failed to open \""
                        + codeBlockFile + "\"");
            } else {
                StringBuffer code = new StringBuffer();
                // Is there a better way of reading in file into a string???
                try {
                    String str;
                    while ((str = reader.readLine()) != null) {
                        code.append(str + "\n");
                    }
                } catch (IOException e) {
                    throw new IllegalActionException("Could not read file"
                            + codeBlockFile.getExpression());
                }
                embeddedCCode.setExpression(code.toString());
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception ex) {
                    throw new IllegalActionException(this, ex,
                            "Failed to close \"" + codeBlockFile + "\"");
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Generate and compile the code.
     *  This method calls {@link #changeEmbeddedCCode()} and then
     *  generates adn compiles the code.
     */
    protected void _generateAndCompileCCode() throws IllegalActionException {
        // Before generating and compiling C code, make sure the contents
        // of the file have been saved into the embeddedCCode parameter
        // (in EmbeddedCActor) by calling the function
        // changeEmbeddedCCode.  The embeddedCCode parameter is set right
        // before code generation in order for the most recent revision of
        // the file to be utilized.

        changeEmbeddedCCode();
        super._generateAndCompileCCode();
    }

    /**
     * Create a new instance of EmbeddedFileActor and set _embeddedActor in the
     * embeddedCActor.
     *  @exception NameDuplicationException If the container already
     *   has an actor with this name.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     */
    protected void setEmbeddedActor() throws IllegalActionException,
            NameDuplicationException {
        _embeddedActor = new EmbeddedFileActor(this, "EmbeddedFileActor");
    }

    /**
     * The file parameter that specifies the file that contains the C Code that
     * this actor should use during execution and/or code generation.
     */
    public FileParameter codeBlockFile;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    /** An actor inside the EmbeddedCFileActor that is used as a dummy
     *  placeholder.  It serves the same purpose as the EmbeddedActor
     *  inside the EmbeddedCActor.
     */
    public static class EmbeddedFileActor extends
            ptolemy.actor.lib.jni.EmbeddedCActor.EmbeddedActor {
        /** Create a new instance of EmbeddedFileActor.
         *  @param container The container.
         *  @param name The name of this actor within the container.
         *  @exception IllegalActionException If this actor cannot be contained
         *   by the proposed container.
         *  @exception NameDuplicationException If the name coincides with
         *   an entity already in the container.
         */
        public EmbeddedFileActor(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);
            // In case an exception occurs and wrapup can't destroy
            // it, at least make sure it isn't saved.
            setPersistent(false);
        }
    }

}
