package ptolemy.actor.gt;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class ChoiceParameter extends StringParameter {

    public ChoiceParameter(NamedObj container, String name, Class<?> enumClass)
            throws IllegalActionException, NameDuplicationException {
        super(container, name);
        if (!enumClass.isEnum()) {
            throw new IllegalActionException("Only a Java enum class is " +
                    "accepted as parameter to enumClass.");
        }
        int i = 0;
        for (Object enumObject : enumClass.getEnumConstants()) {
            String value = enumObject.toString();
            addChoice(value);
            if (i++ == 0) {
                setExpression(value);
            }
        }
        _enumClass = enumClass;
    }

    public Object getChosenValue() throws IllegalActionException {
        String expression = getExpression();
        for (Object enumObject : _enumClass.getEnumConstants()) {
            if (expression.equals(enumObject.toString())) {
                return enumObject;
            }
        }
        return null;
    }

    private Class<?> _enumClass;
}
