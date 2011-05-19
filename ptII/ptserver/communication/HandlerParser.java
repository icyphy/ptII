package ptserver.communication;

import java.util.HashMap;

import ptolemy.data.Token;

public class HandlerParser {
    static private HashMap<String, TokenHandler<?>> handlerMap = new HashMap<String, TokenHandler<?>>();

    private static HashMap<String, TokenHandler<?>> getHandlerMap() {
        return handlerMap;
    }

    static public <T extends TokenHandler<?>> void addHandler(T handler) {
        handlerMap.put(handler.getName(), handler);
    }

    @SuppressWarnings("unchecked")
    static public <T extends TokenHandler<Token>> T getHandler(String name) {
        return (T) getHandlerMap().get(name);
    }

}
