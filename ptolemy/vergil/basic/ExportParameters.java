/* Interface for parameters that provide web export content.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

import java.awt.Color;
import java.io.File;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.basic.export.web.WebExportParameters;

///////////////////////////////////////////////////////////////////
//// ExportParameters
/**
 * A data structure containing parameters for exporting a
 * Ptolemy model to a web page.
 * This data structure will typically be provided by an instance
 * of {@link WebExportParameters}, which is an
 * {@link Attribute} that can be stored in a model.
 *
 * @author Christopher Brooks and Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ExportParameters {

    /** Construct an instance of this data structure with
     *  default values, which are
     *  null for backgroundColor,
     *  false for copyJavaScriptFiles,
     *  null for directoryToExportTo,
     *  false for openCompositesBeforeExport,
     *  false for runBeforeExport,
     *  and true for showInBrowser.
     */
    public ExportParameters() {
        this(null);
    }

    /** Construct an instance of this data structure with
     *  default values, which are
     *  null for backgroundColor,
     *  false for copyJavaScriptFiles,
     *  false for deleteFilesOnExit,
     *  false for openCompositesBeforeExport,
     *  false for runBeforeExport,
     *  true for showInBrowser,
     *  and empty String for HTMLPathForFiles.
     *  @param directoryToExportTo The directory to export to.
     */
    public ExportParameters(File directoryToExportTo) {
        this.directoryToExportTo = directoryToExportTo;
        backgroundColor = null;
        deleteFilesOnExit = false;
        imageFormat = "gif";
        openCompositesBeforeExport = false;
        runBeforeExport = false;
        showInBrowser = true;
        copyJavaScriptFiles = false;
        _jsCopier = null;
        HTMLPathForFiles = "";
    }

    /** Construct an instance of this data structure that is
     *  identical to the one given except for directoryToExportTo,
     *  which is as specified.
     *  @param directoryToExportTo The directory to export to.
     *  @param template The template parameters.
     */
    public ExportParameters(File directoryToExportTo, ExportParameters template) {
        this.directoryToExportTo = directoryToExportTo;
        backgroundColor = template.backgroundColor;
        deleteFilesOnExit = template.deleteFilesOnExit;
        imageFormat = template.imageFormat;
        openCompositesBeforeExport = template.openCompositesBeforeExport;
        runBeforeExport = template.runBeforeExport;
        showInBrowser = template.showInBrowser;
        copyJavaScriptFiles = template.copyJavaScriptFiles;
        _jsCopier = template._jsCopier;
        HTMLPathForFiles = template.HTMLPathForFiles;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the composite entity that
     *  is copying JavaScript and
     *  related files; return null if they are not being copied.
     *  Note that this would ideally be protected, as it is used
     *  only by ExportHTMLAction, but to avoid package dependencies,
     *  we have to make it public.
     *  @return The composite entity that is set to copy JavaScript and related files,
     *  or null if they are not being copied.
     *  @see #setJSCopier(NamedObj)
     */
    public NamedObj getJSCopier() {
        return _jsCopier;
    }

    /** Specify the composite entity responsible for copying JavaScript and
     *  related files. Set to null if they are not being copied.
     *  This will normally be the same as the model for which these
     *  parameters apply, its container, or a container above that
     *  in the hierarchy.
     *  Note that this would ideally be protected, as it is used
     *  only by ExportHTMLAction, but to avoid package dependencies,
     *  we have to make it public.
     *  @param copier The composite entity responsible for
     *   copying JavaScript and related files.
     *  @see #getJSCopier()
     */
    public void setJSCopier(NamedObj copier) {
        _jsCopier = copier;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public fields                     ////

    /** Background color. A null value indicates
     *  to use the background color of the model.
     */
    public Color backgroundColor;

    /** If true, then make an exported web page stand alone.
     *  Instead of referencing JavaScript and image files on the
     *  ptolemy.org website, if this parameter is true, then the
     *  required files will be copied into the target directory.
     *  This is a boolean that defaults to false.
     */
    public boolean copyJavaScriptFiles;

    /** If true, files generated will be deleted when the JVM terminates.
     */
    public boolean deleteFilesOnExit;

    /** The directory to export to.
     */
    public File directoryToExportTo;

    /** The path to use for accessing the file in the HTML code.  This can
     *  differ from the physical location of the file in the file system
     *  depending on how the exporter accesses the files.  For example,
     *  an HttpService uses a URL as a path since the WebServer has a
     *  resource handler to serve files.  The HTML to include e.g. an image
     *  would be:
     *  <pre>
     *  &lt;img src="/files/imagename.gif"/&gt;
     *  </pre>
     *  even though the image is stored in $PTT/org/ptolemy/ptango/temp, since
     *  the resource handler is mapped to http://hostname:port/servicename/files.
     */
    public String HTMLPathForFiles;

    /** The image format to use, which can be one of "gif" (the default),
     *  "png", or "jpg".
     */
    public String imageFormat;

    /** If true, hierarchically open all composite actors
     *  in the model before exporting (so that these also
     *  get exported, and hyperlinks to them are created).
     */
    public boolean openCompositesBeforeExport;

    /** If true, run the model before exporting (to open plotter
     *  or other display windows that get exported). Note that
     *  it is important the model have a finite run.
     */
    public boolean runBeforeExport;

    /** If true, open a web browser to display the resulting
     *  export.
     */
    public boolean showInBrowser;

    /** If true, use the server-side includes of the Ptolemy website. */
    public boolean usePtWebsite;

    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////

    /** Directory into which JavaScript and related files have been written,
     *  or null if they are not being copied.
     */
    protected NamedObj _jsCopier = null;
}
