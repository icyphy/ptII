/*

 Copyright (c) 2008 The Regents of the University of California.
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
package ptolemy.actor.ptalon.gt;

import java.util.LinkedList;
import java.util.List;

import ptolemy.actor.gt.CreationAttribute;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.NegationAttribute;
import ptolemy.actor.gt.OptionAttribute;
import ptolemy.actor.gt.PreservationAttribute;
import ptolemy.actor.ptalon.PtalonActor;
import ptolemy.actor.ptalon.PtalonEvaluator;
import ptolemy.actor.ptalon.PtalonRuntimeException;
import ptolemy.actor.ptalon.PtalonScopeException;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

//////////////////////////////////////////////////////////////////////////
//// TransformationEvaluator

/**


 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 7.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationEvaluator extends PtalonEvaluator {

    public TransformationEvaluator(PtalonActor actor) {
        super(actor);
        _resetParameters(false);
    }

    public void enterTransformation(boolean incremental)
            throws PtalonRuntimeException {
        _isInTransformation = true;
        _isIncrementalTransformation = incremental;
        _negatedObjects.clear();
        _optionalObjects.clear();
        _removedObjects.clear();
        _preservedObjects.clear();
    }

    public void exitTransformation() throws PtalonRuntimeException {
        _isInTransformation = false;
        _removedObjects.clear();
        _preservedObjects.clear();
    }

    public void negateObject(String name) throws PtalonRuntimeException {
        if (_isInTransformation) {
            throw new PtalonRuntimeException("Objects can be marked negated "
                    + "only in the pattern but not in the transformation "
                    + "following the \"=>\" or \"=>+\" mark.");
        }
        try {
            NamedObj object = _getObject(name);
            if (object != null) {
                if (_negatedObjects.contains(object)) {
                    throw new PtalonRuntimeException("Object \"" + name + "\" "
                            + "has already been marked negated.");
                }
                _removeAttributes(object,
                        new Class<?>[] { NegationAttribute.class });
                new NegationAttribute(object, object.uniqueName("_negated"));
                _negatedObjects.add(object);
            }
        } catch (PtalonScopeException e) {
            // Ignore if we can't resolve the name.
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to preserve object.", e);
        }
    }

    public void optionalObject(String name) throws PtalonRuntimeException {
        if (_isInTransformation) {
            throw new PtalonRuntimeException("Objects can be marked optional "
                    + "only in the pattern but not in the transformation "
                    + "following the \"=>\" or \"=>+\" mark.");
        }
        try {
            NamedObj object = _getObject(name);
            if (object != null) {
                if (_optionalObjects.contains(object)) {
                    throw new PtalonRuntimeException("Object \"" + name + "\" "
                            + "has already been marked optional.");
                }
                _removeAttributes(object,
                        new Class<?>[] { OptionAttribute.class });
                new OptionAttribute(object, object.uniqueName("_optional"));
                _optionalObjects.add(object);
            }
        } catch (PtalonScopeException e) {
            // Ignore if we can't resolve the name.
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to preserve object.", e);
        }
    }

    public void preserveObject(String name) throws PtalonRuntimeException {
        if (_isIncrementalTransformation) {
            throw new PtalonRuntimeException(
                    "Objects can be marked "
                            + "preserved only in incremental transformations (declared "
                            + "with \"=>\" but not \"=>+\").");
        }
        try {
            NamedObj object = _getObject(name);
            if (object != null) {
                if (GTTools.isCreated(object)) {
                    throw new PtalonRuntimeException(
                            "Object \""
                                    + name
                                    + "\""
                                    + " is created in the transformation, so it cannot "
                                    + "be preserved.");
                }
                if (_preservedObjects.contains(object)) {
                    throw new PtalonRuntimeException("Object \"" + name + "\" "
                            + "has already been marked removed.");
                }
                _removeAttributes(object,
                        new Class<?>[] { PreservationAttribute.class });
                new PreservationAttribute(object, object
                        .uniqueName("_preserved"));
                _preservedObjects.add(object);
            }
        } catch (PtalonScopeException e) {
            // Ignore if we can't resolve the name.
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to preserve object.", e);
        }
    }

    public void removeObject(String name) throws PtalonRuntimeException {
        if (!_isIncrementalTransformation) {
            throw new PtalonRuntimeException("Objects can be marked removed "
                    + "only in incremental transformations (declared with "
                    + "\"=>+\").");
        }
        try {
            NamedObj object = _getObject(name);
            if (object != null) {
                if (GTTools.isCreated(object)) {
                    throw new PtalonRuntimeException(
                            "Object \""
                                    + name
                                    + "\""
                                    + " is created in the transformation, so it cannot "
                                    + "be removed.");
                }
                if (_removedObjects.contains(object)) {
                    throw new PtalonRuntimeException("Object \"" + name + "\" "
                            + "has already been marked removed.");
                }
                _removeAttributes(object,
                        new Class<?>[] { PreservationAttribute.class });
                _removedObjects.add(object);
            }
        } catch (PtalonScopeException e) {
            // Ignore if we can't resolve the name.
        } catch (Exception e) {
            throw new PtalonRuntimeException("Unable to remove object.", e);
        }
    }

    public void startAtTop() {
        _negatedObjects.clear();
        _optionalObjects.clear();
        super.startAtTop();
    }

    protected void _processAttributes(NamedObj object)
            throws PtalonRuntimeException {
        if (_isInTransformation) {
            try {
                GTTools.deepRemoveAttributes(object,
                        PreservationAttribute.class);
                new CreationAttribute(object, object.uniqueName("_created"));
            } catch (Exception e) {
                throw new PtalonRuntimeException("Unable to create attribute.",
                        e);
            }
        } else if (_isPreservingTransformation()) {
            try {
                GTTools.deepRemoveAttributes(object, CreationAttribute.class);
                new PreservationAttribute(object, object
                        .uniqueName("_preserved"));
            } catch (Exception e) {
                throw new PtalonRuntimeException("Unable to create attribute.",
                        e);
            }
        }
    }

    private NamedObj _getObject(String name) throws PtalonScopeException,
            PtalonRuntimeException {
        String type = _getType(name);
        String uniqueId = _actor.getMappedName(name);
        NamedObj object = null;
        if (type.equals("relation")) {
            object = _actor.getRelation(uniqueId);
        } else if (type.endsWith("port")) {
            object = _actor.getPort(uniqueId);
        } else if (type.equals("actor")) {
            object = _actor.getEntity(uniqueId);
        }
        return object;
    }

    private void _removeAttributes(NamedObj object, Class<?>[] attributeClasses)
            throws IllegalActionException, NameDuplicationException {
        List<?> attributes = object.attributeList();
        List<Attribute> removedAttributes = new LinkedList<Attribute>();
        for (Object attributeObject : attributes) {
            Attribute attribute = (Attribute) attributeObject;
            for (Class<?> attributeClass : attributeClasses) {
                if (attributeClass.isInstance(attribute)) {
                    removedAttributes.add(attribute);
                }
            }
        }
        for (Attribute attribute : removedAttributes) {
            attribute.setContainer(null);
        }
    }

    private boolean _isInTransformation = false;

    private boolean _isIncrementalTransformation = false;

    private List<NamedObj> _negatedObjects = new LinkedList<NamedObj>();

    private List<NamedObj> _optionalObjects = new LinkedList<NamedObj>();

    private List<NamedObj> _preservedObjects = new LinkedList<NamedObj>();

    private List<NamedObj> _removedObjects = new LinkedList<NamedObj>();
}
