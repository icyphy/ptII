/* A recursive algorithm to match a subgraph to the given pattern.

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
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.NamedObj;
import ptolemy.moml.MoMLParser;

/** A recursive algorithm to match a subgraph to the given pattern.

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public class RecursiveGraphMatcher {

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /** Get the latest matching result. This result is not made unmodifiable,
     *  but the user is not supposed to modify it. During the matching process,
     *  if a callback routine (an object of {@link MatchCallback}) is invoked,
     *  it can call this method to retrieve the new match result. However, the
     *  returned object may be changed by future matching. To maintain a copy of
     *  this result, {@link MatchResult#clone()} may be called that returns a
     *  clone of it.
     *
     *  @return The latest matching result.
     */
    public MatchResult getMatchResult() {
        return _match;
    };

    /** Return whether the last matching was successful. The success of a match
     *  does not only mean that at least one match was found, but it also means
     *  that the callback (an object of {@link MatchCallback}) returned
     *  <tt>true</tt> when it was invoked with the last match result.
     *
     *  @return Whether the last matching was successful.
     */
    public boolean isSuccessful() {
        return _success;
    }

    /** Match the given model file with a rule file. This main method takes a
     *  parameter array of length 2 or 3. If the array has 2 elements, the first
     *  string is the rule file name, and the second is the model file name. An
     *  arbitrary match is printed to the console. If it has 3 elements, the
     *  first string should be "<tt>-A</tt>", the second string is the rule file
     *  name, and the third is the model file name. All the matches are printed
     *  to to console in that case.
     *
     *  @param args The parameter array.
     *  @exception Exception If the rule file or the model file cannot be read.
     */
    public static void main(String[] args) throws Exception {
        if (!(args.length == 2 ||
                (args.length == 3 && args[0].equalsIgnoreCase("-A")))) {
            System.err.println("USAGE: java [-A] "
                    + RecursiveGraphMatcher.class.getName()
                    + " <lhs.xml> <host.xml>");
            System.exit(1);
        }

        final boolean all = args.length == 3 && args[0].equalsIgnoreCase("-A");
        String lhsXMLFile = all ? args[1] : args[0];
        String hostXMLFile = all ? args[2] : args[1];

        MatchCallback matchCallback = new MatchCallback() {
            public boolean foundMatch(RecursiveGraphMatcher matcher) {
                MatchResult match = matcher.getMatchResult();
                System.out.println("--- Match " + ++count + " ---");
                _printMatch(match);
                return !all;
            }

            private int count = 0;
        };
        match(lhsXMLFile, hostXMLFile, matchCallback);
    }

    /** Match the LHS graph with the host graph. If the match is successful,
     *  <tt>true</tt> is returned, and the match result is stored internally,
     *  which can be retrieved with {@link #getMatchResult()}.
     *
     *  @param lhsGraph The LHS graph.
     *  @param hostGraph The host graph.
     *  @return <tt>true</tt> if the match is successful; <tt>false</tt>
     *   otherwise.
     */
    public boolean match(CompositeActorMatcher lhsGraph, NamedObj hostGraph) {

        // Matching result.
        _match = new MatchResult();

        // Temporary data structures.
        _lookbackList = new FastLinkedList<LookbackEntry>();
        _completedObjects = new HashSet<Object>();

        _success = _matchObject(lhsGraph, hostGraph);

        assert _lookbackList.isEmpty();
        if (!_success) {
            assert _match.isEmpty();
        }

        // Clear temporary data structures to free memory.
        _lookbackList = null;
        _completedObjects = null;

        return _success;
    }

    /** Match the host model stored in the file with name <tt>hostXMLFile</tt>
     *  with the rule stored in the file with name <tt>lhsXMLFile</tt>, and
     *  record the first matching (which is arbitrarily decided by the recursive
     *  algorithm) in the returned matcher object. The match result can be
     *  obtained with {@link #getMatchResult()}.
     *
     *  @param lhsXMLFile The name of the file in which the rule is stored.
     *  @param hostXMLFile The name of the file in which the model to be matched
     *   is stored.
     *  @return A matcher object with the first match result stored in it. If no
     *   match is found, {@link #isSuccessful()} of the matcher object returns
     *   <tt>false</tt>, and {@link #getMatchResult()} returns an empty match.
     *  @exception Exception If the rule file or the model file cannot be read.
     */
    public static RecursiveGraphMatcher match(String lhsXMLFile,
            String hostXMLFile) throws Exception {
        return match(lhsXMLFile, hostXMLFile, null);
    }

    /** Match the host model stored in the file with name <tt>hostXMLFile</tt>
     *  with the rule stored in the file with name <tt>lhsXMLFile</tt>, and
     *  invoke <tt>callback</tt>'s {@link
     *  MatchCallback#foundMatch(RecursiveGraphMatcher)} method whenever a match
     *  is found. If the callback returns <tt>true</tt>, the match will
     *  terminate and no more matches will be reported; otherwise, the match
     *  process continues, and if one more match is found, the callback will be
     *  invoked again.
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
     */
    public static RecursiveGraphMatcher match(String lhsXMLFile,
            String hostXMLFile, MatchCallback callback) throws Exception {
        MoMLParser parser = new MoMLParser();
        SingleRuleTransformer rule = (SingleRuleTransformer)
                parser.parse(null, new File(lhsXMLFile).toURI().toURL());
        parser.reset();
        NamedObj host =
            parser.parse(null, new File(hostXMLFile).toURI().toURL());

        RecursiveGraphMatcher matcher = new RecursiveGraphMatcher();
        if (callback != null) {
            matcher.setMatchCallback(callback);
        }
        matcher.match(rule.getLeftHandSide(), host);
        return matcher;
    }

    /** Set the callback to be invoked by future calls to {@link
     *  #match(CompositeActorMatcher, NamedObj)}.
     *
     *  @param callback The callback.
     */
    public void setMatchCallback(MatchCallback callback) {
        if (callback == null) {
            _callback = DEFAULT_CALLBACK;
        } else {
            _callback = callback;
        }
    }

    ///////////////////////////////////////////////////////////////////
    ////                         private methods                   ////

    private boolean _checkBackward() {
        FastLinkedList<LookbackEntry>.Entry lhsEntry = _lookbackList.getTail();
        LookbackEntry entry = null;
        Object data = null;
        while (lhsEntry != null) {
            entry = lhsEntry.getValue();
            data = entry.getData();
            if (!_completedObjects.contains(data)) {
                break;
            }
            lhsEntry = lhsEntry.getPrevious();
        }
        if (lhsEntry == null) {
            return _callback.foundMatch(this);
        } else {
            LookbackEntryType type = entry.getType();
            boolean success;

            switch (type) {
            case ATOMIC_ACTOR:
                success = _matchAtomicActor((AtomicActor) data,
                        (AtomicActor) _match.get(data));
                break;
            case COMPOSITE_ENTITY:
                success = _matchCompositeEntity((CompositeEntity) data,
                        (CompositeEntity) _match.get(data));
                break;
            case PATH_LIST:
                success = _matchPathList((PathList) data,
                        (PathList) _match.get(data));
                break;
            case PORT:
                success = _matchPort((Port) data, (Port) _match.get(data));
                break;
            default:
                success = true;
            }
            return success;
        }
    }

    private static ComponentEntity _findFirstChild(CompositeEntity top,
            FastLinkedList<MarkedEntityList> markedList,
            Collection<Object> excludedEntities) {

        List<?> entities = top.entityList(ComponentEntity.class);

        if (!entities.isEmpty()) {
            int i = 0;
            MarkedEntityList currentList = new MarkedEntityList(entities, 0);
            markedList.add(currentList);

            FastLinkedList<MarkedEntityList>.Entry tail = markedList.getTail();

            for (Object entityObject : entities) {
                currentList.setSecond(i);
                if (entityObject instanceof AtomicActor
                        || entityObject instanceof CompositeEntity
                        && _isNewLevel((CompositeEntity) entityObject)) {
                    if (!excludedEntities.contains(entityObject)) {
                        return (ComponentEntity) entityObject;
                    }
                } else {
                    CompositeEntity compositeEntity =
                        (CompositeEntity) entityObject;
                    ComponentEntity child = _findFirstChild(compositeEntity,
                            markedList, excludedEntities);
                    if (child != null && !excludedEntities.contains(child)) {
                        return child;
                    } else {
                        markedList.removeAllAfter(tail);
                    }
                }
                i++;
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private static boolean _findFirstPath(Port startPort, Path path,
            Set<? super Relation> visitedRelations,
            Set<? super Port> visitedPorts) {
        List<?> relationList = startPort.linkedRelationList();
        if (startPort instanceof ComponentPort) {
            ((List) relationList).addAll(((TypedIOPort) startPort).insideRelationList());
        }

        int i = 0;
        FastLinkedList<MarkedEntityList>.Entry tail = path.getTail();
        for (Object relationObject : relationList) {
            Relation relation = (Relation) relationObject;
            if (visitedRelations.contains(relation)) {
                i++;
                continue;
            }

            path.add(new MarkedEntityList(relationList, i));
            visitedRelations.add(relation);

            List<?> portList = relation.linkedPortList();

            int j = 0;
            FastLinkedList<MarkedEntityList>.Entry tail2 = path.getTail();
            for (Object portObject : portList) {
                Port port = (Port) portObject;
                if (visitedPorts.contains(port)) {
                    j++;
                    continue;
                }

                path.add(new MarkedEntityList(portList, j));
                visitedPorts.add(port);
                NamedObj container = port.getContainer();
                boolean reachEnd = true;
                if (container instanceof CompositeEntity) {
                    if (!_isNewLevel((CompositeEntity) container)) {
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
                    path.removeAllAfter(tail2);
                    visitedPorts.remove(port);
                    j++;
                }
            }

            path.removeAllAfter(tail);
            visitedRelations.remove(relation);
            i++;
        }

        return false;
    }

    private static ComponentEntity _findNextChild(CompositeEntity top,
            FastLinkedList<MarkedEntityList> markedList,
            Collection<Object> excludedEntities) {
        if (markedList.isEmpty()) {
            return _findFirstChild(top, markedList, excludedEntities);
        } else {
            FastLinkedList<MarkedEntityList>.Entry entry = markedList.getTail();
            while (entry != null) {
                MarkedEntityList markedEntityList = entry.getValue();
                List<?> entityList = markedEntityList.getFirst();
                for (int index = markedEntityList.getSecond() + 1;
                       index < entityList.size(); index++) {
                    markedEntityList.setSecond(index);
                    ComponentEntity entity =
                        (ComponentEntity) entityList.get(index);
                    if (!excludedEntities.contains(entity)) {
                        markedList.removeAllAfter(entry);
                        if (entity instanceof AtomicActor
                                || entity instanceof CompositeEntity
                                && _isNewLevel((CompositeEntity) entity)) {
                            return entity;
                        } else {
                            CompositeEntity compositeEntity =
                                (CompositeEntity) entity;
                            ComponentEntity child = _findFirstChild(
                                    compositeEntity, markedList,
                                    excludedEntities);
                            if (child != null) {
                                return child;
                            }
                        }
                    }
                }
                entry = entry.getPrevious();
            }
            markedList.clear();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static boolean _findNextPath(Path path, Set<Relation> visitedRelations,
            Set<Port> visitedPorts) {
        FastLinkedList<MarkedEntityList>.Entry entry = path.getTail();
        while (entry != null) {
            MarkedEntityList markedEntityList = entry.getValue();
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
                            || _isNewLevel((CompositeEntity) container)) {
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

                    FastLinkedList<MarkedEntityList>.Entry tail =
                        path.getTail();
                    int i = 0;
                    for (Object portObject : portList) {
                        Port port = (Port) portObject;
                        if (visitedPorts.contains(port)) {
                            i++;
                            continue;
                        }

                        path.add(new MarkedEntityList(portList, i));
                        visitedPorts.add(port);
                        NamedObj container = port.getContainer();
                        if (!(container instanceof CompositeEntity)
                                || _isNewLevel((CompositeEntity) container)) {
                            return true;
                        }

                        if (_findFirstPath(port, path, visitedRelations,
                                visitedPorts)) {
                            return true;
                        } else {
                            path.removeAllAfter(tail);
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

    private static String _getNameString(Object object) {
        return object instanceof NamedObj ? ((NamedObj) object).getFullName()
                : object.toString();
    }

    /** Test whether the composite entity starts a new level of composition.
     *  Return <tt>true</tt> if the composite entity is the top-level composite
     *  entity of the match operation, or the composite entity has a director
     *  defined in it.
     *
     *  @param container The composite entity to be tested.
     *  @return <tt>true</tt> if the composite entity starts a new level;
     *   <tt>false</tt> otherwise.
     */
    private static boolean _isNewLevel(CompositeEntity container) {
        return container instanceof CompositeActor
                && ((CompositeActor) container).isOpaque();
    }

    private boolean _matchAtomicActor(AtomicActor lhsActor,
            AtomicActor hostActor) {
        int matchSize = _match.size();
        boolean success = true;
        boolean lhsChildChecked = false;

        boolean firstEntrance = !_match.containsKey(lhsActor);
        FastLinkedList<LookbackEntry>.Entry lookbackTail = null;
        if (firstEntrance) {
            _match.put(lhsActor, hostActor);
            _lookbackList.add(new LookbackEntry(
                    LookbackEntryType.ATOMIC_ACTOR, lhsActor));
            lookbackTail = _lookbackList.getTail();
        }

        for (Object lhsPortObject : lhsActor.portList()) {
            if (!_match.containsKey(lhsPortObject)) {
                lhsChildChecked = true;
                success = false;
                for (Object hostPortObject : hostActor.portList()) {
                    if (!_match.containsValue(hostPortObject)) {
                        if (_matchObject(lhsPortObject, hostPortObject)) {
                            success = true;
                            break;
                        }
                    }
                }
                break;
            }
        }

        if (success) {
            if (!lhsChildChecked) {
                _completedObjects.add(lhsActor);
                success = _checkBackward();
                _completedObjects.remove(lhsActor);
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

    private boolean _matchCompositeEntity(CompositeEntity lhsEntity,
            CompositeEntity hostEntity) {
        int matchSize = _match.size();
        boolean success = true;
        boolean lhsChildChecked = false;

        boolean firstEntrance = !_match.containsKey(lhsEntity);
        FastLinkedList<LookbackEntry>.Entry lookbackTail = null;
        if (firstEntrance) {
            _match.put(lhsEntity, hostEntity);
            _lookbackList.add(new LookbackEntry(
                    LookbackEntryType.COMPOSITE_ENTITY, lhsEntity));
            lookbackTail = _lookbackList.getTail();

            if (lhsEntity instanceof CompositeActor) {
                CompositeActor lhsComposite = (CompositeActor) lhsEntity;
                Director lhsDirector = lhsComposite.isOpaque() ?
                        lhsComposite.getDirector() : null;
                if (hostEntity instanceof CompositeActor) {
                    CompositeActor hostComposite = (CompositeActor) hostEntity;
                    Director hostDirector = hostComposite.isOpaque() ?
                            hostComposite.getDirector() : null;
                    success = _matchDirector(lhsDirector, hostDirector);
                } else {
                    success = false;
                }
            }
        }

        if (success) {
            FastLinkedList<MarkedEntityList> lhsMarkedList =
                new FastLinkedList<MarkedEntityList>();

            ComponentEntity lhsNextActor =
                _findFirstChild(lhsEntity, lhsMarkedList, _match.keySet());

            if (lhsNextActor != null) {
                lhsChildChecked = true;
                FastLinkedList<MarkedEntityList> hostMarkedList =
                    new FastLinkedList<MarkedEntityList>();
                ComponentEntity hostNextActor = _findFirstChild(hostEntity,
                        hostMarkedList, _match.values());

                success = false;
                while (!success && hostNextActor != null) {
                    if (_matchObject(lhsNextActor, hostNextActor)) {
                        success = true;
                    } else {
                        hostNextActor = _findNextChild(hostEntity,
                                hostMarkedList, _match.values());
                    }
                }
            }
        }

        if (success) {
            if (!lhsChildChecked) {
                _completedObjects.add(lhsEntity);
                success = _checkBackward();
                _completedObjects.remove(lhsEntity);
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

    private boolean _matchDirector(Director lhsDirector,
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
        } else if (lhsObject instanceof PathList
                && hostObject instanceof PathList) {
            return _matchPathList((PathList) lhsObject, (PathList) hostObject);
        } else {
            return false;
        }
    }

    private boolean _matchPath(Path lhsPath, Path hostPath) {

        if (!_shallowMatchPath(lhsPath, hostPath)) {
            return false;
        }

        int matchSize = _match.size();

        _match.put(lhsPath, hostPath);

        Port lhsPort = lhsPath.getEndPort();
        Port hostPort = hostPath.getEndPort();

        boolean success = _matchObject(lhsPort, hostPort);

        if (!success) {
            _match.retain(matchSize);
        }

        return success;
    }

    private boolean _matchPathList(PathList lhsPathList,
            PathList hostPathList) {
        int matchSize = _match.size();
        boolean success = true;
        boolean lhsChildChecked = false;

        boolean firstEntrance = !_match.containsKey(lhsPathList);
        FastLinkedList<LookbackEntry>.Entry lookbackTail = null;
        if (firstEntrance) {
            _match.put(lhsPathList, hostPathList);
            _lookbackList.add(new LookbackEntry(
                    LookbackEntryType.PATH_LIST, lhsPathList));
            lookbackTail = _lookbackList.getTail();
        }

        for (Object lhsPathObject : lhsPathList) {
            if (!_match.containsKey(lhsPathObject)) {
                lhsChildChecked = true;
                success = false;
                for (Object hostPathObject : hostPathList) {
                    if (!_match.containsValue(hostPathObject)) {
                        if (_matchObject(lhsPathObject, hostPathObject)) {
                            success = true;
                            break;
                        }
                    }
                }
                break;
            }
        }

        if (success) {
            if (!lhsChildChecked) {
                _completedObjects.add(lhsPathList);
                success = _checkBackward();
                _completedObjects.remove(lhsPathList);
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

    private boolean _matchPort(Port lhsPort, Port hostPort) {

        int matchSize = _match.size();
        boolean success = true;
        boolean lhsChildChecked = false;

        boolean firstEntrance = !_match.containsKey(lhsPort);
        FastLinkedList<LookbackEntry>.Entry lookbackTail = null;
        if (firstEntrance) {
            _match.put(lhsPort, hostPort);
            _lookbackList.add(new LookbackEntry(LookbackEntryType.PORT,
                    lhsPort));
            lookbackTail = _lookbackList.getTail();

            if (!_shallowMatchPort(lhsPort, hostPort)) {
                success = false;
            }
        }

        if (success && !lhsChildChecked) {
            if (firstEntrance) {
                lhsChildChecked = true;
                success = success && _matchObject(lhsPort.getContainer(),
                        hostPort.getContainer());
            }
        }

        if (success) {
            if (!lhsChildChecked) {
                _completedObjects.add(lhsPort);
                PathList lhsPaths = new PathList();
                Path lhsPath = new Path(lhsPort);
                Set<Relation> visitedRelations = new HashSet<Relation>();
                Set<Port> visitedPorts = new HashSet<Port>();
                boolean foundPath = _findFirstPath(lhsPort, lhsPath,
                        visitedRelations, visitedPorts);
                while (foundPath) {
                    lhsPaths.add((Path) lhsPath.clone());
                    foundPath =
                        _findNextPath(lhsPath, visitedRelations, visitedPorts);
                }

                PathList hostPaths = new PathList();
                Path hostPath = new Path(hostPort);
                visitedRelations = new HashSet<Relation>();
                visitedPorts = new HashSet<Port>();
                foundPath = _findFirstPath(hostPort, hostPath, visitedRelations,
                        visitedPorts);
                while (foundPath) {
                    hostPaths.add((Path) hostPath.clone());
                    foundPath =
                        _findNextPath(hostPath, visitedRelations, visitedPorts);
                }

                lhsChildChecked = true;
                success = success && _matchObject(lhsPaths, hostPaths);
                _completedObjects.remove(lhsPort);
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

    private MatchCallback _callback = DEFAULT_CALLBACK;

    private static final ObjectComparator _comparator = new ObjectComparator();

    ///////////////////////////////////////////////////////////////////
    ////                         private fields                    ////

    private Set<Object> _completedObjects;

    private FastLinkedList<LookbackEntry> _lookbackList;

    /** The map that matches objects in the LHS to the objects in the host.
     *  These objects include actors, ports, relations, etc.
     */
    private MatchResult _match;

    /** The variable that indicates whether the last match operation is
     *  successful. (See {@link #match(CompositeActorMatcher, NamedObj)})
     */
    private boolean _success = false;

    private static final MatchCallback DEFAULT_CALLBACK = new MatchCallback() {
        public boolean foundMatch(RecursiveGraphMatcher matcher) {
            return true;
        }
    };

    ///////////////////////////////////////////////////////////////////
    ////                      private inner classes                ////

    private static class LookbackEntry {

        public Object getData() {
            return _data;
        }

        public LookbackEntryType getType() {
            return _type;
        }

        LookbackEntry(LookbackEntryType type, Object data) {
            _type = type;
            _data = data;
        }

        private Object _data;

        private LookbackEntryType _type;
    }

    private static class MarkedEntityList extends Pair<List<?>, Integer> {

        public boolean equals(Object object) {
            if (object instanceof MarkedEntityList) {
                MarkedEntityList list = (MarkedEntityList) object;
                return getFirst().get(getSecond()) ==
                    list.getFirst().get(list.getSecond());
            } else {
                return false;
            }
        }

        public int hashCode() {
            return getFirst().get(getSecond()).hashCode();
        }

        MarkedEntityList(List<?> list, Integer mark) {
            super(list, mark);
        }

        private static final long serialVersionUID = -8862333308144377821L;

    }

    private static class ObjectComparator implements Comparator<Object> {

        public int compare(Object object1, Object object2) {
            return _getNameString(object1).compareTo(_getNameString(object2));
        }
    }

    private static class Path extends FastLinkedList<MarkedEntityList>
    implements Cloneable {

        public Object clone() {
            Path path = new Path(_startPort);
            Entry entry = getHead();
            while (entry != null) {
                path.add((MarkedEntityList) entry.getValue().clone());
                entry = entry.getNext();
            }
            return path;
        }

        public boolean equals(Object object) {
            return super.equals(object)
                    && _startPort == ((Path) object)._startPort;
        }

        public Port getEndPort() {
            MarkedEntityList list = getTail().getValue();
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
                MarkedEntityList markedList = entry.getValue();
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

    private static class PathList extends LinkedList<Path> {

        public boolean equals(Object object) {
            return this == object;
        }

        private static final long serialVersionUID = 1609066286409133731L;

    }

    private static enum LookbackEntryType {
        ATOMIC_ACTOR, COMPOSITE_ENTITY, PATH_LIST, PORT
    }
}
