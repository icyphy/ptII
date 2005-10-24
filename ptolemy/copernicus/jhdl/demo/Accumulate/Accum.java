/* A simple two port adder for integers

Copyright (c) 1997-2005 The Regents of the University of California.
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
package ptolemy.copernicus.jhdl.demo.Accumulate;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.copernicus.jhdl.*;
import ptolemy.data.IntToken;
import ptolemy.data.Token;
import ptolemy.data.type.*;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;


//////////////////////////////////////////////////////////////////////////
//// Accum

/** Add two integers.  This actor is a simple test for the JDHL/Ptolemy
    interface.

    @author Michael Wirthlin, Steven Neuendorffer, Edward A. Lee, Christopher Hylands
    @version $Id$
    @since Ptolemy II 2.0
    @Pt.ProposedRating Red (cxh)
    @Pt.AcceptedRating Red (cxh)
*/
public class Accum extends TypedAtomicActor {
    /** Construct an actor in the specified container with the specified
     *  name.
     *  @param container The container.
     *  @param name The name of this adder within the container.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public Accum(CompositeEntity container, String name)
        throws IllegalActionException, NameDuplicationException {
        super(container, name);

        input = new TypedIOPort(this, "input", true, false);
        input.setTypeEquals(BaseType.INT);

        output = new TypedIOPort(this, "output", false, true);
        output.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////
    public TypedIOPort input = null;

    /** Output port of type integer. */
    public TypedIOPort output = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    public void initialize() throws IllegalActionException {
        _state = new IntToken(0);
    }

    /** Get an integer token from the <i>input1</i> and add
     *  an integer token from the <i>input2</i> port and
     *  send the result to the <i>output</i> port.
     *
     *  @exception IllegalActionException If there is no director,
     *   or if addition and subtraction are not supported by the
     *   available tokens.
     */
    public void fire() throws IllegalActionException {
        _state = input.get(0).add(_state);
        output.send(0, _state);
    }

    private Token _state;
}
