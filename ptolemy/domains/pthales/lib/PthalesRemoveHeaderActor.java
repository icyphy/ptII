/* Actor that allows "dynamic" implementation of Pthales domain.

 Copyright (c) 2009-2014 The Regents of the University of California.
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

import ptolemy.actor.IOPort;
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
 * Remove header information from another actor and act as a flag to
 * propagate information from PthalesAddHeaderActor in the
 * preinitialize phase.
 *
 * @see ptolemy.domains.pthales.lib.PthalesAddHeaderActor
 *
 * @author R&eacute;mi Barr&egrave;re
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PthalesRemoveHeaderActor extends PthalesAtomicActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesRemoveHeaderActor() throws IllegalActionException,
    NameDuplicationException {
        super();
    }

    /** Create a new actor in the specified container with the specified
     *  name.  The name must be unique within the container or an exception
     *  is thrown. The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *
     *  @param container The container.
     *  @param name The name of this actor within the container.
     *  @exception IllegalActionException If this actor cannot be contained
     *   by the proposed container (see the setContainer() method).
     *  @exception NameDuplicationException If the name coincides with
     *   an entity already in the container.
     */
    public PthalesRemoveHeaderActor(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
    }

    /** Construct an actor in the specified workspace with an empty
     *  string as a name. You can then change the name with setName().
     *  If the workspace argument is null, then use the default workspace.
     *  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the entity.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesRemoveHeaderActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create receivers for each input port.
     *  @exception IllegalActionException If any port throws it.
     */
    @Override
    public void createReceivers() throws IllegalActionException {
        super.createReceivers();

        //set the receiver of the input port to type dynamic
        IOPort portIn = (IOPort) getPort("in");

        Receiver[][] receivers = portIn.getReceivers();

        for (int i = 0; i < portIn.getWidth(); i++) {
            ((PthalesReceiver) receivers[i][0]).setDynamic(true);
        }
    }

    /** Read the contents of the array, and extract the header containing
     * the number of dimensions and the size of each dimension
     * at the beginning of the array then send only the useful information.
     */
    @Override
    public void fire() throws IllegalActionException {

        // Variables
        IOPort portIn = (IOPort) getPort("in");
        IOPort portOut = (IOPort) getPort("out");

        // Token Arrays from simulation
        Token[] tokensIn = null;

        // Header
        int nDims = ((IntToken) portIn.get(0)).intValue();
        int nbTokens = ((IntToken) portIn.get(0)).intValue();
        Token[] headerIn = portIn.get(0, nDims * 2);

        // Input ports created and filled before elementary task called
        int dataSize = nbTokens;
        String[] dims = new String[nDims];
        // FindBugs: ptolemy/domains/pthales/lib/PthalesIOPort.java
        //int[] sizes = new int[dims.length];

        for (int i = 0; i < nDims; i++) {
            dataSize *= ((IntToken) headerIn[2 * i + 1]).intValue();
            dims[i] = ((StringToken) headerIn[2 * i]).stringValue();
            //sizes[i] = ((IntToken) headerIn[2 * i + 1]).intValue();
        }

        // Token Arrays from simulation
        tokensIn = portIn.get(0, dataSize);

        // then input sent to output
        for (int i = 0; i < portOut.getWidth(); i++) {
            portOut.send(i, tokensIn, dataSize);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    @Override
    protected void _initialize() throws IllegalActionException,
    NameDuplicationException {
        super._initialize();

        // input port
        new TypedIOPort(this, "in", true, false);

        // output port
        new TypedIOPort(this, "out", false, true);

    }

}
