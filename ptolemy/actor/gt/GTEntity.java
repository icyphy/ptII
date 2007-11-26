package ptolemy.actor.gt;

import java.util.Set;

import ptolemy.kernel.util.Nameable;
import ptolemy.kernel.util.NamedObj;

public interface GTEntity extends Nameable {

    public NamedObj get(String name);

    public GTIngredientsAttribute getCriteriaAttribute();

    public GTIngredientsAttribute getOperationsAttribute();
    
    public Set<String> labelSet();
    
    public void updateAppearance(GTIngredientsAttribute attribute);
}
