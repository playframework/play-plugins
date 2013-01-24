package play.modules.statsd;

import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.libs.F;
import play.test.FakeApplication;
import play.test.Helpers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class StatsdTest {
    private static final int PORT = 57475;
    private DatagramSocket mockStatsd;
    private FakeApplication fakeApp;

    @Before
    public void setUp() throws IOException {
        mockStatsd = new DatagramSocket(PORT);
        mockStatsd.setSoTimeout(200);
        Map<String, String> config = ImmutableMap.<String, String>builder().put("ehcacheplugin", "disabled")
                .put("statsd.enabled", "true")
                .put("statsd.host", "localhost")
                .put("statsd.port", Integer.toString(PORT)).build();
        fakeApp = Helpers.fakeApplication(config);
        Helpers.start(fakeApp);
    }

    @After
    public void tearDown() throws Exception {
        Helpers.stop(fakeApp);
        mockStatsd.close();
    }

    @Test
    public void gaugeShouldSendGaugeMessage() throws Exception {
        Statsd.gauge("test", 42);
        assertThat(receive(), equalTo("statsd.test:42|g"));
    }

    @Test
    public void incrementShouldSendIncrementByOneMessage() throws Exception {
        Statsd.increment("test");
        assertThat(receive(), equalTo("statsd.test:1|c"));
    }

    @Test
    public void incrementShouldSendIncrementByManyMessage() throws Exception {
        Statsd.increment("test", 10);
        assertThat(receive(), equalTo("statsd.test:10|c"));
    }

    @Test
    public void timingShouldSendTimingMessage() throws Exception {
        Statsd.timing("test", 1234);
        assertThat(receive(), equalTo("statsd.test:1234|ms"));
    }

    @Test
    public void incrementShouldHopefullySendMessageWhenSampleRateJustBelowOne() throws Exception {
        Statsd.increment("test", 0.999999);
        assertThat(receive(), equalTo("statsd.test:1|c|@0.999999"));
    }

    @Test
    public void functionShouldBeTimedAndReportMessage() throws Exception {
        String result = Statsd.time("test", new F.Function0<String>() {
            @Override
            public String apply() throws Throwable {
                Thread.sleep(10);
                return "the result";
            }
        });
        assertThat(result, equalTo("the result"));
        String msg = receive();
        assertThat(msg, msg.startsWith("statsd.test:"), equalTo(true));
        assertThat(msg, msg.endsWith("|ms"), equalTo(true));
    }

    private String receive() throws IOException {
        byte[] buf = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        try {
            mockStatsd.receive(packet);
        } catch (SocketTimeoutException e) {
            fail("Message not received after 200ms");
        }
        return new String(packet.getData(), 0, packet.getLength());
    }
}
