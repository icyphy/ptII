/* An actor that randomizes the location of other actors.

 Copyright (c) 2003-2014 The Regents of the University of California.
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
package ptolemy.domains.wireless.lib;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Iterator;
import java.util.Random;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.LongToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.expr.Variable;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.moml.MoMLChangeRequest;

///////////////////////////////////////////////////////////////////
//// NodeRandomizer

/**
 This actor, when fired, randomizes the locations of all actors in the
 same container that contain an attribute named "randomize" with value
 true.  It can also optionally perform the randomization in its
 initialize() method. In both cases, the randomization is
 performed in a change request, so it will take effect after the
 current iteration.
 <p>
 This actor assumes that the location is represented in an object
 of class Locatable (which is an attribute that implements Settable)
 with name "_location".  This is how Vergil represents locations.
 <p>
 The <i>range</i> parameter is an array of arrays giving the range
 of possible values for each dimension of the location.  For example,
 if the location is in two dimensions, then range has the form
 {{<i>x1</i>, <i>x2</i>}, {<i>y1</i>, <i>y2</i>}},
 indicating that the X value of the location is uniformly
 distributed between <i>x1</i> and <i>x2</i>, and that the Y value is
 uniformly distributed between <i>y1</i> and <i>y2</i>.
 <p>
 If the <i>resetOnEachRun</i> parameter is true (the default value),
 then each run resets the random number generator. If the seed is
 non-zero, then this makes each run identical.  This is useful for
 constructing tests. If the seed is zero, then a new seed is generated
 on each run (using the current time and the hash code of this object).
 <p>
 The <i>maxPrecision</i> parameter specifies the number of digits to be used
 when generating a random location; results are rounded to this precision.
 If the value is 0 (the default value), then use the maximum precision allowed by
 the Java double type and the random number generator.

 @author Sanjeev Kohli, N. Vinay Krishnan, Cheng Tien Ee, Edward Lee, Xiaojun Liu and Elaine Cheong.
 @version $Id$
 @since Ptolemy II 4.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (sanjeev)
 @see ptolemy.kernel.util.Locatable
 */
public class NodeRandomizer extends TypedAtomicActor {
    /** Construct an actor in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the actor.
     *  @param name Name of the actor.
     *  @exception IllegalActionException If the
     *   actor is not compatible with the specified container.
     *  @exception NameDuplicationException If the name collides
     *   with an entity in the container.
     */
    public NodeRandomizer(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        isPersistent = new Parameter(this, "isPersistent");
        isPersistent.setTypeEquals(BaseType.BOOLEAN);
        isPersistent.setToken(BooleanToken.FALSE);

        maxPrecision = new Parameter(this, "maxPrecision");
        maxPrecision.setExpression("0");
        maxPrecision.setTypeEquals(BaseType.INT);

        randomizeInInitialize = new Parameter(this, "randomizeInInitialize");
        randomizeInInitialize.setExpression("false");
        randomizeInInitialize.setTypeEquals(BaseType.BOOLEAN);

        resetOnEachRun = new Parameter(this, "resetOnEachRun");
        resetOnEachRun.setExpression("true");

        range = new Parameter(this, "range");

        Type rangeType = new ArrayType(new ArrayType(BaseType.DOUBLE));
        range.setTypeEquals(rangeType);
        range.setExpression("{{0.0, 500.0}, {0.0, 500.0}}");

        seed = new Parameter(this, "seed");
        seed.setExpression("0L");
        seed.setTypeEquals(BaseType.LONG);

        trigger = new TypedIOPort(this, "trigger", true, false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If the isPersistent parameter is false, then the MoMLChangeRequest
     *  that places the nodes will not be persistent so the user will not
     *  be prompted to save the model upon closing.  Models in the
     *  test suite might want to have this parameter set to false so
     *  as to avoid a dialog asking if the user wants to save the
     *  model.  The default is a boolean with a value of false,
     *  indicating that change will not be persistent.
     */
    public Parameter isPersistent;

    /** The maximum precision (number of digits to be used, results
     *  are rounded to this precision) of the generated locations.  If
     *  equal to 0, the resulting random locations will not be
     *  rounded.  The default value is 0.
     */
    public Parameter maxPrecision;

    /** If set to true, randomly distribute nodes in initialize().
     *  The default value is false.
     */
    public Parameter randomizeInInitialize;

    /** The range of values for locations to be set when randomizing.
     *  This is an array of arrays that defaults to
     *  {{0.0, 500.0}, {0.0, 500.0}},
     *  indicating that the X and Y values are uniformly distributed
     *  between 0.0 and 500.0.
     */
    public Parameter range;

    /** If set to true, reset the random number generator with the
     *  seed value each time preinitialize() is invoked.  The default
     *  value is true, which means that if the seed value is non-zero,
     *  the node layout will be the same each time this model is run.
     *  When the value is false and the seed value is non-zero, the
     *  random number generator will not be reset, and the node layout
     *  will be different each time this model is run within the same
     *  session (within the lifetime of the JVM), although the overall
     *  pattern of node layouts will be the same across different JVM
     *  sessions.
     */
    public Parameter resetOnEachRun;

    /** The seed that controls the random number generation to use when
     *  randomizing.
     *  A seed of zero is interpreted to mean that no seed is specified,
     *  which means that each execution of the model could result in
     *  distinct data. For the value 0, the seed is set to
     *  System.currentTimeMillis() + hashCode(), which means that
     *  with extremely high probability, two distinct actors will have
     *  distinct seeds.  However, current time may not have enough
     *  resolution to ensure that two subsequent executions of the
     *  same model have distinct seeds.
     *  This parameter contains a LongToken, initially with value 0.
     */
    public Parameter seed;

    /** A trigger input to cause this actor to fire in domains
     *  that require a trigger.  This has undeclared type.
     */
    public TypedIOPort trigger;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to react to changes in <i>maxPrecision</i>.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (not thrown in this base class).
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == maxPrecision) {
            IntToken intToken = (IntToken) maxPrecision.getToken();
            int intValue = intToken.intValue();
            if (intValue == 0) {
                // If the precision is 0, use maximum precision
                // allowed by double.
                _mathContext = null;
            } else if (intValue > 0) {
                // If the precision is > 0, create a MathContext with
                // the specified precision (to be used with a
                // BigDecimal for rounding purposes).
                _mathContext = new MathContext(intToken.intValue());
            } else {
                throw new IllegalActionException(this,
                        "Precision must be an integer "
                                + "greater than or equal to 0.");
            }
        } else {
            super.attributeChanged(attribute);
        }
    }

    /** Issue a change request to randomize the locations of the nodes.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        ChangeRequest doRandomize = new ChangeRequest(this, "randomize nodes") {
            @Override
            protected void _execute() throws IllegalActionException {
                _randomize();
            }
        };

        requestChange(doRandomize);
    }

    /** Override the base class to randomize the positions of the
     *  nodes if <i>randomizeInInitialize</i> is set to true. Also
     *  initialize the random number generator so that if a nonzero
     *  seed is provided, then the results are repeatable.  If
     *  <i>resetOnEachRun</i> is set to true (default value), then the
     *  seed is reset for each run.
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it, or if the range parameter
     *   is malformed.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();

        long seedValue = ((LongToken) seed.getToken()).longValue();

        if (seedValue == 0) {
            seedValue = System.currentTimeMillis() + hashCode();
        }

        if (_random == null) {
            _random = new Random(seedValue);
        } else {
            if (((BooleanToken) resetOnEachRun.getToken()).booleanValue()) {
                _random.setSeed(seedValue);
            }
        }

        if (((BooleanToken) randomizeInInitialize.getToken()).booleanValue()) {
            _randomize();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Randomly distribute the nodes in the model that contain a
     *  boolean valued parameter named "randomize" with value true.
     *  This method distributes the location according to uniform
     *  random variables (one for each dimension) with ranges given
     *  by the <i>range</i> parameter. Subclasses can override this
     *  to perform some other randomization.  This delegates to the
     *  method _setLocationOfNode() to actually set the location of the
     *  actor.
     *  <p>If the <i>isPersistent</i> parameter is true, then
     *  the change is marked as a persistent change, which will
     *  cause the model to be modified, which means the user will
     *  be prompted to save the model upon exiting.</p>
     *  @exception IllegalActionException If the range parameter is malformed.
     */
    protected void _randomize() throws IllegalActionException {
        // Get the range.
        ArrayToken rangeValue = (ArrayToken) range.getToken();
        int dimensions = rangeValue.length();
        double[] randomLocation = new double[dimensions];

        CompositeActor container = (CompositeActor) getContainer();
        StringBuffer changeMoML = new StringBuffer("<group>\n");
        Iterator actors = container.deepEntityList().iterator();

        while (actors.hasNext()) {
            Entity node = (Entity) actors.next();

            // Skip actors that are not properly marked.
            Attribute mark = node.getAttribute("randomize");

            if (!(mark instanceof Variable)) {
                continue;
            }

            Token markValue = ((Variable) mark).getToken();

            if (!(markValue instanceof BooleanToken)) {
                continue;
            }

            if (!((BooleanToken) markValue).booleanValue()) {
                continue;
            }

            for (int i = 0; i < dimensions; i++) {
                ArrayToken lowHigh = (ArrayToken) rangeValue.getElement(i);

                if (lowHigh.length() < 2) {
                    throw new IllegalActionException(this, "Invalid range: "
                            + range.getExpression());
                }

                double low = ((DoubleToken) lowHigh.getElement(0))
                        .doubleValue();
                double high = ((DoubleToken) lowHigh.getElement(1))
                        .doubleValue();

                if (high < low) {
                    throw new IllegalActionException(this, "Invalid range: "
                            + range.getExpression());
                }

                // If the precision is 0, then use the maximum precision allowed by double.
                // Otherwise, round according to the maxPrecision parameter.
                if (_mathContext == null) {
                    randomLocation[i] = low + _random.nextDouble()
                            * (high - low);
                } else {
                    double candidateRandomLocation = low + _random.nextDouble()
                            * (high - low);
                    // Create a BigDecimal with the specified precision.
                    BigDecimal decimal = new BigDecimal(
                            candidateRandomLocation, _mathContext);
                    // Obtain the rounded double value.
                    randomLocation[i] = decimal.doubleValue();
                }
            }

            changeMoML.append(_getLocationSetMoML(container, node,
                    randomLocation));
        }

        changeMoML.append("</group>");

        MoMLChangeRequest request = new MoMLChangeRequest(this, container,
                changeMoML.toString());

        // If the isPersistent parameter is false, then don't marke
        // the model as modified in BasicGraphFrame.changeExecuted().
        // Ptalon needs this to avoid prompting for save upon close.
        boolean isPersistentValue = ((BooleanToken) isPersistent.getToken())
                .booleanValue();
        request.setPersistent(isPersistentValue);

        container.requestChange(request);

        // Increment the workspace version number, since the wireless
        // graph connectivity probably changed as a result of the node
        // location randomization.  This is used in conjunction with
        // LimitedRangeChannel.valueChanged(), which invalidates the
        // cache containing the receivers in range of any source port.
        workspace().incrVersion();
    }

    /** Return moml that will set the location of the specified node.
     *  The moml should set the _location attribute, which is the
     *  location as used in Vergil, the visual editor.  Derived
     *  classes may override this to store the location differently.
     *  @param container The container.
     *  @param location The specified location.
     *  @param node The node for which to set the location.
     *  @return MoML that sets the location of the specified node.
     *  @exception IllegalActionException If the location attribute
     *  cannot be set.
     */
    protected String _getLocationSetMoML(CompositeEntity container,
            Entity node, double[] location) throws IllegalActionException {
        // First figure out the name of the class of the _location
        // attribute.  Usually, it is ptolemy.kernel.util.Location,
        // but another possibility is
        // ptolemy.actor.parameters.LocationParameter.
        Attribute locationAttribute = node.getAttribute("_location");
        String className = null;
        if (locationAttribute != null) {
            className = locationAttribute.getClass().getName();
            return "<property name=\"" + node.getName(container)
                    + "._location\" " + "class=\"" + className + "\" value=\"["
                    + location[0] + ", " + location[1] + "]\"/>\n";
        } else {
            // The _location attribute does not exist.
            // FIXME: We could make a new attribute first instead of
            // throwing an exception here.
            throw new IllegalActionException(
                    "The _location attribute does not exist for node = " + node
                    + "with container = " + container);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Math context used to store precision.
    private MathContext _mathContext = null;

    // Random number generator.
    private Random _random;
}
