package ptserver.data;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.ResourceBundle;

import ptolemy.data.Token;

public class HandlerParser {
    private static final ResourceBundle tokenHandlersBundle = ResourceBundle
            .getBundle("TokenHandlers");
    private static final LinkedHashMap<Class<? extends Token>, TokenHandler<?>> handlerMap = new LinkedHashMap<Class<? extends Token>, TokenHandler<?>>();
    private static final ArrayList<TokenHandler<?>> handlerList = new ArrayList<TokenHandler<?>>();

    private static LinkedHashMap<Class<? extends Token>, TokenHandler<?>> getHandlerMap() {
        return handlerMap;
    }

    public static <C extends Token, T extends TokenHandler<C>> void addHandler(
            Class<C> tokenType, T handler) {
        handlerMap.put(tokenType, handler);
    }

    @SuppressWarnings("unchecked")
    public static <T extends TokenHandler<Token>> T getHandler(String name) {
        return (T) getHandlerMap().get(name);
    }

    private HandlerParser() {
        for (String key : tokenHandlersBundle.keySet()) {
            String value = tokenHandlersBundle.getString(key);
            try {
                ClassLoader classLoader = this.getClass().getClassLoader();
                Class<Token> tokenClass = (Class<Token>) classLoader
                        .loadClass(key);
                TokenHandler<? extends Token> tokenHandler = (TokenHandler<? extends Token>) classLoader
                        .loadClass(value).newInstance();
                handlerMap.put(tokenClass, tokenHandler);
                handlerList.add(tokenHandler);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public <T extends Token> void convertToBytes(T token,
            ByteArrayOutputStream outputStream) throws IOException {
        TokenHandler<T> tokenHandler = (TokenHandler<T>) handlerMap.get(token.getClass());
        outputStream.write(tokenHandler.getPosition());
        tokenHandler.convertToBytes(token, outputStream);
    }

    public <T extends Token> T convertToToken(byte[] stream, int startPos,
            int length) {
        
        return null;
    }
    

}
