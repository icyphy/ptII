/* Exception thrown on an attempt to add a named object to a collection that
requires unique names, and finding that there already is an object by that
name in the collection.

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

@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.kvm.kernel.util;

//////////////////////////////////////////////////////////////////////////
//// NameDuplicationException
/** Thrown on an attempt to add a named object to a collection that
requires unique names, and finding that there already is an object
by that name in the collection.
Constructors are provided that take 1 or 2 Nameable references
plus an arbitrary String.  The constructors are robust in that null
references are ignored.  The preferred constructors are those that
take two named objects (the container and the would-be containee),
or two named objects and an arbitrary string (which can be used to
provide additional information about the error).

@author John S. Davis II, Edward A. Lee
@version $Id$
*/
public class NameDuplicationException extends KernelException {

    /** Given container and string for message.
     *  @param container The would be container.
     *  @param moreInfo A message.
     */
    public NameDuplicationException(Nameable container,
            String moreInfo) {
        super(container, null, moreInfo);
    }

    /** Given container and containee.
     *  @param wouldBeContainee The would be containee.
     *  @param container The would be container.
     */
    public NameDuplicationException(Nameable container,
            Nameable wouldBeContainee) {
        if (_getFullName(container).equals("")) {
            _setMessage("Attempt to insert object named \"" +
                    _getName(wouldBeContainee) +
                    "\" into a container that already contains" +
                    " an object with that name.");
        } else {
            _setMessage("Attempt to insert object named \"" +
                    _getName(wouldBeContainee) +
                    "\" into container named \"" +
                    _getFullName(container) +
                    "\", which already contains an object with that name.");
        }
    }

    /** Given container, containee, and string.
     *  @param wouldBeContainee The would be containee.
     *  @param container The would be container.
     *  @param moreInfo A message.
     */
    public NameDuplicationException(Nameable container,
            Nameable wouldBeContainee, String moreInfo) {
        if (_getFullName(container).equals("")) {
            _setMessage("Attempt to insert object named \"" +
                    _getName(wouldBeContainee) +
                    "\" into a container that already contains" +
                    " an object with that name. " +
                    moreInfo);
        } else {
            _setMessage("Attempt to insert object named \"" +
                    _getName(wouldBeContainee) +
                    "\" into container named \"" +
                    _getFullName(container) +
                    "\", which already contains an object with that name. " +
                    moreInfo);
        }
    }
}
