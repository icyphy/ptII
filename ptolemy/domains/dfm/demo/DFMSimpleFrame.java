/* A version of DFMSimple that has a frame.
 
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
import ptolemy.domains.dfm.data.*;
import java.util.Enumeration;
import java.awt.*;
import java.awt.event.*;

//////////////////////////////////////////////////////////////////////////
//// DFMSimpleFrame
/** 
 A very simple demo of DFM domain with a Frame and facet.  It contains 
 couple actors that does
 some floating number calculations with loops and conditions.
@author  William Wu (wbwu@eecs.berkeley.edu) 
@version $id$ 
*/
public class DFMSimpleFrame implements ActionListener {

   public DFMSimpleFrame(int mode, Image facetpic) throws
            IllegalStateException, IllegalActionException,
            NameDuplicationException {

        go = new Button(" Go ");
        go.setActionCommand("go");
        go.addActionListener(this);
        source1param = new Button(" Edit Source 1 Param ");
        source1param.setActionCommand("edit source 1");
        source1param.addActionListener(this);
        source2param = new Button(" Edit Source 2 Param ");
        source2param.setActionCommand("edit source 2");
        source2param.addActionListener(this);
        source3param = new Button(" Edit Source 3 Param ");
        source3param.setActionCommand("edit source 3");
        source3param.addActionListener(this);
        threasholdparam = new Button(" Edit Threashold Param ");
        threasholdparam.setActionCommand("edit threashold");
        threasholdparam.addActionListener(this);

        CompositeActor myUniverse = new CompositeActor();

        myUniverse.setName("Simple_example");
        exec = new Manager("exec");
        // FIXME FIXME FIXME
        myUniverse.setManager(exec);
        local = new DFMDirector("Local");
        myUniverse.setDirector(local);

        //myUniverse.setCycles(Integer.parseInt(args[0]));

        if (mode == 1){ // frame mode 
            quit = new Button("Quit");
            quit.setActionCommand("quit");
            quit.addActionListener(this);
            facet = new facetImage("/users/wbwu/ptII/ptolemy/domains/dfm/demo/facet.gif", 400, 200);
        } else {
            facet = new facetImage(facetpic, 400, 200);
        }

        source1 = new DFMDoubleSourceActor(myUniverse, "source1");
        source1.changeParameter("Value", String.valueOf(3.0));
        source1win = new ParamWin("source1", "Value", 3.0); 
        DFMActorDrawer source1draw = new DFMActorDrawer(20,9,69,71, facet);
        DFMPortDrawer source1outportdraw = new DFMPortDrawer(76,58, facet, true);
        source1.addActorDrawer(source1draw);
        source1.addPortDrawer("output", source1outportdraw);
 
        source2 = new DFMDoubleSourceActor(myUniverse, "source2");
        source2.changeParameter("Value", String.valueOf(5.0));
        source2win = new ParamWin("source2","Value", 5.0); 
        DFMActorDrawer source2draw = new DFMActorDrawer(19,86,68,158,facet);
        DFMPortDrawer source2outportdraw = new DFMPortDrawer(72, 102, facet, true);
        source2.addActorDrawer(source2draw);
        source2.addPortDrawer("output", source2outportdraw);
  
        DFMArithmeticActor plus = new DFMArithmeticActor(myUniverse, "plus1", "ADD");
        DFMActorDrawer plusdraw = new DFMActorDrawer(91,45,137,110,facet);
        DFMPortDrawer plusoutportdraw = new DFMPortDrawer(143,68, facet, true);
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
        DFMPortDrawer selectoutportdraw = new DFMPortDrawer(208, 104, facet, true);
        inselect.addActorDrawer(selectdraw);
        inselect.addPortDrawer("output", selectoutportdraw);

        portin1 = (IOPort) inselect.getPort("input1"); 
        portin2 = (IOPort) inselect.getPort("input2");

        DFMFeedbackActor feedback = new DFMFeedbackActor(myUniverse, "feedback", new DFMToken("PreviousResultValid"));
        DFMActorDrawer feedbackdraw = new DFMActorDrawer(305, 142, 346, 184, facet);
        DFMPortDrawer feedbackoutportdraw = new DFMPortDrawer(227, 167, facet, true);
        feedback.addActorDrawer(feedbackdraw);
        feedback.addPortDrawer("output", feedbackoutportdraw);

        portout1 = (IOPort) plus.getPort("output");   
        portout2 = (IOPort) feedback.getPort("output");

        myUniverse.connect(portin1, portout1, "first_select_input_queue");
        myUniverse.connect(portin2, portout2, "second_select_input_queue");
        
        source3 = new DFMDoubleSourceActor(myUniverse, "source3");
        source3.changeParameter("Value", String.valueOf(8.0));
        source3win = new ParamWin("source3","Value", 8.0); 
        DFMActorDrawer source3draw = new DFMActorDrawer(222,11,268,68,facet);
        DFMPortDrawer source3outportdraw = new DFMPortDrawer(269,60, facet, true);
        source3.addActorDrawer(source3draw);
        source3.addPortDrawer("output", source3outportdraw);
  
        DFMArithmeticActor mul = new DFMArithmeticActor(myUniverse, "plus", "MULTIPLY");
        DFMActorDrawer muldraw = new DFMActorDrawer(289,56,334,113,facet);
        DFMPortDrawer muloutportdraw = new DFMPortDrawer(336,85, facet, true);
        mul.addActorDrawer(muldraw);
        mul.addPortDrawer("output", muloutportdraw);

         
        portin1 = (IOPort) mul.getPort("input1"); 
        portin2 = (IOPort) mul.getPort("input2");
        portout1 = (IOPort) source3.getPort("output");   
        portout2 = (IOPort) inselect.getPort("output");

        myUniverse.connect(portin1, portout1, "first_mul_input_queue");
        myUniverse.connect(portin2, portout2, "second_mul_input_queue");

        threa = new DFMThreasholdActor(myUniverse, "threashold");
        threa.changeParameter("ThreasholdValue", String.valueOf(1000.0));
        threawin = new ParamWin("threashold", "Threashold Value", 1000.0); 
        DFMActorDrawer threadraw = new DFMActorDrawer(353, 58, 386, 112,facet);
        DFMPortDrawer threaoutportdraw = new DFMPortDrawer(360, 163, facet, true);
        threa.addActorDrawer(threadraw);
        threa.addPortDrawer("output", threaoutportdraw);
        
        portin1 = (IOPort) threa.getPort("input"); 
        portout1 = (IOPort) mul.getPort("output"); 
        myUniverse.connect(portin1, portout1, "threashold_input");

        portin1 = (IOPort) feedback.getPort("input"); 
        portout1 = (IOPort) threa.getPort("output"); 
        myUniverse.connect(portin1, portout1, "feedback_loop");

        DFMActorDrawer [] adrawers = {source1draw, 
                                      source2draw, 
                                      source3draw, 
                                      muldraw, 
                                      plusdraw, 
                                      threadraw, 
                                      feedbackdraw, 
                                      selectdraw};

        DFMPortDrawer [] pdrawers = {source1outportdraw, 
                                     source2outportdraw, 
                                     source3outportdraw, 
                                     muloutportdraw, 
                                     plusoutportdraw, 
                                     threaoutportdraw, 
                                     feedbackoutportdraw, 
                                     selectoutportdraw};

        facet.setActorDrawers(adrawers); 
        facet.setPortDrawers(pdrawers); 

        myPanel = new Panel();
        Panel buttonPanel = new Panel();
        buttonPanel.setLayout(new FlowLayout(5,5,5));
        buttonPanel.add(go);
        buttonPanel.add(source1param);
        buttonPanel.add(source2param);
        buttonPanel.add(source3param);
        buttonPanel.add(threasholdparam);
        if (quit != null) buttonPanel.add(quit);
    
        myPanel.add("Center", facet);
        myPanel.add("South", buttonPanel);
        myPanel.setSize(725, 350); 
        if (mode == 1){ 
            Frame frame = new Frame();
            frame.add("Center", myPanel);
            frame.setSize(725, 350);
            frame.setVisible(true);
        }
   }

   public void actionPerformed(ActionEvent action){
        if (action.getActionCommand().equals("go")){
            facet.clearPortDrawerTokens();
            excutionThread = new MyThread();
            excutionThread.start(); 
            go.setEnabled(false);
        } else if (action.getActionCommand().equals("quit")){
            System.exit(0);
        } else if (action.getActionCommand().equals("edit source 1")){
            source1win.setSize(150, 90);
            source1win.setVisible(true); 
        } else if (action.getActionCommand().equals("edit source 2")){
            source2win.setSize(150, 90);
            source2win.setVisible(true); 
        } else if (action.getActionCommand().equals("edit source 3")){
            source3win.setSize(150, 90);
            source3win.setVisible(true); 
        } else if (action.getActionCommand().equals("edit threashold")){
            threawin.setSize(150, 90);
            threawin.setVisible(true); 
        }
   }

   public Panel getPanel(){
        return myPanel;
   }

   MyThread excutionThread;
   DFMDirector local;
   Manager exec;
   Panel myPanel;
   facetImage facet;
   ParamWin source1win;
   ParamWin source2win;
   ParamWin source3win;
   ParamWin threawin;

   Button go, quit, source1param, source2param, source3param, threasholdparam;
   DFMDoubleSourceActor source1; 
   DFMDoubleSourceActor source2; 
   DFMDoubleSourceActor source3; 
   DFMThreasholdActor threa; 
  
   class MyThread extends Thread {
        public void run() {
            System.out.println("*********  Manager start **********");
            exec.run(); 
        } 
   }

   class ParamWin extends Frame implements ActionListener {
        public ParamWin(String name, String label, double value){
             super("Set Parameter for "+name);
             _name = name;
             _initvalue = String.valueOf(value);
             add("North", new Label(name+" : "+label));
             param = new TextField(String.valueOf(value), 10);
             add("Center", param);
             Button ok = new Button("  OK  ");
             ok.setActionCommand("ok"); 
             ok.addActionListener(this); 
             Button cancel = new Button("  CANCEL "); 
             cancel.setActionCommand("cancel"); 
             cancel.addActionListener(this); 
             Panel buttonpanel = new Panel();
             buttonpanel.setLayout(new FlowLayout(5,5,5));
             buttonpanel.add(ok);
             buttonpanel.add(cancel);
             add("South", buttonpanel);
             setSize(160, 120);
             setVisible(false);
             setLocation(300,300);      
        }

        public void actionPerformed(ActionEvent action){
             if (action.getActionCommand().equals("ok")){
                 String value = param.getText();
                 _initvalue = new String(value); 
                 if (_name.equals("source1")){
                     facet.clearPortDrawerTokens();
                     if (source1.changeParameter("Value", value)){
                         setVisible(false);
                     }
                 } else if (_name.equals("source2")){
                     facet.clearPortDrawerTokens();
                     if (source2.changeParameter("Value", value)){
                         setVisible(false);
                     }
                 } else if (_name.equals("source3")){
                     facet.clearPortDrawerTokens();
                     if (source3.changeParameter("Value", value)){
                         setVisible(false);
                     }
                 } else if (_name.equals("threashold")){
                     facet.clearPortDrawerTokens();
                     if (threa.changeParameter("ThreasholdValue", value)){
                         setVisible(false);
                     }
                 }
             } else if (action.getActionCommand().equals("cancel")){
                 setVisible(false);
                 param.setText(_initvalue);
             }
        } 

        TextField param;
        String _name;
        String _initvalue; 

   }

   public static void main(String args[]) throws
            IllegalStateException, IllegalActionException,
            NameDuplicationException {
        DFMSimpleFrame simple = new DFMSimpleFrame(1, null);
   }
}
