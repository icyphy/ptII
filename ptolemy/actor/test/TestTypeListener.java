/* A dummy TypeListener for testing.

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

package ptolemy.actor.test;

import ptolemy.data.type.Type;
import ptolemy.actor.TypeEvent;
import ptolemy.actor.TypeListener;

//////////////////////////////////////////////////////////////////////////
//// TestTypeListener
/**
This dummy type listener implements the TypeListener interface.
It keeps the last type change event information in a String
message and returns that message in the getMessage() method.
The call to getMessage() clears the message.

@author Yuhong Xiong
@version $Id$
*/
public class TestTypeListener implements TypeListener {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the last type change message. The message includes the
     *  full name of the port, the old type, and the new type,
     *  separated by "/". A call to this message
     *  also clears the old message. If there is no type change
     *  event after the last call, this method returns the String
     *  "no type change".
     *  @return A String including the type change information.
     */
    public String getMessage() {
	String temp = new String(_message);
	_message = "no type change";
	return temp;
    }

    /** Notify that the type of a port is changed.
     *
     * @param event The type change event.
     */
    public void typeChanged(TypeEvent event) {
	_message = event.getPort().getFullName() + "/";

	Type oldtype = event.getOldType();
	_message += oldtype.toString() + "/";

	Type newtype = event.getNewType();
	_message += newtype.toString();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private String _message = "";
}
