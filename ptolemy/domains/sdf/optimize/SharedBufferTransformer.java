/* An abstract ancestor for filter actors acting on shared buffers

 Copyright (c) 1998-2010 The Regents of the University of California.
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

package ptolemy.domains.sdf.optimize;

import ptolemy.actor.lib.Transformer;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
////SharedBufferTransformer

/**
<h1>Class comments</h1>
SharedBufferTrasnformer is an abstract ancestor class to be used for filters
using references to shared data frames.
It implements a default version of the BufferingProfile interface.
<p>
See {@link ptolemy.domains.sdf.optimize.OptimizingSDFDirector} and 
{@link ptolemy.domains.sdf.optimize.BufferingProfile} for more information.
</p>
@see ptolemy.domains.sdf.optimize.OptimizingSDFDirector
@see ptolemy.domains.sdf.optimize.BufferingProfile

@author Marc Geilen
@version $Id: $
@since Ptolemy II 0.2
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
*/

public abstract class SharedBufferTransformer extends Transformer implements BufferingProfile {

    private boolean _nextIterationExclusive;

    @Override
    public void initialize() throws IllegalActionException {
        // default to copying firing
        _nextIterationExclusive = false;
        super.initialize();
    }

    public SharedBufferTransformer(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }
    
    public void fire() throws IllegalActionException {
        if(_nextIterationExclusive){
            fireExclusive();
        } else {
            fireCopying();
        }
    }

    public int sharedBuffers() {
        return 1;
    }

    public int exclusiveBuffers() {
        return 0;
    }

    public int sharedExecutionTime() {
        return 1;
    }

    public int exclusiveExecutionTime() {
        return 2;
    }

    protected abstract void fireExclusive() throws IllegalActionException;

    protected abstract void fireCopying() throws IllegalActionException;
 
    public int iterate(int iterationCount, boolean fireExclusive)
        throws IllegalActionException {
        _nextIterationExclusive  = fireExclusive;
        int result = super.iterate(iterationCount);
        // default to copying firing
        _nextIterationExclusive = false;
        return result;
    }
}
