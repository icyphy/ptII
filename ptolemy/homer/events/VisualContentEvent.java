package ptolemy.homer.events;

import java.awt.event.ActionEvent;

import ptolemy.homer.kernel.PositionableElement;

public class VisualContentEvent extends ActionEvent {

    public VisualContentEvent(Object source, int id, String command,
            PositionableElement element) {
        super(source, id, command);
        _element = element;
    }

    public PositionableElement getElement() {
        return _element;
    }

    private PositionableElement _element;

}
