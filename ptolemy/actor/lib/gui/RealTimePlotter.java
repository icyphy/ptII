/* Plot input data as a function of elapsed real time.

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

import ptolemy.actor.TypedIOPort;
import ptolemy.data.DoubleToken;
import ptolemy.data.IntToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.plot.Plot;

///////////////////////////////////////////////////////////////////
//// RealTimePlotter

/**
 This plotter plots input data as a function of elapsed real time in seconds.
 Elapsed time is set to zero when the initialize() method is invoked.
 The resolution of time depends on the implementation of the Java
 virtual machine, but with Sun's JDK 1.3 under Windows 2000, it is
 10 milliseconds.

 <p>This plotter contains an instance of the Plot class from the
 Ptolemy plot package as a public member.

 @author  Edward A. Lee
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class RealTimePlotter extends Plotter {
    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public RealTimePlotter(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Create the input port and make it a multiport.
        input = new TypedIOPort(this, "input", true, false);
        input.setMultiport(true);
        input.setTypeEquals(BaseType.DOUBLE);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Input port, which has type DoubleToken. */
    public TypedIOPort input;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Record the start time.
     *  @exception IllegalActionException If the base class throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _startTime = System.currentTimeMillis();
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
        long elapsedTime = System.currentTimeMillis() - _startTime;
        double currentTime = elapsedTime / 1000.0;
        int width = input.getWidth();
        int offset = ((IntToken) startingDataset.getToken()).intValue();

        for (int i = width - 1; i >= 0; i--) {
            if (input.hasToken(i)) {
                DoubleToken currentToken = (DoubleToken) input.get(i);
                double currentValue = currentToken.doubleValue();

                // NOTE: We assume the superclass ensures this cast is safe.
                ((Plot) plot).addPoint(i + offset, currentTime, currentValue,
                        true);
            }
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The start time. */
    private long _startTime;
}
