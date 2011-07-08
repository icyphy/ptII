/*  Represents a widget for attributes that are in the form of checkbox
 
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

import javax.swing.JCheckBox;

import org.netbeans.api.visual.widget.Scene;

import ptolemy.kernel.util.NamedObj;

/**
* Represents a widget for attributes that are in the form of checkbox
* @author Ishwinder Singh
* @version $Id$ 
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ishwinde)
* @Pt.AcceptedRating Red (ishwinde)
*/

public class CheckBoxWedget extends NamedObjectWidget {

    ///////////////////////////////////////////////////////////////////
    ////                  constructor                              ////

    /** Create an instance of the CheckBox widget on the 
     *  @param scene The scene on whci the widget is to be drawn.
     *  @param namedObject The NamedObj object representing this widget.
     */
    public CheckBoxWedget(Scene scene, NamedObj namedObject) {
        super(scene, namedObject);
    }

    ///////////////////////////////////////////////////////////////////
    ////                  Public Method                            ////

    /** Returns the Checkbox widget.
     */
    public JCheckBox getJCheckBox() {
        return box;
    }

    ///////////////////////////////////////////////////////////////////
    ////                  Protected  Method                        ////

    /** Draws the widget on the scene.
     */
    protected Rectangle calculateClientArea() {
        return new Rectangle(box.getPreferredSize());
    }

    /** Draws the widget on the scene.
     */
    protected void paintWidget() {
        box.setSize(getBounds().getSize());
        box.paint(getGraphics());
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Contained JCheckBox object
    private final JCheckBox box = new JCheckBox();

}
