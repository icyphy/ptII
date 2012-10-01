/**
 *
 */
package ptolemy.domains.coroutine.kernel;

import ptolemy.data.Token;

/**
 * @author shaver
 *
 */
public abstract class ControlToken extends Token {

    public enum ControlType { Non, Entry, Exit };

    public abstract boolean isEntry();

    public abstract boolean isExit();

    public interface Location {}

}
