package ptolemy.actor.ptalon.demo;

//////////////////////////////////////////////////////////////////////////
////KeyValuePair

/**
 A pair of Strings, one a key, and one a value.

 @author Adam Cataldo
 */

public class KeyValuePair {
    /**
     * Create a new KeyValuePair with the given key
     * and value.
     * @param key The key.
     * @param value The value.
     */
    public KeyValuePair(String key, String value) {
        _key = key;
        _value = value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                         public methods                    ////

    /**
     * @return The key of this KeyValuePair.
     */
    public String getKey() {
        return _key;
    }

    /**
     * @return The value of this KeyValuePair.
     */
    public String getValue() {
        return _value;
    }

    ///////////////////////////////////////////////////////////////////
    ////                        private members                    ////

    /**
     * The key.
     */
    private String _key;

    /**
     * The value.
     */
    private String _value;
}
