import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Processo{
    private static final int PORT = 5000;
    private static final String HOST = "localhost";
    private static final Random rand = new Random();

    private final String id;
    private boolean souCoordenador = false;
    private boolean vivo = true;
    private static final Queue<String> fila = new ConcurrentLinkedQueue<>();

    public Processo(String id) {
        this.id = id;
        System.out.println("Processo iniciado: " + id);
        tentarVirarCoordenador();
        iniciarRequisicoes();
    }

    private void tentarVirarCoordenador() {
        new Thread(() -> {
            try (DatagramSocket s = new DatagramSocket(PORT)) {
                souCoordenador = true;
                System.out.println("Processo " + id + " virou coordenador");
               
                Thread.sleep(60000);
                vivo = false;
                fila.clear();
                System.out.println("Coordenador " + id + " morreu. Fila limpa.");
            } catch (BindException e) {
              
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void iniciarRequisicoes() {
        new Thread(() -> {
            try (DatagramSocket socket = new DatagramSocket()) {
                while (vivo) {
                    Thread.sleep(rand.nextInt(15000) + 10000);

                    if (!souCoordenador) {
                        byte[] msg = id.getBytes();
                        DatagramPacket packet = new DatagramPacket(msg, msg.length,
                                InetAddress.getByName(HOST), PORT);
                        try { socket.send(packet); } catch(Exception ex){}

                        // solicita recurso
                        fila.add(id);
                        System.out.println("Processo " + id + " solicitou recurso");
                        System.out.println("Fila atual: " + fila);

                     
                        while (vivo && !fila.peek().equals(id)) Thread.sleep(500);

                        if (!vivo) break;

                        int uso = rand.nextInt(11) + 5;
                        System.out.println("Processo " + id + " usando recurso por " + uso + "s");
                        Thread.sleep(uso * 1000);
                        System.out.println("Processo " + id + " liberou recurso");
                        fila.poll();
                    }
                }
            } catch (Exception e) { }
        }).start();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) { System.err.println("ID obrigatório"); return; }
        new Processo(args[0]);
    }
}
