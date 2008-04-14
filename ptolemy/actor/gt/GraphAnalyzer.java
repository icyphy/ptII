/*

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

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import ptolemy.actor.AtomicActor;
import ptolemy.actor.gt.data.FastLinkedList;
import ptolemy.actor.gt.data.Pair;
import ptolemy.data.BooleanToken;
import ptolemy.data.Token;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.ComponentPort;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.Relation;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NamedObj;

/**

 @author Thomas Huining Feng
 @version $Id$
 @since Ptolemy II 6.1
 @Pt.ProposedRating Red (tfeng)
 @Pt.AcceptedRating Red (tfeng)
 */
public abstract class GraphAnalyzer {

    /** Find the first child within the top composite entity. The child is
     *  either an atomic actor ({@link AtomicActor}) or an opaque composite
     *  entity, one that has a director in it. If the top composite entity does
     *  not have any child, <tt>null</tt> is returned.
     *
     *  @param top The top composite entity in which the search is performed.
     *  @param indexedLists A list that is used to encode the composite entities
     *   visited.
     *  @param excludedObjects The atomic actor or opaque composite entities
     *   that should not be returned.
     *  @return The child found, or <tt>null</tt> if none.
     *  @see #findNextChild(CompositeEntity, IndexedLists, Collection)
     */
    public NamedObj findFirstChild(CompositeEntity top,
            IndexedLists indexedLists, Collection<Object> excludedObjects) {

        List<Object> children = new LinkedList<Object>((Collection<?>) top
                .entityList(ComponentEntity.class));

        Token collapsingToken = _getAttribute(top, "RelationCollapsing",
                RelationCollapsingAttribute.class);
        boolean collapsing = collapsingToken == null ? false
                : ((BooleanToken) collapsingToken).booleanValue();
        if (!collapsing) {
            for (Object relationObject : top.relationList()) {
                children.add(relationObject);
            }
        }

        if (!children.isEmpty()) {
            int i = 0;
            IndexedList currentList = new IndexedList(children, 0);
            indexedLists.add(currentList);
            IndexedLists.Entry currentListEntry = indexedLists.getTail();

            for (Object child : children) {
                currentList.setSecond(i);
                if (!(child instanceof CompositeEntity)
                        || _isOpaque((CompositeEntity) child)) {
                    if (!excludedObjects.contains(child)) {
                        return (NamedObj) child;
                    }
                } else {
                    CompositeEntity compositeEntity = (CompositeEntity) child;
                    NamedObj childObject = findFirstChild(compositeEntity,
                            indexedLists, excludedObjects);
                    if (childObject != null
                            && !excludedObjects.contains(childObject)) {
                        return childObject;
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
     *  @see #findNextPath(Path, Set, Set)
     */
    @SuppressWarnings("unchecked")
    public boolean findFirstPath(Port startPort, Path path,
            Set<? super Relation> visitedRelations,
            Set<? super Port> visitedPorts) {
        List<?> relationList = startPort.linkedRelationList();
        if (startPort instanceof ComponentPort) {
            ((Collection<?>) relationList).addAll(((ComponentPort) startPort)
                    .insideRelationList());
        }

        int i = 0;
        IndexedList currentList = new IndexedList(relationList, 0);
        path.add(currentList);
        Path.Entry currentListEntry = path.getTail();

        for (Object relationObject : relationList) {
            Relation relation = (Relation) relationObject;
            if (visitedRelations.contains(relation)
                    || _ignoreRelation(relation)) {
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
                        if (findFirstPath(port, path, visitedRelations,
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
     *  @param excludedObjects The atomic actor or opaque composite entities
     *   that should not be returned.
     *  @return The child found, or <tt>null</tt> if none.
     *  @see #findFirstChild(CompositeEntity, IndexedLists, Collection)
     */
    public NamedObj findNextChild(CompositeEntity top,
            IndexedLists indexedLists, Collection<Object> excludedObjects) {
        if (indexedLists.isEmpty()) {
            return findFirstChild(top, indexedLists, excludedObjects);
        } else {
            IndexedLists.Entry entry = indexedLists.getTail();
            while (entry != null) {
                IndexedList indexedList = entry.getValue();
                List<?> objectList = indexedList.getFirst();
                for (int index = indexedList.getSecond() + 1; index < objectList
                        .size(); index++) {
                    indexedList.setSecond(index);
                    NamedObj child = (NamedObj) objectList.get(index);
                    indexedLists.removeAllAfter(entry);
                    if (!(child instanceof CompositeEntity)
                            || _isOpaque((CompositeEntity) child)) {
                        if (!excludedObjects.contains(child)) {
                            return child;
                        }
                    } else {
                        CompositeEntity compositeEntity = (CompositeEntity) child;
                        NamedObj childObject = findFirstChild(compositeEntity,
                                indexedLists, excludedObjects);
                        if (childObject != null) {
                            return childObject;
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
     *  @see #findFirstPath(Port, Path, Set, Set)
     */
    @SuppressWarnings("unchecked")
    public boolean findNextPath(Path path, Set<Relation> visitedRelations,
            Set<Port> visitedPorts) {
        Path.Entry entry = path.getTail();
        while (entry != null) {
            IndexedList markedEntityList = entry.getValue();
            List<?> entityList = markedEntityList.getFirst();
            for (int index = markedEntityList.getSecond() + 1; index < entityList
                    .size(); index++) {
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

                    if (findFirstPath(port, path, visitedRelations,
                            visitedPorts)) {
                        return true;
                    }

                    visitedPorts.remove(port);
                } else {
                    Relation relation = (Relation) nextObject;
                    if (visitedRelations.contains(relation)
                            || _ignoreRelation(relation)) {
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

                        if (findFirstPath(port, path, visitedRelations,
                                visitedPorts)) {
                            return true;
                        } else {
                            visitedPorts.remove(port);
                        }
                    }

                    visitedRelations.remove(relation);
                }

                if (findNextPath(path, visitedRelations, visitedPorts)) {
                    return true;
                }
            }
            entry = entry.getPrevious();
        }
        return false;
    }

    public static class IndexedList extends Pair<List<?>, Integer> {

        public boolean equals(Object object) {
            if (object instanceof IndexedList) {
                IndexedList list = (IndexedList) object;
                return getFirst().get(getSecond()) == list.getFirst().get(
                        list.getSecond());
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

    }

    public static class IndexedLists extends FastLinkedList<IndexedList> {
    }

    ///////////////////////////////////////////////////////////////////
    ////                      public inner classes                 ////

    public static class Path extends IndexedLists implements Cloneable {

        public Object clone() {
            // FIXME: Note that we do not call super.clone() here.  Is
            // that right?

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
            return Arrays.hashCode(new int[] { _startPort.hashCode(),
                    super.hashCode() });
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

    protected abstract Token _getAttribute(NamedObj container, String name,
            Class<? extends GTAttribute> attributeClass);

    /** Test whether a relation should be ignored in the matching; return true
     *  if the given relation is hidden (i.e., has a parameter "_hide" whose
     *  value is {@link BooleanToken} true).
     *
     *  @param relation The relation.
     *  @return true if the relation should be ignored in the matching; false
     *   otherwise.
     */
    protected boolean _ignoreRelation(Relation relation) {
        Attribute hideAttribute = relation.getAttribute("_hide");
        if (hideAttribute != null) {
            try {
                BooleanToken token = (BooleanToken) ((Parameter) hideAttribute)
                        .getToken();
                boolean hide = token.booleanValue();
                return hide;
            } catch (IllegalActionException e) {
            }
        }
        return false;
    }

    protected abstract boolean _isOpaque(CompositeEntity entity);
}
