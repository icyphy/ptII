/* NamedObj is the baseclass for most of the common Ptolemy objects.

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

package pt.kernel;

//////////////////////////////////////////////////////////////////////////
//// NamedObj
/** 

NamedObj (named object) is the baseclass for most of the common
Ptolemy objects. 

FIXME: In addition to a name, a NamedObj has a reference
to a container object, which is always an Entity (derived from Node
and then, in turn, from NamedObj). This reference can be null.

@author Richard Stevens
<P>  Richard Stevens is an employee of the U.S. Government, whose
  written work is not subject to copyright.  His contributions to 
  this work fall within the scope of 17 U.S.C. A7 105. <P>

@version $Id$

*/

public class NamedObj {
    /** Construct an object with an empty string as its name. */	
    public NamedObj() {
        this("");
    }

    /** Construct an object with the given name. */	
    public NamedObj(String name) {
        super();
	setName(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** FIXME: Return the Entity object that contains this object.
     *  Return null if this object has no container. */	
//    public Entity getContainer() { return _container; } 

    /** FIXME: Return the object's full name.
     *  If the object has no container,
     *  the full name is the object name preceded by a dot.
     *
     *  If the object has a container, 
     *  the full name is the full name of the container
     *  followed by a dot and the object name.
     */
/**    public String getFullName() {
        if(_container == null) { return "." + _name; }
        else { return _container.getFullName() + "." + _name; }
    }  */

    /** Get the name of the object. */	
    public String getName() { return _name; }

    /**FIXME: Specify the Entity object that contains this object. */	
//    public void setContainer(Entity container) { _container = container; }

   /** FIXME: IS name without a dot still required? Specify the name of 
     * the object. The name may not have an embedded dot. */
    public void setName(String name) throws IllegalArgumentException {
        if (name.indexOf('.') >= 0) { 
	  throw new IllegalArgumentException
	    ("NamedObj name (" + name + ") has illegal embedded dot (.)");
	}
        _name = name;
    }

    /** FIXME: Fullname still bothers. Return a description of the object.
     *  The description is the full name followed by a colon
     *  followed by the full class name.
     */	
/**    public String toString() {
        return getFullName() + ": " + getClass().getName();
    } */


    /** Returns a reference to the existing paramList, or if the list does 
      * not exist, then it creates one and returns a reference to it.  */
    // FIXME: Should _paramList be private? 
      public ParamList getParam(){
      	  if( _paramList == null){
		ParamList _paramList = new ParamList();  }
	  return _paramList;
      }
  

    

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Create a unique name for an object added to a container object.
     * Names are of the form "name#num" where "name" is the name of the
     * container object and "num" is typically an enumeration of the order 
     * in which the object in question was added to the container.
     * FIXME: Does createNewName_ really work? And is it required??
     * Added by John Davis on 6/17/97
     * @param containerName The name of the container object.
     * @param cntVariable The Enumeration of the added object's order 
     * of inclusion.
     * @return Return the new cntVariable.
     */
/**    protected int _createNewName(String newName, int countVariable) {
        countVariable++;
        newName = newName + "#" + countVariable;
        return countVariable;
    }  */


    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

     private String _name;
     private ParamList _paramList;
//   private Entity _container;
}
