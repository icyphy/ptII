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

import java.awt.Color;
import java.io.File;

import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;


///////////////////////////////////////////////////////////////////
//// ExportParameters
/**
 * A data structure containing export parameters.
 * This data structure will typically be provided by an instance
 * of {@link WebExportParameters}, which is an
 * {@link Attribute} that can be stored in a model.
 *
 * @author Christopher Brooks and Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class ExportParameters {
    
    /** Construct an instance of this data structure.
     *  The default values are:
     *  <br/> null for backgroundColor,
     *  <br/> false for copyJavaScriptFiles,
     *  <br/> null for directoryToExportTo,
     *  <br/> false for openCompositesBeforeExport,
     *  <br/> false for runBeforeExport,
     *  <br/> and true for showInBrowser.
     */
    protected ExportParameters() {
        this(null);
    }

    /** Construct an instance of this data structure.
     *  The default values are:
     *  <br/> null for backgroundColor,
     *  <br/> false for copyJavaScriptFiles,
     *  <br/> false for openCompositesBeforeExport,
     *  <br/> false for runBeforeExport,
     *  <br/> and true for showInBrowser.
     *  @param directoryToExportTo The directory to export to.
     */
    public ExportParameters(File directoryToExportTo) {
        this.directoryToExportTo = directoryToExportTo;
        backgroundColor = null;        
        openCompositesBeforeExport = false;
        runBeforeExport = false;
        showInBrowser = true;
        copyJavaScriptFiles = false;
        _jsCopier = null;
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
        openCompositesBeforeExport = template.openCompositesBeforeExport;
        runBeforeExport = template.runBeforeExport;
        showInBrowser = template.showInBrowser;
        copyJavaScriptFiles = template.copyJavaScriptFiles;
        _jsCopier = template._jsCopier;
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
    
    /** The directory to export to.
     */
    public File directoryToExportTo;
    
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
    
    ///////////////////////////////////////////////////////////////////
    ////                         protected fields                  ////

    /** Directory into which JavaScript and related files have been written,
     *  or null if they are not being copied.
     */
    protected NamedObj _jsCopier = null;
}
