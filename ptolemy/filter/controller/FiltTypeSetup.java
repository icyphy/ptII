/* A panel to set up the type of a filter. 
 
Copyright (c) 1997-1998 The Regents of the University of California.
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

$Id$ %S% 
*/

package ptolemy.filter.controller;

import ptolemy.math.FType;
import java.awt.*;
import java.awt.event.*;

//////////////////////////////////////////////////////////////////////////
//// FiltTypeSetup 
/**
 * A panel allow user to input the type for a filter. 
 * a IIR, FIR M & P, FIR Frequency sampling, FIR Windowed, FIR Comb, 
 * 
 * This use an event model that will not work in jdk1.0.
 * 
 * author: William Wu
 * version:
 * date: 4/2/98
 */

public class FiltTypeSetup extends Panel implements ActionListener {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
 
    /**
     * Constructor.  Set up the panel for user to input the initial parameter
     * for the IIR filter, these parameters is only set at this stage of the 
     * design while the bands data can be changed on the plot.
     */
    public FiltTypeSetup(TMain t){

      _tmain = t;
  
      // filter type 
      _ftype = new CheckboxGroup();
      _iir = new Checkbox("IIR", _ftype, true);
      _firWin = new Checkbox("FIR Windowed", _ftype, false);
      _firMP = new Checkbox("FIR M & P", _ftype, false);
      _firFS = new Checkbox("FIR Frequency Sampling", _ftype, false);
      _firComb = new Checkbox("FIR Comb filter", _ftype, false);
   
      // type and name 
   
      _p0 = new Panel();
      _p0.setLayout(new FlowLayout(5,5,5)); 
      _p0.add(new Label("Enter the name"));
      _name = new TextField(15); 
      _p0.add(_name);

      _p1 = new Panel();
      _p1.setLayout(new BorderLayout(5, 5));
      _p1.add("North", new Label("Select Filter Type"));
      _p1.add("Center", _iir);
      _p1.add("South", _firWin);

      _p2 = new Panel();
      _p2.setLayout(new BorderLayout(5, 5));
      _p2.add("North", _firMP);
      _p2.add("Center", _firComb);
      _p2.add("South", _firFS);

      _p3 = new Panel();
      _p3.setLayout(new BorderLayout(5, 5));
      _p3.add("North", _p0);
      _p3.add("Center",_p1);
      _p3.add("South", _p2);
       
      _ok = new Button("OK");

      _ok.setActionCommand("OK"); 
      _ok.addActionListener(this);
      this.setLayout(new BorderLayout(10, 10));
      this.add("Center", _p3);
      this.add("South", _ok);
   }

   /**
    * Handle the event from the ok button.  Obtain all the information
    * from the widgets and send them to TMain.
    */ 
   public void actionPerformed(ActionEvent evt){

      if (evt.getActionCommand().equals("OK")){
          int ft;
          String name;

          // only IIR filter is supported now
          if (_iir.getState() == true){
              ft = FType.IIR;
          } else if (_firWin.getState() == true){
              ft = FType.IIR;
             // ft = FType.FIRWin;
          } else if (_firMP.getState() == true){
              ft = FType.IIR;
              // ft = FType.FIROpt;
          } else if (_firFS.getState() == true){
              ft = FType.IIR;;
              // ft = FType.FIRFs;
          } else if (_firComb.getState() == true){
              ft = FType.IIR;
              // ft = FType.FIRComb;
          } else ft = 0;

          name = _name.getText();

          _tmain.newFilter(ft, name);
       }  
         
   }

   //////////////////////////////////////////////////////////////////////////
   ////                         private variables                        ////
 
   private Panel _p0, _p1, _p2, _p3;

   private TextField _name;
   private CheckboxGroup _ftype;
   private Checkbox _iir, _firWin, _firMP, _firFS, _firComb;
   private Button _ok;
   private TMain _tmain;
}





