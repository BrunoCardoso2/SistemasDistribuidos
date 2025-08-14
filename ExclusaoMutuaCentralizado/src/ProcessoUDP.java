import java.net.*;
import java.util.Random;

public class ProcessoUDP implements Runnable {
    private int id;
    private Random random = new Random();

    public ProcessoUDP(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName("localhost");

            while (true) {
                // Espera randômica antes de pedir recurso
                Thread.sleep((10 + random.nextInt(16)) * 1000L);

                String mensagem = String.valueOf(id);
                byte[] buffer = mensagem.getBytes();
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, 5000);
                socket.send(packet);

                // Recebe resposta
                byte[] recvBuffer = new byte[256];
                DatagramPacket response = new DatagramPacket(recvBuffer, recvBuffer.length);
                socket.receive(response);
                String resp = new String(response.getData(), 0, response.getLength());

                if ("OK".equals(resp)) {
                    int tempo = 5 + random.nextInt(11);
                    System.out.println("Processo " + id + " usando recurso por " + tempo + "s");
                    Thread.sleep(tempo * 1000L);

                    // Envia mensagem para coordenador liberar recurso
                    String liberar = "LIBERAR";
                    buffer = liberar.getBytes();
                    socket.send(new DatagramPacket(buffer, buffer.length, address, 5000));
                } else {
                    System.out.println("Processo " + id + " não conseguiu acesso. Coordenador morto ou recurso ocupado.");
                }
            }
        } catch (Exception e) {
            System.out.println("Processo " + id + " terminado.");
        }
    }
}
