import java.util.*;

public class Cache {
    private ArrayList<String> lines = new ArrayList<>();
    private final int initialCapacity;

    public Cache() {
        this.lines = new ArrayList<>();
        this.initialCapacity = 3;
    }

    public Cache(ArrayList<String> lines, int initialCapacity) {
        this.lines = new ArrayList<>(this.lines);
        this.initialCapacity = initialCapacity;
    }

    public ArrayList<String> getLines() {
        return new ArrayList<>(this.lines);
    }

    public ArrayList<String> addToCache(String line) {
        if (this.lines.size() < this.initialCapacity) {
            this.lines.add(line);
        } else {
            this.lines.remove(0);
            this.lines.add(line);
        }
        return this.lines;
    }

    public static void main(String[] args) {
        Cache lines = new Cache();
        lines.addToCache("Joel");
        lines.addToCache("Marco");
        lines.addToCache("Sami");
        lines.addToCache("Lucas");
        lines.addToCache("João");

        System.out.println(lines.getLines());
    }
}