package ptolemy.data.properties;

import ptolemy.actor.gui.ColorAttribute;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.Attribute;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class PropertyableNamedObj extends PropertyableObject {

    public PropertyableNamedObj(NamedObj object) {
        super(object);
    }

    private NamedObj _namedObj() {
        return (NamedObj) _object;
    }
    
    public void clearHighlight() {
        _removeAttribute("_highlightColor");
    }

    public void clearProperty(String useCase) {
        _removeAttribute(useCase);
    }

    public void clearShowProperty() {
        _removeAttribute("_showInfo");
    }

    public void highlight(String color) {
        ColorAttribute highlightAttribute = 
            (ColorAttribute) _namedObj().getAttribute("_highlightColor");
        
        if (highlightAttribute == null) {
            try {
                highlightAttribute = new ColorAttribute(
                        _namedObj(), "_highlightColor");
                
            } catch (NameDuplicationException e) {
                // This shouldn't happen. If another attribute 
                // has the same name, we should find it before.
                assert false;
            } catch (IllegalActionException e) {
                assert false;
            }
        }
        highlightAttribute.setExpression(color);
    }

    public void showProperty(String property) {
        StringParameter showAttribute = 
            (StringParameter) _namedObj().getAttribute("_showInfo");
        
        if (showAttribute == null) {
            try {
                showAttribute = new StringParameter(
                        _namedObj(), "_showInfo");
                
            } catch (NameDuplicationException e) {
                // This shouldn't happen. If another attribute 
                // has the same name, we should find it before.
                assert false;
            } catch (IllegalActionException e) {
                assert false;
            }
        }
        showAttribute.setExpression(property);
    }

    public void updateProperty(String useCase, Property property) {
        PropertyAttribute attribute = 
            (PropertyAttribute) _namedObj().getAttribute(useCase);
        
        if (attribute == null) {
            try {
                attribute = new PropertyAttribute(
                        _namedObj(), useCase);
                
            } catch (NameDuplicationException e) {
                // This shouldn't happen. If another attribute 
                // has the same name, we should find it before.
                assert false;
            } catch (IllegalActionException e) {
                assert false;
            }
        }
        attribute.setProperty(property);
    }


    private void _removeAttribute(String name) {
        Attribute highlightAttribute = 
            _namedObj().getAttribute(name);

        if (highlightAttribute != null) {
            try {
                highlightAttribute.setContainer(null);
            } catch (IllegalActionException e) {
                assert false;
            } catch (NameDuplicationException e) {
                assert false;
            }
        }
    }
}
