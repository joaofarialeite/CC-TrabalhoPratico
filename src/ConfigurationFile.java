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
    private Map<String, String> IPePORTofSSeSPinCF = new HashMap<>();

    private int PortSP = 0;

    private

    enum validCFValues {
        DB,
        SP,
        SS,
        DD,
        ST,
        LG
    }

    public boolean validCFString(String line) {
        String[] parts = line.split(" ");
        for (validCFValues v : validCFValues.values()) {
            if (v.name().equals(parts[1])) return true;
        }
        return false;
    }

    public void readConfigurationFile(String filePath) throws IOException {
        FileReader file = new FileReader(filePath);
        BufferedReader buffer = new BufferedReader(file);

        while (buffer.ready()) {
            String line = String.valueOf(buffer.readLine());
            if (line.startsWith("#") || line.isBlank()) continue;
            if (validCFString(line)) {
                String[] splitLine = line.split(" ");
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

        associaportaaip();

        // escreve no ficheiro de log a entrada que corresponde a uma leitura do ficheiro de configuração
        this.lf.writeIntoLogFile(this.logFile, "EV".concat(" ").concat(this.DD.concat(" ").concat("conf-file-read").concat(" ").concat(filePath)));



        buffer.close();
        file.close();
    }

    public void associaportaaip() {
        if((this.SP) != null) {
            String[] splitSP = this.SP.split(":");
            if(this.SP.contains(":")){
                this.IPePORTofSSeSPinCF.put(splitSP[0], splitSP[1]);
                this.PortSP = Integer.parseInt(splitSP[1]);
            } else{
                this.IPePORTofSSeSPinCF.put(this.SP, "5555");
                this.PortSP = 5555;
            }
        }

        for (String s : this.SS) {
            if(s!=null) {
                if (s.contains(":")) {
                    String[] splitAddress = s.split(":");
                    this.IPePORTofSSeSPinCF.put(splitAddress[0], splitAddress[1]);
                } else IPePORTofSSeSPinCF.put(s, "5555");
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

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    public String getAllLogFile() {
        return this.allLogFile;
    }

    public void setAllLogFile(String allLogFile) {
        this.allLogFile = allLogFile;
    }

    public String getST() {
        return this.ST;
    }

    public void setST(String ST) {
        this.ST = ST;
    }

    public Map<String, String> getIPePORTofSSeSPinCF() {
        return new HashMap<String, String>(this.IPePORTofSSeSPinCF);
    }

    public LogFile getLf() {
        return this.lf;
    }

    public int getPortSP() {
        return PortSP;
    }
}