/* Interface for actors to allow for different firing modes.

 Copyright (c) 1997-2010 The Regents of the University of California.
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

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
////BufferingProfile

/**
<h1>Class comments</h1>
BufferingProfile is an interface which when implemented by SDF actors
Lets the OptimizingSDFDirector choose from different firings with different properties
For now there are two firing modes define, one where the actor may assume it has 
exclusive access to the information encapsulated by the input tokens. In particular, 
this is used for models where tokens communicate references to shared data structures
such as video frames in image processing.
In the alternative firing mode, the actor may not assume exclusive access and hence is
not allowed to modify the frame.It may have to copy the frame first instead. 

FIXME: we may want to generalize the notion of a profile to make it more generic and allow
more than two modes of firing.

<p>
See {@link ptolemy.domains.sdf.optimize.optimizingSDFDirector} for more information.
</p>
@see ptolemy.domains.sdf.optimize.OptimizingSDFScheduler

@author Marc Geilen
@version $Id: $
@since Ptolemy II 0.2
@Pt.ProposedRating Red (mgeilen)
@Pt.AcceptedRating Red ()
*/
public interface BufferingProfile {
    /****
     * @return
     * returns the number of buffers required upon calling shared fire in excess of
     * the input and output buffer.
     */
    int sharedBuffers();
    
    /****
     * @return
     * returns the number of buffers required upon calling exclusive fire in excess of
     * the input and output buffer.
     */
    int exclusiveBuffers();

    /****
     * @return
     * returns the number of new buffers instantiated temporarily during copying fire.
     */
    int sharedExecutionTime();
    
    /****
     * @return
     * returns the net number of new buffers instantiated temporarily during exclusive fire.
     */
    int exclusiveExecutionTime();

    int iterate(int iterationCount, boolean fireExclusive) throws IllegalActionException;

}
