/* Define a content area using Prototype pattern.

 Copyright (c) 2011-2014 The Regents of the University of California.
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

package ptolemy.homer.kernel;

import ptolemy.kernel.util.IllegalActionException;

///////////////////////////////////////////////////////////////////
//// ContentPrototype

/** Define a content area using Prototype pattern.
 *
 *  @author Peter Foldes
 *  @version $Id$
 *  @since Ptolemy II 10.0
 *  @Pt.ProposedRating Red (pdf)
 *  @Pt.AcceptedRating Red (pdf)
 */
public interface ContentPrototype {

    /** Add an element to the content area.
     *
     *  @param element The element to be added to the content area.
     *  @exception IllegalActionException If the content area is not set.
     */
    void add(PositionableElement element) throws IllegalActionException;

    /** Remove an element from the content area.
     *
     *  @param element The element to be removed.
     *  @exception IllegalActionException If the content area is not set.
     */
    void remove(PositionableElement element) throws IllegalActionException;

    /** Get the content area.
     *
     *  @return The content area.
     */
    Object getContent();

    /** Get a new instance of the prototype.
     *
     *  @return The new instance of the prototype.
     */
    ContentPrototype getNewInstance();
}
