/*

 Copyright (c) 2005 The Regents of the University of California.
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
package ptolemy.backtrack.plugin.dialogs;

import org.eclipse.jface.resource.ImageDescriptor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ptolemy.backtrack.plugin.EclipsePlugin;

//////////////////////////////////////////////////////////////////////////
//// AboutDialog

/**


 @author Thomas Feng
 @version $Id$
 @since Ptolemy II 5.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class AboutDialog extends Dialog {
    Object result;

    public AboutDialog(Shell parent, int style) {
        super(parent, style);
    }

    public AboutDialog(Shell parent) {
        this(parent, 0); // your default style bits go here (not the Shell's style bits)
    }

    public Object open() {
        Shell parent = getParent();
        _shell = new Shell(parent, SWT.MODELESS | SWT.APPLICATION_MODAL);
        _shell.setText(getText());
        _shell.setLayout(new FillLayout());

        // Your code goes here (widget creation, set result, etc).
        Label logo = new Label(_shell, SWT.NULL);
        ImageDescriptor descriptor = EclipsePlugin
                .getImageDescriptor("ptolemy/backtrack/plugin/images/ptolemy.gif");
        Image image = descriptor.createImage();
        logo.setImage(image);

        int width = image.getBounds().width;
        int height = image.getBounds().height;
        Display display = parent.getDisplay();
        Rectangle displayBounds = display.getPrimaryMonitor().getBounds();
        Rectangle bounds = new Rectangle((displayBounds.width - width) / 2,
                ((displayBounds.height - height) / 2) - 80, // Place the dialog higher.
                width, height);
        _shell.setBounds(bounds);

        _shell.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
            }

            public void keyReleased(KeyEvent e) {
                if ((e.keyCode == SWT.CR) || (e.keyCode == SWT.ESC)) {
                    _shell.close();
                }
            }
        });
        logo.addMouseListener(new MouseListener() {
            public void mouseDoubleClick(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
            }

            public void mouseUp(MouseEvent e) {
                if (e.button == 1) {
                    _shell.close();
                }
            }
        });

        _shell.open();

        while (!_shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        return result;
    }

    private Shell _shell;
}
