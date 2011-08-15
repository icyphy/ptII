package ptserver.test;

import ptolemy.actor.injection.PortableContainer;
import ptolemy.data.Token;

public interface SysOutActorInterface {
    void printToken(Token token);

    void place(PortableContainer container);
}
