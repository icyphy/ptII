/* A JavaSpaces Entry that contains a Token.

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

@ProposedRating Red (yuhong@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)

*/
package ptolemy.actor.lib.jspaces;

import ptolemy.data.Token;

import net.jini.core.entry.Entry;

//////////////////////////////////////////////////////////////////////////
//// TokenEntry
/**
TokenEntry is a JavaSpaces Entry that contains a Token. In addition,
aach TokenEntry has a name and a serial number.

@author Yuhong Xiong, Jie Liu
@version $Id$

*/
public class TokenEntry implements Entry {

    /** Construct a TokenEntry. All the entry fields will be null.
     *  This no-arg constructor is required by JavaSpaces for
     *  deserialization.
     */
    public TokenEntry() {
    }

    /** Construct a TokenEntry with the specified name, serialNumber,
     *  and token.
     *  @param name A String name.
     *  @param serialNumber The starting serial number of this entry.
     *  @param token A Token.
     */
    public TokenEntry(String name, long serialnumber, Token token) {
	this.name = name;
	this.serialNumber = new Long(serialnumber);
	this.token = token;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        public variables                   ////

    /** The name of this TokenEntry.
     */
    public String name = null;

    /** The Integer object that contains the serial number of this
     *  TokenEntry. JavaSpaces requires entry fields to be objects.
     */
    public Long serialNumber = null;

    /** The token contained in this TokenEntry.
     */
    public Token token = null;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the serial number of this entry. If the serialNumberObj
     *  is null, return 0.
     *  @return An int.
     */
    public long getSerialNumber() {
	if (serialNumber == null) {
	    return 0;
	}
	return serialNumber.longValue();
    }

    /** Increase the serial number by 1. If the serialNumberObj is
     *  null, change it to contain 1.
     */
    public void increaseSerialNumber() {
	if (serialNumber == null) {
	    serialNumber = new Long(1);
	} else {
	    long val = serialNumber.longValue();
	    serialNumber = new Long(val+1);
	}
    }
}

