/* A base class for threaded DE domain actors.

 Copyright (c) 1998 The Regents of the University of California.
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

package ptolemy.domains.de.kernel;

import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DEThreadActor
/**
A base class for threaded DE domain actor.

@author Lukito Muliadi
@version $Id$
@see DEActor
*/
public abstract class DEThreadActor extends DEActor implements Runnable {

    /** Constructor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @param value The initial output event value.
     *  @param step The step size by which to increase the output event values.
     *
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public DEThreadActor(TypedCompositeActor container, String name)
	 throws NameDuplicationException, IllegalActionException  {
      super(container, name);
    }
  
    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////
    

    /**
     */
    public void initialize() {
        // start a thread.
        pThread = new PtolemyThread(this);
        _isWaiting = true;
        pThread.start();
    }

    /** Awake the thread running this actor.
     */
    public void fire() {
        // Set the flag to false, to make sure only this actor wakes up.
        _isWaiting = false;
        synchronized(_monitor) {
            _monitor.notifyAll();
        }
        // then wait until this actor go to wait.
        while (!_isWaiting) {
            synchronized(_monitor) {
                try {
                    _monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        
    }

    /** Implement this method to define the job of the threaded actor.
     */
    public abstract void run();

    /** Clear input ports then wait until 
     *  input events arrive.
     */
    public void waitForNewInputs() {

        _emptyPorts();
        
        // Set the flag to true, so the director can wake up.
        _isWaiting = true;      
        synchronized(_monitor) {
            _monitor.notifyAll();
        }

        while (_isWaiting) {
            synchronized(_monitor) {
                try {
                    _monitor.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    // Empty all receivers of all input ports.
    private void _emptyPorts() {
        Enumeration ports = inputPorts();
        while (ports.hasMoreElements()) {
            IOPort port = (IOPort)ports.nextElement();
            for (int ch = 0; ch < port.getWidth(); ch++) {
                try {
                    while (port.hasToken(ch)) {
                        port.get(ch);
                    }
                } catch (IllegalActionException e) {
                    e.printStackTrace();
                    throw new InternalErrorException(e.getMessage());
                }
            }
        }
    }
    

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // Private variables should not have doc comments, they should
    // have regular C++ comments.

    PtolemyThread pThread;
    protected boolean _isWaiting = true;

    protected static Object _monitor = new Object();

}

