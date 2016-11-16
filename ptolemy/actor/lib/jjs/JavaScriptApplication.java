/* Execute a script in JavaScript using Nashorn.

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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ScriptObjectMirror;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypeAttribute;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.lib.conversions.json.TokenToJSON;
import ptolemy.actor.parameters.ParameterPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Constants;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.graph.Inequality;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.attributes.URIAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.SingletonAttribute;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.util.CancelException;
import ptolemy.util.FileUtilities;
import ptolemy.util.MessageHandler;
import ptolemy.util.StringUtilities;

///////////////////////////////////////////////////////////////////
//// JavaScriptApplication

/** 
 * Evaluate the arguments, which are expected to be JavaScript files
 * that define Accessors. 
 * 
 * <p>To invoke:</p>
 * <pre> 
 * cd $PTII/org/terraswarm/accessor/accessors/web/hosts
 * $PTII/bin/ptinvoke ptolemy.actor.lib.jjs.JavaScriptApplication ../test/TestComposite.js
 * </pre>
 *
 * @author Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class JavaScriptApplication {

    /** Evaluate the files named by the arguments.
     *  @param args An array of one or more file names.
     *  @exception IllegalActionException If the Nashorn engine cannot be found.
     *  @exception IOException If a file cannot be read or closed.
     *  @exception ScriptException If there is a problem evaluating a file.
     */
    public JavaScriptApplication(String [] args) throws IllegalActionException, IOException, ScriptException {

        ScriptEngineManager factory = new ScriptEngineManager();
        // Create a Nashorn script engine
        ScriptEngine engine = factory.getEngineByName("nashorn");
        if (engine == null) {
            // Coverity Scan is happier if we check for null here.
            throw new IllegalActionException(
                    "Could not get the nashorn engine from the javax.script.ScriptEngineManager.  Nashorn present in JDK 1.8 and later.");
        }

	// Evaluate the contents of basicFunctions.js
	BufferedReader bufferedReader = null;
	try {
	    bufferedReader = FileUtilities.openForReading(
							"$CLASSPATH/ptolemy/actor/lib/jjs/basicFunctions.js", null,
							null);
	    engine.eval(bufferedReader);
	} finally {
	    if (bufferedReader != null) {
		bufferedReader.close();
		bufferedReader = null;
	    }
	}


	InputStreamReader reader = null;
	// Evaluate the contentens of the files named by the args.
	// FIXME: Support -timeout nnnn.
	for (String arg: args) {
	    try {
		// Avoid FindBugs: Internationalization  (FB.DM_DEFAULT_ENCODING)
		reader =  new InputStreamReader(new FileInputStream(arg), "UTF-8");
		engine.eval(reader);
	    } finally {
		if (reader != null) {
		    reader.close();
		    reader = null;
		}
	    }
	}
	// FIXME: Should we close the engine in a finally block?
    }

    /** Invoke one or more JavaScript files.
     *  @param args One or more JavaScript files.
     */
    public static void main(String[] args) {
	try {
	    new JavaScriptApplication(args);
	} catch (Throwable throwable) {
	    System.err.println("Command Failed: " + throwable);
	    throwable.printStackTrace();
	    StringUtilities.exit(1);
	}
    }
}
