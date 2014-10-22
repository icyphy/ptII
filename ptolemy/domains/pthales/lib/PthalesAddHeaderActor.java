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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

/**
 * Add and/or propagate information through relations to another
 * actor that can understand and apply modifications.
 *
 * @see ptolemy.domains.pthales.lib.PthalesRemoveHeaderActor

 * @author R&eacute;mi Barr&egrave;re
 * @version $Id$
 * @since Ptolemy II 10.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class PthalesAddHeaderActor extends PthalesAtomicActor {

    /** Construct an actor in the default workspace with an empty string
     *  as its name.  The object is added to the workspace directory.
     *  Increment the version number of the workspace.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public PthalesAddHeaderActor() throws IllegalActionException,
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
    public PthalesAddHeaderActor(CompositeEntity container, String name)
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
    public PthalesAddHeaderActor(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Read the contents of the array, add a header containing the
     * number of dimensions and the size of each dimension at the
     * beginning of the array.
     */
    @Override
    public void fire() throws IllegalActionException {

        // Variables
        IOPort portIn = (IOPort) getPort("in");
        IOPort portOut = (IOPort) getPort("out");

        // One port in theory
        //        IOPort previousPort = (IOPort) portIn.connectedPortList().get(0);
        int nbDims = PthalesIOPort.getDimensions(portIn).length;

        // Token Arrays from simulation
        Token[] tokensIn = null;

        // Input ports created and filled before elementary task called
        int dataSize = PthalesIOPort.getDataProducedSize(portIn)
                * PthalesIOPort.getNbTokenPerData(portIn);
        //        tokensIn = new Token[dataSize];
        tokensIn = portIn.get(0, dataSize);

        // Header construction
        List<Token> header = new ArrayList<Token>();

        LinkedHashMap<String, Integer> sizes = PthalesIOPort
                .getArraySizes(portIn);

        header.add(new IntToken(nbDims));
        header.add(new IntToken(PthalesIOPort.getNbTokenPerData(portIn)));

        for (String dim : PthalesIOPort.getDimensions(portIn)) {
            header.add(new StringToken(dim));
            header.add(new IntToken(sizes.get(dim)));
        }

        // then sent to output
        for (int i = 0; i < portOut.getWidth(); i++) {
            for (int j = 0; j < header.size(); j++) {
                portOut.send(i, header.get(j));
            }
            portOut.send(i, tokensIn, dataSize);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    @Override
    protected void _initialize() throws IllegalActionException,
            NameDuplicationException {
        super._initialize();

        // input port
        new TypedIOPort(this, "in", true, false);

        // output port
        TypedIOPort portOut = new TypedIOPort(this, "out", false, true);
        portOut.setTypeEquals(BaseType.GENERAL);
    }

}
