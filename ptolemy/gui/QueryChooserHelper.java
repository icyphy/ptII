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

package ptolemy.gui;

import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;

/**
 * A little helper class for QueryChooser classes.
 *
 * @author  fracpete (fracpete at waikato dot ac dot nz)
 * @version $Revision$
 * @since Ptolemy II 8.0
 * @Pt.ProposedRating Red (cxh)
 * @Pt.AcceptedRating Red (cxh)
 */
public class QueryChooserHelper {

    /**  Determine the parent of this frame.
     *
     * @param container        the container
     * @return                the parent frame if one exists or null if not
     */
    public static Frame getParentFrame(Container container) {
        Frame result;
        Container parent;

        result = null;

        parent = container;
        while (parent != null) {
            if (parent instanceof Frame) {
                result = (Frame) parent;
                break;
            } else {
                parent = parent.getParent();
            }
        }

        return result;
    }

    /**
     * Determine the dialog of this frame.
     *
     * @param container        the container
     * @return                the parent dialog if one exists or null if not
     */
    public static Dialog getParentDialog(Container container) {
        Dialog result;
        Container parent;

        result = null;

        parent = container;
        while (parent != null) {
            if (parent instanceof Dialog) {
                result = (Dialog) parent;
                break;
            } else {
                parent = parent.getParent();
            }
        }

        return result;
    }
}
