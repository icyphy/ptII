package ptolemy.homer.kernel;

import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import ptolemy.actor.gui.PortableContainer;
import ptolemy.homer.widgets.NamedObjectWidgetInterface;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 * @author Peter
 *
 */
public class HomerWidgetElement extends PositionableElement {

    public HomerWidgetElement(NamedObj element, Scene scene) {
        super(element);
        _scene = scene;
    }

    /* (non-Javadoc)
     * @see ptolemy.homer.kernel.PositionableElement#addToContainer(ptolemy.actor.gui.PortableContainer)
     */
    @Override
    public void addToContainer(PortableContainer container)
            throws IllegalActionException {
        // Don't use this.
        throw new IllegalActionException("Don't use this method.");
    }

    public Widget getWidget() {
        if (_widget == null) {
            try {
                _widget = (Widget) WidgetLoader.loadWidget(
                        _scene, getElement(), getElement().getClass());
            } catch (IllegalActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (NameDuplicationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return _widget;
    }

    private Widget _widget;
    private Scene _scene;
}
