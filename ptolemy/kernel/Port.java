/* A Port is the interface between Entities and Relations.

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
A Port is the interface between Entities and Relations.
@author John S. Davis, II
@version $Id$
*/
public class Port extends GenericPort {
    /** 
     * @param name - The name of the Port.
     */	
    public Port(String name) {
	 super(name);
	 _connected = false;
	 _multiPortContainer = null;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the MultiPort which contains this Port.
     */	
    public MultiPort getMultiPortContainer() {
        return _multiPortContainer;
    }

    /** Return true if this Port is connected to another Port. Return false
     *  otherwise.
     */	
    public boolean isConnected() {
	MultiPort multiPort = null;
	setMultiPortContainer( multiPort );
        return _connected;
    }

    /** Set the MuliPort which contains this Port.
     */	
    public void setMultiPortContainer(MultiPort multiPort) {
	/*
	if( multiPort == null )
	{
	     // FIXME: Throw an exception here!
	}
	*/
	_multiPortContainer = multiPort;
        return; 
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* This variable is set to true if it is connected through a relation
     * to another port.  
     */
    private boolean _connected;

    /* The MultiPort which contains this Port.
     */
    private MultiPort _multiPortContainer;
}
