/* Instantiate and Invoke Accessors using Nashorn.

   Copyright (c) 2016 The Regents of the University of California.
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
package ptolemy.actor.lib.jjs;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// NashornAccessorHostApplication

/** 
 * Instantiate and Invoke Accessors using Nashorn.
 * Evaluate the arguments, which are expected to be JavaScript files
 * that define Composite Accessors. 
 * 
 * <p>The Nashorn and Cape Code Accessor hosts are similar in that they
 * both use Nashorn as the underlying JavaScript engine.  They also
 * both can invoke JavaScript accessors that use modules defined in
 * $PTII/ptolemy/actor/lib/jjs/modules.  They also both share
 * $PTII/ptolemy/actor/lib/jjs/capeCodeHost.js.</p>
 * 
 * <p>The Nashorn Accessor Host differs from the Cape Code Accessor
 * Host in that Cape Code Accessor Host reads in Ptolemy II .xml MoML
 * files and can invoke regular Ptolemy II actors written in Java such
 * a ptolemy.actor.lib.Ramp.  The Nashorn Accessor Host reads in .js
 * files that define CompositeAccessors.  The Nashorn Accessor Host is
 * not intended to invoke regular Ptolemy II actors written in Java
 * and it does not invoke the Ptolemy II actor execution semantics
 * code implemented in Java.</p>
 *
 * <p>Note that by using code generation, Cape Code .xml MoML files
 * can be converted in to .js Composite Accessor files provided that 
 * the .xml file only uses JavaScript and JSAccessor actors.</p>
 *
 * <dl>
 * <dt><code>-js</code></dt>
 * <dd>Any arguments other than the optional
 * <code>-timeout <i>NNNN</i></code> are regular JavaScript files that are
 * to be evaluated</dd>
 * <dt><code>-timeout</code></dt>
 * <dd>The next argument is the timeout in milliseconds.  The
 * remaining arguments are accessors that will be initialized and
 * invoked.</dd>
 * </dl>

 * <p>To invoke:</p>
 * <pre> 
 * (cd $PTII/org/terraswarm/accessor/accessors/web/hosts; $PTII/bin/ptinvoke ptolemy.actor.lib.jjs.NashornAccessorHostApplication -timeout 10000 hosts/nashorn/test/testNashornHost.js)
 * </pre>
 *
 * <p> The command line syntax is:</p>
 * <pre>
 * [-js javascript.js ...] [-timeout timeoutInMilliseconds] javaScriptOrcompositeAccessor1.js [javaScriptOrCompositeAccessor2.js ...]
 * </pre>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 11.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class NashornAccessorHostApplication {

    /** Evaluate the files named by the arguments.
     *  @param args An array of one or more file names.  See the class comment for 
     *  the syntax.
     *  @exception IllegalActionException If the Nashorn engine cannot be found.
     *  @exception IOException If a file cannot be read or closed.
     *  @exception NoSuchMethodException If evaluateCode() JavaScript method is not defined.
     *  @exception ScriptException If there is a problem evaluating a file.
     */
    public NashornAccessorHostApplication(String [] args)
	throws IllegalActionException, IOException, NoSuchMethodException, ScriptException {

        // Create a Nashorn script engine.
        ScriptEngine engine = JavaScript.createEngine(null, false, false);
        if (engine == null) {
            // Coverity Scan is happier if we check for null here.
            throw new IllegalActionException(
                    "Could not get the nashorn engine from the javax.script.ScriptEngineManager.  Nashorn present in JDK 1.8 and later.");
        }

	if (args[0].equals("-js") || args[2].equals("-js")) {

	    // Process the -timeout NNNN args.
	    int argsStart = 1;
	    int timeout = -1;
	    if (args[1].equals("-timeout")) {
		argsStart = 3;
		timeout = Integer.parseInt(args[2]);
	    } else if (args[0].equals("-timeout")) {
		argsStart = 3;
		timeout = Integer.parseInt(args[1]);
	    }
	    if (timeout > 0) {
		Object instance = engine.eval("function() { print('NashornAccessorHostApplication done.');}");
		((Invocable)engine).invokeFunction("setTimeout", instance, timeout);
	    }

	    // Evaluate the remaining arguments as JavaScript files.
	    int i;
	    for (i = argsStart; i < args.length; i++) {
		try (InputStreamReader reader = new InputStreamReader(new FileInputStream(args[i]), "UTF-8")) {
		    engine.eval(reader);
		}
	    }

	} else {
	    // Process the optional -timeout ms args and then
	    // instantiate and invoke the composite accessors named by
	    // the arguments.
	    /* _instance =*/ ((Invocable)engine)
		.invokeFunction("invoke",
				(Object)args);
	}
	// FIXME: Should we close the engine in a finally block?
    }

    /** Invoke one or more JavaScript files.
     *  @param args One or more JavaScript files.  See the class
     *  comment for the syntax.
     */
    public static void main(String[] args) {
	try {
	    new NashornAccessorHostApplication(args);
	} catch (Throwable throwable) {
	    System.err.println("Command Failed: " + throwable);
	    throwable.printStackTrace();
	    StringUtilities.exit(1);
	}
    }
}
