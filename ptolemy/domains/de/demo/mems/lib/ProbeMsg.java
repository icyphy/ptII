/* A data object that represents an environment state value.  

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

// import ptolemy.actor.*;
// import ptolemy.domains.de.kernel.*;
// import ptolemy.kernel.*;
// import ptolemy.kernel.util.*;
// import ptolemy.data.*;
// import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// ProbeMsg
/**
A data object that represents an environment state value.  Whenever an
environment state (ie temperature, coordinates, humidity, etc), a Probe
object initialized with the new enviornmental value will be created 
by the Environmental Value Generator (MEMSEVG).  The Probe object will be 
wrapped inside an ObjectToken, which will be transported to the sensors 
inside the MEMSProc.  

;;;;;;;; OLD
An probe object that rides inside an ObjectToken.  When the token reaches a 
MEMSEnvir Actor, the actor will call the "probe" method, passing a reference
of the MEMSEnvir itself as argument.  The "probe" method in turn calls the
getTemp() in MEMSEnvir and keeps a copy of the value it returns.
;;;;;;;; OLD

@author Allen Miu
@version $Id$
*/
abstract class ProbeMsg {

    /** Constucts a Probe object and initializes its _value to 0
     */

    public ProbeMsg() {

        /* OLD */ /*_value = 0.0; */
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Calls MEMSEnvir's getXXXXXX() and keeps a copy of the return
     *  value in _value
     *
     *  Must be overridden by a subclass to specify which getXXXXXX()
     *  to invoke.
     *
     *  @param envir The MEMSEnvir that called this method.
     *
     */
    /* OLD */
    //  public void probe(MEMSEnvir envir);

    /** Returns the probe value.
     */
    /* OLD */
    /*
      public double getValue() {
      return _value;
      }
    */

    /** Returns true if this object is of a type thermoProbe. */
    public boolean isThermoProbeMsg() { return thermoProbe; }
    /** Returns true if this object is of a type messageProbe. */
    public boolean isMessageProbeMsg() { return messageProbe; }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /* OLD */ /* protected double _value; */
    /* List of booleans indicating the message type */ 
    protected boolean thermoProbe = false;
    protected boolean messageProbe = false;
}
