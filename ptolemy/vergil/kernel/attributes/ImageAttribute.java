/* An attribute with a reference to an image.

 Copyright (c) 2001-2003 The Regents of the University of California.
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

@ProposedRating Yellow (eal@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.vergil.kernel.attributes;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.URL;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.attributes.FileAttribute;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.vergil.icon.ImageIcon;
import ptolemy.vergil.icon.ResizableAttributeControllerFactory;

//////////////////////////////////////////////////////////////////////////
//// ImageAttribute
/**
This is an attribute that is rendered as an image.  Its <i>source</i>
parameter specifies a file containing an image (GIF, JPEG, etc.), and
its <i>scale</i> attribute specifies a scaling factor, as a percentage.
<p>
@author Edward A. Lee and Steve Neuendorffer
@version $Id$
*/
public class ImageAttribute extends Attribute {

    /** Construct an attribute with the given name contained by the
     *  specified container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public ImageAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        // Hide the name.
        new Attribute(this, "_hideName");

        _icon = new ImageIcon(this, "_icon");
        
        source = new FileAttribute(this, "source");
        source.setExpression("$CLASSPATH/doc/img/ptIIplanetIcon.gif");
        
        scale = new Parameter(this, "scale");
        scale.setTypeEquals(BaseType.DOUBLE);
        scale.setExpression("100.0");
        
        // Create a custom controller.
        new ResizableAttributeControllerFactory(this, "_controllerFactory");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The scale, as a percentage.
     * This is a double that defaults to 100.0.
     */
    public Parameter scale;
     
    /** The source image file. This is a file name or URL, where the default
     *  is "$CLASSPATH/doc/img/ptIIplanetIcon.gif".
     */
    public FileAttribute source;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a change in the source or scale attributes by changing
     *  the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == source) {
            URL url = source.asURL();
            Toolkit tk = Toolkit.getDefaultToolkit();
            Image image = tk.getImage(url);
            _icon.setImage(image);
        } else if (attribute == scale) {      
            double scaleValue = ((DoubleToken)scale.getToken()).doubleValue();
            _icon.scaleImage(scaleValue);
        } else {
            super.attributeChanged(attribute);
        }
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // The image icon.
    private ImageIcon _icon;
}
