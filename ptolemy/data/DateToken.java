/* A token that contains a date.

   @Copyright (c) 2008-2013 The Regents of the University of California.
   All rights reserved.

   Permission is hereby granted, without written agreement and without
   license or royalty fees, to use, copy, modify, and distribute this
   software and its documentation for any purpose, provided that the
   above copyright notice and the following two paragraphs appear in all
   copies of this software.

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

package ptolemy.data;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

/** A token that contains a date.
 * @author Patricia Derler, Christopher  based on DateToken in Kepler by Daniel Crawl and Christopher Brooks
 * @version $Id: DateToken.java 24000 2010-04-28 00:12:36Z berkley $
 * @since Ptolemy II 10.
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DateToken extends AbstractConvertibleToken {

    /** Construct a date token. The current time is used for the date.
     */
    public DateToken() {
        _value = new Date();
    }

    /** Construct a token with a specified java.util.Date.
     *  @param value The specified java.util.Date type to construct the 
     *    token with.
     */
    public DateToken(Date value) {
        _value = value;
    }
	
    /** Construct a DateToken that represents the time at the specified
     *  number of milliseconds since January 1, 1970.
     *  @param value The number of milliseconds since January 1, 1970.
     */
    public DateToken(long value) {
        _value = new Date(value);
    }
	
    /** Construct a DateToken that represents the time specified 
     *  as a string.
     *  @param value The date specified in a format acceptable
     *  to java.text.DateFormat.
     *  @exception IllegalActionException If the date is not
     *  parseable by java.text.DateFormat.
     */
    public DateToken(String value) throws IllegalActionException {
        if (value == null) {
            _isNil = false;
            _value = null;
            return;
        }

        if (value.equals(_NIL)) {
            _isNil = true;
            _value = null;
            return;
        }


        DateFormat df = DateFormat.getDateInstance();
        try {
            _value = df.parse(value);
        } catch (ParseException e) {
            throw new IllegalActionException("The date value " + value + " could not be parsed");
        }
    }

    /**
     * Return the type of this token.
     * @return {@link #DATE}, the least upper bound of all the date types.
     */
    public Type getType() {
        return BaseType.DATE;
    }

    /**
     * Return the java.util.Date value of this token.
     * @return The java.util.Date that this Token was created with.
     */
    public Date getValue() {
        return _value;
    }

    /** Return true if the token is nil, (aka null or missing).
     *  Nil or missing tokens occur when a data source is sparsely populated.
     *  To create a nil DateToken, call new DateToken("nil");
     *  @return True if the token is the {@link #NIL} token.
     */
    public boolean isNil() {
        return _isNil;
    }

    /**
     * Return a String representation of the DateToken. The string is surrounded
     * by double-quotes; without them, the Ptolemy expression parser fails to
     * parse it.
     * 
     * @return A String representation of the DateToken.
     */
    public String toString() {
        if (isNil()) {
            return _NIL;
        }
        if (_value == null) {
            // FindBugs prefers that toString() not return null.
            return "null";
        } else {
            return "\"" + _value.toString() + "\"";
        }
    }

    /** A token that represents a missing value.
     *  Null or missing tokens are common in analytical systems
     *  like R and SAS where they are used to handle sparsely populated data
     *  sources.  In database parlance, missing tokens are sometimes called
     *  null tokens.  Since null is a Java keyword, we use the term "nil".
     *  The toString() method on a nil token returns the string "nil".
     */
    public static final DateToken NIL;

    // This is defined here so as to avoid a forward refererence error.
    /** The String value of a nil token. 
     */
    private static final String _NIL = "nil";

    static {
        try {
            NIL = new DateToken(_NIL);
        } catch (IllegalActionException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////               protected methods                           ////

    @Override
    protected Token _add(Token rightArgument) throws IllegalActionException {
        // FIXME: for addition, we could convert the Date tokens to longs and
        // do the math there.

        // FIXME: handle nil

        // FIXME: int and long are convertible
        return null;
    }

    @Override
    protected Token _divide(Token rightArgument) throws IllegalActionException {
        // FIXME: It only makes sense to divide a Date by an integer?

        // FIXME: handle nil.

        return null;
    }

    @Override
    protected BooleanToken _isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        // FIXME: If we convert the two tokens to longs and the
        // epsilon to a long, then this might make sense?
        // However, double is not losslessly convertible to long?
        // Probably throw an IllegalActionException here.

        // FIXME: handle nil.

        return null;
    }

    /** Return true of the the value of this token is equal
     *  to the value of the argument according to java.util.Date.
     *  Two DateTokens are considered equal if the their values
     *  are non-null and the java.util.Date.equals() method returns
     *  true
     *  @param rightArgument The token with which to test equality.
     *  @exception IllegalActionException Not thrown in this baseclass
     */
    protected BooleanToken _isEqualTo(Token rightArgument)
        throws IllegalActionException {

        // FIXME: LongToken and IntToken are convertible to Date
        if (!(rightArgument instanceof DateToken)) {
            return BooleanToken.FALSE;            
        }
        if (isNil() || rightArgument.isNil()) {
            return BooleanToken.FALSE;
        }

        Date rightDate = ((DateToken) rightArgument).getValue();
        Date leftDate = getValue();
        if (rightDate == null || leftDate == null) {
            return BooleanToken.FALSE;
        }

        if (leftDate.compareTo(rightDate) == 0) {
            return BooleanToken.TRUE;
        }

        return BooleanToken.FALSE;
    }

    @Override
    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        // FIXME: If we convert to longs then is does this make sense?
        // For example, say we had a date that represented a period of 
        // time and wanted to know if it was within a certain month, would
        //that be like modulo
        return null;
    }

    @Override
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        // FIXME: probably does not make sense, throw an exception.
        return null;
    }

    @Override
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        // FIXME: If dates are addable, then they are probably subtractable?
        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////               private variables                           ////

    /** True if the value of this Date is missing. */
    private boolean _isNil = false;

    /** The java.util.Date */
    private Date _value;
}