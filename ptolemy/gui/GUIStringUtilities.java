/* Utilities used by ptolemy.gui.* for manipulating strings.

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
@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.gui;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// GUIStringUtilities
/**
A collection of utilities for manipulating strings.

In theory, these methods should be in
{@link ptolemy.kernel.util.StringUtilities}, but ptolemy.gui gets shipped
without StringUtilities, so we include them here.

@author Christopher Hylands
@version $Id$
@since Ptolemy II 2.0
*/
public class GUIStringUtilities {

    /** Instances of this class cannot be created.
     */
    private GUIStringUtilities() {
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
	    System.setProperty("ptolemy.ptII.dir", home);
	    return home;
        }
	return property;
    }

    /**  If the string is longer than 80 characters, split it up by
     *  displaying adding newlines every 80 characters.
     *  If the <i>longName</i> argument is null, then the string
     *  "<Unnamed>" is returned.
     *  @see GUIStringUtilities#abbreviate
     *  @param longName The string to optionally split up
     *  @return Either the original string, or the string with newlines
     *  inserted
     */
    public static String split(String longName) {
	// In theory, this method should be in
	// ptolemy.kernel.util.StringUtilities, but ptolemy.gui gets shipped
	// without StringUtilities, so we include it here

        if (longName == null) {
            return "<Unnamed>";
        }
	if (longName.length() < 80) {
	    return longName;
	}
	
	StringBuffer results = new StringBuffer();
	int i;
	for(i = 0; i < longName.length() - 80; i+=80) {
	    results.append(longName.substring(i, i+79) + "\n");
	}								   
	results.append(longName.substring(i));

	return results.toString();
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
		    if( token.length() > 0 ) {
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
}
