/* The wireless director.

 Copyright (c) 1998-2002 The Regents of the University of California.
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

@ProposedRating Red (sanjeev@eecs.berkeley.edu)
@AcceptedRating Red (sanjeev@eecs.berkeley.edu)
*/

package ptolemy.domains.wireless.kernel;

import java.util.Iterator;
import java.util.Random;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.data.BooleanToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Entity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.Locatable;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;

//////////////////////////////////////////////////////////////////////////
//// SensorDirector
/**
This director manages the communication between sensor nodes. Whenever a
sensor node broadcasts, it asks the director to put the broadcast
tokens at the input of other sensor nodes that are reachable by the
broadcasting sensor node.

@author Sanjeev Kohli, N. Vinay Krishnan, Cheng Tien Ee, and Xiaojun Liu
@version $Id$
@since Ptolemy II 0.2
@see ptolemy.actor.Director
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
        randomizeGraph = new Parameter(this, "randomizeGraph",
                new BooleanToken(false));
        randomizeGraph.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** If set to true, randomly distribute the nodes before each run.
     *  The default value is false.
     */
    public Parameter randomizeGraph;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the relation that manages the communication through the
     *  named port. If the relation is named <i>foo</i><code>Channel</code>,
     *  then an output port that sends tokens to the relation must be
     *  named <i>foo</i><code>OPort</code>, and an input port that receives
     *  tokens from the relation must be named <i>foo</i><code>IPort</code>.
     *  @param portName The name of an input or output port.
     *  @return The relation that manages the communication through the named
     *   port.
     */
    public TypedIORelation getChannel(String portName) {
        CompositeEntity container = (CompositeEntity)getContainer();
        if (!portName.endsWith("IPort") && !portName.endsWith("OPort")) {
            return null;
        }
        String channel = portName.substring(0, portName.length() - 5);
        TypedIORelation relation =
                (TypedIORelation)container.getRelation(channel + "Channel");
        return relation;
    }

    /** Override the base class to randomize the positions of the nodes
     *  if <i>randomizeNodes</i> is set to true.
     *  @exception IllegalActionException If the initialize() method of
     *   one of the associated actors throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();
        _randomizeNodes();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    // Randomly distribute the sensor nodes present in the model.
    private void _randomizeNodes() throws IllegalActionException {
        if(((BooleanToken)randomizeGraph.getToken()).booleanValue()) {
            Nameable container = getContainer();
            CompositeActor castContainer = (CompositeActor)container;
            Iterator actors  = castContainer.deepEntityList().iterator();
            Iterator actors1;
            Entity node;

            if(_random1 == null) {
                _random1 = new Random(_seed1);
                _random2 = new Random(_seed2);
                _random3 = new Random(_seed1*_seed2);
            } else {
                _random1.setSeed(_random3.nextLong());
                _random2.setSeed(_random3.nextLong());
            }
            double[] randomLocation = new double[2];
            while (actors.hasNext()) {
                node = (Entity)actors.next();
                randomLocation[0] = (_random1.nextFloat())*1000;
                randomLocation[1] = (_random2.nextFloat())*800;
                try {
                    Locatable myLocation =
                            (Locatable)node.getAttribute("_location", Locatable.class);
                    if(myLocation != null)
                            myLocation.setLocation(randomLocation);
                    else {
                        myLocation = new Location(node, "_location");
                        myLocation.setLocation(randomLocation);
                    }
                } catch (NameDuplicationException e) {
                    throw new InternalErrorException(e);
                }
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Random number generators.
    private Random _random1;
    private Random _random2;
    private Random _random3;

    // Seeds for random variables.
    private long _seed1 = 37;
    private long _seed2 = 88;
}
