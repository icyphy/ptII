/* Implementation of a recursive algorithm to match a pattern to any subgraph of
   a graph.

@Copyright (c) 2007-2008 The Regents of the University of California.
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

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.data.FastLinkedList;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.data.SequentialTwoWayHashMap;
import ptolemy.actor.gt.data.Tuple;
import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Type;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.KernelRuntimeException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

/**
 Implementation of a recursive algorithm to match a pattern to any subgraph of a
 a graph. The pattern is specified as the <em>pattern</em> part of a graph
 transformation rule. The graph to be matched to, called <em>host graph</em>, is
 an arbitrary Ptolemy II model, whose top level is an instance of {@link
 CompositeEntity}.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GraphMatcher extends GraphAnalyzer {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Return the most recent match result, which the user should not modify.
     *  During the matching process, when a callback routine (an instance
     *  implementing {@link MatchCallback}) is invoked (see {@link
     *  #setMatchCallback(MatchCallback)}), that callback routine can call this
     *  method to retrieve the match result that has just been found.
     *  <p>
     *  Note that the returned match result object may be changed by future
     *  matching. To maintain a copy, {@link MatchResult#clone()} may be called
     *  that returns a clone of it.
     *
     *  @return The most recent match result.
     */
    public MatchResult getMatchResult() {
        return _matchResult;
    };

    /** Return whether the last matching was successful.
     *
     *  @return Whether the last matching was successful.
     */
    public boolean isSuccessful() {
        return _success;
    }

    /** Match a rule file to a model file. This main method takes a parameter
     *  array of length 2 or 3. If the array's length is 2, the first string is
     *  the rule file name, and the second is the model file name. In this case,
     *  an arbitrary match (the first one found) is printed to the console. If
     *  the array has 3 elements, the first string should be "<tt>-A</tt>"
     *  (meaning "all matches"), the second string is the rule file name, and
     *  the third is the model file name. In that case, all the matches are
     *  printed to to console one by one.
     *
     *  @param args The parameter array.
     *  @exception Exception If the rule file or the model file cannot be read.
     */
    public static void main(String[] args) throws Exception {
        if (!(args.length == 2 || (args.length == 3 && args[0]
                .equalsIgnoreCase("-A")))) {
            System.err.println("USAGE: java [-A] "
                    + GraphMatcher.class.getName() + " <rule.xml> <host.xml>");
            System.exit(1);
        }

        final boolean all = args.length == 3 && args[0].equalsIgnoreCase("-A");
        String ruleXMLFile = all ? args[1] : args[0];
        String hostXMLFile = all ? args[2] : args[1];

        MatchCallback matchCallback = new MatchCallback() {
            public boolean foundMatch(GraphMatcher matcher) {
                MatchResult match = matcher.getMatchResult();
                System.out.println("--- Match " + ++count + " ---");
                _printMatch(match);
                return !all;
            }

            private int count = 0;
        };
        match(ruleXMLFile, hostXMLFile, matchCallback);
    }

    /** Match a pattern specified in the <tt>patternGraph</tt> to a model in
     *  <tt>hostGraph</tt>. If the match is successful, <tt>true</tt> is
     *  returned, and the match result is stored internally, which can be
     *  retrieved with {@link #getMatchResult()}. A matching was successful if
     *  at least one match result was found, and the callback (an instance
     *  implementing {@link MatchCallback}) returned <tt>true</tt> when it was
     *  invoked.
     *
     *  @param pattern The pattern.
     *  @param hostGraph The host graph.
     *  @return <tt>true</tt> if the match is successful; <tt>false</tt>
     *   otherwise.
     */
    public boolean match(Pattern pattern, CompositeEntity hostGraph) {

        // Matching result.
        _matchResult = new MatchResult(_parameterValues);

        // Temporary data structures.
        _parameterValues.clear();
        _lookbackList.clear();
        _temporaryMatch.clear();
        _ignoredOptionalObjects.clear();
        _callbacksInPattern.clear();
        _clearCaches();

        // Record the values of all the iterators.
        Hashtable<ValueIterator, Token> records =
            new Hashtable<ValueIterator, Token>();
        try {
            GTTools.saveValues(pattern, records);
        } catch (IllegalActionException e) {
            throw new KernelRuntimeException(e, "Unable to save values.");
        }

        // Initially, we are not checking negated objects.
        _negation = false;
        _success = false;

        _findAllMatchCallbacksInPattern(pattern);

        try {
            _success = _matchCompositeEntityAtAllLevels(pattern, hostGraph);
        } finally {
            _parameterValues.clear();
            _ignoredOptionalObjects.clear();
            _callbacksInPattern.clear();
            _clearCaches();
        }

        // Restore the values of all the iterators.
        try {
            GTTools.restoreValues(pattern, records);
        } catch (IllegalActionException e) {
            throw new KernelRuntimeException(e, "Unable to restore values.");
        }

        assert _lookbackList.isEmpty();
        assert _temporaryMatch.isEmpty();
        if (!_success) {
            assert _matchResult.isEmpty();
        }

        return _success;
    }

    /** Match the rule stored in the file with name <tt>ruleXMLFile</tt> to the
     *  model stored in the file with name <tt>hostXMLFile</tt>, whose top-level
     *  should be an instance of {@link CompositeEntity}. The first match result
     *  (which is arbitrarily decided by the recursive algorithm) is recorded in
     *  the returned matcher object. This result can be obtained with {@link
     *  #getMatchResult()}. If the match is unsuccessful, the match result is
     *  empty, and {@link #isSuccessful()} of the returned matcher object
     *  returns <tt>false</tt>.
     *
     *  @param ruleXMLFile The name of the file in which the rule is stored.
     *  @param hostXMLFile The name of the file in which the model to be matched
     *   is stored.
     *  @return A matcher object with the first match result stored in it. If no
     *   match is found, {@link #isSuccessful()} of the matcher object returns
     *   <tt>false</tt>, and {@link #getMatchResult()} returns an empty match.
     *  @exception Exception If the rule file or the model file cannot be read.
     *  @see #match(String, String, MatchCallback)
     */
    public static GraphMatcher match(String ruleXMLFile, String hostXMLFile)
            throws Exception {
        return match(ruleXMLFile, hostXMLFile, null);
    }

    /** Match the rule stored in the file with name <tt>ruleXMLFile</tt> to the
     *  model stored in the file with name <tt>hostXMLFile</tt>, whose top-level
     *  should be an instance of {@link CompositeEntity}, and invoke
     *  <tt>callback</tt>'s {@link MatchCallback#foundMatch(GraphMatcher)}
     *  method whenever a match is found. If the callback returns <tt>true</tt>,
     *  the match will terminate and no more matches will be reported;
     *  otherwise, the match process continues, and the callback may be invoked
     *  again. If <tt>callback</tt> is <tt>null</tt>, the behavior is the same
     *  as {@link #match(String, String)}.
     *
     *  @param ruleXMLFile The name of the file in which the rule is stored.
     *  @param hostXMLFile The name of the file in which the model to be matched
     *   is stored.
     *  @param callback The callback to be invoked when matches are found.
     *  @return A matcher object with the last match result stored in it. If no
     *   match is found, or though matches are found, the callback returns
     *   <tt>false</tt> for all the matches, then {@link #isSuccessful()} of the
     *   matcher object returns <tt>false</tt>, and {@link #getMatchResult()}
     *   returns an empty match.
     *  @exception Exception If the rule file or the model file cannot be read.
     *  @see #match(String, String)
     */
    public static GraphMatcher match(String ruleXMLFile, String hostXMLFile,
            MatchCallback callback) throws Exception {
        MoMLParser parser = new MoMLParser();
        TransformationRule rule = (TransformationRule) parser.parse(null,
                new File(ruleXMLFile).toURI().toURL());
        parser.reset();
        CompositeEntity host = (CompositeEntity) parser.parse(null, new File(
                hostXMLFile).toURI().toURL());

        GraphMatcher matcher = new GraphMatcher();
        if (callback != null) {
            matcher.setMatchCallback(callback);
        }
        matcher.match(rule.getPattern(), host);
        return matcher;
    }

    /** Set the callback to be invoked by future calls to {@link
     *  #match(Pattern, CompositeEntity)}.
     *
     *  @param callback The callback. If it is <tt>null</tt>, the callback is
     *   set to {@link #DEFAULT_CALLBACK}.
     *  @see #match(Pattern, CompositeEntity)
     */
    public void setMatchCallback(MatchCallback callback) {
        if (callback == null) {
            _callback = DEFAULT_CALLBACK;
        } else {
            _callback = callback;
        }
    }

    /** The default callback that always returns <tt>true</tt>. A callback is
     *  invoked whenever a match is found. Because this callback always returns
     *  <tt>true</tt>, it terminates the matching process after the first match
     *  is found, and the match result can be obtained later using {@link
     *  #getMatchResult()}.
     */
    public static final MatchCallback DEFAULT_CALLBACK = new MatchCallback() {
        public boolean foundMatch(GraphMatcher matcher) {
            return true;
        }
    };

    ///////////////////////////////////////////////////////////////////
    ////                        protected methods                  ////

    protected boolean _isIgnored(Object object) {
        Boolean ignored = _cachedIgnoredObjects.get(object);
        if (ignored != null) {
            return ignored.booleanValue();
        }

        boolean result;
        if (_isCreated(object) || GTTools.isIgnored(object)) {
            result = true;
        } else if (object instanceof NamedObj) {
            NamedObj optionalContainer = _getOptionalContainer(
                    (NamedObj) object);
            result = optionalContainer != null
                    && _ignoredOptionalObjects.containsKey(optionalContainer)
                    && _ignoredOptionalObjects.get(optionalContainer);
        } else {
            result = false;
        }

        _cachedIgnoredObjects.put(object, result);
        return result;
    }

    /** Test whether the composite entity is opaque or not. Return <tt>true</tt>
     *  if the composite entity is an instance of {@link CompositeActor} and it
     *  is opaque. A composite actor is opaque if it has a director inside,
     *  which means the new level of hierarchy that it creates cannot be
     *  flattened, or it has a {@link HierarchyFlatteningAttribute} attribute
     *  inside with value <tt>true.
     *
     *  @param entity The composite entity to be tested.
     *  @return <tt>true</tt> if the composite entity is an instance of {@link
     *   CompositeActor} and it is opaque.
     */
    protected boolean _isOpaque(CompositeEntity entity) {
        if (entity instanceof CompositeActor
                && ((CompositeActor) entity).isOpaque()) {
            return true;
        } else {
            NamedObj container = entity.getContainer();
            Token hierarchyFlatteningToken = _getAttribute(container,
                    HierarchyFlatteningAttribute.class, true, false, true);
            boolean hierarchyFlattening = hierarchyFlatteningToken == null ?
                    HierarchyFlatteningAttribute.DEFAULT
                    : ((BooleanToken) hierarchyFlatteningToken).booleanValue();
            Token containerIgnoringToken = _getAttribute(container,
                    ContainerIgnoringAttribute.class, false, true, false);
            boolean containerIgnoring = containerIgnoringToken == null ?
                    ContainerIgnoringAttribute.DEFAULT
                    : ((BooleanToken) containerIgnoringToken).booleanValue();
            return !hierarchyFlattening && !containerIgnoring;
        }
    }

    protected boolean _relationCollapsing(NamedObj container) {
        Token collapsingToken = _getAttribute(container,
                RelationCollapsingAttribute.class, true, false, false);
        if (collapsingToken == null) {
            return RelationCollapsingAttribute.DEFAULT;
        } else {
            return ((BooleanToken) collapsingToken).booleanValue();
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    /** Check the items in the lookback list for more matching requirements. If
     *  no more requirements are found (i.e., all the lists in the lookback list
     *  have been fully explored), then a match is found, and the callback's
     *  {@link MatchCallback#foundMatch(GraphMatcher)} is invoked. If that
     *  method returns true, the matching process terminates; otherwise, the
     *  matching proceeds by backtracking.
     *
     *  @return Whether the match is successful.
     */
    private boolean _checkBackward() {
        FastLinkedList<LookbackEntry>.Entry entry = _lookbackList.getTail();
        LookbackEntry lists = null;
        while (entry != null) {
            lists = entry.getValue();
            if (!_negation && !lists.isFinished() ||
                    _negation && !lists.isNegated()) {
                break;
            }
            entry = entry.getPrevious();
        }

        if (entry == null) {
            if (_negation) {
                return true;
            } else {
                _negation = true;
                int matchSize = _matchResult.size();
                if (_checkBackward() && _matchResult.size() > matchSize) {
                    _negation = false;
                    _matchResult.retain(matchSize);
                    return false;
                } else {
                    _negation = false;
                }
            }
            if (_checkConstraints()) {
                for (MatchCallback callback : _callbacksInPattern) {
                    if (!callback.foundMatch(this)) {
                        return false;
                    }
                }
                return _callback.foundMatch(this);
            } else {
                return false;
            }
        } else {
            return _matchList(lists);
        }
    }

    private boolean _checkConstraint(Pattern pattern, Constraint constraint) {
        return constraint.check(pattern, _matchResult);
    }

    private boolean _checkConstraints() {
        if (_matchResult.isEmpty()) {
            return false;
        }

        Iterator<Map.Entry<Object, Object>> iterator = _matchResult.entrySet()
                .iterator();
        Map.Entry<Object, Object> anyEntry = null;
        while (iterator.hasNext()) {
            anyEntry = iterator.next();
            if (anyEntry.getKey() instanceof NamedObj) {
                break;
            }
            anyEntry = null;
        }
        if (anyEntry == null) {
            return false;
        }

        NamedObj patternObject = (NamedObj) anyEntry.getKey();
        NamedObj patternContainer = patternObject.getContainer();
        while (patternContainer != null && _matchResult.containsKey(patternContainer)) {
            patternObject = patternContainer;
            patternContainer = patternContainer.getContainer();
        }
        if (!(patternObject instanceof Pattern)) {
            // The top-level container for the matching is not a pattern, so it
            // has no constraint to check.
            return true;
        }

        Pattern pattern = (Pattern) patternObject;
        try {
            pattern.workspace().getReadAccess();
            List<?> constraints = pattern.attributeList(Constraint.class);
            for (Object constraintObject : constraints) {
                Constraint constraint = (Constraint) constraintObject;
                if (!_checkConstraint(pattern, constraint)) {
                    return false;
                }
            }
        } finally {
            pattern.workspace().doneReading();
        }
        return true;
    }

    private static boolean _checkCriterion(NamedObj patternObject,
            NamedObj hostObject) {
        GTIngredientList ruleList = null;
        if (patternObject instanceof GTEntity) {
            try {
                ruleList = ((GTEntity) patternObject).getCriteriaAttribute()
                        .getIngredientList();
            } catch (MalformedStringException e) {
                return false;
            }
        } else {
            List<?> attributeList = patternObject.attributeList(
                    GTIngredientsAttribute.class);
            if (!attributeList.isEmpty()) {
                try {
                    ruleList = ((GTIngredientsAttribute) attributeList.get(
                            0)).getIngredientList();
                } catch (MalformedStringException e) {
                    return false;
                }
            }
        }

        if (ruleList != null) {
            for (GTIngredient rule : ruleList) {
                if (rule instanceof Criterion) {
                    Criterion criterion = (Criterion) rule;
                    if (criterion.canCheck(hostObject)) {
                        if (!((Criterion) rule).match(hostObject)) {
                            return false;
                        }
                    }
                }
            }
        }

        return true;
    }

    private void _clearCaches() {
        _cachedCreatedObjects.clear();
        _cachedIgnoredObjects.clear();
        _cachedNegatedObjects.clear();
        _cachedOptionalContainers.clear();
    }

    /** Find all instances of NamedObj that implement the MatchCallback
     *  interface, and record them in the _callbacksInPattern list to be
     *  invoked when a match is found.
     *
     *  @param container
     */
    private void _findAllMatchCallbacksInPattern(NamedObj container) {
        if (container instanceof MatchCallback) {
            _callbacksInPattern.add((MatchCallback) container);
        }
        Collection<NamedObj> children = GTTools.getChildren(container, true,
                true, true, true);
        for (NamedObj child : children) {
            _findAllMatchCallbacksInPattern(child);
        }
    }

    private Token _getAttribute(NamedObj container,
            Class<? extends ParameterAttribute> attributeClass,
            boolean searchContainer, boolean patternOnly, boolean hostOnly) {

        boolean isInHost = false;

        while (container != null) {
            if (_matchResult.containsValue(container)) {
                container = (NamedObj) _matchResult.getKey(container);
                isInHost = true;
            } else if (_temporaryMatch.containsValue(container)) {
                container = (NamedObj) _temporaryMatch.getKey(container);
                isInHost = true;
            }

            if ((!patternOnly || !isInHost) && (!hostOnly || isInHost)) {
                List<?> attributes = container.attributeList(attributeClass);
                if (!attributes.isEmpty()) {
                    ParameterAttribute attribute =
                        (ParameterAttribute) attributes.get(0);
                    try {
                        attribute.workspace().getReadAccess();
                        Parameter parameter = attribute.parameter;
                        return parameter == null ? null : parameter.getToken();
                    } catch (IllegalActionException e) {
                        return null;
                    } finally {
                        attribute.workspace().doneReading();
                    }
                }
            }

            if (!searchContainer) {
                break;
            }

            container = container.getContainer();
        }

        return null;
    }

    private static Criterion _getDefiningCriterion(NamedObj patternObject) {
        if (patternObject instanceof Checkable) {
            return ((Checkable) patternObject).getCriterion();
        } else {
            return null;
        }
    }

    /** Get a string that represents the object. If the object is an instance of
     *  {@link NamedObj}, the returned string is its name retrieved by {@link
     *  NamedObj#getFullName()}; otherwise, the <tt>toString</tt> method of the
     *  object is called to get the string.
     *
     *  @param object The object.
     *  @return The string that represents the object.
     */
    private static String _getNameString(Object object) {
        return object instanceof NamedObj ? ((NamedObj) object).getFullName()
                : object.toString();
    }

    private NamedObj _getOptionalContainer(NamedObj object) {
        Object optionalObject = _cachedOptionalContainers.get(object);
        if (optionalObject != null) {
            if (optionalObject instanceof NamedObj) {
                return (NamedObj) optionalObject;
            } else {
                return null;
            }
        }

        NamedObj container = object;
        while (container != null && !GTTools.isOptional(container)) {
            container = container.getContainer();
        }
        if (container == null) {
            // Put any non-NamedObj value so that we won't return it later.
            _cachedOptionalContainers.put(object, Boolean.FALSE);
            return null;
        } else {
            _cachedOptionalContainers.put(object, container);
            return container;
        }
    }

    private boolean _isCreated(Object object) {
        Boolean created = _cachedCreatedObjects.get(object);
        if (created != null) {
            return created.booleanValue();
        }

        created = GTTools.isCreated(object);
        _cachedCreatedObjects.put(object, created);
        return created;
    }

    private boolean _isNegated(Object object) {
        Boolean negated = _cachedNegatedObjects.get(object);
        if (negated != null) {
            return negated.booleanValue();
        }

        negated = GTTools.isNegated(object);
        _cachedNegatedObjects.put(object, negated);
        return negated;
    }

    private boolean _matchAtomicEntity(ComponentEntity patternEntity,
            ComponentEntity hostEntity) {
        if (patternEntity instanceof GTEntity
                && !((GTEntity) patternEntity).match(hostEntity)) {
            return false;
        }

        if (!_checkCriterion(patternEntity, hostEntity)) {
            return false;
        }

        int matchSize = _matchResult.size();
        boolean success = true;
        ObjectList patternList = new ObjectList();
        ObjectList hostList = new ObjectList();

        _matchResult.put(patternEntity, hostEntity);

        if (!(patternEntity instanceof GTEntity)) {
            success = patternEntity.getClass().isInstance(hostEntity);
        }

        if (success) {
            for (Object portObject : patternEntity.portList()) {
                if (!_isIgnored(portObject)) {
                    patternList.add(portObject);
                }
            }
            for (Object portObject : hostEntity.portList()) {
                if (!_isIgnored(portObject)) {
                    hostList.add(portObject);
                }
            }
        }

        success = success && _matchObject(patternList, hostList);

        if (!success) {
            _matchResult.retain(matchSize);
        }

        return success;
    }

    private boolean _matchCompositeEntity(CompositeEntity patternEntity,
            CompositeEntity hostEntity) {
        if (patternEntity instanceof GTEntity
                && !((GTEntity) patternEntity).match(hostEntity)) {
            return false;
        }

        if (!_checkCriterion(patternEntity, hostEntity)) {
            return false;
        }

        int matchSize = _matchResult.size();
        int parameterSize = _parameterValues.size();

        ParameterIterator paramIterator;
        try {
            paramIterator = new ParameterIterator(patternEntity);
        } catch (IllegalActionException e) {
            return false;
        }

        boolean success = false;

        while (!success && paramIterator.next()) {
            success = true;

            ObjectList patternList = new ObjectList();
            ObjectList hostList = new ObjectList();

            _matchResult.put(patternEntity, hostEntity);

            Director patternDirector = null;
            Director hostDirector = null;
            if (patternEntity instanceof CompositeActor
                    && ((CompositeActor) patternEntity).isOpaque()) {
                patternDirector =
                    ((CompositeActor) patternEntity).getDirector();
                if (_isIgnored(patternDirector)) {
                    patternDirector = null;
                }
            }
            if (hostEntity instanceof CompositeActor
                    && ((CompositeActor) hostEntity).isOpaque()) {
                hostDirector = ((CompositeActor) hostEntity).getDirector();
                if (_isIgnored(hostDirector)) {
                    hostDirector = null;
                }
            }
            if (patternDirector != null && hostDirector != null) {
                success = _shallowMatchDirector(patternDirector, hostDirector);
            } else if (patternDirector != null) {
                success = false;
            }

            if (success) {
                IndexedLists patternMarkedList = new IndexedLists();
                NamedObj patternNextChild = findFirstChild(patternEntity,
                        patternMarkedList, _matchResult.keySet());
                while (patternNextChild != null) {
                    patternList.add(patternNextChild);
                    patternNextChild = findNextChild(patternEntity,
                            patternMarkedList, _matchResult.keySet());
                }

                IndexedLists hostMarkedList = new IndexedLists();
                NamedObj hostNextObject = findFirstChild(hostEntity,
                        hostMarkedList, _matchResult.values());
                while (hostNextObject != null) {
                    hostList.add(hostNextObject);
                    hostNextObject = findNextChild(hostEntity, hostMarkedList,
                            _matchResult.values());
                }
            }

            if (success) {
                for (Object portObject : patternEntity.portList()) {
                    if (!_isIgnored(portObject)) {
                        patternList.add(portObject);
                    }
                }
                for (Object portObject : hostEntity.portList()) {
                    if (!_isIgnored(portObject)) {
                        hostList.add(portObject);
                    }
                }
            }

            success = success && _matchObject(patternList, hostList);

            if (!success) {
                _matchResult.retain(matchSize);
            }
        }

        if (!success) {
            _parameterValues.retain(parameterSize);
        }

        return success;
    }

    private boolean _matchCompositeEntityAtAllLevels(
            CompositeEntity patternEntity, CompositeEntity hostEntity) {
        ObjectList patternList = new ObjectList();
        if (!_isIgnored(patternEntity)) {
            patternList.add(patternEntity);
        }
        ObjectList hostList = new ObjectList();
        if (!_isIgnored(hostEntity)) {
            hostList.add(hostEntity);
        }
        IndexedLists markedList = new IndexedLists();
        boolean added = true;
        int i = 0;
        ObjectList.Entry entry = hostList.getHead();

        while (added) {
            added = false;
            int size = hostList.size();
            for (; i < size; i++) {
                markedList.clear();
                hostEntity = (CompositeEntity) entry.getValue();

                NamedObj nextChild = findFirstChild(hostEntity, markedList,
                        _matchResult.keySet());
                while (nextChild != null) {
                    if (nextChild instanceof CompositeEntity) {
                        hostList.add(nextChild);
                        added = true;
                    }
                    nextChild = findNextChild(hostEntity, markedList,
                            _matchResult.keySet());
                }
                entry = entry.getNext();
            }
        }

        return _matchObject(patternList, hostList);
    }

    private boolean _matchList(LookbackEntry matchedObjectLists) {
        ObjectList patternList = matchedObjectLists.getPatternList();
        ObjectList hostList = matchedObjectLists.getHostList();

        int matchSize = _matchResult.size();
        boolean success = true;
        boolean patternChildChecked = false;

        boolean firstEntrance = !_matchResult.containsKey(patternList);
        FastLinkedList<LookbackEntry>.Entry lookbackTail = null;
        if (firstEntrance) {
            _matchResult.put(patternList, hostList);
            _lookbackList.add(matchedObjectLists);
            lookbackTail = _lookbackList.getTail();
        }

        ObjectList.Entry patternEntry = patternList.getHead();
        Object patternObject = null;
        while (patternEntry != null) {
            patternObject = patternEntry.getValue();
            if (_negation == _isNegated(patternObject)
                    && !_isIgnored(patternObject)) {
                break;
            }
            patternEntry = patternEntry.getNext();
        }

        NamedObj optionalContainer = null;

        if (patternEntry != null) {
            ObjectList.Entry previous = patternEntry.getPrevious();
            patternEntry.remove();

            if (patternObject instanceof NamedObj) {
                optionalContainer = _getOptionalContainer(
                        (NamedObj) patternObject);
                if (optionalContainer != null
                        && !_ignoredOptionalObjects.containsKey(
                                optionalContainer)) {
                    _ignoredOptionalObjects.put(optionalContainer, false);
                    _clearCaches();
                } else {
                    optionalContainer = null;
                }
            }

            patternChildChecked = true;
            success = false;
            ObjectList.Entry hostEntryPrevious = null;
            ObjectList.Entry hostEntry = hostList.getHead();
            while (hostEntry != null) {
                hostEntry.remove();
                Object hostObject = hostEntry.getValue();
                if (_matchObject(patternObject, hostObject)) {
                    success = true;
                }
                hostList.addEntryAfter(hostEntry, hostEntryPrevious);
                if (success) {
                    break;
                }
                hostEntryPrevious = hostEntry;
                hostEntry = hostEntry.getNext();
            }
            patternList.addEntryAfter(patternEntry, previous);
        }

        if (success) {
            if (!patternChildChecked) {
                if (_negation) {
                    matchedObjectLists.setNegated(true);
                } else {
                    matchedObjectLists.setFinished(true);
                }
                success = _checkBackward();
                if (_negation) {
                    matchedObjectLists.setNegated(false);
                } else {
                    matchedObjectLists.setFinished(false);
                }
            }
        }

        if (success == _negation) {
            if (!success || optionalContainer != null) {
                _matchResult.retain(matchSize);
            }
            if (optionalContainer != null) {
                _ignoredOptionalObjects.put(optionalContainer, true);
                _clearCaches();
                ObjectList.Entry previous = patternEntry.getPrevious();
                patternEntry.remove();
                success = _checkBackward();
                patternList.addEntryAfter(patternEntry, previous);
            }
        }

        if (firstEntrance) {
            lookbackTail.remove();
        }

        if (optionalContainer != null) {
            _ignoredOptionalObjects.remove(optionalContainer);
            _clearCaches();
        }

        return success;
    }

    private boolean _matchObject(Object patternObject, Object hostObject) {
        Object match = _matchResult.get(patternObject);
        if (match != null && match.equals(hostObject)) {
            return _checkBackward();
        } else if (match != null || _matchResult.containsValue(hostObject)) {
            return false;
        }

        if (patternObject instanceof CompositeEntity
                && hostObject instanceof CompositeEntity) {
            return _matchCompositeEntity((CompositeEntity) patternObject,
                    (CompositeEntity) hostObject);

        } else if (patternObject instanceof ComponentEntity
                && hostObject instanceof ComponentEntity) {
            return _matchAtomicEntity((ComponentEntity) patternObject,
                    (ComponentEntity) hostObject);

        } else if (patternObject instanceof ObjectList
                && hostObject instanceof ObjectList) {
            LookbackEntry matchedObjectLists = new LookbackEntry(
                    (ObjectList) patternObject, (ObjectList) hostObject);
            return _matchList(matchedObjectLists);

        } else if (patternObject instanceof Path && hostObject instanceof Path) {
            return _matchPath((Path) patternObject, (Path) hostObject);

        } else if (patternObject instanceof Port && hostObject instanceof Port) {
            return _matchPort((Port) patternObject, (Port) hostObject);

        } else if (patternObject instanceof Relation
                && hostObject instanceof Relation) {
            return _matchRelation((Relation) patternObject,
                    (Relation) hostObject);

        } else {
            return false;
        }
    }

    private boolean _matchPath(Path patternPath, Path hostPath) {

        if (!_shallowMatchPath(patternPath, hostPath)) {
            return false;
        }

        int matchSize = _matchResult.size();
        boolean success = true;

        _matchResult.put(patternPath, hostPath);

        Port patternPort = patternPath.getEndPort();
        ObjectList patternList = new ObjectList();
        if (!_isIgnored(patternPort)) {
            patternList.add(patternPort);
        }
        Port hostPort = hostPath.getEndPort();
        ObjectList hostList = new ObjectList();
        if (!_isIgnored(hostPort)) {
            hostList.add(hostPort);
        }
        success = _matchObject(patternList, hostList);

        if (!success) {
            _matchResult.retain(matchSize);
        }

        return success;
    }

    private boolean _matchPort(Port patternPort, Port hostPort) {
        if (patternPort instanceof GTEntity
                && !((GTEntity) patternPort).match(hostPort)) {
           return false;
        }

        int matchSize = _matchResult.size();
        boolean success = true;
        NamedObj patternContainer = null;
        NamedObj hostContainer = null;

        _matchResult.put(patternPort, hostPort);

        if (!_shallowMatchPort(patternPort, hostPort)) {
            success = false;
        }

        if (success) {
            patternContainer = patternPort.getContainer();
            hostContainer = hostPort.getContainer();

            Object patternObject = _matchResult.get(patternContainer);
            if (patternObject != null && patternObject != hostContainer) {
                success = false;
            } else {
                Object hostMatch = _matchResult.getKey(hostContainer);
                if (hostMatch != null && hostMatch != patternContainer) {
                    success = false;
                }
            }
        }

        if (success) {
            ObjectList patternList = new ObjectList();
            if (!_isIgnored(patternContainer)) {
                patternList.add(patternContainer);
            }
            ObjectList hostList = new ObjectList();
            if (!_isIgnored(hostContainer)) {
                hostList.add(hostContainer);
            }

            boolean collapsing = _relationCollapsing(
                    patternContainer.getContainer());

            if (collapsing) {
                _temporaryMatch.put(patternContainer, hostContainer);

                Path patternPath = new Path(patternPort);
                Set<Relation> visitedRelations = new HashSet<Relation>();
                Set<Port> visitedPorts = new HashSet<Port>();
                boolean foundPath = findFirstPath(patternPort, patternPath,
                        visitedRelations, visitedPorts);
                while (foundPath) {
                    patternList.add(patternPath.clone());
                    foundPath = findNextPath(patternPath, visitedRelations,
                            visitedPorts);
                }

                Path hostPath = new Path(hostPort);
                visitedRelations = new HashSet<Relation>();
                visitedPorts = new HashSet<Port>();
                foundPath = findFirstPath(hostPort, hostPath, visitedRelations,
                        visitedPorts);
                while (foundPath) {
                    hostList.add(hostPath.clone());
                    foundPath = findNextPath(hostPath, visitedRelations,
                            visitedPorts);
                }

                _temporaryMatch.remove(patternContainer);
            } else {
                for (Object relationObject : patternPort.linkedRelationList()) {
                    Relation relation = (Relation) relationObject;
                    if (!_isIgnored(relation)) {
                        patternList.add(relation);
                    }
                }
                for (Object relationObject : hostPort.linkedRelationList()) {
                    Relation relation = (Relation) relationObject;
                    if (!_isIgnored(relation)) {
                        hostList.add(relation);
                    }
                }
            }

            success = _matchObject(patternList, hostList);
        }

        if (!success) {
            _matchResult.retain(matchSize);
        }

        return success;
    }

    private boolean _matchRelation(Relation patternRelation,
            Relation hostRelation) {
        if (patternRelation instanceof GTEntity
                && !((GTEntity) patternRelation).match(hostRelation)) {
           return false;
        }

        int matchSize = _matchResult.size();
        boolean success = true;

        _matchResult.put(patternRelation, hostRelation);

        if (!_shallowMatchRelation(patternRelation, hostRelation)) {
            success = false;
        }

        if (success) {
            ObjectList patternList = new ObjectList();
            for (Object relationObject : patternRelation.linkedObjectsList()) {
                if (!_isIgnored(relationObject)) {
                    patternList.add(relationObject);
                }
            }

            ObjectList hostList = new ObjectList();
            for (Object relationObject : hostRelation.linkedObjectsList()) {
                if (!_isIgnored(relationObject)) {
                    hostList.add(relationObject);
                }
            }

            success = _matchObject(patternList, hostList);
        }

        if (!success) {
            _matchResult.retain(matchSize);
        }

        return success;
    }

    private static void _printMatch(MatchResult match) {
        List<Object> keyList = new LinkedList<Object>(match.keySet());
        Collections.sort(keyList, _comparator);
        for (Object patternObject : keyList) {
            if (patternObject instanceof NamedObj) {
                System.out.println(_getNameString(patternObject) + " : "
                        + _getNameString(match.get(patternObject)));
            }
        }
    }

    private boolean _shallowMatchDirector(Director patternDirector,
            Director hostDirector) {

        if (!_checkCriterion(patternDirector, hostDirector)) {
            return false;
        }

        if (patternDirector == null && hostDirector == null) {
            return true;
        } else if (patternDirector == null || hostDirector == null) {
            return false;
        }

        int matchSize = _matchResult.size();

        _matchResult.put(patternDirector, hostDirector);

        boolean success = patternDirector.getClass().equals(
                hostDirector.getClass());

        if (!success) {
            _matchResult.retain(matchSize);
        }

        return success;
    }

    private static boolean _shallowMatchPath(Path patternPath, Path hostPath) {
        Port patternStartPort = patternPath.getStartPort();
        Port hostStartPort = hostPath.getStartPort();
        Port patternEndPort = patternPath.getEndPort();
        Port hostEndPort = hostPath.getEndPort();

        return _shallowMatchPort(patternStartPort, hostStartPort)
                && _shallowMatchPort(patternEndPort, hostEndPort);
    }

    private static boolean _shallowMatchPort(Port patternPort, Port hostPort) {

        if (!_checkCriterion(patternPort, hostPort)) {
            return false;
        }

        if (patternPort instanceof IOPort) {
            if (hostPort instanceof IOPort) {
                IOPort patternIOPort = (IOPort) patternPort;
                IOPort hostIOPort = (IOPort) hostPort;
                Criterion criterion = _getDefiningCriterion(patternIOPort);
                if (criterion == null) {
                    boolean isInputEqual =
                        patternIOPort.isInput() == hostIOPort.isInput();
                    boolean isOutputEqual =
                        patternIOPort.isOutput() == hostIOPort.isOutput();
                    boolean isMultiportEqual =
                        patternIOPort.isMultiport() == hostIOPort.isMultiport();
                    boolean isNameEqual =
                        patternIOPort.getName().equals(hostIOPort.getName());

                    boolean isTypeCompatible = true;
                    if (patternIOPort instanceof TypedIOPort) {
                        if (hostIOPort instanceof TypedIOPort) {
                            Type patternType = ((TypedIOPort) patternIOPort)
                                    .getType();
                            Type hostType = ((TypedIOPort) hostIOPort)
                                    .getType();
                            if (patternIOPort.isInput() && hostIOPort.isInput()) {
                                isTypeCompatible = isTypeCompatible
                                        && hostType.isCompatible(patternType);
                            }
                            if (patternIOPort.isOutput()
                                    && hostIOPort.isOutput()) {
                                isTypeCompatible = isTypeCompatible
                                        && patternType.isCompatible(hostType);
                            }
                        } else {
                            isTypeCompatible = false;
                        }
                    }

                    return isInputEqual && isOutputEqual && isMultiportEqual
                            && isNameEqual && isTypeCompatible;
                } else {
                    return criterion.match(hostIOPort);
                }
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    private boolean _shallowMatchRelation(Relation patternRelation,
            Relation hostRelation) {

        if (!_checkCriterion(patternRelation, hostRelation)) {
            return false;
        }

        List<?> attributeList = patternRelation.attributeList(
                GTIngredientsAttribute.class);
        if (!attributeList.isEmpty()) {
            try {
                GTIngredientList ruleList = ((GTIngredientsAttribute)
                        attributeList.get(0)).getIngredientList();
                if (ruleList != null) {
                    for (GTIngredient rule : ruleList) {
                        if (rule instanceof Criterion) {
                            Criterion criterion = (Criterion) rule;
                            if (criterion.canCheck(patternRelation)) {
                                if (!criterion.match(hostRelation)) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            } catch (MalformedStringException e) {
                return false;
            }
        }
        return true;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    private Map<Object, Boolean> _cachedCreatedObjects =
        new HashMap<Object, Boolean>();

    private Map<Object, Boolean> _cachedIgnoredObjects =
        new HashMap<Object, Boolean>();

    private Map<Object, Boolean> _cachedNegatedObjects =
        new HashMap<Object, Boolean>();

    private Map<NamedObj, Object> _cachedOptionalContainers =
        new HashMap<NamedObj, Object>();

    private MatchCallback _callback = DEFAULT_CALLBACK;

    /** The objects in the pattern that implement the MatchCallback interface,
     *  which are to be invoked when a match is found. All these objects are
     *  invoked when a match is found, and if they all return true, the callback
     *  set by the user (using {@link #setMatchCallback(MatchCallback)}) is
     *  invoked. If not all of them return true, a false is returned by the
     *  matcher without invoking the callback set by the user.
     */
    private List<MatchCallback> _callbacksInPattern =
        new LinkedList<MatchCallback>();

    private static final NameComparator _comparator = new NameComparator();

    private Map<Object, Boolean> _ignoredOptionalObjects =
        new HashMap<Object, Boolean>();

    private LookbackList _lookbackList = new LookbackList();

    /** The map that matches objects in the pattern to the objects in the host.
     *  These objects include actors, ports, relations, etc.
     */
    private MatchResult _matchResult;

    private boolean _negation = false;

    private SequentialTwoWayHashMap<ValueIterator, Token> _parameterValues =
        new SequentialTwoWayHashMap<ValueIterator, Token>();

    /** The variable that indicates whether the last match operation is
     *  successful. (See {@link #match(CompositeActorMatcher, NamedObj)})
     */
    private boolean _success = false;

    private MatchResult _temporaryMatch = new MatchResult();

    ///////////////////////////////////////////////////////////////////
    ////                      private inner classes                ////

    private static class LookbackEntry extends Tuple<Object> {

        public ObjectList getHostList() {
            return (ObjectList) get(1);
        }

        public ObjectList getPatternList() {
            return (ObjectList) get(0);
        }

        public boolean isFinished() {
            return (Boolean) get(2);
        }

        public boolean isNegated() {
            return (Boolean) get(3);
        }

        public void setFinished(boolean finished) {
            set(2, finished);
        }

        public void setNegated(boolean negated) {
            set(3, negated);
        }

        LookbackEntry(ObjectList patternList, ObjectList hostList) {
            super(patternList, hostList, false, false);
        }

    }

    private static class LookbackList extends FastLinkedList<LookbackEntry> {
    }

    private static class NameComparator implements Comparator<Object> {

        public int compare(Object object1, Object object2) {
            return _getNameString(object1).compareTo(_getNameString(object2));
        }
    }

    private static class ObjectList extends FastLinkedList<Object> {
    }

    private class ParameterIterator extends GraphAnalyzer {

        public boolean next() {
            if (_firstTime) {
                _firstTime = false;
                return true;
            }
            _computeNext();
            return _valueIterators != null;
        }

        protected boolean _isOpaque(CompositeEntity entity) {
            return !GraphMatcher.this._isOpaque(entity);
        }

        protected boolean _relationCollapsing(NamedObj container) {
            return GraphMatcher.this._relationCollapsing(container);
        }

        ParameterIterator(ComponentEntity entity)
                throws IllegalActionException {
            List<?> valueIterators = entity.attributeList(ValueIterator.class);
            for (Object iteratorObject : valueIterators) {
                ValueIterator iterator = (ValueIterator) iteratorObject;
                Token initial = iterator.initial();
                _parameterValues.put(iterator, initial);
                _valueIterators.add(iterator);
            }

            if (entity instanceof CompositeEntity) {
                CompositeEntity composite = (CompositeEntity) entity;
                IndexedLists markedList = new IndexedLists();
                List<Object> excludedObjects = new LinkedList<Object>();
                NamedObj nextChild = findFirstChild(composite, markedList,
                        excludedObjects);
                while (nextChild != null) {
                    if (nextChild instanceof ComponentEntity) {
                        valueIterators = nextChild.attributeList(
                                ValueIterator.class);
                        for (Object iteratorObject : valueIterators) {
                            ValueIterator iterator =
                                (ValueIterator) iteratorObject;
                            Token initial = iterator.initial();
                            _parameterValues.put(iterator, initial);
                            _valueIterators.add(iterator);
                        }
                    }
                    nextChild = findNextChild(composite, markedList,
                            excludedObjects);
                }
            }
        }

        private void _computeNext() {
            int i = _valueIterators.size();
            boolean terminate = false;
            boolean found = false;
            while (!terminate && !found) {
                ListIterator<ValueIterator> iterators =
                    _valueIterators.listIterator(i);
                terminate = true;
                while (iterators.hasPrevious()) {
                    ValueIterator iterator = iterators.previous();
                    _parameterValues.removeLast();
                    try {
                        _clearCaches();
                        Token next = iterator.next();
                        _parameterValues.put(iterator, next);
                        terminate = false;
                        break;
                    } catch (IllegalActionException e) {
                    }
                    i--;
                }
                if (!terminate) {
                    iterators.next();
                    found = true;
                    while (iterators.hasNext()) {
                        ValueIterator iterator = iterators.next();
                        try {
                            Token initial = iterator.initial();
                            _parameterValues.put(iterator, initial);
                        } catch (IllegalActionException e) {
                            found = false;
                            break;
                        }
                        i++;
                    }
                }
            }
            if (!found) {
                _valueIterators = null;
            }
        }

        private boolean _firstTime = true;

        private List<ValueIterator> _valueIterators =
            new LinkedList<ValueIterator>();
    }
}
