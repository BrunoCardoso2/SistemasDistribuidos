import java.net.*;

public class CoordenadorUDP {
    private static boolean ocupado = false;
    private static boolean alive = true;

    public static void main(String[] args) throws Exception {
        DatagramSocket socket = new DatagramSocket(5000);

        // Thread para morrer e reviver a cada 60s
        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(60_000);
                    alive = false;
                    ocupado = false;
                    System.out.println("Coordenador morreu!");
                    Thread.sleep(1000);
                    alive = true;
                    System.out.println("Novo coordenador escolhido!");
                }
            } catch (InterruptedException e) {}
        }).start();

        byte[] buffer = new byte[256];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String msg = new String(packet.getData(), 0, packet.getLength());
            InetAddress address = packet.getAddress();
            int port = packet.getPort();

            String resposta;
            if (!alive) {
                resposta = "NEGADO";
            } else {
                synchronized (CoordenadorUDP.class) {
                    if (!ocupado) {
                        ocupado = true;
                        resposta = "OK";
                        System.out.println("Recurso concedido para: " + msg);
                    } else {
                        resposta = "NEGADO";
                    }
                }
            }

            byte[] sendData = resposta.getBytes();
            socket.send(new DatagramPacket(sendData, sendData.length, address, port));
        }
    }

    // Método para liberar o recurso (simula release)
    public static synchronized void liberar() {
        ocupado = false;
    }
}
