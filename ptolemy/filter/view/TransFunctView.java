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

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import ptolemy.math.Complex;
import ptolemy.filter.filtermodel.FilterObj;

//////////////////////////////////////////////////////////////////////////
//// TransFunctView 
/**
  A filter view that displays the transfer function.  This view simply has
  scroble panel that shows numerator, denominator and gain of the filter.
  User can save the these info by press the save button on top of the panel.
  <p> 
  @author William Wu (wbwu@eecs.berkeley.edu) 
  @version: $id$ 
 */
public class TransFunctView extends FilterView implements ActionListener, ItemListener {

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

          _precision = new Choice();
          _precision.addItem("0");
          _precision.addItem("1");
          _precision.addItem("2");
          _precision.addItem("3");
          _precision.addItem("4");
          _precision.addItem("5");
          _precision.addItem("6");
          _precision.addItem("7");
          _precision.select("5");
          _precision.addItemListener(this);

          Panel buttonpanel = new Panel();
          buttonpanel.setLayout(new FlowLayout(5,5,5));
          buttonpanel.add(_savebutton); 
          buttonpanel.add(new Label("Number of Precision"));
          buttonpanel.add(_precision); 

          _canvasPane = new TransPane();
          ScrollPane scrollPane = new ScrollPane();
          scrollPane.setSize(450, 150);
          scrollPane.add("Center", _canvasPane);
          _viewPanel = new Panel();
          _viewPanel.add("North", buttonpanel);
          _viewPanel.add("Center", scrollPane);
          _viewPanel.setSize(500,250);
  
          if (_opMode == FilterView.FRAMEMODE){ // frame mode
              String name = new String("");
              if (filter != null) name = filter.getName();
              _frame = _createViewFrame(name);
              _frame.add("Center", _viewPanel);
              _frame.setSize(500,250);
              _frame.setLocation(300,210);
              _frame.setVisible(true);
              _saveFileDialog = new FileDialog(_frame, "Save Transfer Function",
                                               FileDialog.SAVE);
          }

          // get initial transfer function value
          if (filter!= null){
              Complex [] complexnum = null;
              Complex [] complexden = null;  
              double [] realnum = null;
              double [] realden = null;  
              Complex complexgain = null;  
              double realgain = 0.0;
              if (filter.getType()==ptolemy.math.filter.Filter.BLANK){
                  complexnum = filter.getComplexNumerator();
                  complexden = filter.getComplexDenominator();
                  complexgain = filter.getComplexGain();
              } else{
                  realnum = filter.getRealNumerator();
                  realden = filter.getRealDenominator();
                  realgain = filter.getRealGain();
              }
              _setViewTransferFunction(complexnum, realnum, complexden, realden, complexgain, realgain);
          }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public void actionPerformed(ActionEvent evt){
        if (evt.getActionCommand().equals("save")){

            if (_opMode != FilterView.FRAMEMODE) return;

            if (_curDir!=null) _saveFileDialog.setDirectory(_curDir);
            if (_curFile!=null) _saveFileDialog.setFile(_curFile);
            _saveFileDialog.setVisible(true); 
            _curDir = _saveFileDialog.getDirectory();
            _curFile = _saveFileDialog.getFile();
            if ((_curFile == null) || (_curFile.equals(""))) return;
            _saveTransFunct();      

            System.out.println("saving filter transfer function");
        } 
    }


    public void itemStateChanged(ItemEvent evt){
        String precision = _precision.getSelectedItem();
        setPrec(Integer.valueOf(precision).intValue());
    }

    public void setPrec(int prec){
        _prec = prec;
        if (_observed == null) return;
        FilterObj jf = (FilterObj) _observed;
        Complex [] complexnum = null;
        Complex [] complexden = null;  
        double [] realnum = null;
        double [] realden = null;  
        Complex complexgain = null;  
        double realgain = 0.0;
        if (jf.getType()==ptolemy.math.filter.Filter.BLANK){
            complexnum = jf.getComplexNumerator();
            complexden = jf.getComplexDenominator();
            complexgain = jf.getComplexGain();
        } else{
            realnum = jf.getRealNumerator();
            realden = jf.getRealDenominator();
            realgain = jf.getRealGain();
        }
        _setViewTransferFunction(complexnum, realnum, complexden, realden, complexgain, realgain);
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
              FilterObj jf = (FilterObj) _observed;
              Complex [] complexnum = null;
              Complex [] complexden = null;  
              double [] realnum = null;
              double [] realden = null;  
              Complex complexgain = null;  
              double realgain = 0.0;
              if (jf.getType()==ptolemy.math.filter.Filter.BLANK){
                  complexnum = jf.getComplexNumerator();
                  complexden = jf.getComplexDenominator();
                  complexgain = jf.getComplexGain();
              } else{
                  realnum = jf.getRealNumerator();
                  realden = jf.getRealDenominator();
                  realgain = jf.getRealGain();
              }
              _setViewTransferFunction(complexnum, realnum, complexden, realden, complexgain, realgain);
          }
     }

      
     //////////////////////////////////////////////////////////////////////////
     ////                     protected methods                            ////

     /**
      * Set the numerator, denominator and gain on the panel.  Since the transfer
      * could be complex, both real case and complex case inputs are given.  If
      * the filter is real, the complex inputs should be null, and vise versa.
      *
      * @param complexnum numerator in complex
      * @param realnum numerator in real
      * @param complexden denominator in complex
      * @param realden denominator in real
      * @param complexgain gain in complex
      * @param realgain gain in real
      */
     protected void _setViewTransferFunction(Complex [] complexnum, double [] realnum,
                                           Complex [] complexden, double [] realden,
                                           Complex complexgain, double realgain){
         FilterObj jf = (FilterObj) _observed;
         _realNumerator = null; 
         _realDenominator = null; 
         _realGain = 0.0; 
         _complexNumerator = null; 
         _complexDenominator = null; 
         _complexGain = null;
         int size;

         if (complexgain != null){ 
             _complexNumerator = new Complex[complexnum.length];
             _complexDenominator = new Complex[complexden.length];
             _complexGain = new Complex(_chop(complexgain.real, _prec),
                                        _chop(complexgain.imag, _prec));
             for (int i=0;i<complexnum.length;i++){
                  _complexNumerator[i]=new Complex(_chop(complexnum[i].real, _prec),
                                       _chop(complexnum[i].imag, _prec));
             }
             for (int i=0;i<complexden.length;i++){
                  _complexDenominator[i]=new Complex(_chop(complexden[i].real, _prec),
                                       _chop(complexden[i].imag, _prec));
             }
             size = Math.max(complexnum.length, complexden.length)*140+70; 
         } else {
             _realNumerator = new double[realnum.length];
             _realDenominator = new double[realden.length];
             _realGain = _chop(realgain, _prec);

             for (int i=0;i<realnum.length;i++){
                  _realNumerator[i]= _chop(realnum[i], _prec);
             }
             for (int i=0;i<realden.length;i++){
                  _realDenominator[i]= _chop(realden[i], _prec);
             }
             size = Math.max(realnum.length, realden.length)*120+50; 
             
         }
         _canvasPane.setSize(size, 150);
         _canvasPane.repaint();
     }

     //////////////////////////////////////////////////////////////////////////
     ////                     private methods                              ////

     private double _chop(double number, int prec){
         String strValue = String.valueOf(number);
         String strValueChop;
 
         // chop the text of the value to the desired precision
         int pt = strValue.indexOf(".");
         int E = strValue.indexOf("E");
         if ((pt != -1) && ((strValue.length()-pt) > prec+1)){
             strValueChop = (strValue.substring(0,pt+(prec+1))).trim();
             if (E != -1) {
                 strValueChop = strValueChop + strValue.substring(E);
             }
         } else {
             strValueChop = strValue;
         }
         return (Double.valueOf(strValueChop)).doubleValue(); 
     }

     private void _saveTransFunct(){

         File saveFile = new File(_curDir, _curFile);
         PrintWriter ptstream;

         if (saveFile.exists()) {
             System.out.println("Warning: File "+_curFile+
                                " exists, overwriting it now!");
                 
         }
 
         try {
             ptstream = new PrintWriter(new FileOutputStream(saveFile), true);
         } catch (IOException e) {
             return;
         }
   
         if (_realNumerator != null){ 
             ptstream.println(_realGain);
             for (int i=0;i<_realNumerator.length;i++){
                  ptstream.print(_realNumerator[i]+" ");
             } 
             ptstream.println(" ");
             for (int i=0;i<_realDenominator.length;i++){
                  ptstream.print(_realDenominator[i]+" ");
             } 
             ptstream.println(" ");
         }
         ptstream.close();   
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
     private FileDialog _saveFileDialog;
     private String _curDir;
     private String _curFile;
     private Choice _precision;  
     protected int _prec = 5;

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

