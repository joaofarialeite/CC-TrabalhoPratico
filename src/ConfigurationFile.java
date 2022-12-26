import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


public class ConfigurationFile {
    private String domain = null;
    private String DB = null;
    private List<String> SS = new ArrayList<>();
    private String SP = null;
    private String DD = null;
    private String ST = null;
    private String logFile = null;
    private String allLogFile = null;
    private LogFile lf = new LogFile();
    private Map<String, String> serverPortAndAddress = new HashMap<>();
    private List<String> rootServers = new ArrayList<>();

    private int PortSP = 0;

    public boolean validaCFValues(String v) {
        return v.equals("DB") || v.equals("SP") || v.equals("SS") || v.equals("DD") || v.equals("ST") || v.equals("LG");
    }

    public void readConfigurationFile(String filePath) throws IOException {
        FileReader file = new FileReader(filePath);
        BufferedReader buffer = new BufferedReader(file);

        while (buffer.ready()) {
            String line = String.valueOf(buffer.readLine());
            if (line.startsWith("#") || line.isBlank()) continue;
            String[] splitLine = line.split(" ");
            if (validaCFValues(splitLine[1])) {
                switch (splitLine[1]) {
                    case "DB":
                        this.DB = splitLine[2];
                        this.domain = splitLine[0];
                        break;
                    case "SP":
                        this.SP = splitLine[2];
                        this.domain = splitLine[0];
                        break;
                    case "ST":
                        this.ST = splitLine[2];
                        break;
                    case "SS":
                        this.SS.add(splitLine[2]);
                        break;
                    case "DD":
                        this.DD = splitLine[2];
                        break;
                    case "LG":
                        if (Objects.equals(splitLine[0], "all")) this.allLogFile = splitLine[2];
                        else this.logFile = splitLine[2];
                        break;
                    default:
                        System.out.println("Invalid parameters.");
                        break;
                }
            }
        }

        if (!(this.ST == null)) {
            file = new FileReader(this.ST);
            buffer = new BufferedReader(file);

            /*
            adiciona os rootServers
            */
            while (buffer.ready()) {
                String line = String.valueOf(buffer.readLine());
                if (line.startsWith("#") || line.isBlank()) continue;
                else this.rootServers.add(line);
            }
        }

        associatePortToIP();

        // escreve no ficheiro de log a entrada que corresponde a uma leitura do ficheiro de configuração
//        this.lf.writeIntoLogFile(this.logFile, "EV".concat(" ").concat(this.DD.concat(" ").concat("conf-file-read").concat(" ").concat(filePath)));

        buffer.close();
        file.close();
    }


    public void associatePortToIP() {
        if ((this.SP) != null) {
            String[] splitSP = this.SP.split(":");
            if (this.SP.contains(":")) {
                this.serverPortAndAddress.put(splitSP[0], splitSP[1]);
                this.PortSP = Integer.parseInt(splitSP[1]);
            } else {
                this.serverPortAndAddress.put(this.SP, "5555");
                this.PortSP = 5555;
            }
        }

        for (String s : this.SS) {
            if (s != null) {
                if (s.contains(":")) {
                    String[] splitAddress = s.split(":");
                    this.serverPortAndAddress.put(splitAddress[0], splitAddress[1]);
                } else serverPortAndAddress.put(s, "5555");
            }
        }
    }


    public String getDomain() {
        return this.domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getDB() {
        return this.DB;
    }

    public void setDB(String DB) {
        this.DB = DB;
    }

    public String getSP() {
        return this.SP;
    }

    public void setSP(String SP) {
        this.SP = SP;
    }

    public List<String> getSS() {
        return new ArrayList<>(this.SS);
    }

    public String getDD() {
        return this.DD;
    }

    public String getLogFile() {
        return this.logFile;
    }

    public String getAllLogFile() {
        return this.allLogFile;
    }

    public String getST() {
        return this.ST;
    }

    public Map<String, String> getServerPortAndAddress() {
        return new HashMap<String, String>(this.serverPortAndAddress);
    }

    public LogFile getLf() {
        return this.lf;
    }

    public int getPortSP() {
        return this.PortSP;
    }

    public List<String> getRootServers() {
        return this.rootServers;
    }
}