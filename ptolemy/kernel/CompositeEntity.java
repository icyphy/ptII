/* A CompositeEntity is a non-atomic vertex in a hierarchical graph.

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
import java.io.IOException;
import java.util.Enumeration;
import collections.LinkedList;
import pt.kernel.NameDuplicationException;

//////////////////////////////////////////////////////////////////////////
//// CompositeEntity
/** 
A CompositeEntity is a non-atomic vertex in a hierarchical graph. A 
CompositeEntity can contain other HierEntities, thus facilitating 
hierarchy.

@author John S. Davis, II
@version $Id$
*/
public class CompositeEntity extends HierEntity {
    /** 
     */	
    public CompositeEntity() {
         super();
    }

    /** 
     * @param name The name of the CompositeEntity
     */	
    public CompositeEntity(String name) {
         super(name);
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return a data structure holding the contents of this CompositeEntity. 
     */	
    public NamedObjList getAllContents() {
	if( _contents == null ) {
	     _contents = new NamedObjList();
	}
	return _contents;
    }


    /** Return true since CompositeEntities are not atomic.
     */	
    public boolean isAtomic() {
	return false;
    }


    /** Set the contents of this CompositeEntity.
     */	
    public void setContents(NamedObjList contents) {
	_contents = contents;
    }


    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* The HierEntities contained by this CompositeEntity */
    /* FIXME: Develop HierEntityList */
    private NamedObjList _contents = null;

}
