/* A transformer that writes an applet version of the model.

 Copyright (c) 2001-2003 The Regents of the University of California.
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


package ptolemy.copernicus.applet;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.gui.SizeAttribute;
import ptolemy.actor.gui.WindowPropertiesAttribute;
import ptolemy.copernicus.kernel.Copernicus;
import ptolemy.copernicus.kernel.MakefileWriter;
import ptolemy.data.ArrayToken;
import ptolemy.data.IntToken;
import ptolemy.data.IntMatrixToken;
import ptolemy.data.RecordToken;
import ptolemy.data.expr.UtilityFunctions;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.util.ClassUtilities;
import ptolemy.util.StringUtilities;

import soot.HasPhaseOptions;
import soot.PhaseOptions;
import soot.SceneTransformer;

import java.net.URL;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
A transformer that writes an applet version of a model.
For a model called Foo, we generate Foo/makefile, Foo/Foo.xml,
Foo/Foo.htm Foo/FooVergil.htm in the directory named by the
outDir parameter.

<p>Potential future enhancements
<menu>
<li> Optionally copy the necessary jar files to the target directory.
<li> Pull out the top level annotation and add the text to the web page.
</menu>
@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class AppletWriter extends SceneTransformer implements HasPhaseOptions {
    /** Construct a new transformer
     */
    private AppletWriter(CompositeActor model) {
        _model = model;
    }

    /** Return an instance of this transformer that will operate on
     * the given model. The model is assumed to already have been
     * properly initialized so that resolved types and other static
     * properties of the model can be inspected.
     */
    public static AppletWriter v(CompositeActor model) {
        return new AppletWriter(model);
    }

    public String getDefaultOptions() {
        return "templateDirectory:" +
            TEMPLATE_DIRECTORY_DEFAULT;
    }

    public String getDeclaredOptions() {
        return "targetPackage modelPath outDir templateDirectory";
    }


    public String getPhaseName() {
        return "";
    }

    /** Save the model as an applet.
     *  <p>For example, if the model is called MyModel, and
     *  this phase is called with:
     *  <pre>
     *        -p wjtp.appletWriter targetPackage:foo.bar
     *  </pre>
     *  Then we will create the directory $PTII/foo/bar/MyModel and
     *  place MyModel.xml, MyModel.htm, MyModelVergil.htm in that
     *  directory.
     *
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.appletWriter</code>.
     *  @param options The options Map.  This method uses the
     *  <code>targetPackage</code> option to specify package
     *  to generate code in.
     */
    protected void internalTransform(String phaseName, Map options)
    {

        System.out.println("AppletWriter.internalTransform("
                + phaseName + ", " + options + ")");

        // URL that names the model.
        _modelPath = PhaseOptions.getString(options, "modelPath");

        _outputDirectory = PhaseOptions.getString(options, "outDir");

        // Determine where $PTII is so that we can find the right directory.
        _ptIIDirectory = null;
        try {
            // NOTE: getProperty() will probably fail in applets, which
            // is why this is in a try block.
            // NOTE: This property is set by the vergil startup script.
            _ptIIDirectory = System.getProperty("ptolemy.ptII.dir");
        } catch (SecurityException security) {
            throw new InternalErrorException(null, security,
                    "Could not find "
                    + "'ptolemy.ptII.dir'"
                    + " property.  Vergil should be "
                    + "invoked with -Dptolemy.ptII.dir"
                    + "=\"$PTII\"");
        }

        // If the targetPackage is foo.bar, and the model is Bif,
        // the we will do mkdir $PTII/foo/bar/Bif/
        _targetPackage = PhaseOptions.getString(options, "targetPackage");

        // Determine the value of _domainJar, which is the
        // path to the domain specific jar, e.g. "ptolemy/domains/sdf/sdf.jar"

        System.out.println("AppletWriter: _model: " + _model);
        Director director = _model.getDirector();
        System.out.println("AppletWriter: director: " + director);
        String directorPackage = director.getClass().getPackage().getName();
        if (!directorPackage.endsWith(".kernel")) {
            System.out.println("Warning: the directorPackage does not end "
                    + "with '.kernel', it is :" + directorPackage);
        }

        _domainJar = _getDomainJar(directorPackage);

        _sanitizedModelName = StringUtilities.sanitizeName(_model.getName());


        _codeBase = MakefileWriter.codeBase(_targetPackage,
                _outputDirectory, _ptIIDirectory);

        // Create the directory where we will create the files.
        File outDirFile = new File(_outputDirectory);
        if (!outDirFile.isDirectory()) {
            // MakefileWriter should have already created the directory
            outDirFile.mkdirs();
        }

        try {
	    // If the code base is the current directory, then we
	    // copy the jar files over and set the value of _domainJar
	    // to the names of the jar files separated by commas.
	    // We always want to generate the list of jar files so
	    // that if for example we use fsm, then we are sure to
	    // include diva.jar
	    StringBuffer jarFilesResults = new StringBuffer();
	    Iterator jarFiles = _findJarFiles(director).iterator();
	    while (jarFiles.hasNext()) {
		String jarFile = (String)jarFiles.next();
		if (jarFilesResults.length() > 0) {
		    jarFilesResults.append(",");
		}
		jarFilesResults.append(jarFile);
	    }
	    _modelJarFiles = jarFilesResults.toString();
        } catch (IOException ex) {
            // This exception tends to get eaten by soot, so we print as well.
            System.err.println("Problem writing makefile or html files:");
            ex.printStackTrace();
            throw new InternalErrorException(null, ex,
                    "Problem writing the makefile or htm files.");
        }

        // Get the size of the vergil window from the model.
        int appletWidth = 400;
        int appletHeight = 450;
        int vergilWidth = 600;
        int vergilHeight = 800;
        try {
            WindowPropertiesAttribute windowProperties =
                (WindowPropertiesAttribute) _model.getAttribute(
                        "_windowProperties");
            ArrayToken boundsToken =
                (ArrayToken)((RecordToken)windowProperties.getToken())
                .get("bounds");

            appletWidth = ((IntToken)boundsToken.getElement(2)).intValue();
            appletHeight = ((IntToken)boundsToken.getElement(3)).intValue();
        } catch (Exception ex) {
            System.out.println("Warning: Failed to get applet width "
                    + "and height, using defaults: " + ex.getMessage());
        }

        try {
            SizeAttribute vergilSize =
                (SizeAttribute) _model.getAttribute(
                        "_vergilSize");

            IntMatrixToken vergilSizeToken =
                (IntMatrixToken) vergilSize.getToken();

            vergilWidth = vergilSizeToken.getElementAt(0, 0);
            vergilHeight = vergilSizeToken.getElementAt(0, 1);
        } catch (Exception ex) {
            System.out.println("Warning: Failed to get vergil width "
                    + "and height, using defaults: " + ex.getMessage());
        }

        // The vergil applet shows the model and the top level window.
        vergilHeight += appletHeight;
        // Add 200 to the applet height to include the control panels.
        appletHeight += 200;

        // Set up the HashMap we will use when we read in files like
        // model.htm.in and search for strings like @codebase@ and
        // substitute in the value of _codeBase.
        _substituteMap = new HashMap();
        _substituteMap.put("@appletHeight@", Integer.toString(appletHeight));
        _substituteMap.put("@appletWidth@", Integer.toString(appletWidth));
        _substituteMap.put("@codeBase@", _codeBase);
        _substituteMap.put("@modelJarFiles@", _modelJarFiles);
        _substituteMap.put("@outDir@", _outputDirectory);
        _substituteMap.put("@sanitizedModelName@",
                _sanitizedModelName);
        _substituteMap.put("@ptIIDirectory@", _ptIIDirectory);
        _substituteMap.put("@vergilHeight@", Integer.toString(vergilHeight));
        _substituteMap.put("@vergilWidth@", Integer.toString(vergilWidth));

        // Print out the map for debugging purposes
        Iterator keys = _substituteMap.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String)keys.next();
            System.out.println("AppletWriter: '" + key + "' '" +
                    (String)_substituteMap.get(key) + "'");
        }

        // Generate the .xml file.
        String newModelFileName =
            _outputDirectory + "/" + _sanitizedModelName + ".xml";

        try {
            // Since we strip out the graphical information in
            // Copernicus.readInModel(), we need to copy the original
            // model so that the vergil applet has the layout info.
            File newModelFile = new File(newModelFileName);
            System.out.println("AppletWriter: about to write '"
                    + newModelFile + "'");

            // Avoid end of line and localization issues.
            URL modelURL = new URL(_modelPath);
            BufferedInputStream in = 
                new BufferedInputStream(modelURL.openStream());
            BufferedOutputStream out =
                new BufferedOutputStream(
                        new FileOutputStream(newModelFile));
            int c;

            while ((c = in.read()) != -1) {
                out.write(c);
            }
            // FIXME: need finally?
            in.close();
            out.close();

        } catch (IOException ex) {
            System.out.println("AppletWriter: WARNING: Problem reading '"
                    + _modelPath + "' and writing '" + newModelFileName
                    + "', instead we call exportMoML(), which will lose "
                    + "vergil layout information: "
                    + ex.getMessage());
            System.out.println("AppletWriter: about to write '"
                     + newModelFileName + "'");
            try {
                Writer modelFileWriter =
                    new BufferedWriter(
                            new OutputStreamWriter(
                                    new FileOutputStream(newModelFileName)));
                _model.exportMoML(modelFileWriter);
                // FIXME: need finally?
                modelFileWriter.close();
            } catch (IOException ex2) {
                // Rethrow original exception ex.
                throw new InternalErrorException(null, ex,
                        "Problem reading '" + _modelPath + "' or "
                        + "writing '" + newModelFileName + "'\n"
                        + "Also tried calling exportMoML():"
                        + ex2.getMessage());
            }
        }

        // The directory that contains the templates.
        // FIXME: this could be a Ptolemy parameter?

        //_templateDirectory =
        //    StringUtilities.substitute(PhaseOptions.getString(options,
        //            "templateDirectory"),
        //            "$PTII", _ptIIDirectory);
        _templateDirectory =
            PhaseOptions.getString(options, "templateDirectory");

        System.out.println("AppletWriter: _templateDirectory: '"
                + _templateDirectory + "'");

        // Read in the templates and generate new files.
        try {
            Copernicus.substitute(_templateDirectory + "model.htm.in",
                    _substituteMap,
                    _outputDirectory + "/"
                    + _sanitizedModelName + ".htm");
            Copernicus.substitute(_templateDirectory + "modelVergil.htm.in",
                    _substituteMap,
                    _outputDirectory + "/"
                    + _sanitizedModelName
                    + "Vergil.htm");

            // Copy $PTII/doc/default.css as well.
            File defaultStyleSheetDirectory =
                new File(_outputDirectory + "/doc");
            defaultStyleSheetDirectory.mkdirs();

            Copernicus.substitute(_templateDirectory + "default.css",
                    _substituteMap,
                    defaultStyleSheetDirectory.toString()
                    + "/default.css" );

        } catch (IOException ex) {
            // This exception tends to get eaten by soot, so we print as well.
            System.err.println("Problem writing makefile or html files:" + ex);
            ex.printStackTrace();
            throw new InternalErrorException(null, ex,
                    "Problem writing the makefile or htm files");
        }
    }

    // Return a Map that maps classes to jar files for all the AtomicEntities
    // and Directors in the model
    private Map _allAtomicEntityJars() {
        HashMap results = new HashMap();
        Iterator atomicEntities = _model.allAtomicEntityList().iterator();
        while (atomicEntities.hasNext()) {
            Object object = atomicEntities.next();
            results.put(object.getClass().getName(),
                    _getDomainJar(object.getClass().getPackage()
                            .getName()));

            if (object instanceof AtomicActor) {
                // Add in the Managers.
                results.put(((AtomicActor)object).getDirector().getClass()
                        .getName(),
                        _getDomainJar(((AtomicActor)object).getDirector()
                                .getClass().getPackage().getName()));
            }
        }
        return results;
    }



            
    // If jarFile exists, optionally copy it and return true.
    // If jarFile does not exist, return false.
    private boolean _copyPotentialJarFile(String jarFile, String className,
            HashSet jarFilesThatHaveBeenRequired) 
            throws IOException  {

        File potentialSourceJarFile =
            new File(_ptIIDirectory, jarFile);

        System.out.println("AppletWriter: className: " + className
                + "\tpotentialSourceJarFile: "
                + potentialSourceJarFile);

        if (potentialSourceJarFile.exists()) {
            jarFilesThatHaveBeenRequired.add(jarFile);
            if (_codeBase.equals(".")) {
                // If the codeBase is equal to the current directory,
                // we copy the jar file.


                // Ptolemy II development trees will have jar files
                // if 'make install' was run.
                _copyFile(_ptIIDirectory + File.separator + jarFile,
                        _outputDirectory, jarFile);
            }
            return true;
        }
        return false;
    }

    // find jar necessary jar files and optionally copy jar files into
    // _outputDirectory.  Note that we need look for jar files to find
    // diva.jar if we are using the FSM domain.
    // Return the jar files that have been copied
    private Set _findJarFiles(Director director)
            throws IOException {

        // In the perfect world, we would run tree shaking here, or
        // look up classes as resources.  However, if we are running
        // in a devel tree, then the ptII directory will be returned
        // as the resource when we look up a class, which is not
        // at all what we want.
        // appletviewer -J-verbose could be used for tree shaking.

        // We use a HashMap that maps class names to destination jar
        // files.

        Map classMap = _allAtomicEntityJars();

        classMap.put("ptolemy.actor.gui.MoMLApplet",
                "ptolemy/ptsupport.jar");
        // classMap.put("ptolemy.actor.lib.python.PythonScript",
        //                 "lib/jython.jar");
        // classMap.put("caltrop.ptolemy.actors.CalInterpreter",
        //                 "lib/ptCal.jar");
        classMap.put(director.getClass().getName(),
                _domainJar);
        classMap.put("ptolemy.vergil.MoMLViewerApplet",
                "ptolemy/vergil/vergilApplet.jar");
        // FIXME: unfortunately, vergil depends on FSM now.
        classMap.put("ptolemy.domains.fsm.kernel.FSMActor",
                "ptolemy/domains/fsm/fsm.jar");
        // FIXME: vergil.fsm.modal.ModalModel depends on CTStepSizeControlActor
        classMap.put("ptolemy.domains.ct.kernel.CTStepSizeControlActor",
                "ptolemy/domains/ct/ct.jar");
        classMap.put("diva.graph.GraphController",
                "lib/diva.jar");

        // First, we search for the jar file, then we try
        // getting the class as a resource.
        // FIXME: we don't handle the case where there are no
        // individual jar files because the user did not run 'make install'.

        HashSet jarFilesThatHaveBeenRequired = new HashSet();

        // Add jar files that are contained in ptsupport.jar.
        // FIXME: we could open ptsupport.jar here and get the complete
        // list of directories.  Instead, we get the primary offenders.
        jarFilesThatHaveBeenRequired.add("ptolemy/actor/actor.jar");
        jarFilesThatHaveBeenRequired.add("ptolemy/actor/lib/lib.jar");

	// Set to true if we need to fix up jar files because 
	// jar files are not present probably because
	// 'make install' was not run.
	boolean fixJarFiles = false;

        Iterator classNames = classMap.keySet().iterator();
        while (classNames.hasNext()) {
            String className = (String)classNames.next();

            if (jarFilesThatHaveBeenRequired
                    .contains((String)classMap.get(className))) {
                // If we have already possibly copied the jar file, then skip
                continue;
            }

            if (!_copyPotentialJarFile((String)classMap.get(className),
                        className, jarFilesThatHaveBeenRequired)) {
                // The className could not be found in the classMap

                // Under Web Start, the resource that contains a class
                // will have a mangled name, so we copy the jar file.
                String classResource =
                    ClassUtilities.lookupClassAsResource(className);


                if (classResource == null) {
                    throw new IOException("Could not find '" + className
                            + "' as a resource.\n"
                            + "Try adding this class to the "
                            + "necessaryClasses parameter"
                                          );
                }

                // Under Web Start, if there was a space in the
                // pathname, the space will have been converted to %20
                // but looking up a file will fail if the file has
                // a space in it and we are looking for a %20.
                classResource = StringUtilities
                    .substitute(classResource, "%20", " "); 

                // We need to actually look up the file to deal with
                // the various C:/ptII, c:/ptII, c:\ptII, C:\ptII possibilities
                String canonicalClassResource =
                    UtilityFunctions.findFile(classResource);

                String canonicalPtIIDirectory =
                    UtilityFunctions.findFile(_ptIIDirectory);
                if (canonicalClassResource.equals(canonicalPtIIDirectory)) {
                    // Failed to find the jar file.

                    // Look for a jar file under $PTII in the directory
                    // where the class is found.  If the class is foo.bar.biz,
                    // the we look for $PTII/foo/bar/bar.jar

                    String pathName = className.replace('.', '/');
                    String directoryName = pathName.substring(0,  
                            pathName.lastIndexOf("/"));
                    String jarFileName = directoryName
                        + directoryName.substring(
                                directoryName.lastIndexOf("/"))
                        + ".jar";

                    if (_copyPotentialJarFile(jarFileName,
                        className, jarFilesThatHaveBeenRequired)) {
                        
                    } else {   
                        String warning = "Looking up '" + className
                            + "'\nreturned the $PTII directory '"
                            + _ptIIDirectory + "' instead of a jar file.\n'"
                            + jarFileName + "' was not present?\n";
                        if (_codeBase.equals(".")) {
                            // We only need print an error message if
                            // we are actually trying to copy the file
                            throw new IOException(warning 
                                    + "Since the applet directory is not "
                                    + "inside the Ptolemy II tree, we need "
                                    + "to have\n"
                                    + "access to the jar files. If the jar "
                                    + "files are not present, then we cannot"
                                    + "copy\n"
                                    + "them to the new location and the java "
                                    + "classes will not be found by the "
                                    + "applet.\n"
                                    + "One solution is to run \" cd $PTII;"
                                    + "make install\" to create the jar files."
                                    + "\nAnother solution is to place the "
                                    + "applet directory under the\nPtolemy II "
                                    + "directory.");
                        } else {
                            // Print it so that the user knows that running
                            // make install would be a good job
                            System.out.println("Warning: " + warning
                                    + "Perhaps you need to run "
                                    + "'make install' to create the "
                                    + "jar files?"
                                    + "\nIf the jar files are not "
                                    + "present, then the archive "
                                    + "applet parameter will not "
                                    + "include all of the jar files.");
                            fixJarFiles = true;
                            continue;
                        }
                    }
                }

//                 System.out.println("AppletWriter: "
//                         + "\n\tclassResource:    " + classResource
//                         + "\n\t_outputDirectory: " + _outputDirectory
//                         + "\n\tclassName:        " + className
//                         + "\n\tclassMap.get():   "
//                         + (String)classMap.get(className));
//                 jarFilesThatHaveBeenRequired
//                     .add((String)classMap.get(className));
// 		if (_codeBase.equals(".")) {
// 		    // If the codeBase is equal to the current directory,
// 		    // we copy the jar file.
// 		    _copyFile(classResource, _outputDirectory,
//                             (String)classMap.get(className));
// 		} 
            }
        }

	
        jarFilesThatHaveBeenRequired.remove("ptolemy/actor/actor.jar");
        jarFilesThatHaveBeenRequired.remove("ptolemy/actor/lib/lib.jar");

        File potentialDomainJarFile =
            new File(_ptIIDirectory, _domainJar);
        if (!potentialDomainJarFile.exists()) {
            // If we are running under the Windows installer, then
            // the domain specific jar files might not be present
            // so we add ptolemy/domains/domains.jar
            // We don't always require domains.jar because if
            // the domain specific jar file is present, then the
            // domain specific jar file will be much smaller.
            System.out.println("AppletWriter: Warning: could not find '"
                    + _domainJar + "', '"
                    + potentialDomainJarFile + "' does not exist, "
                    + "adding domains.jar to jarfiles");
            jarFilesThatHaveBeenRequired.add("ptolemy/domains/domains.jar");
        }

	if (fixJarFiles) {
	    // If the code generator was run but codeBase != . and
	    // make install was not run, then we will not have figured
	    // out very many jar files.  So, we fix up the list
	    // 
	    jarFilesThatHaveBeenRequired.add("ptolemy/ptsupport.jar");
            jarFilesThatHaveBeenRequired.add(_domainJar);

	}
        return jarFilesThatHaveBeenRequired;
    }

    // Copy sourceFile to the destinationFile in destinationDirectory.
    private void _copyFile(String sourceFileName,
            String destinationDirectory,
            String destinationFileName )
            throws IOException {
        File sourceFile = new File(sourceFileName);
        if ( !sourceFile.isFile()) {
            throw new FileNotFoundException("'"
                    + sourceFileName + "' is not a file or cannot be found."
                    + "\nPerhaps you need "
                    + "to run 'make install'?");
        }

        File destinationFile = new File(destinationDirectory,
                destinationFileName);
        File destinationParent = new File(destinationFile.getParent());
        destinationParent.mkdirs();

        System.out.println("AppletWriter: Copying " + sourceFile
                + " (" + sourceFile.length()/1024 + "K) to "
                + destinationFile);

        // Avoid end of line and localization issues.
        BufferedInputStream in =
            new BufferedInputStream(new FileInputStream(sourceFile));
        BufferedOutputStream out =
            new BufferedOutputStream(new FileOutputStream(destinationFile));
        int c;

        while ((c = in.read()) != -1)
            out.write(c);

        // FIXME: need finally?
        in.close();
        out.close();
    }

    // Given a domain package, return the corresponding jar file
    private static String _getDomainJar(String domainPackage) {
        String domainPackageDomain =
            domainPackage.substring(0,
                    domainPackage.lastIndexOf(".")
                                    );

        String domainDomain =
            domainPackageDomain.substring(domainPackageDomain
                    .lastIndexOf(".") + 1);

        return StringUtilities.substitute(domainPackageDomain, ".", "/")
            + "/" + domainDomain + ".jar";
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////


    // The relative path to $PTII, for example "../../..".
    private String _codeBase;

    // The path to the jar file containing the domain classes,
    // for example "ptolemy/domains/sdf/sdf.jar".
    private String _domainJar;

    // The model we are generating code for.
    private CompositeActor _model;

    // The jar files that are necessary to run the model if the codebase
    // is ".".
    // For example: "ptolemy/ptsupport.jar,ptolemy/domains/sdf/sdf.jar".
    private String _modelJarFiles;

    // URL that names the model.
    private String _modelPath;

    // The full path to the directory where we are creating the model
    private String _outputDirectory;

    // The sanitized modelName
    private String _sanitizedModelName;

    // The value of the ptolemy.ptII.dir property.
    private String _ptIIDirectory;

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
    "ptolemy/copernicus/applet/";
}

