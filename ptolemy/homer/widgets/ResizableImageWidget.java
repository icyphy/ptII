/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
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
package ptolemy.homer.widgets;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Rectangle;
import java.awt.image.ImageObserver;

import javax.swing.GrayFilter;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.ErrorManager;

/** A fork of ImageWidget with a fix to support resize-ability.
 *  Based on bug report http://netbeans.org/bugzilla/show_bug.cgi?id=170884
 *  and article http://java.dzone.com/news/how-add-resize-functionality-v
 *  A widget representing image. The origin of the widget is at its top-left corner.
 *  @author David Kaspar
@version $Id$
@since Ptolemy II 10.0
 *  @version $Id$
 */
public class ResizableImageWidget extends Widget {

    ///////////////////////////////////////////////////////////////////
    ////                         constructors                      ////

    /** Creates an image widget.
     *  @param scene The scene.
     */
    public ResizableImageWidget(Scene scene) {
        super(scene);
    }

    /** Creates an image widget.
     *  @param scene The scene.
     *  @param image The image.
     */
    public ResizableImageWidget(Scene scene, Image image) {
        super(scene);
        setImage(image);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Returns an image.
     *  @return The image.
     *  @see #setImage(Image)
     */
    public Image getImage() {
        return _image;
    }

    /** Returns whether the label is painted as disabled.
     *  @return True if the label is painted as disabled.
     */
    public boolean isPaintAsDisabled() {
        return _paintAsDisabled;
    }

    /** Sets an image.
     *  @param image The image
     *  @see #getImage()
     */
    public void setImage(Image image) {
        if (_image == image) {
            return;
        }

        setImageCore(image);
    }

    /** Sets whether the label is painted as disabled.
     *  @param paintAsDisabled If true, then the label is painted as disabled.
     */
    public void setPaintAsDisabled(boolean paintAsDisabled) {
        boolean repaint = _paintAsDisabled != paintAsDisabled;
        _paintAsDisabled = paintAsDisabled;

        if (repaint) {
            repaint();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Calculates a client area of the image.
     *  @return The calculated client area.
     */
    @Override
    protected Rectangle calculateClientArea() {
        if (_image != null) {
            return new Rectangle(0, 0, _width, _height);
        }

        return super.calculateClientArea();
    }

    /** Paints the image widget.
     */
    @Override
    protected void paintWidget() {
        if (_image == null) {
            return;
        }

        Graphics2D gr = getGraphics();
        if (_image != null) {
            Rectangle bounds = getBounds();
            if (_paintAsDisabled) {
                if (_disabledImage == null) {
                    _disabledImage = GrayFilter.createDisabledImage(_image);
                    MediaTracker tracker = new MediaTracker(
                            getScene().getView());
                    tracker.addImage(_disabledImage, 0);
                    try {
                        tracker.waitForAll();
                    } catch (InterruptedException e) {
                        ErrorManager.getDefault().notify(e);
                    }
                }
                gr.drawImage(_disabledImage, bounds.x, bounds.y,
                        bounds.x + bounds.width, bounds.y + bounds.height, 0, 0,
                        _width, _height, _observer);
            } else {
                gr.drawImage(_image, bounds.x, bounds.y,
                        bounds.x + bounds.width, bounds.y + bounds.height, 0, 0,
                        _width, _height, _observer);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected variables               ////

    /** The image observer.
     */
    protected ImageObserver _observer = new ImageObserver() {
        @Override
        public boolean imageUpdate(Image img, int infoflags, int x, int y,
                int width, int height) {
            setImageCore(_image);
            getScene().validate();

            return (infoflags
                    & (ImageObserver.ABORT | ImageObserver.ERROR)) == 0;
        }
    };

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Set the image core.
     *  @param image
     */
    private void setImageCore(Image image) {
        if (image == _image) {
            return;
        }

        int oldWidth = _width;
        int oldHeight = _height;

        _image = image;
        _disabledImage = null;
        _width = image != null ? image.getWidth(_observer) : 0;
        _height = image != null ? image.getHeight(_observer) : 0;

        if (oldWidth == _width && oldHeight == _height) {
            repaint();
        } else {
            revalidate();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    /** The disabled image.
     */
    private Image _disabledImage;

    /** The height of the image.
     */
    private int _height;

    /** The image.
     */
    private Image _image;

    /** Whether or not the image is painted as disabled.
     */
    private boolean _paintAsDisabled;

    /** The width of the image.
     */
    private int _width;
}
