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
/*
 * Created on Apr 5, 2005
 *
 */
package ptolemy.codegen.c.actor.lib.io;

import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.CodeStream;
import ptolemy.codegen.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;


/**
 * @author Jackie
 * @version $Id$
 */
public class LineWriter extends CCodeGeneratorHelper {
    /**
     * Constructor method for the LineWriter helper
     * @param actor the associated actor
     */
    public LineWriter(ptolemy.actor.lib.io.LineWriter actor) {
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
        tmpStream.appendCodeBlock("writeLine");
        stream.append(processCode(tmpStream.toString()));
    }

    /** Generate initialization code.
     *  This method reads the <code>initBlock</code> from Test.c,
     *  replaces macros with their values and returns the results.
     *  @return The processed <code>initBlock</code>.
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.io.LineWriter actor = (ptolemy.actor.lib.io.LineWriter) getComponent();
        CodeStream tmpStream = new CodeStream(this);

        //tmpStream.appendCodeBlock("initBlock");

        
        if (actor.fileName.getExpression().equals("System.out")) {
            _fileOpen = false;
            tmpStream.appendCodeBlock("openForStdout");
        } else {
            _fileOpen = true;

            // FIXME: how do we handle relative file path??
            String fileNameString = actor.fileName.getExpression();
            fileNameString = fileNameString.replaceFirst("file:/", "");
            fileNameString = fileNameString.replaceAll("%20", " ");

            boolean fileExist = FileUtilities.nameToFile(fileNameString, null)
                .exists();
            boolean askForOverwrite = actor.confirmOverwrite.getExpression()
                .equals("true");

            if (fileExist && askForOverwrite) {
                tmpStream.appendCodeBlock("confirmOverwrite");
            }

            if (actor.append.getExpression().equals("true")) {
                tmpStream.appendCodeBlock("openForAppend");
            } else {
                tmpStream.appendCodeBlock("openForWrite");
            }
        }

        return processCode(tmpStream.toString());
    }
    
    /** Get the files needed by the code generated for the
     *  LineWriter actor.
     *  @return A set of strings that are names of the files
     *   needed by the code generated for the LineWriter actor.
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
