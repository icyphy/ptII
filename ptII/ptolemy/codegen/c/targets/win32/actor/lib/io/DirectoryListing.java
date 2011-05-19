/* A code generation helper class for actor.lib.io.DirectoryListing

 @Copyright (c) 2006-2008 The Regents of the University of California.
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
package ptolemy.codegen.c.targets.win32.actor.lib.io;

import java.util.ArrayList;
import java.util.Set;

import ptolemy.codegen.c.actor.lib.io.FileReader;
import ptolemy.codegen.c.kernel.CCodeGeneratorHelper;
import ptolemy.kernel.util.IllegalActionException;

/**
 * A Win32-specific helper class for ptolemy.actor.lib.io.DirectoryListing.
 *
 * @author Man-Kit Leung, Tony Huang
 * @version $Id$
 * @since Ptolemy II 7.0
 * @Pt.ProposedRating Red (mankit)
 * @Pt.AcceptedRating Red (mankit)
 */
public class DirectoryListing extends CCodeGeneratorHelper {
    /**
     * Construct the DirectoryListing helper.
     * @param actor the associated actor.
     */
    public DirectoryListing(ptolemy.actor.lib.io.DirectoryListing actor) {
        super(actor);
    }

    /**
     * Generate fire code.
     * Get the file path from the actor's directoryOrURL parameter. Read the
     * <code>initBlock</code> from DirectoryListing.c and pass the file path
     * string as an argument to code block. Replace macros with their values
     * and return the processed code string.
     * @return The processed code string.
     * @exception IllegalActionException If the file path parameter is invalid
     *  or the code stream encounters an error in processing the specified code
     *  block(s).
     */
    protected String _generateFireCode() throws IllegalActionException {
        super._generateFireCode();

        ptolemy.actor.lib.io.DirectoryListing actor = (ptolemy.actor.lib.io.DirectoryListing) getComponent();
        String fileNameString = FileReader.getFileName(actor.directoryOrURL);
        ArrayList args = new ArrayList();
        args.add(fileNameString);
        _codeStream.appendCodeBlock("fireBlock", args);
        return processCode(_codeStream.toString());
    }

    /**
     * Get the files needed by the code generated for the
     * DirectoryListing actor.
     * @return A set of strings that are names of the files
     *  needed by the code generated for the DirectoryListing actor.
     * @exception IllegalActionException Not Thrown in this subclass.
     */
    public Set getHeaderFiles() throws IllegalActionException {
        Set files = super.getHeaderFiles();
        files.add("<stdio.h>");
        //#include <tchar.h>
        //#include <strsafe.h>        
        return files;
    }
}
