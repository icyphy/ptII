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

import java.util.Hashtable;
import java.util.Enumeration;
import pt.exceptions.NameDuplicationException;
import pt.exceptions.NullReferenceException;

//////////////////////////////////////////////////////////////////////////
//// Port
/** 
A Port is the interface between Entities and Relations.
@author John S. Davis, II
@version $Id$
*/
public class Port extends GenericPort {
    /** 
     */	
    public Port() {
	super();
        if( aliasPairList_ == null ) {
             aliasPairList_ = new Hashtable();
        }
    }

    /** 
     * @param name The name of the Port.
     * @exception NameDuplicationException Attempt to store two instances of
     * the same class with identical names in the same container.
     */	
    public Port(String name) throws NameDuplicationException {
	super(name);
        if( aliasPairList_ == null ) {
             aliasPairList_ = new Hashtable();
        }
	addNewAliasPair_();
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Insert this Port in the alias chain one level outside of 
     *  the position of the argument.
     *  @param innerPort The Port outside of which this Port will be placed.
     */
    public synchronized void aliasInsertAsOuter(Port innerPort) {
	 Port outerPort = (Port)innerPort.getUpAlias();
	 Port middlePort = this;

	 juxtaposeOuterWithInner_( middlePort, innerPort ); 
	 juxtaposeOuterWithInner_( outerPort, middlePort );

	 if( outerPort != null ) {
	      outerPort.aliasSet();
	 }
	 innerPort.aliasSet();
    }

    /** Copy the alias pair values from the static alias pair list.
     */
    public void aliasSet() {
	 String name = this.getName();
	 AliasPair aliasPair = (AliasPair)aliasPairList_.get( name ); 
	 innerAlias_ = (Port)aliasPair.getInnerAlias();
	 outerAlias_ = (Port)aliasPair.getOuterAlias();
    }

    /** Connect this Port to a Relation.
     * @param relation The Relation to which this Port will be connected.
     * @return Return true if the connection is successful. Return false
     * if the connection is unsuccessful because this Port already is
     * already connected to a non-null Relation.
     * @exception NullReferenceException Attempt to pass null object 
     * references as arguments.
     * @exception NameDuplicationException Attempt to store two instances of
     * the same class with identical names in the same container.
     */	
    public boolean connectToRelation(Relation relation) 
	throws NullReferenceException, NameDuplicationException {
	if( _relation != null ) {
	     return false;
	}
	_relation = relation;
	if( _relation == null ) {
	     throw new NullReferenceException( 
	     "Null Relation passed to Port.connectToRelation()" );
	}
	_relation.connectPort( this );
        return true;
    }

    /** Disconnect this Port from its Relation.
     * @return Return the name of the Relation from which the Port
     * was connected. Return null if the connection was non-existent.
     */	
    public String disconnect() {
	if( _relation == null ) {
	     return null;
	}
	_relation.disconnectPort(this);
	_relation = null;
	return _relation.getName();
    }

    /** Return the Ports that are connected to this Port through its Relation.
     *  Return null if no connections exist.
     */	
    public Enumeration getConnectedPorts() {
	return _relation.getPorts();
    }

    /** Return a reference to the GenericPort alias one level in in the
     *  hierarchy. Return null if this port is the innermost port of the
     *  hierarchy.
     */
    public GenericPort getInnerAlias() {
        return innerAlias_;
    }

    /** Return the MultiPort which contains this Port.
     */	
    public MultiPort getMultiPortContainer() {
        return multiPortContainer_;
    }

    /** Return the name of this Port's Relation. Return null if
     *  the Relation is null.
     */	
    public String getRelationName() {
	if( _relation == null ) {
	     return null;
	}
        return _relation.getName();
    }

    /** Return a reference to the GenericPort alias one level up in the
     *  hierarchy. Return null if this port is at the top of the
     *  hierarchy.
     */
    public GenericPort getUpAlias() {
        return outerAlias_;
    }

    /** Return false since this is a Port.
     */
    public final boolean isMulti() {
        return false;
    }

    /** Return true if this Port is connected to another Port. Return false
     *  otherwise.
     */	
    public boolean isConnected() {
	if( _relation != null ) {
	     if( !_relation.isDangling() ) {
		  return true;
	     }
	}
        return false;
    }


    /** Prepare for a new connection by returning a port. 
     * @return Return the real port.
     * @exception NullReferenceException Attempt to pass null object
     * references as arguments.
     */	
    public Port prepareForConnection() throws NullReferenceException {
	GenericPort genericPort = realPort();

	return (Port)genericPort;
    }


    /** Set the MuliPort which contains this Port.
     * @param multiPort The MultiPort which will be the container of this Port.
     * @exception NullReferenceException Attempt to pass null object 
     * references as arguments.
     */	
    public void setMultiPortContainer(MultiPort multiPort) 
	throws NullReferenceException {
	if( multiPort == null ) {
	     throw new NullReferenceException( 
	     "Null Multiport passed to Port.setMultiPortContainer()" );
	}
	multiPortContainer_ = multiPort;
        return; 
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         protected methods                        ////

    //////////////////////////////////////////////////////////////////////////
    ////                         protected variables                      ////

    /** This is the Relation through which the Port connects to other
     *  Ports. The Relation is non-null only for Ports at the lowest level
     *  of the hierarchy.
     */
    protected Relation _relation;

    //////////////////////////////////////////////////////////////////////////
    ////                         private methods                          ////

    /** Create a new alias pair associated with this Port. 
     * @param portName The name of the Port associated with this alias pair.
     * @exception NameDuplicationException Attempt to store two instances of 
     * the same class with identical names in the same container.
     */
    private void addNewAliasPair_() throws NameDuplicationException {

	String portName = this.getName();
        AliasPair newPair = new AliasPair();
        AliasPair duplicatePair;
        duplicatePair = (AliasPair)aliasPairList_.put( portName, newPair );
        if( duplicatePair != null ) {
   	     aliasPairList_.put( portName, duplicatePair );

             // Two Ports with identical names have been created.
             throw new NameDuplicationException( portName );
        }
    }

    /** Juxtapose two Ports in the alias chain. 
     * @param outerPort The Port which will be outermost in the pair.
     * @param innerPort The Port which will be innermost in the pair.
     */
    private void juxtaposeOuterWithInner_(Port outerPort, Port innerPort) {

	 if( innerPort != null ) {
	      String innerName = innerPort.getName(); 
	      AliasPair innerAliasPair 
		= (AliasPair)aliasPairList_.get( innerName );
	      innerAliasPair.setOuterAlias( outerPort );
	 } 

	 if( outerPort != null ) {
	      String outerName = outerPort.getName(); 
	      AliasPair outerAliasPair 
		= (AliasPair)aliasPairList_.get( outerName ); 
	      outerAliasPair.setInnerAlias( innerPort );
	 }
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /** This list is used to verify that up and down aliases are
     * consistent.
     */
    private static Hashtable aliasPairList_;

    /** The Port reference one level down the hierarchy the chain.
     *  This reference remains null for Ports at the lowest level of
     *  hierarchy chain.
     */
    private Port innerAlias_;

    /* The Port reference one level up the hierarchy the chain.
     */
    private Port outerAlias_;

    /* The MultiPort which contains this Port.
     */
    private MultiPort multiPortContainer_;
}
