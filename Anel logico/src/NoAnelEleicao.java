import java.net.*;
import java.util.Scanner;

public class NoAnelEleicao {
    private String[] ips;
    private int[] portas;
    private int indice; // Posição no array
    private int id; // ID único do nó
    private DatagramSocket socket;
    private boolean emEleicao = false;
    private boolean coordenadorDefinido = false;

    public NoAnelEleicao(String[] ips, int[] portas, int indice, int id) throws Exception {
        this.ips = ips;
        this.portas = portas;
        this.indice = indice;
        this.id = id;
        this.socket = new DatagramSocket(portas[indice]);
    }

    public void iniciar() {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    DatagramPacket pacote = new DatagramPacket(buffer, buffer.length);
                    socket.receive(pacote);
                    String msg = new String(pacote.getData(), 0, pacote.getLength());

                    String[] partes = msg.split(":");
                    String tipo = partes[0];

                    if (tipo.equals("ELEICAO")) {
                        int idRecebido = Integer.parseInt(partes[1]);

                        if (idRecebido == id) {
                            // Voltou para mim, sou o coordenador
                            System.out.println("[ELEICAO] Eu (" + id + ") sou o coordenador!");
                            coordenadorDefinido = true;
                            emEleicao = false;
                            enviarParaProximo("COORDENADOR:" + id);
                        } else if (idRecebido < id) {
                            // Meu ID é maior, envio o meu
                            enviarParaProximo("ELEICAO:" + id);
                        } else {
                            // Apenas repassa
                            enviarParaProximo(msg);
                        }

                    } else if (tipo.equals("COORDENADOR")) {
                        int idCoordenador = Integer.parseInt(partes[1]);
                        System.out.println("[INFO] Coordenador definido: " + idCoordenador);
                        coordenadorDefinido = true;
                        emEleicao = false;

                        if (idCoordenador != id) {
                            enviarParaProximo(msg);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void enviarParaProximo(String msg) throws Exception {
        int proximo = (indice + 1) % ips.length;
        InetAddress ip = InetAddress.getByName(ips[proximo]);
        byte[] dados = msg.getBytes();
        DatagramPacket pacote = new DatagramPacket(dados, dados.length, ip, portas[proximo]);
        socket.send(pacote);
        System.out.println("[ENVIO] " + msg + " -> Nó " + proximo);
    }

    public void iniciarEleicao() throws Exception {
        if (!emEleicao) {
            emEleicao = true;
            coordenadorDefinido = false;
            enviarParaProximo("ELEICAO:" + id);
        }
    }

    public static void main(String[] args) throws Exception {
        // Configuração de IPs e portas
        String[] ips = {"127.0.0.1", "127.0.0.1", "127.0.0.1"};
        int[] portas = {5000, 5001, 5002};

        int indice = Integer.parseInt(args[0]); // 0, 1 ou 2
        int id = Integer.parseInt(args[1]); // ID único do nó

        NoAnelEleicao no = new NoAnelEleicao(ips, portas, indice, id);
        no.iniciar();

        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("Digite 'e' para iniciar eleição:");
            String cmd = sc.nextLine();
            if (cmd.equalsIgnoreCase("e")) {
                no.iniciarEleicao();
            }
        }
    }
}
