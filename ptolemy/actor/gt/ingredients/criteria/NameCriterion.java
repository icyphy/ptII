package ptolemy.actor.gt.ingredients.criteria;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import ptolemy.actor.gt.GTIngredientElement;
import ptolemy.actor.gt.GTIngredientList;
import ptolemy.actor.gt.ValidationException;
import ptolemy.actor.gt.util.RegularExpressionString;
import ptolemy.kernel.util.NamedObj;

public class NameCriterion extends Criterion {

    public NameCriterion(GTIngredientList owner) {
        this(owner, "");
    }

    public NameCriterion(GTIngredientList owner, String values) {
        super(owner, 1);
        setValues(values);
    }

    public GTIngredientElement[] getElements() {
        return _ELEMENTS;
    }

    public Object getValue(int index) {
        switch (index) {
        case 0:
            return _name;
        default:
            return null;
        }
    }

    public String getValues() {
        StringBuffer buffer = new StringBuffer();
        _encodeStringField(buffer, 0, _name.get());
        return buffer.toString();
    }

    public boolean match(NamedObj object) {
        Pattern pattern = _name.getPattern();
        Matcher matcher = pattern.matcher(object.getName());
        return matcher.matches();
    }

    public void setValue(int index, Object value) {
        switch (index) {
        case 0:
            _name.set((String) value);
            break;
        }
    }

    public void setValues(String values) {
        FieldIterator fieldIterator = new FieldIterator(values);
        _name.set(_decodeStringField(0, fieldIterator));
    }

    public void validate() throws ValidationException {
        if (_name.get().equals("")) {
            throw new ValidationException("Name must not be empty.");
        }

        try {
            _name.getPattern();
        } catch (PatternSyntaxException e) {
            throw new ValidationException("Regular expression \"" + _name +
                    "\" cannot be compiled.", e);
        }
    }

    private static final CriterionElement[] _ELEMENTS = {
        new StringCriterionElement("name", false, true, false)
    };

    private RegularExpressionString _name = new RegularExpressionString();
}
