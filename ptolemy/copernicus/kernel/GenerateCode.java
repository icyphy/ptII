/* Standalone application that generates code

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

import ptolemy.kernel.util.StringUtilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;

//////////////////////////////////////////////////////////////////////////
//// GenerateCode
/** Standalone application that generates code using the Ptolemy II
code generation system.  This class acts a wrapper for the Main class
by providing defaults arguments for the various backends.  The default
arguments are read in from a GenerateCode.in file, variables are
substituted and the command executed.

<p>For example:
<pre>
java -classpath $PTII ptolemy.copernicus.kernel.GenerateCode java foo.xml
</pre>
Will read in the $PTII/ptolemy/copernicus/java/GenerateCode.in, substitute
in the appropriate variables and then generate code for foo.xml

<p>The first argument must name a directory in 
<code>ptolemy/copernicus/</code> that contains a 
<code>GenerateCode.in</code> file.
<p> The second argument must name a .xml file as a relative pathname.

<p>Generating code is fairly complex, so there are many other variables
that can be set as the other arguments.  The general format is
<code>-<i>variableValue</i> <i>variableValue</i></code>, for example:
<code>-JAVA c:/jdk1.3.0/bin/java</code>

<p>Below are the variables that have defaults.  See the 
<code>GenerateCode.in</code> variable for other variables.

<dl>
<dd> <code>@JAVA@</code>
<dt> The path to the java interpreter.
<br>Defaults to the value of the <code>java.home</code> property +
<code>bin/java</code>, for example:
<code>c:/jdk1.3.1/bin/java</code>

<dd> <code>@JVM_SIZE@</code>
<dt> The command line arguments to pass to the java interpreter that
control the size.
<br> Defaults to <code>-Xmx256m</code>.

<dd> <code>@PTII@</code>
<dt> The location of the Ptolemy II tree
<br> Defaults to the value of the ptolemy.ptII.dir property, which
should correspond with  <code>$PTII</code> or, if ptolemy.ptII.dir
is not set, then the value location of the top of the Ptolemy II tree
which is determined by looking for the
<code>ptolemy/kernel/NamedObj.class</code> resource.

<dd> <code>CLASSPATH</code>
<dt> Defaults to the value of <code>@PTII@</code>
<dd> <code>CLASSPATHSEPARATOR</code>
<dt> The classpath separator
Under Windows, the default is <code>;</code>, und
<br> Under Unix, the default is <code>:</code>

<dt> <code>@JAVA_SYSTEM_JAR@</code>
<dd> Defaults to the value of the <code>java.home</code> property 
+ <code>lib/rt.jar</code>
<dd> <code>SOOT_DIR</code>
<dt> Defaults to the value of <code>@PTII@/lib</code>

<dd> <code>SOOT_CLASSES</code>
<dt> Defaults to 
@SOOT_DIR@/sootclasses.jar@CLASSPATHSEPARATOR@@SOOT_DIR@/jasminclasses.jar@CLASSPATHSEPARATOR@@JAVA_SYSTEM_JAR@
<dt> <code>@ROOT@</code>
<dd> The top level directory to write the code in.
The code will appear in <code>@ROOT/@TARGET_PATH</code>
<dt> <code>@WATCH_DOG_TIMEOUT@</code>
<dd> The number of milliseconds that code generation will run for.
<br> Defaults to 600000, which is 10 minutes.

<dt> <code>@MODEL@</code>
<dd> The name of the model, as it appears in the .xml file.
The name should be a well structured java identifier.
   <menu>
   <li>Start with a letter, ideally capitalized
   <li>contain no spaces
   </menu>
<br> <b>This is the only variable that has no useful default</b>
Usually, the value of this variable is set as the second command line argument.

<dt> <code>@TARGET_PACKAGE@</code>
<dd> The package to generate code in with the model name appended.
<br> Defaults to <code>ptolemy.copernicus.java.cg.@MODEL@</code>
<dt> <code>@TARGET_PATH@</code>
<dd> The path representation of the @TARGET_PATH@.  
<br> Defaults to the value of @TARGET_PATH@ with slashes substituted
for the dots, for example
<code>ptolemy/copernicus/java/cg/@MODEL@</code>
<dt> <code>@ITERATIONS</code>
<dd> Defaults to <code>1000</code>
<dt> <code>@ITERATIONS_PARAMETER@</code>
<dd> Defaults to <code>,iterations:@ITERATIONS@</code>.
Set this to the empty string if the model has the number of iterations built.
<dt> <code>@SOOT_USER_ARGS@</code>
<dd> User arguments to be passed to soo
<br> The default is the empty string.
</dl>

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class GenerateCode {
    /** Given a string and a Map containing String key/value pairs,
     *  substitute any keys found in the input with the corresponding
     *  values.
     *
     *  @param input The input string that contains substrings
     *  like "@codeBase@".
     *  @param substituteMap The Map of String keys like "@codeBase@"
     *  and String values like "../../..".
     *  @return  A string with the keys properly substituted with
     *  their corresponding values.
     */
    public static String substitute(String input,
            Map substituteMap) {

	// At first glance it would appear that we could use StringTokenizer
	// however, the token is really the String @codeBase@, not
	// the @ character.  StringTokenizer has problems with
	// "@codebase", which reports as having one token, but
	// should not be substituted since it is not "@codebase@"

	Iterator keys = substituteMap.keySet().iterator();

	while (keys.hasNext()) {
	    String key = (String)keys.next();
	    input = StringUtilities.substitute(input, key,
                    (String)substituteMap.get(key));
	}
	return input;
    }

    /** Read in the contents of inputFileName, and replace each matching
     *	String key found in substituteMap with the corresponding String value.
     *
     *  @param inputFileName  The name of the file to read from.
     *  @param substituteMap The Map of String keys like "@codeBase@"
     *  and String values like "../../..".
     *  @param outputFileName The name of the file to write to.
     */
    public static void substitute(String inputFileName,
            Map substituteMap,
            String outputFileName)
            throws FileNotFoundException, IOException {
	BufferedReader inputFile =
	    new BufferedReader(new FileReader(inputFileName));
	PrintWriter outputFile =
	    new PrintWriter(new BufferedWriter(new FileWriter(outputFileName)));
	String inputLine;
	while ( (inputLine = inputFile.readLine()) != null) {
	    outputFile.println(substitute(inputLine, substituteMap));
 	}
	inputFile.close();
	outputFile.close();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Map used to map @model@ to MyModel.
    private Map _substituteMap;

    // The value of the ptolemy.ptII.dir property.
    private String _ptIIDirectory;

}
