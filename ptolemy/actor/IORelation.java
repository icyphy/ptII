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

@ProposedRating Red (liuj@eecs.berkeley.edu)

*/

package pt.actors;

import pt.kernel.*;
import java.util.Enumeration;
import collections.*;

//////////////////////////////////////////////////////////////////////////
//// IORelation
/** 
Relation supporting transfer of data between ports.
The purpose of this relation is to ensure that IOPorts are only
connected to IOPorts. IORelation can have width to represent something
like a bus. The default width id one.

@author Edward A. Lee
@version $Id$
*/
public class IORelation extends ComponentRelation {

    /** Construct a relation in the default workspace with an empty string
     *  as its name. Width = 1.
     *  Increment the version number of the workspace.
     */
    public IORelation() {
        super();
    }

    /** Construct a relation in the specified workspace with an empty
     *  string as a name (you can then change the name with setName()).
     *  If the workspace argument is null, then use the default workspace.
     *  Increment the version number of the workspace. Width = 1.
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
     *  Width = 1.
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

    /** Construct a relation,  with the given name and width, contained by 
     *  the specified
     *  entity. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This relation will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  If width < 1, then create a width one IOrelation. 
     *  Increment the version of the workspace.
     *  @param container The parent entity.
     *  @param name The name of the relation.
     *  @exception NameDuplicationException Name coincides with
     *   a relation already in the container.
     */	
    public IORelation(CompositeEntity container, String name, int width)
            throws NameDuplicationException {
        super(container, name);
        if(width < 1) {
            width = 1;
        }
        _width = width;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////
    
    /** Return the destination Receptionists for a given port. The given port
     * should be an output port. The Receptionists are ordered as an array of
     * Receptionists. Each objects in the same row receive the same token.
     * Receptionists in different rows receive distinct tokens.
     *
     * @see IOPort#deepReceptionists
     * @param The 'output' port.
     * @return An array of Receptionist objects
     * @exception InvalidStateException If the Receptionist arrays from 
     *            linked Ports have different length from the width of this
     *            relation.
     */
    public Receptionist[][] deepReceptionists(IOPort givenPort)
            throws InvalidStateException {
        Enumeration inputs = linkedInputPortsExcept(givenPort);
        Receptionist[][] result = new Receptionist[_width][];
        while(inputs.hasMoreElements()) {
            IOPort p = (IOPort) inputs.nextElement();
            Receptionist[][] portHolder = p.deepReceptionists(this);
            if(portHolder.length !=_width) {
                throw new InvalidStateException("width error");
            }
            result = _cascade(result, portHolder);
        }
        return result;
    }
            
    /** Return the width of the IORelation
     * @return width
     */	
    public int getWidth() {
        return _width;
    }

    /** Enumerate the linked input ports.
     * synchronized on work space.
     * @see pt.kernel.Relation#linkedPorts.
     * @return Ann enumeration of the linked input ports
     */	
    public Enumeration linkedInputPorts() {
        synchronized(workspace()) {
            LinkedList inputPorts = new LinkedList();
            Enumeration ports = linkedPorts();
            while(ports.hasMoreElements()) {
                IOPort p = (IOPort) ports.nextElement();
                if(p.isInput()) {
                    inputPorts.insertLast(p);
                }
            }
            return inputPorts.elements();
        }
    }

    /** Enumerate the linked input ports except the given port.
     * synchronized on work space.
     * @see pt.kernel.Relation#linkedPortsExcept.
     * @param except The port not included in the returned Enumeration.
     * @return Ann enumeration of the linked input ports
     */	
    public Enumeration linkedInputPortsExcept(IOPort except) {
        // If the givenPort is not an input port, just call 
        // linkedInputPorts, otherwise construct a new LinkedList.
        if (!except.isInput()) {
            return linkedInputPorts();
        }
        synchronized(workspace()) {
            LinkedList resultPorts = new LinkedList();
            Enumeration inputPorts = linkedInputPorts();
            while(inputPorts.hasMoreElements()) {
                IOPort p = (IOPort) inputPorts.nextElement();
                if(p != except) {
                    resultPorts.insertLast(p);
                }
            }
            return resultPorts.elements();
        }
    }

    /** Enumerate the linked output ports.
     * synchronized on work space.
     * @see pt.kernel.Relation#linkedPorts.
     * @return Ann enumeration of the linked input ports
     */	
    public Enumeration linkedOutputPorts() {
        synchronized(workspace()) {
            LinkedList outputPorts = new LinkedList();
            Enumeration ports = linkedPorts();
            while(ports.hasMoreElements()) {
                IOPort p = (IOPort) ports.nextElement();
                if(p.isOutput()) {
                    outputPorts.insertLast(p);
                }
            }
            return outputPorts.elements();
        }
    }

    /** Enumerate the linked output ports except the given port.
     * synchronized on work space.
     * @see pt.kernel.Relation#linkedPortsExcept.
     * @param except The port not included in the returned Enumeration.
     * @return Ann enumeration of the linked output ports
     */	
    public Enumeration linkedOutputPortsExcept(IOPort except) {
        // If the givenPort is not an output port, just call 
        // linkedOutputPorts, otherwise construct a new LinkedList.
        if (!except.isOutput()) {
            return linkedOutputPorts();
        }
        synchronized(workspace()) {
            LinkedList resultPorts = new LinkedList();
            Enumeration outputPorts = linkedOutputPorts();
            while(outputPorts.hasMoreElements()) {
                IOPort p = (IOPort) outputPorts.nextElement();
                if(p != except) {
                    resultPorts.insertLast(p);
                }
            }
            return resultPorts.elements();
        }
    }

    
    /** Set the width of the IORelation. If the width is less than one, 
     *  the width is set to be one.
     *  @see getWidth
     *  @param width the width to be set
     */
    public void setWidth(int width) {
        if(width <1) {
            width = 1;
        }
        _width = width;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    /** Cascade two Receptionist arrays to a new array. For each row, each 
     *  element of the second array is put behind the elements of the
     *  first array. This method is solely for deepReceptionists.
     *  The two input arrays must have the same number of rows. 
     * @param array1 the first array.
     * @param array2 the second array.
     */
    public Receptionist[][] _cascade(Receptionist[][] array1,
                                     Receptionist[][] array2) {
        int n = array1.length;
        Receptionist[][] result = new Receptionist[n][];
        for (int i = 0; i <n; i++) {
            int m1 = array1[i].length;
            int m2 = array2[i].length;
            result[i] = new Receptionist[m1+m2];
            for (int j = 0; j < m1; j++) {
                result[i][j] = array1[i][j];
            }
            for (int j = m1; j < m1+m2; j++) {
                result[i][j] = array2[i][j-m1];
            }
        }
        return result;
    }

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

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    //width of the relation.
    private int _width = 1;
    
}
