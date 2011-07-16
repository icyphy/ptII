package ptolemy.homer.events;

import java.awt.event.ActionEvent;

import ptolemy.homer.kernel.ContentPrototype;

/**
 * @author Peter
 *
 */
public class TabEvent extends ActionEvent {

    public TabEvent(Object source, int id, String command, String tag,
            String name, int position, ContentPrototype content) {
        super(source, id, command);
        _tag = tag;
        _name = name;
        _position = position;
        _content = content;
    }

    public String getTag() {
        return _tag;
    }

    public String getName() {
        return _name;
    }

    public int getPosition() {
        return _position;
    }
    
    public ContentPrototype getContent() {
        return _content;
    }

    private String _tag;
    private String _name;
    private int _position;
    private ContentPrototype _content;

}
