/* A director that does nothing.

 Copyright (c) 2009-2013 The Regents of the University of California.
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
package ptolemy.domains.fmi.kernel;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.Initializable;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
//// FMIDirector

/**
 Execute the FMI Master Algorithm.

 @author Christopher Brooks, Based on the DoNothingDirector by Edward A. Lee
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (cxh)
 */
public class FMIDirector extends Director {
    /** Construct a director with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public FMIDirector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    @Override
    public void addInitializable(Initializable initializable) {
    }

    /** Execute the FMI Master Algorithm.
     *  @exception IllegalActionException If the Master Algorithm
     *  cannot be executed or if thrown by the super class.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();
    }

    /** Return the stop time, which is always 1.0.
     *  @return 1.0
     *  @exception IllegalActionException Not thrown in this base class.
     */
    @Override
    public double getStopTime() throws IllegalActionException {
        System.out.println("FMUDirector: stop time would be "
                + super.getStopTime() + ", returning 1.0 instead.");
        return 1.0;
    }

    /** Do nothing except call the super class.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
    }

    /** Do nothing except call the super class.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        // The DoNothingDirector returns false here, we return true.
        return super.postfire();
    }

    /** Do nothing except call the super class.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public boolean prefire() throws IllegalActionException {
        // The DoNothingDirector returns false here, we return true.
        return super.prefire();
    }

    /** Do nothing except call the super class.
     *  @exception IllegalActionException If thrown by the super class.
     */
    @Override
    public void preinitialize() throws IllegalActionException {
        super.preinitialize();
    }

    @Override
    public boolean transferInputs(IOPort port) {
        return false;
    }

    @Override
    public boolean transferOutputs(IOPort port) {
        return false;
    }
}
