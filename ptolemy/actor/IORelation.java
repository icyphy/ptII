/* Relation supporting transfer of data between ports.

 Copyright (c) 1997- The Regents of the University of California.
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

    /** Construct a relation in the default workspace with an empty string
     *  as its name.
     *  Increment the version number of the workspace.
     */
    public IORelation() {
        super();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name (you can then change the name with setName()).
     *  If the workspace argument is null, then use the default workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the relation.
     */
    public IORelation(Workspace workspace) {
	super(workspace);
    }

    /** Construct a relation with the given name contained by the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.
     *  @param container The parent entity.
     *  @param name The name of the relation.
     *  @exception NameDuplicationException Name coincides with
     *   a relation already in the container.
     */	
    public IORelation(CompositeEntity container, String name)
            throws NameDuplicationException {
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
        return super._getPortList(port);
    }
}
