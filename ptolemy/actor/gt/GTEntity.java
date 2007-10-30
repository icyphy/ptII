package ptolemy.actor.gt;

import ptolemy.kernel.util.Nameable;

public interface GTEntity extends Nameable {

    public GTIngredientsAttribute getCriteriaAttribute();

    public GTIngredientsAttribute getOperationsAttribute();

    public PatternEntityAttribute getPatternEntityAttribute();
    
    public void updateAppearance(GTIngredientsAttribute attribute);
}
