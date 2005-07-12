/*
 @Copyright (c) 2005 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION_2
 COPYRIGHTENDKEY


 */

package ptolemy.codegen.c.actor.lib.io;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;

/**
 * A helper class for ptolemy.actor.lib.io.LineReader
 * 
 * @author Jackie
 * @version $Id$
 * @since Ptolemy II 5.1
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LineReader extends CCodeGeneratorHelper {
    /**
     * Constructor method for the LineReader helper
     * @param actor the associated actor
     */
    public LineReader(ptolemy.actor.lib.io.LineReader actor) {
        super(actor);
    }

    /**
     * Generate fire code
     * The method reads in codeBlock1 and puts into the
     * given stream buffer
     * @param stream the given buffer to append the code to
     */
    public void generateFireCode(StringBuffer stream)
            throws IllegalActionException {
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("readLine");
        stream.append(processCode(tmpStream.toString()));
    }

    /** Generate initialization code.
     *  This method reads the <code>initBlock</code> from Test.c,
     *  replaces macros with their values and returns the results.
     *  @return The processed <code>initBlock</code>.
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("initBlock");

        ptolemy.actor.lib.io.LineReader actor = 
            (ptolemy.actor.lib.io.LineReader) getComponent();

        int skipLines = Integer.parseInt(actor.numberOfLinesToSkip
                .getExpression());

        // FIXME: How do we fix the file path parameter of the actor?? 
        //String fileNameString = actor.fileOrURL.asFile().getCanonicalPath();

        String fileNameString = actor.fileOrURL.getExpression();
        
        if (fileNameString.equals("System.in")) {
            _fileOpen = false;
            tmpStream.append("openForStdin");
        } else {
            _fileOpen = true;
            try {
            	fileNameString = FileUtilities.nameToFile(actor.fileOrURL.
                        getExpression(), null).getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalActionException("Cannot open file: "
                        + fileNameString);
            }
            
            tmpStream.appendCodeBlock("openForRead");

            for (int i = 0; i < skipLines; i++) {
                tmpStream.appendCodeBlock("skipLine");
            }
        }
        return processCode(tmpStream.toString());
    }

    /** Generate preinitialization code.
     *  This method reads the <code>preinitBlock</code> from LineReader.c,
     *  replaces macros with their values and returns the results.
     *  @return The processed code block.
     */
    public String generatePreinitializeCode() throws IllegalActionException {
        super.generatePreinitializeCode();
        CodeStream tmpStream = new CodeStream(this);
        tmpStream.appendCodeBlock("preinitBlock");
        return processCode(tmpStream.toString());
    }

    /** Generate wrap up code.
     *  This method reads the <code>wrapUpBlock</code> from LineReader.c,
     *  replaces macros with their values and returns the results.
     *  @return The processed <code>wrapUpBlock</code>.
     */
    public void generateWrapupCode(StringBuffer stream)
            throws IllegalActionException {
        if (_fileOpen) {
            CodeStream tmpStream = new CodeStream(this);
            tmpStream.appendCodeBlock("wrapUpBlock");
            stream.append(processCode(tmpStream.toString()));
        }
    }

    /** Get the files needed by the code generated for the
     *  LineReader actor.
     *  @return A set of strings that are names of the files
     *   needed by the code generated for the LineReader actor.
     */
    public Set getIncludingFiles() {
        Set files = new HashSet();
        files.add("\"stdio.h\"");
        return files;
    }

    /**
     * indicate whether or not the user requests to open a file
     * e.g. false - write to standard (console) output
     *      true - some file name is specified
     */
    private boolean _fileOpen = false;
}
