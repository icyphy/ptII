/* A tableau factory that opens a contained component.

 Copyright (c) 1997-2014 The Regents of the University of California.
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
package ptolemy.actor.gui;

import java.util.List;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

///////////////////////////////////////////////////////////////////
//// LevelSkippingTableauFactory

/**
 This class is an attribute that creates a tableau to view an object
 contained by the model associated with the specified effigy.
 When a model is opened, this object looks for a contained entity
 with the name given by <i>entityName</i>, or looks for the first
 contained entity if no name is given, and opens that entity rather
 than the model associated with the specified effigy.

 @author Edward A. Lee
 @version $Id$
 @since Ptolemy II 1.0
 @Pt.ProposedRating Yellow (eal)
 @Pt.AcceptedRating Red (eal)
 */
public class LevelSkippingTableauFactory extends TableauFactory {
    /** Create a factory with the given name and container.
     *  @param container The container.
     *  @param name The name.
     *  @exception IllegalActionException If the container is incompatible
     *   with this attribute.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public LevelSkippingTableauFactory(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        entityName = new StringParameter(this, "entityName");
        entityName.setExpression("");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         parameters                        ////

    /** The name of the contained entity to open, or an empty string
     *  to just open the first one found.  This is a string that defaults
     *  to empty.
     */
    public StringParameter entityName;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Create a tableau for the specified effigy by identifying an
     *  object contained by the specified effigy as given by <i>entityName</i>,
     *  or the first entity contained by that object if no <i>entityName</i>
     *  is given.  If the specified effigy is not an instance of PtolemyEffigy,
     *  this simply return null. If the model associated with the effigy
     *  does not contain the specified entity, then also return null.
     *  @param effigy The model effigy.
     *  @return A tableau for the effigy, or null if one cannot be created.
     *  @exception Exception If the factory should be able to create a
     *   Tableau for the effigy, but something goes wrong.
     */
    @Override
    public Tableau createTableau(Effigy effigy) throws Exception {
        if (!(effigy instanceof PtolemyEffigy)) {
            return null;
        }

        NamedObj model = ((PtolemyEffigy) effigy).getModel();

        if (model instanceof CompositeEntity) {
            String name = entityName.stringValue();
            NamedObj toOpen = null;

            if (!name.trim().equals("")) {
                toOpen = ((CompositeEntity) model).getEntity(name);
            } else {
                List entities = ((CompositeEntity) model).entityList();

                if (entities.size() > 0) {
                    toOpen = (NamedObj) entities.get(0);
                }
            }

            if (toOpen != null) {
                Configuration configuration = (Configuration) effigy.toplevel();
                return configuration.openModel(toOpen);
            }
        }

        return null;
    }
}
