/* A look-and-feel independent trigger for pop-ups.

 Copyright (c) 1999-2000 The Regents of the University of California.
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
@ProposedRating Red (Ed.Willink@rrl.co.uk)
@AcceptedRating Red (Ed.Willink@rrl.co.uk)
*/

package ptolemy.vergil.form;

import java.awt.event.*;

/** PopupListener extends MouseAdapter to combine the three possible look-and-feel dependent pop-up activations
into the single look-and-feel independent showPopUp invocation.
@author Edward D. Willink
@version $Id$
*/
abstract class PopupListener extends MouseAdapter
{
    public void mousePressed(MouseEvent e) { if (e.isPopupTrigger()) showPopup(e); }
    public void mouseClicked(MouseEvent e) { if (e.isPopupTrigger()) showPopup(e); }
    public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) showPopup(e); }
    /** Activate a pop-up in look-and-feel independent fashion */
    public abstract void showPopup(MouseEvent e);
}
