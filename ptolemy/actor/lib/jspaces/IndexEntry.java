/* A JavaSpaces Entry that contains the index of TokenEntries.

 Copyright (c) 1997-2000 The Regents of the University of California.
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
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.actor.lib.jspaces;

import ptolemy.data.Token;

import net.jini.core.entry.Entry;

//////////////////////////////////////////////////////////////////////////
//// IndexEntry
/**
IndexEntry is a JavaSpaces Entry that contains a index for TokenEntries.
An IndexEntry has a name, a type, and a position number.
The name is the name of corresponding token entries. The type can be 
"minimum" or "maximum", showing the index type that this entry represents.
The position number is a positive long.


@author Jie Liu, Yuhong Xiong
@version $Id$

*/
public class IndexEntry implements Entry {

    /** Construct an IndexEntry. All the entry fields will be null.
     *  This no-arg constructor is required by JavaSpaces for
     *  deserialization.
     */
    public IndexEntry() {
    }

    /** Construct an IndexEntry with the specified name, serialNumber,
     *  and token.
     *  @param name A String name.
     *  @param serialNumber The starting serial number of this entry.
     *  @param token A Token.
     */
    public IndexEntry(String name, String type, Long position) {
	this.name = name;
	this.type = type;
	this.position = position;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public variables                   ////

    /** The name of this TokenEntries.
     */
    public String name = null;

    /** The type, either "minimum" or "maximum"
     */
    public String type = null;

    /** The token contained in this TokenEntry.
     */
    public Long position = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the position of this index. If the position
     *  is null, return 0. 
     *  @return The postion.
     */
    public long getPosition() {
	if (position == null) {
	    return 0;
	}
	return position.longValue();
    }

    /** Increase the index position by 1. If the position is
     *  null, change it to contain 1.
     */
    public void increment() {
	    position = new Long(getPosition()+1);
    }
}

