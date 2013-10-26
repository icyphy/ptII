/* Interface indicating support for exporting an HTML file and supporting files.

 Copyright (c) 2011-2013 The Regents of the University of California.
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

package ptolemy.vergil.basic;

import java.awt.print.PrinterException;
import java.io.IOException;
import java.io.Writer;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// HTMLExportable
/**
 * Interface for parameters and attribute
 * indicating support for exporting an HTML file and supporting files.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface HTMLExportable {

    /** Export to HTML as given by the parameters.
     *  Implementers should write an "index.html" file plus any
     *  required supporting files in the directory given in the parameters.
     *  The caller is responsible for checking with the user whether
     *  any contents of the specified directory can be overwritten.
     *  @param parameters The parameters for the export.
     *  @param writer The writer to use the write the HTML. If this is null,
     *   then the implementer should create an index.html file in the
     *   directory given by the directoryToExportTo field of the parameters.
     *  @exception IOException If unable to write any files.
     *  @exception PrinterException If unable to write associated files.
     *  @exception IllegalActionException If something goes wrong.
     */
    public void writeHTML(ExportParameters parameters, Writer writer)
            throws PrinterException, IOException, IllegalActionException;
}
