/* A Relation is an arc in a hierarchical graph.

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

import collections.UpdatableSeq;

//////////////////////////////////////////////////////////////////////////
//// Relation
/** 
A Relation is an arc in a hierarchical graph. Relations serve as a general 
notion of connection Entities and should be thought of as nets that can be 
specialized to point-to-point connections. 
<A HREF="pt.kernel.Particles.html#_top_">Particles</A> reside in in Relations 
during transit. Unlike Entities, Relations can not contain ports but they 
do have port references. Relations come in two types as follows: 
<UL>
<LI><EM>Source Relations</EM>: Facilitate a one-to-many topology.</LI>
<LI><EM>Destination Relations</EM>: Facilitate a many-to-one topology.</LI>
</UL>
@author John S. Davis, II
@version $Id$
@see pt.kernel.Particle
*/
public class Relation extends NamedObj {
    /** 
     */	
    public Relation() {
	 super();
	 _sourcePorts = null;
	 _destinationPorts = null;
         _isSourceOrDestination = 0;
    }

    /** 
     * @param name The name of the Relation.
     */	
    public Relation(String name) {
	 super(name);
	 _sourcePorts = null;
	 _destinationPorts = null;
         _isSourceOrDestination = 0;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** A Relation must be set as either a Source or Destination relation.
     * @return Return true if this Relation has been set as a Source or
     * Destination relation. Return false otherwise.
     */	
    public boolean isRelationTypeSet() {
	if( _isSourceOrDestination <= 0 || _isSourceOrDestination >= 3 )
	{
	     return true;
	}
        return false;
    }

    /** A Relation must be set as either a Source or Destination relation.
     * @return Return true if this Relation is a Source. Return false otherwise.
     */	
    public boolean isSource() {
	if( _isSourceOrDestination == 1 )
	{
	     return true;
	}
        return false;
    }

    /** A Relation must be set as either a Source or Destination relation.
     * @return Return true if this Relation is a Destination. Return false 
     * otherwise.
     */	
    public boolean isDestination() {
	if( _isSourceOrDestination == 2 )
	{
	     return true;
	}
        return false;
    }

    /** Make this Relation a Source if it has not already been set to a
     *  Destination
     * @return Return true if successful. Return false if this Relation
     * was previously set as a Destination.
     */	
    public boolean makeSourceRelation() {
	if( _isSourceOrDestination != 2 )
	{
	     _isSourceOrDestination = 1;
	     return true;
	}
        return false;
    }

    /** Make this Relation a Destination if it has not already been set to a
     *  Destination
     * @return Return true if successful. Return false if this Relation
     * was previously set as a Source.
     */	
    public boolean makeDestinationRelation() {
	if( _isSourceOrDestination != 1 )
	{
	     _isSourceOrDestination = 2;
	     return true;
	}
        return false;
    }

    /** Description
     * @see full-classname#method-name()
     * @param parameter-name description
     * @param parameter-name description
     * @return description
     * @exception full-classname description
     */	
    public boolean setSourcePort(GenPort port) {
	if( isSource() && _sourcePorts == null )
	{
	     port.newConnection();
	     _sourcePorts.insertFirst( port );
	}
        return true;
    }

    /** Clear all port references.
     */	
    public void clearAllConnections() {
        _sourcePorts.clear();
        _destinationPorts.clear();
        return;
    }

    /** Description
     * @see full-classname#method-name()
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
     * @see full-classname#method-name()
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
     * @see full-classname#method-name()
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

    /* The list of source ports connected via this Relation
     */
    private UpdatableSeq _sourcePorts;

    /* The list of destination ports connected via this Relation
     */
    private UpdatableSeq _destinationPorts;

    /* Set to 1 if this is an source relation. Set to 2 if this is a 
     * destination relation. Set to 0 by the constructor to indicate null. 
     */
    private int _isSourceOrDestination;

    /* Private variables should not have doc comments, they should
       have regular comments.
     */
    private int aPrivateVariable;
}
