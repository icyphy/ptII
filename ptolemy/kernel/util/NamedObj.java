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
Description:   NamedObj is the baseclass for most of the common
               Ptolemy objects.  A NamedObj is, simply put, a named
               object; in addition to a name, a NamedObj has a reference
               to a parent object, which is always a Block (a type of
               NamedObj). This reference can be null. A NamedObj also
               has a descriptor. <P>

@author Richard Stevens
  Richard S. Stevens is an employee of the U.S. Government, whose
  written work is not subject to copyright.  His contributions to 
  this work fall within the scope of 17 U.S.C. A7 105.

@version @(#)JavaTemplate.java	1.6	02/14/97
@see classname
@see full-classname
*/
public abstract class NamedObj {
    /** no-arg Constructor - Construct a blank NamedObj
     * @see full-classname/method-name
     * @return Set the name, parent, and descriptor to null
     * @exception full-classname description
     */	
    public NamedObj() {
        this(null, null, null);
    }

    /** Constructor with 3 arguments
     * @see full-classname/method-name
     * @param n name
     * @param p parent
     * @param d descriptor
     * @return Set the name, parent, and descriptor 
     *         to the respective arguments
     * @exception full-classname description
     */	
    public NamedObj(String n, Block p, String d) {
        super();
        setName(n);
        setParent(p);
        setDescriptor(d);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Get the name of the object
     * @see full-classname/method-name
     * @return The object name
     * @exception full-classname description
     */	
    public String getName() {
        return nm;
    }
    public String name() {
        return getName();
    }

    /** Get the descriptor
     * @see full-classname/method-name
     * @return The descriptor
     * @exception full-classname description
     */	
    public String getDescriptor() {
        return dscrptr;
    }
    public String descriptor() {
        return getDescriptor();
    }

    /** Get the parent block
     * @see full-classname/method-name
     * @return The parent block
     * @exception full-classname description
     */	
    public Block getParent() {
        return prnt;
    }
    public Block parent() {
        return getParent();
    }

    /** This has no relation to the class name; it specifies
     * the specific instance's place in the universe-galaxy-star
     * hierarchy. The default implementation returns names that might look
     * like
     *
     *       universe.galaxy.star.port
     *
     * for a porthole; the output is the fullName of the parent, plus a
     * period, plus the name of the NamedObj it is called on.
     *
     * @see full-classname/method-name
     * @return The full object name
     * @exception full-classname description
     */	
    abstract public String getFullName();
    public String fullName() {
        return getFullName();
    }

    /** Set the object name
     * @see full-classname/method-name
     * @param myName A String to be the new object name
     * @return void
     * @exception full-classname description
     */	
    public void setName (String myName) {
        nm = myName;
    }

    /** Description Set the parent block
     * @see full-classname/method-name
     * @param myParent A Block to be the new parent
     * @return void
     * @exception full-classname description
     */	
    public void setParent(Block myParent) {
        prnt = myParent;
    }

    /** Set the name and parent
     * @see full-classname/method-name
     * @param myName A String to be the new object name
     * @param myParent A Block to be the new parent
     * @return void
     * @exception full-classname description
     */	
    public void setNameParent(String myName, Block myParent) {
        setName(myName);
        setParent(myParent);
    }

    /** Prepare the object for system execution 
     * (abstract - must be defined in derived class)
     * @see full-classname/method-name
     * @return void
     * @exception full-classname description
     */	
    abstract public void initialize();

    /** Print a description of the object
     * (abstract - must be defined in derived class)
     * @see full-classname/method-name
     * @param verbose If true, verbose description, else less verbose
     * @return A String describing the object
     * @exception full-classname description
     */	
    abstract public String print(boolean verbose);


    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Set the descriptor
     * @see full-classname/method-name
     * @param myDescriptor A String giving the new descriptor
     * @return void
     * @exception full-classname description
     */	
     protected void setDescriptor(String myDescriptor) {
         dscrptr = myDescriptor;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////


    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* Private variables should not have doc comments, they should
       have regular comments.
     */
     private String nm;            // name
     private Block prnt;           // parent
     private String dscrptr;       // descriptor
}
