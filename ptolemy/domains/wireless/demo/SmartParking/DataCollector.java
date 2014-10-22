/*
 @Copyright (c) 2004-2014 The Regents of the University of California.
 All rights reserved.

 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the
 above copyright notice and the following two paragraphs appear in all
 copies of this software.

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
package ptolemy.domains.wireless.demo.SmartParking;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.BooleanToken;
import ptolemy.data.RecordToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.type.RecordType;
import ptolemy.data.type.Type;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
///DataCollector

/**
 This class collects sensor update for the state of each parking spot,
 and provides a parking client information of available parking spots.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class DataCollector extends TypedAtomicActor {
    public DataCollector(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        update = new TypedIOPort(this, "update", true, false);

        String[] labels = { "lot", "state" };
        Type[] types = { BaseType.STRING, BaseType.INT };
        RecordType recordType = new RecordType(labels, types);
        update.setTypeEquals(recordType);

        debug = new TypedIOPort(this, "debug", false, true);
        debug.setTypeEquals(recordType);

        isFull = new TypedIOPort(this, "isFull", false, true);
        isFull.setTypeEquals(BaseType.BOOLEAN);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Port that receives parking lot updates.
     */
    public TypedIOPort update;

    /** Output port for sending out the received message just for
     *  debugging.
     */
    public TypedIOPort debug;

    /** Output port for seting a signal light to tell people
     *  whehter the parking lot is full.
     */
    public TypedIOPort isFull;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** When it receives token from the update port, which is
     *  used to receive updates from sensors,
     *  it updates the set of available spots and signal an event
     *  to indicate whether the parking lot is full.
     *  @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (update.isOutsideConnected()) {
            if (update.hasToken(0)) {
                RecordToken updateMsg = (RecordToken) update.get(0);
                _parkingManager.update(updateMsg);
                debug.send(0, updateMsg);

                if (_parkingManager.getAvailable().size() == 0 && !_isFull) {
                    _isFull = true;
                    isFull.send(0, new BooleanToken("true"));
                }

                if (_parkingManager.getAvailable().size() > 0 && _isFull) {
                    _isFull = false;
                    isFull.send(0, new BooleanToken("false"));
                }
            }
        }
    }

    /** Initialize the private varialbles.
     *  @exception IllegalActionException If thrown by the base class.
     */
    @Override
    public void initialize() throws IllegalActionException {
        super.initialize();
        _parkingManager = new ParkingManager();
        _isFull = false;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ParkingManager _parkingManager;

    private boolean _isFull = false;
}
