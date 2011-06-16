package ptserver.test;

import ptolemy.actor.gui.PortableContainer;
import ptolemy.data.Token;

public interface SysOutActorInterface {
    void printToken(Token token);

    void place(PortableContainer container);
}
