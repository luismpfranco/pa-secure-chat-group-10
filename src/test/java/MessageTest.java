import org.example.Client;
import org.example.Message;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
/**
 * This class tests the Message class.
 */
public class MessageTest {
    /**
     * Tests the constructor and the getters of the Message class.
     */
    @Test
    void testConstructorAndGetters(){
        String testMessage = "Test message";
        String testUsername = "TestUser";

        Message message = new Message(testMessage.getBytes(), testUsername.getBytes());

        assertArrayEquals(testMessage.getBytes(), message.getMessage(), "Message should match");
        assertArrayEquals(testUsername.getBytes(), message.getDigest(), "Username should match");
    }
}