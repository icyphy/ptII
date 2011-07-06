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

import java.awt.Color;

import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;

import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
////EditTextWidget

/**
* TODO
* @author Ishwinder Singh
* @version $Id$ 
* @since Ptolemy II 8.1
* @Pt.ProposedRating Red (ishwinde)
* @Pt.AcceptedRating Red (ishwinde)
*/
public class EditTextWidget extends NamedObjectWidget {

    public EditTextWidget(Scene scene, NamedObj namedObject) {
        super(scene, namedObject);
        LabelWidget labelWidget = new LabelWidget(scene);
        labelWidget.setOpaque(true);
        labelWidget.setBackground(Color.WHITE);
        labelWidget.setCheckClipping(true);
        labelWidget.setAlignment(LabelWidget.Alignment.CENTER);
        labelWidget.setVerticalAlignment(LabelWidget.VerticalAlignment.CENTER);
        addChild(labelWidget);
        if (namedObject instanceof Settable) {
            labelWidget.setLabel(((Settable) namedObject).getExpression());
        }
    }

}
