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

package pt.kernel;

//////////////////////////////////////////////////////////////////////////
//// Port
/** 
Description of the class
@author 
@version $Id$
@see classname
@see full-classname
*/
public class Port extends GenericPort {
    /** 
     * @param name - The name of the Port.
     */	
    public Port(String name) {
	 super(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return true if this Port is connected to another Port. Return false
     *  otherwise.
     */	
    public boolean isConnected() {
        return _connected;
    }

    /** Description
     * @see full-classname/method-name
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public int APublicMethod() {
        return 1;
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Description
     * @see full-classname/method-name
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    protected int AProtectedMethod() {
        return 1;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    /** Description */
    protected int aProtectedVariable;

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /* Private methods should not have doc comments, they should
     * have regular comments.
     * @see full-classname/method-name
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    private int APrivateMethod() {
        return 1;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* This variable is set to true if is is connected through a relation
     * to another port.  
     */
    private boolean _connected;
}
