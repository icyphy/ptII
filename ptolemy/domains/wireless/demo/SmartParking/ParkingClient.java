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

import java.util.HashSet;
import java.util.Random;

import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.TypedIOPort;
import ptolemy.data.IntToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;

///////////////////////////////////////////////////////////////////
///ParkingClient

/**
 This class contact with the ParkingManager to check the available
 parking spots and randomly choose one to park.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class ParkingClient extends TypedAtomicActor {
    public ParkingClient(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);
        carArrival = new TypedIOPort(this, "carArrival", true, false);
        parkingTo = new TypedIOPort(this, "parkingTo", false, true);
        parkingTo.setTypeEquals(BaseType.STRING);
        leave = new TypedIOPort(this, "leave", false, true);
        leave.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** Port that receives a car arrival event.
     */
    public TypedIOPort carArrival;

    /** Port for which lot to park.
     */
    public TypedIOPort parkingTo;

    /** Port for leave due to no parking lot available.
     */
    public TypedIOPort leave;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** check the available parking spots and randomly choose one to
     * park.
     * @exception IllegalActionException If the superclass throws it.
     */
    @Override
    public void fire() throws IllegalActionException {
        super.fire();

        if (carArrival.isOutsideConnected()) {
            if (carArrival.hasToken(0)) {
                carArrival.get(0);

                HashSet lots = _parkingManager.getAvailable();

                if (lots.size() > 0) {
                    Object[] lotsArray = lots.toArray();
                    int index = _getRandom(lots.size());
                    parkingTo.send(0,
                            new StringToken((String) lotsArray[index]));
                } else {
                    leave.send(0, new IntToken(_LEAVE));
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
        _random = new Random();
        _parkingManager = new ParkingManager();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////
    //  Generate random number uniformly distributed between 0 and the
    // <i>size</i> parameter.
    private int _getRandom(int size) {
        // Generate a double between 0 and 1, uniformly distributed.
        double randomValue = _random.nextDouble();
        double cdf = 0.0;
        int value = 0;

        for (int i = 0; i < size; i++) {
            cdf += 1.0 / size;

            if (randomValue <= cdf) {
                value = i;
                break;
            }
        }

        return value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    private ParkingManager _parkingManager;

    private Random _random;

    private static int _LEAVE = 1;
}
