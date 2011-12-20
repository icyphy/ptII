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

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;


///////////////////////////////////////////////////////////////////
//// WebExportParameters
/**
 * Container for parameters that customize web export.
 *
 * @author Christopher Brooks and Edward A. Lee
 * @version $Id$
 * @since Ptolemy II 8.1
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class WebExportParameters extends Attribute {
    
    /** Construct an attribute with the given name contained by the specified
     *  entity.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public WebExportParameters(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        directoryToExportTo = new FileParameter(this, "directoryToExportTo");
        Parameter allowFiles = new Parameter(directoryToExportTo, "allowFiles");
        allowFiles.setExpression("false");
        allowFiles.setVisibility(Settable.NONE);
        Parameter allowDirectories = new Parameter(directoryToExportTo, "allowDirectories");
        allowDirectories.setExpression("true");
        allowDirectories.setVisibility(Settable.NONE);

        backgroundColor = new ColorAttribute(this, "backgroundColor");
        
        openCompositesBeforeExport = new Parameter(this, "openCompositesBeforeExport");
        openCompositesBeforeExport.setTypeEquals(BaseType.BOOLEAN);
        openCompositesBeforeExport.setExpression("false");
        
        runBeforeExport = new Parameter(this, "runBeforeExport");
        runBeforeExport.setTypeEquals(BaseType.BOOLEAN);
        runBeforeExport.setExpression("false");
        
        showInBrowser = new Parameter(this, "showInBrowser");
        showInBrowser.setTypeEquals(BaseType.BOOLEAN);
        showInBrowser.setExpression("true");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** Background color. By default this is blank, which indicates
     *  to use the background color of the model.
     */
    public ColorAttribute backgroundColor;
    
    /** The directory to export to. If a relative name is given,
     *  then it is relative to the location of the model file.
     *  By default, this is blank,
     *  which will result in writing to a directory with name
     *  equal to the sanitized name of the model,
     *  and the directory will be contained in the same location
     *  where the model that contains this attribute is stored.
     */
    public FileParameter directoryToExportTo;
    
    /** If true, hierarchically open all composite actors
     *  in the model before exporting (so that these also
     *  get exported, and hyperlinks to them are created).
     *  This is a boolean that defaults to false.
     */
    public Parameter openCompositesBeforeExport;

    /** If true, run the model before exporting (to open plotter
     *  or other display windows that get exported). Note that
     *  it is important the model have a finite run. This is a
     *  boolean that defaults to false.
     */
    public Parameter runBeforeExport;
    
    /** If true, open a web browser to display the resulting
     *  export. This is a boolean that defaults to true.
     */
    public Parameter showInBrowser;
    
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if the parameter values are different from the defaults.
     *  @return True if the parameter values are different from the defaults.
     */
    public boolean parametersChanged() {
        return !_isMoMLSuppressed(1);
    }
}
