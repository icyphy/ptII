/* Filter design application 
 
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
 
package ptolemy.filter.controller;
 
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.io.*;
import ptolemy.math.Complex;
import ptolemy.math.filter.Filter;
import ptolemy.filter.view.FilterView;

//////////////////////////////////////////////////////////////////////////
//// FilterApplication

/**
 Filter design application class.  User can invoke this class when
 the filter package is used as a stand-alone application.
 This object creates manager, asks for initial user input on design 
 a new filter, and user action to delete the current filter. 
 filter from files.  It has a frame user interface, with menu items to 
 create, delete filter, also show/hide view controller.
 <p>

 @author: William Wu
 @version: %W%	%G%
 @date: 10/29/98
 */ 
 

public class FilterApplication extends Frame implements ActionListener {

    /** 
     * Constructor.  This setups the menu, and message window.
     * It also creates the manager.
     */
   
    public FilterApplication(String title){
 
        super(title);
 
        MenuBar menubar = new MenuBar();
        this.setMenuBar(menubar);
        Menu file = new Menu("File");

        MenuItem newf = new MenuItem("New");
        file.add(newf);
        MenuItem dele = new MenuItem("Delete");
        file.add(dele);
        MenuItem quit = new MenuItem("Quit");
        file.add(quit);

        file.addActionListener(this);
        newf.setActionCommand("New");
        dele.setActionCommand("Delete");
        quit.setActionCommand("Quit");

        Menu edit = new Menu("View");
        MenuItem viewcontroller = new MenuItem("Show/Hide View Controller");
        edit.add(viewcontroller);

        edit.addActionListener(this);
        viewcontroller.setActionCommand("Show/Hide view controller");

        menubar.add(file);
        menubar.add(edit);
        this.setLayout(new BorderLayout(15, 15));
        _message = new TextArea(30, 20);
        this.add("Center", _message);  

        // run Manager in Frame mode
        _man = new Manager(FilterView.FRAMEMODE);
        _newline = System.getProperty("line.separator");
        this.setSize(new Dimension(200,200)); 
        this.setLocation(300, 300);
        this.setVisible(true);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    /**
     * Handle different selection on the menu items.
     * @param evt action event created by user. 
     */
    public void actionPerformed(ActionEvent evt){ 

        String command = evt.getActionCommand(); 
        if (command.equals("New")) {

            // delete the filter first
            _man.deletefilter();
            // _filtset = new Filtset(this, this, "Setup New Filter"); 
            if (_filttypeset == null) _filttypeset = new FiltTypeSet(this, "Set Filter Type");
            _filttypeset.setVisible(true);

        } else if (command.equals("Delete")) {

            _man.deletefilter();

        } else if (command.equals("Quit")) {

            System.exit(0);

        } else if (command.equals("Show/Hide view controller")) {

            _man.toggleViewControllerVisibility();

        }

    }

    /**
     * Process the kill window event.
     * @param event kill window event.
     */
    public void processEvent(AWTEvent event) {
        if (event.getID() == Event.WINDOW_DESTROY) {
            System.exit(0);
        }   
        super.processEvent(event);
    }


    /**
     * Call manager to create a new filter.  Called by filter type setup 
     * dialog.
     *
     * @param type type of filter (IIR, FIR, etc)
     * @param name name of filter 
     */ 
    public void newFilter(int type, String name){
        _man.newFilter(type, name);
    }

    /**
     * Main function.  
     */
    public static void main(String[] args){
        FilterApplication tm = new FilterApplication("J-Filter");
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private FiltTypeSet _filttypeset;
    private TextArea _message;
    private Manager _man;
    private String _newline;


    //////////////////////////////////////////////////////////////////////////
    ////                         inner class                              ////

    // Dialog ask user to enter the type and name of the filter when
    // the filter is created.
    class FiltTypeSet extends Dialog implements ActionListener {

        public FiltTypeSet(Frame parent, String title){
            super(parent, title, false);
            this.setLayout(new BorderLayout(5, 5));

            // filter type 
            _ftype = new CheckboxGroup();
            _iir = new Checkbox("IIR", _ftype, true);
            _firWin = new Checkbox("FIR Windowed", _ftype, false);
            _firMP = new Checkbox("FIR M & P", _ftype, false);
            _firFS = new Checkbox("FIR Frequency Sampling", _ftype, false);
            _blank = new Checkbox("Blank", _ftype, false);
   
            // type and name 
   
            _p0 = new Panel();
            _p0.setLayout(new FlowLayout(5,5,5)); 
            _p0.add(new Label("Enter the name"));
            _name = new TextField(15); 
            _p0.add(_name);

            _p1 = new Panel();
            _p1.setLayout(new BorderLayout(5, 5));
            _p1.add("North", new Label("Select Filter Type"));
            _p1.add("Center", _blank);
            _p1.add("South", _iir);

            _p2 = new Panel();
            _p2.setLayout(new BorderLayout(5, 5));
            _p1.add("North", _firWin);
            _p2.add("Center", _firMP);
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
            this.pack();
            this.setLocation(400, 350);
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
                    ft = Filter.IIR;
                } else if (_firWin.getState() == true){
                    ft = Filter.IIR;
                    // ft = Filter.FIRWIN;
                } else if (_firMP.getState() == true){
                    ft = Filter.IIR;
                    // ft = Filter.FIROPT;
                } else if (_firFS.getState() == true){
                    ft = Filter.IIR;;
                    // ft = DigitalFilter.FIRFS;
                } else if (_blank.getState() == true){
                    ft = Filter.IIR;
                } else ft = 0;

                name = _name.getText();
                setVisible(false);
                newFilter(ft, name);
            }  
         
        }

        // private variables  
        private Panel _p0, _p1, _p2, _p3;

        private TextField _name;
        private CheckboxGroup _ftype;
        private Checkbox _iir, _firWin, _firMP, _firFS, _blank;
        private Button _ok;
    }

}
