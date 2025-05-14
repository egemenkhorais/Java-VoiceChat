import java.io.*;
import java.net.*;
import java.util.*;

public class DynamicTDMServer {
    private static final int PORT = 50005;
    private static final int TOTAL_SLOT_DURATION_MS = 160; // Toplam slot süresi

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(PORT);
        List<Socket> clients = new ArrayList<>();

        System.out.println("Sunucu başlatıldı, istemciler bekleniyor...");

        // İstemcileri dinamik olarak kabul et
        new Thread(() -> {
            while (true) {
                try {
                    Socket client = serverSocket.accept();
                    synchronized (clients) {
                        clients.add(client);
                    }
                    System.out.println("Yeni istemci bağlandı: " + client.getInetAddress());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        // Zaman dilimi döngüsü
        while(true) {
            List<Socket> currentClients;
            synchronized (clients){
                currentClients=new ArrayList<>(clients);
            }

            int clientCount=currentClients.size();
            if (clientCount==0) {
                try {
                    Thread.sleep(70); // Bekleme süresi
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                continue;
            }

            int individualSlot = TOTAL_SLOT_DURATION_MS / clientCount;

            for (int i = 0; i < clientCount; i++) {
                Socket sender = currentClients.get(i);
                List<Socket> receivers = new ArrayList<>(currentClients);
                receivers.remove(sender);

                try {
                    InputStream in=sender.getInputStream();
                    List<OutputStream> outs=new ArrayList<>();
                    for (Socket s:receivers) {
                        outs.add(s.getOutputStream());
                    }

                    long startTime=System.currentTimeMillis();
                    byte[] buffer=new byte[64]; //Ses için buffer (İşlemciye yüklenir değiştirilebilir)

                    while (System.currentTimeMillis() - startTime < individualSlot) {
                        if (in.available() > 0) {
                            int count = in.read(buffer);
                            if (count > 0) {
                                for (OutputStream out : outs) {
                                    out.write(buffer, 0, count);
                                }
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
