/* One line description of file.

 Copyright (c) 1997 The Regents of the University of California.
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

package pt.actors;
import pt.kernel.*;
import collections.LinkedList;
import java.util.Enumeration;
//import java.util.*;


//////////////////////////////////////////////////////////////////////////
//// Director
/** 
Description of the class
@author Mudit Goel
@version $Id$
@see classname
@see full-classname
*/
public class Director extends NamedObj implements Executable {
    /** Constructor
     */	
    public Director(CompositeActor container, String name) {
        super(name);
        _container = container;
    }
    
    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    public Nameable getContainer() {
        return _container;
    }

    public void invalidateSchedule() {
    }
    
    public void initialize() {
    }

    public boolean iterate() throws IllegalActionException {
	System.out.println("Iterate beeing called");
        if (prefire()) {
            fire();
            postfire();
        } 
        return _complete;
    }
    
    /** Description
     */	
    public void fire() throws IllegalActionException {
        
    }

    public void postfire() {
    }

    //FIXME:
    public boolean prefire() {
        return true;
    }
    
    public void registerNewActor(Actor actor) {
        synchronized(workspace()) {
            if (_listOfNewActors == null) {
                _listOfNewActors = new LinkedList();
            }
            _listOfNewActors.insertLast(actor);
        }
    }

    public Enumeration getNewActors() {
        return _listOfNewActors.elements();
    }
    
    public void clearNewActors() {
        _listOfNewActors = null;
    }
    
    public void run() throws IllegalActionException {
        initialize();
        while (!iterate());
        wrapup();
        return;
    }

    //FIXME::
    public boolean scheduleValid() {
        return false;
    }

    public boolean getComplete() {
        return _complete;
    }

    public void setComplete(boolean complete) {
        _complete = complete;
    }

    public void wrapup() {
        Enumeration allactors = ((CompositeActor)getContainer()).deepGetEntities();
        while (allactors.hasMoreElements()) {
            Actor actor = (Actor)allactors.nextElement();
            actor.wrapup();
        }
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private CompositeActor _container = null;
    private boolean _complete = true;
    private boolean _schedulevalid;
    private CompositeActor _subsystem;
    private LinkedList _listOfNewActors = null;
    
}
