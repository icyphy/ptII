/* The main objects of the filter. 
 
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
 
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;


// Dialog ask user to enter the type and name of the filter

class FiltTypeSet extends Dialog {

    public FiltTypeSet(Frame parent, TMain p, String title){
        super(parent, title, false);
        this.setLayout(new BorderLayout(5, 5));
        _panel = new FiltTypeSetup(p); 
        this.add("Center", _panel);
        this.pack();
        this.show();
    }

    /** 
     * Dispose the dialog.  Called by Tmain.
     */
    public void kil(){
       this.setVisible(false);
       this.dispose();
    }

    FiltTypeSetup _panel;
}

//////////////////////////////////////////////////////////////////////////
//// TMain 
/**
 * Main object for the filter program.  It handles the creation of manager,
 * initial user input on design the filter, save filter to files, and load
 * filter from files.  It has a frame user interface, with menu items to 
 * create, delete, load, save filter, also add/delete views.
 * Maybe this object can be merged with the manager.
 *
 * author: William Wu
 * version:
 * date: 3/2/98
 */ 
 

public class TMain extends Frame implements ActionListener {

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
  
    /** 
     * Constructor.  This setup the menu, and message window.
     * It also creates the manager.
     */
   
    public TMain(String title){
      super(title);

 
      _menubar = new MenuBar();
      this.setMenuBar(_menubar);
      _file = new Menu("File");

      // New Blank create a empty sheet of pole-zero plot, so the user
      // can add poles-zeros as they wish.
      MenuItem newb = new MenuItem("New Blank");
      _file.add(newb);
      MenuItem newf = new MenuItem("New");
      _file.add(newf);
      MenuItem load = new MenuItem("Load");
      _file.add(load);
      MenuItem dele = new MenuItem("Delete");
      _file.add(dele);
      MenuItem sav = new MenuItem("Save");
      _file.add(sav);
      MenuItem quit = new MenuItem("Quit");
      _file.add(quit);

      _file.addActionListener(this);
      newb.setActionCommand("New Blank");
      newf.setActionCommand("New");
      load.setActionCommand("Load");
      dele.setActionCommand("Delete");
      sav.setActionCommand("Save");
      quit.setActionCommand("Quit");

      _edit = new Menu("View");
      MenuItem apz = new MenuItem("Add Pole-Zero Plot");
      _edit.add(apz);
      MenuItem afreq = new MenuItem("Add Frequency Response");
      _edit.add(afreq);
      MenuItem aimpul = new MenuItem("Add Impulse Response");
      _edit.add(aimpul);
      MenuItem dpz = new MenuItem("Delete Pole-Zero Plot");
      _edit.add(dpz);
      MenuItem dfreq = new MenuItem("Delete Frequency Response");
      _edit.add(dfreq);
      MenuItem dimpul = new MenuItem("Delete Impulse Response");
      _edit.add(dimpul);

      _edit.addActionListener(this);
      apz.setActionCommand("Add Pole-Zero Plot");
      dpz.setActionCommand("Delete Pole-Zero Plot");
      afreq.setActionCommand("Add Frequency Response");
      dfreq.setActionCommand("Delete Frequency Response");
      aimpul.setActionCommand("Add Impulse Response");
      dimpul.setActionCommand("Delete Impulse Response");

      _menubar.add(_file);
      _menubar.add(_edit);
      this.setLayout(new BorderLayout(15, 15));
      _message = new TextArea(30, 20);
      this.add("Center", _message);  
      _curDir = null;
      _curFile = null;
      _file_dia_s = new FileDialog(this, "Save Filter", FileDialog.SAVE);
      _file_dia_l = new FileDialog(this, "Load Filter", FileDialog.LOAD);
      _man = new Manager(Manager.FrameMode);
      _newline = System.getProperty("line.separator");
      this.setSize(new Dimension(200,200)); 
      this.show();
   }

   /**
    * Handle different selection on the menu items.
    */
   public void actionPerformed(ActionEvent evt){ 

       String command = evt.getActionCommand(); 
       if (command.equals("New")) {

           // delete the filter first
           _man.deletefilter();
           // _filtset = new Filtset(this, this, "Setup New Filter"); 
           _filttypeset = new FiltTypeSet(this, this, "Set Filter Type");


       } else if (command.equals("New Blank")) {

           _man.createFilterbyPolesZeros("Blank", 1, new Vector(), new Vector(), new Vector());

       } else if (command.equals("Save")) {

           if (_curDir!=null) _file_dia_s.setDirectory(_curDir);
           if (_curFile!=null) _file_dia_s.setFile(_curFile);
           _file_dia_s.pack();      
           _file_dia_s.show(); 
           _curDir = _file_dia_s.getDirectory();
           _curFile = _file_dia_s.getFile();
           if ((_curFile == null) || (_curFile.equals(""))) return;
           saveFilter();      

       } else if (command.equals("Load")) {

           if (_curDir!=null) _file_dia_l.setDirectory(_curDir);
           if (_curFile!=null) _file_dia_l.setFile(_curFile);
           _file_dia_l.pack();      
           _file_dia_l.show(); 
           _curDir = _file_dia_l.getDirectory();
System.out.println(_curDir);
           _curFile = _file_dia_l.getFile();
           if ((_curFile == null) ||(_curFile.equals(""))) return;
           loadFilter(_curFile);      

       } else if (command.equals("Delete")) {

           _man.deletefilter();

       } else if (command.equals("Quit")) {

           System.exit(0);

       } else if (command.equals("Add Pole-Zero Plot")) {

           _man.addpolezeroview();

       } else if (command.equals("Add Frequency Response")){

           _man.addfreqview();

       } else if (command.equals("Add Impulse Response")){

           _man.addimpulview();

       } else if (command.equals("Show Filter Parameter Options")){
           _man.addfiltparamview();

       } else if (command.equals("Delete Pole-Zero Plot")) {

           _man.removePoleZeroView();

       } else if (command.equals("Delete Frequency Response")){

           _man.removeFreqView();

       } else if (command.equals("Delete Impulse Response")){

           _man.removeImpulsView();

       } else if (command.equals("Hide Filter Parameter Options")){
           _man.removefiltparamview();

       }

   }


   public void processEvent(AWTEvent event) {
        if (event.getID() == Event.WINDOW_DESTROY) {
             System.exit(0);
        }   
        super.processEvent(event);
   }

   /**
    * Load filter from the file.
    */
   public void loadFilter(String filename){

       File fileT = new File(_curDir, filename);
       BufferedReader fin = null;
       String line;
       String name = new String("wow");
       String type;
       String entry;
       int ty = 1;
       Vector v1 = new Vector(); 
       Vector v2 = new Vector(); 
       Vector v3 = new Vector(); 
       Vector v4 = new Vector(); 
       if (fileT.exists()) {
            try {
               fin = new BufferedReader(new FileReader(fileT)); 
            } catch (FileNotFoundException e) {
                return;
            }
 
            while (true) {
                
                try { 
                    line = fin.readLine();
                } catch (IOException e) {
                    break;
                }

                if (line == null) break;
System.out.println("reading in a line");
                String lcline = new String(line.toLowerCase());
                if (lcline.startsWith("name: ")){
System.out.println("reading in name");
                    name = (line.substring(6)).trim();
                } else if (lcline.startsWith("type: ")){ 
System.out.println("reading in type");
                    type = (line.substring(6)).trim();
                    if (type.equals("IIR")) ty = 1;
                    else if (type.equals("FIR")) ty = 2;
                    else ty = 0;
                } else if (lcline.startsWith("pole: ")){
System.out.println("reading in Pole"+lcline);
                    int comma = line.indexOf(",", 6);
                    if (comma != -1) {
                         String real = (line.substring(6,comma)).trim(); 
                         String imag = (line.substring(comma+1)).trim(); 
                         Double reald = new Double(real);
                         Double imagd = new Double(imag);
                         v1.addElement(reald); 
                         v2.addElement(imagd); 
                         v3.addElement(new Boolean(true)); 
                    }
                } else if (lcline.startsWith("zero: ")){
System.out.println("reading in Zero" + lcline);
                    int comma = line.indexOf(",", 6);
System.out.println("first comma "+comma);

                    if (comma != -1) {
System.out.println("second comma "+comma);
                         String real = (line.substring(6,comma)).trim(); 
                         String imag = (line.substring(comma+1)).trim(); 
                         Double reald = new Double(real);
                         Double imagd = new Double(imag);
                         v1.addElement(reald); 
                         v2.addElement(imagd); 
                         v3.addElement(new Boolean(false)); 
                    }
                } else {
                  // bad grammer
                }
            
            }
            _man.createFilterbyPolesZeros(name, ty, v1, v2, v3);
       }            
   }

   /**
    * Save filter.  Depends on the saving mode, if it is in mode 0
    * it will save the poles and zeros, if in mode 1, it is ptolemy IIR mode,
    * so it will save three files: gain, numerator, denominator.  So each
    * can be used in the parameter entry widget as "< filename" of a IIR star.
    */  
   public void saveFilter(){
      
       File fileT; 
       PrintWriter ptstream;
       Vector v1 = new Vector();
       Vector v2 = new Vector();
       Vector v3 = new Vector();
       Vector v4 = new Vector();
       int type = -1;
       String name = new String("Wow");
       String buffer;
       double gain;
       double [] num;
       double [] denom;

     if (_smode == 0){ // regular save
       fileT = new File(_curDir, _curFile);

       if (fileT.exists()) {
            _message.append("Warning: File "+_curFile+" exists, overwriting it now!"+ _newline ); 
       }

       try {
           ptstream = new PrintWriter(new FileOutputStream(fileT), true);
       } catch (IOException e) {
           return;
       }


       type = _man.getfilter(v1,v2,v3,name);     
       if (type >= 0 ){
             ptstream.println("Name: "+name);
             if (type == 1) ptstream.println("Type: "+"IIR");
             if (type == 2) ptstream.println("Type: "+"FIR");
             for (int i = 0 ; i<v1.size(); i++){
                  double x = ((Double) v1.elementAt(i)).doubleValue();
                  double y = ((Double) v2.elementAt(i)).doubleValue();
                  boolean p = ((Boolean) v3.elementAt(i)).booleanValue();
                  if (p == true){
                        ptstream.println("Pole: "+x+","+y); 
                  }
                  if (p == false) {
                        ptstream.println("Zero: "+x+","+y); 
                  }
             }
       }
       ptstream.close();

     } else if (_smode == 1){ // ptolemy save
       String gfilename = new String(_curFile+".gain");
       String nfilename = new String(_curFile+".numer");
       String dfilename = new String(_curFile+".denom");
              
       // save gain
       fileT = new File(_curDir, gfilename);
       if (fileT.exists()) {
            _message.append("Warning: File "+gfilename+" exists, overwriting it now!"+_newline); 
       }
     
       try {
           ptstream = new PrintWriter(new FileOutputStream(fileT), true);
       } catch (IOException e) {
           return;
       }
       gain = _man.getfilterGain();
       ptstream.println(gain); 
       ptstream.close(); 

       // save numerator 
       fileT = new File(_curDir, nfilename);
       if (fileT.exists()) {
            _message.append("Warning: File "+nfilename+" exists, overwriting it now!"+_newline); 
       }
       try {
           ptstream = new PrintWriter(new FileOutputStream(fileT), true);
       } catch (IOException e) {
           return;
       }
       num = _man.getfilterNumerator();
       for (int i=0;i<num.length;i++){
           ptstream.print(num[i]+" ");
       }
       ptstream.close(); 

       // save denominator 
       fileT = new File(_curDir, dfilename);
       if (fileT.exists()) {
            _message.append("Warning: File "+dfilename+" exists, overwriting it now!"+_newline); 
       }
       try {
           ptstream = new PrintWriter(new FileOutputStream(fileT), true);
       } catch (IOException e) {
           return;
       }
       denom = _man.getfilterDenominator();
       for (int i=0;i<denom.length;i++){
           ptstream.print(denom[i]+" ");
       }
       ptstream.close(); 
     }
   }

   public void newFilter(int type, String name){
        _filttypeset.kil();
        _man.newFilter(type, name);
   }

   /**
    * Main function.  With an argument of "ptolemyIIR", the ptolemy save mode
    * is turned on.
    */
   public static void main(String[] args){
        if (args.length > 0){
            if (args[0].equals("ptolemyIIR")){
                _smode = 1;
            } else {
                _smode = 0;
            }
        } 
        TMain tm = new TMain("J-Filter");
   }


   //////////////////////////////////////////////////////////////////////////
   ////                         private variables                        ////

   private static int _smode; // mode of saving, 0 - regular,
                              // 1 - ptolemy IIR style, saves
                              // in three files: gain, denom, numer
   private int _m, _t, _f;
   private double _fs;
   private String _name;
   private FiltTypeSet _filttypeset;
   private MenuBar _menubar;
   private Menu _edit;
   private Menu _file;
   private TextArea _message;
   private Manager _man;
   private FileDialog _file_dia_s;
   private FileDialog _file_dia_l;
   private String _curDir;
   private String _curFile;
   private String _newline;
}
