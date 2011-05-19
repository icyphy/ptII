package ptserver.communication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ptolemy.data.DoubleToken;
import ptolemy.data.LongToken;
import ptolemy.data.Token;

public class TokenPublisher {
    private final MQTTPublisher publisher;
    private String topic;
    private long lastSent;
    private long period;
    private int tokenCount;
    private int tokensPerPeriod;

    public TokenPublisher(long period, MQTTPublisher publisher, String topic,
            int tokensPerPeriod) {
        this.publisher = publisher;
        this.period = period;
        this.topic = topic;
        lastSent = System.currentTimeMillis();
        this.tokensPerPeriod = tokensPerPeriod;
    }

    public MQTTPublisher getPublisher() {
        return publisher;
    }

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
            10000);

    private int count;

    public void sendToken(Token token) {
        TokenHandler<Token> handler = null;
        if (token instanceof DoubleToken) {
            handler = HandlerParser.getHandler("a");
        } else if (token instanceof LongToken) {
            handler = HandlerParser.getHandler("l");
        }
        try {
            handler.convertToBytes(token, outputStream);
        } catch (IOException e1) {
        }
        tokenCount++;
        long now = System.currentTimeMillis();
        if (now - lastSent >= period) {
            try {
                publisher.publishToTopic(topic, outputStream.toByteArray());
                System.out.println(count++);
            } catch (MqttException e) {
            }
            if (tokenCount > tokensPerPeriod) {
                long waitTime = (long) (1.0 * (tokenCount - tokensPerPeriod)
                        / tokensPerPeriod * period);
                try {
                    Thread.sleep(waitTime);
                } catch (InterruptedException e) {
                }
            }
            outputStream.reset();
            tokenCount = 0;
            lastSent = System.currentTimeMillis();
        }
    }

}
