/* This creates an example implementing Rate-distortion algorithm

 Copyright (c) 1998-1999 The Regents of the University of California.
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

package ptolemy.domains.pn.demo;
import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.data.expr.*;
import ptolemy.actor.*;
import ptolemy.domains.pn.kernel.*;
import ptolemy.domains.pn.lib.*;
import java.util.Enumeration;

//import gui.DynamicGraphView;

//////////////////////////////////////////////////////////////////////////
//// PNWavepacket
/** 
This is currently a Universe containing some PN Actors. This might not support
hierarchy currently.
@author  Mudit Goel
@version @(#)PNWavepacket.java	1.23 08/20/98
*/
public class PNWavepacket {

    public static void main(String argv[]) throws 
            IllegalStateException, IllegalActionException, 
            NameDuplicationException {
        int segmentsize = 4096;
	int blocks = 1;
	int numsamples = segmentsize;
	int numbits = 4;
        int rbudget = numsamples*numbits;
	
        CompositeActor myUniverse = new CompositeActor();
        myUniverse.setName("PNWavepacket");
	Manager exec = new Manager("exec");
        // FIXME FIXME FIXME
	myUniverse.setManager(exec);
	PNDirector local = new PNDirector("Local");
	myUniverse.setDirector(local);
        //myUniverse.setCycles(Integer.parseInt(args[0]));
        PNAudioSource source = new PNAudioSource(myUniverse, "source");
        //source.setInitState("/users/mudit/_Ptolemy/tycho/java/ptolemy/domains/pn/demo/test.bin");
	StringToken file = new StringToken("/users/mudit/ptII/ptolemy/domains/pn/demo/test.bin");
	Parameter param = (Parameter)source.getAttribute("Audio_Source");
	param.setToken(file);
        //Daub8
        double[] lowpass = {-0.010597401785, 0.032883011667, 0.030841381836, -0.187034811719, -0.027983769417, 0.630880767930, 0.71484650553, 0.230377813309};

        double[] highpass = new double[lowpass.length];
        for (int i = 0; i < lowpass.length; i++) {
            highpass[i] = lowpass[lowpass.length-1-i];
            if (i%2 == 1) {
                highpass[i] = -highpass[i];
            }
            System.out.println("hp["+i + "] = " + highpass[i]);
        }
 
        PNRDMainController controller = new PNRDMainController(myUniverse, "controller");
        //controller.setInitState(segmentsize, blocks, 1, rbudget, 1, highpass, lowpass);
	param = (Parameter)controller.getAttribute("Segment_Size");
	param.setToken(new IntToken(segmentsize));
	param = (Parameter)controller.getAttribute("Maximum_Number_Of_Blocks");
	param.setToken(new IntToken(blocks));
	param=(Parameter)controller.getAttribute("Different_Number_Of_Blocks");
	param.setToken(new IntToken(1));
	param=(Parameter)controller.getAttribute("Rate_Budget");
	param.setToken(new IntToken(rbudget));
	param=(Parameter)controller.getAttribute("Maximum_Tree_Depth");
	param.setToken(new IntToken(1));
        //controller.setInitState(segmentsize, blocks, 1, rbudget, 1);

        IOPort portin = (IOPort)controller.getPort("input");
        IOPort portout = (IOPort)source.getPort("output");
        myUniverse.connect(portin, portout, "source_queue");
        //portin.getQueue(portout).setCapacity(1);

        portin = (IOPort)source.getPort("input");
        portout = (IOPort)controller.getPort("output");
        myUniverse.connect(portin, portout, "source_in_queue");
        //portin.getQueue(portout).setCapacity(1);

        //myUniverse.start();
	exec.run();
        System.out.println("Bye World\n");
        return;
    }
}



