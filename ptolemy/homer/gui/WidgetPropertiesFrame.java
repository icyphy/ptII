/* Frame to edit properties of widgets.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

///////////////////////////////////////////////////////////////////
////WidgetPropertiesFrame

/**
* Frame to edit properties of widgets.
* @author Ishwinder Singh
* @version $Id$ 
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ishwinde)
* @Pt.AcceptedRating Red (ishwinde)
*/

package ptolemy.homer.gui;

import java.awt.Checkbox;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.netbeans.api.visual.widget.Widget;

import ptolemy.homer.widgets.NamedObjectWidgetInterface;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// WidgetPropertiesFrame 

/** The property window for setting widget position and size.
 *  @author Ishwinder Singh
 *  @version $Id$ 
 *  @since Ptolemy II 8.1
 *  @Pt.ProposedRating Red (ahuseyno)
 *  @Pt.AcceptedRating Red (ahuseyno)
 */
public class WidgetPropertiesFrame extends JPanel {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create the window with spinners set.
     *  @param widget Widget whose properties are being displayed.
     */
    public WidgetPropertiesFrame(Widget widget) {
        setLayout(new GridLayout(3, 4, 10, 10));
        setLocation(500, 200);

        // Set up row #1.
        add(new JLabel("Height: "));
        add(_heightSpinner);
        add(new JLabel("X: "));
        add(_xSpinner);

        // Set up row #2.
        add(new JLabel("Width: "));
        add(_widthSpinner);
        add(new JLabel("Y: "));
        add(_ySpinner);

        // Set up row #3.
        add(new JLabel(""));
        add(_enabled);
        add(_required);
        add(new JLabel(""));

        _widget = widget;
        if (_widget instanceof NamedObjectWidgetInterface) {
            NamedObj namedObj = ((NamedObjectWidgetInterface) _widget)
                    .getPositionableElement().getElement();
            if (!(namedObj instanceof Settable)) {
                _enabled.setEnabled(false);
            }
        }

        _heightSpinner.setValue(widget.getPreferredBounds().getHeight());
        _widthSpinner.setValue(widget.getPreferredBounds().getWidth());
        _xSpinner.setValue(widget.getPreferredLocation().getX());
        _ySpinner.setValue(widget.getPreferredLocation().getY());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         Private Methods                   ////

    /** Get the specified position and dimension for the widget.
     *  @return The target size of the widget.
     */
    public Rectangle getBounds() {
        Rectangle bounds = null;
        try {
            Point position = new Point(
                    ((SpinnerNumberModel) _xSpinner.getModel()).getNumber()
                            .intValue(),
                    ((SpinnerNumberModel) _ySpinner.getModel()).getNumber()
                            .intValue());

            Dimension size = new Dimension(
                    ((SpinnerNumberModel) _widthSpinner.getModel()).getNumber()
                            .intValue(),
                    ((SpinnerNumberModel) _heightSpinner.getModel())
                            .getNumber().intValue());

            bounds = new Rectangle(position, size);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null,
                    "The specified values are invalid.", "Invalid Value(s)",
                    JOptionPane.ERROR_MESSAGE);
        }

        return bounds;
    }

    /** Get the enabled status of the widget.
     *  @return The enabled status of the widget.
     */
    public boolean getEnabled() {
        return _enabled.getState();
    }

    /** Get the required status of the widget.
     *  @return The required status of the widget.
     */
    public boolean getRequired() {
        return _required.getState();
    }

    /** Show the prompt and return the selection.
     *  @return The return value of the dialog.
     */
    public int showPrompt() {
        JOptionPane optionPane = new JOptionPane(this,
                JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
        JDialog dialog = optionPane.createDialog("Properties");
        dialog.pack();
        dialog.setVisible(true);
        dialog.setModal(true);

        if (optionPane.getValue() == null) {
            return (int) JOptionPane.CANCEL_OPTION;
        } else {
            return (Integer) optionPane.getValue();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** Height spinner UI element.
     */
    private final JSpinner _heightSpinner = new JSpinner(
            new SpinnerNumberModel(100, 1, 9999, 1));

    /** Width spinner UI element.
     */
    private final JSpinner _widthSpinner = new JSpinner(new SpinnerNumberModel(
            100, 1, 9999, 1));

    /** X position spinner UI element.
     */
    private final JSpinner _xSpinner = new JSpinner(new SpinnerNumberModel(100,
            0, 9999, 1));

    /** Y position spinner UI element.
     */
    private final JSpinner _ySpinner = new JSpinner(new SpinnerNumberModel(100,
            0, 9999, 1));

    /** Widget whose properties are being edited.
     */
    private final Widget _widget;

    /** Checkbox UI element to tell if the widget will be enabled.
     */
    private final Checkbox _enabled = new Checkbox("Enabled", true);

    /** Checkbox UI element to tell if the widget will be required.
     */
    private final Checkbox _required = new Checkbox("Required");
}
