/* An attribute with a reference to a rectangle.

Copyright (c) 2003-2004 The Regents of the University of California.
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

package ptolemy.vergil.kernel.attributes;

import ptolemy.data.IntToken;
import ptolemy.data.expr.ModelScope;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.BaseType;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// AttributeExpressionAttribute
/**
   This is a text attribute whose text string is derived from the
   expression of a parameter.  <p>
   @author Steve Neuendorffer
   @version $Id$
   @since Ptolemy II 4.0
   @Pt.ProposedRating Yellow (eal)
   @Pt.AcceptedRating Red (cxh)
*/
public class AttributeExpressionAttribute extends AbstractTextAttribute {

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
    public AttributeExpressionAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        attributeName = new StringAttribute(this, "attributeName");
        displayWidth = new Parameter(this, "displayWidth");
        displayWidth.setExpression("6");
        displayWidth.setTypeEquals(BaseType.INT);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the attribute of the container whose value to display. */
    public StringAttribute attributeName;

    /** The number of characters to display. This is an integer, with
     *  default value 6.
     */
    public Parameter displayWidth;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** React to a changes in the attributes by changing
     *  the icon.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If the change is not acceptable
     *   to this container (should not be thrown).
     */
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        if (attribute == attributeName) {
            _attributeName = attributeName.getExpression();
            _icon.setText(_getText());
        } else if(attribute == displayWidth) {
            _displayWidth =
                ((IntToken) displayWidth.getToken()).intValue();
            _icon.setText(_getText());
        } else {
            super.attributeChanged(attribute);
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    /** Return the a new string that contains the expression of the
     *  referred to attribute.
     *  @return A new shape.
     */
    protected String _getText() {
        NamedObj container = (NamedObj)getContainer();
        if (container != null) {
            Attribute associatedAttribute = 
                ModelScope.getScopedVariable(null, container, _attributeName);
            if (associatedAttribute instanceof Settable) {
                String value = ((Settable)associatedAttribute).getExpression();
                String truncated = value;
                int width = _displayWidth;
                if (value.length() > width) {
                    truncated = value.substring(0, width) + "...";
                }
                if (truncated.length() == 0) {
                    truncated = " ";
                }
                return truncated;
            }
        }
        return "???";
    }

    ///////////////////////////////////////////////////////////////////
    ////                        protected members                  ////

    /** Most recent value of the rounding parameter. */
    protected int _displayWidth = 0;
    protected String _attributeName = "";
}
