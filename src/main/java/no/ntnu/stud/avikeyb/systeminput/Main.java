package no.ntnu.stud.avikeyb.systeminput;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_17;
import org.java_websocket.handshake.ServerHandshake;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

/**
 * Created by pitmairen on 28/03/2017.
 */
public class Main {


    public static void main(String[] args) throws URISyntaxException, InterruptedException, AWTException {
        //WebSocketImpl.DEBUG = true;
        if(args.length == 0){
            System.out.println("Usage java -jar systeminput.jar ws://example.com/output");
            System.exit(1);
        }

        Main main = new Main(new URI(args[0]));
        main.run();
    }


    private final URI keyboardOutputSocketURI;
    private final Robot robot;

    private static HashMap<Character, Integer> characterToKeyEventMap = new HashMap<>();
    private static StringBuilder outputBuffer = new StringBuilder();
    private WebSocketClient ws;


    public Main(URI keyboardOutputSocket) throws AWTException {
        this.robot = new Robot();
        this.keyboardOutputSocketURI = keyboardOutputSocket;
    }


    private void run() {

        connectToServer();

        while (true) {
            // Continuously check and send the buffer if there is anything to send
            robot.delay(10);
            sendBuffer();
        }

    }


    // Adds the output received from the socket to the output buffer.
    // Because it is unclear if the robot is thread safe, the robot is not called directly from the web socket thread.
    // Instead the buffer is used so that the robot is only called from the main thread.
    private synchronized void addToOutputBuffer(String output) {
        outputBuffer.append(output);
    }


    // Translate the current output buffer into key events and send it to the robot
    private synchronized void sendBuffer() {

        if (outputBuffer.length() == 0) {
            return;
        }

        String output = outputBuffer.toString();
        outputBuffer.setLength(0); // Reset the output buffer

        for (int i = 0; i < output.length(); i++) {
            Character c = output.charAt(i);
            if (characterToKeyEventMap.containsKey(c)) {
                robot.keyPress(characterToKeyEventMap.get(c));
                robot.keyRelease(characterToKeyEventMap.get(c));
            }
        }

    }

    private synchronized void connectToServer() {

        System.out.println("Trying to connect to server " + keyboardOutputSocketURI);

        ws = new WebSocketClient(keyboardOutputSocketURI, new Draft_17()) {
            @Override
            public void onMessage(String message) {
                System.out.println("received " + message);
                addToOutputBuffer(message);
            }

            @Override
            public void onOpen(ServerHandshake handshake) {
                System.out.println("Connected to server");
            }

            @Override
            public void onClose(int code, String reason, boolean remote) {
                System.out.println("Connection to server closed. Reconnecting...");
                connectToServer();
            }

            @Override
            public void onError(Exception ex) {
                ex.printStackTrace();
            }
        };
        ws.connect();

    }


    // Helper method for adding character to event mappings
    private static void m(Character character, int event) {
        characterToKeyEventMap.put(character, event);
    }

    static {

        m('a', KeyEvent.VK_A);
        m('b', KeyEvent.VK_B);
        m('c', KeyEvent.VK_C);
        m('d', KeyEvent.VK_D);
        m('e', KeyEvent.VK_E);
        m('f', KeyEvent.VK_F);
        m('g', KeyEvent.VK_G);
        m('h', KeyEvent.VK_H);
        m('i', KeyEvent.VK_I);
        m('j', KeyEvent.VK_J);
        m('k', KeyEvent.VK_K);
        m('l', KeyEvent.VK_L);
        m('m', KeyEvent.VK_M);
        m('n', KeyEvent.VK_N);
        m('o', KeyEvent.VK_O);
        m('p', KeyEvent.VK_P);
        m('q', KeyEvent.VK_Q);
        m('r', KeyEvent.VK_R);
        m('s', KeyEvent.VK_S);
        m('t', KeyEvent.VK_T);
        m('u', KeyEvent.VK_U);
        m('v', KeyEvent.VK_V);
        m('w', KeyEvent.VK_W);
        m('x', KeyEvent.VK_X);
        m('y', KeyEvent.VK_Y);
        m('z', KeyEvent.VK_Z);
        m(' ', KeyEvent.VK_SPACE);
        m('!', KeyEvent.VK_EXCLAMATION_MARK);
        m(',', KeyEvent.VK_COMMA);
        m('.', KeyEvent.VK_PERIOD);

    }
}
