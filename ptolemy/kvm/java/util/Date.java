package java.util;

//import java.util.*;

/*
 * Date is designed to provide a standar calendar using integer math.
 * This is done by using a series of sub-epochs to reduce a date's numeric
 * value until it can be easily handled by int math.
 * Calculating the wekly sub-epoch requires only basic long operators:
 * addition and subtraction, which KVM currently supports.
 *
 * The class only supports positive time values.  This restriction does not
 * pose any problems for time values after 1970.
 */

public class Date
{
    // Useful millisecond and integer constants.
    public static final int  ONE_SECOND = 1000;
    public static final int  ONE_MINUTE = 60*ONE_SECOND;
    public static final int  ONE_HOUR   = 60*ONE_MINUTE;
    public static final long ONE_DAY    = 24L*ONE_HOUR;
    public static final long TWO_DAYS   = 2L*ONE_DAY;
    public static final long THREE_DAYS = 3L*ONE_DAY;
    public static final long ONE_WEEK   = 7L*ONE_DAY;
    public static final int  MONTH_DAYS = 28;
    public static final long ONE_MONTH  = MONTH_DAYS*ONE_DAY;
    public static final int  YEAR_DAYS        = 365;
    public static final long ONE_YEAR         = ONE_DAY*YEAR_DAYS;
    public static final int  LEAP_YEAR_DAYS   = YEAR_DAYS+1;
    public static final long ONE_LEAP_YEAR    = ONE_DAY*LEAP_YEAR_DAYS;
    public static final int  DECADE_DAYS      = (YEAR_DAYS*10)+2;
    public static final long ONE_DECADE       = ONE_DAY*DECADE_DAYS;
    // Decade that has an extra leap year
    public static final int  LEAP_DECADE_DAYS = DECADE_DAYS+1;
    public static final long ONE_LEAP_DECADE  = ONE_DAY*LEAP_DECADE_DAYS;

    public static final int EPOCH_YEAR = 1970;
    public static final int EPOCH_DAY = 4;

    public static final String DAYS[] = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
    public static final String MONTHS[] = { "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                                            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

    // The month length array values are the number of days to
    // be added to 28 to get the number of days in the month.
    // Apparently long array are not supported either - this
    // would have been easier if they were.
    //--------------------------------------------------------
    private static final int MONTH_LENGTH[]      = {3,0,3,2,3,2,3,3,2,3,2,3}; // 0-based
    private static final int LEAP_MONTH_LENGTH[] = {3,1,3,2,3,2,3,3,2,3,2,3}; // 0-based

    private int year = 0;
    private int month = 0;
    private int date = 0;
    private int day = 0;
    private int hour = 0;
    private int min = 0;
    private int sec = 0;
    private int millis = 0;

    private transient long fastTime;
    /**
     * Allocates a <code>InitDate</code> object and initializes it so that
     * it represents the time at which it was allocated, measured to the
     * nearest millisecond.
     *
     * @see     java.lang.System#currentTimeMillis()
     */
    public Date()
        {
        this(System.currentTimeMillis());
        }

    /**
     * Allocates a <code>Date</code> object and initializes it to
     * represent the specified number of milliseconds since the
     * standard base time known as "the epoch", namely January 1,
     * 1970, 00:00:00 GMT.
     *
     * @param   date   the milliseconds since January 1, 1970, 00:00:00 GMT.
     * @see     java.lang.System#currentTimeMillis()
     */
    public Date(long date)
        {
        synchronized ( DAYS )
            {
            setTime( date );
            }
        }

    /**
     * Allocates a <code>Date</code> object and initializes it so that
     * it represents midnight, local time, at the beginning of the day
     * specified by the <code>year</code>, <code>month</code>, and
     * <code>date</code> arguments.
     *
     * @param   year    the year minus 1900.
     * @param   month   the month between 0-11.
     * @param   date    the day of the month between 1-31.
     */
    public Date(int year, int month, int date) {
        this(year, month, date, 0, 0, 0);
    }

    /**
     * Allocates a <code>Date</code> object and initializes it so that
     * it represents the instant at the start of the second specified
     * by the <code>year</code>, <code>month</code>, <code>date</code>,
     * <code>hrs</code>, <code>min</code>, and <code>sec</code> arguments,
     * in the local time zone.
     *
     * @param   year    the year.
     * @param   month   the month between 0-11.
     * @param   date    the day of the month between 1-31.
     * @param   hrs     the hours between 0-23.
     * @param   min     the minutes between 0-59.
     * @param   sec     the seconds between 0-59.
     */
    public Date(int year, int month, int date, int hrs, int min, int sec)
        {
        this.year = year;
        this.month = month;
        this.date = date;
        this.hour = hrs;
        this.min = min;
        this.sec = sec;
        this.millis = 0;

        synchronized ( DAYS )
            {
            computeTime();
            }
        }

    /**
     * Tests if this date is after the specified date.
     *
     * @param   when   a date.
     * @return  <code>true</code> if and only if the instant represented
     *          by this <tt>Date</tt> object is strictly later than the
     *          instant represented by <tt>when</tt>;
     *          <code>false</code> otherwise.
     */
    public boolean after(Date when)
        {
        return getTime() > when.getTime();
        }

    /**
     * Tests if this date is before the specified date.
     *
     * @param   when   a date.
     * @return  <code>true</code> if and only if the instant of time
     *            represented by this <tt>Date</tt> object is strictly
     *            earlier than the instant represented by <tt>when</tt>;
     *          <code>false</code> otherwise.
     */
    public boolean before(Date when)
        {
        return getTime() < when.getTime();
        }

    /**
     * Compares two Dates for ordering.
     *
     * @param   anotherDate   the <code>Date</code> to be compared.
     * @return  the value <code>0</code> if the argument Date is equal to
     *          this Date; a value less than <code>0</code> if this Date
     *          is before the Date argument; and a value greater than
     *      <code>0</code> if this Date is after the Date argument.
     */
    public int compareTo(Date anotherDate)
        {
        long thisTime = this.getTime();
        long anotherTime = anotherDate.getTime();
        return (thisTime<anotherTime ? -1 : (thisTime==anotherTime ? 0 : 1));
        }

    /**
     * Compares this Date to another Object.  If the Object is a Date,
     * this function behaves like <code>compareTo(Date)</code>.  Otherwise,
     * it throws a <code>ClassCastException</code> (as Dates are comparable
     * only to other Dates).
     *
     * @param   o the <code>Object</code> to be compared.
     * @return  the value <code>0</code> if the argument is a Date
     *      equal to this Date; a value less than <code>0</code> if the
     *      argument is a Date after this Date; and a value greater than
     *      <code>0</code> if the argument is a Date before this Date.
     * @exception ClassCastException if the argument is not a
     *        <code>Date</code>.
     * @see     java.lang.Comparable
     * @since   JDK1.2
     */
    public int compareTo(Object o)
        {
        return compareTo((Date)o);
        }

    /**
     * Calculates the number of days between two dates.  This is done by
     * setting the time portion of the dates to zero, getting the time
     * difference between them, and counting the days in this difference.
     * The chronological order of the dates does not affect the results.
     *
     * @param   anotherDate   the <code>Date</code> to be used for calculation.
     * @return  The number of days between this Date and the Date passed.
     */
    public int daysBetween(Date anotherDate)
        {
        long timeDiff = 0;
        int dayDiff = 0;

        // set time portion of dates to zero so the calc
        // doesn't get an extra day
        //----------------------------------------------
        Date date1 = new Date( year, month, date );
        Date date2 = new Date( anotherDate.getYear(),
                               anotherDate.getMonth(),
                               anotherDate.getDate() );

        if ( date1.before( date2 ) )
            timeDiff = date2.getTime() - date1.getTime();
        else
            timeDiff = date1.getTime() - date2.getTime();

        for ( dayDiff = 0; timeDiff >= ONE_DECADE; dayDiff+=DECADE_DAYS )
            {
            timeDiff -= ONE_DECADE;
            }

        for ( dayDiff = 0; timeDiff >= ONE_YEAR; dayDiff+=YEAR_DAYS )
            {
            timeDiff -= ONE_YEAR;
            }

        for ( dayDiff = 0; timeDiff >= ONE_MONTH; dayDiff+=MONTH_DAYS )
            {
            timeDiff -= ONE_MONTH;
            }

        for ( dayDiff = 0; timeDiff >= ONE_DAY; dayDiff++ )
            {
            timeDiff -= ONE_DAY;
            }
        return dayDiff;
        }

    /**
     * Compares two dates for equality.
     * The result is <code>true</code> if and only if the argument is
     * not <code>null</code> and is a <code>Date</code> object that
     * represents the same point in time, to the millisecond, as this object.
     * <p>
     * Thus, two <code>Date</code> objects are equal if and only if the
     * <code>getTime</code> method returns the same <code>long</code>
     * value for both.
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     * @see     java.util.Date#getTime()
     */
    public boolean equals(Object obj)
        {
        return obj != null && obj instanceof Date && getTime() == ((Date) obj).getTime();
        }

    /**
     * Returns the number of milliseconds since January 1, 1970, 00:00:00 GMT
     * represented by this <tt>Date</tt> object.
     *
     * @return  the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *          represented by this date.
     */
    public long getTime()
        {
        return( fastTime );
        }

    /**
     * Returns a value that is the result of subtracting 1900 from the
     * year that contains or begins with the instant in time represented
     * by this <code>Date</code> object, as interpreted in the local
     * time zone.
     *
     * @return  the year represented by this date.
     */
    public int getYear()
        {
        return( year );
        }

    /**
     * Returns a number representing the month that contains or begins
     * with the instant in time represented by this <tt>Date</tt> object.
     * The value returned is between <code>0</code> and <code>11</code>,
     * with the value <code>0</code> representing January.
     *
     * @return  the month represented by this date.
     */
    public int getMonth()
        {
        return( month );
        }

    /**
     * Returns the day of the month represented by this <tt>Date</tt> object.
     * The value returned is between <code>1</code> and <code>31</code>
     * representing the day of the month that contains or begins with the
     * instant in time represented by this <tt>Date</tt> object.
     *
     * @return  the day of the month represented by this date.
     */
    public int getDate()
        {
        return( date );
        }

    /**
     * Returns the day of the week represented by this date. The
     * returned value (<tt>0</tt> = Sunday, <tt>1</tt> = Monday,
     * <tt>2</tt> = Tuesday, <tt>3</tt> = Wednesday, <tt>4</tt> =
     * Thursday, <tt>5</tt> = Friday, <tt>6</tt> = Saturday)
     * represents the day of the week that contains or begins with
     * the instant in time represented by this <tt>Date</tt> object.
     *
     * @return  the day of the week represented by this date.
     */
    public int getDay()
        {
        return( day );
        }

    /**
     * Returns the hour represented by this <tt>Date</tt> object. The
     * returned value is a number (<tt>0</tt> through <tt>23</tt>)
     * representing the hour within the day that contains or begins
     * with the instant in time represented by this <tt>Date</tt>
     * object.
     *
     * @return  the hour represented by this date.
     */
    public int getHours()
        {
        return( hour );
        }

    /**
     * Returns the number of minutes past the hour represented by this date.
     * The value returned is between <code>0</code> and <code>59</code>.
     *
     * @return  the number of minutes past the hour represented by this date.
     */
    public int getMinutes()
        {
        return( min );
        }

    /**
     * Returns the number of seconds past the minute represented by this date.
     * The value returned is between <code>0</code> and <code>59</code>.
     *
     * @return  the number of seconds past the minute represented by this date.
     */
    public int getSeconds()
        {
        return( sec );
        }

    /**
     * Returns the number of milliseconds past the second represented by this date.
     * The value returned is between <code>0</code> and <code>999</code>.
     *
     * @return  the number of milliseconds past the second represented by this date.
     */
    public int getMillis()
        {
        return( millis );
        }

    /**
     * Sets this <tt>Date</tt> object to represent a point in time that is
     * <tt>time</tt> milliseconds after January 1, 1970 00:00:00 GMT.
     *
     * @param   time   the number of milliseconds.
     */
    public void setTime(long time)
        {
            // should probably throw exception here
        if( time < 0 )
            return;
        fastTime = time;

        // Calc decade
        //------------
        long decadeLength;
        int incday;
        year = EPOCH_YEAR;
        day = EPOCH_DAY-1;
        for ( int i=0; i<20; i++ )  // 20 decades should be enough (year 3970)
            {
                if ( year%4 == 0 ||     // decade start on a leap year?
                        (year+1)%4 == 0 )   // second year a leap year?
                    {
                decadeLength = ONE_LEAP_DECADE;
                incday = 13;
                    }
            else
                {
                decadeLength = ONE_DECADE;
                incday = 12;
                }
            if ( time < decadeLength )
                break;
            time -= decadeLength;
            incrementDay( incday );
            year += 10;
            }

        // Calc year
        //----------
        long yearLength;
        for ( int i=0; i<10; i++ )
            {
                if ( year%4 == 0 )  // leap year?
                yearLength = ONE_LEAP_YEAR;
            else
                yearLength = ONE_YEAR;
                if ( (year-1)%4 == 0 )  // prior year a leap year?
                incrementDay( 2 );
            else
                incrementDay( 1 );
            if ( time < yearLength )
                break;
            time -= yearLength;
            year++;
            }

        boolean leapyear = (year%4 == 0);
        // Calc month
        //-----------
        month = 0;
        long monthLength;
        int daysadd = 0;
        for ( int i=0; i<12; i++ )
            {
            monthLength = ONE_MONTH;
            if( leapyear )
                daysadd = LEAP_MONTH_LENGTH[i];
            else
                daysadd = MONTH_LENGTH[i];
            if( daysadd == 1 )
                monthLength += ONE_DAY;
            else if( daysadd == 2 )
                monthLength += TWO_DAYS;
            else if( daysadd == 3 )
                monthLength += THREE_DAYS;
            if ( time < monthLength )
                break;
            time -= monthLength;
            incrementDay( MONTH_DAYS+daysadd );
            month++;
            }

        // Calc date
        //----------
        date = 1;
        for ( int i=0; i<31; i++ )
            {
            if ( time < ONE_DAY )
                break;
            time -= ONE_DAY;
            date++;
            incrementDay( 1 );
            }

        // Calc hour
        //----------
        int iTime = (int) time;
        hour = (iTime-(iTime%ONE_HOUR))/ONE_HOUR;
        iTime = iTime%ONE_HOUR;

        // Calc min
        //---------
        min = (iTime-(iTime%ONE_MINUTE))/ONE_MINUTE;
        iTime = iTime%ONE_MINUTE;

        // Calc sec
        //---------
        sec = (iTime-(iTime%ONE_SECOND))/ONE_SECOND;
        iTime = iTime%ONE_SECOND;
        millis = iTime;
        }

    public String toString()
        {
        String ampm = "am";
        int localHour = hour;
        if ( localHour > 12 )
            {
            localHour -= 12;
            ampm = "pm";
            }
        if ( localHour == 0 )
            {
            localHour = 12;
            }

        return( DAYS[day]+" "+MONTHS[month]+" "+date+", "+year+" "+
                localHour+":"+min+":"+sec+":"+millis+" "+ampm );
        //      return( month+"/"+date+"/"+year+" "+hour+":"+min+":"+sec+":"+millis );
        }

    /**
     * Computes this <tt>Date</tt> object's representation of time using
     * the current values of the time objects.
     */
    protected void computeTime()
        {
        long time = 0L;
        day = EPOCH_DAY;

        // should probably throw exception here
        if( year < EPOCH_YEAR )
            return;

        // Calc decade
        //------------
        int workYear = EPOCH_YEAR;
        while ( true )
            {
            if ( year <= workYear+10 )
                break;

            // Increment decade
            if ( workYear%4 == 0 ||     // decade start on a leap year?
                    (workYear+1)%4 == 0 )     // second year a leap year?
                {
                time += ONE_LEAP_DECADE;
                incrementDay( 13 );
                }
            else
                {
                time += ONE_DECADE;
                incrementDay( 12 );
                }

            workYear += 10;
            }

        // Calc year
        //----------
        while ( true )
            {
            if ( year == workYear )
                break;

            // Invcrement year
            if ( workYear%4 == 0 )  // leap year?
                time += ONE_LEAP_YEAR;
            else
                time += ONE_YEAR;

            // Increment day
            if ( workYear%4 == 0 )  // workYear a leap year?
                incrementDay( 2 );
            else
                incrementDay( 1 );

            workYear++;
            }

        // Calc month
        //-----------
        boolean leapyear = (year%4 == 0);
        for ( int i=0; i < month; i++ )
            {
            time += ONE_MONTH;

            int daysadd = 0;
            if( leapyear )
                daysadd = LEAP_MONTH_LENGTH[i];
            else
                daysadd = MONTH_LENGTH[i];

            if( daysadd == 1 )
                time += ONE_DAY;
            else if( daysadd == 2 )
                time += TWO_DAYS;
            else if( daysadd == 3 )
                time += THREE_DAYS;

            incrementDay( MONTH_DAYS+daysadd );
            }

        // Calc date
        //----------
        for ( int i=1; i < date; i++ )
            {
            time += ONE_DAY;
            incrementDay( 1 );
            }

        // Calc hour
        //----------
        time += hour * ONE_HOUR;

        // Calc min
        //---------
        time += min * ONE_MINUTE;

        // Calc sec
        //---------
        time += sec * ONE_SECOND;

        fastTime = time;
        }


    /**
     * Increments this <tt>Date</tt> object's representation of the day of
     * the week, ensuring boundary constraints.
     */
    protected void incrementDay( int days )
        {
        day = (day+days)%7;
        }

}
