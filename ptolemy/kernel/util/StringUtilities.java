/* Utilities for manipulating strings.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

package ptolemy.kernel.util;

import java.io.File;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// StringUtilities
/**
A collection of utilities for manipulating strings.

@see ptolemy.gui.GUIStringUtilities
@author Steve Neuendorffer, Christopher Hylands
@version $Id$
@since Ptolemy II 1.0
*/
public class StringUtilities {

    /** Instances of this class cannot be created.
     */
    private StringUtilities() {
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Given a string, replace all the instances of XML special characters
     *  with their corresponding XML entities.  This is necessary to
     *  allow arbitrary strings to be encoded within XML.  This method
     *  <pre>
     *  & becomes &amp;amp;
     *  " becomes &amp;quot;
     *  < becomes &amp;lt;
     *  > becomes &amp;gt;
     *  newline becomes &#10;
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
		throw new InternalErrorException(null, security,
						 "Could not find '"
						 + propertyName
						 + "' System property");
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
                throw new InternalErrorException(null, malformed,
                        "While trying to find '" + propertyName 
                        + "', could not convert '"
                        + ptIIAsFile + "' to a URL");
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
		throw new InternalErrorException(null, null,
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

    /** Replace all occurrences of <i>old</i> in the specified
     *  string with <i>replacement</i>.
     *  @param string The string to edit.
     *  @param old The string to replace.
     *  @param replacement The string to replace it with.
     *  @return A new string with the specified replacements.
     */
    public static String substitute(String string,
            String old, String replacement) {
        int start = string.indexOf(old);
        while (start != -1) {
            StringBuffer buffer = new StringBuffer(string);
            buffer.delete(start, start + old.length());
            buffer.insert(start, replacement);
            string = new String(buffer);
            start = string.indexOf(old, start + replacement.length());
        }
        return string;
    }
}
