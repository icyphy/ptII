package ptolemy.data.properties;

import ptolemy.data.Token;


public class TokenProperty implements Property {

    private Token _token;

    public TokenProperty (Token token) {
        _token = token;
    }
    public boolean isCompatible(Property property) {
        return property instanceof TokenProperty;
    }

    public boolean isConstant() {
        return true;
    }

    public boolean isInstantiable() {
        return true;
    }

    public boolean isSubstitutionInstance(Property property) {
        return property instanceof TokenProperty;
    }

    public Token getToken() {
        return _token;
    }
    
    public String toString() {
        return _token.toString();
    }
}
