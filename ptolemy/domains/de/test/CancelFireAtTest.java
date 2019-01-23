/* An actor that handles an HttpRequest by producing an output and waiting for an input.

 Copyright (c) 1997-2014 The Regents of the University of California.
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

package ptolemy.domains.de.test;

import ptolemy.actor.Director;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.util.Time;
import ptolemy.data.BooleanToken;
import ptolemy.data.type.BaseType;
import ptolemy.domains.de.kernel.DEDirector;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/** A test actor to test the cancelFireAt() method of the director.
 *
 *  @author Edward A. Lee
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (ltrnc)
 *  @Pt.AcceptedRating Red (ltrnc)
 */
public class CancelFireAtTest extends TypedAtomicActor {

    /** Create an instance of the actor.
     *  @param container The container
     *  @param name The name.
     *  @exception IllegalActionException If the superclass throws it.
     *  @exception NameDuplicationException If the super
     */
    public CancelFireAtTest(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Ports
        out = new TypedIOPort(this, "out", false, true);
        out.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The output.
     */
    public TypedIOPort out;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Send true to the output and cancel the firing at times 1.0 and 3.0.
     *  @exception IllegalActionException If sending the
     *   outputs fails.
     */
    @Override
    public synchronized void fire() throws IllegalActionException {
        // The methods of the servlet are invoked in another
        // thread, so we synchronize on this actor for mutual exclusion.
        super.fire();
        out.send(0, BooleanToken.TRUE);
        DEDirector director = (DEDirector) getDirector();
        director.cancelFireAt(this, new Time(director, 1.0), 1);
        director.cancelFireAt(this, new Time(director, 3.0));
    }

    /** Schedule firings at times 0, 1, 2, 3, 4.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        Director director = getDirector();
        director.fireAt(this, new Time(director, 0.0));
        director.fireAt(this, new Time(director, 1.0));
        director.fireAt(this, new Time(director, 2.0));
        director.fireAt(this, new Time(director, 3.0));
        director.fireAt(this, new Time(director, 4.0));
    }
}
