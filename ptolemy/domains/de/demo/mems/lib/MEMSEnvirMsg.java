/* A data object that represents the communication "packet" transferred
   between MEMSEnvir.  

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
*/

package ptolemy.domains.de.demo.mems.lib;

//////////////////////////////////////////////////////////////////////////
//// MEMSEnvirMsg
/** 
A data object that represents the communication "packet" transferred
between MEMSEnvir actors.  

The MEMSEnvir communication "layer" is one below the layer of MEMSDevice.
Hence, the MEMSEnvirMsg is simply a packet that contains the MEMSMsg
plus some extra "header" information used only by the MEMSEnvir Actors.
The header information is mainly used for describing the physical
characteristics (eg coordinates) of the environment from which the 
MEMSMsg is sent.  Using the header information provided by the source,
each MEMSEnvir Actors that receives the MEMSEnvirMsg can determine
how to process the MEMSMsg "packet" beforing passing it to its 
associated MEMSDevice object.

For example, when Device B recieves a message from Device A, the 
MEMSEnvir of Device B will compare its own coordinate values with 
Device A's coordinate values contained in the message header, and 
decide whether Device A is within range.  If not, the MEMSEnvir of 
Device B will discard the message.  Else, it will "unwrap" the
environmental information from the message and pass the original
MEMSMsg sent by Device A to Device B.

A standard MEMSEnvirMsg packet should contain the following information:

1) The Physical Coordinate (as represented by the Coord object) of
the MEMSDevice that generated the MEMSMsg

2) The MEMSMsg

@author Allen Miu
@version $Id$
*/
public class MEMSEnvirMsg {
    /** Constructs a MEMSEnvirMsg object to be transmitted between MEMSEnvir
     *  Actors
     *
     * @param coord = coordinates of the source
     * @param messge = the data being transported
     */
    public MEMSEnvirMsg(Coord coord, MEMSMsg message) {
        this.coord = coord;
        this.content = message;
        /* FIXME: might want a function to calculate the bandwidth */
        this.xferTimeCount = message.getSize();
    }

    /* header info */
    public Coord coord;
    /* message content */
    public MEMSMsg content;
    /* Transfer Time (in terms of clock cycles) Count */
    /* FIXME: might want to write get() and decr() methods for this value */
    public int xferTimeCount;
}
