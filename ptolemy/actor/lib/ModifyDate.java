/* Modify date by adding or subtracting a value to one of the date fields.

   @Copyright (c) 2008-2016 The Regents of the University of California.
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

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.DateToken;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// DateElements

/**
  Modify date by adding or subtracting a value to one of the date fields.

 @author Patricia Derler
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (pd)
 @Pt.AcceptedRating Red (pd)
 */
public class ModifyDate extends TypedAtomicActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ModifyDate(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.DATE);

        operation = new StringParameter(this, "operation");
        operation.addChoice("+");
        operation.addChoice("-");
        operation.setToken(new StringToken("+"));

        value = new TypedIOPort(this, "value", true, false);
        value.setTypeEquals(BaseType.INT);

        unit = new StringParameter(this, "unit");
        unit.addChoice("Year");
        unit.addChoice("Month");
        unit.addChoice("Day");
        unit.addChoice("Hour");
        unit.addChoice("Minute");
        unit.addChoice("Second");
        unit.addChoice("Millisecond");
        unit.addChoice("Microsecond");
        unit.addChoice("Nanosecond");
        unit.setToken(new StringToken("Second"));

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.DATE);
    }

    /** Input for date tokens.
     */
    public TypedIOPort input;

    /** Parameter for operation. The operation is a choice of the string
     *  values "+" and "-" and defaults to "+".
     */
    public Parameter operation;

    /** Input for value in a given unit to be added or subtracted from date
     *  token received by input.
     */
    public TypedIOPort value;

    /** Unit of value to be added or subtracted. This can be either part of
     *  the date, i.e. year, month, day, ... and it defaults to second.
     */
    public Parameter unit;

    /** Output for the new date token.
     */
    public TypedIOPort output;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Compute the absolute value of the input.  If there is no input, then
     *  produce no output.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        if (input.hasToken(0)) {
            DateToken inputToken = (DateToken) input.get(0);

            DateToken token = new DateToken(inputToken.getValue(),
                    inputToken.getPrecision(), inputToken.getTimeZone());

            String operationString = ((StringToken) operation.getToken())
                    .stringValue();
            String unitString = ((StringToken) unit.getToken()).stringValue();

            int val = ((IntToken) value.get(0)).intValue();
            if (operationString.equals("+")) {
                //nothing to do;
            } else if (operationString.equals("-")) {
                val = val * (-1);
            } else {
                throw new IllegalActionException(this, "Operation "
                        + operationString + " not supported.");
            }

            boolean setUsingCalendar = true;
            Calendar calendar = token.getCalendarInstance();
            if (unitString.equals("Year")) {
                calendar.add(Calendar.YEAR, val);
            } else if (unitString.equals("Month")) {
                calendar.add(Calendar.MONTH, val);
            } else if (unitString.equals("Day")) {
                calendar.add(Calendar.DAY_OF_MONTH, val);
            } else if (unitString.equals("Hour")) {
                calendar.add(Calendar.HOUR_OF_DAY, val);
            } else if (unitString.equals("Minute")) {
                calendar.add(Calendar.MINUTE, val);
            } else if (unitString.equals("Second")) {
                calendar.add(Calendar.SECOND, val);
            } else if (unitString.equals("Millisecond")) {
                calendar.add(Calendar.MILLISECOND, val);
            } else if (unitString.equals("Microsecond")) {
                setUsingCalendar = false;
                token.addMicroseconds(val);
                // Update _value and _calendar in DateToken.
                token.setTimeInMilliseconds(token.getTimeInMilliseconds());
            } else if (unitString.equals("Nanosecond")) {
                setUsingCalendar = false;
                token.addNanoseconds(val);
                // Update _value and _calendar in DateToken.
                token.setTimeInMilliseconds(token.getTimeInMilliseconds());
            } else {
                throw new IllegalActionException(this, "The unit " + unitString
                        + " is not supported");
            }

            // If units are greater than milliseonds
            if (setUsingCalendar) {
                // Update _value and _calendar in DateToken.
                token.setTimeInMilliseconds(calendar.getTimeInMillis());
            }

            output.send(0, token);
        }
    }
}
