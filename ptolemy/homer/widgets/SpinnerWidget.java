/* TODO
 Copyright (c) 2011 The Regents of the University of California.
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
package ptolemy.homer.widgets;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComboBox;

import org.netbeans.api.visual.widget.Scene;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class SpinnerWidget extends NamedObjectIconWidget {

    public SpinnerWidget(Scene scene, NamedObj namedObject)
            throws NameDuplicationException, IllegalActionException {
        super(scene, namedObject);

        try {
            BufferedImage img = ImageIO.read(new File("J:/0026.jpg"));

            setImage(img);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //  ImageWidget spinnerImg = new ImageWidget(scene);

        //        try {
        //            spinnerImg.setImage(ImageIO.read(new File("J:/spinner.png")));
        //        } catch (IOException ex) {
        //
        //        }
        //
        //        addChild(spinnerImg);
    }

    public JComboBox getJComboBox() {
        return box;
    }

    protected Rectangle calculateClientArea() {
        return new Rectangle(box.getPreferredSize());
    }

    protected void paintWidget() {
        box.setSize(getBounds().getSize());
        box.paint(getGraphics());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private final JComboBox box = new JComboBox();

}
