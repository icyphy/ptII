/* A transfer function view 

@Copyright (c) 1998 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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
package ptolemy.filter.view;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import ptolemy.math.Complex;
import ptolemy.filter.filtermodel.FilterObj;
import ptolemy.filter.controller.Manager;

//////////////////////////////////////////////////////////////////////////
//// TransFunctView 
/**
  A filter view that displays the transfer function.  This view simply has
  scroble panel that shows numerator, denominator and gain of the filter.
  User can save the these info by press the save button on top of the panel.
  <p> 
  @author William Wu (wbwu@eecs.berkeley.edu) 
  @version
 */
public class TransFunctView extends FilterView implements ActionListener {

    /**
     * Constructor.  The scrobble panel is created.  If the operation
     * mode is frame, then a view frame is also created.  Transfer
     * function data is then requested from filter.
     * <p>
     * @param filter observed filter object
     * @param mode operation mode
     * @param viewname name of thie view
     */
    public TransFunctView(FilterObj filter, int mode, String viewname){
          super(viewname, filter);
          _opMode = mode;

          // set the panel to place the plot
          _savebutton = new Button("   Save Transfer Function   ");
          _savebutton.addActionListener(this);
          _savebutton.setActionCommand("save");
          Panel buttonpanel = new Panel();
          buttonpanel.setLayout(new FlowLayout(5,5,5));
          buttonpanel.add(_savebutton); 
          _canvasPane = new TransPane();
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setSize(450, 150);
          scrollPane.add("Center", _canvasPane);
          _viewPanel = new Panel();
          _viewPanel.add("North", buttonpanel);
          _viewPanel.add("Center", scrollPane);
          _viewPanel.setSize(500,250);
  
          if (_opMode == Manager.FRAMEMODE){ // frame mode
              _frame = _createViewFrame(((FilterObj)filter).getName());
              _frame.add("Center", _viewPanel);
              _frame.setSize(500,250);
              _frame.setLocation(300,210);
              _frame.setVisible(true);
          }

          // get initial transfer function value
          _setViewTransferFunction();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void actionPerformed(ActionEvent evt){
        if (evt.getActionCommand().equals("save")){
            System.out.println("saving filter transfer function");
        } 
    }

    /**
     * To notify the view about the updated filter.  When
     * filter is modified, the filter calls <code> notifyObservers()
     * </code>, which calls each observer's <code> update() </code>
     * function.  This update will query filter for the new
     * numerator, denominator, and gan then pass them to the panel,
     * by calling <code> _setViewTransferFunction </code>.
     * <p>
     */
     public void update(Observable observed, Object arg){
          String command = (String)arg;
          if (command.equals("UpdatedFilter")){
              _setViewTransferFunction();
          }
     }

      
     //////////////////////////////////////////////////////////////////////////
     ////                     private methods                              ////

     private void _setViewTransferFunction(){
         int prec = 5;
         FilterObj jf = (FilterObj) _observed;
         Complex [] complexnum;
         Complex [] complexden;
         Complex complexgain;
         double [] realnum;
         double [] realden;
         double realgain;
         _realNumerator = null; 
         _realDenominator = null; 
         _realGain = 0.0; 
         _complexNumerator = null; 
         _complexDenominator = null; 
         _complexGain = null;
         int size;
 
         if (jf.getType() == ptolemy.math.filter.Filter.BLANK){
             complexnum = jf.getComplexNumerator();
             complexden = jf.getComplexDenominator();
             complexgain = jf.getComplexGain();
             _complexNumerator = new Complex[complexnum.length];
             _complexDenominator = new Complex[complexden.length];
             _complexGain = new Complex(_chop(complexgain.real, prec),
                                        _chop(complexgain.imag, prec));
             for (int i=0;i<complexnum.length;i++){
                  _complexNumerator[i]=new Complex(_chop(complexnum[i].real, prec),
                                       _chop(complexnum[i].imag, prec));
             }
             for (int i=0;i<complexden.length;i++){
                  _complexDenominator[i]=new Complex(_chop(complexden[i].real, prec),
                                       _chop(complexden[i].imag, prec));
             }
             size = Math.max(complexnum.length, complexden.length)*140+70; 
         } else {
             realnum = jf.getRealNumerator();
             realden = jf.getRealDenominator();
             realgain = jf.getRealGain();
             _realNumerator = new double[realnum.length];
             _realDenominator = new double[realden.length];
             _realGain = _chop(realgain, prec);

             for (int i=0;i<realnum.length;i++){
                  _realNumerator[i]=_chop(realnum[i], prec);
             }
             for (int i=0;i<realden.length;i++){
                  _realDenominator[i]=_chop(realden[i], prec);
             }
             size = Math.max(realnum.length, realden.length)*120+50; 
             
         }
         _canvasPane.setSize(size, 150);
         _canvasPane.repaint();
     }

     private double _chop(double number, int prec){
         String strValue = String.valueOf(number);
         String strValueChop;
 
         // chop the text of the value to the desired precision
         int pt = strValue.indexOf(".");
         if ((pt != -1) && ((strValue.length()-pt) > prec+1)){
             strValueChop = (strValue.substring(0,pt+(prec+1))).trim();
         } else {
             strValueChop = strValue;
         }
         
         return (Double.valueOf(strValueChop)).doubleValue(); 
     }
 
     ///////////////////////////////////////////////////////////////////
     ////                        private variables                  ////

     private double [] _realNumerator; 
     private double [] _realDenominator; 
     private double _realGain; 
     private Complex [] _complexNumerator; 
     private Complex [] _complexDenominator; 
     private Complex _complexGain;
     private TransPane _canvasPane; 
     private Button _savebutton;

     ///////////////////////////////////////////////////////////////////
     ////                        inner class                        ////
     class TransPane extends Canvas {

          public void paint(Graphics g){
              _drawTransferFunction(g); 
          }

          private void _drawTransferFunction(Graphics g){
            
              g.setColor(getBackground());
              int x = getSize().width;
              int y = getSize().height;
              g.fillRect(0,0,x,y); 
              g.setColor(getForeground()); 

              if (_realNumerator != null){
                  int cursor;
                  g.drawString(String.valueOf(_realGain), 10, 50);
                  cursor = 10 + String.valueOf(_realGain).length()*7+10; 
                  for (int i=0;i<_realNumerator.length;i++){
                       if ((_realNumerator[i]>0) && (i>0)){
                           g.drawString("+", cursor, 40);
                           cursor = cursor + 9;
                       } 
                       g.drawString(String.valueOf(_realNumerator[i]), cursor, 40);
                       cursor = cursor + String.valueOf(_realNumerator[i]).length()*8+5; 
                       if (i>0){
                           g.drawString("z", cursor, 40); 
                           cursor = cursor + 7;
                           g.drawString("-", cursor, 35); 
                           cursor = cursor + 10;
                           g.drawString(String.valueOf(i), cursor, 35); 
                           cursor = cursor + String.valueOf(i).length()*8+7; 
                       }
                  }    
                  cursor = 10 + String.valueOf(_realGain).length()*7+10; 
                  g.drawLine(cursor, 50, cursor+_realDenominator.length*120, 50);
                  for (int i=0;i<_realDenominator.length;i++){
                       if ((_realDenominator[i]>0) && (i>0)){
                           g.drawString("+", cursor, 70);
                           cursor = cursor + 9;
                       } 
                       g.drawString(String.valueOf(_realDenominator[i]), cursor, 70);
                       cursor = cursor + String.valueOf(_realDenominator[i]).length()*7+5; 
                       if (i>0){
                           g.drawString("z", cursor, 70); 
                           cursor = cursor + 7;
                           g.drawString("-", cursor, 65); 
                           cursor = cursor + 10;
                           g.drawString(String.valueOf(i), cursor, 65); 
                           cursor = cursor + String.valueOf(i).length()*8+7; 
                       }   
                  } 
              } else {

              }
            //  g.setFont(saved);
          }

          // Font _transferFont = new Font("Serif", Font.ITALIC, 12);
     }
 
}
