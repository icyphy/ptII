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

package ptolemy.vergil.basic.export.html;

import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Nameable;


///////////////////////////////////////////////////////////////////
//// WebExportable
/**
 * Interface for parameters that provide web export content.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface WebExportable extends Nameable {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of this object.
     *  This may include, for example, HTML or header
     *  content, including for example JavaScript definitions that
     *  may be needed by the area attributes.
     *  @param exporter The web exporter to be used.
     *  @exception IllegalActionException If something is wrong with the
     *   specification of outside content.
     */
    public void provideContent(WebExporter exporter) throws IllegalActionException;

    /** Provide content to the specified web exporter to be
     *  included in a web page for the container of
     *  the container of this object. For example, if this
     *  object is contained by an {@link Entity}, then 
     *  this method provides content for a web page for the container
     *  of the entity.
     *  This may include, for example, attributes for
     *  the area element for the portion of the image
     *  map corresponding to the container of this object.
     *  But it can also include any arbitrary HTML or header
     *  content, including for example JavaScript definitions that
     *  may be needed by the area attributes.
     *  @throws IllegalActionException If something is wrong with the
     *   specification of outside content.
     */
    public void provideOutsideContent(WebExporter exporter) throws IllegalActionException;
}
