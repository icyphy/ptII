/* A simple demo of DFM domain.

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
@ProposedRating Green (yourname@eecs.berkeley.edu)
@AcceptedRating Green (reviewmoderator@eecs.berkeley.edu)

*/

package ptolemy.domains.dfm.demo;

import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.domains.dfm.data.*;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.domains.dfm.lib.*;
import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// DFMSimple
/** 
 A very simple demo of DFM domain.  It contains couple actors that does
 some floating number calculations with loops and conditions.
@author  William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
*/
public class DFMSimple {

   public static void main(String args[]) throws
            IllegalStateException, IllegalActionException,
            NameDuplicationException {

        CompositeActor myUniverse = new CompositeActor();

        myUniverse.setName("Simple_example");
        Manager exec = new Manager("exec");
        // FIXME FIXME FIXME
        myUniverse.setManager(exec);
        DFMDirector local = new DFMDirector("Local");
        myUniverse.setDirector(local);
        //myUniverse.setCycles(Integer.parseInt(args[0]));
        DFMDoubleSourceActor source1 = new DFMDoubleSourceActor(myUniverse, "source1");
        source1.changeParameter("Value", String.valueOf(3.0));
        DFMDoubleSourceActor source2 = new DFMDoubleSourceActor(myUniverse, "source2");
        source2.changeParameter("Value", String.valueOf(5.0));
  
        DFMArithmeticActor plus = new DFMArithmeticActor(myUniverse, "plus1", "ADD");
        IOPort portin1 = (IOPort) plus.getPort("input1");
        IOPort portin2 = (IOPort) plus.getPort("input2");
        IOPort portout1 = (IOPort) source1.getPort("output");
        IOPort portout2 = (IOPort) source2.getPort("output");
        myUniverse.connect(portin1, portout1, "first_plus_input_queue");
        myUniverse.connect(portin2, portout2, "second_plus_input_queue");

        DFMSelectInputActor inselect = new DFMSelectInputActor(myUniverse, "input select");
        portin1 = (IOPort) inselect.getPort("input1"); 
        portin2 = (IOPort) inselect.getPort("input2");

        DFMFeedbackActor feedback = new DFMFeedbackActor(myUniverse, "feedback", new DFMToken("New"));
        portout1 = (IOPort) plus.getPort("output");   
        portout2 = (IOPort) feedback.getPort("output");

        myUniverse.connect(portin1, portout1, "first_select_input_queue");
        myUniverse.connect(portin2, portout2, "second_select_input_queue");
        
        DFMDoubleSourceActor source3 = new DFMDoubleSourceActor(myUniverse, "source3");
        source3.changeParameter("Value", String.valueOf(8.0));

        DFMArithmeticActor mul = new DFMArithmeticActor(myUniverse, "plus", "MULTIPLY");
         
        portin1 = (IOPort) mul.getPort("input1"); 
        portin2 = (IOPort) mul.getPort("input2");
        portout1 = (IOPort) source3.getPort("output");   
        portout2 = (IOPort) inselect.getPort("output");

        myUniverse.connect(portin1, portout1, "first_mul_input_queue");
        myUniverse.connect(portin2, portout2, "second_mul_input_queue");

        DFMThreasholdActor threa = new DFMThreasholdActor(myUniverse, "threashold");
        threa.changeParameter("ThreasholdValue", String.valueOf(1000.0));
        
        portin1 = (IOPort) threa.getPort("input"); 
        portout1 = (IOPort) mul.getPort("output"); 
        myUniverse.connect(portin1, portout1, "threashold_input");

        portin1 = (IOPort) feedback.getPort("input"); 
        portout1 = (IOPort) threa.getPort("output"); 
        myUniverse.connect(portin1, portout1, "feedback_loop");

        exec.run();
        System.out.println("Bye World\n");
        return;
        
    }
}
