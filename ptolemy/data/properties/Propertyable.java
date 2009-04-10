package ptolemy.data.properties;

public interface Propertyable {

    public void clearHighlight();
    public void clearProperty(String useCase);

    public void clearShowProperty();
    public Property getProperty();

    public void highlight(String color);
    public void setProperty(Property property);

    public void showProperty(String property);
    public void updateProperty(String useCase, Property property);
}
