/* A transformer that writes a makefile that that can be used to run a model

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/


package ptolemy.copernicus.kernel;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.gui.JNLPUtilities;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;

import soot.HasPhaseOptions;
import soot.PhaseOptions;
import soot.SceneTransformer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
A transformer that writes a makefile that can be used to run a model
that has been code generated.
<p>For a model called Foo, we generate Foo/makefile.
in the directory named by the outDir parameter.

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.1
*/
public class MakefileWriter extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private MakefileWriter(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     * the given model. The model is assumed to already have been
     * properly initialized so that resolved types and other static
     * properties of the model can be inspected.
     */
    public static MakefileWriter v(CompositeActor model) {
        return new MakefileWriter(model);
    }

    public String getPhaseName() {
        return "";
    }

    public String getDefaultOptions() {
        return "templateDirectory:" +
            TEMPLATE_DIRECTORY_DEFAULT;
    }

    public String getDeclaredOptions() {
        return 
          "_generatorAttributeFileName outDir targetPackage templateDirectory";
    }

    /** Add a makefile substitution from the given name to the given value.
     */
    public static void addMakefileSubstitution(String name, String value) {
        _addedSubstitutions.put(name, value);
    }

    /** Convert targetPackage "foo/bar" to codeBase
     *  "../../.."
     *  @param targetPackage The package where we are creating the code
     *  @param outputDirectory The directory where we are producing code.
     *  @param ptIIDirectory The Ptolemy II directory, usually the value
     *  of $PTII
     *  @return The codebase.  If the codebase is ".", then we may
     *  want to copy jar files.
     */
    public static String codeBase(String targetPackage,
            String outputDirectory,
            String ptIIDirectory) {
        // There is something a little bit strange
        // here, since we actually create the code in a sub
        // package of _targetPackage We could rename the
        // targetPackage parameter to parentTargetPackage but I'd
        // rather keep things uniform with the other generators?

        int start = targetPackage.indexOf('.');
        // codeBase has one more level than targetPackage.
        StringBuffer buffer = new StringBuffer("..");
        while (start != -1) {
            buffer.append("/..");
            start = targetPackage.indexOf('.', start + 1);
        }
        String codeBase = buffer.toString();

        if (JNLPUtilities.isRunningUnderWebStart()) {
            // If we are under WebStart, we always copy jar files
            // because under WebStart the jar files have munged names,
            // and the applet will not find them even if
            codeBase = ".";
        } else {
            try {
                if (!_isSubdirectory(ptIIDirectory, outputDirectory)) {
                    // System.out.println("'" + outputDirectory + "' is not a "
                    //        + "subdirectory of '" + ptIIDirectory + "', so "
                    //        + "we copy the jar files and set the "
                    //        + "codebase to '.'");
                    codeBase = ".";
                } else {
                    if (codeBase.equals("..")) {
                        if (outputDirectory.startsWith(ptIIDirectory)) {
                            // If targetPackage is short, i.e. "Butterfly",
                            // but we are writing to a subdirectory of
                            // $PTII, then we should adjust the classpath
                            // accordingly.

                            // FIXME: separator could be \ instead /
                            int start2 = outputDirectory.indexOf('/',
                                    ptIIDirectory.length() + 2);
                            StringBuffer buffer2 = new StringBuffer("..");
                            while (start2 != -1) {
                                buffer2.append("/..");
                                start2 = outputDirectory.indexOf('/',
                                        start2 + 1);
                            }
                            codeBase = buffer2.toString();
                            System.out.println("MakefileWriter: codeBase was "
                                    + ".., recalculated to " + codeBase); 

                        }
                    } else {
                        System.out.println("WARNING: codeBase == .., which "
                                + "usually means that there will be a problem "
                                + "finding the jar files.  Resetting codeBase "
                                + "to ., which will copy the jars");
                        codeBase = ".";
                    }
                }
            } catch (IOException ex) {
                System.out.println("_isSubdirectory threw an exception: "
                        + ex);
                ex.printStackTrace();
            }
        }
        return codeBase;
    }


    /** Generate a makefile to that can be used to run the generated code.
     *  <p>For example, if the model is called MyModel, and
     *  this phase is called with:
     *  <pre>
     *        -p wjtp.makefileWriter targetPackage:foo.bar,outdir:c:/tmp,templateDirectory:bif
     *  </pre>
     *  Then we will create the directory c:/tmp/foo/bar/MyModel, read
     *  in $PTII/bif/makefile.in and generate c:/tmp/foo/bar/makefile.
     *
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.appletWriter</code>.
     *  @param options The options Map.
     *  This transformer uses the following options:
     *  <dl>
     *  <dd>_generatorAttributeFileName
     *  <dd>The pathname to the {@link GeneratorAttribute} that contains
     *  the keys and values will we use to update the makefile template with.
     *  <dt>outDir
     *  <dd>The absolute path to the directory where the generated code
     *  will reside, for example:
     *  <code>c:/ptII/ptolemy/copernicus/applet/cg/Butterfly</code>
     *  <dt>targetPackage
     *  <dd>The package where the generated code will reside, for example:
     *  <code>ptolemy.copernicus.applet.cg.Butterfly</code>
     *  <dt>templateDirectory
     *  <dd>The directory where we should read the makefile.in file
     *
     *  </dl>
     */
    protected void internalTransform(String phaseName, Map options)
    {
        System.out.println("MakefileWriter.internalTransform("
                + phaseName + ", " + options + ")");

        //System.out.println(_model.description());

        // Read in the GeneratorAttribute and use it for substitution

        // Note that this option has a leading _
        _generatorAttributeFileName = PhaseOptions.getString(options,
                "_generatorAttributeFileName");

        if (_generatorAttributeFileName.length() == 0) {
            throw new InternalErrorException("Could not find "
                    + "_generatorAttributeFileName soot command line option. "
                    + "Usually, _generatorAttributeFileName contains the file "
                    + "name of the MoML that contains the GeneratorAttribute"
                    + "we are to use to create the makefile.  See "
                    + "ptolemy/copernicus/Copernicus.java for details");
        }

        System.out.println("MakefileWriter: parsing " +
                _generatorAttributeFileName);

        GeneratorAttribute generatorAttribute = null;
        try {
            // We need the GeneratorAttribute, but we already
            // filtered it out in KernelMain, and we updated
            // it in copernicus.kernel.Copernicus, so we
            // the values inside the GeneratorAttribute inside _model
            // are likely to be wrong.
            MoMLParser parser = new MoMLParser();

            CompositeActor toplevel;

            // Get the old filters, save them, add our own
            // filters, use them, remove our filters,
            // and then readd the old filters in the finally clause.
            // We do something
            // similar in GeneratorAttribute.updateModelAttributes()
            List oldFilters = parser.getMoMLFilters();
            parser.setMoMLFilters(null);
            try {
                // Handle Backward Compatibility.
                parser.addMoMLFilters(BackwardCompatibility.allFilters());

                // We don't call parseFile() here because it calls gets
                // the user.dir property.
                toplevel = (CompositeActor)parser
                    .parse(null,
                            new File(_generatorAttributeFileName).toURL());
            } finally {
                // Restore the saved momlfilters
                parser.setMoMLFilters(oldFilters);
            }

            generatorAttribute = (GeneratorAttribute)
                toplevel.getAttribute(Copernicus.GENERATOR_NAME,
                        GeneratorAttribute.class);
            if (generatorAttribute == null) {
                System.out.println("MakefileWriter: Warning, parsing '"
                        + _generatorAttributeFileName
                        + "' did not contain an attribute "
                        + " called '" + Copernicus.GENERATOR_NAME + "'"
                        + toplevel.exportMoML());
                generatorAttribute = new GeneratorAttribute(
                        toplevel, Copernicus.GENERATOR_NAME);
            }
        } catch (Exception ex) {
            throw new InternalErrorException(_model, ex, "Problem getting the"
                    + " _generator attribute");
        }



        _outputDirectory = PhaseOptions.getString(options, "outDir");
        if (!_outputDirectory.endsWith("/")) {
            _outputDirectory = _outputDirectory + "/";
        }

        // Create the directory where we will create the files.
        File outDirFile = new File(_outputDirectory);
        if (!outDirFile.isDirectory()) {
            outDirFile.mkdirs();
        }

        _targetPackage = PhaseOptions.getString(options, "targetPackage");

        _templateDirectory = PhaseOptions.getString(options, "templateDirectory");
        if (!_templateDirectory.endsWith("/")) {
            _templateDirectory = _templateDirectory + "/";
        }


        Map substituteMap;
        try {
            substituteMap = Copernicus.newMap(generatorAttribute);
            substituteMap.put("@outDir@", _outputDirectory);
            substituteMap.put("@targetPackage@", _targetPackage);
            substituteMap.put("@templateDirectory@", _templateDirectory);
            substituteMap.putAll(_addedSubstitutions);
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(_model, ex,
                    "Problem generating substitution map from "
                    + generatorAttribute);
        }

        try {
            System.out.println("MakefileWriter: reading '"
                    + _templateDirectory + "makefile.in'\n\t writing '"
                    + _outputDirectory + "makefile'");

            Copernicus.substitute(_templateDirectory + "makefile.in",
                    substituteMap,
                    _outputDirectory + "makefile");
        } catch (Exception ex) {
            // This exception tends to get eaten by soot, so we print as well.
            System.err.println("Problem writing makefile:" + ex);
            ex.printStackTrace();
            throw new InternalErrorException(_model, ex,
                    "Problem writing the makefile");
        }

        // Obfuscation script is optional
        BufferedReader inputFile = null;
        String obfuscateTemplate = _templateDirectory
            + "obfuscateScript.jos.in";
        try {
            inputFile = Copernicus.openAsFileOrURL(obfuscateTemplate);
        } catch (IOException ex) {
            System.out.println("Note: Optional obfuscation template not "
                    + "found (This can be ignored): "  + ex);
        }
        if (inputFile != null) {
            try {
                Copernicus.substitute(inputFile, substituteMap,
                        _outputDirectory + "obfuscateScript.jos");
            } catch (Exception ex) {
                // This exception tends to get eaten by soot, so we print
                System.err.println("Problem writing obfuscation script:" + ex);
                ex.printStackTrace();
                throw new InternalErrorException(_model, ex,
                        "Problem writing the makefile");
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Return true if _possibleSubdirectory is a subdirectory of parent. */
    private static boolean _isSubdirectory(String parent,
            String possibleSubdirectory)
            throws IOException {
        //System.out.println("_isSubdirectory: start \n\t" + parent + "\n\t" +
        //                           possibleSubdirectory);
        File parentFile = new File(parent);
        File possibleSubdirectoryFile = new File(possibleSubdirectory);
        if (parentFile.isFile() || possibleSubdirectoryFile.isFile()) {
            throw new IOException ("'" + parent + "' or '"
                    + possibleSubdirectory + "' is a file, "
                    + "it should be a directory");
        }
        String parentCanonical = parentFile.getCanonicalPath();
        String possibleSubdirectoryCanonical =
            possibleSubdirectoryFile.getCanonicalPath();
        // System.out.println("\n\n_isSubdirectory: \n\t"
        //                   + parentCanonical + "\n\t"
        //                   + possibleSubdirectoryCanonical);
        return possibleSubdirectoryCanonical.startsWith(parentCanonical);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // A map of additional substitutions.
    private static Map _addedSubstitutions = new HashMap();

    // The relative path to $PTII, for example "../../..".
    private String _codeBase;

    // The file name of the MoML file that contains the GeneratorAttribute
    // that contains the key/value pairs we will use when substituting
    // in the makefile.
    private String _generatorAttributeFileName;

    // The model we are generating code for.
    private CompositeActor _model;

    // The full path to the directory where we are creating the model
    private String _outputDirectory;

    // The sanitized modelName
    private String _sanitizedModelName;

    // The value of the ptolemy.ptII.dir property.
    private String _ptIIDirectory;

    // The user directory where we are writing.  _ptIIUserDirectory
    // will be a parent directory of _outputDirectory.
    private String _ptIIUserDirectory;

    // Map used to map @model@ to MyModel.
    private Map _substituteMap;

    // The parent package relative to $PTII to generate the code in
    // The code itself is generated in a child package of the parent package
    // with the same name as the model.  So if the _targetPackage
    // is foo.bar, and the model is MyModel, we will create the code
    // in foo.bar.MyModel.
    private String _targetPackage;

    // The directory that contains the templates (makefile.in,
    // model.htm.in, modelApplet.htm.in)
    private String _templateDirectory;

    // Initial default for _templateDirectory;
    private final String TEMPLATE_DIRECTORY_DEFAULT =
    "ptolemy/copernicus/kernel/";
}

