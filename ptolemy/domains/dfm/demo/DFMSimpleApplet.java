/* A version of DFMSimple that is in a applet
 
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
 
package ptolemy.domains.dfm.demo;


import ptolemy.kernel.*;
import ptolemy.kernel.util.*;
import ptolemy.data.*;
import ptolemy.actor.*;
import ptolemy.domains.dfm.kernel.*;
import ptolemy.domains.dfm.lib.*;
import java.util.Enumeration;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// DFMSimpleFrame
/** 
 A very simple demo of DFM domain with a Frame and facet.  It contains 
 couple actors that does
 some floating number calculations with loops and conditions.
@author  William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
*/
public class DFMSimpleFrame {


   public DFMSimpleFrame() throws
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

        facetImage facet = new facetImage();

        DFMDoubleSourceActor source1 = new DFMDoubleSourceActor(myUniverse, "source1");
        source1.changeParameter("Value", String.valueOf(3.0));
        DFMActorDrawer source1draw = new DFMActorDrawer(20,9,69,71, facet);
        DFMPortDrawer source1outportdraw = new DFMPortDrawer(76,58, facet);
        source1.addActorDrawer(source1draw);
        source1.addPortDrawer("output", source1outportdraw);
 
        DFMDoubleSourceActor source2 = new DFMDoubleSourceActor(myUniverse, "source2");
        source2.changeParameter("Value", String.valueOf(5.0));
        DFMActorDrawer source2draw = new DFMActorDrawer(19,86,68,158,facet);
        DFMPortDrawer source2outportdraw = new DFMPortDrawer(72, 102, facet);
        source2.addActorDrawer(source2draw);
        source2.addPortDrawer("output", source2outportdraw);
  
        DFMArithmeticActor plus = new DFMArithmeticActor(myUniverse, "plus1", "ADD");
        DFMActorDrawer plusdraw = new DFMActorDrawer(91,45,137,110,facet);
        DFMPortDrawer plusoutportdraw = new DFMPortDrawer(143,68, facet);
        plus.addActorDrawer(plusdraw);
        plus.addPortDrawer("output", plusoutportdraw);

        IOPort portin1 = (IOPort) plus.getPort("input1");
        IOPort portin2 = (IOPort) plus.getPort("input2");
        IOPort portout1 = (IOPort) source1.getPort("output");
        IOPort portout2 = (IOPort) source2.getPort("output");
        myUniverse.connect(portin1, portout1, "first_plus_input_queue");
        myUniverse.connect(portin2, portout2, "second_plus_input_queue");

        DFMSelectInputActor inselect = new DFMSelectInputActor(myUniverse, "input select");
        DFMActorDrawer selectdraw = new DFMActorDrawer(162, 45, 201, 147, facet);
        DFMPortDrawer selectoutportdraw = new DFMPortDrawer(208, 104, facet);
        inselect.addActorDrawer(selectdraw);
        inselect.addPortDrawer("output", selectoutportdraw);

        portin1 = (IOPort) inselect.getPort("input1"); 
        portin2 = (IOPort) inselect.getPort("input2");

        DFMFeedbackActor feedback = new DFMFeedbackActor(myUniverse, "feedback", new DFMToken("New"));
        DFMActorDrawer feedbackdraw = new DFMActorDrawer(305, 142, 346, 184, facet);
        DFMPortDrawer feedbackoutportdraw = new DFMPortDrawer(227, 167, facet);
        feedback.addActorDrawer(feedbackdraw);
        feedback.addPortDrawer("output", feedbackoutportdraw);

        portout1 = (IOPort) plus.getPort("output");   
        portout2 = (IOPort) feedback.getPort("output");

        myUniverse.connect(portin1, portout1, "first_select_input_queue");
        myUniverse.connect(portin2, portout2, "second_select_input_queue");
        
        DFMDoubleSourceActor source3 = new DFMDoubleSourceActor(myUniverse, "source3");
        source3.changeParameter("Value", String.valueOf(8.0));
        DFMActorDrawer source3draw = new DFMActorDrawer(222,11,268,68,facet);
        DFMPortDrawer source3outportdraw = new DFMPortDrawer(269,60, facet);
        source3.addActorDrawer(source3draw);
        source3.addPortDrawer("output", source3outportdraw);
  
        DFMArithmeticActor mul = new DFMArithmeticActor(myUniverse, "plus", "MULTIPLY");
        DFMActorDrawer muldraw = new DFMActorDrawer(289,56,334,113,facet);
        DFMPortDrawer muloutportdraw = new DFMPortDrawer(336,85, facet);
        mul.addActorDrawer(muldraw);
        mul.addPortDrawer("output", muloutportdraw);

         
        portin1 = (IOPort) mul.getPort("input1"); 
        portin2 = (IOPort) mul.getPort("input2");
        portout1 = (IOPort) source3.getPort("output");   
        portout2 = (IOPort) inselect.getPort("output");

        myUniverse.connect(portin1, portout1, "first_mul_input_queue");
        myUniverse.connect(portin2, portout2, "second_mul_input_queue");

        DFMThreasholdActor threa = new DFMThreasholdActor(myUniverse, "threashold");
        threa.changeParameter("ThreasholdValue", String.valueOf(1000.0));
        DFMActorDrawer threadraw = new DFMActorDrawer(353, 58, 386, 112,facet);
        DFMPortDrawer threaoutportdraw = new DFMPortDrawer(360, 163, facet);
        threa.addActorDrawer(threadraw);
        threa.addPortDrawer("output", threaoutportdraw);
        
        portin1 = (IOPort) threa.getPort("input"); 
        portout1 = (IOPort) mul.getPort("output"); 
        myUniverse.connect(portin1, portout1, "threashold_input");

        portin1 = (IOPort) feedback.getPort("input"); 
        portout1 = (IOPort) threa.getPort("output"); 
        myUniverse.connect(portin1, portout1, "feedback_loop");

        DFMActorDrawer [] adrawers = {source1draw, source2draw, source3draw, muldraw, plusdraw, threadraw, feedbackdraw, selectdraw};
        DFMPortDrawer [] pdrawers = {source1outportdraw, source2outportdraw, source3outportdraw, muloutportdraw, plusoutportdraw, threaoutportdraw, feedbackoutportdraw, selectoutportdraw};
        facet.setActorDrawers(adrawers); 
        facet.setPortDrawers(pdrawers); 
        Frame frame = new Frame();
        frame.add("Center", facet);
        frame.setSize(450, 250);
        frame.setVisible(true);
        exec.run();
        System.out.println("Bye World\n");
        return;
   }


   public static void main(String args[]) throws
            IllegalStateException, IllegalActionException,
            NameDuplicationException {
        DFMSimpleFrame simple = new DFMSimpleFrame();
   }
}
