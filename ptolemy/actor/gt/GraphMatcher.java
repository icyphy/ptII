/* Implementation of a recursive algorithm to match a pattern to any subgraph of
   a graph.

 Copyright (c) 1997-2008 The Regents of the University of California.
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
package ptolemy.actor.gt;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.GTIngredient.NamedObjMatchResult;
import ptolemy.actor.gt.data.FastLinkedList;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.data.Pair;
import ptolemy.actor.gt.ingredients.criteria.AttributeCriterion;
import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.actor.gt.ingredients.criteria.PortCriterion;
import ptolemy.actor.gt.ingredients.criteria.SubclassCriterion;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.data.type.Type;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
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
        return _match;
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
        _match = new MatchResult();

        // Temporary data structures.
        _lookbackList = new LookbackList();
        _temporaryMatch = new MatchResult();

        _success = _matchChildrenCompositeEntity(pattern, hostGraph);

        assert _lookbackList.isEmpty();
        assert _temporaryMatch.isEmpty();
        if (!_success) {
            assert _match.isEmpty();
        }

        // Clear temporary data structures to free memory.
        _lookbackList = null;

        return _success;
    }

    /** Match the rule stored in the file with name <tt>ruleXMLFile</tt> to the
     *  model stored in the file with name <tt>hostXMLFile</tt>, whose top-level
     *  should be an instanceof {@link CompositeEntity}. The first match result
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
     *  should be an instanceof {@link CompositeEntity}, and invoke
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

    protected Token _getAttribute(NamedObj container, String name,
            Class<? extends TransformationAttribute> attributeClass) {

        while (container != null) {
            if (_match.containsValue(container)) {
                container = (NamedObj) _match.getKey(container);
            } else if (_temporaryMatch.containsValue(container)) {
                container = (NamedObj) _temporaryMatch.getKey(container);
            }
            Attribute attribute = container.getAttribute(name);
            if (attribute != null && attributeClass.isInstance(attribute)) {
                Parameter parameter = (Parameter) attribute.attributeList()
                        .get(0);
                try {
                    return parameter == null ? null : parameter.getToken();
                } catch (IllegalActionException e) {
                    return null;
                }
            }
            container = container.getContainer();
        }

        return null;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

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
            Token value = _getAttribute(container, "HierarchyFlattening",
                    HierarchyFlatteningAttribute.class);
            boolean isOpaque = value == null ? true : !((BooleanToken) value)
                    .booleanValue();
            return isOpaque;
        }
    }

    /** Check the items in the lookback list for more matching requirements. If
     *  no more requirements are found (i.e., all the lists in the lookback list
     *  have been fully explored), then a match is found, and the callback's
     *  {@link MatchCallback#foundMatch(GraphMatcher)} is invoked. If that
     *  method returns true, the matching process terminates; otherwise, the
     *  matching proceeds by backtracking.
     *
     *  @return Whether the match is successful.
     *  @see #_lookbackList
     */
    private boolean _checkBackward() {
        FastLinkedList<LookbackEntry>.Entry entry = _lookbackList.getTail();
        LookbackEntry lists = null;
        while (entry != null) {
            lists = entry.getValue();
            if (!lists.isFinished()) {
                break;
            }
            entry = entry.getPrevious();
        }
        if (entry == null) {
            if (_checkConstraints()) {
                return _callback.foundMatch(this);
            } else {
                return false;
            }
        } else {
            return _matchList(lists);
        }
    }

    private boolean _checkConstraint(Pattern pattern, Constraint constraint) {
        return constraint.check(pattern, _match);
    }

    private boolean _checkConstraints() {
        if (_match.isEmpty()) {
            return false;
        }

        Iterator<Map.Entry<Object, Object>> iterator = _match.entrySet()
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
        while (patternContainer != null && _match.containsKey(patternContainer)) {
            patternObject = patternContainer;
            patternContainer = patternContainer.getContainer();
        }
        if (!(patternObject instanceof Pattern)) {
            // The top-level container for the matching is not a pattern, so it
            // has no constraint to check.
            return true;
        }

        Pattern pattern = (Pattern) patternObject;
        List<?> constraints = pattern.attributeList(Constraint.class);
        for (Object constraintObject : constraints) {
            Constraint constraint = (Constraint) constraintObject;
            if (!_checkConstraint(pattern, constraint)) {
                return false;
            }
        }
        return true;
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

    private static PortCriterion _getPortRule(Port port) {
        if (port instanceof PortMatcher) {
            return ((PortMatcher) port).getPortCriterion();
        } else {
            return null;
        }
    }

    private boolean _matchAtomicEntity(ComponentEntity patternActor,
            ComponentEntity hostActor) {
        int matchSize = _match.size();
        boolean success = true;
        ObjectList patternList = new ObjectList();
        ObjectList hostList = new ObjectList();

        _match.put(patternActor, hostActor);

        if (patternActor instanceof AtomicActorMatcher) {
            AtomicActorMatcher matcher = (AtomicActorMatcher) patternActor;

            GTIngredientList ruleList = null;
            try {
                ruleList = matcher.criteria.getIngredientList();
            } catch (MalformedStringException e) {
                success = false;
            }

            if (success) {
                for (GTIngredient rule : ruleList) {
                    if (rule instanceof AttributeCriterion
                            || rule instanceof SubclassCriterion) {
                        success = ((Criterion) rule).match(hostActor) == NamedObjMatchResult.MATCH;
                        if (!success) {
                            break;
                        }
                    }
                }
            }
        } else {
            success = patternActor.getClass().isInstance(hostActor);
        }

        if (success) {
            patternList.addAll((Collection<?>) patternActor.portList());
            hostList.addAll((Collection<?>) hostActor.portList());
        }

        success = success && _matchObject(patternList, hostList);

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private boolean _matchChildrenCompositeEntity(
            CompositeEntity patternEntity, CompositeEntity hostEntity) {
        ObjectList patternList = new ObjectList();
        patternList.add(patternEntity);
        ObjectList hostList = new ObjectList();
        hostList.add(hostEntity);
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
                        _match.keySet());
                while (nextChild != null) {
                    if (nextChild instanceof CompositeEntity) {
                        hostList.add(nextChild);
                        added = true;
                    }
                    nextChild = findNextChild(hostEntity, markedList, _match
                            .keySet());
                }
                entry = entry.getNext();
            }
        }

        return _matchObject(patternList, hostList);
    }

    private boolean _matchCompositeEntity(CompositeEntity patternEntity,
            CompositeEntity hostEntity) {
        int matchSize = _match.size();
        boolean success = true;
        ObjectList patternList = new ObjectList();
        ObjectList hostList = new ObjectList();

        _match.put(patternEntity, hostEntity);

        Director patternDirector = null;
        Director hostDirector = null;
        if (patternEntity instanceof CompositeActor
                && ((CompositeActor) patternEntity).isOpaque()) {
            patternDirector = ((CompositeActor) patternEntity).getDirector();
        }
        if (hostEntity instanceof CompositeActor
                && ((CompositeActor) hostEntity).isOpaque()) {
            hostDirector = ((CompositeActor) hostEntity).getDirector();
        }
        if (patternDirector != null && hostDirector != null) {
            success = _shallowMatchDirector(patternDirector, hostDirector);
        } else if (patternDirector != null) {
            success = false;
        }

        if (success) {
            IndexedLists patternMarkedList = new IndexedLists();
            NamedObj patternNextChild = findFirstChild(patternEntity,
                    patternMarkedList, _match.keySet());
            while (patternNextChild != null) {
                patternList.add(patternNextChild);
                patternNextChild = findNextChild(patternEntity,
                        patternMarkedList, _match.keySet());
            }

            IndexedLists hostMarkedList = new IndexedLists();
            NamedObj hostNextObject = findFirstChild(hostEntity,
                    hostMarkedList, _match.values());
            while (hostNextObject != null) {
                hostList.add(hostNextObject);
                hostNextObject = findNextChild(hostEntity, hostMarkedList,
                        _match.values());
            }
        }

        if (success) {
            patternList.addAll((Collection<?>) patternEntity.portList());
            hostList.addAll((Collection<?>) hostEntity.portList());
        }

        success = success && _matchObject(patternList, hostList);

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private boolean _matchList(LookbackEntry matchedObjectLists) {
        ObjectList patternList = matchedObjectLists.getPatternList();
        ObjectList hostList = matchedObjectLists.getHostList();

        int matchSize = _match.size();
        boolean success = true;
        boolean patternChildChecked = false;

        boolean firstEntrance = !_match.containsKey(patternList);
        FastLinkedList<LookbackEntry>.Entry lookbackTail = null;
        if (firstEntrance) {
            _match.put(patternList, hostList);
            _lookbackList.add(matchedObjectLists);
            lookbackTail = _lookbackList.getTail();
        }

        ObjectList.Entry patternEntry = patternList.getHead();
        if (patternEntry != null) {
            patternEntry.remove();
            Object patternObject = patternEntry.getValue();
            patternChildChecked = true;
            success = false;
            ObjectList.Entry hostEntryPrevious = null;
            ObjectList.Entry hostEntry = hostList.getHead();
            while (hostEntry != null) {
                hostEntry.remove();
                Object hostObject = hostEntry.getValue();
                if (_matchObject(patternObject, hostObject)) {
                    success = true;
                    break;
                }
                hostList.addEntryAfter(hostEntry, hostEntryPrevious);
                hostEntryPrevious = hostEntry;
                hostEntry = hostEntry.getNext();
            }
            patternList.addEntryToHead(patternEntry);
        }

        if (success) {
            if (!patternChildChecked) {
                matchedObjectLists.setFinished(true);
                success = _checkBackward();
                matchedObjectLists.setFinished(false);
                if (!success) {
                    _match.retain(matchSize);
                    if (firstEntrance) {
                        lookbackTail.remove();
                    }
                }
            }
            return success;
        } else {
            _match.retain(matchSize);
            if (firstEntrance) {
                lookbackTail.remove();
            }
            return false;
        }
    }

    private boolean _matchObject(Object patternObject, Object hostObject) {
        Object match = _match.get(patternObject);
        if (match != null && match.equals(hostObject)) {
            return _checkBackward();
        } else if (match != null || _match.containsValue(hostObject)) {
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

        int matchSize = _match.size();
        boolean success = true;

        _match.put(patternPath, hostPath);

        Port patternPort = patternPath.getEndPort();
        Port hostPort = hostPath.getEndPort();
        success = _matchObject(patternPort, hostPort);

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private boolean _matchPort(Port patternPort, Port hostPort) {

        int matchSize = _match.size();
        boolean success = true;
        NamedObj patternContainer = null;
        NamedObj hostContainer = null;

        _match.put(patternPort, hostPort);

        if (!_shallowMatchPort(patternPort, hostPort)) {
            success = false;
        }

        if (success) {
            patternContainer = patternPort.getContainer();
            hostContainer = hostPort.getContainer();

            Object patternObject = _match.get(patternContainer);
            if (patternObject != null && patternObject != hostContainer) {
                success = false;
            } else {
                Object hostMatch = _match.getKey(hostContainer);
                if (hostMatch != null && hostMatch != patternContainer) {
                    success = false;
                }
            }
        }

        if (success) {
            ObjectList patternList = new ObjectList();
            patternList.add(patternContainer);
            ObjectList hostList = new ObjectList();
            hostList.add(hostContainer);

            Token collapsingToken = _getAttribute(patternContainer
                    .getContainer(), "RelationCollapsing",
                    RelationCollapsingAttribute.class);
            boolean collapsing = collapsingToken == null ? false
                    : ((BooleanToken) collapsingToken).booleanValue();

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
                    if (!_ignoreRelation(relation)) {
                        patternList.add(relation);
                    }
                }
                for (Object relationObject : hostPort.linkedRelationList()) {
                    Relation relation = (Relation) relationObject;
                    if (!_ignoreRelation(relation)) {
                        hostList.add(relation);
                    }
                }
            }

            success = _matchObject(patternList, hostList);
        }

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private boolean _matchRelation(Relation patternRelation,
            Relation hostRelation) {

        int matchSize = _match.size();
        boolean success = true;

        _match.put(patternRelation, hostRelation);

        if (!_shallowMatchRelation(patternRelation, hostRelation)) {
            success = false;
        }

        if (success) {
            ObjectList patternList = new ObjectList();
            patternList.addAll((Collection<?>) patternRelation
                    .linkedObjectsList());

            ObjectList hostList = new ObjectList();
            hostList.addAll((Collection<?>) hostRelation.linkedObjectsList());

            success = _matchObject(patternList, hostList);
        }

        if (!success) {
            _match.retain(matchSize);
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

        if (patternDirector == null && hostDirector == null) {
            return true;
        } else if (patternDirector == null || hostDirector == null) {
            return false;
        }

        int matchSize = _match.size();

        _match.put(patternDirector, hostDirector);

        boolean success = patternDirector.getClass().equals(
                hostDirector.getClass());

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private static boolean _shallowMatchPath(Path patternPath, Path hostPath) {
        Port patternStartPort = patternPath.getStartPort();
        Port hostStartPort = hostPath.getStartPort();
        Port patternEndPort = patternPath.getEndPort();
        Port hostEndPort = hostPath.getEndPort();

        // TODO: Check the relations and ports in between.

        return _shallowMatchPort(patternStartPort, hostStartPort)
                && _shallowMatchPort(patternEndPort, hostEndPort);
    }

    private static boolean _shallowMatchPort(Port patternPort, Port hostPort) {
        if (patternPort instanceof IOPort) {
            if (hostPort instanceof IOPort) {
                IOPort patternIOPort = (IOPort) patternPort;
                IOPort hostIOPort = (IOPort) hostPort;
                PortCriterion portRule = _getPortRule(patternIOPort);
                if (portRule == null) {
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
                    return portRule.match(hostIOPort) == NamedObjMatchResult.MATCH;
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
        // TODO
        return true;
    }

    private MatchCallback _callback = DEFAULT_CALLBACK;

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    private static final NameComparator _comparator = new NameComparator();

    private LookbackList _lookbackList;

    /** The map that matches objects in the pattern to the objects in the host.
     *  These objects include actors, ports, relations, etc.
     */
    private MatchResult _match;

    /** The variable that indicates whether the last match operation is
     *  successful. (See {@link #match(CompositeActorMatcher, NamedObj)})
     */
    private boolean _success = false;

    private MatchResult _temporaryMatch;

    ///////////////////////////////////////////////////////////////////
    ////                      private inner classes                ////

    private static class LookbackEntry extends
            Pair<Pair<ObjectList, ObjectList>, Boolean> {

        public ObjectList getHostList() {
            return getFirst().getSecond();
        }

        public ObjectList getPatternList() {
            return getFirst().getFirst();
        }

        public boolean isFinished() {
            return getSecond();
        }

        public void setFinished(boolean finished) {
            setSecond(finished);
        }

        LookbackEntry(ObjectList patternList, ObjectList hostList) {
            super(new Pair<ObjectList, ObjectList>(patternList, hostList),
                    false);
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
}
