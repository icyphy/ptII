package ptolemy.homer.kernel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;

import ptolemy.homer.events.NonVisualContentEvent;
import ptolemy.homer.events.TabEvent;
import ptolemy.homer.events.VisualContentEvent;
import ptolemy.homer.gui.HomerMainFrame;
import ptolemy.homer.gui.TabScenePanel;
import ptolemy.kernel.ComponentEntity;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * @author Peter
 *
 */
public class HomerMultiContent extends MultiContent<TabScenePanel> {

    public HomerMultiContent(TabScenePanel contentPrototype,
            HomerMainFrame mainFrame) {
        super(contentPrototype);
        _mainFrame = mainFrame;
    }

    // TODO
    public HomerMultiContent(TabScenePanel contentPrototype,
            CompositeEntity model, HomerMainFrame mainFrame)
            throws IllegalActionException, NameDuplicationException {
        super(contentPrototype, model);
        _mainFrame = mainFrame;
        // Besides finding the positionable elements, all remote elements must be parsed.
    }

    public void add(NamedObj element) {
        _remoteElements.add(element);
        NonVisualContentEvent addEvent = new NonVisualContentEvent(this,
                ActionEvent.ACTION_PERFORMED, "add", element);
        _nofityAllListeners(addEvent);
    }

    public void remove(NamedObj object) {
        _remoteElements.remove(object);
        NonVisualContentEvent removeEvent = new NonVisualContentEvent(this,
                ActionEvent.ACTION_PERFORMED, "remove", object);
        _nofityAllListeners(removeEvent);
    }

    @Override
    public void addElement(String tabTag, PositionableElement element)
            throws IllegalActionException {
        super.addElement(tabTag, element);
        add(element.getElement());
        VisualContentEvent addElementEvent = new VisualContentEvent(this,
                ActionEvent.ACTION_PERFORMED, "addElement", element);
        _nofityAllListeners(addElementEvent);
    }

    @Override
    public void removeElement(PositionableElement element) {
        super.removeElement(element);
        remove(element.getElement());
        VisualContentEvent removeElementEvent = new VisualContentEvent(this,
                ActionEvent.ACTION_PERFORMED, "removeElement", element);
        _nofityAllListeners(removeElementEvent);
    }

    @Override
    public String addTab(ComponentEntity topLevel, String tag, String name, ContentPrototype content)
            throws IllegalActionException, NameDuplicationException {
        String newTag = super.addTab(topLevel, tag, name, content);

        TabEvent addTabEvent = new TabEvent(this, ActionEvent.ACTION_PERFORMED,
                "addTab", newTag, name, _order.size(), content);
        _nofityAllListeners(addTabEvent);
        return newTag;
    }

    @Override
    public void removeTab(String tag) {
        ArrayList<PositionableElement> elements = (ArrayList<PositionableElement>) _contents
                .get(tag).getElements().clone();
        for (PositionableElement element : elements) {
            removeElement(element);
        }
        int position = _order.indexOf(tag);
        TabEvent removeTabEvent = new TabEvent(this,
                ActionEvent.ACTION_PERFORMED, "removeTab", tag, _contents.get(
                        tag).getName(), position, null);
        _nofityAllListeners(removeTabEvent);
        super.removeTab(tag);
    }

    public void removeTab(int index) {
        String tag = _order.get(index);
        removeTab(tag);
    }
    
    @Override
    public void setNameAt(int position, String text) throws IllegalActionException {
        super.setNameAt(position, text);

        TabEvent renameTabEvent = new TabEvent(this, ActionEvent.ACTION_PERFORMED,
                "renameTab", _order.get(position), text, position, null);
        _nofityAllListeners(renameTabEvent);
    }

    public void addListener(ActionListener listener) {
        _listeners.add(listener);
    }

    public void removeListener(ActionListener listener) {
        _listeners.remove(listener);
    }

    private void _nofityAllListeners(ActionEvent e) {
        for (ActionListener listener : _listeners) {
            listener.actionPerformed(e);
        }
    }

    public HashSet<NamedObj> getRemoteElements() {
        return _remoteElements;
    }

    /** Clear the contents, but keep all the listeners attached.
     */
    @Override
    public void clear() {
        super.clear();
        _remoteElements.clear();
        ActionEvent clearEvent = new ActionEvent(this,
                ActionEvent.ACTION_PERFORMED, "clear");
        _nofityAllListeners(clearEvent);
    }

    public boolean contains(NamedObj key) {
        return _remoteElements.contains(key);
    }

    /** Complete list of all named objects executed remotely.
     */
    protected HashSet<NamedObj> _remoteElements = new HashSet<NamedObj>();

    protected HomerMainFrame _mainFrame;

    protected HashSet<ActionListener> _listeners = new HashSet<ActionListener>();
}
