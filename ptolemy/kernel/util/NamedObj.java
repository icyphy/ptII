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
NamedObj (named Object) is the baseclass for most of the common
Ptolemy objects. 

@author Mudit Goel
@version $Id$
*/

public class NamedObj {

    /** Construct an object with an empty string as its name. 
     */	
    public NamedObj() {
        setName("");
    }

    /** Construct an object with the given name. 
     */	
    public NamedObj(String name) {
	setName(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** 
     * @return the name of the object. 
     */	
    public String getName() { 
        return _name; 
    }

    /** 
     * @param name of the object.  
     */
    public void setName(String name) {
        _name = name;
    }

    /** 
     * @return a reference to the list of parameters.  
     */
    public ParamList getParams(){
        if( _paramList == null){
            _paramList = new ParamList();
        }
        return _paramList;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    private String _name;
    private ParamList _paramList = null;
}
