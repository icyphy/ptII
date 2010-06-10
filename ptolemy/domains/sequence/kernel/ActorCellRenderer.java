/* A renderer that displays getDisplayName() instead of the standard name in a JList.*/
package ptolemy.domains.sequence.kernel;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import ptolemy.actor.Actor;

///////////////////////////////////////////////////////////////////
////SequentialScheduleEditorPane

/**
* A renderer that displays getDisplayName() instead of the standard
* name in a JList.
*
* @author Bastian Ristau
* @version $Id: VisualSequenceDirector.java 58040 2010-06-10 00:03:05Z eal $
* @since Ptolemy II 8.0
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
