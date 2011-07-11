/* Mouse listener for widgets.
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
////WidgetMouseListener

/**
* Mouse listener for widgets.
* @author Ishwinder Singh
* @version $Id$ 
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ishwinde)
* @Pt.AcceptedRating Red (ishwinde)
*/

package ptolemy.homer.widgets;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.netbeans.api.visual.widget.Widget;

import ptolemy.homer.gui.WidgetPropertiesFrame;

public class WidgetMouseListener implements MouseListener {

    ///////////////////////////////////////////////////////////////////
    ////                         constructor                       ////

    /** Create a new Mouse listener for the the widget.
     * @param layeredPane LayeredPane of the widget
     * @param widget  Widget
     * */

    public WidgetMouseListener(PortablePlaceableWidget portablePlaceableWidget) {
        _widget = portablePlaceableWidget;
    }

    /** Action for mouse clicked event. Open the properties frame for the
     *  widget clicked. 
     * @param e Event object.
     * */
    public void mouseClicked(MouseEvent e) {

        if (e.getClickCount() == 2) {
            //            System.out.println(e.getSource().getClass());
            //            JPanel glassPane = (JPanel) e.getSource();
            //            System.out.println(glassPane.getParent().getClass());
            //
            //            JLayeredPane layeredPane = (JLayeredPane) glassPane.getParent();
            //
            //            layeredPane.repaint();

            _widget.revalidate();

            // layeredPane.getParent().repaint();

            new WidgetPropertiesFrame(_widget).setVisible(true);
        }

    }

    /** Action for mouse pressed event.
     * @param e Event object.
     * */
    public void mousePressed(MouseEvent e) {
    }

    /** Action for mouse released event. 
     * @param e Event object.
     * */
    public void mouseReleased(MouseEvent e) {

    }

    /** Action for mouse entered event. 
     * @param e Event object.
     * */
    public void mouseEntered(MouseEvent e) {

    }

    /** Action for mouse clicked event. 
     * @param e Event object.
     * */
    public void mouseExited(MouseEvent e) {

    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    //Widget of this listener
    private final Widget _widget;

}
