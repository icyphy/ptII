/* MainTest2

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

package ptolemy.domains.de.demo.mems.demo;

import ptolemy.domains.de.demo.mems.lib.*;
import ptolemy.domains.de.demo.mems.gui.*;

import ptolemy.plot.*;
import ptolemy.actor.*;
import ptolemy.domains.de.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.kernel.*;

//////////////////////////////////////////////////////////////////////////
//// MainTest2
/**

@author Allen Miu
@version $Id$
*/

public class MainTest2 {
    static public void main(String args[]) throws 
            NameDuplicationException,
            IllegalActionException {

   
        double stoptime = 0.0;
    
        // create plot object
        MEMSPlot plot=new MEMSPlot(10,10);

    
        try {
            stoptime = Double.valueOf(args[0]).doubleValue();
            if(args.length > 1) {
                Debug.debugLevel = Integer.parseInt(args[1]);
                System.out.println("Running DEBUG LEVEL = " + args[1]);
            }
        } catch (Exception e) {
            System.err.println("Usage:  MainTest STOP_TIME [DEBUG_LEVEL]");
            System.exit(1);
        }


        // Create the top level Composite Actor
        TypedCompositeActor sys = new TypedCompositeActor();
        sys.setName("DESystem");

        // Create directors and associate them with the top level 
        // composite actor.
        DECQDirector dir = new DECQDirector(sys, "DELocalDirector");
        Manager manager = new Manager();
        sys.setManager(manager);

        // Build the system
        MEMSGlob glob = new MEMSGlob(sys,"Glob",plot);
    
        MEMSDevice mems1 = new MEMSDevice(sys,"Device");
        MEMSDevice mems2 = new MEMSDevice(sys,"Device");
        MEMSDevice mems3 = new MEMSDevice(sys,"Device");
        MEMSDevice mems4 = new MEMSDevice(sys,"Device");
        MEMSDevice mems5 = new MEMSDevice(sys,"Device");
    
        MEMSEnvir  envr1 = new MEMSEnvir_alpha(sys,"Enviro",mems1,1.0,6.0,0,69,plot);
        MEMSEnvir  envr2 = new MEMSEnvir(sys,"Enviro",mems2,2.5,2,0,69,plot);
        MEMSEnvir  envr3 = new MEMSEnvir(sys,"Enviro",mems3,6.0,1.0,0,69,plot);
        MEMSEnvir  envr4 = new MEMSEnvir(sys,"Enviro",mems4,8.0,6.0,0,69,plot);
        MEMSEnvir  envr5 = new MEMSEnvir(sys,"Enviro",mems5,5.0,8.0,0,69,plot);

        ComponentRelation r1 = sys.connect(mems1.msgIO,envr1.deviceMsgIO,"R1");
        ComponentRelation r2 = sys.connect(mems2.msgIO,envr2.deviceMsgIO,"R2");
        ComponentRelation r3 = sys.connect(mems3.msgIO,envr3.deviceMsgIO,"R3");
        ComponentRelation r4 = sys.connect(mems4.msgIO,envr4.deviceMsgIO,"R4");
        ComponentRelation r5 = sys.connect(mems5.msgIO,envr5.deviceMsgIO,"R5");

        ComponentRelation r6 = sys.connect(envr1.carrierMsgIO,envr2.carrierMsgIO,"R6");
        envr3.carrierMsgIO.link(r6);
        envr4.carrierMsgIO.link(r6);
        envr5.carrierMsgIO.link(r6);

        dir.setStopTime(stoptime);
        manager.run();
    }
}
