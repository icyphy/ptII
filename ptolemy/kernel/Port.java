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

import java.util.Enumeration;

//////////////////////////////////////////////////////////////////////////
//// Port
/** 
A Port is the interface between Entities and Relations.
@author Mudit Goel
@version $Id$
*/
public class Port extends NamedObj {
    /** 
     */	
    public Port() {
	super();
	_relationsList = new CrossRefList(this);
    }

    /** 
     * @param name The name of the Port.
     */	
    public Port(String name) {
	super(name);
	_relationsList = new CrossRefList(this);
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Connect this Port to a Relation.
     * @param relation The Relation to which this Port will be connected.
     * @exception java.lang.NullPointerException Signals an attempt
     *  to a pass null object as an argument.
     * @exception pt.kernel.GraphException Attempt to connect this 
     *  port to a relation to which it's already connected.
     */	
    public void connectToRelation(Relation relation)
    	    throws NullPointerException {
        if( relation == null ) {
	    throw new NullPointerException( 
	            "Null Relation passed to Port.connectToRelation()" );

	//FIXME: Out here assuming that we can never have a case that
	// can force the crossRefList to be such that one List has a 
	// reference to the other list while the other list doesn't have
	// a reference to the first one!!
	//else if(_relationsList != null) {
	//    if( _relationsList.isMember(relation) ) 
        //            throw new GraphException("Relation is already 
	//	    associated with the port");
	//}
	} else {
            if(_relationsList == null)
                _relationsList = new CrossRefList(this);
        }
        _relationsList.associate( relation._portList );
	return;
    }


    /** Disconnect this Port from all the Relations it was connected to.
     */	
    public void disconnectAllRelations() {
	if( _relationsList == null ) return;
	_relationsList.dissociate();
	_relationsList = null;
	return;
    }


    /** Disconnect this Port and the specified Relation. If the Relation
     *  is not associated with this port, the method doesn't do anything.
     * @param The Relation from which this port is being disconnected
     */
    public void disconnectRelation(Relation relation) {
        if ( _relationsList != null ) _relationsList.dissociate(relation);
        return;
    } 


    /** Returns an enumeration of the CrossRefList and thus of all the 
     *  relations the Port is associated with
     * @return Return an enumeration of relations. 
     */
    public Enumeration enumRelations() {
        if (_relationsList == null) _relationsList = new CrossRefList(this);
	return _relationsList.elements();
    }


    /** Return the Entity which owns this Port. */
    public Entity getEntity() {
	return _entity;
    }


    /** Return the size of the corresponding CrossRefList, i.e. the number
     *  of relations associated with this port.
     * @return null if the crossRefList does not exist, i.e. there is no 
     *  relation associated with the port. Else return the size of the list, 
     *  i.e. the number of relations.
     */
    public int numRelations() {
        if (_relationsList == null) return 0;
	else return _relationsList.size();
    }


    /** Set the Entity which owns this Port. 
     * @exception NameDuplicationException Attempt to store two 
     *  instances of the same class with identical names in the same 
     *  container.
     */
    public void setEntity(Entity entity) throws 
	    GraphException, NameDuplicationException {
	NamedObjList list = entity.getPorts();
	list.append( this );
	_entity = entity;
    }

    
    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    // The Entity which owns this port.
    private Entity _entity = null;

    // The list of relations for this port.
    private CrossRefList _relationsList;
    
}




