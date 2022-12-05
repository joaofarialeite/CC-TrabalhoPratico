import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class SS {
    private int port;
    private int time_out;
    private String debug;
    private String path;
    private ArrayList<String> SPDataCopy;
    private Map<String, String> macros;
    private Map<String, String> alias;
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
    public ConfigurationFile cf = new ConfigurationFile();
    public LogFile lf = new LogFile();
    public Query q = new Query();
    private int DBlines;

    public SS(int port, int time_out, String debug, String path) throws IOException {
        this.port = port;
        this.time_out = time_out;
        this.debug = debug;
        this.path = path;
        this.SPDataCopy = new ArrayList<>();
        this.macros = new HashMap<>();
        this.alias = new HashMap<>();
        this.SOASP = "";
        this.SOAADMIN = "";
        this.SOASERIAL = 0;
        this.SOAREFRESH = 0;
        this.SOARETRY = 0;
        this.SOAEXPIRE = 0;
        this.servidoresAutoritativos = new HashMap<>();
        this.servidoresAutoritativosAUX = new ArrayList<>();
        this.servidoresEmail = new HashMap<>();
        this.servidoresEmailAUX = new ArrayList<>();
        this.IPAdressesAUX = new ArrayList<>();
        this.cf.readConfigurationFile(this.path);
        this.DBlines = 0;
    }

    public SS() throws IOException {
        this.cf.readConfigurationFile(this.path);
    }

    public void fillWithData(String line) {
        if ( !(line.startsWith("#")) && !(line.isBlank())) this.SPDataCopy.add(line);
        if(this.SPDataCopy.size() == this.getDBlines()) {
            //System.out.println("Entrei");
            this.readSSData();
        }
    }

    public void readSSData() {
        //if (this.SPDataCopy.size() != this.DBlines) System.out.println( "SP" + this.SPDataCopy.size() +"DB" + this.DBlines);

        for (String line : this.SPDataCopy) {
            if (line.startsWith("#") || line.isBlank()) continue;
            String[] splitLine = line.split(" ");
            if (splitLine[1].equals("DEFAULT"))
                this.macros.put(splitLine[0], splitLine[2]);
            else if (splitLine[1].equals("CNAME"))
                this.alias.put(splitLine[0], splitLine[2]);
        }

        for (String line : this.SPDataCopy) {
            if (line.startsWith("#") || line.isBlank()) continue;
            String[] splitLine = line.split(" ");
            if (line.startsWith("#") || line.isBlank() || splitLine[1].equals("DEFAULT") || splitLine[1].equals("CNAME"))
                continue;
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
                    System.out.println("Invalid parameters." + line);
                    break;
            }
        }
    }

    public ConfigurationFile getCf() {
        return this.cf;
    }

    public LogFile getLf() {
        return this.lf;
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

    public ArrayList<String> getSPDataCopy() {
        return new ArrayList<>(this.SPDataCopy);
    }

    public int getDBlines() {
        return this.DBlines;
    }

    public void setDBlines(int DBlines) {
        this.DBlines = DBlines;
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

    public String responseQueryCliente(String query) throws IOException {
        String response = null;

        if (validaQuery(query)) {
            if (query.contains(",")) {
                String[] splitQuery = query.split(",");
                this.q.setMESSAGE_ID(Integer.parseInt(splitQuery[0]));
                this.q.setFLAGS(splitQuery[1]);
                this.q.setRESPONSE_CODE(Integer.parseInt(splitQuery[2]));
                this.q.setQUERY_INFO_TYPE(splitQuery[6].substring(0, splitQuery[6].length() - 1));
                String[] splitByCommas = splitQuery[5].split(";");
                this.q.setQUERY_INFO_NAME(splitByCommas[1]);
                // escreve no ficheiro de log a entrada que corresponde à receção de uma query por parte do servidor
                lf.writeIntoLogFile(this.cf.getAllLogFile(), "QR".concat(" ").concat(Integer.toString(this.q.getMESSAGE_ID())).concat(" ").concat(this.q.getQUERY_INFO_NAME().concat(" ").concat(this.q.getQUERY_INFO_TYPE())));
            } else {
                String[] splitQuery = query.split(" ");
                this.q.setMESSAGE_ID(ThreadLocalRandom.current().nextInt(1, 65535));
                this.q.setQUERY_INFO_NAME(splitQuery[2]);
                this.q.setQUERY_INFO_TYPE(splitQuery[3]);
                this.q.setFLAGS("R+A");
                this.q.setRESPONSE_CODE(0);
                // escreve no ficheiro de log a entrada que corresponde à receção de uma query por parte do servidor
                lf.writeIntoLogFile(this.cf.getAllLogFile(), "QR".concat(" ").concat(splitQuery[1]).concat(" ").concat(this.q.getQUERY_INFO_NAME().concat(" ").concat(this.q.getQUERY_INFO_TYPE())));
            }
            ArrayList<String> aux = new ArrayList<>();
            StringBuilder string = new StringBuilder();
            StringBuilder stringMX = new StringBuilder();
            StringBuilder stringNS = new StringBuilder();
            StringBuilder stringA = new StringBuilder();
            StringBuilder firstMX = new StringBuilder();
            StringBuilder firstNS = new StringBuilder();
            StringBuilder firstA = new StringBuilder();
            int i = 0;

            string.append(this.q.getMESSAGE_ID()).append(',').append(this.q.getFLAGS()).append(',').append(this.q.getRESPONSE_CODE()).append(',');

            this.q.setRESPONSE_VALUES(this.getServidoresEmail().get(this.q.getQUERY_INFO_NAME()));
            this.q.setNUMBER_OF_VALUES(this.q.getRESPONSE_VALUES().size());

            // constroi string para a resposta
            stringMX.append(this.q.getRESPONSE_VALUES().size());
            for (String s : this.q.getRESPONSE_VALUES()) {
                if (i < this.q.getRESPONSE_VALUES().size() - 1) {
                    firstMX.append(s).append(',').append('\n');
                    i++;
                } else firstMX.append(s).append(';');
            }
            firstMX.append('\n');
            i = 0;

            this.q.setAUTHORITIES_VALUES(this.getServidoresAutoritativos().get(this.q.getQUERY_INFO_NAME()));
            this.q.setNUMBER_OF_AUTHORITIES(this.q.getAUTHORITIES_VALUES().size());

            //constroi string para a resposta
            stringNS.append(this.q.getAUTHORITIES_VALUES().size());
            for (String s : this.q.getAUTHORITIES_VALUES()) {
                if (i < this.q.getAUTHORITIES_VALUES().size() - 1) {
                    firstNS.append(s).append(',').append('\n');
                    i++;
                } else firstNS.append(s).append(';');
            }
            firstNS.append('\n');
            i = 0;

            // verifica nos servidores autoritativos se faz algum match com um endereço IP
            for (String a : this.getIPAdressesAUX())
                for (String b : this.getServidoresAutoritativos().get(this.q.getQUERY_INFO_NAME())) {
                    String[] splitAddress = a.split(" ");
                    if (b.contains(splitAddress[0])) aux.add(a);
                }

            // verifica nos servidores de email se faz algum match com um endereço IP
            for (String a : this.getIPAdressesAUX())
                for (String b : this.getServidoresEmail().get(this.q.getQUERY_INFO_NAME())) {
                    String[] splitAddress = a.split(" ");
                    if (b.contains(splitAddress[0])) aux.add(a);
                }

            this.q.setEXTRA_VALUES(aux);
            this.q.setNUMBER_OF_EXTRA_VALUES(aux.size());

            stringA.append(this.q.getEXTRA_VALUES().size());
            for (String s : this.q.getEXTRA_VALUES())
                if (i < this.q.getEXTRA_VALUES().size() - 1) {
                    firstA.append(s).append(',').append('\n');
                    i++;
                } else firstA.append(s);
            i = 0;

            switch (this.q.getQUERY_INFO_TYPE()) {
                case "MX":
                    response = string.append(stringMX.append(',').append(stringNS.append(',')
                        .append(stringA.append(';').append(this.q.getQUERY_INFO_NAME()).append(',').append(this.q.getQUERY_INFO_TYPE())
                                .append(';').append('\n').append(firstMX.append(firstNS.append(firstA.append(';'))))))).toString();
                    break;
                case "NS":
                    response = string.append(stringNS.append(',').append(stringMX.append(',')
                        .append(stringA.append(';').append(this.q.getQUERY_INFO_NAME()).append(',').append(this.q.getQUERY_INFO_TYPE()).append(';')
                                .append('\n').append(firstNS.append(firstMX.append(firstA.append(';'))))))).toString();
                    break;
                case "A" :
                    response = string.append(stringA.append(',').append(stringNS.append(',').append(stringMX.append(';').append(this.q.getQUERY_INFO_NAME())
                                .append(',').append(this.q.getQUERY_INFO_TYPE()).append(';').append('\n').append(firstA.append(';').append('\n').append(firstNS.append(firstMX)))))).toString();
                    break;
                default:
                    System.out.println("Nao li nenhuma");
                    break;
            }

            //System.out.println(response);

            if (query.contains(",")) {
                // escreve no ficheiro de log a entrada que corresponde ao envio de uma query por parte do servidor
                lf.writeIntoLogFile(this.cf.getAllLogFile(), "RE".concat(" ").concat(Integer.toString(this.q.getMESSAGE_ID())).concat(" ")
                        .concat(this.q.getQUERY_INFO_NAME().concat(" ").concat(this.q.getQUERY_INFO_TYPE())));
            } else {
                String[] splitQuery = query.split(" ");
                // escreve no ficheiro de log a entrada que corresponde ao envio de uma query por parte do servidor
                lf.writeIntoLogFile(this.cf.getAllLogFile(), "RE".concat(" ").concat(splitQuery[1]).concat(" ")
                        .concat(this.q.getQUERY_INFO_NAME().concat(" ").concat(this.q.getQUERY_INFO_TYPE())));
            }
            return response;
        }
        return "A query recebida não é válida.";
    }

    public boolean validaTypeValue(String query) {
        if (query.contains(",")) {
            String[] splitQuery = query.split(",");
            String[] splitAgain = splitQuery[6].split(";");
            return !splitAgain[0].equals("MX") && !splitAgain[0].equals("NS") && !splitAgain[0].equals("A");
        }
        String[] splitQuery = query.split(" ");
        return !splitQuery[3].equals("MX") && !splitQuery[3].equals("NS") && !splitQuery[3].equals("A");
    }

    public boolean validaQuery(String query) throws IOException {
        if (query.contains(",")) {
            String[] splitQuery1 = query.split(",");
            String[] splitAgain = splitQuery1[5].split(";");
            if (!this.cf.getDomain().concat(".").equals(splitAgain[1])) {
                lf.writeIntoLogFile(this.cf.getAllLogFile(), "FL" + " " + this.cf.getDD() + " " + "conf-file-read" + " A query enviada para o servidor não é válida. O servidor não é SP ou SS do domínio indicado.");
                System.out.println("O servidor não é SP ou SS do domínio indicado.");
                return false;
            } else if (validaTypeValue(query)) {
                lf.writeIntoLogFile(this.cf.getAllLogFile(), "FL" + " " + this.cf.getDD() + " " + "db-file-read" + " A query enviada para o servidor não é válida. Type value não existe.");
                System.out.println("Type value não existe.");
                return false;
            }
        } else {
            String[] splitQuery2 = query.split(" ");
            if (!this.cf.getDomain().concat(".").equals(splitQuery2[2])) {
                lf.writeIntoLogFile(this.cf.getAllLogFile(), "FL" + " " + this.cf.getDD() + " " + "conf-file-read" + " A query enviada para o servidor não é válida. O servidor não é SP ou SS do domínio indicado.");
                System.out.println("O servidor não é SP ou SS do domínio indicado.");
                return false;
            } else if (validaTypeValue(query)) {
                lf.writeIntoLogFile(this.cf.getAllLogFile(), "FL" + " " + this.cf.getDD() + " " + "db-file-read" + " A query enviada para o servidor não é válida. Type value não existe.");
                System.out.println("Type value não existe.");
                return false;
            }
        }
        return true;
    }
}