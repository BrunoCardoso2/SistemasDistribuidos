import java.util.*;
import java.util.concurrent.*;

public class SistemaUDP {
    private static Set<Integer> ids = ConcurrentHashMap.newKeySet();
    private static Random random = new Random();

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();

        new Thread(() -> {
            try {
                while (true) {
                    Thread.sleep(40_000L);
                    int id;
                    do { id = random.nextInt(1000); } while (!ids.add(id));
                    ProcessoUDP p = new ProcessoUDP(id);
                    Thread t = new Thread(p);
                    threads.add(t);
                    t.start();
                    System.out.println("Processo criado com ID: " + id);
                }
            } catch (InterruptedException ignored) {}
        }).start();
    }
}
