/* A data object that informs the MEMSDevice actor that a message is
arriving.  

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
@ProposedRating Red (lmuliadi@eecs.berkeley.edu)
*/

package ptolemy.domains.de.demo.mems.lib;

// import ptolemy.actor.*;
// import ptolemy.domains.de.kernel.*;
// import ptolemy.kernel.*;
// import ptolemy.kernel.util.*;
// import ptolemy.data.*;
// import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// messageProbeMsg
/** 

A data object that informs the MEMSDevice actor that a message is
arriving. It contains a time value (in terms of cycle) indicating
the remaining time left before the message completes its transmission
at the receiver.

;;;;;;; OLD
 An probe object that invokes the getTemp() method in the MEMSEnvir
   actor and keeps a copy of the value returned by getTemp().
;;;;;;; OLD

@author Allen Miu
@version $Id$
*/
public class messageProbeMsg extends ProbeMsg {

  ///////////////////////////////////////////////////////////////////
  ////                         public methods                    ////

  /** Creates a messageProbe object that stores the temperature value
   */
  public messageProbeMsg(int xferTimeRemaining) {
    _xferTimeRemaining = xferTimeRemaining;
    messageProbe = true;
  }

  /** Returns the remaining time of the message transfer at the receiver.
   */
  public int getTimeRemaining() {
    return _xferTimeRemaining;
  }

  ///////////////////////////////////////////////////////////////////
  ////                         private methods                   ////
  private int _xferTimeRemaining;
}
