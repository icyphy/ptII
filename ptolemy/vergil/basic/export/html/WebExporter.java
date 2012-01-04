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

import java.io.File;

import ptolemy.actor.gui.PtolemyFrame;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.ExportParameters;


///////////////////////////////////////////////////////////////////
//// WebExporter
/**
 * Interface for an object that provides a web exporting service
 * for a Ptolemy II model.
 *
 * @author Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface WebExporter {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add HTML content at the specified position.
     *  The position is expected to be one of "head", "start", or "end",
     *  where anything else is interpreted as equivalent to "end".
     *  If <i>onceOnly</i> is true, then if identical content has
     *  already been added to the specified position, then it is not
     *  added again.
     *  @param position The position for the content.
     *  @param onceOnly True to prevent duplicate content.
     *  @param content The content to add.
     */
    public void addContent(String position, boolean onceOnly, String content);
    
    /** Define an attribute to be included in the HTML area element
     *  corresponding to the region of the image map covered by
     *  the specified object. For example, if an <i>attribute</i> "href"
     *  is added, where the <i>value</i> is a URI, then the
     *  area in the image map for the specified object will include
     *  a hyperlink to the specified URI. If the specified object
     *  already has a value for the specified attribute, then
     *  the previous value is replaced by the new one.
     *  @param object The object for which area elements are being added.
     *  @param attribute The attribute to add to the area element.
     *  @param value The value of the attribute.
     *  @param overwrite If true, overwrite any previously defined value for
     *   the specified attribute. If false, then do nothing if there is already
     *   an attribute with the specified name.
     *  @return True if the specified attribute and value was defined (i.e.,
     *   if there was a previous value, it was overwritten).
     */
    public boolean defineAreaAttribute(
            NamedObj object, String attribute, String value, boolean overwrite);

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
