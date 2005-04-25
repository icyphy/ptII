/*
 *	%Z%%M% %I% %E% %U%
 *
 * Copyright (c) 1996-2000 Sun Microsystems, Inc. All Rights Reserved.
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package vendors.vr;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class AnnotationsEditor extends JPanel implements ItemListener,
	ActionListener {
    VolRend volRend;
    AnnotationsEditor(VolRend volRend) {
	this.volRend = volRend;
	setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	JPanel boxPanel = new JPanel();
	boxPanel.setLayout(new BoxLayout(boxPanel, BoxLayout.Y_AXIS));

	boxPanel.add(new JLabel("Outline Boxes"));
	AttrComponent px =
			new JPanelToggle(this, boxPanel, volRend.plusXBoxAttr);
	AttrComponent py =
			new JPanelToggle(this, boxPanel, volRend.plusYBoxAttr);
	AttrComponent pz =
			new JPanelToggle(this, boxPanel, volRend.plusZBoxAttr);
	AttrComponent mx =
			new JPanelToggle(this, boxPanel, volRend.minusXBoxAttr);
	AttrComponent my =
			new JPanelToggle(this, boxPanel, volRend.minusYBoxAttr);
	AttrComponent mz =
			new JPanelToggle(this, boxPanel, volRend.minusZBoxAttr);
	add(boxPanel);

	JPanel imagePanel = new JPanel();
	imagePanel.setLayout(new BoxLayout(imagePanel, BoxLayout.Y_AXIS));

	imagePanel.add(new JLabel("Face Images"));
	AttrComponent pxi =
		    new JPanelString(this, imagePanel, volRend.plusXImageAttr);
	AttrComponent pyi =
		    new JPanelString(this, imagePanel, volRend.plusYImageAttr);
	AttrComponent pzi =
		    new JPanelString(this, imagePanel, volRend.plusZImageAttr);
	AttrComponent mxi =
		    new JPanelString(this, imagePanel, volRend.minusXImageAttr);
	AttrComponent myi =
		    new JPanelString(this, imagePanel, volRend.minusYImageAttr);
	AttrComponent mzi =
		    new JPanelString(this, imagePanel, volRend.minusZImageAttr);
	add(imagePanel);
    }

    public void itemStateChanged(ItemEvent e) {
        String name = ((Component)e.getItemSelectable()).getName();
        boolean value = (e.getStateChange() == ItemEvent.SELECTED);
        ToggleAttr attr = (ToggleAttr) volRend.context.getAttr(name);
        attr.set(value);
        volRend.update();
    }
    public void actionPerformed(ActionEvent e) {
        String name = ((Component)e.getSource()).getName();
        String value = e.getActionCommand();
        StringAttr attr = (StringAttr) volRend.context.getAttr(name);
        attr.set(value);
        volRend.update();
    }

}
