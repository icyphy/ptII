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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.netbeans.api.visual.widget.Widget;

public class WidgetPropertiesFrame extends javax.swing.JFrame {

    /** Creates new form WidgetPropertiesFrame 
     * @param layeredPane LayerPane containing the widget
     * @param widget Widget*/

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    public WidgetPropertiesFrame(Widget widget) {
        initComponents();
        xTextField.setText(String.valueOf(widget.getPreferredBounds().getX()));
        yTextField.setText(String.valueOf(widget.getPreferredBounds().getY()));
        heightTextField.setText(String.valueOf(widget.getPreferredBounds()
                .getHeight()));
        widthTextField.setText(String.valueOf(widget.getPreferredBounds()
                .getWidth()));
        _widget = widget;

    }

    ///////////////////////////////////////////////////////////////////
    ////                         Private Methods                   ////

    /** Initialize Swing components.
     */
    private void initComponents() {

        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        xLabel = new javax.swing.JLabel();
        yLabel = new javax.swing.JLabel();
        heightLabel = new javax.swing.JLabel();
        widthLabel = new javax.swing.JLabel();
        xTextField = new javax.swing.JTextField();
        yTextField = new javax.swing.JTextField();
        heightTextField = new javax.swing.JTextField();
        widthTextField = new javax.swing.JTextField();
        checkbox = new java.awt.Checkbox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Widget Properties");

        okButton.setText("Ok");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        xLabel.setText("X:");

        yLabel.setText("Y:");

        heightLabel.setText("Height:");

        widthLabel.setText("Width:");

        checkbox.setLabel("Disabled");

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
                                                                                                                        xLabel)
                                                                                                                .addComponent(
                                                                                                                        heightLabel))
                                                                                                .addGap(18,
                                                                                                        18,
                                                                                                        18)
                                                                                                .addGroup(
                                                                                                        layout.createParallelGroup(
                                                                                                                javax.swing.GroupLayout.Alignment.LEADING,
                                                                                                                false)
                                                                                                                .addComponent(
                                                                                                                        xTextField)
                                                                                                                .addComponent(
                                                                                                                        heightTextField,
                                                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                                                        53,
                                                                                                                        Short.MAX_VALUE)))
                                                                                .addComponent(
                                                                                        checkbox,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                                                .addPreferredGap(
                                                                        javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                                                .addComponent(okButton))
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
                                                                                                        yLabel)
                                                                                                .addGap(18,
                                                                                                        18,
                                                                                                        18))
                                                                                .addGroup(
                                                                                        javax.swing.GroupLayout.Alignment.TRAILING,
                                                                                        layout.createSequentialGroup()
                                                                                                .addComponent(
                                                                                                        widthLabel)
                                                                                                .addGap(10,
                                                                                                        10,
                                                                                                        10)))
                                                                .addGroup(
                                                                        layout.createParallelGroup(
                                                                                javax.swing.GroupLayout.Alignment.LEADING)
                                                                                .addComponent(
                                                                                        widthTextField,
                                                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                                                        56,
                                                                                        Short.MAX_VALUE)
                                                                                .addComponent(
                                                                                        yTextField,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                                                        56,
                                                                                        javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGroup(
                                                        layout.createSequentialGroup()
                                                                .addGap(44, 44,
                                                                        44)
                                                                .addComponent(
                                                                        cancelButton)))
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
                                                        xTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(xLabel)
                                                .addComponent(yLabel)
                                                .addComponent(
                                                        yTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        20,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(
                                        javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(
                                                        widthTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        20,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(widthLabel)
                                                .addComponent(
                                                        heightTextField,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addComponent(heightLabel))
                                .addGap(22, 22, 22)
                                .addComponent(checkbox,
                                        javax.swing.GroupLayout.PREFERRED_SIZE,
                                        javax.swing.GroupLayout.DEFAULT_SIZE,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(24, 24, 24)
                                .addGroup(
                                        layout.createParallelGroup(
                                                javax.swing.GroupLayout.Alignment.BASELINE)
                                                .addComponent(cancelButton)
                                                .addComponent(okButton))
                                .addContainerGap(33, Short.MAX_VALUE)));
        setLocation(500, 200);

        pack();
    }

    /** Action for OK button. Updated the properties of the widget.
     *  @param evt Event object.
     */
    private void okButtonActionPerformed(ActionEvent evt) {
        int x, y;

        try {
            x = Integer.parseInt(xTextField.getText());

            y = Integer.parseInt(yTextField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "You Entered Invalid vaules.",
                    "Invalid", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // _widget.setPreferredLocation(new Point(x, y));
        _widget.setPreferredBounds(new Rectangle(new Point(x, y),
                new Dimension(200, 500)));

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
    private java.awt.Checkbox checkbox;
    private javax.swing.JButton okButton;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel xLabel;
    private javax.swing.JLabel yLabel;
    private javax.swing.JLabel heightLabel;
    private javax.swing.JLabel widthLabel;
    private javax.swing.JTextField xTextField;
    private javax.swing.JTextField yTextField;
    private javax.swing.JTextField heightTextField;
    private javax.swing.JTextField widthTextField;
    // End of variables declaration

}
