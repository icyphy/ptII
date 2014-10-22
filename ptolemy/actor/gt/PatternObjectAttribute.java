/* An attribute to record the name of the object in the pattern that corresponds
   to the container of the attribute in the replacement.

@Copyright (c) 2007-2014 The Regents of the University of California.
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

                       PT_COPYRIGHT_VERSION_2
                       COPYRIGHTENDKEY



 */

package ptolemy.actor.gt;

import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.InternalErrorException;
import ptolemy.kernel.util.KernelException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Settable;
import ptolemy.kernel.util.StringAttribute;
import ptolemy.kernel.util.ValueListener;
import ptolemy.kernel.util.Workspace;

/**
 An attribute to record the name of the object in the pattern that corresponds
 to the container of the attribute in the replacement. This attribute is usually
 created by the visual editor for TransformationRules. It may also be edited
 manually in a table in the editor.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Yellow (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PatternObjectAttribute extends StringAttribute implements
        ValueListener {

    /** Construct an attribute in the default workspace with an empty string
     *  as its name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     */
    public PatternObjectAttribute() {
        _init();
    }

    /** Construct an attribute with the given name contained by the specified
     *  container. The container argument must not be null, or a
     *  NullPointerException will be thrown.  This attribute will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty
     *  string. The object is added to the directory of the workspace
     *  if the container is null.
     *  Increment the version of the workspace.
     *  @param container The container.
     *  @param name The name of this attribute.
     *  @exception IllegalActionException If the attribute is not of an
     *   acceptable class for the container, or if the name contains a period.
     *  @exception NameDuplicationException If the name coincides with
     *   an attribute already in the container.
     */
    public PatternObjectAttribute(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        _init();
    }

    /** Construct an attribute in the specified workspace with an empty
     *  string as a name.
     *  The object is added to the directory of the workspace.
     *  Increment the version number of the workspace.
     *  @param workspace The workspace that will list the attribute.
     */
    public PatternObjectAttribute(Workspace workspace) {
        super(workspace);
        _init();
    }

    /** Update the appearance of the container when the value of this attribute
     *  is changed.
     *
     *  @param settable The changed attribute.
     */
    @Override
    public void valueChanged(Settable settable) {
        if (settable == this) {
            NamedObj container = getContainer();
            if (GTTools.isInReplacement(container)) {
                // Update the ports with the criteria attribute of the
                // corresponding actor in the pattern of the transformation
                // rule.
                NamedObj correspondingEntity = GTTools
                        .getCorrespondingPatternObject(container);
                if (correspondingEntity != null) {
                    GTIngredientsAttribute criteria;
                    try {
                        criteria = (GTIngredientsAttribute) container
                                .getAttribute("criteria",
                                        GTIngredientsAttribute.class);
                    } catch (IllegalActionException e) {
                        throw new InternalErrorException(e);
                    }
                    if (criteria != null) {
                        if (container instanceof GTEntity) {
                            criteria.setPersistent(false);
                            try {
                                criteria.setExpression("");
                            } catch (IllegalActionException e) {
                                // Ignore because criteria is not used for
                                // patternObject.
                            }
                        } else {
                            try {
                                criteria.setContainer(null);
                            } catch (KernelException e) {
                                throw new InternalErrorException(e);
                            }
                        }
                    }
                    if (container instanceof GTEntity
                            && correspondingEntity instanceof GTEntity) {
                        ((GTEntity) container)
                                .updateAppearance(((GTEntity) correspondingEntity)
                                        .getCriteriaAttribute());
                    }
                }
            }
        }
    }

    /** Initialize this attribute.
     */
    private void _init() {
        setClassName("ptolemy.actor.gt.PatternObjectAttribute");
        setVisibility(EXPERT);
        addValueListener(this);
    }
}
