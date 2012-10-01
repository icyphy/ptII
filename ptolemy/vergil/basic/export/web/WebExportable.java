/* Interface for parameters that provide web export content.

 Copyright (c) 2011 The Regents of the University of California.
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

package ptolemy.vergil.basic.export.web;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;


///////////////////////////////////////////////////////////////////
//// WebExportable
/**
 * Interface for parameters that provide web export content.
 *
 * @author Edward A. Lee, Elizabeth Latronico
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface WebExportable extends Nameable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the Mime type of the content (for example, text/html).
     * The Mime type is used by browsers to determine how to render the content.
     * The Mime type is set using HttpResponse's setContentType() method.
     * Please see the list of Mime types here, in the Media type and subtype(s)
     * field of the table:
     * http://reference.sitepoint.com/html/mime-types-full
     *
     * @return The Mime type of the content (for example, text/html)
     */
    public String getMimeType();

    /** Returns true if the content in the WebExporter for this object should
     *  be overwritten; false if the original content should be kept.
     *  Note that all objects from the WebExportable are treated in a uniform
     *  manner.  For example, it's not possible to overwrite some objects'
     *  values but keep other objects' values from the same WebExportable.
     *
     * @return True if the content in the WebExporter for this object should
     *  be overwritten; false if the original content should be kept
     */
    public boolean isOverwriteable();

    /** Provide content to the specified web exporter.
     *  This may include, for example, HTML pages and fragments, Javascript
     *  function definitions and calls, CSS styling, and more. Throw an
     *  IllegalActionException if something is wrong with the web content.
     *
     *  @param exporter The web exporter to be used
     *  @exception IllegalActionException If something is wrong with the web
     *  content.
     */
    public void provideContent(WebExporter exporter) throws
        IllegalActionException;
}
