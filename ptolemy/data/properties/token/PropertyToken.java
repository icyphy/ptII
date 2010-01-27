/*
 * Copyright (c) 2007-2009 The Regents of the University of California. All
 * rights reserved.
 * 
 * Permission is hereby granted, without written agreement and without license
 * or royalty fees, to use, copy, modify, and distribute this software and its
 * documentation for any purpose, provided that the above copyright notice and
 * the following two paragraphs appear in all copies of this software.
 * 
 * IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR
 * DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT
 * OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF
 * CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN
 * "AS IS" BASIS, AND THE UNIVERSITY OF CALIFORNIA HAS NO OBLIGATION TO PROVIDE
 * MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
 */
package ptolemy.data.properties.token;

import ptolemy.data.DoubleToken;
import ptolemy.data.FloatToken;
import ptolemy.data.Token;
import ptolemy.data.properties.Property;

///////////////////////////////////////////////////////////////////
//// PropertyToken

/**
   A property that represents a Ptolemy Token.
   @author Man-Kit Leung
   @version $Id$
   @since Ptolemy II 7.1
   @Pt.ProposedRating Red (mankit)
   @Pt.AcceptedRating Red (mankit)
*/
public class PropertyToken extends Property {

    /** Construct a PropertyToken.
     *  @param token The Ptolemy Token.
     */
    public PropertyToken(Token token) {
        _token = token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * Return true if the given object is equal to this lattice property. Two
     * token properties are considered equal if they contain the same data
     * token.
     */
    public boolean equals(Object object) {
        if (object instanceof PropertyToken) {
            PropertyToken property = (PropertyToken) object;
            if (property.getToken() instanceof DoubleToken
                    || property.getToken() instanceof FloatToken) {

                // need to do string compare because of truncated floating point
                // numbers in MoML file
                return _token.toString().equals(property.getToken().toString());
            } else {
                // compare tokens, not values!
                return _token.equals(property._token);
            }
        }
        return false;
    }

    /**
     * Return a hash code value for this token. This method returns the hash
     * code of the contained token.
     * @return A hash code value for this token.
     */
    public int hashCode() {
        return _token.hashCode();
    }

    /** Return true if the property argument is a PropertyToken.
     *  @param property The property to be checked.   
     *  @return True if the property argument is a PropertyToken.
     */
    public boolean isCompatible(Property property) {
        // FIXME: what is the difference betwee isCompatible() and isSubstitutionInstance?
        return property instanceof PropertyToken;
    }

    /** Return true if this property is a constant.
     *  @return Always return true in this base class.
     */
    public boolean isConstant() {
        return true;
    }

    /** Return true if this property is instantiable.
     *  @return Always return true in this base class.
     */
    public boolean isInstantiable() {
        return true;
    }

    /** Return true if the property argument is a PropertyToken.
     *  @param property The property to be checked.   
     *  @return True if the property argument is a PropertyToken.
     */
    public boolean isSubstitutionInstance(Property property) {
        // FIXME: what is the difference betwee isCompatible() and isSubstitutionInstance?
        return property instanceof PropertyToken;
    }

    /** Return the token with which this property was created.
     *  @return The token.   
     */   
    public Token getToken() {
        return _token;
    }

    /** Return the string value of this property.
     *  @return The string value of this property.  If
     *  the underlying token is null, then the empty string
     *  is returned.  Otherwise, the string value of the token
     *  is returned.
     */ 
    public String toString() {
        // FIXME: Charles Shelton 05/27/09 - How do we distinguish between an empty string token and an unresolved property (null) token?
        return _token == null ? "" : _token.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The token represented by this property. */
    private final Token _token;
}
