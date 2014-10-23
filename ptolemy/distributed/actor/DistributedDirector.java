/* A Director governs the execution of a CompositeActor in a Distributed
 environment.

 @Copyright (c) 2005-2014 The Regents of Aalborg University.
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
package ptolemy.distributed.actor;

import java.util.HashMap;
import java.util.LinkedList;

import ptolemy.actor.Director;
import ptolemy.actor.Receiver;
import ptolemy.distributed.domains.sdf.kernel.DistributedSDFReceiver;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;

///////////////////////////////////////////////////////////////////
//// DistributedDirector

/**
 The DistributedDirector extends Director to function on a distributed
 environment. It provides receivers of the type DistributedSDFReceiver.
 //TODO: Make it more generic that the type of receivers it provides can be
 changed in case different MoC are to be implemented.

 @see ptolemy.actor.Director

 @author Daniel Lazaro Cuadrado (kapokasa@kom.aau.dk)
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (kapokasa)
 @Pt.AcceptedRating Red (cxh)
 */
public class DistributedDirector extends Director {
    /** Construct a director in the default workspace with an empty string
     *  as its name.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public DistributedDirector() throws IllegalActionException,
    NameDuplicationException {
        super();
    }

    /** Construct a director in the workspace with an empty name.
     *  @param workspace The workspace of this object.
     *  @exception NameDuplicationException If construction of Time objects fails.
     *  @exception IllegalActionException If construction of Time objects fails.
     */
    public DistributedDirector(Workspace workspace)
            throws IllegalActionException, NameDuplicationException {
        super(workspace);
    }

    /** Construct a director in the given container with the given name.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.
     *  If the name argument is null, then the name is set to the
     *  empty string.
     *
     *  @param container The container.
     *  @param name The name of this director.
     *  @exception IllegalActionException If the name has a period in it, or
     *   the director is not compatible with the specified container, or if
     *   the time resolution parameter is malformed.
     *  @exception NameDuplicationException If the container already contains
     *   an entity with the specified name.
     */
    public DistributedDirector(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the map from ID to the receiver containing the ID.
     *  @return A HashMap from ID to the receiver containing the ID.
     */
    public HashMap getIdsReceiversMap() {
        return idsReceiversMap;
    }

    /** Return a DistributedSDFReceiver. The receiver created will have the
     *  next available ID in the listOfIds that has been previously set. In
     *  case the listOfIds is empty, the ID will be a number starting from
     *  1000 (lastID). This should never occur! It is there for testing
     *  purposes and could be removed.
     *  @return A DistributedSDFReceiver with the next available ID in the
     *  listOfIds.
     */
    @Override
    public Receiver newReceiver() {
        Integer ID;
        DistributedSDFReceiver receiver;

        if (!listOfIds.isEmpty()) {
            ID = (Integer) listOfIds.getFirst();
            listOfIds.removeFirst();
            receiver = new DistributedSDFReceiver(ID);
            idsReceiversMap.put(ID, receiver);

            if (VERBOSE) {
                System.out.println("DistributedDirector.Created receiver: "
                        + ID);
            }
        } else {
            ID = Integer.valueOf(lastId);
            lastId += 1;
            receiver = new DistributedSDFReceiver(ID);
        }

        return receiver;
    }

    /** Specify the list of IDs to be used for Receiver creation by the
     *  newReceiver method.
     *  @param list A list of IDs.
     */
    public void setListOfIds(LinkedList list) {
        if (VERBOSE) {
            System.out.println("DistributedDirector.setListOfIds: "
                    + list.toString());
        }

        listOfIds = list;
    }

    ///////////////////////////////////////////////////////////////////
    ////                       private   variables                 ////

    /** List of IDs to assign no newly created receivers. */
    private LinkedList listOfIds = new LinkedList();

    /** Starting ID number for whenever a receiver is created and the list
     *  of IDs is empty. This should never happen and it is meant for
     *  debugging purposes. */
    private int lastId = 1000;

    /** When true depicts debugging messages. */
    private boolean VERBOSE = false;

    /** Map from IDs to the receiver containing the ID. */
    private HashMap idsReceiversMap = new HashMap();
}
