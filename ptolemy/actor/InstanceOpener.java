/* An interface that is used to open an instance of a NamedObj.

 Copyright (c) 2013-2014 The Regents of the University of California.
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
package ptolemy.actor;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// InstanceOpener
/** An interface that is used to open an instance of a NamedObj.

 <p>This interface exists so that ModalController and ModalRefinement
 need not depend on Configuration.

 @author Christopher Brooks
 @version $Id$
 @since Ptolemy II 10.0
 @Pt.ProposedRating Green (cxh)
 @Pt.AcceptedRating Yellow (cxh)

 */
public interface InstanceOpener {

    /** Open the specified instance.
     *  A derived class looks for the instance, and if the instance already has
     *  open tableaux, then put those in the foreground
     *  Otherwise, create a new tableau and if
     *  necessary, a new effigy.  Unless there is a more natural container
     *  for the effigy (e.g. it is a hierarchical model), then if a new
     *  effigy is created, it is put into the directory of the configuration.
     *  Any new tableau created will be contained by that effigy.
     *
     *  @param entity The entity to open.
     *  @exception IllegalActionException If constructing an effigy or tableau
     *   fails.
     *  @exception NameDuplicationException If a name conflict occurs (this
     *   should not be thrown).
     */
    public void openAnInstance(NamedObj entity) throws IllegalActionException,
            NameDuplicationException;

}
