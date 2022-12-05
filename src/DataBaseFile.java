import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class DataBaseFile {
    private Map<String, String> macros;
    private String SOASP;
    private String SOAADMIN;
    private long SOASERIAL;
    private long SOAREFRESH;
    private long SOARETRY;
    private long SOAEXPIRE;
    private Map<String, List<String>> servidoresAutoritativos;
    private ArrayList<String> servidoresAutoritativosAUX;
    private Map<String, List<String>> servidoresEmail;
    private ArrayList<String> servidoresEmailAUX;
    private ArrayList<String> IPAdressesAUX;
    private Map<String, String> alias;
    private ConfigurationFile cf;

    private int lines_of_filedb = 0;

    public boolean validaDBValues(String v) {
        return v.equals("DEFAULT") || v.equals("SOASP") || v.equals("SOAADMIN") || v.equals("SOASERIAL") || v.equals("SOAREFRESH") || v.equals("SOARETRY")
                || v.equals("SOAEXPIRE") || v.equals("NS") || v.equals("MX") || v.equals("A") || v.equals("CNAME");
    }

    public DataBaseFile(ConfigurationFile cf) throws IOException {
        this.macros = new HashMap<>();
        this.SOASP = null;
        this.SOAADMIN = null;
        this.SOASERIAL = 0;
        this.SOAREFRESH = 0;
        this.SOARETRY = 0;
        this.SOAEXPIRE = 0;
        this.servidoresAutoritativos = new HashMap<>();
        this.servidoresAutoritativosAUX = new ArrayList<>();
        this.servidoresEmail = new HashMap<>();
        this.servidoresEmailAUX = new ArrayList<>();
        this.IPAdressesAUX = new ArrayList<>();
        this.alias = new HashMap<>();
        this.cf = cf;
    }

    public void readDataBaseFile(String filePath) throws IOException {
        FileReader file = new FileReader(filePath);
        FileReader fileCNAME = new FileReader(filePath);
        BufferedReader buffer = new BufferedReader(file);
        BufferedReader bufferCNAME = new BufferedReader(fileCNAME);
        String newLine = null;
        String[] split = null;
        int i = 0;

        while (bufferCNAME.ready()) {
            String line = String.valueOf(bufferCNAME.readLine());
            if (line.startsWith("#") || line.isBlank()) continue;
            String[] splitLine = line.split(" ");
            if (splitLine[1].equals("DEFAULT"))
                this.macros.put(splitLine[0], splitLine[2]);
            else if (splitLine[1].equals("CNAME"))
                this.alias.put(splitLine[0], splitLine[2]);
            this.lines_of_filedb++;
        }
        bufferCNAME.close();
        fileCNAME.close();

        while (buffer.ready()) {
            String line = String.valueOf(buffer.readLine());
            String[] splitLine = line.split(" ");
            if (line.startsWith("#") || line.isBlank() || splitLine[1].equals("DEFAULT") || splitLine[1].equals("CNAME"))
                continue;
            if (validaDBValues(splitLine[1]) && splitLine.length >= 4) {
                line = changeMacro(line);
                line = changeTTL(line);
                switch (splitLine[1]) {
                    case "SOASP":
                        this.SOASP = splitLine[2];
                        break;
                    case "SOAADMIN":
                        this.SOAADMIN = splitLine[2];
                        break;
                    case "SOASERIAL":
                        this.SOASERIAL = Long.parseLong(splitLine[2]);
                        break;
                    case "SOAREFRESH":
                        this.SOAREFRESH = Long.parseLong(splitLine[2]);
                        break;
                    case "SOARETRY":
                        this.SOARETRY = Long.parseLong(splitLine[2]);
                        break;
                    case "SOAEXPIRE":
                        this.SOAEXPIRE = Long.parseLong(splitLine[2]);
                        break;
                    case "NS":
                        this.servidoresAutoritativosAUX.add(splitLine[2]);
                        addToServidoresAutoritativos(line);
                        break;
                    case "MX":
                        this.servidoresEmailAUX.add(splitLine[2]);
                        addToServidoresEmail(line);
                        break;
                    case "A":
                        line = changeNS(line);
                        line = changeMX(line);
                        line = changeCNAME(line);
                        this.IPAdressesAUX.add(line);
                        break;
                    default:
                        System.out.println("Invalid parameters.");
                        break;
                }
            } else {
                System.out.println("Existem linhas inválidas no ficheiro de base de dados.");
                return;
            }
        }
        // escreve no ficheiro de log a entrada que corresponde à leitura da base de dados do servidor
        this.cf.getLf().writeIntoLogFile(this.cf.getLogFile(), "EV " + this.cf.getDD() + " db-file-read " + this.cf.getDB());
        //System.out.println("Tudo válido!");
        buffer.close();
        file.close();
    }

    public String getSOASP() {
        return this.SOASP;
    }

    public String getSOAADMIN() {
        return this.SOAADMIN;
    }

    public long getSOASERIAL() {
        return this.SOASERIAL;
    }

    public void setSOASERIAL(long SOASERIAL) {
        this.SOASERIAL = SOASERIAL;
    }

    public long getSOAREFRESH() {
        return this.SOAREFRESH;
    }

    public long getSOARETRY() {
        return this.SOARETRY;
    }

    public long getSOAEXPIRE() {
        return this.SOAEXPIRE;
    }

    public void setSOAEXPIRE(long SOAEXPIRE) {
        this.SOAEXPIRE = SOAEXPIRE;
    }

    public void setLines_of_filedb(int lines_of_filedb) {
        this.lines_of_filedb = lines_of_filedb;
    }

    public int getLines_of_filedb() {
        return this.lines_of_filedb;
    }

    public Map<String, List<String>> getServidoresAutoritativos() {
        return new HashMap<>(this.servidoresAutoritativos);
    }

    public ArrayList<String> getServidoresAutoritativosAUX() {
        return new ArrayList<>(this.servidoresAutoritativosAUX);
    }

    public Map<String, List<String>> getServidoresEmail() {
        return new HashMap<>(this.servidoresEmail);
    }

    public ArrayList<String> getServidoresEmailAUX() {
        return new ArrayList<>(this.servidoresEmailAUX);
    }

    public ArrayList<String> getIPAdressesAUX() {
        return new ArrayList<>(this.IPAdressesAUX);
    }

    public Map<String, String> getAlias() {
        return new HashMap<>(this.alias);
    }

    public Map<String, String> getMacros() {
        return new HashMap<>(this.macros);
    }

    public void addToServidoresAutoritativos(String line) {
        String[] splitString = line.split(" ");
        // o segundo if é utilizado para alterar o sp.smaller.example.com por ns1.smaller.example.com
        String[] splitAgain = splitString[2].split("\\.");
        if (this.alias.containsKey(splitAgain[0])) {
            line = line.replace(splitAgain[0], this.alias.get(splitAgain[0]));
        }
        if (this.servidoresAutoritativos.containsKey(splitString[0])) {
            List<String> aux = this.servidoresAutoritativos.get(splitString[0]);
            aux.add(line);
            this.servidoresAutoritativos.put(splitString[0], aux);
        } else {
            List<String> novo = new ArrayList<>();
            novo.add(line);
            this.servidoresAutoritativos.put(splitString[0], novo);
        }
    }

    public void addToServidoresEmail(String line) {
        String[] splitString = line.split(" ");
        if (this.servidoresEmail.containsKey(splitString[0])) {
            List<String> aux = this.servidoresEmail.get(splitString[0]);
            aux.add(line);
            this.servidoresEmail.put(splitString[0], aux);
        } else {
            List<String> novo = new ArrayList<>();
            novo.add(line);
            this.servidoresEmail.put(splitString[0], novo);
        }
    }

    /*
    Isto só funciona quando só existe este valor para identificar o objeto nas linhas
     */
    public String changeMacro(String line) {
        String[] splitLine = line.split(" ");
        if (this.macros.containsKey("@"))
            line = line.replace("@", this.macros.get("@"));
        return line;
    }

    public String changeTTL(String line) {
        String[] splitLine = line.split(" ");
        int i = 0;
        if (this.macros.containsKey("TTL"))
            line = line.replace("TTL", this.macros.get("TTL"));
        return line;
    }

    public String changeNS(String line) {
        String[] splitLine = line.split(" ");
        for (String s : this.servidoresAutoritativosAUX) {
            if (s.contains(splitLine[0])) line = line.replace(splitLine[0], s);
        }
        return line;
    }

    public String changeMX(String line) {
        String[] splitLine = line.split(" ");
        for (String s : this.servidoresEmailAUX) {
            if (s.contains(splitLine[0])) line = line.replace(splitLine[0], s);
        }
        return line;
    }

    public String changeCNAME(String line) {
        String[] splitLine = line.split(" ");
        for (String s : this.alias.keySet()) {
            if (splitLine[0].contains(s)) {
                String newLine = line.substring(s.length(), line.length());
                line = this.alias.get(s).concat(newLine);
            }
        }
        return line;
    }
}