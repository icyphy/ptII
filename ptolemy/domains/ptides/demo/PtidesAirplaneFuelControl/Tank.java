package ptolemy.domains.ptides.demo.PtidesAirplaneFuelControl;

import ptolemy.data.DoubleToken;
import ptolemy.data.expr.Parameter;
import ptolemy.domains.wireless.kernel.WirelessComposite;
import ptolemy.kernel.CompositeEntity;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.vergil.icon.EditorIcon;
import ptolemy.vergil.kernel.attributes.EllipseAttribute;
import ptolemy.vergil.kernel.attributes.RectangleAttribute;

public class Tank extends WirelessComposite {

    public Tank(CompositeEntity container, String name)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);

        EditorIcon node_icon = new EditorIcon(this, "_icon");
        _rect = new RectangleAttribute(node_icon, "_rect");
        _rect2 = new RectangleAttribute(node_icon, "_rect2");
        //_rect.centered.setToken("true");
        _rect.width.setToken("50");
        _rect.height.setToken("50");
        _rect2.width.setToken("50");
        _rect2.height.setToken("0");

        _rect.fillColor.setToken("{0.0, 0.0, 0.5, 1.0}");
        _rect.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");
        _rect2.fillColor.setToken("{1.0, 1.0, 1.0, 1.0}");
        _rect2.lineColor.setToken("{0.0, 0.0, 0.0, 1.0}");

        node_icon.setPersistent(false);
    }

    @Override
    public void attributeChanged(Attribute attribute)
            throws IllegalActionException {
        super.attributeChanged(attribute);

    }

    @Override
    public void fire() throws IllegalActionException {
        super.fire();
        _rect2.height.setToken(new DoubleToken(
                50 - ((DoubleToken) ((Parameter) getAttribute("fuel_level"))
                        .getToken()).doubleValue() / 2));
    }

    private RectangleAttribute _rect;
    private RectangleAttribute _rect2;

}
