/* Action to edit a custom icon.

 Copyright (c) 1999-2003 The Regents of the University of California.
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

@ProposedRating Red (eal@eecs.berkeley.edu)
@AcceptedRating Red (johnr@eecs.berkeley.edu)
*/

package ptolemy.vergil.toolbox;

import java.awt.event.ActionEvent;

import ptolemy.actor.gui.Configuration;
import ptolemy.kernel.util.ChangeRequest;
import ptolemy.kernel.util.NamedObj;
import ptolemy.util.MessageHandler;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.icon.XMLIcon;

//////////////////////////////////////////////////////////////////////////
//// ConfigureAction
/** Action to edit a custom icon.
 */
public class EditIconAction extends FigureAction {

    public EditIconAction() {
        super("Edit Custom Icon");
        // For some inexplicable reason, the I key doesn't work here.
        // putValue(GUIUtilities.ACCELERATOR_KEY,
        //       KeyStroke.getKeyStroke(KeyEvent.VK_I, Event.CTRL_MASK));
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Process the edit icon command.
     *  @param e The event.
     */
    public void actionPerformed(ActionEvent e) {
        if (_configuration == null) {
            MessageHandler.error(
                    "Cannot edit icon without a configuration.");
            return;
        }

        // Determine which entity was selected for the action.
        super.actionPerformed(e);
        final NamedObj object = getTarget();
            
        // Do this as a change request since it may add a new icon.
        ChangeRequest request = new ChangeRequest(this, "Edit Custom Icon") {
            protected void _execute() throws Exception {
                EditorIcon icon = (EditorIcon)object
                        .getAttribute("_icon", EditorIcon.class);
                if (icon == null) {
                    icon = new EditorIcon(object, "_icon");
                } else if (icon instanceof XMLIcon) {
                    // There is an icon currently that is not custom.
                    // Without trashing the _iconDescription, we can remove
                    // this icon and replace it.
                    icon.setContainer(null);
                    icon = new EditorIcon(object, "_icon");
                }
                _configuration.openModel(icon);
            }
        };
        object.requestChange(request);
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
