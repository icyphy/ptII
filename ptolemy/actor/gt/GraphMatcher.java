/* A recursive algorithm to match a pattern to subgraphs of a graph.

 Copyright (c) 1997-2005 The Regents of the University of California.
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.CompositeActor;
import ptolemy.actor.Director;
import ptolemy.actor.TypedIOPort;
import ptolemy.actor.gt.data.FastLinkedList;
import ptolemy.actor.gt.data.MatchResult;
import ptolemy.actor.gt.data.Pair;
import ptolemy.actor.gt.rules.SubclassRule;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

/**
 A recursive algorithm to match a pattern to subgraphs of a graph. The pattern
 is specified as the <em>pattern</em> part of a graph transformation rule. The
 graph to be matched to, called <em>host graph</em>, is an arbitrary Ptolemy II
 model, whose top level is an instance of {@link CompositeEntity}.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class GraphMatcher {

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
        if (!(args.length == 2 ||
                (args.length == 3 && args[0].equalsIgnoreCase("-A")))) {
            System.err.println("USAGE: java [-A] "
                    + GraphMatcher.class.getName() + " <lhs.xml> <host.xml>");
            System.exit(1);
        }

        final boolean all = args.length == 3 && args[0].equalsIgnoreCase("-A");
        String lhsXMLFile = all ? args[1] : args[0];
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
        match(lhsXMLFile, hostXMLFile, matchCallback);
    }

    /** Match a pattern specified in the <tt>lhsGraph</tt> to a model in
     *  <tt>hostGraph</tt>. If the match is successful, <tt>true</tt> is
     *  returned, and the match result is stored internally, which can be
     *  retrieved with {@link #getMatchResult()}. A matching was successful if
     *  at least one match result was found, and the callback (an instance
     *  implementing {@link MatchCallback}) returned <tt>true</tt> when it was
     *  invoked.
     *
     *  @param lhsGraph The pattern.
     *  @param hostGraph The host graph.
     *  @return <tt>true</tt> if the match is successful; <tt>false</tt>
     *   otherwise.
     */
    public boolean match(CompositeActorMatcher lhsGraph,
            CompositeEntity hostGraph) {

        // Matching result.
        _match = new MatchResult();

        // Temporary data structures.
        _lookbackList = new LookbackList();

        _success = _matchObject(lhsGraph, hostGraph);

        assert _lookbackList.isEmpty();
        if (!_success) {
            assert _match.isEmpty();
        }

        // Clear temporary data structures to free memory.
        _lookbackList = null;

        return _success;
    }

    /** Match the rule stored in the file with name <tt>lhsXMLFile</tt> to the
     *  model stored in the file with name <tt>hostXMLFile</tt>, whose top-level
     *  should be an instanceof {@link CompositeEntity}. The first match result
     *  (which is arbitrarily decided by the recursive algorithm) is recorded in
     *  the returned matcher object. This result can be obtained with {@link
     *  #getMatchResult()}. If the match is unsuccessful, the match result is
     *  empty, and {@link #isSuccessful()} of the returned matcher object
     *  returns <tt>false</tt>.
     *
     *  @param lhsXMLFile The name of the file in which the rule is stored.
     *  @param hostXMLFile The name of the file in which the model to be matched
     *   is stored.
     *  @return A matcher object with the first match result stored in it. If no
     *   match is found, {@link #isSuccessful()} of the matcher object returns
     *   <tt>false</tt>, and {@link #getMatchResult()} returns an empty match.
     *  @exception Exception If the rule file or the model file cannot be read.
     *  @see #match(String, String, MatchCallback)
     */
    public static GraphMatcher match(String lhsXMLFile, String hostXMLFile)
    throws Exception {
        return match(lhsXMLFile, hostXMLFile, null);
    }

    /** Match the rule stored in the file with name <tt>lhsXMLFile</tt> to the
     *  model stored in the file with name <tt>hostXMLFile</tt>, whose top-level
     *  should be an instanceof {@link CompositeEntity}, and invoke
     *  <tt>callback</tt>'s {@link MatchCallback#foundMatch(GraphMatcher)}
     *  method whenever a match is found. If the callback returns <tt>true</tt>,
     *  the match will terminate and no more matches will be reported;
     *  otherwise, the match process continues, and the callback may be invoked
     *  again. If <tt>callback</tt> is <tt>null</tt>, the behavior is the same
     *  as {@link #match(String, String)}.
     *
     *  @param lhsXMLFile The name of the file in which the rule is stored.
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
    public static GraphMatcher match(String lhsXMLFile, String hostXMLFile,
            MatchCallback callback) throws Exception {
        MoMLParser parser = new MoMLParser();
        SingleRuleTransformer rule = (SingleRuleTransformer)
                parser.parse(null, new File(lhsXMLFile).toURI().toURL());
        parser.reset();
        CompositeEntity host = (CompositeEntity)
            parser.parse(null, new File(hostXMLFile).toURI().toURL());

        GraphMatcher matcher = new GraphMatcher();
        if (callback != null) {
            matcher.setMatchCallback(callback);
        }
        matcher.match(rule.getPattern(), host);
        return matcher;
    }

    /** Set the callback to be invoked by future calls to {@link
     *  #match(CompositeActorMatcher, CompositeEntity)}.
     *
     *  @param callback The callback. If it is <tt>null</tt>, the callback is
     *   set to {@link #DEFAULT_CALLBACK}.
     *  @see #match(CompositeActorMatcher, CompositeEntity)
     */
    public void setMatchCallback(MatchCallback callback) {
        if (callback == null) {
            _callback = DEFAULT_CALLBACK;
        } else {
            _callback = callback;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                          public fields                    ////

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
    ////                         private methods                   ////

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
            return _callback.foundMatch(this);
        } else {
            return _matchList(lists);
        }
    }

    /** Find the first child within the top composite entity. The child is
     *  either an atomic actor ({@link AtomicActor}) or an opaque composite
     *  entity, one that has a director in it. If the top composite entity does
     *  not have any child, <tt>null</tt> is returned.
     * 
     *  @param top The top composite entity in which the search is performed.
     *  @param indexedLists A list that is used to encode the composite entities
     *   visited.
     *  @param excludedEntities The atomic actor or opaque composite entities
     *   that should not be returned.
     *  @return The child found, or <tt>null</tt> if none.
     *  @see #_findNextChild(CompositeEntity, IndexedLists, Collection)
     */
    private static ComponentEntity _findFirstChild(CompositeEntity top,
            IndexedLists indexedLists, Collection<Object> excludedEntities) {

        List<?> entities = top.entityList(ComponentEntity.class);

        if (!entities.isEmpty()) {
            int i = 0;
            IndexedList currentList = new IndexedList(entities, 0);
            indexedLists.add(currentList);
            IndexedLists.Entry currentListEntry = indexedLists.getTail();

            for (Object entityObject : entities) {
                currentList.setSecond(i);
                if (entityObject instanceof AtomicActor
                        || entityObject instanceof CompositeEntity
                        && _isOpaque((CompositeEntity) entityObject)) {
                    if (!excludedEntities.contains(entityObject)) {
                        return (ComponentEntity) entityObject;
                    }
                } else {
                    CompositeEntity compositeEntity =
                        (CompositeEntity) entityObject;
                    ComponentEntity child = _findFirstChild(compositeEntity,
                            indexedLists, excludedEntities);
                    if (child != null && !excludedEntities.contains(child)) {
                        return child;
                    }
                }
                i++;
            }

            currentListEntry.remove();
        }

        return null;
    }

    /** Find the first path starting from the <tt>startPort</tt>, and store it
     *  in the <tt>path</tt> parameter if found. A path is a sequence of
     *  alternating ports ({@link Port}) and relations ({@link Relation}),
     *  starting and ending with two ports (which may be equal, in which case
     *  the path is a loop). If a path contains ports between the start port and
     *  the end port, those ports in between must be ports of a transparent
     *  composite entities (those with no directors inside). If no path is found
     *  starting from the <tt>startPort</tt>, <tt>null</tt> is returned.
     * 
     *  @param startPort The port from which the search starts.
     *  @param path The path to obtain the result.
     *  @param visitedRelations A set that records all the relations that have
     *   been visited during the search.
     *  @param visitedPorts A set that records all the ports that have been
     *   visited during the search.
     *  @return <tt>true</tt> if a path is found and stored in the <tt>path</tt>
     *   parameter; <tt>false</tt> otherwise.
     *  @see #_findNextPath(Path, Set, Set)
     */
    @SuppressWarnings("unchecked")
    private static boolean _findFirstPath(Port startPort, Path path,
            Set<? super Relation> visitedRelations,
            Set<? super Port> visitedPorts) {
        List<?> relationList = startPort.linkedRelationList();
        if (startPort instanceof ComponentPort) {
            relationList.addAll(((TypedIOPort) startPort).insideRelationList());
        }

        int i = 0;
        IndexedList currentList = new IndexedList(relationList, 0);
        path.add(currentList);
        Path.Entry currentListEntry = path.getTail();

        for (Object relationObject : relationList) {
            Relation relation = (Relation) relationObject;
            if (visitedRelations.contains(relation)) {
                i++;
                continue;
            }

            currentList.setSecond(i);
            visitedRelations.add(relation);
            List<?> portList = relation.linkedPortList();

            int j = 0;
            IndexedList currentList2 = new IndexedList(portList, 0);
            path.add(currentList2);
            Path.Entry currentListEntry2 = path.getTail();

            for (Object portObject : portList) {
                Port port = (Port) portObject;
                if (visitedPorts.contains(port)) {
                    j++;
                    continue;
                }

                currentList2.setSecond(j);
                visitedPorts.add(port);
                NamedObj container = port.getContainer();

                boolean reachEnd = true;
                if (container instanceof CompositeEntity) {
                    if (!_isOpaque((CompositeEntity) container)) {
                        if (_findFirstPath(port, path, visitedRelations,
                                visitedPorts)) {
                            return true;
                        } else {
                            reachEnd = false;
                        }
                    }
                }

                if (reachEnd) {
                    return true;
                } else {
                    visitedPorts.remove(port);
                    j++;
                }
            }

            currentListEntry2.remove();
            visitedRelations.remove(relation);
            i++;
        }

        currentListEntry.remove();

        return false;
    }

    /** Find the next child within the top composite entity. The child is either
     *  an atomic actor ({@link AtomicActor}) or an opaque composite entity, one
     *  that has a director in it. If the top composite entity does not have any
     *  more child, <tt>null</tt> is returned.
     * 
     *  @param top The top composite entity in which the search is performed.
     *  @param indexedLists A list that is used to encode the composite entities
     *   visited.
     *  @param excludedEntities The atomic actor or opaque composite entities
     *   that should not be returned.
     *  @return The child found, or <tt>null</tt> if none.
     *  @see #_findFirstChild(CompositeEntity, IndexedLists, Collection)
     */
    private static ComponentEntity _findNextChild(CompositeEntity top,
            IndexedLists indexedLists, Collection<Object> excludedEntities) {
        if (indexedLists.isEmpty()) {
            return _findFirstChild(top, indexedLists, excludedEntities);
        } else {
            IndexedLists.Entry entry = indexedLists.getTail();
            while (entry != null) {
                IndexedList indexedList = entry.getValue();
                List<?> entityList = indexedList.getFirst();
                for (int index = indexedList.getSecond() + 1;
                       index < entityList.size(); index++) {
                    indexedList.setSecond(index);
                    ComponentEntity entity =
                        (ComponentEntity) entityList.get(index);
                    if (!excludedEntities.contains(entity)) {
                        indexedLists.removeAllAfter(entry);
                        if (entity instanceof AtomicActor
                                || entity instanceof CompositeEntity
                                && _isOpaque((CompositeEntity) entity)) {
                            return entity;
                        } else {
                            CompositeEntity compositeEntity =
                                (CompositeEntity) entity;
                            ComponentEntity child = _findFirstChild(
                                    compositeEntity, indexedLists,
                                    excludedEntities);
                            if (child != null) {
                                return child;
                            }
                        }
                    }
                }
                entry = entry.getPrevious();
            }
            indexedLists.clear();
            return null;
        }
    }

    /** Find the next path, and store it in the <tt>path</tt> parameter if
     *  found. A path is a sequence of alternating ports ({@link Port}) and
     *  relations ({@link Relation}), starting and ending with two ports (which
     *  may be equal, in which case the path is a loop). If a path contains
     *  ports between the start port and the end port, those ports in between
     *  must be ports of a transparent composite entities (those with no
     *  directors inside). If no more path is found, <tt>null</tt> is returned.
     * 
     *  @param path The path to obtain the result.
     *  @param visitedRelations A set that records all the relations that have
     *   been visited during the search.
     *  @param visitedPorts A set that records all the ports that have been
     *   visited during the search.
     *  @return <tt>true</tt> if a path is found and stored in the <tt>path</tt>
     *   parameter; <tt>false</tt> otherwise.
     *  @see #_findFirstPath(Port, Path, Set, Set)
     */
    @SuppressWarnings("unchecked")
    private static boolean _findNextPath(Path path,
            Set<Relation> visitedRelations, Set<Port> visitedPorts) {
        Path.Entry entry = path.getTail();
        while (entry != null) {
            IndexedList markedEntityList = entry.getValue();
            List<?> entityList = markedEntityList.getFirst();
            for (int index = markedEntityList.getSecond() + 1;
                   index < entityList.size(); index++) {
                markedEntityList.setSecond(index);
                path.removeAllAfter(entry);

                Object nextObject = entityList.get(index);
                if (nextObject instanceof Port) {
                    Port port = (Port) nextObject;
                    if (visitedPorts.contains(port)) {
                        continue;
                    }

                    visitedPorts.add(port);

                    NamedObj container = port.getContainer();
                    if (!(container instanceof CompositeEntity)
                            || _isOpaque((CompositeEntity) container)) {
                        return true;
                    }

                    if (_findFirstPath(port, path, visitedRelations,
                            visitedPorts)) {
                        return true;
                    }

                    visitedPorts.remove(port);
                } else {
                    Relation relation = (Relation) nextObject;
                    if (visitedRelations.contains(relation)) {
                        continue;
                    }

                    visitedRelations.add(relation);
                    List<?> portList = relation.linkedPortList();

                    int i = 0;
                    for (Object portObject : portList) {
                        Port port = (Port) portObject;
                        if (visitedPorts.contains(port)) {
                            i++;
                            continue;
                        }

                        path.add(new IndexedList(portList, i));
                        visitedPorts.add(port);
                        NamedObj container = port.getContainer();
                        if (!(container instanceof CompositeEntity)
                                || _isOpaque((CompositeEntity) container)) {
                            return true;
                        }

                        if (_findFirstPath(port, path, visitedRelations,
                                visitedPorts)) {
                            return true;
                        } else {
                            visitedPorts.remove(port);
                        }
                    }

                    visitedRelations.remove(relation);
                }

                if (_findNextPath(path, visitedRelations, visitedPorts)) {
                    return true;
                }
            }
            entry = entry.getPrevious();
        }
        return false;
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

    /** Test whether the composite entity is opaque or not. Return <tt>true</tt>
     *  if the composite entity is an instance of {@link CompositeActor} and it
     *  is opaque. A composite actor is opaque if it has a director inside,
     *  which means the new level of hierarchy that it creates cannot be
     *  flattened.
     *
     *  @param entity The composite entity to be tested.
     *  @return <tt>true</tt> if the composite entity is an instance of {@link
     *   CompositeActor} and it is opaque.
     */
    private static boolean _isOpaque(CompositeEntity entity) {
        return entity instanceof CompositeActor
                && ((CompositeActor) entity).isOpaque();
    }

    private boolean _matchAtomicActor(AtomicActor lhsActor,
            AtomicActor hostActor) {
        int matchSize = _match.size();
        boolean success = true;

        _match.put(lhsActor, hostActor);
        
        if (lhsActor instanceof AtomicActorMatcher) {
            AtomicActorMatcher matcher = (AtomicActorMatcher) lhsActor;
            try {
                for (Rule rule : matcher.ruleList.getRuleList()) {
                    if (rule instanceof SubclassRule) {
                        try {
                            Class<?> superclass =
                                Class.forName(((SubclassRule) rule)
                                        .getSuperclass());
                            if (!superclass.isInstance(hostActor)) {
                                success = false;
                                break;
                            }
                        } catch (ClassNotFoundException e) {
                            success = false;
                            break;
                        }
                    }
                }
            } catch (MalformedStringException e) {
                success = false;
            }
        } else {
            success = lhsActor.getClass().isInstance(hostActor);
        }

        if (success) {
            ObjectList lhsList = new ObjectList();
            lhsList.addAll((Collection<?>) lhsActor.portList());
    
            ObjectList hostList = new ObjectList();
            hostList.addAll((Collection<?>) hostActor.portList());
    
            success = _matchObject(lhsList, hostList);
        }

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private boolean _matchCompositeEntity(CompositeEntity lhsEntity,
            CompositeEntity hostEntity) {
        int matchSize = _match.size();
        boolean success = true;

        _match.put(lhsEntity, hostEntity);

        if (lhsEntity instanceof CompositeActor) {
            CompositeActor lhsComposite = (CompositeActor) lhsEntity;
            Director lhsDirector = lhsComposite.isOpaque() ?
                    lhsComposite.getDirector() : null;
            if (hostEntity instanceof CompositeActor) {
                CompositeActor hostComposite = (CompositeActor) hostEntity;
                Director hostDirector = hostComposite.isOpaque() ?
                        hostComposite.getDirector() : null;
                success = _shallowMatchDirector(lhsDirector, hostDirector);
            } else {
                success = false;
            }
        }

        if (success) {
            IndexedLists lhsMarkedList = new IndexedLists();
            ComponentEntity lhsNextActor =
                _findFirstChild(lhsEntity, lhsMarkedList, _match.keySet());
            ObjectList lhsList = new ObjectList();
            while (lhsNextActor != null) {
                lhsList.add(lhsNextActor);
                lhsNextActor = _findNextChild(lhsEntity, lhsMarkedList,
                        _match.keySet());
            }

            IndexedLists hostMarkedList = new IndexedLists();
            ComponentEntity hostNextActor = _findFirstChild(hostEntity,
                    hostMarkedList, _match.values());
            ObjectList hostList = new ObjectList();
            while (hostNextActor != null) {
                hostList.add(hostNextActor);
                hostNextActor = _findNextChild(hostEntity, hostMarkedList,
                        _match.values());
            }

            success = _matchObject(lhsList, hostList);
        }

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private boolean _matchList(LookbackEntry matchedObjectLists) {
        ObjectList lhsList = matchedObjectLists.getLHSList();
        ObjectList hostList = matchedObjectLists.getHostList();

        int matchSize = _match.size();
        boolean success = true;
        boolean lhsChildChecked = false;

        boolean firstEntrance = !_match.containsKey(lhsList);
        FastLinkedList<LookbackEntry>.Entry lookbackTail = null;
        if (firstEntrance) {
            _match.put(lhsList, hostList);
            _lookbackList.add(matchedObjectLists);
            lookbackTail = _lookbackList.getTail();
        }

        ObjectList.Entry lhsEntry = lhsList.getHead();
        if (lhsEntry != null) {
            lhsEntry.remove();
            Object lhsObject = lhsEntry.getValue();
            lhsChildChecked = true;
            success = false;
            ObjectList.Entry hostEntryPrevious = null;
            ObjectList.Entry hostEntry = hostList.getHead();
            while (hostEntry != null) {
                hostEntry.remove();
                Object hostObject = hostEntry.getValue();
                if (_matchObject(lhsObject, hostObject)) {
                    success = true;
                    break;
                }
                hostList.addEntryAfter(hostEntry, hostEntryPrevious);
                hostEntryPrevious = hostEntry;
                hostEntry = hostEntry.getNext();
            }
            lhsList.addEntryToHead(lhsEntry);
        }

        if (success) {
            if (!lhsChildChecked) {
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

    private boolean _matchObject(Object lhsObject, Object hostObject) {
        Object match = _match.get(lhsObject);
        if (match == hostObject) {
            return _checkBackward();
        } else if (match != null || _match.containsValue(hostObject)) {
            return false;
        }

        if (lhsObject instanceof AtomicActor
                && hostObject instanceof AtomicActor) {
            return _matchAtomicActor((AtomicActor) lhsObject,
                    (AtomicActor) hostObject);
        } else if (lhsObject instanceof CompositeEntity
                && hostObject instanceof CompositeEntity) {
            return _matchCompositeEntity((CompositeEntity) lhsObject,
                    (CompositeEntity) hostObject);
        } else if (lhsObject instanceof Port && hostObject instanceof Port) {
            return _matchPort((Port) lhsObject, (Port) hostObject);
        } else if (lhsObject instanceof Path && hostObject instanceof Path) {
            return _matchPath((Path) lhsObject, (Path) hostObject);
        } else if (lhsObject instanceof ObjectList
                && hostObject instanceof ObjectList) {
            LookbackEntry matchedObjectLists = new LookbackEntry(
                    (ObjectList) lhsObject, (ObjectList) hostObject);
            return _matchList(matchedObjectLists);
        } else {
            return false;
        }
    }

    private boolean _matchPath(Path lhsPath, Path hostPath) {

        if (!_shallowMatchPath(lhsPath, hostPath)) {
            return false;
        }

        int matchSize = _match.size();
        boolean success = true;

        _match.put(lhsPath, hostPath);

        Port lhsPort = lhsPath.getEndPort();
        Port hostPort = hostPath.getEndPort();

        success = _matchObject(lhsPort, hostPort);

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private boolean _matchPort(Port lhsPort, Port hostPort) {

        int matchSize = _match.size();
        boolean success = true;

        _match.put(lhsPort, hostPort);

        if (!_shallowMatchPort(lhsPort, hostPort)) {
            success = false;
        }

        if (success) {
            ObjectList lhsList = new ObjectList();
            lhsList.add(lhsPort.getContainer());

            Path lhsPath = new Path(lhsPort);
            Set<Relation> visitedRelations = new HashSet<Relation>();
            Set<Port> visitedPorts = new HashSet<Port>();
            boolean foundPath = _findFirstPath(lhsPort, lhsPath,
                    visitedRelations, visitedPorts);
            while (foundPath) {
                lhsList.add(lhsPath.clone());
                foundPath =
                    _findNextPath(lhsPath, visitedRelations, visitedPorts);
            }

            ObjectList hostList = new ObjectList();
            hostList.add(hostPort.getContainer());

            Path hostPath = new Path(hostPort);
            visitedRelations = new HashSet<Relation>();
            visitedPorts = new HashSet<Port>();
            foundPath = _findFirstPath(hostPort, hostPath, visitedRelations,
                    visitedPorts);
            while (foundPath) {
                hostList.add(hostPath.clone());
                foundPath =
                    _findNextPath(hostPath, visitedRelations, visitedPorts);
            }

            success = _matchObject(lhsList, hostList);
        }

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private static void _printMatch(MatchResult match) {
        List<Object> keyList = new LinkedList<Object>(match.keySet());
        Collections.sort(keyList, _comparator);
        for (Object lhsObject : keyList) {
            if (lhsObject instanceof NamedObj) {
                System.out.println(_getNameString(lhsObject) + " : " +
                        _getNameString(match.get(lhsObject)));
            }
        }
    }

    private boolean _shallowMatchDirector(Director lhsDirector,
            Director hostDirector) {

        if (lhsDirector == null && hostDirector == null) {
            return true;
        } else if (lhsDirector == null || hostDirector == null) {
            return false;
        }

        int matchSize = _match.size();

        _match.put(lhsDirector, hostDirector);

        boolean success =
            lhsDirector.getClass().equals(hostDirector.getClass());

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private static boolean _shallowMatchPath(Path lhsPath, Path hostPath) {
        Port lhsStartPort = lhsPath.getStartPort();
        Port hostStartPort = hostPath.getStartPort();
        Port lhsEndPort = lhsPath.getEndPort();
        Port hostEndPort = hostPath.getEndPort();

        // TODO: Check the relations and ports in between.

        return _shallowMatchPort(lhsStartPort, hostStartPort)
                && _shallowMatchPort(lhsEndPort, hostEndPort);
    }

    private static boolean _shallowMatchPort(Port lhsPort, Port hostPort) {
        if (lhsPort instanceof TypedIOPort) {
            if (hostPort instanceof TypedIOPort) {
                TypedIOPort lhsTypedPort = (TypedIOPort) lhsPort;
                TypedIOPort hostTypedPort = (TypedIOPort) hostPort;
                if (lhsTypedPort.isInput() && !hostTypedPort.isInput()) {
                    return false;
                } else if (lhsTypedPort.isOutput()
                        && !hostTypedPort.isOutput()) {
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } else {
            return true;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    private MatchCallback _callback = DEFAULT_CALLBACK;

    private static final NameComparator _comparator = new NameComparator();

    private LookbackList _lookbackList;

    /** The map that matches objects in the LHS to the objects in the host.
     *  These objects include actors, ports, relations, etc.
     */
    private MatchResult _match;

    /** The variable that indicates whether the last match operation is
     *  successful. (See {@link #match(CompositeActorMatcher, NamedObj)})
     */
    private boolean _success = false;

    ///////////////////////////////////////////////////////////////////
    ////                      private inner classes                ////

    private static class IndexedList extends Pair<List<?>, Integer> {

        public boolean equals(Object object) {
            if (object instanceof IndexedList) {
                IndexedList list = (IndexedList) object;
                return getFirst().get(getSecond()) ==
                    list.getFirst().get(list.getSecond());
            } else {
                return false;
            }
        }

        public int hashCode() {
            return getFirst().get(getSecond()).hashCode();
        }

        IndexedList(List<?> list, Integer mark) {
            super(list, mark);
        }

        private static final long serialVersionUID = -8862333308144377821L;

    }

    private static class IndexedLists extends FastLinkedList<IndexedList> {
    }

    private static class LookbackEntry
    extends Pair<Pair<ObjectList, ObjectList>, Boolean> {

        public ObjectList getHostList() {
            return getFirst().getSecond();
        }

        public ObjectList getLHSList() {
            return getFirst().getFirst();
        }

        public boolean isFinished() {
            return getSecond();
        }

        public void setFinished(boolean finished) {
            setSecond(finished);
        }

        LookbackEntry(ObjectList lhsList, ObjectList hostList) {
            super(new Pair<ObjectList, ObjectList>(lhsList, hostList), false);
        }

        private static final long serialVersionUID = -2952044613606267420L;
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

    private static class Path extends IndexedLists implements Cloneable {

        public Object clone() {
            Path path = new Path(_startPort);
            Entry entry = getHead();
            while (entry != null) {
                path.add((IndexedList) entry.getValue().clone());
                entry = entry.getNext();
            }
            return path;
        }

        public boolean equals(Object object) {
            return super.equals(object)
                    && _startPort == ((Path) object)._startPort;
        }

        public Port getEndPort() {
            IndexedList list = getTail().getValue();
            return (Port) ((List<?>) list.getFirst()).get(list.getSecond());
        }

        public Port getStartPort() {
            return _startPort;
        }

        public int hashCode() {
            return Arrays.hashCode(new int[] {_startPort.hashCode(),
                    super.hashCode()});
        }

        public String toString() {
            StringBuffer buffer = new StringBuffer();
            buffer.append(_startPort.getFullName());
            buffer.append(":[");
            Entry entry = getHead();
            int i = 0;
            while (entry != null) {
                IndexedList markedList = entry.getValue();
                List<?> list = (List<?>) markedList.getFirst();
                NamedObj object = (NamedObj) list.get(markedList.getSecond());
                if (i++ > 0) {
                    buffer.append(", ");
                }
                buffer.append(object.getFullName());
                entry = entry.getNext();
            }
            buffer.append("]");
            return buffer.toString();
        }

        Path(Port startPort) {
            _startPort = startPort;
        }

        private Port _startPort;
    }
}
