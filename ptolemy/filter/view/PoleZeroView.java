/* Polezero view object 
 
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

package ptolemy.filter.view;
 
import ptolemy.filter.filtermodel.*; 
import ptolemy.math.PoleZero; 
import ptolemy.math.Complex; 

import java.util.*;
import java.awt.*;

//////////////////////////////////////////////////////////////////////////
//// PoleZeroView  
/** 
 * Pole-zero plot observer.  This observer is specificly for the pole zero
 * plot of a filter.  
 *
 * author: William Wu
 * version:
 * date: 3/2/98
 */ 
 
public class PoleZeroView extends View {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Default constructor.  Default operation mode is in frame mode.  
     */  
    public PoleZeroView(){
          PoleZeroPlot plot = new PoleZeroPlot();
          plot.setView(this);
          _plots = new Plot[1];
          _plots[0]=plot;
          _opMode = 0;
          // _frame = new InteractPoleZeroFrame("", plot);
          _frame = new Frame();
    }

    /**
     * Constructor.  Once the plot is created, it will be placed
     * in the frame, (if the View is in frame mode).  
     * Filter's <code> getPoleZero() </code> is called to get
     * the data and passed it too the plot.
     */ 
    public PoleZeroView(Observable filter, int mode, String name){
          PoleZeroPlot plot = new PoleZeroPlot();
          plot.setView(this);
          _plots = new Plot[1];
          _plots[0]=plot;
          _opMode = mode;

          _viewPanel = new Panel();
          _viewPanel.add("Center", plot);
          _viewPanel.resize(300,350);
          if (_opMode == 0){ // frame mode
              _frame = new Frame(name);
              _frame.add("Center", _viewPanel);
              _frame.resize(300, 350);
              _frame.show();
              //  _frame = new InteractPoleZeroFrame(name, plot);
              plot.init();
              plot.resize(300,300);
          } 
          _observed = filter;

          // get it all started 
          update(_observed, "UpdatedFilter");
    }  
    


    /**
     * To notify the view about the updated filter.  When 
     * filter is modified, the filter calls <code> notifyObservers()
     * </code>, which calls each observer's <code> update() </code>
     * function.  This update will query filter for the new
     * poles and zeros then pass them to the plot.
     * remember dataset 1: pole
     *          dataset 2: zero 
     *          dataset 3: non interact corrsponding pole 
     *          dataset 4: non interact corrsponding zero 
     */ 
    public void update(Observable o, Object arg){
          String command = (String)arg;
          if (command.equals("UpdatedFilter")){

               FilterObj jf = (FilterObj) _observed;
               ((InteractPlot)_plots[0]).eraseInteractComponents();
               _plots[0].eraseAllPoints(1);
               _plots[0].eraseAllPoints(2);
               InteractComponent ic;
               int polecount=0;
               int ind = 0;
               int zerocount=0;

               PoleZero [] data = jf.getPole();
               if (data!=null){

                   Vector polevec = new Vector();
                   for (int i = 0;i<data.length;i++){
                       polevec.addElement(data[i]);     
                   } 

                   polecount = data.length; 


                   while (polevec.size() != 0){
                       
                       PoleZero pole = (PoleZero) polevec.firstElement();
                       polevec.removeElementAt(0);
 
                       ic = new InteractComponent("Pole", 
                                InteractComponent.Cross);
                       ic.setDrawingParam(Color.black,5, false,
                                InteractComponent.SymmetricOri);
                       ic.setInteractParam(new String("Pole real"), 
                        new String("Pole imag"), InteractComponent.AllDegFree); 
                    
                       ic.setAssociation(1, ind);
                       ic.xv = pole.re(); 
                       ic.yv = pole.im();
                       ind++;

                       if (pole.conjugate != null){

                            PoleZero conjpole = pole.conjugate;
                            polevec.removeElement((Object) conjpole);
                            
                            if (conjpole.conjugate != pole) System.out.println(" error: in consistent conjugate pair");

                            InteractComponent icpair = new InteractComponent("Pole", 
                                InteractComponent.Cross);
                            icpair.setDrawingParam(Color.black,5, false,
                                InteractComponent.SymmetricOri);
                            icpair.setInteractParam(new String("Pole real"), 
                                new String("Pole imag"), 
                                InteractComponent.AllDegFree); 
                    
                            icpair.setAssociation(1, ind);
                            icpair.xv = conjpole.re(); 
                            icpair.yv = conjpole.im(); 
                            ind++;

                            // now set the link between two interact components
                            ic.setLink(icpair,
                                       InteractComponent.YaxisMirrorXaxisSynch);
                            icpair.setLink(ic,
                                    InteractComponent.YaxisMirrorXaxisSynch);

                            ((InteractPlot)_plots[0]).addInteractPoint(icpair, 
                                          1, icpair.xv, icpair.yv, false); 
                       }
                       ((InteractPlot)_plots[0]).addInteractPoint(ic, 
                              1, ic.xv, ic.yv, false); 
                    
                    }
                    
               }

               ind = 0;

               data = jf.getZero();
               if (data!=null){

                   zerocount = data.length; 

                   Vector zerovec = new Vector();
                   for (int i = 0;i<data.length;i++){
                       zerovec.addElement(data[i]);     
                   } 

                   while (zerovec.size() != 0){
                       
                       PoleZero zero = (PoleZero) zerovec.firstElement();
                       zerovec.removeElementAt(0);
 
                       ic = new InteractComponent("Zero", 
                                InteractComponent.Circle);
                       ic.setDrawingParam(Color.blue,5, false,
                                InteractComponent.SymmetricOri);
                       ic.setInteractParam(new String("Zero real"), 
                        new String("Zero imag"), InteractComponent.AllDegFree); 

                       ic.xv = zero.re(); 
                       ic.yv = zero.im(); 
                       ic.setAssociation(2, ind);
                       ind++;

                       if (zero.conjugate != null){

                            PoleZero conjzero = zero.conjugate;
                            zerovec.removeElement((Object) conjzero);
                            
                            if (conjzero.conjugate != zero) System.out.println(" error: in consistent conjugate pair");

                            InteractComponent icpair = new InteractComponent(
                                      "Zero", InteractComponent.Circle);
                            icpair.setDrawingParam(Color.blue,5, false,
                                      InteractComponent.SymmetricOri);
                            icpair.setInteractParam(new String("Zero real"), 
                                      new String("Zero imag"), 
                                      InteractComponent.AllDegFree); 
                    
                            icpair.setAssociation(2, ind);
                            icpair.xv = conjzero.re(); 
                            icpair.yv = conjzero.im(); 
                            ind++;

                            // now set the link between two interact components
                            ic.setLink(icpair,
                                       InteractComponent.YaxisMirrorXaxisSynch);
                            icpair.setLink(ic,
                                    InteractComponent.YaxisMirrorXaxisSynch);

                            ((InteractPlot)_plots[0]).addInteractPoint(icpair, 
                                   2, icpair.xv, icpair.yv, false); 
                        }
                        ((InteractPlot)_plots[0]).addInteractPoint(ic, 
                               2, ic.xv, ic.yv, false); 
                   }

               }
      
               if (zerocount > polecount){
                   for (int i=0;i<zerocount-polecount;i++){
                      // ic = new InteractComponent("CorrspondPole",2,Color.darkGray,0,8,
                      //        false, new String("Pole real"), 
                      //        new String("Pole imag"), 0, 0); 
                      // ic.xv = 0.0; 
                      // ic.yv = 0.0; 
                      // ic.dataset = 3;
                      // ((InteractPlot)_plots[0]).addInteractPoint(ic, 
                      //        3, 0.0, 0.0, false); 
                   }
               }
                       
  //             if (polecount > zerocount){
  //                 for (int i=0;i<polecount-zerocount;i++){
                       //ic = new InteractComponent("CorrspondZero",1,Color.cyan,0,8,
                       //       false, new String("Zero real"), 
                       //       new String("Zero imag"), 0, 0); 
                       //ic.xv = 0.0; 
                       //ic.yv = 0.0; 
                       //ic.dataset = 4;
                       //((InteractPlot)_plots[0]).addInteractPoint(ic, 
                       //       4, 0.0, 0.0, false); 
//                   }
//               }
  
               _plots[0].repaint();
          }
     }

     /**
      * New changes have been made on the plot.  View
      * passes the new data to the filter.
      */ 
     public void newChange(Vector data){
          FilterObj jf = (FilterObj) _observed;
          PoleZero [] pole;
          PoleZero [] zero;
          int pcount = 0;
          int zcount = 0;
          int curp = 0;
          int curz = 0;
          // count the number of poles and zeros
          for (int i=0;i<data.size();i++){
               InteractComponent ic = (InteractComponent) data.elementAt(i);
               if (ic.getDataSetNum() == 1) { // poles
                    pcount++;
               } else if (ic.getDataSetNum() == 2){ // zeros
                    zcount++;
               } 
          }

System.out.println("pole count: "+pcount);
System.out.println("zero count: "+zcount);
          pole = new PoleZero[pcount]; 
          zero = new PoleZero[zcount];
          int i=0; 
          while (data.size()>0){
                
               InteractComponent ic = (InteractComponent) data.firstElement();
               data.removeElementAt(0);
               PoleZero pz = new PoleZero(new Complex(ic.xv, ic.yv));
               if (ic.getDataSetNum() == 1) { //poles
                   pole[curp] = pz;
                   InteractComponent pair = ic.getPairIC(); 
                   if (pair != null){
                       data.removeElement((Object) pair);
                       PoleZero conjp = new PoleZero(new Complex(pair.xv, pair.yv));
                       curp++;
                       pole[curp] = conjp;
                       pz.conjugate = conjp;
                       conjp.conjugate = pz;
                       
                   } 
                   curp++;
               } else if (ic.getDataSetNum() == 2) { //zeros
                   zero[curz] = pz;
                   InteractComponent pair = ic.getPairIC(); 
                   if (pair != null){
                       data.removeElement((Object) pair);
                       PoleZero conjz = new PoleZero(new Complex(pair.xv, pair.yv));
                       curz++;
                       zero[curz] = conjz;
                       pz.conjugate = conjz;
                       conjz.conjugate = pz;
                   }
                   curz++;
               }
                
          }
          Vector sent = new Vector();
          sent.addElement(pole);
          sent.addElement(zero);
          jf.receive(1,"Update", sent);
     }    
}
