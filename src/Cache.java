import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Cache {
    private ArrayList<String> lines = new ArrayList<>();

    public Cache() {
        this.lines = new ArrayList<>();
    }

    public Cache(ArrayList<String> lines) {
        this.lines = new ArrayList<>(this.lines);
    }

    /*
    quando é adicionada a nova entrada, além das linhas que vamos buscar ao array, também precisamos de saber a quem pertence este array (origem) e
    o tempo que passou desde o arranque do servidor até ao momento da entrada na cache.
    A nossa função de adicionar novas entradas na cache já faz a atualização das entradas na cache.
     */
    public void addEntryToCache(String line, String origin, Timestamp time) {
        String[] splitLine = line.split(" ");
        String priority = "";
        int index;

        /*
        Se a entrada não tiver definido o campo prioridade é adicionado um valor DEFAULT. Neste caso 0
         */
        if (splitLine.length < 5) priority = "0";
        else priority = splitLine[4];

        /*
        o index começa em 1
         */
        if (this.lines.size() == 0) index = 1;
        else index = this.lines.size() + 1;

        String entry = splitLine[0] + " " + splitLine[1] + " " + splitLine[2] + " " + splitLine[3] + " " + priority + " " + origin + " " + time + " " + index + " VALID";
        this.lines.add(entry);
    }

    /*
    Nesta função, vamos considerar que quando é dado um index == 0, então a procura vai ser feita através do tuplo (name, type)
     */
    public int findEntry(int index, String name, String type) {
        for (; index < this.lines.size(); index++) {
            String[] splitEntry = this.lines.get(index).split(" ");
            if (splitEntry[0].equals(name) && splitEntry[1].equals(type) && splitEntry[9].equals("VALID"))
                return index + 1;
        }
        return -1;
    }

    /*
    função que encontra todas as entradas que fazem match com o tuplo (name, type)
     */
    public List<Integer> findEntries(int index, String name, String type) {
        List<Integer> entries = new ArrayList<>();

        for (; index < this.lines.size(); index++) {
            String[] splitEntry = this.lines.get(index).split(" ");
            if (splitEntry[0].equals(name) && splitEntry[1].equals(type) && splitEntry[9].equals("VALID"))
                entries.add(index + 1);
        }
        return entries;
    }

    public void update() {
        for (int i = 0; i < this.lines.size(); i++) {
            String[] splitEntry = this.lines.get(i).split(" ");
            String concat = splitEntry[6].concat(" ").concat(splitEntry[7]);
            if ((Timestamp.valueOf(LocalDateTime.now()).getTime() - Timestamp.valueOf(concat).getTime() > Long.parseLong(splitEntry[3])))
                this.lines.remove(i);
        }
    }

    /*
    Apenas para os SS. Quando o temporizador associado à idade da base de dados dum SS relativo a um domínio
    atinge o valor de SOAEXPIRE, todas as entradas com o name igual ao domínio passado como parâmetro são atualizadas para FREE.
     */
    /*public void SSDataExpire(String domain) {
        for (String entry : this.lines) {
            String[] splitEntry = entry.split(" ");
            if (splitEntry[0].equals(domain) && splitEntry[5].equals("SS")) {
                String newEntry = entry;
                this.lines.remove(entry);
                newEntry = newEntry.replace(splitEntry[9], "FREE");
                this.lines.add(newEntry);
            }
        }
    }*/

    public ArrayList<String> getLines() {
        return this.lines;
    }

    public static void main(String[] args) {
        Cache lines = new Cache();
        lines.addEntryToCache("www A 193.136.130.79 86400 200", "SP", Timestamp.valueOf(LocalDateTime.of(2022, 12, 6, 20, 33)));
        lines.addEntryToCache("ns1 A 193.136.130.250 86400", "SS", Timestamp.valueOf(LocalDateTime.now()));
        lines.addEntryToCache("www A 193.136.130.80 86400 200", "SP", Timestamp.valueOf(LocalDateTime.now()));
        lines.addEntryToCache("www A 193.136.130.81 86400 200", "SS", Timestamp.valueOf(LocalDateTime.of(2022, 12, 8, 0, 34)));
        lines.update();
        //System.out.println(lines.findEntries(0, "www", "A"));
        System.out.println(lines.getLines());
    }
}