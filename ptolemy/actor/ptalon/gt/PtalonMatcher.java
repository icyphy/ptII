/*  A graph transformation matcher for Ptalon.

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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.TypedCompositeActor;
import ptolemy.actor.TypedIORelation;
import ptolemy.actor.gt.ContainerIgnoringAttribute;
import ptolemy.actor.gt.GTCompositeActor;
import ptolemy.actor.gt.GTTools;
import ptolemy.actor.gt.IgnoringAttribute;
import ptolemy.actor.ptalon.PtalonActor;
import ptolemy.actor.ptalon.PtalonEvaluator;
import ptolemy.actor.ptalon.PtalonExpressionParameter;
import ptolemy.actor.ptalon.PtalonLexer;
import ptolemy.actor.ptalon.PtalonRecognizer;
import ptolemy.data.BooleanToken;
import ptolemy.data.StringToken;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.Settable;

///////////////////////////////////////////////////////////////////
//// PtalonMatcher

/**
 A graph transformation matcher for Ptalon.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 8.0
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class PtalonMatcher extends TypedCompositeActor implements
        GTCompositeActor {

    /** Construct a PtalonMatcher with a name and a container.
     *  The container argument must not be null, or a
     *  NullPointerException will be thrown.  This actor will use the
     *  workspace of the container for synchronization and version counts.
     *  If the name argument is null, then the name is set to the empty string.
     *  Increment the version of the workspace.  This actor will have no
     *  local director initially, and its executive director will be simply
     *  the director of the container.
     *
     *  @param container The container.
     *  @param name The name of this actor.
     *  @exception IllegalActionException If the container is incompatible
     *   with this actor.
     *  @exception NameDuplicationException If the name coincides with
     *   an actor already in the container.
     */
    public PtalonMatcher(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        // The superclass sets the classname, see the class comment
        // in TypedCompositeActor.
        setClassName("ptolemy.actor.ptalon.gt.PtalonMatcher");
        ptalonCodeLocation = new FileParameter(this, "ptalonCodeLocation");
    }

    ///////////////////////////////////////////////////////////////////
    ////                         ports and parameters              ////

    /** The location of the Ptalon code. */
    public FileParameter ptalonCodeLocation;

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Setup parameters for this actor.
     *  @param attribute The attribute that changed.
     *  @exception IllegalActionException If there are problems setting
     *  up parameters for this actor, or if the superclass throws it.
     */
    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

        // FIXME: This is confusing.  _handleAttributeChange
        // will initially be true, which means the code will
        // always run.  Probably one of the methods in the try block
        // calls attributeChanged and _handleAttributeChanged() is
        // necessary to avoid endless loops.
        if (!_handleAttributeChange) {
            return;
        }

        HashKey key = _getKey();
        if (!key.equals(_currentKey)) {
            _handleAttributeChange = false;
            _currentKey = key;
            try {
                if (_currentActor != null) {
                    new IgnoringAttribute(_currentActor,
                            _IGNORING_ATTRIBUTE_NAME);
                }
                if (!_actors.containsKey(key)) {
                    _createPtalonActor(key);
                } else {
                    _currentActor = _actors.get(key);
                    _currentActor.getAttribute(_IGNORING_ATTRIBUTE_NAME)
                            .setContainer(null);
                }
                if (_currentActor != null) {
                    Attribute ignoringAttribute = _currentActor
                            .getAttribute(_IGNORING_ATTRIBUTE_NAME);
                    if (ignoringAttribute != null) {
                        ignoringAttribute.setContainer(null);
                    }
                    _mirrorPtalonActor();
                    _createParameters();
                    _rearrangePtalonActors();
                }
            } catch (Throwable throwable) {
                throw new IllegalActionException(null, throwable, "Unable to "
                        + "create Ptalon actor inside.");
            } finally {
                _handleAttributeChange = true;
            }
        }
    }

    /** Clear the list of actors.
     */
    public void clearActors() {
        _actors.clear();
    }

    /** Set the container.
     *  @param container The proposed container.
     *  @exception IllegalActionException If the action would result in a
     *   recursive containment structure, or if
     *   this entity and container are not in the same workspace.
     *  @exception NameDuplicationException If the container already has
     *   an entity with the name of this entity.
     */
    @Override
    public void setContainer(CompositeEntity container)
            throws IllegalActionException, NameDuplicationException {
        super.setContainer(container);

        // If we are in the pattern and we have not yet created a
        // _containerIgnoring, then do so.  If we are not in the
        // pattern and the _containerIgnoring parameter is null,
        // then set the container of the _containerIgnoring attribute
        // to null
        if (GTTools.isInPattern(this)) {
            if (getAttribute("_containerIgnoring") == null) {
                Attribute attribute = new ContainerIgnoringAttribute(this,
                        "_containerIgnoring");
                Location location = new Location(attribute, "_location");
                location.setExpression("{15, 25}");
            }
        } else {
            Attribute attribute = getAttribute("_containerIgnoring");
            if (attribute != null) {
                attribute.setContainer(null);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public inner classes              ////

    /**  A nested ptalon actor.
     */
    public static class NestedPtalonActor extends PtalonActor implements
            GTCompositeActor {

        /** Construct a NestedPtalonActor with a name and a container.
         *  The container argument must not be null, or a
         *  NullPointerException will be thrown.  This actor will use the
         *  workspace of the container for synchronization and version counts.
         *  If the name argument is null, then the name is set to the empty string.
         *  Increment the version of the workspace.  This actor will have no
         *  local director initially, and its executive director will be simply
         *  the director of the container.
         *
         *  @param container The container.
         *  @param name The name of this actor.
         *  @exception IllegalActionException If the container is incompatible
         *   with this actor.
         *  @exception NameDuplicationException If the name coincides with
         *   an actor already in the container.
         */
        public NestedPtalonActor(CompositeEntity container, String name)
                throws IllegalActionException, NameDuplicationException {
            super(container, name);

            setClassName("ptolemy.actor.ptalon.gt.PtalonMatcher"
                    + "$NestedPtalonActor");
            setPersistent(false);
        }

        /** If we are not initializing or fixed, then call attributeChanged()
         *  in the super class.
         *  @param attribute The attribute that changed.
         *  @exception IllegalActionException If thrown by the superclass.
         */
        @Override
        public void attributeChanged(Attribute attribute)
                throws IllegalActionException {
            if (!_initializing && !_fixed) {
                super.attributeChanged(attribute);
            }
        }

        /** Initialize the ptalon actor and the code location.
         *  @exception IllegalActionException If there is problem
         *  initializing the ptalon actor or the ptalon code
         *  location.
         */
        public void init() throws IllegalActionException {
            _initializing = false;
            super._initializePtalonActor();
            super._initializePtalonCodeLocation();
        }

        /** Create a new PtalonEvaluator.
         *  @param actor The actor for which to create a PtalonEvaluator.
         *  @return The PtalonEvaluator
         */
        @Override
        protected PtalonEvaluator _createPtalonEvaluator(PtalonActor actor) {
            return new TransformationEvaluator(actor);
        }

        /** Create a new PtalonRecognizer.
         *  @param lexer The lexer for which to create a PtalonRecognizer
         *  @return The PtalonRecognizer
         */
        @Override
        protected PtalonRecognizer _createPtalonRecognizer(PtalonLexer lexer) {
            PtalonRecognizer parser = super._createPtalonRecognizer(lexer);
            parser.enableGTExtension(true);
            return parser;
        }

        private boolean _fixed = false;

        private boolean _initializing = true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private void _createParameters() throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        _currentActor._fixed = true;
        Set<PtalonExpressionParameter> parameters = new HashSet<PtalonExpressionParameter>();
        for (Object parameterObject : _currentActor
                .attributeList(PtalonExpressionParameter.class)) {
            PtalonExpressionParameter parameter = (PtalonExpressionParameter) parameterObject;
            parameter.setVisibility(Settable.NOT_EDITABLE);
            Attribute myAttribute = getAttribute(_MIRRORED_PARAMETER_PREFIX
                    + parameter.getName());
            if (myAttribute != null
                    && !(myAttribute instanceof PtalonExpressionParameter)) {
                myAttribute.setContainer(null);
                myAttribute = null;
            }
            PtalonExpressionParameter myParameter = (PtalonExpressionParameter) myAttribute;
            if (myParameter == null) {
                myParameter = (PtalonExpressionParameter) parameter.clone();
                myParameter.setName(_MIRRORED_PARAMETER_PREFIX
                        + myParameter.getName());
                myParameter.setContainer(this);
                myParameter.setVisibility(Settable.FULL);
                myParameter.setToken(parameter.getExpression());
                parameter.setToken(parameter.getToken().toString());
            } else {
                Parameter hideParameter = (Parameter) myParameter
                        .getAttribute("_hide");
                if (hideParameter != null) {
                    hideParameter.setContainer(null);
                }
            }
            parameters.add(myParameter);
        }
        for (Object parameterObject : attributeList(PtalonExpressionParameter.class)) {
            PtalonExpressionParameter parameter = (PtalonExpressionParameter) parameterObject;
            if (!parameters.contains(parameter)) {
                if (parameter.getAttribute("_hide") == null) {
                    new Parameter(parameter, "_hide")
                            .setToken(BooleanToken.TRUE);
                }
            }
        }
    }

    private void _createPtalonActor(HashKey key) throws IllegalActionException,
            NameDuplicationException {
        NestedPtalonActor actor = new NestedPtalonActor(this,
                uniqueName("PtalonActor"));
        actor.ptalonCodeLocation.setToken(key._codeLocation);
        for (Map.Entry<String, Token> entry : key._parameterMap.entrySet()) {
            String name = entry.getKey();
            Token value = entry.getValue();
            new PtalonExpressionParameter(actor, name).setToken(value);
        }
        actor.init();
        key = _getKey(actor);
        NestedPtalonActor oldActor = _actors.get(key);
        if (oldActor == null) {
            _actors.put(key, actor);
        } else {
            actor.setContainer(null);
            actor = oldActor;
        }
        _currentActor = actor;
        _currentKey = key;
    }

    private HashKey _getKey() throws IllegalActionException {
        HashKey key = new HashKey((StringToken) ptalonCodeLocation.getToken());
        for (Object parameter : attributeList(PtalonExpressionParameter.class)) {
            key.put((PtalonExpressionParameter) parameter);
        }
        return key;
    }

    private HashKey _getKey(PtalonActor actor) throws IllegalActionException {
        HashKey key = new HashKey(
                (StringToken) actor.ptalonCodeLocation.getToken());
        for (Object parameter : actor
                .attributeList(PtalonExpressionParameter.class)) {
            key.put((PtalonExpressionParameter) parameter);
        }
        return key;
    }

    private void _mirrorPtalonActor() throws IllegalActionException,
            NameDuplicationException, CloneNotSupportedException {
        removeAllPorts();
        removeAllRelations();
        for (Object portObject : _currentActor.portList()) {
            Port port = (Port) portObject;
            Port mirrorPort = (Port) port.clone();
            mirrorPort.setContainer(this);
            TypedIORelation relation = new TypedIORelation(this,
                    uniqueName("relation"));
            port.link(relation);
            mirrorPort.link(relation);
        }
    }

    private void _rearrangePtalonActors() throws IllegalActionException,
            NameDuplicationException {
        final int width = 640;
        final int xSpace = 80;
        final int ySpace = 80;
        final int xStart = 45;
        final int yStart = 110;
        int x = xStart;
        int y = yStart;
        for (Object actorObject : entityList(PtalonActor.class)) {
            PtalonActor actor = (PtalonActor) actorObject;
            Location location = (Location) actor.getAttribute("_location",
                    Location.class);
            if (location == null) {
                location = new Location(actor, "_location");
            }
            location.setExpression("{" + x + ", " + y + "}");
            location.validate();
            x += xSpace;
            if (x + xSpace >= width) {
                x = xStart;
                y += ySpace;
            }
        }
    }

    private static final String _IGNORING_ATTRIBUTE_NAME = "_ignoreInMatching";

    private static final String _MIRRORED_PARAMETER_PREFIX = "m_";

    private Map<HashKey, NestedPtalonActor> _actors = new HashMap<HashKey, NestedPtalonActor>();

    private NestedPtalonActor _currentActor;

    private HashKey _currentKey;

    private boolean _handleAttributeChange = true;

    private static class HashKey {

        @Override
        public boolean equals(Object object) {
            if (object instanceof HashKey) {
                HashKey key = (HashKey) object;
                if (_codeLocation == key._codeLocation || _codeLocation != null
                        && _codeLocation.equals(key._codeLocation)) {
                    return _parameterMap.equals(key._parameterMap);
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            int code = 0;
            if (_codeLocation != null) {
                code += _codeLocation.hashCode();
            }
            code += _parameterMap.hashCode();
            return code;
        }

        public void put(PtalonExpressionParameter parameter)
                throws IllegalActionException {
            String name = parameter.getName();
            if (name.startsWith(_MIRRORED_PARAMETER_PREFIX)) {
                name = name.substring(_MIRRORED_PARAMETER_PREFIX.length());
            }
            _parameterMap.put(name, parameter.getToken());
        }

        HashKey(StringToken codeLocation) {
            _codeLocation = codeLocation;
        }

        private StringToken _codeLocation;

        private Map<String, Token> _parameterMap = new HashMap<String, Token>();
    }
}
