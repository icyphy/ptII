/* Construct date token by parsing all date elements (year, day, month, ...).

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
package ptolemy.actor.lib;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.parameters.PortParameter;
import ptolemy.data.BooleanToken;
import ptolemy.data.DateToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
  Construct date token by parsing all date elements (year, day, month, ...).

 @author Patricia Derler
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (pd)
 @Pt.AcceptedRating Red (pd)
 */
public class DateConstructor extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DateConstructor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DATE);

        year = new PortParameter(this, "year", new IntToken(0));
        new SingletonParameter(year.getPort(), "_showName").setToken(BooleanToken.TRUE);

        month = new PortParameter(this, "month", new IntToken(0));
        month.setTypeEquals(BaseType.INT);
        new SingletonParameter(month.getPort(), "_showName").setToken(BooleanToken.TRUE);

        day = new PortParameter(this, "day", new IntToken(1));
        day.setTypeEquals(BaseType.INT);
        new SingletonParameter(day.getPort(), "_showName").setToken(BooleanToken.TRUE);

        hour = new PortParameter(this, "hour", new IntToken(0));
        hour.setTypeEquals(BaseType.INT);
        new SingletonParameter(hour.getPort(), "_showName").setToken(BooleanToken.TRUE);

        minute = new PortParameter(this, "minute", new IntToken(0));
        minute.setTypeEquals(BaseType.INT);
        new SingletonParameter(minute.getPort(), "_showName").setToken(BooleanToken.TRUE);

        second = new PortParameter(this, "second", new IntToken(0));
        second.setTypeEquals(BaseType.INT);
        new SingletonParameter(second.getPort(), "_showName").setToken(BooleanToken.TRUE);

        millisecond = new PortParameter(this, "millisecond", new IntToken(0));
        millisecond.setTypeEquals(BaseType.INT);
        new SingletonParameter(millisecond.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        microsecond = new PortParameter(this, "microsecond", new IntToken(0));
        microsecond.setTypeEquals(BaseType.INT);
        new SingletonParameter(microsecond.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        nanosecond = new PortParameter(this, "nanosecond", new IntToken(0));
        nanosecond.setTypeEquals(BaseType.INT);
        new SingletonParameter(nanosecond.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        timeZone = new PortParameter(this, "timeZone", new StringToken("+0000"));
        timeZone.setTypeEquals(BaseType.STRING);
        new SingletonParameter(timeZone.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);

        useTimeInMillis = new Parameter(this, "useTimeInMillis", new BooleanToken(false));
        useTimeInMillis.setTypeEquals(BaseType.BOOLEAN);
        
        timeInMillis = new PortParameter(this, "timeInMillis", new IntToken(0));
        timeInMillis.setTypeEquals(BaseType.LONG);
        new SingletonParameter(timeInMillis.getPort(), "_showName")
                .setToken(BooleanToken.TRUE);
        
        precision = new StringParameter(this, "precision");
        precision.addChoice("second");
        precision.addChoice("millisecond");
        precision.addChoice("microsecond");
        precision.addChoice("nanosecond");
        precision.setExpression("millisecond");
    }

    /** The output for the constructed date.
     */
    public TypedIOPort output;

    /** The year.
     */
    public PortParameter year;

    /** The month.
     */
    public PortParameter month;

    /** The day of the month.
     */
    public PortParameter day;

    /** The hour of the day.
     */
    public PortParameter hour;

    /** The minutes.
     */
    public PortParameter minute;

    /** The seconds.
     */
    public PortParameter second;

    /** The milliseconds.
     */
    public PortParameter millisecond;

    /** The microseconds.
     */
    public PortParameter microsecond;

    /** The nanoseconds.
     */
    public PortParameter nanosecond;

    /** The time zone.
     */
    public PortParameter timeZone;

    /** The time as a long value representing the milliseconds since
     *  January 1, 1970.
     */
    public PortParameter timeInMillis;
    
    public Parameter useTimeInMillis;

    /** The precision of the date. The precision defaults to
     *  milliseconds.
     */
    public Parameter precision;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Construct a date token with all tokens present. If a token
     *  for the long value is present, use this token and time zone
     *  as well as precision. Otherwise use tokens on other inputs to
     *  create ports.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        DateToken dateToken = null;
        int datePrecision = DateToken.PRECISION_MILLISECOND;

        String precisionValue = ((StringToken) precision.getToken()).stringValue();
        if (precisionValue.equals("second")) {
            datePrecision = DateToken.PRECISION_SECOND;
        }
        if (precisionValue.equals("millisecond")) {
            datePrecision = DateToken.PRECISION_MILLISECOND;
        }
        if (precisionValue.equals("microsecond")) {
            datePrecision = DateToken.PRECISION_MICROSECOND;
        }
        if (precisionValue.equals("nanosecond")) {
            datePrecision = DateToken.PRECISION_NANOSECOND;
        } else {
            datePrecision = DateToken.PRECISION_MILLISECOND;
        }
        String timeZoneValue = _getStringValue(timeZone);
        long timeAsLongValue = _getLongValue(timeInMillis);
        int microsecondValue = _getIntValue(microsecond);
        int nanosecondValue = _getIntValue(nanosecond);
        
        if (!((BooleanToken)useTimeInMillis.getToken()).booleanValue()) {
            int yearValue = _getIntValue(year);
            int monthValue = _getIntValue(month);
            int dayValue = _getIntValue(day);
            int hourValue = _getIntValue(hour);
            int minuteValue = _getIntValue(minute);
            int secondValue = _getIntValue(second);
            int millisecondValue = _getIntValue(millisecond);
            
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, yearValue);
            c.set(Calendar.MONTH, monthValue);
            c.set(Calendar.DAY_OF_MONTH, dayValue);
            c.set(Calendar.HOUR, hourValue);
            c.set(Calendar.MINUTE, minuteValue);
            c.set(Calendar.SECOND, secondValue);
            c.set(Calendar.MILLISECOND, millisecondValue);
            //c.setTimeZone(TimeZone.getTimeZone(timeZoneValue));
            timeAsLongValue = c.getTimeInMillis();
        }
        dateToken = new DateToken(timeAsLongValue, datePrecision,
                TimeZone.getTimeZone("GMT" + timeZoneValue));
        dateToken.addMicroseconds(microsecondValue);
        dateToken.addNanoseconds(nanosecondValue);

        output.send(0, dateToken);
    }
    
    private int _getIntValue(PortParameter portParameter) throws IllegalActionException {
        int value = ((IntToken) portParameter.getToken()).intValue();
        if (portParameter.getPort().connectedPortList().size() > 0 &&
                portParameter.getPort().hasToken(0)) {
            value = ((IntToken)portParameter.getPort().get(0)).intValue();
        }
        return value;
    }
    
    private long _getLongValue(PortParameter portParameter) throws IllegalActionException {
        long value = ((LongToken) portParameter.getToken()).longValue();
        if (portParameter.getPort().connectedPortList().size() > 0 &&
                portParameter.getPort().hasToken(0)) {
            value = ((LongToken)portParameter.getPort().get(0)).longValue();
        }
        return value;
    }
    
    private String _getStringValue(PortParameter portParameter) throws IllegalActionException {
        String value = ((StringToken) portParameter.getToken()).stringValue();
        if (portParameter.getPort().connectedPortList().size() > 0 &&
                portParameter.getPort().hasToken(0)) {
            value = ((StringToken)portParameter.getPort().get(0)).stringValue();
        }
        return value;
    }
    
}
