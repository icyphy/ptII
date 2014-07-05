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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DateToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DateElements

/**
  Output elements of date (year, month, day, hour, etc.) from date.

 @author Patricia Derler
 @version $Id: AbsoluteValue.java 65768 2013-03-07 03:33:00Z cxh $
 @since Ptolemy II 10.
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
        
        month = new TypedIOPort(this, "month", false, true);
        month.setTypeEquals(BaseType.INT);
        
        day = new TypedIOPort(this, "day", false, true);
        day.setTypeEquals(BaseType.INT);
        
        hour = new TypedIOPort(this, "hour", false, true);
        hour.setTypeEquals(BaseType.INT);
        
        minute = new TypedIOPort(this, "minute", false, true);
        minute.setTypeEquals(BaseType.INT);
        
        second = new TypedIOPort(this, "second", false, true);
        second.setTypeEquals(BaseType.INT);
        
        millisecond = new TypedIOPort(this, "millisecond", false, true);
        millisecond.setTypeEquals(BaseType.INT);
        
        microsecond = new TypedIOPort(this, "microsecond", false, true);
        microsecond.setTypeEquals(BaseType.INT);
        
        nanosecond = new TypedIOPort(this, "nanosecond", false, true);
        nanosecond.setTypeEquals(BaseType.INT);
        
        timezone = new TypedIOPort(this, "timezone", false, true);
        timezone.setTypeEquals(BaseType.STRING);
        
    }
    
    public TypedIOPort input;
    public TypedIOPort year;
    public TypedIOPort month;
    public TypedIOPort day;
    public TypedIOPort hour;
    public TypedIOPort minute;
    public TypedIOPort second;
    public TypedIOPort millisecond;
    public TypedIOPort microsecond;
    public TypedIOPort nanosecond;
    public TypedIOPort timezone;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Output all elements of a date if input has a date token.
     *  @exception IllegalActionException If there is no director.
     */
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
            }
        }
    }
}
