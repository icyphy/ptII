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

NamedObj is the baseclass for most of the common Ptolemy objects.  A
NamedObj is, simply put, a named object; in addition to a name, a
NamedObj has a reference to a container object, which is always an
Entity (derived from Node and then, in turn, from NamedObj). This
reference can be null.

@author Richard Stevens
<P>  Richard Stevens is an employee of the U.S. Government, whose
  written work is not subject to copyright.  His contributions to 
  this work fall within the scope of 17 U.S.C. A7 105. <P>

@version $Id$
@see classname
@see full-classname */
public class NamedObj {
    /** Constructor with no arguments - Set the name to empty string
     */	
    public NamedObj() {
        this("");
    }

    /** Constructor with 1 argument - Set the name
     */	
    public NamedObj(String newName) {
        super();
	_name = newName;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Get the name of the object
     */	
    public String getName() {
        return _name;
    }

   /** Set the name of the object
     */	
    public void setName(String newName) {
        _name = newName;
    }

    /** Specify the specific instance's place in the
     * universe-galaxy-star hierarchy. The default implementation
     * returns names that might look like
     * 
     * <P> .universe.galaxy.star.port <P>
     * 
     * for a porthole; the output is the fullName of the container, plus
     * a period, plus the name of the NamedObj it is called on.
     * This has no relation to the class name.
     *
     * @return The full object name
     */
    public String getFullName() {
        if(_container == null) { return "." + _name; }
        else { return _container.getFullName() + "." + _name; }
    }

    /** Get the container entity
     */	
    public Entity getContainer() {
        return _container;
    }

    /** Set the container entity
     */	
    public void setContainer(Entity newContainer) {
        _container = newContainer;
    }

    /** Prepare the object for system execution 
     * (do nothing at this level - define in derived class)
     */	
    public void reset() {}

    /** Return a description of the object
     *  this method needed for Tcl Interface class
     */	
    public String toString() {
        return new String(getFullName() + ": " + getClass().getName());
    }

    /** Create a NamedObj whose name is a given string
     *  this method needed for Tcl Interface class
     */	
    public static NamedObj valueOf (String newName) {
        return new NamedObj(newName);
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////


    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////


    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

     private String _name;
     private Entity _container;
}
