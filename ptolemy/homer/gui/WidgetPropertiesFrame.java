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

import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.netbeans.api.visual.widget.Widget;

import ptolemy.homer.widgets.NamedObjectWidgetInterface;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

public class WidgetPropertiesFrame extends javax.swing.JFrame {

    /** Creates new form WidgetPropertiesFrame 
     * @param layeredPane LayerPane containing the widget
     * @param widget Widget*/

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    public WidgetPropertiesFrame(Widget widget) {
        _widget = widget;
        initComponents();
        _xTextField.setText(String
                .valueOf(widget.getPreferredLocation().getX()));
        _yTextField.setText(String
                .valueOf(widget.getPreferredLocation().getY()));
        _heightTextField.setText(String.valueOf(widget.getPreferredBounds()
                .getHeight()));
        _widthTextField.setText(String.valueOf(widget.getPreferredBounds()
                .getWidth()));

    }

    ///////////////////////////////////////////////////////////////////
    ////                         Private Methods                   ////

    /** Initialize Swing components.
     */
    private void initComponents() {

        _okButton = new javax.swing.JButton();
        _cancelButton = new javax.swing.JButton();
        _xLabel = new javax.swing.JLabel();
        _yLabel = new javax.swing.JLabel();
        _heightLabel = new javax.swing.JLabel();
        _widthLabel = new javax.swing.JLabel();
        _xTextField = new javax.swing.JTextField();
        _yTextField = new javax.swing.JTextField();
        _heightTextField = new javax.swing.JTextField();
        _widthTextField = new javax.swing.JTextField();
        _checkbox = new java.awt.Checkbox();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Widget Properties");

        _okButton.setText("Ok");
        _okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        _cancelButton.setText("Cancel");
        _cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        _xLabel.setText("X:");

        _yLabel.setText("Y:");

        _heightLabel.setText("Height:");

        _widthLabel.setText("Width:");

        _checkbox.setLabel("Disabled");
        if (_widget instanceof NamedObjectWidgetInterface) {
            _namedObj = ((NamedObjectWidgetInterface) _widget).getNamedObject();
            if (!(_namedObj instanceof Settable)) {
                _checkbox.disable();
            }

        }

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(
                getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        layout.createSequentialGroup()
                                .addGap(45, 45, 45)
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                .addGroup(
                                                        layout.createSequentialGroup()
                                                                .addGroup(
                                                                        layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                .addGroup(
                                                                                        layout.createSequentialGroup()
                                                                                                .addGroup(
                                                                                                        layout.createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.TRAILING)
                                                                                                                .addComponent(
                                                                                                                        _xLabel)
                                                                                                                .addComponent(
                                                                                                                        _heightLabel))
                                                                                                .addGap(18,
                                                                                                        18,
                                                                                                        18)
                                                                                                .addGroup(
                                                                                                        layout.createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                false)
                                                                                                                .addComponent(
                                                                                                                        _xTextField)
                                                                                                                .addComponent(
                                                                                                                        _heightTextField,
                                                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                        53,
                                                                                                                        Short.MAX_VALUE)))
                                                                                .addComponent(
                                                                                        _checkbox,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                                .addComponent(_okButton))
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(
                                                        layout.createSequentialGroup()
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(
                                                                        layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                false)
                                                                                .addGroup(
                                                                                        layout.createSequentialGroup()
                                                                                                .addGap(37,
                                                                                                        37,
                                                                                                        37)
                                                                                                .addComponent(
                                                                                                        _yLabel)
                                                                                                .addGap(18,
                                                                                                        18,
                                                                                                        18))
                                                                                .addGroup(
                                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                        layout.createSequentialGroup()
                                                                                                .addComponent(
                                                                                                        _widthLabel)
                                                                                                .addGap(10,
                                                                                                        10,
                                                                                                        10)))
                                                                .addGroup(
                                                                        layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(
                                                                                        _widthTextField,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        56,
                                                                                        Short.MAX_VALUE)
                                                                                .addComponent(
                                                                                        _yTextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        56,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGroup(
                                                        layout.createSequentialGroup()
                                                                .addGap(44, 44,
                                                                        44)
                                                                .addComponent(
                                                                        _cancelButton)))
                                .addGap(61, 61, 61)));
        layout.setVerticalGroup(layout
                .createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(
                        layout.createSequentialGroup()
                                .addGap(27, 27, 27)
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(
                                                        _xTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(_xLabel)
                                                .addComponent(_yLabel)
                                                .addComponent(
                                                        _yTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        20,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(
                                                        _widthTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        20,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(_widthLabel)
                                                .addComponent(
                                                        _heightTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(_heightLabel))
                                .addGap(22, 22, 22)
                                .addComponent(_checkbox,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(_cancelButton)
                                                .addComponent(_okButton))
                                .addContainerGap(33, Short.MAX_VALUE)));
        setLocation(500, 200);

        pack();
    }

    /** Action for OK button. Updated the properties of the widget.
     *  @param evt Event object.
     */
    private void okButtonActionPerformed(ActionEvent evt) {
        double x, y;
        double height, width;

        try {
            x = Double.parseDouble(_xTextField.getText());
            y = Double.parseDouble(_yTextField.getText());
            height = Double.parseDouble(_heightTextField.getText());
            width = Double.parseDouble(_widthTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "You Entered Invalid vaules.",
                    "Invalid", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Rectangle rect = new Rectangle();
        rect.setRect(x, y, width, height);
        _widget.setPreferredBounds(rect);

        dispose();
    }

    /** Action for cancel button. Close the frame.
     *  @param evt Event object.
     */
    private void cancelButtonActionPerformed(ActionEvent evt) {
        dispose();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private final Widget _widget;
    private NamedObj _namedObj;
    private java.awt.Checkbox _checkbox;
    private javax.swing.JButton _okButton;
    private javax.swing.JButton _cancelButton;
    private javax.swing.JLabel _xLabel;
    private javax.swing.JLabel _yLabel;
    private javax.swing.JLabel _heightLabel;
    private javax.swing.JLabel _widthLabel;
    private javax.swing.JTextField _xTextField;
    private javax.swing.JTextField _yTextField;
    private javax.swing.JTextField _heightTextField;
    private javax.swing.JTextField _widthTextField;
    // End of variables declaration

}
