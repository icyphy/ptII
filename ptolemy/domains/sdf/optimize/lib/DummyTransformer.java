/* DummyTransformer is a simple transformer actor implementing the
   BufferingProfile interface.
   It is used for testing the OptimizingSDFDirector.

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

package ptolemy.domains.sdf.optimize.lib;

import ptolemy.data.Token;
import ptolemy.domains.sdf.optimize.SharedBufferTransformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

/**
<h1>Class comments</h1>
A DummyTransformer is a simple actor with one input port and one output port
imitation a filter type of actor
It is used for testing the OptimizingSDFDirector.
<p>
See {@link ptolemy.domains.sdf.optimize.OptimizingSDFDirector},
{@link ptolemy.domains.sdf.optimize.SharedBufferTransformer},
{@link ptolemy.domains.sdf.optimize.OptimizingSDFScheduler} and
{@link ptolemy.domains.sdf.optimize.BufferingProfile} for more information.
</p>
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector
@see ptolemy.domains.sdf.optimize.SharedBufferTransformer
@see ptolemy.domains.sdf.optimize.BufferingProfile

@author Marc Geilen
@version $Id$
@since Ptolemy II 10.0
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
 */

public class DummyTransformer extends SharedBufferTransformer {

    /**
     * Constructs an instance of a dummy transformer actor for testing purposes.
     * It mimics a typical image processing operation on a shared frame buffer,
     * where it can perform the operation in-place, modifying the frame buffer,
     * or operate in a copying mode where it produces a new frame buffer, leaving
     * the original intact for other operations.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DummyTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                    protected fields                         ////

    /**
     * Fire the actor in shared firing mode.
     * It makes a copy of the frame buffer referred to by the reference token.
     * @exception IllegalActionException
     */
    @Override
    protected void _fireCopying() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token t = input.get(0);
            if (!(t instanceof DummyReferenceToken)) {
                throw new IllegalActionException(
                        "Token is of wrong type. Expected DummyReferenceToken");
            }
            DummyReferenceToken rt = (DummyReferenceToken) t;
            // Get and duplicate the frame
            DummyFrame f = ((DummyFrame) rt.getReference()).clone();
            f.value++;
            // send a new token
            output.send(0, new DummyReferenceToken(f));
        }
    }

    /**
     * Fire the actor in exclusive firing mode.
     * It directly modifies the frame buffer referred to by the reference token.
     * @exception IllegalActionException
     */
    @Override
    protected void _fireExclusive() throws IllegalActionException {
        if (input.hasToken(0)) {
            Token t = input.get(0);
            DummyReferenceToken rt = (DummyReferenceToken) t;
            // Get the frame without duplicating
            DummyFrame f = (DummyFrame) rt.getReference();
            f.value++;
            // send the original token
            output.send(0, t);
        }
    }

}
