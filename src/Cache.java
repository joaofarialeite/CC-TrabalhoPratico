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

    public List<String> findIPAdresses() {
        List<String> aux = new ArrayList<>();
        for (String line : this.lines) {
            String[] splitLine = line.split(" ");
            if (splitLine[1].equals("A") && splitLine[9].equals("VALID"))
                aux.add(entryConvert(line));
        }
        return aux;
    }

    public Map<String, List<String>> findServidoresAutoritativos() {
        Map<String, List<String>> servidoresAutoritativos = new HashMap<>();
        for (String line : this.lines) {
            String[] splitLine = line.split(" ");
            if (splitLine[1].equals("NS") && splitLine[9].equals("VALID")) {
                if (servidoresAutoritativos.containsKey(splitLine[0]))
                    servidoresAutoritativos.get(splitLine[0]).add(entryConvert(line));
                else {
                    List<String> novo = new ArrayList<>();
                    novo.add(entryConvert(line));
                    servidoresAutoritativos.put(splitLine[0], novo);
                }
            }
        }
        return servidoresAutoritativos;
    }

    public Map<String, List<String>> findServidoresEmail() {
        Map<String, List<String>> servidoresEmail = new HashMap<>();
        for (String line : this.lines) {
            String[] splitLine = line.split(" ");
            if (splitLine[1].equals("MX") && splitLine[9].equals("VALID")) {
                if (servidoresEmail.containsKey(splitLine[0]))
                    servidoresEmail.get(splitLine[0]).add(entryConvert(line));
                else {
                    List<String> novo = new ArrayList<>();
                    novo.add(entryConvert(line));
                    servidoresEmail.put(splitLine[0], novo);
                }
            }
        }
        return servidoresEmail;
    }

    public String getSOAEXPIRE() {
        for (String entry : this.lines) {
            String[] splitEntry = entry.split(" ");
            if (splitEntry[1].equals("SOAEXPIRE")) return splitEntry[2];
        }
        return null;
    }

    /**
     * Nesta função, vamos considerar que quando é dado um index == 0, então a procura vai ser feita através do tuplo (name, type)
     * Sempre que queremos encontrar uma entrada na cache, a posição dessa entrada é sempre o indice da posição desta entrada - 1.
     */
    public int findEntry(int index, String name, String type) {
        update();
        for (; index - 1 < this.lines.size(); index++) {
            String[] splitEntry = this.lines.get(index).split(" ");
            if (splitEntry[0].equals(name) && splitEntry[1].equals(type) && splitEntry[9].equals("VALID"))
                return index;
        }
        return -1;
    }

    /*
    função que encontra todas as entradas que fazem match com o tuplo (name, type)
     */
    public List<Integer> findEntries(int index, String name, String type) {
        update();
        List<Integer> entries = new ArrayList<>();

        for (; index - 1 < this.lines.size(); index++) {
            String[] splitEntry = this.lines.get(index).split(" ");
            if (splitEntry[0].equals(name) && splitEntry[1].equals(type) && splitEntry[9].equals("VALID"))
                entries.add(index);
        }
        return entries;
    }

    public void update() {
        for (int i = 0; i < this.lines.size(); i++) {
            String[] splitEntry = this.lines.get(i).split(" ");
            String concat = splitEntry[6].concat(" ").concat(splitEntry[7]);
            if ((Timestamp.valueOf(LocalDateTime.now()).getTime() - Timestamp.valueOf(concat).getTime() > Long.parseLong(splitEntry[3])) && splitEntry[9].equals("VALID")) {
                String aux = this.lines.get(i);
                aux = aux.replace("VALID", "FREE");
                this.lines.set(i, aux);
            }
        }
    }

    public String entryConvert(String entry) {
        String[] splitEntry = entry.split(" ");
        return splitEntry[0] + " " + splitEntry[1] + " " + splitEntry[2] + " " + splitEntry[3] + " " + splitEntry[4];
    }

    /*
    Apenas para os SS. Quando o temporizador associado à idade da base de dados dum SS relativo a um domínio
    atinge o valor de SOAEXPIRE, todas as entradas com o name igual ao domínio passado como parâmetro são atualizadas para FREE.
     */
    public void SSDataExpire(String domain) {
        for (int i = 0; i < this.lines.size(); i++) {
            String[] splitEntry = this.lines.get(i).split(" ");
            String concat = splitEntry[6].concat(" ").concat(splitEntry[7]);
            if (splitEntry[0].equals(domain) && splitEntry[5].equals("SP")) {
                if ((Timestamp.valueOf(LocalDateTime.now()).getTime() - Timestamp.valueOf(concat).getTime() > Long.parseLong(getSOAEXPIRE())) && splitEntry[9].equals("VALID")) {
                    String aux = this.lines.get(i);
                    aux = aux.replace("VALID", "FREE");
                    this.lines.set(i, aux);
                }
            }
        }
    }

    public ArrayList<String> getLines() {
        return this.lines;
    }

    /**
     * Testes para a cache.
     */
    public static void main(String[] args) {
        Cache cache = new Cache();
        cache.addEntryToCache("cc.lei. SOAEXPIRE 604800 86400", "SP", Timestamp.valueOf(LocalDateTime.of(2022, 12, 18, 12, 39)));
        cache.addEntryToCache("cc.lei. NS ns1.cc.lei. 86399", "SP", Timestamp.valueOf(LocalDateTime.of(2022, 12, 21, 12, 39)));
        cache.addEntryToCache("cc.lei. NS ns2.cc.lei. 86400", "SP", Timestamp.valueOf(LocalDateTime.of(2022, 12, 20, 12, 39)));
        cache.addEntryToCache("g6e00.cc.lei. NS ns1.g6e00.cc.lei. 86400", "TEST", Timestamp.valueOf(LocalDateTime.of(2022, 12, 20, 12, 39)));
        cache.addEntryToCache("cc.lei. MX mx1.cc.lei 86400 10", "TEST", Timestamp.valueOf(LocalDateTime.now()));

        cache.SSDataExpire("cc.lei.");
        //cache.findEntry(0, "cc.lei.", "NS");
        for (String entry : cache.getLines())
            System.out.println(entry);
    }
}