/* An attribute for specifying that a parameter is edited with a
TextArea (multi-line).

 Copyright (c) 2002-2003 The Regents of the University of California.
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

@ProposedRating Red (zkemenczy@rim.net)
@AcceptedRating Red (cxh@eecs.berkeley.edu)
*/

package ptolemy.actor.gui.style;

import ptolemy.actor.gui.PtolemyQuery;
import ptolemy.data.IntToken;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;

//////////////////////////////////////////////////////////////////////////
//// TextStyle
/**
This attribute annotates user settable attributes to specify an
arbitrary multi-line text area style for configuring the containing
attribute.  This style can be used with any Settable attribute.

@see ptolemy.actor.gui.EditorPaneFactory
@see ParameterEditorStyle
@author Zoltan Kemenczy, Research in Motion Ltd
@version $Id$
@since Ptolemy II 2.1
*/

public class TextStyle extends ParameterEditorStyle {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name. This constructor is for testing only.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public TextStyle() {
        super();
    }

    /** Construct an attribute with the specified container and name.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable attribute for the container, or if the container
     *   is not an instance of Settable.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public TextStyle(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        
        height = new Parameter(this, "height");
        height.setToken("10");
        height.setTypeEquals(BaseType.INT);

        width = new Parameter(this, "width");
        width.setToken("30");
        width.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The height (in lines) of the text box. This is an integer
     *  that defaults to 10.
     */
    public Parameter height;

    /** The width (in lines) of the text box. This is an integer
     *  that defaults to 30.
     */
    public Parameter width;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return true if this style is acceptable for the given parameter.
     *  @param param The attribute that this annotates.
     *  @return True.
     */
    public boolean acceptable(Settable param) {
        return true;
    }

    /** Create a new type-in line
     *  entry in the given query associated with the
     *  attribute containing this style.  The name of the entry is
     *  the name of the attribute.  Attach the attribute to the created entry.
     *
     *  @param query The query into which to add the entry.
     */
    public void addEntry(PtolemyQuery query) {
        Settable container = (Settable)getContainer();
        String name = container.getName();
        String defaultValue = "";
        defaultValue = container.getExpression();
        try {
            int heightValue = ((IntToken)height.getToken()).intValue();
            int widthValue = ((IntToken)width.getToken()).intValue();
            query.addTextArea(
                    name, 
                    name, 
                    defaultValue,
                    PtolemyQuery.preferredBackgroundColor(container),
                    heightValue,
                    widthValue);
            query.attachParameter(container, name);
        } catch (IllegalActionException e) {
            throw new InternalErrorException(e);
        }
    }
}
