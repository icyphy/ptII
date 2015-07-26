/*
@Copyright (c) 2014-2015 The Regents of the University of California.
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
/**
 *
 */
package ptolemy.domains.wireless.lib.bluetooth;

/**
 * This class defines all allowable Bluetooth Wired commands, that wil be parsed within the Bluetooth Device actor.
 *
 * @author Phillip Azar
 *
 * @see BluetoothDevice
 */
public enum BluetoothCommand {
    COMMAND_SWITCHON,
    COMMAND_SWITCHOFF,
    COMMAND_SCAN,
    COMMAND_STOPSCAN,
    COMMAND_CONNECT,
    COMMAND_DISCONNECT,
    COMMAND_PAIR,
    COMMAND_UNPAIR,
    COMMAND_DISCOVERABLE,
    COMMAND_HIDE,
    COMMAND_SENDDATA,
    COMMAND_NOCOMMAND
}
