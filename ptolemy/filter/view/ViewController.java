/* Filter views controller

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

package ptolemy.filter.view;

import ptolemy.filter.controller.Manager;
import java.awt.*;
import java.awt.event.*;

/////////////////////////////////////////////////////////////////////////
//// ViewController
/**
 A window that hide/show the views of the current filter design.
 It is constructed by Manager, and references of view are passed in.
 User use this window to show/hide the views, like PoleZeroView,
 FreqResponseView, ImpulseResponseView, etc.
 <p>
 When a new filter is created, this class is recreated, since there
 could be different view for different type of filter.
 <p>
@author  William Wu (wbwu@eecs.berkeley.edu)
@version %W%    %G%
*/

public class ViewController extends Frame implements ActionListener {

     public ViewController(String name, String [] viewers, Manager man){
          super("Show / Hide Filter Views for Filter: "+name);
          Button ok, apply, cancel;

          _manager = man;
          _viewsname = viewers;
          Panel checkboxpanel = new Panel();
          checkboxpanel.setLayout(new FlowLayout(5,5,5));
          if (viewers != null){
              _views = new CheckboxGroup[_viewsname.length];
              _hideshow = new Checkbox[_viewsname.length][];
              for (int i=0;i<_viewsname.length;i++){
                   _views[i] = new CheckboxGroup();
                   _hideshow[i] = new Checkbox[2];
                   _hideshow[i][0] = new Checkbox("show", _views[i], true);
                   _hideshow[i][1] = new Checkbox("hide", _views[i], false);
                   checkboxpanel.add(new Label("Show/Hide "+ _viewsname[i]));
                   checkboxpanel.add(_hideshow[i][0]);
                   checkboxpanel.add(_hideshow[i][1]);
              }
          }

          ok = new Button("  OK  ");
          ok.addActionListener(this);
          ok.setActionCommand("ok");
          apply = new Button("  Apply  ");
          apply.addActionListener(this);
          apply.setActionCommand("apply");
          cancel = new Button("  Cancel  ");
          cancel.addActionListener(this);
          cancel.setActionCommand("cancel");

          Panel buttonpanel = new Panel();
          buttonpanel.setLayout(new FlowLayout(5,5,5));
          buttonpanel.add(ok);
          buttonpanel.add(apply);
          buttonpanel.add(cancel);

          this.add("Center", checkboxpanel);
          this.add("South", buttonpanel);
          this.setSize(350, 150);
     }


    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    /**
     * Handle user input on what view to show or hide.  It calls Manager
     * for toggling the views.
     * @param evt action event created by user.
     */
    public void actionPerformed(ActionEvent evt){
         if (evt.getActionCommand().equals("ok")){
             for (int i=0;i<_views.length;i++){
                 _manager.toggleView(_viewsname[i], _hideshow[i][0].getState());
             }
             this.setVisible(false);
         } else if (evt.getActionCommand().equals("apply")){
             for (int i=0;i<_views.length;i++){
                 _manager.toggleView(_viewsname[i], _hideshow[i][0].getState());
             }
         } else if (evt.getActionCommand().equals("cancel")){
             this.setVisible(false);
         }
    }

    public void setViewVisible(String name, boolean visible){

         for (int i=0;i<_views.length;i++){
System.out.println("view name: "+name +"  stored name "+ _viewsname[i]);
              if (_viewsname[i].equals(name)){
System.out.println("found same name");
                  if (visible){
                      _hideshow[i][0].setState(true);
                      _hideshow[i][1].setState(false);
                  } else {
                      _hideshow[i][0].setState(false);
                      _hideshow[i][1].setState(true);
                  }
              }
         }
         repaint();
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////
    private Manager _manager;
    private Checkbox [][] _hideshow;
    private CheckboxGroup [] _views;
    private String [] _viewsname;
}
