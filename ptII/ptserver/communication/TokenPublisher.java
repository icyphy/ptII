package ptserver.communication;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import ptolemy.data.Token;
import ptolemy.kernel.util.IllegalActionException;
import ptserver.data.TokenParser;

import com.ibm.mqtt.MqttException;

public class TokenPublisher {
    private final MQTTPublisher publisher;
    private final String topic;
    private long lastSent;
    private final long period;
    private int tokenCount;
    private final int tokensPerPeriod;

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

    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream(
            10000);

    private int count;

    public void sendToken(Token token) {
        try {
            TokenParser.getInstance().convertToBytes(token, outputStream);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (IllegalActionException e) {
            e.printStackTrace();
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
