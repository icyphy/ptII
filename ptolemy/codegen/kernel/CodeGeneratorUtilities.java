/* Utilities for code generation.

 Copyright (c) 2002-2006 The Regents of the University of California.
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
package ptolemy.codegen.kernel;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ptolemy.data.expr.Variable;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.StringUtilities;

//////////////////////////////////////////////////////////////////////////
//// CodeGeneratorUtilities

/**
 Utilities that are useful for code generators.
 @author Christopher Hylands
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (cxh)
 @Pt.AcceptedRating Red (cxh)
 */
public class CodeGeneratorUtilities {

    /** Given a NamedObj, generate a HashMap containing String key/value
     *  pairs where each key is a Variable contained in the namedObj
     *  argument, and each value is the value of the Variable.
     *  @param namedObj The NamedObj that contains Variables
     *  @return The HashMap consisting of key/value Strings.
     *  @param IllegalActionException If there is a problem getting the
     *  Variables.   
     */
    public static HashMap newMap(NamedObj namedObj)
            throws IllegalActionException {
        HashMap substituteMap = new HashMap();
        Iterator attributes = namedObj.attributeList().iterator();

        while (attributes.hasNext()) {
            Attribute attribute = (Attribute) attributes.next();

            if (attribute instanceof Variable) {
                Variable variable = (Variable) attribute;

                // If getToken() fails, make sure that you are calling
                // setExpression with a string that has double quotes.
                String value = variable.getToken().toString();

                // Strip out any leading and trailing double quotes
                if (value.startsWith("\"") && (value.length() > 2)) {
                    value = value.substring(1, value.length() - 1);
                }

                substituteMap.put("@" + variable.getName() + "@", value);
            }
        }

        //         System.out.println("The map for " + namedObj +":");
        //         Iterator keys = substituteMap.keySet().iterator();
        //         while (keys.hasNext()) {
        //             String key = (String)keys.next();
        //             System.out.println(key + "\t" + (String)substituteMap.get(key));
        //         }
        return substituteMap;
    }

    /** Given a string that names a file or URL, try to
     *  open as a file, and then as a URL.
     *  @param inputFileName The name of the file or URL to open
     *  @return A BufferedReader that refers to the inputFileName
     *  @exception FileNotFoundException If the file cannot be found.
     *  @exception IOException If there were problems creating
     *  the BufferedReader.
     */
    public static BufferedReader openAsFileOrURL(String inputFileName)
            throws FileNotFoundException, IOException {
        BufferedReader inputFile;

        try {
            inputFile = new BufferedReader(new FileReader(inputFileName));
        } catch (IOException ex) {
            // Try it as a resource
            URL inputFileURL = Thread.currentThread().getContextClassLoader()
                    .getResource(inputFileName);

            if (inputFileURL == null) {
                throw ex;
            }

            inputFile = new BufferedReader(new InputStreamReader(inputFileURL
                    .openStream()));
        }

        return inputFile;
    }

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
    public static String substitute(String input, Map substituteMap) {
        // At first glance it would appear that we could use StringTokenizer
        // however, the token is really the String @codeBase@, not
        // the @ character.  StringTokenizer has problems with
        // "@codebase", which reports as having one token, but
        // should not be substituted since it is not "@codebase@"
        Iterator keys = substituteMap.keySet().iterator();

        while (keys.hasNext()) {
            String key = (String) keys.next();
            input = StringUtilities.substitute(input, key,
                    (String) substituteMap.get(key));
        }

        return input;
    }

    /** Read in the contents of inputFileName, and for each Parameter
     *  in namedObj, search for strings like
     *  <code>@<i>ParameterName</i>@</code> in inputFileName, and
     *  substitute in the value of the Parameter and return the results.
     *
     *  @param inputFileName  The name of the file to read from.
     *  @param namedObj The NamedObj that contains Parameters to
     *  be searched for in inputFileName.
     *  @return The contents of inputFileName after doing the substitutions
     *  @exception FileNotFoundException If the input file cannot be found.
     *  @exception IOException If there is a problem creating the
     *  substitution map.   
     */
    public static String substitute(String inputFileName, NamedObj namedObj)
            throws FileNotFoundException, IOException {
        Map substituteMap;

        try {
            substituteMap = newMap(namedObj);
        } catch (IllegalActionException ex) {
            // IOException does not have a constructor that takes a
            // cause argument.
            IOException exception = new IOException("Problem generating a "
                    + "substitution map for " + namedObj.getName());
            exception.initCause(ex);
            throw exception;
        }

        URL inputFileURL = Thread.currentThread().getContextClassLoader()
                .getResource(inputFileName);

        if (inputFileURL == null) {
            throw new FileNotFoundException("Failed to find '" + inputFileName
                    + "' as a resource");
        }

        BufferedReader inputReader = new BufferedReader(new InputStreamReader(
                inputFileURL.openStream()));
        String inputLine;
        StringBuffer output = new StringBuffer();
        String lineSeparator = System.getProperty("line.separator");

        while ((inputLine = inputReader.readLine()) != null) {
            output.append(substitute(inputLine + lineSeparator, substituteMap));
        }

        inputReader.close();
        return output.toString();
    }

    /** Read in the contents of inputFile, and replace each matching
     *  String key found in substituteMap with the corresponding
     *  String value and write the results to outputFileName.
     *  @param inputFile A BufferedReader that refers to the file to be
     *  read in.
     *  @param substituteMap The Map of String keys like "@codeBase@"
     *  and String values like "../../..".
     *  @param outputFileName The name of the file to write to.
     *  @see #substitute(String, Map, String)
     */
    public static void substitute(BufferedReader inputFile, Map substituteMap,
            String outputFileName) throws FileNotFoundException, IOException {
        PrintWriter outputFile = new PrintWriter(new BufferedWriter(
                new FileWriter(outputFileName)));
        String inputLine;

        while ((inputLine = inputFile.readLine()) != null) {
            outputFile.println(substitute(inputLine, substituteMap));
        }

        inputFile.close();
        outputFile.close();
    }

    /** Read in the contents of inputFileName, and replace each
     *  matching String key found in substituteMap with the
     *  corresponding String value and write the results to
     *  outputFileName.
     *  @param inputFileName  The name of the file to read from.
     *  @param substituteMap The Map of String keys like "@codeBase@"
     *  and String values like "../../..".
     *  @param outputFileName The name of the file to write to.
     *  @see #substitute(BufferedReader, Map, String)
     */
    public static void substitute(String inputFileName, Map substituteMap,
            String outputFileName) throws FileNotFoundException, IOException {
        BufferedReader inputFile = openAsFileOrURL(inputFileName);
        substitute(inputFile, substituteMap, outputFileName);
    }
}
