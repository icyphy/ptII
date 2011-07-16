package ptolemy.homer.events;

import java.awt.event.ActionEvent;

import ptolemy.kernel.util.NamedObj;

/**
 * @author Peter
 *
 */
public class NonVisualContentEvent extends ActionEvent {

    public NonVisualContentEvent(Object source, int id, String command, NamedObj element) {
        super(source, id, command);
        _element = element;
    }
    
    public NamedObj getElement() {
        return _element;
    }
    
    private NamedObj _element;

}
