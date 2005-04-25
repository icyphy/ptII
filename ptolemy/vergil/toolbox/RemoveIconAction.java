/* Action to remove a custom icon.

Copyright (c) 2003-2005 The Regents of the University of California.
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
package ptolemy.vergil.toolbox;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLChangeRequest;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;

import java.awt.event.ActionEvent;
import java.util.Iterator;


//////////////////////////////////////////////////////////////////////////
//// ConfigureAction

/** Action to remove a custom icon.
    @author Edward A. Lee
    @version $Id$
    @since Ptolemy II 4.0
    @Pt.ProposedRating Red (eal)
    @Pt.AcceptedRating Red (johnr)
*/
public class RemoveIconAction extends FigureAction {
    public RemoveIconAction() {
        super("Remove Custom Icon");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Process the remove icon command.
     *  @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
        // Determine which entity was selected for the look inside action.
        super.actionPerformed(e);

        NamedObj object = getTarget();

        // In theory, there should be only one.
        // But just in case, we remove all.
        Iterator icons = object.attributeList(EditorIcon.class).iterator();

        while (icons.hasNext()) {
            EditorIcon icon = (EditorIcon) icons.next();

            // An XMLIcon is not a custom icon, so don't remove it.
            if (!(icon instanceof XMLIcon)) {
                String moml = "<deleteProperty name=\"" + icon.getName()
                    + "\"/>";
                MoMLChangeRequest request = new MoMLChangeRequest(this, object,
                        moml);
                object.requestChange(request);
            }
        }
    }

    /** Specify the configuration.  This has to be called with a
     *  non-null argument for this action to work.
     */
    public void setConfiguration(Configuration configuration) {
        _configuration = configuration;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The configuration.
    private Configuration _configuration;
}
