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
public class DEThreadActor extends DEActor implements Runnable {

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
        PtolemyThread pthread = new PtolemyThread(this);
    }

    /**
     */
    public void fire() {
        notifyAll();
        
    }
    


    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////
    
    // Private variables should not have doc comments, they should
    // have regular C++ comments.
  
}

