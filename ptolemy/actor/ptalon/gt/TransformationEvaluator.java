/*  Parse transformation actor or values and set parameters of actors.

 Copyright (c) 2008-2014 The Regents of the University of California.
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

///////////////////////////////////////////////////////////////////
//// TransformationEvaluator

/**
 Parse transformation actor or values and set parameters of actors.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class TransformationEvaluator extends PtalonEvaluator {

    /** Construct transformation evaluator.
     *  @param actor the Ptalon actor
     */
    public TransformationEvaluator(PtalonActor actor) {
        super(actor);
        _resetParameters(false);
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Enter the transformation.
     *  @param incremental  True if this is an incremental transformation.
     *  @exception PtalonRuntimeException Not thrown in this baseclass.
     */
    @Override
    public void enterTransformation(boolean incremental)
            throws PtalonRuntimeException {
        _isInTransformation = true;
        _isIncrementalTransformation = incremental;
        _negatedObjects.clear();
        _optionalObjects.clear();
        _removedObjects.clear();
        _preservedObjects.clear();
    }

    /** Exit the transformation.
     *  @exception PtalonRuntimeException Not thrown in this baseclass.
     */
    @Override
    public void exitTransformation() throws PtalonRuntimeException {
        _isInTransformation = false;
        _removedObjects.clear();
        _preservedObjects.clear();
    }

    /**
     * Negate an object.
     * A negated object is a NamedObj with a
     * {@link ptolemy.actor.gt.NegationAttribute}.
     * @param name The name of the NamedObj to be negated.
     * @exception PtalonRuntimeException If we are in a transformation
     * or if the object has already been negated.
     */
    @Override
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
        } catch (Throwable throwable) {
            throw new PtalonRuntimeException("Unable to preserve object.",
                    throwable);
        }
    }

    /**
     * Mark a NamedObj as optional.
     * An optional object is a NamedObj with an
     * {@link ptolemy.actor.gt.OptionAttribute}.
     * @param name The name of the NamedObj to be marked optional.
     * @exception PtalonRuntimeException If we are in a transformation or if the object has
     * already been marked optional
     */
    @Override
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
        } catch (Throwable throwable) {
            throw new PtalonRuntimeException("Unable to preserve object.",
                    throwable);
        }
    }

    /**
     * Preserve an object.
     * A preserved object is a NamedObj with an
     * {@link ptolemy.actor.gt.OptionAttribute}.
     * @param name The name of the NamedObj to be preserved
     * @exception PtalonRuntimeException If we are in a transformation or if the object has
     * already been preserved.
     */
    @Override
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
                new PreservationAttribute(object,
                        object.uniqueName("_preserved"));
                _preservedObjects.add(object);
            }
        } catch (PtalonScopeException e) {
            // Ignore if we can't resolve the name.
        } catch (Throwable throwable) {
            throw new PtalonRuntimeException("Unable to preserve object.",
                    throwable);
        }
    }

    /**
     * Remove an object.
     * @param name The name of the NamedObj to be removed.
     * @exception PtalonRuntimeException If we are in a transformation or if the object has
     * already been marked as removed.
     */
    @Override
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
        } catch (Throwable throwable) {
            throw new PtalonRuntimeException("Unable to remove object.",
                    throwable);
        }
    }

    /** Prepare the compiler to start at the outermost scope of the Ptalon
     *  program during run time.
     */
    @Override
    public void startAtTop() {
        _negatedObjects.clear();
        _optionalObjects.clear();
        super.startAtTop();
    }

    ///////////////////////////////////////////////////////////////////
    ////                         protected methods                 ////

    /** Process an attribute.
     *  @param object The NamedObj to be processed.
     *  @exception PtalonRuntimeException If thrown while processing
     *  the attribute.
     */
    @Override
    protected void _processAttributes(NamedObj object)
            throws PtalonRuntimeException {
        if (_isInTransformation) {
            try {
                GTTools.deepRemoveAttributes(object,
                        PreservationAttribute.class);
                new CreationAttribute(object, object.uniqueName("_created"));
            } catch (Throwable throwable) {
                throw new PtalonRuntimeException("Unable to create attribute.",
                        throwable);
            }
        } else if (_isPreservingTransformation()) {
            try {
                GTTools.deepRemoveAttributes(object, CreationAttribute.class);
                new PreservationAttribute(object,
                        object.uniqueName("_preserved"));
            } catch (Throwable throwable) {
                throw new PtalonRuntimeException("Unable to create attribute.",
                        throwable);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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

    ///////////////////////////////////////////////////////////////////
    ////                         private variables                 ////

    private boolean _isInTransformation = false;

    private boolean _isIncrementalTransformation = false;

    private List<NamedObj> _negatedObjects = new LinkedList<NamedObj>();

    private List<NamedObj> _optionalObjects = new LinkedList<NamedObj>();

    private List<NamedObj> _preservedObjects = new LinkedList<NamedObj>();

    private List<NamedObj> _removedObjects = new LinkedList<NamedObj>();
}
