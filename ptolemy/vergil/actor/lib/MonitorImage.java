/* Display image inputs in the icon.

 Copyright (c) 1998-2014 The Regents of the University of California.
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
package ptolemy.vergil.actor.lib;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import ptolemy.actor.lib.Sink;
import ptolemy.data.ImageToken;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Workspace;
import ptolemy.vergil.icon.ImageIcon;

///////////////////////////////////////////////////////////////////
//// MonitorImage

/**
 Display image inputs in the icon.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 0.3
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (bilung)
 */
public class MonitorImage extends Sink {
    /** Construct an actor.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the entity cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public MonitorImage(CompositeEntity container, String name)
            throws NameDuplicationException, IllegalActionException {
        super(container, name);

        input.setTypeEquals(BaseType.OBJECT);

        _icon = new ImageIcon(this, "_icon");
        _icon.setPersistent(false);
        FileParameter source = new FileParameter(this, "source");
        source.setExpression("$CLASSPATH/ptolemy/vergil/kernel/attributes/ptIIplanetIcon.gif");
        URL url = source.asURL();
        Toolkit tk = Toolkit.getDefaultToolkit();
        Image image = tk.getImage(url);
        _icon.setImage(image);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the actor into the specified workspace.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    @Override
    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        MonitorImage newObject = (MonitorImage) super.clone(workspace);
        newObject._icon = (ImageIcon) newObject.getAttribute("_icon");
        return newObject;
    }

    /** Read at most one token from the input and record its value.
     *  @exception IllegalActionException If the input token does not
     *   contain an image, or if there is no director.
     *  @return True.
     */
    @Override
    public boolean postfire() throws IllegalActionException {
        if (input.hasToken(0)) {
            ImageToken token = (ImageToken) input.get(0);
            Image value = token.asAWTImage();
            _icon.setImage(value);
        }

        return super.postfire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////
    // The image icon.
    private ImageIcon _icon;
}
