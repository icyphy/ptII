/* Interface for objects that need to be notified of hierarchy changes above them.

 Copyright (c) 1997-2010 The Regents of the University of California.
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
package ptolemy.kernel.util;


///////////////////////////////////////////////////////////////////
//// HierarchyListener

/**
 Interface for objects that need to be notified of hierarchy changes above them.
 Objects that implement this listener register with their container
 and will be notified when certain significant events above them in the hierarchy
 occur. In particular:
 <ul>
 <li> if the container of any object above changes;
 <li> if any object changes from an instance to a class or vice versa; or
 <li> if any object changes from opaque to transparent or vice versa
      (acquires or loses a Director).
 </ul>
 In each case,
 two methods will be called on the object implementing this interface:
 first, {@link #hierarchyWillChange} is called, notifying the object that
 the hierarchy is about to change; then {@link #hierarchyChanged} is called,
 notifying the object that the hierarchy has changed.
 The notified object can prevent changes by throwing an exception when
 the first method is called.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal    )
 */
public interface HierarchyListener extends Nameable {

    // Note: This extends Nameable to ensure that objects that implement
    // this interface have a container that is a NamedObj (or have no
    // container).

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Notify this object that the containment hierarchy above it has
     *  changed.
     *  @exception IllegalActionException If the change is not
     *   acceptable.
     */
    public void hierarchyChanged() throws IllegalActionException;

    /** Notify this object that the containment hierarchy above it will be
     *  changed.
     *  @exception IllegalActionException If the change is not
     *   acceptable.
     */
    public void hierarchyWillChange() throws IllegalActionException;
}
