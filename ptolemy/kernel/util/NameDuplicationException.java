/* NameDuplicationException is thrown in response to attempts to store 
objects with identical names in the same container object.

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
//// NameDuplicationException
/** NameDuplicationException is thrown in response to attempts to store 
objects with identical names in the same container object.
@author John S. Davis, II
@version @(#)NameDuplicationException.java	$Revision$	$Date$
*/
public class NameDuplicationException extends GraphException {
    /** 
     * @param duplicateName The name which has been duplicated.
     */	
    public NameDuplicationException(String duplicateName) {
        super();
        _duplicateName = duplicateName;
    }

    /** 
     * @param message The detailed message.
     * @param duplicateName The name which has been duplicated.
     */	
    public NameDuplicationException(String message, String duplicateName) {
        super(message);
        _duplicateName = duplicateName;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         public methods                           ////

    /** Return the name which has been duplicated.
     */
    public String getDuplicateName() {
        return _duplicateName;
    }

    //////////////////////////////////////////////////////////////////////////
    ////                         private variables                        ////

    /* The name which has been duplicated
     */
    private String _duplicateName;
}










