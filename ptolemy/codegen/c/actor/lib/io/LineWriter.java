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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;

/**
 * A helper class for ptolemy.actor.lib.io.LineWriter.
 *
 * @author Jackie
 * @version $Id$
 * @since Ptolemy II 6.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class LineWriter extends CCodeGeneratorHelper {
    /**
     * Constructor method for the LineWriter helper.
     * @param actor the associated actor.
     */
    public LineWriter(ptolemy.actor.lib.io.LineWriter actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * The method reads in <code>writeLine</code> from LineWriter.c,
     * replaces macros with their values and appends to the given code buffer.
     * @return The generated code.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateFireCode() throws IllegalActionException {
        StringBuffer code = new StringBuffer();
        code.append(super.generateFireCode());
        code.append(_generateBlockCode("writeLine"));
        return code.toString();
    }

    /**
     * Generate initialization code.
     * This method first checks if "System.out" is the file parameter. If so,
     * it reads from LineWriter.c for the <code>openForStdout</code> block,
     * which is code for opening standard output stream.  Then, the method
     * checks the actor's confirmOverwrite and appends parameters, reads the
     * <code>confirmOverwrite</code>, <code>openForAppend</code>, and
     * <code>openForWrite</code> blocks accordingly.  Then it replaces macros
     * with their values and returns the resulting code string.
     * @return The processed code string.
     * @exception IllegalActionException If the code stream encounters an
     *  error in processing the specified code block(s).
     */
    public String generateInitializeCode() throws IllegalActionException {
        super.generateInitializeCode();

        ptolemy.actor.lib.io.LineWriter actor = (ptolemy.actor.lib.io.LineWriter) getComponent();
        _codeStream.clear();

        if (actor.fileName.getExpression().equals("System.out")) {
            _codeStream.appendCodeBlock("openForStdout");
        } else {
            String fileNameString = FileReader.getFileName(actor.fileName);

            fileNameString = FileReader.getFileName(actor.fileName);
            ArrayList args = new ArrayList();
            args.add(fileNameString);
            
            if (actor.confirmOverwrite.getExpression().equals("true")) {
                _codeStream.appendCodeBlock("confirmOverwrite", args);
            }

            if (actor.append.getExpression().equals("true")) {
                _codeStream.appendCodeBlock("openForAppend", args);
            } else {
                _codeStream.appendCodeBlock("openForWrite", args);
            }
        }

        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the LineWriter actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the LineWriter actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = new HashSet();
        files.add("<stdio.h>");
        return files;
    }
}
