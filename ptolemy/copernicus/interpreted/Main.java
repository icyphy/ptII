/* Main for interpreted generation, that is no code generation at all, just save the model as a .xml file

 Copyright (c) 2001-2014 The Regents of the University of California.
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
package ptolemy.copernicus.interpreted;

import ptolemy.copernicus.kernel.GeneratorAttribute;
import ptolemy.copernicus.kernel.KernelMain;
import ptolemy.copernicus.kernel.MakefileWriter;
import soot.Pack;
import soot.PackManager;

///////////////////////////////////////////////////////////////////
//// Main

/**
 Read in a MoML model and generate a .xml file
 that will run the model as in standard interpreted mode.

 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class Main extends KernelMain {
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add transforms to the Scene.
     */
    @Override
    public void addTransforms() {
        Pack pack = PackManager.v().getPack("wjtp");

        // Generate the makefile files in outputDirectory
        addTransform(pack, "wjtp.makefileWriter", MakefileWriter.v(_toplevel),
                "_generatorAttributeFileName:" + _generatorAttributeFileName
                + " targetPackage:" + _targetPackage
                + " templateDirectory:" + _templateDirectory
                + " outputDirectory:" + _outputDirectory);

        // Generate the interpreted files in outputDirectory
        addTransform(pack, "wjtp.interpretedWriter",
                InterpretedWriter.v(_toplevel), " outputDirectory:"
                        + _outputDirectory);
    }

    /** Parse any code generator specific arguments.
     */
    @Override
    protected String[] _parseArgs(GeneratorAttribute attribute)
            throws Exception {
        _targetPackage = attribute.getParameter("targetPackage");
        _templateDirectory = attribute.getParameter("templateDirectory");
        //_watchDogTimeout = attribute.getParameter("watchDogTimeout");
        _outputDirectory = attribute.getParameter("outputDirectory");
        _generatorAttributeFileName = attribute
                .getParameter("generatorAttributeFileName");

        //String sootArgs = attribute.getParameter("sootArgs");
        return new String[0];
    }

    private static String _generatorAttributeFileName = "unsetParameter";

    //private static String _watchDogTimeout = "unsetParameter";

    private static String _targetPackage = "unsetParameter";

    private static String _templateDirectory = "ptolemy/copernicus/interpreted";

    private static String _outputDirectory = "unsetParameter";
}
