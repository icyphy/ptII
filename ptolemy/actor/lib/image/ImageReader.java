/* An actor that reads an image from a URL parameter and Object Token
that contains a java.awt.Image

@Copyright (c) 2001-2003 The Regents of the University of California.
All rights reserved.

Permission is hereby granted, without written agreement and without
license or royalty fees, to use, copy, modify, and distribute this
software and its documentation for any purpose, provided that the
above copyright notice and the following two paragraphs appear in all
copies of this software.

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

                                                PT_COPYRIGHT_VERSION 2
                                                COPYRIGHTENDKEY
@ProposedRating Red (cxh@eecs.berkeley.edu)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.lib.image;

import ptolemy.actor.lib.Source;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.*;
import ptolemy.data.Token;
import ptolemy.data.AWTImageToken;
import ptolemy.data.StringToken;
import ptolemy.data.type.BaseType;
import ptolemy.data.expr.Parameter;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.ImageIcon;

//////////////////////////////////////////////////////////////////////////
//// ImageReader
/**
This actor reads an Image from a URL parameter, and outputs it as an Object
Token

<p>It is possible to load a file
from the local file system by using the prefix "file://" instead of
"http://". Relative file paths are allowed. To specify a file
relative to the current directory, use "../" or "./". For example,
if the current directory contains a file called "test.jpg", then
<i>sourceURL</i> should be set to "file:./test.jpg". If the parent
directory contains a file called "test.jpg", then <i>sourceURL</i>
should be set to "file:../test.jpg". To reference the file
test.jpg, located at "/tmp/test.jpg", <i>sourceURL</i>
should be set to "file:///tmp/test.jpg" The default value is
"file:///tmp/test.jpg".

<p>FIXME: It would be nice if we could read images from stdin.

@see URLToImage
@author  Christopher Hylands
@version $Id$
*/
public class ImageReader extends Source {
    // We don't extend ptolemy.actor.lib.Reader because we are not
    // reading in data by columns.  Probably this class and
    // ptolemy.actor.lib.Reader should extend a common base class?

    /** Construct an actor with the given container and name.
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the actor cannot be contained
     *   by the proposed container.
     *  @exception NameDuplicationException If the container already has an
     *   actor with this name.
     */
    public ImageReader(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // Set the type of the output port.
        //output.setMultiport(true);
        output.setTypeEquals(BaseType.OBJECT);

        sourceURL = new Parameter(this, "sourceURL", new StringToken(""));
        sourceURL.setTypeEquals(BaseType.STRING);
    }


    ///////////////////////////////////////////////////////////////////
    ////                     ports and parameters                  ////

    /** The URL of the file to read from. This parameter contains
     *  a StringToken.  By default, it contains an empty string, which
     *  is interpreted to mean that input should be directed to the
     *  standard input.
     *  FIXME: Should this bring up a dialog box to type (or select) a URL?
     */
    public Parameter sourceURL;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** If the specified attribute is <i>URL</i>, then close
     *  the current file (if there is one) and open the new one.
     *  @param attribute The attribute that has changed.
     *  @exception IllegalActionException If the specified attribute
     *   is <i>URL</i> and the file cannot be opened.
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == sourceURL) {
            // Would it be worth checking to see if the URL exists and
            // is readable?
            StringToken URLToken = (StringToken)sourceURL.getToken();
        }
        super.attributeChanged(attribute);
    }

    /** Clone the actor into the specified workspace. This calls the
     *  base class and then set the filename public member.
     *  @param workspace The workspace for the new object.
     *  @return A new actor.
     *  @exception CloneNotSupportedException If a derived class contains
     *   an attribute that cannot be cloned.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        ImageReader newObject = (ImageReader)super.clone(workspace);
        //newObject.output.setMultiport(true);
        return newObject;
    }

    /** Output the data read in the prefire.
     *  @exception IllegalActionException If there's no director.
     */
    public void fire() throws IllegalActionException {
        super.fire();
        output.broadcast(new AWTImageToken(_image));
    }

    /** Open the file at the URL, and set the width of the output.
     */
    public void initialize() throws IllegalActionException {
        attributeChanged(sourceURL);
    }

    /** Read in an image.
     *  @exception IllegalActionException If an IO error occurs.
     */
    public boolean prefire() throws IllegalActionException {
        StringToken URLToken = (StringToken)sourceURL.getToken();
        if (URLToken == null) {
            throw new IllegalActionException("sourceURL was null");
        }
        _image = new ImageIcon(URLToken.stringValue()).getImage();
        return super.prefire();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private members                   ////

    // String for the URL.
    private String _source;

    // Image that is read in.
    private Image _image;
}
