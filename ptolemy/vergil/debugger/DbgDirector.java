/* Interface for the XXDbgDirector

 Copyright (c) 1999-2000 SUPELEC.
 All rights reserved.
 Permission is hereby granted, without written agreement and without
 license or royalty fees, to use, copy, modify, and distribute this
 software and its documentation for any purpose, provided that the above
 copyright notice and the following two paragraphs appear in all copies
 of this software.

 IN NO EVENT SHALL SUPELEC BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE
 OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF SUPELEC HAS BEEN ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 SUPELEC SPECIFICALLY DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT
 LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 PARTICULAR PURPOSE. THE SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS"
 BASIS, AND SUPELEC HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT,
 UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

                                        PT_COPYRIGHT_VERSION_2
                                        COPYRIGHTENDKEY
@ProposedRating Red (frederic.boulanger@supelec.fr)
@AcceptedRating Red 
*/

package ptolemy.vergil.debugger;

import java.util.List;

//////////////////////////////////////////////////////////////////////////
//// DbgDirector
/**
A common interface for XXXDbgDirector (useless if debugging capabilities
are included in director
@author SUPELEC team
@version $Id$
@see DbgDirector
@see ptolemy.vergil.debugger.DbgDirector
*/
public interface DbgDirector {

    /** Return the ExecState
     * @see ptolemy.vergil.debugger.DbgDirector#getState()
     * @return the ExecState of the director
     */
    //    public ExecState getState();

    public void addDebuggingListener(DebuggingListener listener);

    public void removeDebuggingListener(DebuggingListener listener);

    public List DebuggingListenerList();

    /** Set the Pdb
     * @see ptolemy.vergil.debugger.DbgDirector#setPdb()
     * @param pdb reference to the instance of the debugger
     */ 
    //    public void setPdb(Pdb pdb);

}
