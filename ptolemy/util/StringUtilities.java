/* Utilities used to manipulate strings.

 Copyright (c) 2002-2003 The Regents of the University of California.
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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.util;

// Note that classes in ptolemy.util do not depend on any
// other ptolemy packages.

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

//////////////////////////////////////////////////////////////////////////
//// StringUtilities
/**
A collection of utilities for manipulating strings.
These utilities do not depend on any other ptolemy.* packages.


@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.1
*/
public class StringUtilities {

    /** Instances of this class cannot be created.
     */
    private StringUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Abbreviate a string.
     *  If the string is longer than 80 characters, truncate it by
     *  displaying the first 37 chars, then ". . .", then the last 38
     *  characters.
     *  If the <i>longName</i> argument is null, then the string
     *  "<Unnamed>" is returned.
     *  @see #split(String longName)
     *  @return The name.
     */
    public static String abbreviate(String longName) {
        if (longName == null) {
            return "<Unnamed>";
        }
        if (longName.length() < 80) {
            return longName;
        }
        return longName.substring(0,37) + ". . ."
            + longName.substring(longName.length() - 38);
    }

    /** Return a string with a maximum line length of <i>length</i>
     *  characters, limited to the given number of characters. 
     *  If there are more than 10 newlines, then the string is truncated
     *  after 10 lines.
     *  If the string is truncated, an ellipsis will be appended to
     *  the end  of the string.
     *  @param string The string to truncate.
     *  @param length The length to which to truncate the string.
     */
    public static String ellipsis(String string, int length) {
        string = StringUtilities.split(string);

        // Third argument being true means return the delimiters as tokens.
        StringTokenizer tokenizer = new StringTokenizer(string, "\n", true);
        // If there are more than 10 lines and 10 newlines, return
        // truncate after the first 20 lines and newlines. 
        // This is necessary so that we can deal with very long lines
        // of text without spaces.
        if (tokenizer.countTokens() > 20) {
            StringBuffer results = new StringBuffer();
            for (int i = 0; i < 20 && tokenizer.hasMoreTokens(); i++) {
                results.append(tokenizer.nextToken());
            }
            results.append("..."); 
            string = results.toString();
        }

        if (string.length() > length) {
            return string.substring(0, length-3) + "...";
        }
        return string;
    }

    /** Given a string, replace all the instances of XML special characters
     *  with their corresponding XML entities.  This is necessary to
     *  allow arbitrary strings to be encoded within XML.  This method
     *  <pre>
     *  &amp; becomes &amp;amp;
     *  " becomes &amp;quot;
     *  < becomes &amp;lt;
     *  > becomes &amp;gt;
     *  newline becomes &amp;#10;
     *  </pre>
     *
     *  @param string The string to escape.
     *  @return A new string with special characters replaced.
     */
    public static String escapeForXML(String string) {
        string = substitute(string, "&", "&amp;");
        string = substitute(string, "\"", "&quot;");
        string = substitute(string, "<", "&lt;");
        string = substitute(string, ">", "&gt;");
        string = substitute(string, "\n", "&#10;");
        return string;
    }

    /** Return a number of spaces that is proportional to the argument.
     *  If the argument is negative or zero, return an empty string.
     *  @param level The level of indenting represented by the spaces.
     *  @return A string with zero or more spaces.
     */
    public static String getIndentPrefix(int level) {
        if (level <= 0) {
            return "";
        }
        StringBuffer result = new StringBuffer("");
        for (int i = 0; i < level; i++) {
            result.append("    ");
        }
        return result.toString();
    }

    /** Return the preferences directory, creating it if necessary.
     *  @return A string naming the preferences directory.  The last
     *  character of the string will have the file.separator character
     *  appended.
     *  @exception IOException If the directory could not be created.
     *  @see #PREFERENCES_DIRECTORY
     */
    public static String preferencesDirectory() throws IOException {
        String preferencesDirectoryName =
            StringUtilities.getProperty("user.home")
            + StringUtilities.getProperty("file.separator")
            + StringUtilities.PREFERENCES_DIRECTORY
            + StringUtilities.getProperty("file.separator");
        File preferencesDirectory = new File(preferencesDirectoryName);
        if (!preferencesDirectory.isDirectory()) {
            if (preferencesDirectory.mkdirs() == false) {
                throw new IOException("Could not create user preferences "
                        + "directory '"
                        + preferencesDirectoryName + "'");

            }
        }
        return preferencesDirectoryName;
    }

    /** Get the specified property from the environment. An empty string
     *  is returned if the argument environment variable does not exist,
     *  though if certain properties are not defined, then we
     *  make various attempts to determine them and then set them.
     *  See the javadoc page for java.util.System.getProperties() for
     *  a list of system properties.
     *  <p>The following properties are handled specially
     *  <dl>
     *  <dt> "ptolemy.ptII.dir"
     *  <dd> vergil usually sets the ptolemy.ptII.dir property to the
     *  value of $PTII.  However, if we are running under Web Start,
     *  then this property might not be set, in which case we look
     *  for "ptolemy/kernel/util/NamedObj.class" and set the
     *  property accordingly.
     *  <dt> "ptolemy.ptII.dirAsURL"
     *  <dd> Return $PTII as a URL.  For example, if $PTII was c:\ptII,
     *  then return file:/c:/ptII/.
     *  <dt> "user.dir"
     *  <dd> Return the canonical path name to the current working directory.
     *  This is necessary because under JDK1.4.1 System.getProperty()
     *  returns <code><b>c</b>:/<i>foo</i></code>
     *  whereas most of the other methods that operate
     *  on path names return <code><b>C</b>:/<i>foo</i></code>.
     *  </dl>
     *  @param propertyName The name of property.
     *  @return A String containing the string value of the property.
     */
    public static String getProperty(String propertyName) {
        // NOTE: getProperty() will probably fail in applets, which
        // is why this is in a try block.
        String property = null;
        try {
            property = System.getProperty(propertyName);
        } catch (SecurityException security) {
            if (!propertyName.equals("ptolemy.ptII.dir")) {
                throw new RuntimeException("Could not find '"
                        + propertyName + "' System property", security);
            }
        }
        if (propertyName.equals("user.dir")) {
            try {
                File userDirFile = new File(property);
                return userDirFile.getCanonicalPath();
            } catch (IOException ex) {
                return property;
            }
        }
        if (property != null) {
            return property;
        }
        if (propertyName.equals("ptolemy.ptII.dirAsURL")) {
            // Return $PTII as a URL.  For example, if $PTII was c:\ptII,
            // then return file:/c:/ptII/
            File ptIIAsFile = new File(getProperty("ptolemy.ptII.dir"));

            try {
                URL ptIIAsURL = ptIIAsFile.toURL();
                return ptIIAsURL.toString();
            } catch (java.net.MalformedURLException malformed) {
                throw new RuntimeException(
                        "While trying to find '" + propertyName
                        + "', could not convert '"
                        + ptIIAsFile + "' to a URL",
                        malformed);
            }
        }

        if (propertyName.equals("ptolemy.ptII.dir")) {
            String namedObjPath = "ptolemy/kernel/util/NamedObj.class";
            String home = null;
            // PTII variable was not set
            URL namedObjURL =
                Thread.currentThread().getContextClassLoader()
                .getResource(namedObjPath);

            if (namedObjURL != null) {
                String namedObjFileName = namedObjURL.getFile().toString();
                // FIXME: How do we get from a URL to a pathname?
                if (namedObjFileName.startsWith("file:")) {
                    // We get rid of either file:/ or file:\
                    namedObjFileName = namedObjFileName.substring(6);
                }
                String abnormalHome = namedObjFileName.substring(0,
                        namedObjFileName.length()
                        - namedObjPath.length());

                // abnormalHome will have values like: "/C:/ptII/"
                // which cause no end of trouble, so we construct a File
                // and call toString().

                home = (new File(abnormalHome)).toString();

                // If we are running under Web Start, then strip off
                // the trailing "!"
                if (home.endsWith("!")) {
                    home =
                        home.substring(0, home.length() - 1);
                }

                // Web Start
                String ptsupportJarName = File.separator + "DMptolemy"
                    + File.separator + "RMptsupport.jar";
                if (home.endsWith(ptsupportJarName)) {
                    home =
                        home.substring(0, home.length()
                                - ptsupportJarName.length());
                }

                ptsupportJarName = File.separator + "ptolemy"
                    + File.separator + "ptsupport.jar";
                if (home.endsWith(ptsupportJarName)) {
                    home =
                        home.substring(0, home.length()
                                - ptsupportJarName.length());
                }
            }

            if (home == null) {
                throw new RuntimeException(
                        "Could not find "
                        + "'ptolemy.ptII.dir'"
                        + " property.  Also tried loading '"
                        + namedObjPath + "' as a resource and working from that. "
                        + "Vergil should be "
                        + "invoked with -Dptolemy.ptII.dir"
                        + "=\"$PTII\"");
            }
            try {
                System.setProperty("ptolemy.ptII.dir", home);
            } catch (SecurityException security) {
                // Ignore, we are probably running as an applet or -sandbox
            }
            return home;
        }
        if (property == null) {
            return "";
        }

        return property;
    }

    /** Sanitize a String so that it can be used as a Java identifier.
     *  Section 3.8 of the Java language spec says:
     *  <blockquote>
     *  "An identifier is an unlimited-length sequence of Java letters
     *  and Java digits, the first of which must be a Java letter. An
     *  identifier cannot have the same spelling (Unicode character
     *  sequence) as a keyword (3.9), boolean literal (3.10.3), or
     *  the null literal (3.10.7).  "
     *  </blockquote>
     *  Java characters are A-Z, a-z, $ and _.
     *  <p> Characters that are not permitted in a Java identifier are changed
     *  to an underscores.
     *  This method does not check that the returned string is a
     *  keyword or literal.
     *  Note that two different strings can sanitize to the same
     *  string.
     *  This method is commonly used during code generation to map the
     *  name of a ptolemy object to a valid identifier name.
     *  @param name A string with spaces and other characters that
     *  cannot be in a Java name.
     *  @return A String that follows the Java identifier rules.
     */
    public static String sanitizeName(String name) {
        char [] nameArray = name.toCharArray();
        for (int i = 0; i < nameArray.length; i++) {
            if (!Character.isJavaIdentifierPart(nameArray[i])) {
                nameArray[i] = '_';
            }
        }
        if (!Character.isJavaIdentifierStart(nameArray[0])) {
            return "_" + new String(nameArray);
        } else {
            return new String(nameArray);
        }
    }

    /**  If the string is longer than 79 characters, split it up by
     *  displaying adding newlines in all newline delimited substrings
     *  that are longer than 79 characters. 
     *  If the <i>longName</i> argument is null, then the string
     *  "<Unnamed>" is returned.
     *  @see #abbreviate(String longName)
     *  @param longName The string to optionally split up
     *  @return Either the original string, or the string with newlines
     *  inserted
     */
    public static String split(String longName) {
        if (longName == null) {
            return "<Unnamed>";
        }
        if (longName.length() < 80) {
            return longName;
        }

        StringBuffer results = new StringBuffer();

        // The third argument is true, which means return the delimiters
        // as part of the tokens.
        StringTokenizer tokenizer =
            new StringTokenizer(longName, "\r\n", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            int i = 0;
            while (i < token.length() - 79) {
                // We look for the space from the end of the first 79
                // characters.  If we find one, then we use that
                // as the place to insert a newline.
                int lastSpaceIndex
                    = token.substring(i, i + 79).lastIndexOf(" ");
                if (lastSpaceIndex < 0 ) {
                    // No space found, just insert a new line after 79.
                    results.append(token.substring(i, i + 79) + "\n");
                    i += 79;
                } else {
                    results.append(token.substring(i,
                            i + lastSpaceIndex) + "\n");
                    i += lastSpaceIndex + 1;
                }
            }
            results.append(token.substring(i));
        }
        return results.toString();
    }

    /** Replace all occurrences of <i>pattern</i> in the specified
     *  string with <i>replacement</i>.  Note that the pattern is NOT
     *  a regular expression, and that relative to the
     *  String.replaceAll() method in jdk1.4, this method is extremely
     *  slow.
     *  @param string The string to edit.
     *  @param old The string to replace.
     *  @param replacement The string to replace it with.
     *  @return A new string with the specified replacements.
     */
    public static String substitute(String string,
            String pattern, String replacement) {
        int start = string.indexOf(pattern);
        while (start != -1) {
            StringBuffer buffer = new StringBuffer(string);
            buffer.delete(start, start + pattern.length());
            buffer.insert(start, replacement);
            string = new String(buffer);
            start = string.indexOf(pattern, start + replacement.length());
        }
        return string;
    }

    /** Perform file prefix substitution.

     *  If <i>string</i> starts with <i>prefix<i>, then we return a
     *  new string that consists of the value or <i>replacement</i>
     *  followed by the value of <i>string</i> with the value of
     *  <i>prefix</i> removed.  For example,
     *  substituteFilePrefix("c:/ptII", "c:/ptII/ptolemy, "$PTII")
     *  will return "$PTII/ptolemy"
     *
     *  <p>If <i>prefix</i> is not a simple prefix of <i>string</i>, then
     *  we use the file system to find the canonical names of the files.
     *  For this to work, <i>prefix<i> and <i>string</i> should name
     *  files that exist, see java.io.File.getCanonicalFile() for details.
     * 
     *  <p>If <i>prefix</i> is not a prefix of <i>string</i>, then
     *  we return <i>string<i>
     *  
     *  @param prefix The prefix string, for example, "c:/ptII".
     *  @param string The string to be substituted, for example,
     *  "c:/ptII/ptolemy".
     *  @param replacement The replacement to be substituted in, for example,
     *  "$PTII"
     *  @return The possibly substituted string.
     */   
    public static String substituteFilePrefix(String prefix,
            String string, String replacement) {

        // This method is currently used by $PTII/util/testsuite/auto.tcl

        if (string.startsWith(prefix)) {
            // Hmm, what about file separators?
            return replacement + string.substring(prefix.length());
        } else {
            try {
                String prefixCanonicalPath =
                    (new File(prefix)).getCanonicalPath();

                String stringCanonicalPath =
                    (new File(string)).getCanonicalPath();

                if (stringCanonicalPath.startsWith(prefixCanonicalPath)) {
                    return replacement
                        + stringCanonicalPath.substring(
                                prefixCanonicalPath.length());
                }
            } catch (Throwable throwable) {
                // ignore.
            }
        }
        return string;
    }

    /** Tokenize a String to an array of Strings for use with
     *  Runtime.exec(String []).
     *
     *  <p>Lines that begin with an octothorpe '#' are ignored.
     *  Substrings that start and end with a double quote are considered
     *  to be a single token and are returned as a single array element.
     *
     *  @param inputString  The String to tokenize
     *  @return An array of substrings.
     *  @exception IOException Thrown if StreamTokenizer.nextToken() throws it.
     */
    public static String [] tokenizeForExec(String inputString)
            throws IOException {

        // The java.lang.Runtime.exec(String command) call uses
        // java.util.StringTokenizer() to parse the command string.
        // Unfortunately, this means that double quotes are not handled
        // in the same way that the shell handles them in that 'ls "foo
        // 'bar"' will interpreted as three tokens 'ls', '"foo' and
        // 'bar"'.  In the shell, the string would be two tokens 'ls' and
        // '"foo bar"'.  What is worse is that the exec() behaviour is
        // slightly different under Windows and Unix.  To solve this
        // problem, we preprocess the command argument using
        // java.io.StreamTokenizer, which converts quoted substrings into
        // single tokens.  We then call java.lang.Runtime.exec(String []
        // commands);

        // Parse the command into tokens
        List commandList = new LinkedList();

        StreamTokenizer streamTokenizer =
            new StreamTokenizer(new StringReader(inputString));

        // We reset the syntax so that we don't convert to numbers,
        // otherwise, if PTII is "d:\\tmp\\ptII\ 2.0", then
        // we have no end of problems.
        streamTokenizer.resetSyntax();
        streamTokenizer.whitespaceChars(0 , 32);
        streamTokenizer.wordChars(33, 127);

        // We can't use quoteChar here because it does backslash
        // substitution, so "c:\ptII" ends up as "c:ptII"
        // Substituting forward slashes for backward slashes seems like
        // overkill.
        // streamTokenizer.quoteChar('"');
        streamTokenizer.ordinaryChar('"');

        streamTokenizer.eolIsSignificant(true);

        streamTokenizer.commentChar('#');

        // Current token
        String token = "";

        // Single character token, usually a -
        String singleToken = "";

        // Set to true if we are inside a double quoted String.
        boolean inDoubleQuotedString = false;

        while (streamTokenizer.nextToken()
                != StreamTokenizer.TT_EOF) {
            switch (streamTokenizer.ttype) {
            case StreamTokenizer.TT_WORD:
                if (inDoubleQuotedString) {
                    if ( token.length() > 0 ) {
                        token += " ";
                    }
                    token += singleToken + streamTokenizer.sval;
                } else {
                    token = singleToken + streamTokenizer.sval;
                    commandList.add(token);
                }
                singleToken = "";
                break;
            case StreamTokenizer.TT_NUMBER:
                throw new RuntimeException("Internal error: Found TT_NUMBER: '"
                        + streamTokenizer.nval + "'.  We should not be "
                        + "tokenizing numbers");
                //break;
            case StreamTokenizer.TT_EOL:
                break;
            case StreamTokenizer.TT_EOF:
                break;
            default:
                singleToken =
                    (new Character((char)streamTokenizer.ttype)).toString();
                if (singleToken.equals("\"")) {
                    if (inDoubleQuotedString) {
                        commandList.add(token);
                    }
                    inDoubleQuotedString = ! inDoubleQuotedString;
                    singleToken = "";
                    token = "";
                }
                break;
            }

        }

        return
            (String [])commandList.toArray(new String[commandList.size()]);
    }


    /** Return a string that contains a description of how to use this
     *  class.
     *  @param commnandTemplate  A string naming the command and the
     *  format of the arguments, for example
     *  "moml [options] [file . . .]"
     *  @param commandOptions A 2xN array of Strings that list command-line
     *  options that take arguments where the first
     *  element is a String naming the command line option, and the
     *  second element is the argument, for example
     *  <code>{"-class", "<classname>")</code>
     *  @param commandFlags An array of Strings that list command-line
     *  options that are either present or not.
     *  @returns A string that descripts the command.
     */
    public static String usageString(String commandTemplate,
            String [][] commandOptions, String [] commandFlags) {
        // This method is static so that we can reuse it in places
        // like copernicus/kernel/Copernicus and actor/gui/MoMLApplication
        String result = "Usage: " + commandTemplate + "\n\n"
            + "Options that take values:\n";

        int i;
        for (i = 0; i < commandOptions.length; i++) {
            result += " " + commandOptions[i][0] +
                " " + commandOptions[i][1] + "\n";
        }
        result += "\nBoolean flags:\n";
        for (i = 0; i < commandFlags.length; i++) {
            result += " " + commandFlags[i];
        }
        return result;
    }
    ///////////////////////////////////////////////////////////////////
    ////                         public variables                  ////

    // If you change these, be sure to try running vergil on
    // a HSIF moml file
    // vergil ../hsif/demo/SwimmingPool/SwimmingPool.xml 
    /** Maximum length in characters of a long string before 
     *  {@link #ellipse(String, int)} truncates and add a
     *  trailing . . .
     */ 
    public static final int ELLIPSIS_LENGTH_LONG = 2000;

    /** Maximum length in characters of a short string before 
     *  {@link #ellipse(String, int)} truncates and add a
     *  trailing . . .
     */ 
    public static final int ELLIPSIS_LENGTH_SHORT = 400;

    /** Location of Application preferences such as the user library.
     *  @see #preferencesDirectory()
     */
    public static String PREFERENCES_DIRECTORY = ".ptolemyII";


}
