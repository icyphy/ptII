/* A mouse filter class for dealing with popups on different platforms.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.basic;

import java.awt.event.MouseEvent;

import ptolemy.gui.PtGUIUtilities;
import diva.canvas.event.ExtendedMouseFilter;

//////////////////////////////////////////////////////////////////////////
//// PopupMouseFilter

/**
 This class is a mouseFilter that recognizes popup events.
 Unfortunately, on because of the Diva event dispatch architecture,
 it is difficult to have interactors receive mouseReleased events,
 without possibly hiding mousePressed events from other interactors.
 This class is a workaround for popup menus which uses
 MouseEvent.isPopupTrigger() for macs, but uses a hardcoded right
 mouse button for PC's.

 @author Steve Neuendorffer
 @version $Id$
 @since Ptolemy II 2.0
 @Pt.ProposedRating Red (eal)
 @Pt.AcceptedRating Red (johnr)
 */
public class PopupMouseFilter extends ExtendedMouseFilter {
    /** Create an attribute controller associated with the specified graph
     *  controller.
     */
    public PopupMouseFilter() {
        super(3, 0, 0);
    }

    /**
     * Test whether the given MouseEvent passes the filter.
     */
    @Override
    public boolean accept(MouseEvent event) {
        if (PtGUIUtilities.macOSLookAndFeel()) {
            return event.isPopupTrigger();
        } else {
            return super.accept(event);
        }
    }
}
