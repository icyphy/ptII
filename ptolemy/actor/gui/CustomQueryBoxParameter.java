/*

 Copyright (c) 2008-2009 The Regents of the University of California.
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

package ptolemy.actor.gui;

import javax.swing.Box;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Settable;

/**
 * An interface for Parameter classes that supply their own
 * customized GUI element for modifying the query content.
 *
 * <p>See <a href="http://www.scms.waikato.ac.nz/~fracpete/downloads/ptolemy/custom_query_boxes/" target="_top">http://www.scms.waikato.ac.nz/~fracpete/downloads/ptolemy/custom_query_boxes/</a>
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public interface CustomQueryBoxParameter {

    /**
     * Create a customized query box for the given query.
     *
     * @param query                        the query to add the custom box to
     * @param attribute                        the attribute the query box is associated with
     * @return a query box.
     * @exception IllegalActionException         if something goes wrong
     */
    public Box createQueryBox(PtolemyQuery query, Settable attribute)
            throws IllegalActionException;
}
