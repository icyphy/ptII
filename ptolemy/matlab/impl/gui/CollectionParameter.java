package ptolemy.matlab.impl.gui;

import java.util.Collection;

import ptolemy.data.expr.ChoiceParameter;
import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

/**
 *  An alternative to {@link ChoiceParameter} providing with a non-enum-based implementation.
 * 
 * @author David Guardado Barcia
 *
 */
public class CollectionParameter extends StringParameter {

	private final Collection<Object> choices;
	
	public CollectionParameter(NamedObj container, String name, Collection<Object> choices)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        int i = 0;
        for (Object choice : choices) {
            addChoice(choice.toString());
            if (i++ == 0) {
                setExpression(choice.toString());
            }
        }
        this.choices = choices;
    }

	public Object getChosenValue() {
        final String expression = getExpression();
        for (Object choice : choices) {
            if (expression.equals(choice.toString())) {
                return choice;
            }
        }
        return null;
    }

}
