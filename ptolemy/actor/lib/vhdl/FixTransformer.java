/* A base class for actors that transform an input stream into an output stream.

 Copyright (c) 1998-2014 The Regents of the University of California.
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

 */
package ptolemy.actor.lib.vhdl;

import java.util.Iterator;
import java.util.Locale;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoRoomException;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.FixToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;
import ptolemy.math.FixPointQuantization;
import ptolemy.math.Overflow;
import ptolemy.math.Precision;
import ptolemy.math.Quantization;
import ptolemy.math.Rounding;

///////////////////////////////////////////////////////////////////
//// FixPointTransformer

/**
 This is an abstract base class for actors that transform an input
 stream into output stream.  It provides an fix point input and an
 fix point output port, and manages the cloning of these ports.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public class FixTransformer extends TypedAtomicActor {

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FixTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        output = newFixOutputPort("output");
        Parameter synthesizable = new Parameter(this, "synthesizable");
        synthesizable.setExpression("true");
        synthesizable.setTypeEquals(BaseType.BOOLEAN);
    }

    /**
     * Return the precision string of the given port.
     * @param port The given port.
     * @return The precision string.
     * @exception IllegalActionException Thrown if there is no precision
     *  parameter for the given port.
     */
    public String getPortPrecision(IOPort port) throws IllegalActionException {

        Parameter precision = null;
        precision = (Parameter) ((Entity) port.getContainer())
                .getAttribute(port.getName() + "Precision");

        if (precision == null) {
            throw new IllegalActionException(this, port.getName()
                    + " does not have an precision parameter.");
        }

        return precision.getExpression();
    }

    /**
     * Send the quantized output token according the output precision,
     * overflow and rounding parameters of the output port.
     * @param channel The given channel to send the output token.
     * @param port The given output port.
     * @param token The given output token.
     * @exception IllegalActionException If the token to be sent cannot
     *  be converted to the type of this port, or if the token is null.
     * @exception NoRoomException If there is no room in the receiver.
     */
    public void sendOutput(TypedIOPort port, int channel, Token token)
            throws NoRoomException, IllegalActionException {
        if (port.getType() == BaseType.FIX && token instanceof FixToken) {
            Precision precision = new Precision(
                    ((Parameter) getAttribute(port.getName() + "Precision"))
                            .getExpression());

            Overflow overflow = Overflow.getName(((Parameter) getAttribute(port
                    .getName() + "Overflow")).getExpression().toLowerCase(
                    Locale.getDefault()));

            Rounding rounding = Rounding.getName(((Parameter) getAttribute(port
                    .getName() + "Rounding")).getExpression().toLowerCase(
                    Locale.getDefault()));

            Quantization quantization = new FixPointQuantization(precision,
                    overflow, rounding);

            token = ((FixToken) token).quantize(quantization);
        }

        port.send(channel, token);
    }

    /** Create a new fix point type output port with given the name.
     *  The container of the created port is this actor. This also
     *  create a new precision parameter associated with this port.
     * @param name The given name of the port.
     * @return The new output port.
     * @exception IllegalActionException If parameters cannot be created.
     * @exception NameDuplicationException If a parameter with the
     *  same name already exists.
     */
    public QueuedTypedIOPort newFixOutputPort(String name)
            throws IllegalActionException, NameDuplicationException {

        // For each output port, we want to have an assoicated
        // precision, overflow and rounding parameters.
        Parameter precision = new StringParameter(this, name + "Precision");
        precision.setExpression("31:0");

        Parameter overflow = new StringParameter(this, name + "Overflow");
        Parameter rounding = new StringParameter(this, name + "Rounding");

        overflow.setExpression("CLIP");

        Iterator iterator = Overflow.nameIterator();
        while (iterator.hasNext()) {
            overflow.addChoice(((String) iterator.next()).toUpperCase(Locale
                    .getDefault()));
        }

        rounding.setExpression("HALF_EVEN");

        iterator = Rounding.nameIterator();
        while (iterator.hasNext()) {
            rounding.addChoice(((String) iterator.next()).toUpperCase(Locale
                    .getDefault()));
        }

        QueuedTypedIOPort port = new QueuedTypedIOPort(this, name, false, true);

        port.setTypeEquals(BaseType.FIX);

        return port;
    }

    /**
     * Verify that the bit width of the given FixToken is equal to
     * the given width. If not, an IllegalActionException is thrown.
     * @param token The given fix-point token.
     * @param width The given width.
     * @exception IllegalActionException Thrown If the bit width of the
     *  given fix token is not equal to given width.
     */
    protected void _checkFixTokenWidth(FixToken token, int width)
            throws IllegalActionException {
        if (token.fixValue().getPrecision().getNumberOfBits() != width) {
            throw new IllegalActionException(this, "Bit width violation: "
                    + token + " is not equal to " + width);
        }
    }

    /**
     * Verify that the bit width of the given fix-point token is equal
     * to the minimum bit width that is required to represent the given
     * value.
     * @param token The given token.
     * @param max The given value.
     * @exception IllegalActionException Thrown if
     *  _checkFixToken(FixToken, in) throws it.
     */
    protected void _checkFixMaxValue(FixToken token, int max)
            throws IllegalActionException {

        _checkFixTokenWidth(token, Integer.toBinaryString(max).length());
    }

    /**
     * Set quantization parameters of the output port with the given
     * parameter expression strings. Hide the parameters in the configure
     * dialog.
     * @param precisionString The given expression for the precision parameter.
     * @param overflowString The given expression for the overflow parameter.
     * @param roundingString The given expression for the rounding parameter.
     */
    protected void _setAndHideQuantizationParameters(String precisionString,
            String overflowString, String roundingString) {
        //throws IllegalActionException {

        _setQuantizationParameters(precisionString, overflowString,
                roundingString);
        _showQuantizationParameters(false, false, false);
    }

    /**
     * Set the visibility of the precision, overflow and rounding
     * parameters.
     * @param showPrecision The visibility of the precision parameter.
     * @param showOverflow The visibility of the overflow parameter.
     * @param showRounding The visibility of the precision parameter.
     */
    protected void _showQuantizationParameters(boolean showPrecision,
            boolean showOverflow, boolean showRounding) {
        //throws IllegalActionException {
        Parameter precision = (Parameter) getAttribute("outputPrecision");
        precision.setVisibility(showPrecision ? Settable.FULL : Settable.NONE);

        Parameter overflow = (Parameter) getAttribute("outputOverflow");
        overflow.setVisibility(showOverflow ? Settable.FULL : Settable.NONE);

        Parameter rounding = (Parameter) getAttribute("outputRounding");
        rounding.setVisibility(showRounding ? Settable.FULL : Settable.NONE);
    }

    /**
     * Set quantization parameters for the output port.
     * @param precisionString The given precision expression.
     * @param overflowString The given overflow expression.
     * @param roundingString The given rounding expression.
     */
    protected void _setQuantizationParameters(String precisionString,
            String overflowString, String roundingString) {
        //throws IllegalActionException {
        if (precisionString != null) {
            ((Parameter) getAttribute("outputPrecision"))
                    .setExpression(precisionString);
        }
        if (overflowString != null) {
            ((Parameter) getAttribute("outputOverflow"))
                    .setExpression(overflowString);
        }
        if (roundingString != null) {
            ((Parameter) getAttribute("outputRounding"))
                    .setExpression(roundingString);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Queued output to simulate pipelined add.  The output is fix
     *  point type.
     */
    public QueuedTypedIOPort output;

}
