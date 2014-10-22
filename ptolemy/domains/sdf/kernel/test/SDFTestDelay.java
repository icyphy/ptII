/*
 @Copyright (c) 1998-2014 The Regents of the University of California.
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

 PT_COPYRIGHT_VERSION 2
 COPYRIGHTENDKEY

 */
package ptolemy.domains.sdf.kernel.test;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
 * This actor copies one token from its input to its output when fired.
 *
 * @author Steve Neuendorffer
 * @version $Id$
 * @since Ptolemy II 0.4
 * @Pt.ProposedRating Red
 * @Pt.AcceptedRating Red
 */
public class SDFTestDelay extends TypedAtomicActor {
    /*
     * Object constructor. This creates an object of class SDFTestDelay.
     * The object will have a name given by the 'name' argument and will
     * be contained within the composite actor given by the 'container'
     * argument.   This method throws two exceptions, which are error
     * conditions that the method (or one of the methods it calls) has
     * detected (notice the @exception tag).
     */

    /**
     * Create an SDFTestDelay actor in the given container with the given name.
     * This actor copies one token from the input port named "input" to the
     * output port named "output" when fired.
     * @exception IllegalActionException If the contained methods throw it
     * @exception NameDuplicationException If the contained methods throw it
     */
    public SDFTestDelay(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        /* This starts out by calling TypedAtomicActor's constructor.
         * allows us to add some extra code specific to this actor, in
         * addition to the regular initializer in TypedAtomicActor.
         */
        super(container, name);

        /* This construct is called a "try-catch block".  It is used to
         * detect exceptions that occurred during the execution of some
         * other code.  In this case, we detect IllegalActionException and
         * print a reasonable error message.  This is convenient under
         * some environments which can destroy the debugging information from
         * java
         */
        /* Create a new input port */
        input = new TypedIOPort(this, "input", true, false);

        /* Set the Consumption rate on the port.  A good way to think of
         * this is that you are making a contract with the Ptolemy system.
         * If you always consume one token from this port when fired,
         * then Ptolemy will always guarantee that there will be a token
         * ready when you are fired.
         */
        input_tokenConsumptionRate = new Parameter(input,
                "tokenConsumptionRate", new IntToken(1));

        /* The getPort method (in ptolemy.kernel.Entity) finds a port by
         * name.  It returns a Port object, but all the ports in the
         * actor package are of type TypedIOPort,
         * which extends Port.   So, we
         * have to upcast the return value to the appropriate type.
         * The setDeclaredType calls use the type system to define what
         * types of tokens are valid for this actor.
         */
        input.setTypeEquals(BaseType.INT);

        /* Similarly for the output port */
        output = new TypedIOPort(this, "output", false, true);
        output_tokenProductionRate = new Parameter(output,
                "tokenProductionRate", new IntToken(1));

        output.setTypeEquals(BaseType.INT);
    }

    public TypedIOPort input;

    public TypedIOPort output;

    public Parameter input_tokenConsumptionRate;

    public Parameter output_tokenProductionRate;

    /* Notice that constructors start the file.  Public methods should follow,
     * in alphabetical order, followed by protected and private methods.
     * Instance variables come at the end of the class.
     */
    /* The fire method is where the bulk of the action takes place.   In
     * SDF, fire will be called exactly once between prefire and postfire.
     * In this class, we get an input port, print its value, and put the
     * same token back on the output port.
     */

    /** This fires an actor and may be invoked several times between
     *  invocations of prefire() and postfire(). It may produce output
     *  data. Typically, the fire() method performs the computation associated
     *  with an actor.
     *
     *  @exception IllegalActionException If firing is not permitted.
     */
    @Override
    public void fire() throws IllegalActionException {
        /* First we declare a variable that is a Token that can carry
         * integer data (ptolemy.data.IntToken).
         */
        IntToken message;

        /* Figure out how many tokens should be copied from the input to the
         * output.
         */
        int tokens = ((IntToken) input_tokenConsumptionRate.getToken())
                .intValue();

        if (((IntToken) output_tokenProductionRate.getToken()).intValue() != tokens) {
            throw new IllegalActionException(
                    "SDFTestDelay: Rates on input port and output port "
                            + "must match!");
        }

        int i;

        for (i = 0; i < tokens; i++) {
            /* now that we have the port, we can get a token from it.
             * The argument to get is known as the channel.
             * The channel must be
             * zero, since there can only be one IORelation connected to this
             * input.  Multiple channels are useful with Multiports, which
             * allow more than one relation to be connected to a port.
             */
            message = (IntToken) input.get(0);

            /* After looking at the token, pass it along to the next actor
             */
            output.send(0, message);
        }
    }

    /* Postfire is the last step in an iteration.   This actor returns
     * true, since it places no limit on how execution may proceed.   In
     * practice this method can be used to define a stopping condition,
     * where execution will stop once some condition has been met.
     */

    /** This method should be invoked once per iteration, after the last
     *  invocation of fire() in that iteration. It may produce output data.
     *  It returns true if the execution can proceed into the next iteration.
     *  This method typically wraps up an iteration, which may involve
     *  updating local state. In an opaque, non-atomic entity, it may also
     *  transfer output data.
     *
     *  @return True if the execution can continue.
     *  @exception IllegalActionException If postfiring is not permitted.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        return true;
    }

    /* Prefire is the first step in every iteration.  This method returns
     * true if the actor can perform useful work by being fired.   For
     * example, an actor could return false if it was waiting for a certain
     * amount of data for a calculation, but that amount of data was not
     * yet present.  Most actors under SDF will just return true, since they
     * have made a contract with the SDF domain to be fired when enough data
     * is present.
     */

    /** This method should be invoked once per iteration, before the first
     *  invocation of fire() in that iteration.  It returns true if the
     *  iteration can proceed (the fire() method can be invoked). Thus
     *  this method will typically check preconditions for an iteration, if
     *  there are any. In an opaque, non-atomic entity,
     *  it may move data into an inner subsystem.
     *
     *  @return True if the iteration can proceed.
     *  @exception IllegalActionException If prefiring is not permitted.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        return true;
    }

    /* When this method is called, the actor must do everything in its power
     * to immediately stop whatever it is doing.   This is primarily
     * called under
     * two circumstances.  The first is a user initiated break, such as
     * via CTRL-C.  The second is if an error occurs during wrapup() that
     * cannot be recovered from.
     */

    /** This method is invoked to immediately terminate any execution
     *  within an actor.
     */
    @Override
    public void terminate() {
        return;
    }

    /* This method is called at the end of an execution, to allow the
     * actor to cleanup after itself.  Often this will involve destroying
     * graphical windows,  flushing buffers, etc.   This method will also get
     * called under most abnormal termination conditions.
     * This actor has nothing to do here.
     */

    /** This method should be invoked exactly once per execution
     *  of an application.  None of the other action methods should be
     *  be invoked after it.  It finalizes an execution, typically closing
     *  files, displaying final results, etc.
     *
     *  @exception IllegalActionException If wrapup is not permitted.
     */
    @Override
    public void wrapup() throws IllegalActionException {
        return;
    }
}
