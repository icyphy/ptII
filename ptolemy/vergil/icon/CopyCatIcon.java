/* An icon that copies the icon of an entity with the same container.

 Copyright (c) 1999-2003 The Regents of the University of California.
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
@AcceptedRating Red (eal@eecs.berkeley.edu)
*/

package ptolemy.vergil.icon;

import java.awt.Color;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.SingletonConfigurableAttribute;
import ptolemy.kernel.util.Workspace;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.toolbox.BasicRectangle;

//////////////////////////////////////////////////////////////////////////
//// CopyCatIcon
/**
This is an icon that copies the icon of the last entity contained by
the same container, if there is one, and behaves like the base class
if not.

@author Edward A. Lee
@version $Id$
*/
public class CopyCatIcon extends XMLIcon {
    
    /** Create a new icon with the given name in the given container.
     *  @param container The container.
     *  @param name The name of the attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public CopyCatIcon(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Clone the object into the specified workspace. The new object is
     *  <i>not</i> added to the directory of that workspace (you must do this
     *  yourself if you want it there).
     *  The result is an object with no container.
     *  @param workspace The workspace for the cloned object.
     *  @exception CloneNotSupportedException Not thrown in this base class
     *  @return The new Attribute.
     */
    public Object clone(Workspace workspace)
            throws CloneNotSupportedException {
        CopyCatIcon newObject = (CopyCatIcon)super.clone(workspace);
        newObject._originalDescription = null;
        return newObject;
    }
    
    /** Create a new background figure.  This method looks for entities
     *  contained by the same container, and if there are any, copies
     *  the icon of the last such entity.  If there are none, then it
     *  behaves like the base class.
     *  @return A new figure.
     */
    public Figure createBackgroundFigure() {
        
        Figure result = null;        
        Nameable container = getContainer();
        if (container instanceof CompositeEntity) {
            CompositeEntity myContainer = ((CompositeEntity)container);
            ComponentEntity entity = null;
            Iterator entities
                    = myContainer.entityList().iterator();
            while (entities.hasNext()) {
                entity = (ComponentEntity)entities.next();
            }
            try {
                if (entity != null) {
                    // Look for an icon within the entity.
                    EditorIcon icon = null;
                    Iterator icons = entity.attributeList(EditorIcon.class).iterator();
                    while (icons.hasNext()) {
                        icon = (EditorIcon)icons.next();
                    }
                    if (icon != null) {
                        result = icon.createBackgroundFigure();
                    } else {
                        // If there is no icon, then maybe there is an
                        // _iconDescription attribute.
                        SingletonConfigurableAttribute description
                                = (SingletonConfigurableAttribute)
                                entity.getAttribute("_iconDescription",
                                SingletonConfigurableAttribute.class);
                        if (description != null) {
                            // Look for an icon description in my container.
                            SingletonConfigurableAttribute myDescription
                                    = (SingletonConfigurableAttribute)
                                    myContainer.getAttribute("_iconDescription",
                                    SingletonConfigurableAttribute.class);
                            if (myDescription != null) {
                                // Save my original description, in case I go
                                // back to having nothing inside.
                                if (_originalDescription == null) {
                                    _originalDescription = myDescription.getText();
                                }
                                myDescription.configure(
                                        null,
                                        null,
                                        description.getText());
                            }
                        }
                    }
                } else {
                    // Restore the original description if we don't have
                    // one now.
                    if (result == null && _originalDescription != null) {
                        // Restore the original icon description.
                        // Look for an icon description in my container.
                        SingletonConfigurableAttribute myDescription
                                = (SingletonConfigurableAttribute)
                                myContainer.getAttribute("_iconDescription",
                                SingletonConfigurableAttribute.class);
                        if (myDescription != null) {
                            myDescription.configure(
                                    null,
                                    null,
                                    _originalDescription);
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore and use default icon.
            }
        }
        // If all else fails, behave like the superclass.
        if (result == null) {
            result = super.createBackgroundFigure();
        }
        // Wrap in a CompositeFigure with echos of the bounding box.
        // Note that the bounds here are actually bigger than the
        // bounding box, which may be OK in this case.
        Rectangle2D bounds = result.getBounds();
        CompositeFigure composite = new CompositeFigure();
        BasicRectangle rectangle = new BasicRectangle(
                bounds.getX() + 10.0,
                bounds.getY() + 10.0,
                bounds.getWidth(),
                bounds.getHeight(),
                Color.white);
        composite.add(rectangle);
        BasicRectangle rectangle2 = new BasicRectangle(
                bounds.getX() + 5.0,
                bounds.getY() + 5.0,
                bounds.getWidth(),
                bounds.getHeight(),
                Color.white);
        composite.add(rectangle2);
        BasicRectangle rectangle3 = new BasicRectangle(
                bounds.getX(),
                bounds.getY(),
                bounds.getWidth(),
                bounds.getHeight(),
                Color.white);
        composite.add(rectangle3);
        composite.add(result);
        return composite;
    }
    
    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    // Original description of the icon.
    private String _originalDescription = null;
}
