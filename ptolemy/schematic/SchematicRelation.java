/* A SchematicRelation represents a relation in a schematic.

 Copyright (c) 1998 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.schematic;

import java.util.Enumeration;
import collections.LinkedList;

//////////////////////////////////////////////////////////////////////////
//// SchematicRelation 
/**

A SchematicRelation represents a relation in a Ptolemy II schematic.
It contains links, which specify the topology of the schematic.


@author Steve Neuendorffer, John Reekie
@version $Id$
*/
public class SchematicRelation extends SchematicElement {

    /** 
     * Create a new SchematicRelation object.
     */
    public SchematicRelation () {
        ;
    }

    /**
     * Add a new link to this relation. The name of the link
     * is the concatenation of the entity name and the port
     * name, separated by a period.
     */
    public void addLink (String name) {
        ;
    }

   /**
     * Test if this relation contains the given link.
     */
    public boolean containsLink (String name) {
        return false;
    }

   /**
     * Return an enumeration over the links in this relation.
     */
    public Enumeration links () {
        return null;
    }

}

