package ptolemy.actor.gt.controller;

import java.io.Reader;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ptolemy.actor.gt.GTTools;
import ptolemy.data.Token;
import ptolemy.data.expr.FileParameter;
import ptolemy.kernel.undo.UndoStackAttribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.Location;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;
import ptolemy.kernel.util.Workspace;
import ptolemy.moml.MoMLFilter;
import ptolemy.moml.MoMLParser;

public class FileUpdater extends FileParameter {

    public FileUpdater(NamedObj container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
    }

    public Object clone(Workspace workspace) throws CloneNotSupportedException {
        FileUpdater newObject = (FileUpdater) super.clone(workspace);
        newObject._lastUndoStack = null;
        return newObject;
    }

    protected void _setToken(Token newToken) throws IllegalActionException {
        super._setToken(newToken);

        if (_lastUndoStack != null) {
            try {
                _lastUndoStack.undo();
            } catch (Exception e) {
                throw new IllegalActionException(this, e, "Unable to undo " +
                        "previous updates.");
            } finally {
                _lastUndoStack = null;
            }
        }

        if (stringValue().equals("")) {
            return;
        }

        NamedObj container = getContainer();
        NamedObj context = container.getContainer();
        UndoStackAttribute undoStack;
        try {
            undoStack = new UndoStackAttribute(context, context.uniqueName(
                    "_undoStack"));
        } catch (NameDuplicationException e) {
            // This should not happen.
            throw new IllegalActionException(this, e, "Unable to create " +
                    "empty undo stack.");
        }
        undoStack.moveToFirst();
        Location location = (Location) container.getAttribute("_location",
                Location.class);
        Reader reader = openForReading();
        MoMLParser parser = new MoMLParser();
        parser.setContext(context);
        parser.setUndoable(true);
        MoMLContentFilter filter = new MoMLContentFilter(container, context,
                location == null ? null : location.getExpression());
        MoMLParser.addMoMLFilter(filter);
        boolean isModified = MoMLParser.isModified();
        try {
            parser.parse(getBaseDirectory().toURL(), reader);
        } catch (Exception e) {
            throw new IllegalActionException(this, e, "Unable to apply the " +
                    "update in file \"" + stringValue() + "\".");
        } finally {
            MoMLParser.getMoMLFilters().remove(filter);
            MoMLParser.setModified(isModified);
            try {
                undoStack.setContainer(null);
                _lastUndoStack = undoStack;
            } catch (NameDuplicationException e) {
                // Ignore. (This should not happen.)
            }
        }
    }

    private UndoStackAttribute _lastUndoStack;

    private static class MoMLContentFilter implements MoMLFilter {

        public String filterAttributeValue(NamedObj container,
                String element, String attributeName,
                String attributeValue) {
            if (container == _container.getContainer()
                    && element.equals("entity")
                    && attributeName.equals("name")) {
                return _container.getName();
            } else {
                return attributeValue;
            }
        }

        public void filterEndElement(NamedObj container,
                String elementName, StringBuffer currentCharData)
                throws Exception {
            if (!_names.contains(container.getName(_context))) {
                container.setPersistent(false);
            }
            if (_location != null && container instanceof Location
                    && container.getContainer() == _container
                    && elementName.equals("property")
                    && container.getName().equals("_location")) {
                ((Location) container).setExpression(_location);
            }
        }

        MoMLContentFilter(NamedObj container, NamedObj context,
                String location) {
            _container = container;
            _context = context;
            _location = location;
            _recordAllNames(container);
        }

        private void _recordAllNames(NamedObj container) {
            _names.add(container.getName(_context));
            Collection<NamedObj> children = (Collection<NamedObj>) GTTools
                    .getChildren(container, true, true, true, true);
            for (NamedObj child : children) {
                _recordAllNames(child);
            }
        }

        private NamedObj _container;

        private NamedObj _context;

        private String _location;

        private Set<String> _names = new HashSet<String>();
    }
}
