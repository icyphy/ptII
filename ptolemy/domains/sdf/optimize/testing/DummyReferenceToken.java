/**
 * 
 */
package ptolemy.domains.sdf.optimize.testing;

import ptolemy.data.Token;

/**
 * @author mgeilen
 *
 */
public class DummyReferenceToken extends Token {

    private Object _ref;
    
    /**
     * 
     */
    public DummyReferenceToken(Object r) {
        this._ref = r;
    }
    
    public Object getReference(){
        return this._ref;
    }

    @Override
    public String toString() {
        return this._ref.toString();
    }
    
}
