/* A token that contains a set of label/token pairs.

 Copyright (c) 1997-2002 The Regents of the University of California.
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

@ProposedRating Red (neuendor@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/

package ptolemy.data;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.data.type.Type;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.XmlParser;

import org.w3c.dom.Document;



//////////////////////////////////////////////////////////////////////////
//// XmlToken
/**
A token that contains a xml document. 

Currently, no operations between function tokens (add, multiply, etc.)
are supported.

@author Yang Zhao
@version $Id$
@since Ptolemy II 1.0
*/

public class XmlToken extends Token {

    /** Construct an empty token.
     */
    public XmlToken() {
        super();
        _toString = null;
    }

    /** Construct an XmlToken from the specified string.
     *  @param xmlStr A string.
     *  @exception IllegalActionException If the string
     *  is not parsable.
     */
    public XmlToken(String xmlStr) throws Exception {
        XmlParser parser = new XmlParser();
        _doc = parser.parser(xmlStr);
        _toString = xmlStr;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
	/** Convert the specified token into an instance of XMLToken.
	 *  If the specified token is not an instance of XMLToken, 
	 *  an exception is thrown.
	 *  @param token The token to be converted to a XMLToken.
	 *  @return A XMLToken.
	 *  @exception IllegalActionException If the conversion
	 *   cannot be carried out.
	 */
    public static XmlToken convert(Token token)
            throws IllegalActionException {
        if (token instanceof XmlToken) {
            return (XmlToken)token;
        }

        throw new IllegalActionException(
                notSupportedConversionMessage(token, "xmltoken"));
    }

    /** Return the dom document parsed from the xml string.
     *  @return A Document.
     */
    public Document getDomTree() {
        return _doc;
    }

	/** Return the type of this token.
	 *  @return BaseType.XMLTOKEN.
	 */
    public Type getType() {
        return BaseType.XMLTOKEN;
    }

     /** Return the value of this Token as a string.
     *  @return A String.
     */
    public String toString() {
        return _toString;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private Document _doc;
    private String _toString;
}
