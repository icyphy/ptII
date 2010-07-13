/* An dynamic rate multidimensional SDF actor.

 Copyright (c) 2009-2010 The Regents of the University of California.
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

package ptolemy.domains.pthales.lib;

import java.util.LinkedHashMap;

import ptolemy.actor.IOPort;
import ptolemy.actor.NoTokenException;
import ptolemy.actor.Receiver;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.domains.pthales.kernel.PthalesReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 A composite actor imposes the use of PthalesIOPort
 as they contain needed values used by PThalesDirector.
 A PthalesCompositeActor can contain actors from different model (as SDF),
 but the port must be a PthalesIOPort, because of the ArrayOL parameters. 

 @author Dai Bui
 @see ptolemy.actor.TypedIOPort
 @version $Id: $
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (daib)
 @Pt.AcceptedRating Red (daib)
 */

public class PthalesDynamicCompositeActor extends PthalesCompositeActor {

    /**
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public PthalesDynamicCompositeActor() throws NameDuplicationException,
            IllegalActionException {
        // TODO Auto-generated constructor stub
    }

    /**
     * @param container
     * @param name
     * @throws IllegalActionException
     * @throws NameDuplicationException
     */
    public PthalesDynamicCompositeActor(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param workspace
     * @throws NameDuplicationException
     * @throws IllegalActionException
     */
    public PthalesDynamicCompositeActor(Workspace workspace)
            throws NameDuplicationException, IllegalActionException {
        super(workspace);
        // TODO Auto-generated constructor stub
    }

    /** compute the number of iterations of the actor
     * 
     * @return The number of iterations of the actor.
     * @throws NoTokenException
     * @throws IllegalActionException
     */
    public int computeIterations() throws NoTokenException,
            IllegalActionException {

        //FIXME This implementation does not consider multiple input ports.
        //we need to implement a more general function, specified by users
        //that computes the iterations based on the values at the input ports.

        for (Object port : inputPortList()) {
            IOPort portIn = (IOPort) port;

            // Header
            int nDims = ((IntToken) portIn.get(0)).intValue();
            int nbTokens = ((IntToken) portIn.get(0)).intValue();
            Token[] headerIn = portIn.get(0, nDims * 2);

            //            int iterations = nbTokens;
            LinkedHashMap<String, Integer> sizes = new LinkedHashMap<String, Integer>();
            for (int i = 0; i < nDims; i++) {
                sizes.put(((StringToken) headerIn[2 * i]).stringValue(),
                        ((IntToken) headerIn[2 * i + 1]).intValue());
                //                iterations *= ((IntToken) headerIn[2 * i + 1]).intValue();
            }

            //          Simple example : pattern is fixed and iterations
            LinkedHashMap<String, Integer[]> patternDims = PthalesIOPort
                    .getInternalPattern(portIn);
            LinkedHashMap<String, Integer[]> tilingDims = PthalesIOPort
                    .getTiling(portIn);

            int iterations = 1;
            
            // Input array dimension
            Object[] dims = tilingDims.keySet().toArray();

            for (int i = 0; i < tilingDims.size(); i++) {

                // No tiling => no repetition on dimension => next dim
                if (tilingDims.get(dims[i]) != null) {
                    int nb = nbTokens;
                    int jump = 1;
                    if (patternDims.get(dims[i]) != null) {
                        nb = (patternDims.get(dims[i])[0] - 1)
                                * patternDims.get(dims[i])[1] + 1;
                        jump = patternDims.get(dims[i])[1];
                    }
                    int val = (int) Math
                            .floor((sizes.get(dims[i]) - nb) / jump) + 1;

                    iterations *= val;

                }
            }
            
            return iterations;
        }
        return 0;
    }

    /** Initialize this actor.  
     *  @exception IllegalActionException If a super class throws it.
     */
    public void initialize() throws IllegalActionException {
        super.initialize();

        for (Object portIn : inputPortList()) {
            Receiver[][] receivers = ((TypedIOPort) portIn).getReceivers();

            for (int i = 0; i < ((IOPort) portIn).getWidth(); i++) {
                ((PthalesReceiver) receivers[i][0]).setDynamic(true);
            }
        }
    }

    /** Invoke a specified number of iterations of the actor. An
     *  iteration is equivalent to invoking prefire(), fire(), and
     *  postfire(), in that order. In an iteration, if prefire()
     *  returns true, then fire() will be called once, followed by
     *  postfire(). Otherwise, if prefire() returns false, fire()
     *  and postfire() are not invoked, and this method returns
     *  NOT_READY. If postfire() returns false, then no more
     *  iterations are invoked, and this method returns STOP_ITERATING.
     *  Otherwise, it returns COMPLETED.  If stop() is called during
     *  this iteration, then cease iterating and return STOP_ITERATING.
     *  <p>
     *  This base class method actually invokes prefire(), fire(),
     *  and postfire(), as described above, but a derived class
     *  may override the method to execute more efficient code.
     *
     *  @param count The number of iterations to perform.
     *  @return NOT_READY, STOP_ITERATING, or COMPLETED.
     *  @exception IllegalActionException If iterating is not
     *   permitted, or if prefire(), fire(), or postfire() throw it.
     */
    public int iterate(int count) throws IllegalActionException {
        int iterations = count;

        //check if we need to compute the iterations
        if (count == 0) {
            iterations = computeIterations();
        }

        return super.iterate(iterations);
    }
}
