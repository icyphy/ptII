/* Scanner that builds an agent-friendly catalog from Ptolemy MoML libraries.

 Copyright (c) 2024-2026 The Regents of the University of California.
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
package org.ptolemy.agent.library;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.ptolemy.agent.library.LibraryIndex.Entry;
import org.ptolemy.agent.library.LibraryIndex.Param;

import ptolemy.actor.Director;
import ptolemy.actor.IOPort;
import ptolemy.actor.TypedAtomicActor;
import ptolemy.actor.injection.ActorModuleInitializer;
import ptolemy.data.expr.Parameter;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.Port;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.EntityLibrary;
import ptolemy.moml.MoMLParser;
import ptolemy.moml.filter.BackwardCompatibility;
import ptolemy.moml.filter.MoMLFilterSimple;

///////////////////////////////////////////////////////////////////
//// LibraryScanner

/**
 Build a component catalog by loading the same MoML library files that
 Vergil uses, forcing lazy {@link EntityLibrary} objects to populate,
 and introspecting the instantiated actors/directors.

 @author Ptolemy II Agent contributors
 @version $Id$
 @since Ptolemy II 11.1
 */
public final class LibraryScanner {

    private static volatile boolean _injectorInitialized = false;

    private LibraryScanner() {
    }

    /** Scan {@code ptolemy/configs/basicLibrary.xml} below the current
     *  PTII root.
     *  @return Catalog entries in library order, de-duplicated by class.
     *  @exception Exception If the library cannot be parsed.
     */
    public static List<Entry> scanDefault() throws Exception {
        File root = _resolvePtIIRoot();
        File library = new File(root, "ptolemy/configs/basicLibrary.xml");
        if (!library.exists()) {
            throw new java.io.FileNotFoundException(
                    library.getAbsolutePath());
        }
        return scan(library);
    }

    /** Scan a concrete MoML library file.
     *  @param libraryFile Library MoML file.
     *  @return Catalog entries in library order, de-duplicated by class.
     *  @exception Exception If the library cannot be parsed.
     */
    public static List<Entry> scan(File libraryFile) throws Exception {
        _initializeOnce();
        Workspace workspace = new Workspace("AgentLibraryScanner");
        MoMLParser.setMoMLFilters(BackwardCompatibility.allFilters(),
                workspace);
        MoMLParser.addMoMLFilter(new _OptionalLibraryFilter(), workspace);
        MoMLParser parser = new MoMLParser(workspace);
        parser.resetAll();

        URL url = libraryFile.toURI().toURL();
        NamedObj root = (NamedObj) parser.parse(null, url);
        Map<String, Entry> entries = new LinkedHashMap<String, Entry>();
        if (root instanceof CompositeEntity) {
            _collect((CompositeEntity) root, new ArrayList<String>(), entries);
        }
        return new ArrayList<Entry>(entries.values());
    }

    private static void _collect(CompositeEntity composite,
            List<String> path, Map<String, Entry> entries) {
        _collectDirectorAttributes(composite, path, entries);

        List<ComponentEntity> children;
        try {
            @SuppressWarnings("unchecked")
            List<ComponentEntity> populated = composite.entityList();
            children = populated;
        } catch (Throwable t) {
            return;
        }
        for (ComponentEntity child : children) {
            String className = child.getClassName();
            if (_isLibraryContainer(child)) {
                List<String> childPath = new ArrayList<String>(path);
                childPath.add(child.getName());
                _collect((CompositeEntity) child, childPath, entries);
                continue;
            }
            if (_isUsableComponent(child)) {
                _put(entries, _entryFor(child, path));
            }
            if (child instanceof CompositeEntity && _isStructuralContainer(
                    className)) {
                List<String> childPath = new ArrayList<String>(path);
                childPath.add(child.getName());
                _collect((CompositeEntity) child, childPath, entries);
            }
        }
    }

    private static void _collectDirectorAttributes(CompositeEntity composite,
            List<String> path, Map<String, Entry> entries) {
        List<Attribute> attrs;
        try {
            @SuppressWarnings("unchecked")
            List<Attribute> populated = composite.attributeList();
            attrs = populated;
        } catch (Throwable t) {
            return;
        }
        for (Attribute attr : attrs) {
            String className = attr.getClassName();
            if (attr instanceof Director || _looksLikeDirector(className)) {
                _put(entries, _entryFor(attr, path));
            }
        }
    }

    private static Entry _entryFor(NamedObj object, List<String> path) {
        String className = object.getClassName();
        String displayName = object.getName();
        String category = _category(path);
        return new Entry(className, displayName, category,
                _description(object, category), _inputs(object),
                _outputs(object), _parameters(object));
    }

    private static List<String> _inputs(NamedObj object) {
        List<String> out = new ArrayList<String>();
        if (object instanceof TypedAtomicActor) {
            // inputPortList returns List<TypedIOPort> which is not assignable
            // to List<IOPort> due to invariant generics, so use a wildcard.
            List<?> ports = ((TypedAtomicActor) object).inputPortList();
            for (Object portObj : ports) {
                if (portObj instanceof IOPort) {
                    _addOnce(out, ((IOPort) portObj).getName());
                }
            }
            return out;
        }
        if (object instanceof ComponentEntity) {
            List<?> ports = ((ComponentEntity) object).portList();
            for (Object portObj : ports) {
                if (portObj instanceof IOPort
                        && ((IOPort) portObj).isInput()) {
                    _addOnce(out, ((Port) portObj).getName());
                }
            }
        }
        return out;
    }

    private static List<String> _outputs(NamedObj object) {
        List<String> out = new ArrayList<String>();
        if (object instanceof TypedAtomicActor) {
            List<?> ports = ((TypedAtomicActor) object).outputPortList();
            for (Object portObj : ports) {
                if (portObj instanceof IOPort) {
                    _addOnce(out, ((IOPort) portObj).getName());
                }
            }
            return out;
        }
        if (object instanceof ComponentEntity) {
            List<?> ports = ((ComponentEntity) object).portList();
            for (Object portObj : ports) {
                if (portObj instanceof IOPort
                        && ((IOPort) portObj).isOutput()) {
                    _addOnce(out, ((Port) portObj).getName());
                }
            }
        }
        return out;
    }

    private static List<Param> _parameters(NamedObj object) {
        List<Param> out = new ArrayList<Param>();
        @SuppressWarnings("unchecked")
        List<Attribute> attrs = object.attributeList();
        for (Attribute attr : attrs) {
            if (attr instanceof Parameter && !_isInternal(attr.getName())) {
                Parameter parameter = (Parameter) attr;
                out.add(new Param(parameter.getName(),
                        parameter.getExpression(), ""));
            }
        }
        return out;
    }

    private static String _description(NamedObj object, String category) {
        String className = object.getClassName();
        String displayName = object.getName();
        if (_looksLikeDirector(className)) {
            return "Director available from the Ptolemy II " + category
                    + " library.";
        }
        return "Ptolemy II actor from the " + category + " library.";
    }

    private static boolean _isLibraryContainer(ComponentEntity entity) {
        return entity instanceof EntityLibrary
                || "ptolemy.moml.EntityLibrary".equals(entity.getClassName());
    }

    private static boolean _isStructuralContainer(String className) {
        return "ptolemy.kernel.CompositeEntity".equals(className);
    }

    private static boolean _isUsableComponent(ComponentEntity entity) {
        String className = entity.getClassName();
        return className != null && className.length() > 0
                && !_isLibraryContainer(entity)
                && !_isStructuralContainer(className)
                && !className.startsWith("ptolemy.actor.gui.");
    }

    private static boolean _looksLikeDirector(String className) {
        return className != null && (className.equals("ptolemy.actor.Director")
                || className.endsWith("Director"));
    }

    private static boolean _isInternal(String name) {
        return name == null || name.startsWith("_");
    }

    private static void _put(Map<String, Entry> entries, Entry entry) {
        if (entry.className != null && !entries.containsKey(entry.className)) {
            entries.put(entry.className, entry);
        }
    }

    private static void _addOnce(List<String> list, String value) {
        if (value != null && !list.contains(value)) {
            list.add(value);
        }
    }

    /** Extract a short, frontend-friendly category from the library path.
     *  Skips generic wrappers ("actor library", "Actors", ...) and
     *  picks the FIRST informative segment, since the basicLibrary
     *  organizes things as
     *  "actor library / Actors / Sources / Generic / Ramp" — we want
     *  "Sources", not "Actors".
     */
    private static String _category(List<String> path) {
        if (path == null || path.isEmpty()) {
            return "Library";
        }
        for (String segment : path) {
            if (segment == null || segment.length() == 0) {
                continue;
            }
            String normalized = segment.trim();
            if (_isWrapperSegment(normalized)) {
                continue;
            }
            return _toTitleCase(normalized);
        }
        // No informative subgroup found — fall back to the last segment.
        return _toTitleCase(path.get(path.size() - 1));
    }

    private static boolean _isWrapperSegment(String name) {
        String n = name == null ? "" : name.toLowerCase(Locale.US);
        return n.equals("actor library") || n.equals("more libraries")
                || n.equals("user library") || n.equals("actors")
                || n.equals("library") || n.equals("auto");
    }

    private static String _toTitleCase(String raw) {
        if (raw == null || raw.length() == 0) {
            return "";
        }
        // Words split on whitespace; preserve any trailing groupings.
        StringBuilder out = new StringBuilder(raw.length());
        boolean atWordStart = true;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (Character.isWhitespace(c) || c == '-' || c == '_') {
                out.append(c == '_' ? ' ' : c);
                atWordStart = true;
                continue;
            }
            if (atWordStart) {
                out.append(Character.toUpperCase(c));
                atWordStart = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static File _resolvePtIIRoot() {
        String[] candidates = new String[] {
                System.getenv("PTII"),
                System.getProperty("ptolemy.ptII.dir"),
                System.getProperty("user.dir"),
                "." };
        for (int i = 0; i < candidates.length; i++) {
            if (candidates[i] == null || candidates[i].length() == 0) {
                continue;
            }
            File root = new File(candidates[i]).getAbsoluteFile();
            File found = _findRoot(root);
            if (found != null) {
                return found;
            }
        }
        return new File(".").getAbsoluteFile();
    }

    private static File _findRoot(File start) {
        File current = start;
        while (current != null) {
            if (new File(current, "ptolemy/configs/basicLibrary.xml")
                    .exists()) {
                return current;
            }
            current = current.getParentFile();
        }
        return null;
    }

    private static synchronized void _initializeOnce() {
        if (!_injectorInitialized) {
            ActorModuleInitializer.initializeInjector();
            _injectorInitialized = true;
        }
    }

    /** Skip optional libraries whose third-party dependencies are not
     *  present in a lean Ptolemy checkout. The agent can still use the
     *  rest of the standard actor library.
     */
    private static final class _OptionalLibraryFilter
            extends MoMLFilterSimple {
        @Override
        public String filterAttributeValue(NamedObj container, String element,
                String attributeName, String attributeValue, String xmlFile) {
            if ("input".equals(element) && "source".equals(attributeName)
                    && attributeValue != null
                    && (_isOptionalSource(attributeValue))) {
                return null;
            }
            return attributeValue;
        }

        @Override
        public void filterEndElement(NamedObj container, String elementName,
                StringBuffer currentCharData, String xmlFile) {
        }

        @Override
        public String toString() {
            return "Skip optional Ptolemy libraries with unavailable"
                    + " third-party dependencies.\n";
        }

        private static boolean _isOptionalSource(String source) {
            return source.endsWith("ptolemy/actor/ptalon/ptalon.xml")
                    || source.endsWith("ptolemy/actor/lib/colt/colt.xml")
                    || source.endsWith("ptolemy/configs/jjsUtilities.xml")
                    || source.endsWith("ptolemy/actor/lib/jjs/js.xml");
        }
    }
}
