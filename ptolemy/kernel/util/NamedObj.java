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
     * @return Reference to created named object
     */	
    public NamedObj() {
        this("");
    }

    /** Constructor with 1 argument - Set the name
     * @param name newName
     * @return Reference to created named object
     */	
    public NamedObj(String newName) {
        super();
	name = newName;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Get the name of the object
     * @return The object name
     */	
    public String getName() {
        return name;
    }

   /** Set the name of the object if not previously set
     * @return false if previously set and left unchanged, else true
     */	
    public boolean setName(String newName) {
        if (!name.equals("")) { return false; }
        name = newName;
        return true;
    }

    /** Specify the specific instance's place in the
     * universe-galaxy-star hierarchy. The default implementation
     * returns names that might look like
     * 
     * <P> universe.galaxy.star.port <P>
     * 
     * for a porthole; the output is the fullName of the container, plus
     * a period, plus the name of the NamedObj it is called on.
     * This has no relation to the class name.
     *
     * @return The full object name
     */
    public String getFullName() {
        if(container == null) { return "." + name; }
        else { return container.getFullName() + "." + name; }
    }

    /** Get the container entity
     * @return The container entity
     */	
    public Entity getContainer() {
        return container;
    }

    /** Set the container entity
     * @param newContainer An entity to be the new container
     * @return void
     */	
    public void setContainer(Entity newContainer) {
        container = newContainer;
    }

    /** Prepare the object for system execution 
     * (do nothing at this level - define in derived class)
     * @return void
     */	
    public void reset() {}

    /** Print a description of the object
     * @return A String describing the object
     */	
    public String toString() {
        return new String(getFullName() + ": " + getClass().getName());
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////


    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////


    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* Private variables should not have doc comments, they should
     * have regular comments.
     */
     private String name;            // name
     private Entity container;       // container
}
