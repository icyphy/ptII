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
	super(args[0]);
	_initialize();

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
     *  @param momlClassName The name of the top level model or the
     *  .xml file that we are to generate code for.  For example:
     *  "ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom".
     */
    public KernelMain(String momlClassName) {
	_momlClassName = momlClassName;
    }

    /** Sample main() method that parses a MoML class, initializes
     *  the model and creates actor instances.  In this class, 
     *  this method does not do much, it is only a sample.
     *
     *  @param args The first element of the array is the MoML class
     *  name or file name, subsequent optional arguments are Soot
     *  command line options.
     *  <br>The most common option is <code>-d ../../..</code>, which
     *  will store the generated files in ../../..
     *  Another common option is
     *  <code> -p <i>phase-name</i> <i>key1[</i>:<i>value1]</i>,<i>key2[</i>:<i>value2]</i>,<i>...</i>,<i>keyn[</i>:<i>valuen]</i></code>
     *  which will set the run time option <i>key</i> to <i>value</i> for
     *  <i>phase-name</i> (default for <i>value</i> is true)
     *  An example is:<br>
     *  <code>-p wjtp.at deep,targetPackage:ptolemy.copernicus.jhdl.cg</code>
     *  <br>For a complete list of Soot Options, pass in "-h", or run
     *  <code>$PTII/bin/soot -h<code>, or see
     *  <a href="http://www.sable.mcgill.ca/soot/tutorial/usage">http://www.sable.mcgill.ca/soot/tutorial/usage</a>
     *
     *  @exception IllegalActionException if the model cannot be parsed.
     */
    public static void main(String[] args) throws IllegalActionException {
	KernelMain kernelMain = new KernelMain(args[0]);
	kernelMain._initialize();
	kernelMain._callSootMain(args);
    }

    /** Return the model that we are generating code for.
     */
    public CompositeActor toplevel() {
        return _toplevel;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Call soot.Main.main(), which does command line argument processing
     *  and then starts the transformation.  This method should be called
     *  after calling _initialize() and setting
     *  up the transformations.
     *
     *  @param args Soot command line arguments to be passed
     *  to soot.Main.main().  this method changes the first element of the
     *  args array to "java.lang.Object"and then call soot.Main.main(args).
     */	
    protected void _callSootMain(String [] args) {
        // This is rather ugly.  The moml Class is not a Java class, so
        // soot won't recognize it.  However, if we give soot nothing, then 
        // it won't run.  Note that later we will call setLibraryClass() on
        // this class so that we don't actually generate code for it.
        args[0] = "java.lang.Object";
	//System.out.println("KernelMain._callSootMain(): " + args.length);
        soot.Main.main(args);
    }

    /** Read in a MoML class, either as a top level model or
     *  a file, initialize the model, then create instance classes for actors.
     *  <p> The MoML class name is processed as follows:
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
     *  </ol>
     *  @exception IllegalActionException if the model cannot be parsed.
     */
    protected void _initialize()
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
                ActorTransformer.v(_toplevel)));

    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The name of the MoML class, either as a top level model or
     *  as an xml file that we are generating code for.	
     */	
    protected String _momlClassName;

    /** The CompositeActor we are generating code for.
     */
    protected CompositeActor _toplevel;

    ///////////////////////////////////////////////////////////////////
    ////                         inner classes                     ////

    public static class _IgnoreAllApplicationClasses
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
