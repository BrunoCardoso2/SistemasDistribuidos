import java.util.*;
import java.util.concurrent.*;

public class Gerenciador{
    private static final Set<String> ids = ConcurrentHashMap.newKeySet();
    private static String gerarId() {
        Random r = new Random();
        String id;
        do { id = String.format("%06d", r.nextInt(1_000_000)); }
        while (!ids.add(id));
        return id;
    }

    public static void main(String[] args) {
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    String id = gerarId();
                    new ProcessBuilder("java", "-cp", ".", "Processo", id).inheritIO().start();
                } catch (Exception e) { e.printStackTrace(); }
            }
        }, 0, 40000);
    }
}
