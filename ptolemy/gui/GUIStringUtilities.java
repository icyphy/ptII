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

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
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
     *  @see #split
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


    /** Tokenize a String to an array of Strings for use with
     *  Runtime.exec(String []).
     * 
     *  <p>Lines that begin with an octothorpe '#' are ignored.
     *  Substrings that start and end with a double quote are considered
     *  to be a single token and are returned as a single array element.
     *
     *  @param inputString  The String to tokenize
     *  @returns An array of substrings.
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

	streamTokenizer.wordChars(33, 127);

	// We can't use quoteChar here because it does backslash
	// substitution, so "c:\ptII" ends up as "c:ptII"
	// Substituting forward slashes for backward slashes seems like
	// overkill.
	// streamTokenizer.quoteChar('"');
	streamTokenizer.ordinaryChar('"');

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
		token = Double.toString(streamTokenizer.nval);
		commandList.add(token);
		break;
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
