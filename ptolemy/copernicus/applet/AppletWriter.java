/* A transformer that writes an applet version of the model.

 Copyright (c) 2001-2002 The Regents of the University of California.
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

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.copernicus.kernel.Copernicus;
import ptolemy.copernicus.kernel.GeneratorAttribute;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.StringUtilities;

import soot.*;
import soot.jimple.*;
import soot.toolkits.scalar.*;
import soot.util.*;
import soot.toolkits.graph.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

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
public class AppletWriter extends SceneTransformer {
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
        return super.getDefaultOptions() + "templateDirectory:" +
	    TEMPLATE_DIRECTORY_DEFAULT;
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() + " targetPackage outDir templateDirectory";
    }


    /** Save the model as an applet.
     *  <p>For example, if the model is called MyModel, and
     *  this phase is called with:
     *  <pre>
     *	-p wjtp.appletWriter targetPackage:foo.bar
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

	_outputDirectory = Options.getString(options, "outDir");

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
	_targetPackage = Options.getString(options, "targetPackage");

	// Check to see if the _outputDirectory has the same root
	// as the _ptIIDirectory.  _outputDirectory is not under
	// _ptIIDirectory, then we will need to copy jar files around
	boolean copyJarFiles = false;

	// Convert _targetPackage "foo/bar" to _codeBase
	// "../../.."  There is something a little bit strange
	// here, since we actually create the code in a sub
	// package of _targetPackage We could rename the
	// targetPackage parameter to parentTargetPackage but I'd
	// rather keep things uniform with the other generators?

	int start = _targetPackage.indexOf('.');
	// _codeBase has one more level than _targetPackage.
	StringBuffer buffer = new StringBuffer("..");
	while (start != -1) {
	    buffer.append("/..");
	    start = _targetPackage.indexOf('.', start + 1);
	}
	_codeBase = buffer.toString();

	try {
	    if (!_isSubdirectory(_ptIIDirectory, _outputDirectory)) {
		copyJarFiles = true;
		_codeBase = ".";
	    }
	} catch (IOException ex) {}

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

	String directorPackageDomain =
            directorPackage.substring(0,
                    directorPackage.lastIndexOf(".")
                                      );

	String directorDomain =
            directorPackageDomain.substring(directorPackageDomain
                    .lastIndexOf(".") + 1);

	_domainJar =
	    StringUtilities.substitute(directorPackageDomain, ".", "/")
	    + "/" + directorDomain + ".jar";

	_sanitizedModelName = StringUtilities.sanitizeName(_model.getName());


	// Create the directory where we will create the files.
	File outDirFile = new File(_outputDirectory);
	if (outDirFile.isDirectory()) {
	    System.out.println(" Warning: '" + outDirFile
                    + "' already exists.");
	}
	outDirFile.mkdirs();

	// Set up the HashMap we will use when we read in files like
	// makefile.in and search for strings like @codebase@ and substitute
	// in the value of _codeBase.
	_substituteMap = new HashMap();
	_substituteMap.put("@codeBase@", _codeBase);
	_substituteMap.put("@domainJar@", _domainJar);
	_substituteMap.put("@outDir@", _outputDirectory);
	_substituteMap.put("@sanitizedModelName@",
                _sanitizedModelName);
	_substituteMap.put("@ptIIDirectory@", _ptIIDirectory);

	// Print out the map for debugging purposes
	Iterator keys = _substituteMap.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    System.out.println("AppletWriter: '" + key + "' '" +
                    (String)_substituteMap.get(key) + "'");
	}

	// Generate the .xml file.
	String modelFileName =
	    _outputDirectory + "/" + _sanitizedModelName + ".xml";
	System.out.println("AppletWriter: about to write '"
			   + modelFileName + "'");
	try {
	    Writer modelFileWriter =
		new BufferedWriter(new OutputStreamWriter(new FileOutputStream(modelFileName)));
	    _model.exportMoML(modelFileWriter);
	    modelFileWriter.close();
	} catch (IOException ex) {
	    throw new InternalErrorException("Problem writing '"
                    + modelFileName + "': " + ex);
	}

	// Read in the templates and generate new files.

	// The directory that contains the templates.
	// FIXME: this could be a Ptolemy parameter?

	//_templateDirectory =
	//    StringUtilities.substitute(Options.getString(options,
        //            "templateDirectory"),
        //            "$PTII", _ptIIDirectory);
	_templateDirectory = Options.getString(options, "templateDirectory");

	System.out.println("AppletWriter: _templateDirectory: '"
			   + _templateDirectory + "'");

	try {
	    Copernicus.substitute(_templateDirectory + "makefile.in",
				    _substituteMap,
				    _outputDirectory + "/makefile");
	    Copernicus.substitute(_templateDirectory + "model.htm.in",
				    _substituteMap,
				    _outputDirectory + "/"
				    + _sanitizedModelName + ".htm");
	    Copernicus.substitute(_templateDirectory + "modelVergil.htm.in",
				    _substituteMap,
				    _outputDirectory + "/"
				    + _sanitizedModelName
				    + "Vergil.htm");
	    if (copyJarFiles) {
		_copyJarFiles(director);
	    }
	} catch (IOException ex) {
	    // This exception tends to get eaten by soot, so we print as well.
	    System.err.println("Problem writing makefile or html files:" + ex);
	    ex.printStackTrace();
	    throw new InternalErrorException("Problem writing the makefile "
                    + "or htm files: " + ex);
	}
    }

    // Copy jar files into _outputDirectory
    private void _copyJarFiles(Director director) 
	throws IOException {

	Map classMap = new HashMap();
	classMap.put("ptolemy.actor.gui.MoMLApplet",
		     "ptolemy/ptsupport.jar");
	classMap.put(director.getClass().getName(),
		     _domainJar);
	classMap.put("ptolemy.vergil.MoMLViewerApplet",
		     "ptolemy/vergil/vergilApplet.jar");
	classMap.put("diva.foo",
		     "lib/diva.jar");

	Iterator classNames = classMap.entrySet().iterator();
	while (classNames.hasNext()) {
	    String className = (String)classNames.next();
	    // FIXME: will this work if we are getting classes
	    // from a directory?
	    String classResource =
		GeneratorAttribute.lookupClassAsResource(className);

	    if (classResource != null) {
		System.out.println("AppletWriter: " + classResource
				   + " " + _outputDirectory 
				   + " " + (String)classMap.get(className));
		// This is a dumb way to copy files, Java should have
		// a method that has the os do it for us
		File inputFile = new File(classResource);
		File outputFile = new File(_outputDirectory,
					   (String)classMap.get(className));
		outputFile.mkdirs();
		
		FileReader in = new FileReader(inputFile);
		FileWriter out = new FileWriter(outputFile);
		int c;

		while ((c = in.read()) != -1)
		    out.write(c);
		
		in.close();
		out.close();
	    }
	}
    }

    // Return true if possibleSubdirectory is a subdirectory of parent.
    private boolean _isSubdirectory(String parent,
				    String possibleSubdirectory)
	throws IOException {
	System.out.println("_isSubdirectory: start \n\t" + parent + "\n\t" +
			   possibleSubdirectory);
	File parentFile = new File(parent);
	File possibleSubdirectoryFile = new File(possibleSubdirectory);
	if (parentFile.isFile() || possibleSubdirectoryFile.isFile()) {
	    throw new IOException ("'" + parent + "' or '" 
				   + possibleSubdirectory + "' is a file, "
				   + "it should be a directory");
	}
	String parentCanonical = parentFile.getCanonicalPath();
	String possibleSubdirectoryCanonical =
	    parentFile.getCanonicalPath();
	System.out.println("_isSubdirectory: \n\t" + parentCanonical + "\n\t" +
			   possibleSubdirectoryCanonical);
	return parentCanonical.startsWith(possibleSubdirectoryCanonical);
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

