/* Internel manager of filter package.

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

*/

package ptolemy.filter.controller;

import java.util.*;
import ptolemy.math.Complex;
import ptolemy.math.filter.Filter;
import ptolemy.filter.filtermodel.*;
import ptolemy.filter.view.*;

//////////////////////////////////////////////////////////////////////////
//// Manager
/**
  The manager handle all the creation the filter object
  and the views.  It also handles the user input from the FilterApplication
  and passes them to the filter object.  It can also hide/show views.

  @author: William Wu (wbwu@eecs.berkeley.edu)
  @version: %W%   %G%
  @date: 3/2/98

 */

public class Manager {


    /**
     * Constructor.  _opMode is set for specify which mode the ptfilter is in
     * either as stand alone application or applet on the web, see FilterView.
     * @param mode mode of operation, either Frame mode, or Applet mode.
     */
    public Manager(int mode){
        _opMode = mode;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /**
     * Create a new filter with user specification.  It will delete
     * the previous filter if it exists.  New views will be created for
     * the filter.  Different types of filter will have different views.
     * A view controller will also be created.
     *
     * @param type filter's type.
     * @param name filter's name.
     */
    public void newFilter(int type, String name){

        if (_fobj!= null){
            deletefilter();
        }
        _fobj = new FilterObj();
        _fobj.init(name, type);
        if (type == Filter.IIR){ // IIR
            if (_opMode == FilterView.FRAMEMODE){ // frame mode, it is ok to start
                // filter design now since all the
                // graphics is taken care internally
                _fobj.setIIRParameter(Filter.BUTTERWORTH, Filter.BILINEAR,
                        Filter.LOWPASS, 1.0);
            }
            _addView("IIRFilterParameterView");
        }

        _addView("PoleZeroView");
        _addView("FreqView");
        _addView("ImpulseView");
        _addView("TransferFunctionView");

        // setup view controller
        Enumeration viewkey = _views.keys();
        String [] viewnames = new String[_views.size()];
        int i=0;
        while (viewkey.hasMoreElements()){
            viewnames[i++] = new String((String) viewkey.nextElement());
        }
        _viewcontroller = new ViewController(name, viewnames, this);

        // send the view controller reference to the view
        viewkey = _views.keys();
        i = 0;
        while (viewkey.hasMoreElements()){
            FilterView view = (FilterView) _views.get(viewkey.nextElement());
            view.setViewController(_viewcontroller);
        }
    }


    /**
     * Get view by the given name.  If the name doesn't match,
     * null is returned.
     *
     * @param name name of the view to obtain.
     */
    public FilterView getView(String name){
        Enumeration viewkeys = _views.keys();
        FilterView view = null;
        while (viewkeys.hasMoreElements()){
            String viewname = (String) viewkeys.nextElement();
            if (viewname.equals(name)){
                view = (FilterView) _views.get(viewname);
                break;
            }
        }
        return view;
    }

    public FilterObj getFilterObject(){
        return _fobj;
    }


    /**
     * Delete the filter object.  It check if the view objects
     * exists or not, delete them if they do exist.
     */
    public void deletefilter() {
        if (_fobj != null){
            Enumeration viewkey = _views.keys();
            while (viewkey.hasMoreElements()){
                String viewname = (String) viewkey.nextElement();
                FilterView deleteview = (FilterView) _views.get(viewname);
                deleteview.setVisible(false);  // hide view first
                _fobj.deleteObserver((Observer) deleteview);
                _views.remove(viewname);
            }
            _fobj = null;
            _viewcontroller.setVisible(false);
            _viewcontroller = null;
        }
    }

    public void toggleViewControllerVisibility(){
        if (_viewcontroller != null){
            if (_viewcontroller.isVisible()){
                _viewcontroller.setVisible(false);
            } else {
                _viewcontroller.setVisible(true);
            }
        }
    }

    public void toggleView(String toggleviewname, boolean show){
        Enumeration viewkeys = _views.keys();
        FilterView view = null;
        while (viewkeys.hasMoreElements()){
            String viewname = (String) viewkeys.nextElement();
            if (viewname.equals(toggleviewname)){
                view = (FilterView) _views.get(viewname);
                view.setVisible(show);
                break;
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public variables                        ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                         ////

    //
    // Add a view according to the given name.
    // It calls filter object's <code> addObserver() </code>
    // to add the observer to the observer list.
    //
    private void _addView(String name){
        if (name.equals("PoleZeroView")){
            PoleZeroView pzv = new PoleZeroView(_fobj, _opMode,
                    new String("PoleZeroView"));
            _fobj.addObserver(pzv);
            _views.put(new String(name), pzv);
        } else if (name.equals("FreqView")){
            FreqView fv = new FreqView(_fobj, _opMode, new String("FreqView"));
            _fobj.addObserver(fv);
            _views.put(new String(name), fv);
        } else if (name.equals("ImpulseView")){
            ImpulseView iv = new ImpulseView(_fobj, _opMode,
                    new String("ImpulseView"));
            _fobj.addObserver(iv);
            _views.put(new String(name), iv);
        } else if (name.equals("IIRFilterParameterView")){
            IIRFiltSetView iirv = new IIRFiltSetView(_fobj, _opMode,
                    new String("IIRFilterParameterView"));
            _fobj.addObserver(iirv);
            _views.put(new String(name), iirv);
        } else if (name.equals("TransferFunctionView")){
            TransFunctView tfv = new TransFunctView(_fobj, _opMode,
                    new String("TransferFunctionView"));
            _fobj.addObserver(tfv);
            _views.put(new String(name), tfv);
        }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private int _opMode;
    private Hashtable _views = new Hashtable();
    private FilterObj _fobj;
    private ViewController _viewcontroller;
}
