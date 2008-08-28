package ptolemy.data.properties;

public interface Propertyable {

    public Property getProperty();
    public void setProperty(Property property);
    
    public void clearProperty(String useCase);
    public void updateProperty(String useCase, Property property);

    public void clearShowProperty();
    public void showProperty(String property);

    public void clearHighlight();
    public void highlight(String color);
}
