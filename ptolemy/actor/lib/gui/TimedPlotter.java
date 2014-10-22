/* Plot functions of time.

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
package ptolemy.actor.lib.gui;

import java.util.ArrayList;

import ptolemy.actor.TimedActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
//// TimedPlotter

/**
 A signal plotter.  This plotter contains an instance of the Plot class
 from the Ptolemy plot package as a public member.  Data at the input, which
 can consist of any number of channels, are plotted on this instance.
 Each channel is plotted as a separate data set.
 The horizontal axis represents time, which by default is the global
 time of the model (the model time of the top-level director).
 Setting <i>useLocalTime</i> to true changes this to use the
 local time of the input port, which is (in most domains) the model
 time of the local director.

 @author  Edward A. Lee, Contributor: Bert Rodiers
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Green (eal)
 @Pt.AcceptedRating Green (cxh)
 */
public class TimedPlotter extends Plotter implements TimedActor {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public TimedPlotter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        disconnectGraphOnAbsentValue = new Parameter(this,
                "disconnectGraphOnAbsentValue", new BooleanToken(false));
        disconnectGraphOnAbsentValue.setTypeEquals(BaseType.BOOLEAN);

        // Create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);

        useLocalTime = new Parameter(this, "useLocalTime");
        useLocalTime.setTypeEquals(BaseType.BOOLEAN);
        useLocalTime.setExpression("false");
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** When disconnectGraphOnAbsentValue is True there will be a gap
     *  in the graph each time a the actor is fired, but the value
     *  is absent for a certain channel. Especially in the continuous
     *  domain this options is useful. By default this parameter is
     *  False.
     */
    public Parameter disconnectGraphOnAbsentValue;

    /** Input port, which has type DoubleToken. */
    public TypedIOPort input;

    /** If true, use the model time reported by the input port,
     *  which is normally the model time of the local director.
     *  If false (the default), use the model time reported by
     *  the top-level director. Local time may differ
     *  from global time inside modal models and certain domains
     *  that manipulate time.
     */
    public Parameter useLocalTime;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then creates new ports and parameters.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class has an
     *   attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        TimedPlotter newObject = (TimedPlotter) super.clone(workspace);
        newObject._connected = new ArrayList<Boolean>();
        return newObject;
    }

    /** Initialize this actor.  Derived classes override this method
     *  to perform actions that should occur once at the beginning of
     *  an execution, but after type resolution.  Derived classes can
     *  produce output data and schedule events.
     *
     *  @exception IllegalActionException If a derived class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        int width = input.getWidth();
        _connected.clear();
        for (int i = 0; i < width; i++) {
            _connected.add(true);
        }
        ((Plot) plot).markDisconnections(true);
    }

    /** Read at most one input from each channel and plot it as a
     *  function of time.
     *  This is done in postfire to ensure that data has settled.
     *  @exception IllegalActionException If there is no director, or
     *   if the base class throws it.
     *  @return True if it is OK to continue.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        double currentTimeValue;
        int width = input.getWidth();

        boolean disconnectOnAbsent = ((BooleanToken) disconnectGraphOnAbsentValue
                .getToken()).booleanValue();
        int offset = ((IntToken) startingDataset.getToken()).intValue();

        for (int i = width - 1; i >= 0; i--) {
            if (input.hasToken(i)) {
                boolean localTime = ((BooleanToken) useLocalTime.getToken())
                        .booleanValue();
                if (localTime) {
                    currentTimeValue = input.getModelTime(i).getDoubleValue();
                } else {
                    currentTimeValue = getDirector().getGlobalTime()
                            .getDoubleValue();
                }

                DoubleToken currentToken = (DoubleToken) input.get(i);
                double currentValue = currentToken.doubleValue();

                // NOTE: We assume the superclass ensures this cast is safe.
                ((Plot) plot).addPoint(i + offset, currentTimeValue,
                        currentValue, _connected.get(i));
                if (disconnectOnAbsent) {
                    _connected.set(i, true);
                }
            } else if (disconnectOnAbsent) {
                // We have not token, and hence we want to create a gap
                // in the graph.
                _connected.set(i, false);
            }
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private ArrayList<Boolean> _connected = new ArrayList<Boolean>();
}
