/* Output elements of date (year, month, day, hour, etc.) from date.

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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DateToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.SingletonParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DateElements

/**
  Output elements of date (year, month, day, hour, etc.) from date.

 @author Patricia Derler
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (pd)
 @Pt.AcceptedRating Red (pd)
 */
public class DateElements extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DateElements(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DATE);

        year = new TypedIOPort(this, "year", false, true);
        year.setTypeEquals(BaseType.INT);
        new SingletonParameter(year, "_showName").setToken(BooleanToken.TRUE);

        month = new TypedIOPort(this, "month", false, true);
        month.setTypeEquals(BaseType.INT);
        new SingletonParameter(month, "_showName").setToken(BooleanToken.TRUE);

        day = new TypedIOPort(this, "day", false, true);
        day.setTypeEquals(BaseType.INT);
        new SingletonParameter(day, "_showName").setToken(BooleanToken.TRUE);

        hour = new TypedIOPort(this, "hour", false, true);
        hour.setTypeEquals(BaseType.INT);
        new SingletonParameter(hour, "_showName").setToken(BooleanToken.TRUE);

        minute = new TypedIOPort(this, "minute", false, true);
        minute.setTypeEquals(BaseType.INT);
        new SingletonParameter(minute, "_showName").setToken(BooleanToken.TRUE);

        second = new TypedIOPort(this, "second", false, true);
        second.setTypeEquals(BaseType.INT);
        new SingletonParameter(second, "_showName").setToken(BooleanToken.TRUE);

        millisecond = new TypedIOPort(this, "millisecond", false, true);
        millisecond.setTypeEquals(BaseType.INT);
        new SingletonParameter(millisecond, "_showName")
                .setToken(BooleanToken.TRUE);

        microsecond = new TypedIOPort(this, "microsecond", false, true);
        microsecond.setTypeEquals(BaseType.INT);
        new SingletonParameter(microsecond, "_showName")
                .setToken(BooleanToken.TRUE);

        nanosecond = new TypedIOPort(this, "nanosecond", false, true);
        nanosecond.setTypeEquals(BaseType.INT);
        new SingletonParameter(nanosecond, "_showName")
                .setToken(BooleanToken.TRUE);

        timezone = new TypedIOPort(this, "timezone", false, true);
        timezone.setTypeEquals(BaseType.STRING);
        new SingletonParameter(timezone, "_showName")
                .setToken(BooleanToken.TRUE);
        
        timeInMillis = new TypedIOPort(this, "timeInMillis", false, true);
        timeInMillis.setTypeEquals(BaseType.LONG);
        new SingletonParameter(timeInMillis, "_showName")
                .setToken(BooleanToken.TRUE);
    }

    /** Input for date token.
     */
    public TypedIOPort input;

    /** Year of date received on input.
     */
    public TypedIOPort year;

    /** Month of date received on input.
     */
    public TypedIOPort month;

    /** Day of the month of date received on input.
     */
    public TypedIOPort day;

    /** Hour of date received on input.
     */
    public TypedIOPort hour;

    /** Minute of date received on input.
     */
    public TypedIOPort minute;

    /** Second of date received on input.
     */
    public TypedIOPort second;

    /** Millisecond of date received on input.
     */
    public TypedIOPort millisecond;

    /** Microsecond of date received on input.
     */
    public TypedIOPort microsecond;

    /** Nanosecond of date received on input.
     */
    public TypedIOPort nanosecond;
    
    /** Time in UTC milliseconds since epoch.
     */
    public TypedIOPort timeInMillis;

    /** Time zone of date received on input.
     */
    public TypedIOPort timezone;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output all elements of a date if input has a date token.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            Token token = input.get(0);
            if (token instanceof DateToken) {
                DateToken dateToken = (DateToken) token;
                year.send(0, new IntToken(dateToken.getYear()));
                month.send(0, new IntToken(dateToken.getMonth()));
                day.send(0, new IntToken(dateToken.getDay()));
                hour.send(0, new IntToken(dateToken.getHour()));
                minute.send(0, new IntToken(dateToken.getMinute()));
                second.send(0, new IntToken(dateToken.getSecond()));
                millisecond.send(0, new IntToken(dateToken.getMillisecond()));
                microsecond.send(0, new IntToken(dateToken.getMicrosecond()));
                nanosecond.send(0, new IntToken(dateToken.getNanosecond()));
                timezone.send(0, new StringToken(dateToken.getTimezoneID()));
                timeInMillis.send(0, new LongToken(dateToken.getCalendarInstance().getTimeInMillis()));
            }
        }
    }
}
