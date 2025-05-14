import javax.sound.sampled.*;
import java.io.*;
import java.net.*;

public class DynamicTDMClient {
    private static final String SERVER_IP = "192.168.0.15";
    private static final int PORT = 50005;
    private static final int TOTAL_SLOT_DURATION_MS = 160; // Toplam slot süresi

    public static void main(String[] args) throws Exception {
        Socket socket = new Socket(SERVER_IP, PORT);

        AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, false);
        TargetDataLine mic = AudioSystem.getTargetDataLine(format);
        mic.open(format);
        mic.start();

        SourceDataLine speakers=AudioSystem.getSourceDataLine(format);
        speakers.open(format);
        speakers.start();

        InputStream in=socket.getInputStream();
        OutputStream out=socket.getOutputStream();
        byte[] buffer=new byte[64];

        while(true) {
            // Konuşma kısmı
            long talkStartTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - talkStartTime < TOTAL_SLOT_DURATION_MS) {
                int count = mic.read(buffer, 0, buffer.length);
                if (count > 0) {
                    out.write(buffer, 0, count);
                }
            }
            // Dinleme kısmı
            long listenStartTime = System.currentTimeMillis();
            while (System.currentTimeMillis() - listenStartTime < TOTAL_SLOT_DURATION_MS) {
                if (in.available() > 0) {
                    int count = in.read(buffer, 0, buffer.length);
                    if (count > 0) {
                        speakers.write(buffer, 0, count);
                    }
                }
            }
        }
    }
}
