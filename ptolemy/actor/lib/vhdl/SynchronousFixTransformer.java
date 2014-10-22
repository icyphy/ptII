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
import ptolemy.data.FixToken;
import ptolemy.data.IntToken;
import ptolemy.data.ScalarToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.math.FixPoint;
import ptolemy.math.FixPointQuantization;
import ptolemy.math.Overflow;
import ptolemy.math.Precision;
import ptolemy.math.Rounding;

///////////////////////////////////////////////////////////////////
//// SynchronousFixPointTransformer

/**
 This is an abstract base class for actors that transform
 an input stream into an output stream with a specified latency
 parameter. The default latency is initially set to 0.

 @author Man-Kit Leung
 @version $Id$
 @since Ptolemy II 6.0
 @Pt.ProposedRating Red (mankit)
 @Pt.AcceptedRating Red (mankit)
 */
public abstract class SynchronousFixTransformer extends FixTransformer {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public SynchronousFixTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        latency = new Parameter(this, "latency", new IntToken(0));

        initialValue = new Parameter(this, "initialValue");
        initialValue.setTypeEquals(BaseType.SCALAR);
        initialValue.setExpression("0.0");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The number cycle delay of the output data.
     */
    public Parameter latency;

    /** The number cycle delay of the output data.
     */
    public Parameter initialValue;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to determine which function is being
     *  specified.
     *  @param attribute The attribute that changed.
     * @exception IllegalActionException
     *  @exception IllegalActionException If the function is not recognized.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        if (attribute == latency) {
            int latencyValue = ((ScalarToken) latency.getToken()).intValue();
            output.resize(latencyValue);

            try {
                if (latencyValue == 0) {
                    initialValue.setContainer(null);
                } else {
                    initialValue.setContainer(this);
                }
            } catch (NameDuplicationException ex) {
                throw new IllegalActionException(this, ex,
                        "Fail to set the initialValue parameter.");
            }
            Precision precision = new Precision(
                    ((Parameter) getAttribute("outputPrecision"))
                            .getExpression());

            Overflow overflow = Overflow
                    .getName(((Parameter) getAttribute("outputOverflow"))
                            .getExpression().toLowerCase(Locale.getDefault()));

            Rounding rounding = Rounding
                    .getName(((Parameter) getAttribute("outputRounding"))
                            .getExpression().toLowerCase(Locale.getDefault()));

            FixPoint result = new FixPoint(
                    ((ScalarToken) initialValue.getToken()).doubleValue(),
                    new FixPointQuantization(precision, overflow, rounding));
            output.setInitToken(new FixToken(result));
        }
        if (attribute == initialValue) {
            Precision precision = new Precision(
                    ((Parameter) getAttribute("outputPrecision"))
                            .getExpression());

            Overflow overflow = Overflow
                    .getName(((Parameter) getAttribute("outputOverflow"))
                            .getExpression().toLowerCase(Locale.getDefault()));

            Rounding rounding = Rounding
                    .getName(((Parameter) getAttribute("outputRounding"))
                            .getExpression().toLowerCase(Locale.getDefault()));

            FixPoint result = new FixPoint(
                    ((ScalarToken) initialValue.getToken()).doubleValue(),
                    new FixPointQuantization(precision, overflow, rounding));
            output.setInitToken(new FixToken(result));
        }
    }

    /** Initialize the state of the actor.
     *  @exception IllegalActionException If there is no director.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        Precision precision = new Precision(
                ((Parameter) getAttribute("outputPrecision")).getExpression());

        Overflow overflow = Overflow
                .getName(((Parameter) getAttribute("outputOverflow"))
                        .getExpression().toLowerCase(Locale.getDefault()));

        Rounding rounding = Rounding
                .getName(((Parameter) getAttribute("outputRounding"))
                        .getExpression().toLowerCase(Locale.getDefault()));

        FixPoint result = new FixPoint(
                ((ScalarToken) initialValue.getToken()).doubleValue(),
                new FixPointQuantization(precision, overflow, rounding));
        int latencyValue = ((ScalarToken) latency.getToken()).intValue();
        output.setSize(latencyValue, new FixToken(result));
    }

    /** Return false. This actor can produce some output event the input
     *  receiver has status unknown.
     *
     *  @return False.
     */
    @Override
    public boolean isStrict() {
        try {
            int latencyValue = ((ScalarToken) latency.getToken()).intValue();

            if (latencyValue > 0) {
                return false;
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "Failed to get the value of the latency parameter?");
        }
        return true;
    }

    /** Override the base class to declare that the <i>output</i>
     *  does not depend on the <i>input</i> in a firing.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
        try {
            int latencyValue = ((ScalarToken) latency.getToken()).intValue();

            if (latencyValue > 0) {
                Iterator inputPorts = inputPortList().iterator();
                while (inputPorts.hasNext()) {
                    IOPort input = (IOPort) inputPorts.next();
                    removeDependency(input, output);
                }
            }
        } catch (IllegalActionException ex) {
            throw new InternalErrorException(this, ex,
                    "Failed to get the value of the latency parameter?");
        }
    }
}
