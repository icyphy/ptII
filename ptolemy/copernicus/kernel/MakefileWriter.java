/* A transformer that writes a makefile that that can be used to run a model

 Copyright (c) 2002 The Regents of the University of California.
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
import ptolemy.actor.Director;
import ptolemy.copernicus.kernel.Copernicus;
import ptolemy.copernicus.kernel.GeneratorAttribute;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.StringUtilities;
import ptolemy.gui.GUIStringUtilities;
import ptolemy.actor.gui.JNLPUtilities;

import soot.*;
import soot.jimple.*;
import soot.toolkits.scalar.*;
import soot.util.*;
import soot.toolkits.graph.*;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Iterator;
import java.util.HashMap;
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
public class MakefileWriter extends SceneTransformer {
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

    public String getDefaultOptions() {
        return super.getDefaultOptions() + "templateDirectory:" +
	    TEMPLATE_DIRECTORY_DEFAULT;
    }

    public String getDeclaredOptions() {
        return super.getDeclaredOptions() +
            " targetPackage outDir templateDirectory";
    }


    /** Convert targetPackage "foo/bar" to codeBase
     *  "../../.."  
     *  @param targetPackage The package where we are creating the code
     *  @param outputDirectory The directory where we are producing code.
     *  @param ptIIDirectory The Ptolemy II directory, usually the value 
     *  of $PTII 
     *  @returns The codebase.  If the codebase is ".", then we may
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
     *	-p wjtp.makefileWriter targetPackage:foo.bar,outdir:c:/tmp,templateDirectory:bif
     *  </pre>
     *  Then we will create the directory c:/tmp/foo/bar/MyModel, read
     *  in $PTII/bif/makefile.in and generate c:/tmp/foo/bar/makefile.
     *
     *  @param phaseName The name of the phase, for example
     *  <code>wjtp.appletWriter</code>.
     *  @param options The options Map.
     *  This transformer uses the following options:
     *  <dl>
     *  <dt>targetPackage
     *  <dd>The package where the generated code will reside, for example:
     *  <code>ptolemy.copernicus.applet.cg.Butterfly</code>
     *  <dt>outDir
     *  <dd>The absolute path to the directory where the generated code
     *  will reside, for example:
     *  <code>c:/ptII/ptolemy/copernicus/applet/cg/Butterfly</code>
     *  <dt>templateDirectory
     *  <dd>The directory where we should read the makefile.in file
     *  
     *  </dl>
     */
    protected void internalTransform(String phaseName, Map options)
    {
        System.out.println("MakefileWriter.internalTransform("
                + phaseName + ", " + options + ")");

	_outputDirectory = Options.getString(options, "outDir");

	// Determine where $PTII is so that we can find the right directory.
	_ptIIDirectory = null;
        try {
            // NOTE: getProperty() will probably fail in applets, which
            // is why this is in a try block.
	    // NOTE: This property is set by the vergil startup script.
	    _ptIIDirectory =
                GUIStringUtilities.getProperty("ptolemy.ptII.dir");
        } catch (SecurityException security) {
            throw new InternalErrorException(null, security,
                    "Could not find "
                    + "'ptolemy.ptII.dir'"
                    + " property.  Vergil should be "
                    + "invoked with -Dptolemy.ptII.dir"
                    + "=\"$PTII\"");
        }

	_ptIIUserDirectory = Options.getString(options, "putIIUserDir");

	// If the targetPackage is foo.bar, and the model is MyModel,
	// the we will do mkdir $PTII/foo/bar/MyModel/
	_targetPackage = Options.getString(options, "targetPackage");

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
	_substituteMap.put("@outDir@", _outputDirectory);
	_substituteMap.put("@ptIIDirectory@", _ptIIDirectory);
	_substituteMap.put("@ptIIUserDirectory@", _ptIIUserDirectory);
	_substituteMap.put("@sanitizedModelName@",
                _sanitizedModelName);
	_substituteMap.put("@targetPackage@", _targetPackage);

	// Print out the map for debugging purposes
	Iterator keys = _substituteMap.keySet().iterator();
	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    System.out.println("MakefileWriter: '" + key + "' '" +
                    (String)_substituteMap.get(key) + "'");
	}

	// Read in the templates and generate new files.

	// The directory that contains the templates.
	// FIXME: this could be a Ptolemy parameter?

	//_templateDirectory =
	//    StringUtilities.substitute(Options.getString(options,
        //            "templateDirectory"),
        //            "$PTII", _ptIIDirectory);
	_templateDirectory = Options.getString(options, "templateDirectory");

        if (!_templateDirectory.endsWith("/")) {
            _templateDirectory = _templateDirectory + "/";
        }

	System.out.println("MakefileWriter: _templateDirectory: '"
			   + _templateDirectory + "'");

	try {
	    Copernicus.substitute(_templateDirectory + "makefile.in",
				    _substituteMap,
				    _outputDirectory + "/makefile");
	} catch (IOException ex) {
	    // This exception tends to get eaten by soot, so we print as well.
	    System.err.println("Problem writing makefile or html files:" + ex);
	    ex.printStackTrace();
	    throw new InternalErrorException("Problem writing the makefile "
                    + "or htm files: " + ex);
	}
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////


    /** Return true if _possibleSubdirectory is a subdirectory of parent. */
    private static boolean _isSubdirectory(String parent,
				    String possibleSubdirectory)
	throws IOException {
	//System.out.println("_isSubdirectory: start \n\t" + parent + "\n\t" +
        //			   possibleSubdirectory);
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
	//		   + parentCanonical + "\n\t"
	//		   + possibleSubdirectoryCanonical);
	return possibleSubdirectoryCanonical.startsWith(parentCanonical);
    }


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // The relative path to $PTII, for example "../../..".
    private String _codeBase;

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

