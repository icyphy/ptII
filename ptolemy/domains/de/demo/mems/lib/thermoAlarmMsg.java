/* A MEMSMsg subclass that represents a Thermo Alarm message.

 Copyright (c) 1998-2000 The Regents of the University of California.
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

@ProposedRating Red (lmuliadi@eecs.berkeley.edu)

*/

package ptolemy.domains.de.demo.mems.lib;

//////////////////////////////////////////////////////////////////////////
//// thermoAlarmMsg
/**
A MEMSMsg subclass that represents a Thermo Alarm message.

@author Allen Miu
@version $Id$
*/
public class thermoAlarmMsg extends MEMSMsg {

    /** Constructs a thermoAlarmMsg object with a null content
     *
     * @param ASID = adjacent source id
     * @param SID  = source id
     * @param DID  = destination id
     */
    public thermoAlarmMsg(int ASID, int SID, int DID) {
        super(ASID,SID,DID,null);
        thermoAlarmMsg = true;
        size = 3;
    }

}




