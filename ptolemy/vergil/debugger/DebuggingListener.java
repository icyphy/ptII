/* A listener for debugging event.

 Copyright (c) 1999-2000 SUPELEC.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL SUPELEC BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF
 THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF SUPELEC HAS BEEN ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 SUPELEC DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND
 SUPELEC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
 ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (frederic.boulanger@supelec.fr)
@AcceptedRating Red 
*/

package ptolemy.vergil.debugger;

import ptolemy.actor.*;
import ptolemy.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// DebuggingListener
/**
A listener for debugging event
@author SUPELEC team
@version $Id$
@see DebugginListener
@see ptolemy.vergil.debugger.DebugginListener
*/
public interface DebuggingListener {

    public void prefireEvent(Actor actor);
    public void fireEvent(Actor actor);
    public void postfireEvent(Actor actor);
    public void postpostfireEvent(Actor actor);

}  


