/* A token that contains a date.

   @Copyright (c) 2008-2014 The Regents of the University of California.
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.data.type.TypeLattice;
import ptolemy.graph.CPO;
import ptolemy.kernel.util.IllegalActionException;

/** A token that contains a date.
 *
 *  Note: Java 8 provides a much improved implementation of dates and times.
 *  This implementation should be upgraded eventually.
 * @author Patricia Derler, Christopher  based on DateToken in Kepler by Daniel Crawl and Christopher Brooks
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class DateToken extends AbstractConvertibleToken implements
        PartiallyOrderedToken {

    /** Construct a date token. The current time is used for the date,
     *  the default precision is milliseconds and the default time zone
     *  is the local time zone.
     */
    public DateToken() {
        this(Calendar.getInstance().getTimeInMillis(), PRECISION_MILLISECOND,
                TimeZone.getDefault());
    }

    /** Construct a DateToken that represents the time since January 1, 1970.
     *  The time zone defaults to the local time zone.
     *  @param value The time since January 1, 1970 in the default precision
     *  of milliseconds.
     */
    public DateToken(long value) {
        this(value, PRECISION_MILLISECOND, TimeZone.getDefault());
    }

    /** Construct a DateToken that represents the time since January 1, 1970.
     *  The time zone defaults to the local time zone.
     *  @param value The time since January 1, 1970 in the given precision.
     *  @param precision The precision.
     */
    public DateToken(long value, int precision) {
        this(value, precision, TimeZone.getDefault());
    }

    /** Construct a DateToken that represents the time since January 1, 1970.
     *  @param value The time since January 1, 1970 in the given precision.
     *  @param precision The precision.
     *  @param timeZone The string representation of the time zone ID.
     */
    public DateToken(long value, int precision, String timeZone) {
        _isNil = false;
        _precision = precision;
        _timeZone = TimeZone.getTimeZone(timeZone);
        _value = value;
    }

    /** Construct a DateToken that represents the time since January 1, 1970.
     *  @param value The time since January 1, 1970 in the given precision.
     *  @param precision The precision.
     *  @param timeZone The time zone.
     */
    public DateToken(long value, int precision, TimeZone timeZone) {
        _isNil = false;
        _precision = precision;
        _timeZone = timeZone;
        _value = value;
    }

    /** Construct a DateToken that represents the time specified as a
     *  string. The string is first parsed by the default
     *  java.text.DateFormat parser. Because we have up to nanosecond
     *  precision, we might have to
     *  pre-process the string and take out the digits representing
     *  nanoseconds and microseconds. Then any leading
     *  and trailing double quotes are removed and a
     *  java.text.SimpleDateFormat with a parser with the value of
     *  {@link #_SIMPLE_DATE_FORMAT} is used.
     *
     *  @param value The date specified in a format acceptable
     *  to java.text.DateFormat.
     *  @exception IllegalActionException If the date is not
     *  parseable by java.text.DateFormat.
     */
    public DateToken(String value) throws IllegalActionException {
        _precision = PRECISION_MILLISECOND;
        if (value == null) {
            _isNil = false;
            _value = 0l;
            return;
        }

        if (value.equals(_NIL)) {
            _isNil = true;
            _value = 0l;
            return;
        }
        String dateString = value;

        // Simple date format is not thread safe - intermediate parsing results are
        // stored in instance fields.
        synchronized (_SIMPLE_DATE_FORMAT) {
            try {
                // See https://stackoverflow.com/questions/4713825/how-to-parse-output-of-new-date-tostring
                // FIXME: this is probably Locale.US-specific

                // Remove leading and trailing double quotes.
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1, value.length() - 1);
                }
                Calendar calendar = Calendar.getInstance();
                // Parse dates in varying precision
                if (value.length() == _SIMPLE_DATE_FORMAT.length()) {
                    calendar.setTime(_simpleDateFormat.parse(value));
                    _value = calendar.getTimeInMillis();
                    _precision = PRECISION_MILLISECOND;
                } else if (value.length() == _SIMPLE_DATE_FORMAT.length() + 3) {
                    String micros = value.substring(value.indexOf(".") + 4,
                            value.indexOf(".") + 7);
                    value = value.substring(0, value.indexOf(".") + 4)
                            + value.substring(value.indexOf(".") + 7);
                    calendar.setTime(_simpleDateFormat.parse(value));
                    _value = calendar.getTimeInMillis();
                    _value = _value * 1000 + Integer.parseInt(micros);
                    _precision = PRECISION_MICROSECOND;
                } else if (value.length() == _SIMPLE_DATE_FORMAT.length() + 6) {
                    String micros = value.substring(value.indexOf(".") + 4,
                            value.indexOf(".") + 7);
                    String nanos = value.substring(value.indexOf(".") + 7,
                            value.indexOf(".") + 10);
                    value = value.substring(0, value.indexOf(".") + 4)
                            + value.substring(value.indexOf(".") + 10);
                    calendar.setTime(_simpleDateFormat.parse(value));
                    _value = calendar.getTimeInMillis();
                    _value = (_value * 1000 + Integer.parseInt(micros)) * 1000
                            + Integer.parseInt(nanos);
                    _precision = PRECISION_NANOSECOND;
                } else {
                    throw new IllegalActionException(null, "Unexpected date"
                            + "format: " + dateString + " is not formatted as "
                            + _SIMPLE_DATE_FORMAT);
                }
                
                String timeZoneOffset = value.substring(24, 29);
                _timeZone = TimeZone.getTimeZone("GMT" + timeZoneOffset);
                calendar.setTimeZone(_timeZone);
                _calendar = calendar;
            } catch (ParseException ex) {
                throw new IllegalActionException(null, ex, "The date value \""
                        + value + "\" could not be parsed to a Date."
                        + "Also tried parsing with the \""
                        + _simpleDateFormat.toPattern()
                        + "\" pattern, the exception was: " + ex.getMessage());
            }
        }
        _isNil = false;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Add nanoseconds to time. If the precision is less than nanoseconds,
     *  do nothing.
     *  @param nanoseconds The nanoseconds to add.
     */
    public void addNanoseconds(int nanoseconds) {
        if (_precision >= PRECISION_NANOSECOND) {
            _value += nanoseconds;
            if (nanoseconds >= 1000000) {
                _calendar.setTimeInMillis(getTimeInMilliseconds());
            }
        }
    }

    /** Add microseconds to time. If the precision is less than microseconds,
     *  do nothing.
     *  @param microseconds The microseconds to add.
     */
    public void addMicroseconds(int microseconds) {
        if (_precision == PRECISION_MICROSECOND) {
            _value += microseconds;
        } else if (_precision == PRECISION_NANOSECOND) {
            _value += microseconds * 1000;
        }
        if (_precision >= PRECISION_MICROSECOND && microseconds >= 1000) {
            _calendar.setTimeInMillis(getTimeInMilliseconds());
        }
    }

    /** Convert the specified token into an instance of DateToken.
     *  This method does lossless conversion.
     *  If the argument is already an instance of DateToken,
     *  it is returned without any change.  If the argument is
     *  a nil token, then a new nil Token is returned, see {@link
     *  #NIL}.  Otherwise, if the argument is below DateToken in the
     *  type hierarchy, it is converted to an instance of DateToken or
     *  one of the subclasses of DateToken and returned. If none of
     *  the above condition is met, an exception is thrown.
     *
     *  @param token The token to be converted to a DateToken.
     *  @return A DateToken.
     *  @exception IllegalActionException If the conversion
     *   cannot be carried out.
     */
    public static DateToken convert(Token token) throws IllegalActionException {
        if (token instanceof DateToken) {
            return (DateToken) token;
        }

        if (token.isNil()) {
            return DateToken.NIL;
        }

        int compare = TypeLattice.compare(BaseType.DATE, token);

        if (compare == CPO.LOWER || compare == CPO.INCOMPARABLE) {
            // We could try to create a DateToken from a String here,
            // but not all Strings are convertible to Dates.  Marten wrote:
            // "This seems wrong to me. It is not generally possible to
            // convert a String into a Date. Also, the type lattice
            // doesn't permit that conversion. Type inference is
            // supposed to yield a typing of which the automatic type
            // conversions that it imposes during run time work
            // without exception. We should not misuse the conversion
            // method to build a customized parser."
            throw new IllegalActionException(
                    notSupportedIncomparableConversionMessage(token, "date"));
        }

        compare = TypeLattice.compare(BaseType.STRING, token);

        if (compare == CPO.SAME || compare == CPO.HIGHER) {
            StringToken stringToken = StringToken.convert(token);
            DateToken result = new DateToken(stringToken.stringValue());
            return result;
        }

        throw new IllegalActionException(notSupportedConversionMessage(token,
                "date"));
    }

    /** Get the calendar instance representing this date.
     *  @return The calendar instance.
     */
    public Calendar getCalendarInstance() {
        if (_calendar == null) {
            _calendar = new GregorianCalendar(_timeZone);
            _calendar.setTimeInMillis(getTimeInMilliseconds());
            _calendar.setTimeZone(_timeZone);
        }
        return _calendar;
    }
    
    /** Create a DateToken with a value.
     *  @param value The date specified in a format acceptable
     *  to java.text.DateFormat.
     *  @return a DateToken.
     */
    public static DateToken date(String value) throws IllegalActionException {
        return new DateToken(value);
    }

    /** Get the date of the month part of this date.
     * @return The date of the month.
     */
    public int getDay() {
        Calendar calendar = getCalendarInstance();
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /** Get the hour part of this date.
     * @return The hour.
     */
    public int getHour() {
        Calendar calendar = getCalendarInstance();
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    /** Get the minute part of this date.
     * @return The minute.
     */
    public int getMinute() {
        Calendar calendar = getCalendarInstance();
        return calendar.get(Calendar.MINUTE);
    }

    /** Get the microsecond part of this date.
     * @return The microsecond.
     */
    public int getMicrosecond() {
        if (_precision < PRECISION_MICROSECOND) {
            return 0;
        } else if (_precision == PRECISION_MICROSECOND) {
            return (int) (_value % 1000);
        } else if (_precision == PRECISION_NANOSECOND) {
            return (int) ((_value % 1000000) / 1000);
        }
        return 0;
    }

    /** Get the millisecond part of this date.
     * @return The millisecond.
     */
    public int getMillisecond() {
        Calendar calendar = getCalendarInstance();
        return calendar.get(Calendar.MILLISECOND);
    }

    /** Get the month part of this date.
     * @return The month.
     */
    public int getMonth() {
        Calendar calendar = getCalendarInstance();
        return calendar.get(Calendar.MONTH);
    }

    /** Get the nanosecond part of this date.
     * @return The nanosecond.
     */
    public int getNanosecond() {
        if (_precision < PRECISION_NANOSECOND) {
            return 0;
        } else if (_precision == PRECISION_NANOSECOND) {
            return (int) (_value % 1000);
        }
        return 0;
    }

    /** Get the precision of this date.
     *  @return The precision.
     */
    public int getPrecision() {
        return _precision;
    }

    /** Get the second part of this date.
     * @return The second.
     */
    public int getSecond() {
        Calendar calendar = getCalendarInstance();
        return calendar.get(Calendar.SECOND);
    }

    /** Get the time zone of this date.
     * @return The time zone.
     */
    public TimeZone getTimeZone() {
        return _timeZone;
    }

    /** Get the time zone id of this date.
     * @return The time zone.
     */
    public String getTimezoneID() {
        Calendar c = getCalendarInstance();
        return c.getTimeZone().getDisplayName();
    }

    /** Get the time since January 1, 1970 in the given precision.
     * @return The time since Januarly 1, 1970.
     */
    public long getValue() {
        return _value;
    }

    /** Get time in milliseconds since January 1, 1970.
     *  @return The time as a long value.
     */
    public long getTimeInMilliseconds() {
        if (_precision == PRECISION_NANOSECOND) {
            return _value / 1000000;
        } else if (_precision == PRECISION_MICROSECOND) {
            return _value / 1000;
        } else if (_precision == PRECISION_MILLISECOND) {
            return _value;
        } else if (_precision == PRECISION_SECOND) {
            return _value * 1000;
        }
        return 0l;
    }

    /** Return the type of this token.
     *  @return {@link ptolemy.data.type.BaseType#DATE},
     *  the least upper bound of all the date types.
     */
    @Override
    public Type getType() {
        return BaseType.DATE;
    }

    /** Get the year of this date.
     * @return The year.
     */
    public int getYear() {
        Calendar calendar = getCalendarInstance();
        return calendar.get(Calendar.YEAR);
    }
    
    

    /** Check whether the value of this token is strictly greater than
     *  that of the argument token.  The argument and this token are
     *  converted to equivalent types, and then compared.  Generally,
     *  this is the higher of the type of this token and the argument
     *  type.  This method defers to the _isLessThan() method to perform
     *  a type-specific equality check.  Derived classes should
     *  implement that method to provide type specific actions for
     *  equality testing.
     *
     *  @param rightArgument The token to compare against.
     *  @return A boolean token with value true if this token has the
     *  same units as the argument, and is strictly greater than the
     *  argument.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public final BooleanToken isGreaterThan(PartiallyOrderedToken rightArgument)
            throws IllegalActionException {
        // Similar to the same method in ScalarToken.
        int typeInfo = TypeLattice.compare(getType(), (Token) rightArgument);

        if (typeInfo == CPO.SAME) {
            return ((DateToken) rightArgument)._doIsLessThan(this);
        } else if (typeInfo == CPO.HIGHER) {
            // This line is different from ScalarToken and causes problems with StringTokens.
            PartiallyOrderedToken convertedArgument = (PartiallyOrderedToken) getType()
                    .convert((Token) rightArgument);
            try {
                return convertedArgument.isLessThan(this);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "isGreaterThan", (Token) this, (Token) rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.isLessThan(this);
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "isGreaterThan", (Token) this, (Token) rightArgument));
        }
    }

    /** Check whether the value of this token is strictly less than that of the
     *  argument token.
     *
     *  Only a partial order is assumed, so !(a &lt; b) need not imply (a &ge; b).
     *
     *  @param rightArgument The token on greater than side of the inequality.
     *  @return BooleanToken.TRUE, if this token is less than the
     *    argument token. BooleanToken.FALSE, otherwise.
     *  @exception IllegalActionException If the tokens are incomparable.
     */
    @Override
    public BooleanToken isLessThan(PartiallyOrderedToken rightArgument)
            throws IllegalActionException {
        DateToken rightDateToken = null;
        try {
            rightDateToken = convert((Token) rightArgument);
        } catch (IllegalActionException ex) {
            //// FIXME: Since PartiallyOrderedToken is an interface, we cannot do:
            //throw new IllegalActionException(null, ex, notSupportedMessage(
            //        "isLessThan", this, rightArgument))
            //// and must do this instead:
            throw new IllegalActionException("Cannot compare ScalarToken with "
                    + rightArgument);
        }
        return isLessThan(rightDateToken);
    }

    /** Check whether the value of this token is strictly less than that of the
     *  argument token.
     *
     *  @param rightArgument The token to compare against.
     *  @return A boolean token with value true if this token is strictly
     *  less than the argument.
     *  @exception IllegalActionException If the argument token and
     *  this token are of incomparable types, or have different units,
     *  or the operation does not make sense for the given types.
     */
    public BooleanToken isLessThan(DateToken rightArgument)
            throws IllegalActionException {
        // FIXME: Copied from ScalarToken, but one line is different
        int typeInfo = TypeLattice.compare(getType(), rightArgument);

        if (typeInfo == CPO.SAME) {
            return _doIsLessThan(rightArgument);
        } else if (typeInfo == CPO.HIGHER) {
            DateToken convertedArgument = (DateToken) getType().convert(
                    rightArgument);
            try {
                return _doIsLessThan(convertedArgument);
            } catch (IllegalActionException ex) {
                // If the type-specific operation fails, then create a
                // better error message that has the types of the
                // arguments that were passed in.
                throw new IllegalActionException(null, ex, notSupportedMessage(
                        "isLessThan", this, rightArgument));
            }
        } else if (typeInfo == CPO.LOWER) {
            return rightArgument.isGreaterThan(this);
        } else {
            throw new IllegalActionException(notSupportedIncomparableMessage(
                    "isLessThan", this, rightArgument));
        }
    }

    /** Return true if the token is nil, (aka null or missing).
     *  Nil or missing tokens occur when a data source is sparsely populated.
     *  To create a nil DateToken, call new DateToken("nil");
     *  @return True if the token is the {@link #NIL} token.
     */
    @Override
    public boolean isNil() {
        return _isNil;
    }
    
    /** Return the value of the token as a String.
     *  @return The string value, which is the same as
     *  the value returned by {@link toString()}, except
     *  toString() wraps the string value in double quotes.
     */
    public String stringValue() {
        if (isNil()) {
            return _NIL;
        }
        Calendar c = getCalendarInstance();
        _simpleDateFormat.setTimeZone(_timeZone);
        String timeString = _simpleDateFormat.format(c.getTime());

        String beforeTimeZone = timeString.substring(0,
                timeString.lastIndexOf(" "));
        beforeTimeZone = beforeTimeZone.substring(0,
                beforeTimeZone.lastIndexOf(" "));

        String remainder = timeString.substring(beforeTimeZone.length());

        return beforeTimeZone + String.format("%03d", getMicrosecond())
                + String.format("%03d", getNanosecond()) + remainder;
    }

    /**
     * Return a String representation of the DateToken. The string is surrounded
     * by double-quotes; without them, the Ptolemy expression parser fails to
     * parse it.
     *
     * <p>Unfortunately, the Java Date class has a fatal flaw in that
     * Date.toString() does not return the value of the number of ms., so
     * we use a format that includes the number of ms.</p>
     *
     * @return A String representation of the DateToken.
     */
    @Override
    public String toString() {
        return "date(\"" + stringValue() + "\")";
    }

    /** A token that represents a missing value.
     *  Null or missing tokens are common in analytical systems
     *  like R and SAS where they are used to handle sparsely populated data
     *  sources.  In database parlance, missing tokens are sometimes called
     *  null tokens.  Since null is a Java keyword, we use the term "nil".
     *  The toString() method on a nil token returns the string "nil".
     */
    public static final DateToken NIL;

    /** The flag indicating that the the precision is seconds. */
    public static final int PRECISION_SECOND = 1;

    /** The flag indicating that the the precision is milliseconds. */
    public static final int PRECISION_MILLISECOND = 2;

    /** The flag indicating that the the precision is microseconds. */
    public static final int PRECISION_MICROSECOND = 3;

    /** The flag indicating that the the precision is nanoseconds. */
    public static final int PRECISION_NANOSECOND = 4;

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Subtract is not supported for Dates.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException Always thrown because
     *  multiplying a Date does not make sense.
     */
    @Override
    protected Token _add(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(null, notSupportedMessage("add", this,
                rightArgument));
    }

    /** Subtract is not supported for Dates.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException Always thrown because
     *  dividing a Date does not make sense.
     */
    @Override
    protected Token _divide(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(null, notSupportedMessage("divide",
                this, rightArgument));
    }

    /** The isCloseTo() method brings both tokens to the same precision. 
     *  Then compute difference between time value in given lower precision. 
     *  If difference is less than epsilon (casted to an int), return true.
     *  @param token The token to compare to this token
     *  @param epsilon the epsilon
     *  @return A new token containing the result.
     *  @exception IllegalActionException Always thrown because
     *  isCloseTo() on a Date does not make sense.
     */
    @Override
    protected BooleanToken _isCloseTo(Token token, double epsilon)
            throws IllegalActionException {
        // Christopher Brooks: If we convert the two tokens to longs and the
        // epsilon to a long, then this might make sense?
        // However, double is not losslessly convertible to long?
        // Probably throw an IllegalActionException here.
        
        // Patricia Derler - first version of an implementation of isCloseTo below.
        // First get both tokens to the same precision. Then compare the difference. 
        // If difference is less than epsilon (casted to an int), return true.
        
        DateToken dateToken = null;
        if (token instanceof StringToken) {
            dateToken = new DateToken(((StringToken)token).stringValue());
        } else if (token instanceof DateToken) {
            dateToken = (DateToken) token;
        } else {
            throw new IllegalActionException(null, "Cannot compute _isCloseTo for DateToken and " 
                    + token.getType());
        }
        long dateValue = dateToken._value;
        if (dateToken.getPrecision() > getPrecision()) {
            int precisionDifference = dateToken.getPrecision() - getPrecision();
            dateValue = (long) (dateValue / (long) Math.pow(1000, precisionDifference));
        }
        if (Math.abs(dateValue - _value) < epsilon) {
            return BooleanToken.TRUE;
        } else {
            return BooleanToken.FALSE;
        }
    }

    /** Return true of the the value of this token is equal
     *  to the value of the argument according to java.util.Date.
     *  Two DateTokens are considered equal if the their values
     *  are non-null and the java.util.Date.equals() method returns
     *  true.
     *  It is assumed that the type of the argument is the
     *  same as the type of this class.
     *  @param rightArgument The token with which to test equality.
     *  @return true if the right argument is equal to this token.
     *  @exception IllegalActionException Not thrown in this baseclass
     */
    @Override
    protected BooleanToken _isEqualTo(Token rightArgument)
            throws IllegalActionException {

        // The caller of this method should convert
        // the rightArgument to a DateToken, but we check anyway.
        if (!(rightArgument instanceof DateToken)) {
            return BooleanToken.FALSE;
        }
        DateToken rightArgumentDateToken = (DateToken) rightArgument;

        if (isNil() || rightArgument.isNil()) {
            return BooleanToken.FALSE;
        }

        Calendar left = getCalendarInstance();
        Calendar right = rightArgumentDateToken.getCalendarInstance();

        return BooleanToken.getInstance(left.compareTo(right) == 0
                && _getMicroAndNanoSeconds() == rightArgumentDateToken
                        ._getMicroAndNanoSeconds());
    }

    /** Test for ordering of the values of this Token and the argument
     *  Token.
     *  @param rightArgument The token to compare to this token.
     *  @exception IllegalActionException If this method is not
     *  supported by the derived class.
     *  @return A new Token containing the result.
     */
    protected BooleanToken _isLessThan(DateToken rightArgument)
            throws IllegalActionException {

        if (isNil() || rightArgument.isNil()) {
            return BooleanToken.FALSE;
        }

        Calendar left = getCalendarInstance();
        Calendar right = rightArgument.getCalendarInstance();

        return BooleanToken
                .getInstance(left.compareTo(right) < 0
                        || (left.compareTo(right) == 0 && _getMicroAndNanoSeconds() < rightArgument
                                ._getMicroAndNanoSeconds()));
    }

    /** Modulo is not supported for Dates.
     *  @param rightArgument The token to divide into this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException Always thrown because
     *  modulo of a Date does not make sense.
     */
    @Override
    protected Token _modulo(Token rightArgument) throws IllegalActionException {
        throw new IllegalActionException(null, notSupportedMessage("modulo",
                this, rightArgument));
    }

    /** Multiply is not supported for Dates.
     *  @param rightArgument The token to multiply this token by.
     *  @return A new token containing the result.
     *  @exception IllegalActionException Always thrown because
     *  multiplying a Date does not make sense.
     */
    @Override
    protected Token _multiply(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(null, notSupportedMessage("multiply",
                this, rightArgument));
    }

    /** Subtract is not supported for Dates.
     *  @param rightArgument The token to subtract from this token.
     *  @return A new token containing the result.
     *  @exception IllegalActionException Always thrown because
     *  subtracting a Date does not make sense.
     */
    @Override
    protected Token _subtract(Token rightArgument)
            throws IllegalActionException {
        throw new IllegalActionException(null, notSupportedMessage("subtract",
                this, rightArgument));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    // This is protected so that the DateToken(String) javadoc can refer
    // to it.
    /** The format in which dates are reported.  Milliseconds are included
     *  so that the toString() method returns a string that can be parsed
     *  to the same Date.
     */
    protected static final String _SIMPLE_DATE_FORMAT = "EEE MMM dd HH:mm:ss.SSS ZZZZZ yyyy";

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Test for ordering of the values of this Token and the argument
     *  Token.  It is guaranteed by the caller that the type and
     *  units of the argument is the same as the type of this class.
     *  This method may defer to the _isLessThan() method that takes a
     *  ScalarToken.  Derived classes should implement that method
     *  instead to provide type-specific operation.
     *  @param rightArgument The token with which to test ordering.
     *  @return A BooleanToken which contains the result of the test.
     *  @exception IllegalActionException If the units of the argument
     *  are not the same as the units of this token, or the method is
     *  not supported by the derived class or if either this token or
     *  the argument token is a nil token.
     */
    private BooleanToken _doIsLessThan(PartiallyOrderedToken rightArgument)
            throws IllegalActionException {
        if (isNil() || ((Token) rightArgument).isNil()) {
            throw new IllegalActionException(notSupportedMessage("isLessThan",
                    this, (Token) rightArgument)
                    + " because one or the other is nil");
        }

        DateToken convertedArgument = (DateToken) rightArgument;

        return _isLessThan(convertedArgument);
    }

    private long _getMicroAndNanoSeconds() {
        if (_precision == PRECISION_NANOSECOND) {
            return _value % 1000000;
        } else if (_precision == PRECISION_MICROSECOND) {
            return (_value % 1000) * 1000;
        }
        return 0l;
    }

    /** True if the value of this Date is missing. */
    private boolean _isNil = false;

    /** The String value of a nil token. */
    private static final String _NIL = "nil";

    /** The format used to read and write dates. */
    private SimpleDateFormat _simpleDateFormat = new SimpleDateFormat(
            _SIMPLE_DATE_FORMAT);

    /** The time in a given precision */
    private long _value;

    private int _precision;

    private Calendar _calendar;

    /** The time zone.
     */
    private TimeZone _timeZone;

    static {
        try {
            NIL = new DateToken(_NIL);
        } catch (IllegalActionException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }
}
