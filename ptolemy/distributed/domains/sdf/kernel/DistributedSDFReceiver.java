/* Extends SDFReceiver with an ID.

@Copyright (c) 2005 The Regents of Aalborg University.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

IN NO EVENT SHALL AALBORG UNIVERSITY BE LIABLE TO ANY PARTY
FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
AALBORG UNIVERSITY HAS BEEN ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

AALBORG UNIVERSITY SPECIFICALLY DISCLAIMS ANY WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND AALBORG UNIVERSITY
HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
ENHANCEMENTS, OR MODIFICATIONS.


*/
package ptolemy.distributed.domains.sdf.kernel;

import ptolemy.actor.IOPort;
import ptolemy.domains.sdf.kernel.SDFReceiver;
import ptolemy.kernel.util.IllegalActionException;

//////////////////////////////////////////////////////////////////////////
//// DistributedSDFReceiver

/**
The DistributedSDFReceiver class extends SDFReceiver with an unique ID. This
is useful in order to unambiguously identify receivers in a distributed
environment.

@author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
@version
@since
@Pt.ProposedRating Red (kapokasa)
@Pt.AcceptedRating
@see ptolemy.domains.sdf.kernel.SDFReceiver
*/
public class DistributedSDFReceiver extends SDFReceiver {

    /** Construct an empty receiver with no container and unique ID.
     */
    public DistributedSDFReceiver() {
        super();
        init();
    }

    /** Construct an empty receiver with no container, given size and
     *  unique ID.
     *  @param size The size of the queue in the receiver.
     */
    public DistributedSDFReceiver(int size) {
        super(size);
        init();
    }

    /** Construct an empty receiver with the specified container and
     *  unique ID.
     *  @param container The container of the receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public DistributedSDFReceiver(IOPort container)
        throws IllegalActionException {
        super(container);
        init();
    }

    /** Construct an empty receiver with the specified container, size and
     *  unique ID.
     *  @param container The container of the receiver.
     *  @param size The size of the queue in the receiver.
     *  @exception IllegalActionException If the container does
     *   not accept this receiver.
     */
    public DistributedSDFReceiver(IOPort container, int size)
        throws IllegalActionException {
        super(container, size);
        init();
    }

    /** Construct an empty receiver with no container and a given ID.
     *  @param newID The new ID for the Receiver.
     */
    public DistributedSDFReceiver(Integer newId) {
        super();
        ID = newId;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the ID of the receiver.
     *  @return The ID of the Receiver.
     */
    public Integer getID() {
        return ID;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Initialize the receiver with an ID of value lastID. lastID is
     *  increased by 1.
     */
    protected void init() {
        ID = lastID;
        lastID = new Integer(lastID.intValue() + 1);
        if (VERBOSE) {
            System.out.println("Created Receiver: " + ID);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** Enables debugging messages. */
    protected boolean VERBOSE = false;

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Static member that contains the next ID value to be assigned to
     * the next receiver constructed. */
    private static Integer lastID = new Integer(1);

    /** ID of the receiver. */
    private Integer ID;
}
