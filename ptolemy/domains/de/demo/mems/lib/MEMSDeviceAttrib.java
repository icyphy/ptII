/* MMESDeviceAttrib

 Copyright (c) 1997-2000 The Regents of the University of California.
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
//// MEMSDeviceAttrib
/**

@author Allen Miu, Lukito Muliadi
@version $Id$
*/

/* this object is immutable */
public class MEMSDeviceAttrib {

    /* MEMS Device clock period for processing messages */
    private final double _clock = 1.0;

    /* probe processing time values */
    private final double _probeTemperatureProcTime = 2.0;

    /* defines the radius of the signal reception range */
    private double _range;

    /* the size of circular buffer that holds the id of seen messages */
    private final int _seenMsgBufSize = 256;

    /* the threshold temperature of which, when exceeded, 
       causes the MEMSDevice to trigger an alarm message */
    private final double _thermoAlarmThreshold = 72.0;

    /* message processing time values */
    private final double _thermoAlarmProcTime = 3.0;

    /* FIXME: might want to add bandwidth parameters (also in 
       MEMSEnvirMsg to calc xfertime) */

    /** Constructs a MEMSDeviceAttrib that describes the physical 
     *  characteristics of a particular MEMSDevice.
     */
    public MEMSDeviceAttrib(double range) {
        _range = range;
    }

    /* arranged in alphabetical order */
    public double getClockPeriod() { return _clock; }
    public double getProbeTempProcTime() { return _probeTemperatureProcTime; }
    public double getRange() { return _range; }
    public int    getSeenMsgBufSize() { return _seenMsgBufSize; }
    public double getThermoAlarmThreshold() { return _thermoAlarmThreshold; }
    public double getThermoAlarmProcTime()  { return _thermoAlarmProcTime; }

}
