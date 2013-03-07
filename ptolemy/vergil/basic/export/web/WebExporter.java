/* Interface for parameters that provide web export content.

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

package ptolemy.vergil.basic.export.web;

import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.vergil.basic.ExportParameters;

///////////////////////////////////////////////////////////////////
//// WebExporter
/**
 * Interface for an object that provides a web exporting service
 * for a Ptolemy II model.
 *
 * @author Edward A. Lee, Elizabeth Latronico
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface WebExporter {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add the given web content as a new attribute.
     *  If <i>onceOnly</i> is true, then if identical content has
     *  already been added, then it is not added again.
     *  @param webAttribute The WebAttribute containing the content to add.
     *  @param overwrite If true, overwrite any previously defined value for
     *   the specified attribute. If false, then do nothing if there is already
     *   an attribute with the specified name.
     *  @return True if the specified attribute and value was defined (i.e.,
     *   if there was a previous value, it was overwritten).
     */
    public boolean defineAttribute(WebAttribute webAttribute, boolean overwrite);

    /** Add the given web content as a new element to the specified position.
     *  The position is expected to be one of "head", "start", or "end",
     *  where anything else is interpreted as equivalent to "end".
     *  If <i>onceOnly</i> is true, then if identical content has
     *  already been added to the specified position, then it is not
     *  added again.
     *  @param webElement The WebElement containing the content to add and the
     *  position.
     *  @param onceOnly True to prevent duplicate content.
     */
    public void defineElement(WebElement webElement, boolean onceOnly);

    /** During invocation an export, return
     *  the parameters of the export. If not currently doing an export, return null.
     *  @return The directory being written to.
     */
    public ExportParameters getExportParameters();

    /** The frame (window) being exported.
     *  @return The frame being exported.
     */
    public PtolemyFrame getFrame();

    /** Set the title to be used for the page being exported.
     *  @param title The title.
     *  @param showInHTML True to produce an HTML title prior to the model image.
     */
    public void setTitle(String title, boolean showInHTML);
}
