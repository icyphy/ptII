package ptolemy.data.properties;

public class PropertyableObject implements Propertyable {

    public PropertyableObject(Object object) {
        _object = object;
    }
    public void clearHighlight() {
        // Do nothing.

    }

    public void clearProperty(String useCase) {
        // Do nothing.
    }

    public void clearShowProperty() {
        // Do nothing.
    }

    public Property getProperty() {
        return _property;
    }

    public void highlight(String color) {
        // Do nothing.
    }

    public void setProperty(Property property) {
        _property = property;
    }

    public void showProperty(String property) {
        // Do nothing.
    }

    public void updateProperty(String useCase, Property property) {
        // Do nothing.
    }

    protected Object _object;

    private Property _property;

}
