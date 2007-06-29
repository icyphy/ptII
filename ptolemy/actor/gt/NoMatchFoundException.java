package ptolemy.actor.gt;

public class NoMatchFoundException extends SubgraphMatchingException {

    public NoMatchFoundException() {
        super("No match found.");
    }
    
    private static final long serialVersionUID = 2579085528185146927L;
    
}
