/* Abstract base class that provides common main() functionality
to be used by various backends.

 Copyright (c) 2001 The Regents of the University of California.
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

import soot.ConsoleCompilationListener;
import soot.Main;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.Transform;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Manager;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.moml.MoMLParser;

import com.microstar.xml.XmlException;

import java.util.Iterator;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// Main
/** Abstract base class that provides common main() functionality
to be used by various backends.

The backends should extend this class and create a constructor that
looks like:
<pre>
public class Main extends KernelMain {
    public Main(String [] args) {
	super(args);
	// Process args
	// Do transformations
	_callSootMain(args);
    }
    public static void main(String[] args) {
	Main main = new Main(args);
    }
}
</pre>

We do the bulk of the work in the constructor so that we can easily 
test these classes.

@author Stephen Neuendorffer, Christopher Hylands
@version $Id$
*/
public class KernelMain {    

    /** Set up code generation arguments.
     *  @param options Optional soot style options to be prepended to any
     *  Soot style options contained in the args parameter.
     *  @param args An array of Strings, where the first element names
     *  a MoML Class or file, and the subsequent optional arguments
     *  are Soot style options such as
     */
    public KernelMain (String options, String[] args) {

	// The options argument is a separate argument to make it easy
	// to prepend Ptolemy II specific code genereration arguments
	// to the array of arguments passed in by main().

        if(args.length == 0) {
	    throw new IllegalArgumentException("args.length == 0, args must "
					       + "be at least of length 1 "
					       + "and contain the name of "
					       + "a MoML class or file.");
        }            

	_momlClassName = args[0];

	// Build up the options string
	StringBuffer optionsBuffer = null;
	if (options != null) {
	    optionsBuffer = new StringBuffer(options);
	} else {
	    optionsBuffer = new StringBuffer();
	}

	for (int i = 1; i < args.length; i++) {
	    optionsBuffer.append(" " + args[i]);
	}

	_sootOptions = optionsBuffer.toString();
    }

    /** Set up code generation arguments.
     *  @param momlClassName The name of the top level model or the
     *  .xml file that we are to generate code for.
     *  @param transformOptions Soot Options of the format:
     *  <code><i>[pass</i> <i>parameter</i>:<i>value]</i></code>.
     *  For example:
     *  <code>deep  targetPackage:ptolemy.copernicus.shallow.cg</code>
     *  @param sootOptions Soot command line options.
     *  The most common option is <code>-d ../../..</code>, which
     *  will store the generated files in ../../..
     *  <br>For a complete list of sootOptions, pass in "-h", or run
     *  <code>$PTII/bin/soot -h<code>, or see
     *  <a href="http://www.sable.mcgill.ca/soot/tutorial/usage">http://www.sable.mcgill.ca/soot/tutorial/usage</a>
     */
    public KernelMain(String momlClassName, String transformOptions,
		      String sootOptions) {
	_momlClassName = momlClassName;
	_transformOptions = transformOptions;
	_sootOptions = sootOptions;
    }

    /** Read in a MoML class, either as a top level model or
     *  a file, initialize the model, then create instance classes for actors.
     *  <p> The MoML class name is processed as follows
     *  <ol>
     *  <li> The momlClassName argument is assumed to be a dot
     *  separated top level model name such as
     *  <code>ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom</code>
     *  and inserted into a MoML fragment:  
     *  <p><code>
     *  &lt;entity name="ToplevelModel" class=" + momlClassName + "/&gt;
     *  </code>
     *  and then passed to MoMLParser.parse().
     *  <li>If the parse fails, then the name is tried as a
     *  relative MoML file name and passed to MoMLParser.parseFile().
     *  <ol>
     *  @exception IllegalActionException if the model cannot be parsed.
     */
    public void parseInitializeCreateActorInstances()
	throws IllegalActionException {

        // Call the MOML parser on the test file to generate a Ptolemy II
        // model.
        MoMLParser parser = new MoMLParser();
        try {
	    // First, try it as a top level model
	    String source = "<entity name=\"ToplevelModel\""
	        + "class=\"" + _momlClassName + "\"/>\n";
	     _toplevel = (CompositeActor)parser.parse(source);        

        } catch (Exception exception) {
	    try {
		// Then try it as an xml file
		_toplevel = (CompositeActor)parser.parseFile(_momlClassName);
	    } catch (Exception exception2) {
		throw new 
		    IllegalActionException("Failed to parse '" 
					   + _momlClassName
					   + "': " + exception);
	    }
        }

        // Temporary hack because cloning doesn't properly clone
        // type constraints.
        CompositeActor modelClass = null;
	try {
	    modelClass = (CompositeActor)
            parser._searchForClass(_momlClassName,
				   _toplevel.getMoMLInfo().source);
	} catch (XmlException xml) {
		throw new 
		    IllegalActionException("Failed to find class '"
					   + _momlClassName + "' in '"
					   + _toplevel.getMoMLInfo().source
					   + "': " + xml);
	}

        if(modelClass != null) {
            _toplevel = modelClass;
        }                          

        // Initialize the model to ensure type resolution and scheduling
        // are done.
        try {
            Manager manager = new Manager(_toplevel.workspace(), "manager");
            _toplevel.setManager(manager);
            manager.initialize();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize "
				       + "composite actor: " + e);
        }

        // A Hack to ignore the class we specify on the command
	// line. This is a soot problem that requires this hack.
	// We will provide soot with java.lang.Object as its
	// only application class in Main. The first transformation
	// will ignore all application classes (i.e. set them to
	// library classes)
        Scene.v().getPack("wjtp").add(new Transform("wjtp.hack", 
                new _IgnoreAllApplicationClasses(), ""));

        // Create instance classes for actors.
	// This transformer takes no input as far as soot is concerned
	// (i.e. no application classes) and creates application
	// classes from the model. 
        Scene.v().getPack("wjtp").add(new Transform("wjtp.at", 
                ActorTransformer.v(_toplevel), _sootOptions));

    }
    
    /** Sample main() method that parses a MoML class, initializes
     *  the model and creates actor instances.  In this class, 
     *  this method does not do much, it is only a sample.
     *  @params args The first element of the array is the MoML class
     *  name or file name, subsequent optional arguments are Soot
     *  style options args of the format:
     *  <code><i>[pass</i> <i>parameter</i>:<i>value]</i></code>.
     *  For example:
     *  <code>deep  targetPackage:ptolemy.copernicus.shallow.cg</code>
     *  @exception IllegalActionException if the model cannot be parsed.
     */
    public static void main(String[] args) throws IllegalActionException {
	KernelMain kernelMain = new KernelMain(null, args);
	kernelMain.parseInitializeCreateActorInstances();
    }

    /** Return the model that we are generating code for.
     */
    public CompositeActor toplevel() {
        return _toplevel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    protected void _callSootMain(String [] args) {
        // This is rather ugly.  The moml Class is not a Java class, so
        // soot won't recognize it.  However, if we give soot nothing, then 
        // it won't run.  Note that later we will call setLibraryClass() on
        // this class so that we don't actually generate code for it.
        args[0] = "java.lang.Object";
        soot.Main.main(args);
        //soot.Main m = new soot.Main();
        //ConsoleCompilationListener ccl = new ConsoleCompilationListener();
        //soot.Main.addCompilationListener(ccl);
        //(new Thread(m)).start();
	
    }


    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name of the MoML class, either as a top level model or
     *  as an xml file that we are generating code for.	
     */	
    protected String _momlClassName;

    /** Soot style options arguments of the form
     *  <code><i>[pass</i> <i>parameter</i>:<i>value] . . .</i></code>.
     *  For example:
     *  <code>deep  targetPackage:ptolemy.copernicus.shallow.cg</code>
     */
    protected String _sootOptions;
    protected String _transformOptions;

    /** The CompositeActor we are generating code for.
     */
    protected CompositeActor _toplevel;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    private static class _IgnoreAllApplicationClasses
	extends SceneTransformer {
        /** Transform the Scene according to the information specified
         *  in the model for this transform.
         *  @param phaseName The phase this transform is operating under.
         *  @param options The options to apply. 
         */
        protected void internalTransform(String phaseName, Map options) {
            for(Iterator classes =
		    Scene.v().getApplicationClasses().snapshotIterator();
                classes.hasNext();) {
                ((SootClass)classes.next()).setLibraryClass();
            }
        }
    }

}
    











