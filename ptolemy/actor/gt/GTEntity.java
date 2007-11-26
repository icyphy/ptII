package ptolemy.actor.gt;

import java.util.Set;

import ptolemy.actor.gt.ingredients.criteria.Criterion;
import ptolemy.kernel.util.Nameable;

public interface GTEntity extends Nameable {

    public Criterion get(String name);

    public GTIngredientsAttribute getCriteriaAttribute();

    public GTIngredientsAttribute getOperationsAttribute();

    public Set<String> labelSet();

    public void updateAppearance(GTIngredientsAttribute attribute);
}
