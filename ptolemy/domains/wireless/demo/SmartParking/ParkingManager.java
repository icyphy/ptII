/*
 @Copyright (c) 2004-2005 The Regents of the University of California.
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

import ptolemy.data.IntToken;
import ptolemy.data.RecordToken;
import ptolemy.data.StringToken;

//////////////////////////////////////////////////////////////////////////
///ParkingManager

/**
 This class manages the information of which parking lot is taken, and
 which is still free.

 @author Yang Zhao
 @version $Id$
 @since Ptolemy II 3.0
 @Pt.ProposedRating Yellow (cxh)
 @Pt.AcceptedRating Yellow (cxh)
 */
public class ParkingManager {
    public static HashSet AvailableLots = new HashSet();

    public static HashSet ParkedLots = new HashSet();

    public ParkingManager() {
    }

    public ParkingManager(HashSet availableLots, HashSet parkedLots) {
        AvailableLots = new HashSet(availableLots);
        ParkedLots = new HashSet(parkedLots);
    }

    public synchronized void update(RecordToken updateMsg) {
        String lot = ((StringToken) updateMsg.get("lot")).stringValue();
        int state = ((IntToken) updateMsg.get("state")).intValue();

        if (state == 0) { //use 0 to represent the lot is free.
            AvailableLots.add(lot);
            ParkedLots.remove(lot);
        } else if (state == 1) {
            AvailableLots.remove(lot);
            ParkedLots.add(lot);
        }
    }

    public HashSet getAvailable() {
        return AvailableLots;
    }
}
