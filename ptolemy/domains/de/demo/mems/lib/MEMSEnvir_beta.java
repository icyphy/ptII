/* MEMSEnvir_beta

 Copyright (c) 1997-2000 The Regents of the University of California.
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
import ptolemy.domains.de.demo.mems.gui.*;
import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// MEMSEnvir_beta
/**

@author Allen Miu, Lukito Muliadi
@version $Id$
*/

public class MEMSEnvir_beta extends MEMSEnvir {

  public MEMSEnvir_beta(TypedCompositeActor container, String name, 
          MEMSDevice mems,
          double x, double y, double z, double temperature,
          MEMSPlot plot)
          throws IllegalActionException, NameDuplicationException  {
      super(container,name,mems,x,y,z,temperature,plot);
  }

    protected double getNewTemp() {
        double newTemp;
        if((curTime == 50)) {
            newTemp = 75.0;
        } else {
            newTemp = 69.0;
        }
        return newTemp;
    }

}
