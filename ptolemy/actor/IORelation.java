/* Relation supporting transfer of data between ports.

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
//// IORelation
/** 
Relation supporting transfer of data between ports.
The sole purpose of this relation is to ensure that IOPorts are only
connected to IOPorts.

@author Edward A. Lee
@version $Id$
*/
public class IORelation extends ComponentRelation {

    /** Create an object with no name and no container */	
    public IORelation() {
         super();
    }

    /** Create an object with a name and no container. 
     *  @param name
     *  @exception IllegalActionException Argument is null.
     */	
    public IORelation(String name)
           throws IllegalActionException {
        super(name);
    }

    /** Create an object with a name and a container. 
     *  @param container
     *  @param name
     *  @exception IllegalActionException Name argument is null.
     *  @exception NameDuplicationException Name collides with a name already
     *   on the container's contents list.
     */	
    public IORelation(CompositeEntity container, String name)
           throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Return a reference to the local port list.  Throw an exception if
     *  the specified port is not an IOPort.
     *  NOTE : This method has been made protected for the sole purpose
     *  of connecting Ports to Relations (see Port.link(Relation)). It
     *  should NOT be accessed by any other method.
     *  @param port The port to link to.
     *  @exception IllegalActionException Incompatible port.
     */
    protected CrossRefList _getPortList (Port port) 
            throws IllegalActionException {
        if (!(port instanceof IOPort)) {
            throw new IllegalActionException(this, port,
                    "IORelation can only link to a IOPort.");
        }
        return _portList;
    }
}
