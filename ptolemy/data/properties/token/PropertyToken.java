package ptolemy.data.properties.token;

import ptolemy.data.Token;
import ptolemy.data.properties.Property;


public class PropertyToken extends Property {

    private Token _token;

    public PropertyToken (Token token) {
        _token = token;
    }
    
    public boolean isCompatible(Property property) {
        return property instanceof PropertyToken;
    }

    public boolean isConstant() {
        return true;
    }

    public boolean isInstantiable() {
        return true;
    }

    public boolean isSubstitutionInstance(Property property) {
        return property instanceof PropertyToken;
    }

    public Token getToken() {
        return _token;
    }
    
    public String toString() {
        return _token.toString();
    }
    
    public boolean equals(Object object) {
        if (object instanceof PropertyToken) {
            PropertyToken property = (PropertyToken) object;
            return _token.equals(property._token);
        } 
        return false;
    }
}
