/* A data object that represents the communication "packet" transferred
   between MEMSDevice.

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
//// MEMSMsg
/**
   A data object that represents the communication "packet" transferred
   between MEMSDevice.  It is an abstract class which should be extended
   by subclasses that represent specify message types (eg thermoAlarmMsg
   MEMS messages)

   A standard packet should have the following information:

   Adjacent Source ID (ASID) = The ID of the MEMSDevice last forwarded
   the current packet.

   Source ID (SID) = The ID of the original MEMSDevice that generated
   the current packet.

   Destination ID (DID) = The ID of the current packet's destination
   MEMSDevice.

   Message ID = An unique message ID number (to be assigned internally by this class.

@author Allen Miu
@version $Id$
*/
public abstract class MEMSMsg {
  /** Constructs a MEMSMsg object to be transmitted between MEMSDevices
   *  and assigns an unique message ID to the current object.
   *
   * @param ASID = adjacent source id
   * @param SID  = source id
   * @param DID  = destination id
   * @param content = message content
   */
  public MEMSMsg(int ASID, int SID, int DID, Object content) {
    this.ASID = ASID;
    this.SID = SID;
    this.DID = DID;
    this.content = content;
    _msgID = msgID_count;
    msgID_count++;
  }

  /** Returns the size (in terms of clock ticks) of this message
   */
  public int getSize() {
    return size;
  }

  /** Returns this object's message ID
   */
  public int getID() {
    return _msgID;
  }
  
  /** Returns true if this MEMSMsg is of a type thermoAlarmMsg.
   */
  public boolean isThermoAlarm() { return thermoAlarmMsg; }

  /** Returns true if this MEMSMsg is of a type garbledMsg.
   */
  public boolean isGarbledMsg() { return garbledMsg; }

  /* A counter that keeps the next unique msgID value */
  protected static int msgID_count = 0;
  /* List of booleans indicating the message type */
  protected boolean thermoAlarmMsg = false;
  protected boolean garbledMsg = false;

  /* A special ID value representing "all nodes."  If the DID has this
     value, it means that this packet is destined for ALL MEMSDevice. 
  */
  public static final int ALL_NODES = -1;
  /* adjacent source id */
  public int ASID;
  /* source id */
  public int SID;
  /* destination id */
  public int DID;
  /* message content */
  public Object content;
  /* message size */
  /* FIXME: might want to write get() to return a value normalized to the bandwidth of this MEMSDevice */
  protected int size = 1;

  private int _msgID;

}




