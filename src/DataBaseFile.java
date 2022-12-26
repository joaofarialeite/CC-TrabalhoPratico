import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseFile {
    private ConfigurationFile cf;
    private List<String> dataForSP = new ArrayList<>();
    private int DBLines = 0;
    private Map<String, String> macros = new HashMap<>();
    private Map<String, String> alias = new HashMap<>();
    private List<String> servidoresAutoritativosAUX = new ArrayList<>();
    private List<String> servidoresEmailAUX = new ArrayList<>();

    public DataBaseFile(ConfigurationFile cf) {
        this.cf = cf;
    }

    public boolean validaDBValues(String v) {
        return v.equals("DEFAULT") || v.equals("SOASP") || v.equals("SOAADMIN") || v.equals("SOASERIAL") || v.equals("SOAREFRESH") || v.equals("SOARETRY")
                || v.equals("SOAEXPIRE") || v.equals("NS") || v.equals("MX") || v.equals("A") || v.equals("CNAME");
    }

    /*public void readDataBaseFile(String filePath) throws IOException {
        FileReader file = new FileReader(filePath);
        BufferedReader buffer = new BufferedReader(file);

        while (buffer.ready()) {
            String line = String.valueOf(buffer.readLine());
            String[] splitLine = line.split(" ");
            if (line.startsWith("#") || line.isBlank()) continue;
            else if (validaDBValues(splitLine[1]) && splitLine.length >= 4 || validaDBValues(splitLine[1]) && splitLine[1].equals("DEFAULT") && splitLine.length >= 3)
                this.dataForSP.add(line);
            else {
                System.out.println("Existem linhas inválidas no ficheiro de base de dados.");
                this.cf.getLf().writeIntoLogFile(this.cf.getLogFile(), "FL " + this.cf.getSP() + " db-file-read - linhas inválidas nop ficheiro de dados do SP");
                continue;
            }
            DBLines++;
        }
        // escreve no ficheiro de log a entrada que corresponde à leitura da base de dados do servidor
        this.cf.getLf().writeIntoLogFile(this.cf.getLogFile(), "EV " + this.cf.getDD() + " db-file-read " + this.cf.getDB());
        //System.out.println("Tudo válido!");
        buffer.close();
        file.close();
    }*/

    public void readDataBaseFile(String filePath, Cache c) throws IOException {
        FileReader file = new FileReader(filePath);
        FileReader fileCNAME = new FileReader(filePath);
        BufferedReader buffer = new BufferedReader(file);
        BufferedReader bufferCNAME = new BufferedReader(fileCNAME);

        while (bufferCNAME.ready()) {
            String line = bufferCNAME.readLine();
            String[] splitLine = line.split(" ");
            if (line.startsWith("#") || line.isBlank()) continue;
            if (splitLine[1].equals("DEFAULT")) this.macros.put(splitLine[0], splitLine[2]);
            else if (splitLine[1].equals("CNAME")) this.alias.put(splitLine[0], splitLine[2]);
        }

        while (buffer.ready()) {
            String line = String.valueOf(buffer.readLine());
            String[] splitLine = line.split(" ");
            if (line.startsWith("#") || line.isBlank() || splitLine[1].equals("DEFAULT") || splitLine[1].equals("CNAME"))
                continue;
            if (validaDBValues(splitLine[1]) && splitLine.length >= 4) {
                if (this.macros.containsKey("@"))
                    line = line.replace("@", this.macros.get("@"));
                if (this.macros.containsKey("TTL"))
                    line = line.replace("TTL", this.macros.get("TTL"));
                switch (splitLine[1]) {
                    case "NS":
                        this.servidoresAutoritativosAUX.add(line);
                        break;
                    case "MX":
                        this.servidoresEmailAUX.add(line);
                        break;
                    case "A":
                        for (String s : this.servidoresAutoritativosAUX) {
                            String[] splitS = s.split(" ");
                            if (splitS[2].startsWith(splitLine[0]))
                                line = line.replace(splitLine[0], splitS[2]);
                        }
                        for (String s : this.servidoresEmailAUX) {
                            String[] splitS = s.split(" ");
                            if (splitS[2].startsWith(splitLine[0]))
                                line = line.replace(splitLine[0], splitS[2]);
                        }
                        for (String s : this.alias.keySet()) {
                            if (splitLine[0].contains(s)) {
                                String newLine = line.substring(s.length());
                                line = this.alias.get(s).concat(newLine);
                            }
                        }
                }
                /*
                adiciona as linhas do ficheiro de dados à cache do SP. Todos os dados já estão formatados
                 */
                c.addEntryToCache(line, "FILE", Timestamp.valueOf(LocalDateTime.now()));
            } else {
                System.out.println("Existem linhas inválidas no ficheiro de base de dados.");
                this.cf.getLf().writeIntoLogFile(this.cf.getLogFile(), "FL " + this.cf.getSP() + " db-file-read - linhas inválidas nop ficheiro de dados do SP");
                continue;
            }
            DBLines++;
        }
        // escreve no ficheiro de log a entrada que corresponde à leitura da base de dados do servidor
//        this.cf.getLf().writeIntoLogFile(this.cf.getLogFile(), "EV " + this.cf.getDD() + " db-file-read " + this.cf.getDB());
        buffer.close();
        file.close();
    }

    public List<String> getDataForSP() {
        return this.dataForSP;
    }

    public int getDBLines() {
        return this.DBLines;
    }

    public void setDBLines(int DBLines) {
        this.DBLines = DBLines;
    }
}