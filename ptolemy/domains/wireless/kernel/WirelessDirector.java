/* The wireless director.

 Copyright (c) 2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import java.util.Iterator;
import java.util.Random;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Receiver;
import ptolemy.data.ArrayToken;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.LongToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.ArrayType;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.Type;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// WirelessDirector
/**
This director is nearly identical to the DE director except that it
creates instances of WirelessReceiver and it has
a set of attributes that can be used to randomize the location of wireless
nodes on each execution.  The location of each actor that is not an
instance of WirelessChannel will be set to a random location.
The <i>range</i> parameter is an array of arrays giving the range
of possible values for each dimension of the location.  For example,
if the location is in two dimensions, then range has the form
{{<i>x1</i>, <i>x2</i>}, {<i>y1</i>, <i>y2</i>}},
indicating that the X value of the location is uniformly
distributed between <i>x1</i> and <i>x2</i>, and that the Y value is
uniformly distributed between <i>y1</i> and <i>y2</i>.

@author Sanjeev Kohli, N. Vinay Krishnan, Cheng Tien Ee, and Xiaojun Liu
@version $Id$
@see DEDirector
*/
public class WirelessDirector extends DEDirector {

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string. Increment the version number of the workspace.
     *  @param container Container of the director.
     *  @param name Name of this director.
     *  @exception IllegalActionException If the
     *   director is not compatible with the specified container.
     *  @exception NameDuplicationException If the container not a
     *   CompositeActor and the name collides with an entity in the container.
     */
    public WirelessDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {

        super(container, name);
        randomizeLocations = new Parameter(this, "randomizeLocations",
                new BooleanToken(false));
        randomizeLocations.setTypeEquals(BaseType.BOOLEAN);

        range = new Parameter(this, "range");
        Type rangeType = new ArrayType(new ArrayType(BaseType.DOUBLE));
        range.setTypeEquals(rangeType);
        range.setExpression("{{0.0, 500.0}, {0.0, 500.0}}");

        seed = new Parameter(this, "seed", new LongToken(0));
        seed.setTypeEquals(BaseType.LONG);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If set to true, randomly distribute nodes before each run.
     *  The default value is false.
     */
    public Parameter randomizeLocations;

    /** The range of values for locations to be set when
     *  <i>randomizeLocations</i> is true. This is an array of
     *  arrays that defaults to {{0.0, 500.0}, {0.0, 500.0}},
     *  indicating that the X and Y values are uniformly distributed
     *  between 0.0 and 500.0.
     */
    public Parameter range;

    /** The seed that controls the random number generation to use when
     *  <i>randomizeLocations</i> is true.
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

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Override the base class to randomize the positions of the nodes
     *  if <i>randomizeLocations</i> is set to true.
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        if(((BooleanToken)randomizeLocations.getToken()).booleanValue()) {
            long seedValue = ((LongToken)(seed.getToken())).longValue();
            if (seedValue == (long)0) {
                seedValue = System.currentTimeMillis() + hashCode();
            }
            if(_random == null) {
                _random = new Random(seedValue);
            } else {
                _random.setSeed(seedValue);
            }
            _randomizeLocations();
        }
    }

    /** Return a new receiver of a type WirelessReceiver.
     *  @return A new WirelessReceiver.
     */
    public Receiver newReceiver() {
        if (_debugging) _debug("Creating new WirelessReceiver.");
        return new WirelessReceiver();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Randomly distribute the sensor nodes present in the model.
     *  This method distributes the location according to uniform
     *  random variables (one for each dimension) with ranges given
     *  by the <i>range</i> parameter. Subclasses can override this
     *  to perform some other randomization.  This delegates to the
     *  method _setLocationOfNode() to actually set the location of the
     *  actor.
     *  @exception IllegalActionException If the range parameter is malformed.
     */
    protected void _randomizeLocations() throws IllegalActionException {
        // Get the range.
        ArrayToken rangeValue = (ArrayToken)range.getToken();
        int dimensions = rangeValue.length();
        double[] randomLocation = new double[dimensions];

        Iterator actors
                = ((CompositeActor)getContainer()).deepEntityList().iterator();
        while (actors.hasNext()) {
            Entity node = (Entity)actors.next();

            // Skip channels.
            if (node instanceof WirelessMedia) {
                continue;
            }
            for (int i = 0; i < dimensions; i++) {
                ArrayToken lowHigh = (ArrayToken)rangeValue.getElement(i);
                if (lowHigh.length() < 2) {
                    throw new IllegalActionException(this,
                    "Invalid range: " + range.getExpression());
                }
                double low = ((DoubleToken)lowHigh.getElement(0)).doubleValue();
                double high = ((DoubleToken)lowHigh.getElement(1)).doubleValue();
                if (high < low) {
                    throw new IllegalActionException(this,
                    "Invalid range: " + range.getExpression());
                }
                randomLocation[i] = low + (_random.nextDouble())*(high - low);
            }
            _setLocationOfNode(randomLocation, node);
        }
    }

    /** Set the location of the specified node.  This sets the _location
     *  attribute, which is the location as used in Vergil, the visual editor.
     *  Derived classes may override this to set the location differently.     *
     *  @param location The specified location.
     *  @param node The node for which to set the location.
     *  @exception IllegalActionException If the location attribute
     *   cannot be set.
     */
    protected void _setLocationOfNode(double[] location, Entity node)
            throws IllegalActionException {
        try {
            Locatable myLocation =
                    (Locatable)node.getAttribute("_location", Locatable.class);
            if(myLocation != null) {
                myLocation.setLocation(location);
            } else {
                myLocation = new Location(node, "_location");
                myLocation.setLocation(location);
            }
        } catch (NameDuplicationException e) {
            throw new InternalErrorException(e);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Random number generator.
    private Random _random;
}
