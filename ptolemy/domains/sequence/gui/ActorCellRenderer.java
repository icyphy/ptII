/* A renderer that displays getDisplayName() instead of the standard name in a JList.
 Copyright (c) 2010 The Regents of the University of California.
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
package ptolemy.domains.sequence.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ptolemy.actor.Actor;

///////////////////////////////////////////////////////////////////
//// ActorCellRenderer

/**
* A renderer that displays getDisplayName() instead of the standard
* name in a JList.
*
* @author Bastian Ristau
* @version $Id$
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ristau)
* @Pt.AcceptedRating Red (ristau)
*/
public class ActorCellRenderer extends DefaultListCellRenderer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    public Component getListCellRendererComponent(JList list, // the list
            Object value, // value to display
            int index, // cell index
            boolean isSelected, // is the cell selected
            boolean cellHasFocus) // does the cell have focus
    {
        super.getListCellRendererComponent(list, value, index, isSelected,
                cellHasFocus);
        Actor actor = null;
        if (value instanceof Actor) {
            actor = (Actor) value;
        }
        if (actor != null) {
            this.setText(actor.getDisplayName());
        }
        return this;
    }

}
