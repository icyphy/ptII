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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.util.FileUtilities;
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
 * $PTII/ptolemy/actor/lib/jjs/modules.</p>
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
 * <p>The main() method of this class takes the following command
 * line arguments:</p>
 * <dl>
 * <dt><code>-accessor|--accessor</code>
 * <dd>If present, then the files named as command line arguments are
 * Composite Accessors to be instantiated and initialized.
 * If not present, then the files named
 * as command line arguments are to be interpreted as regular
 * JavaScript files.</dd>
 * <dt><code>-echo|--echo</code></dt>
 * <dd>Echo the command that would be run by hand to replicate the
 * test. This is helpful for use under Ant apply.</dd>
 * <dt><code>-h|--h|-help|--help</code></dt>
 * <dd>Print a usage message</dd>
 * <dt><code>-timeout|--timeout <i>milliseconds</i></code></dt>
 * <dd>The minimum amount of time the script should run.</dd>
 * <dt><code>-v|--v|-version|--version</code></dt>
 * <dd>Print out the version number</dd>
 * </dl>
 *
 * <p>After the flags, the one or more JavaScript files are present that
 * name either or regular JavaScript files.</p>
 *
 * <p>To run a very simple test:</p>
 * <pre>
 * (cd $PTII/org/terraswarm/accessor/accessors/web/hosts; $PTII/bin/ptinvoke ptolemy.actor.lib.jjs.NashornAccessorHostApplication -accessor -timeout 10000 hosts/nashorn/test/testNashornHost.js)
 * </pre>
 *
 * <p>To run a composite accessor:</p>
 * <pre>
 * (cd $PTII/org/terraswarm/accessor/accessors/web/hosts; $PTII/bin/ptinvoke ptolemy.actor.lib.jjs.NashornAccessorHostApplication -accessor -timeout 10000 test/auto/RampJSDisplay.js)
 * </pre>
 *
 * <p> The command line syntax is:</p>
 * <pre>
 * java -classpath $PTII ptolemy.actor.lib.jjs.NashornAccessorHostApplication \
 *  [-accessor|--accessors] \
 *  [-h|--h|-help|--help] \
 *  [-echo|--echo] \
 *  [-timeout|--timeout milliseconds] \
 *  [-v|--v|-version|--version] \
 *  accessorOrRegularJavaScriptFile1.js [accessorOrRegularJavaScriptFile2.js ...]
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
     *  @return 0 for success, 3 for argument problems.  See main() in commonHost.js.
     *  @exception IllegalActionException If the Nashorn engine cannot be found.
     *  @exception IOException If a file cannot be read or closed.
     *  @exception NoSuchMethodException If evaluateCode() JavaScript method is not defined.
     *  @exception ScriptException If there is a problem evaluating a file.
     */
    public static int evaluate(String [] args)
            throws Throwable {

        // Create a Nashorn script engine.
        // NOTE: This is somewhat similar to JavaScript.createEngine().
        ScriptEngineManager factory = new ScriptEngineManager();
        ScriptEngine engine = factory.getEngineByName("nashorn");
        if (engine == null) {
            // Coverity Scan is happier if we check for null here.
            throw new Exception(
                    "Could not get the nashorn engine from the javax.script.ScriptEngineManager."
                    + " Nashorn is present in JDK 1.8 and later.");
        }
        
        // Load nashornHost.js, which provides top-level functions.
        engine.eval(FileUtilities.openForReading(
                "$CLASSPATH/ptolemy/actor/lib/jjs/nashornHost.js", null,
                null));
	
	String accessorMainPath = "$CLASSPATH/org/terraswarm/accessor/accessors/web/hosts/util/accessorMain.js";
        engine.eval(FileUtilities.openForReading(accessorMainPath, null, null));

        // Load localFunctions.js, which loads commonHost.js and
        // provides more top-level functions.
        //String localFunctionsPath = "$CLASSPATH/ptolemy/actor/lib/jjs/localFunctions.js";
        //engine.eval(FileUtilities.openForReading(localFunctionsPath, null, null));

        // Evaluate the command-line arguments. This will either instantiate and
        // initialize accessors or evaluate specified JavaScript code.
	Object returnValue = ((Invocable)engine)
	        .invokeFunction("accessorMain", (Object)args);
	
        // FIXME: Should we close the engine in a finally block?
	if (returnValue == null) {
	    System.err.println("NashornAccessorHostApplication.evaluate(" 
	            + Arrays.toString(args) + ") returned null?");
	    return -1;
	}
	return ((Integer)returnValue).intValue();
    }

    /** Invoke one or more JavaScript files.
     *  @param args One or more JavaScript files.  See the class
     *  comment for the syntax.
     */
    public static void main(String[] args) {
        try {
	    int returnValue = NashornAccessorHostApplication.evaluate(args);
            StringUtilities.exit(returnValue);
        } catch (Throwable throwable) {
            System.err.println("Command Failed: " + throwable);
            throwable.printStackTrace();
            StringUtilities.exit(1);
        }
    }
    /** Utility method to read a string from an input stream.
     *  @param stream The stream.
     *  @return The string.
     * @exception IOException If the stream cannot be read.
     */
    public static String readFromInputStream(InputStream stream)
            throws IOException {
        StringBuffer response = new StringBuffer();
        BufferedReader reader = null;
        String line = "";
        // Avoid Coverity Scan: "Dubious method used (FB.DM_DEFAULT_ENCODING)"
        reader = new BufferedReader(new InputStreamReader(stream,
                java.nio.charset.Charset.defaultCharset()));

        String lineBreak = System.getProperty("line.separator");
        while ((line = reader.readLine()) != null) {
            response.append(line);
            if (!line.endsWith(lineBreak)) {
                response.append(lineBreak);
            }
        }
        reader.close();
        return response.toString();
    }
}
