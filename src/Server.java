import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Server {
    private int port;
    private int time_out;
    private String modo_debug;
    private String path;
    private String SOASP;
    private String SOAADMIN;
    private long SOASERIAL;
    private long SOAREFRESH;
    private long SOARETRY;
    private long SOAEXPIRE;
    private ArrayList<String> servidoresAutoritativosAUX = new ArrayList<>();
    private ArrayList<String> servidoresEmailAUX = new ArrayList<>();
    private ArrayList<String> IPAdressesAUX = new ArrayList<>();
    private Map<String, String> macros = new HashMap<>();
    private Map<String, String> alias = new HashMap<>();
    private Query q = new Query();
    private LogFile lf = new LogFile();
    private ConfigurationFile cf = new ConfigurationFile();
    private int numberOfDBLines = 0;

    public Server() throws IOException {
    }

    public Server(int port, int time_out, String modo_debug, String path) throws IOException {
        this.port = port;
        this.time_out = time_out;
        this.modo_debug = modo_debug;
        this.path = path;
        this.cf.readConfigurationFile(path);
    }

    public void inicializaServidor(List<String> data) throws IOException {
        this.lf.writeIntoLogFile(this.cf.getAllLogFile(), "ST " + this.cf.getDD() + " Port:" + this.port + " Time_Out:" + this.time_out + " Mode:" + this.modo_debug + " --- Servidor inicializado");
        /*
        faz uma procura pelas macros dentro do array de dados
         */
        for (String line : data) {
            String[] splitLine = line.split(" ");
            if (splitLine[1].equals("DEFAULT")) this.macros.put(splitLine[0], splitLine[2]);
            else if (splitLine[1].equals("CNAME")) this.alias.put(splitLine[0], splitLine[2]);
        }

        /*
        inicializa um servidor com os dados necessários para responder a queries, etc
         */
        for (String line : data) {
            String[] splitLine = line.split(" ");
            if (this.macros.containsKey("@") && !splitLine[1].equals("DEFAULT"))
                line = line.replace("@", this.macros.get("@"));
            if (this.macros.containsKey("TTL") && !splitLine[1].equals("DEFAULT"))
                line = line.replace("TTL", this.macros.get("TTL"));
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
                    this.IPAdressesAUX.add(line);
                    break;
                default:
                    break;
            }
        }
    }

    public String responseQueryCliente(String query) throws IOException {
        List<String> aux = new ArrayList<>();
        int i = 0;

        if (query.contains(",")) {
            String[] splitQuery = query.split(",");
            this.q.setMESSAGE_ID(Integer.parseInt(splitQuery[0]));
            this.q.setFLAGS(splitQuery[1]);
            this.q.setRESPONSE_CODE(Integer.parseInt(splitQuery[2]));
            this.q.setQUERY_INFO_TYPE(splitQuery[6].substring(0, splitQuery[6].length() - 1));
            String[] splitByComma = splitQuery[5].split(";");
            this.q.setQUERY_INFO_NAME(splitByComma[1]);
        } else {
            String[] splitQuery = query.split(" ");
            this.q.setMESSAGE_ID(ThreadLocalRandom.current().nextInt(1, 65535));
            this.q.setQUERY_INFO_NAME(splitQuery[2]);
            this.q.setQUERY_INFO_TYPE(splitQuery[3]);
            this.q.setFLAGS("R+A");
            this.q.setRESPONSE_CODE(0);                  // valor a alterar agora na segunda fase
        }
        /*
        escrita no ficheiro de log - query recebida
         */
        this.lf.writeIntoLogFile(this.cf.getLogFile(), "QR " + this.q.getMESSAGE_ID() + " Domain:" + this.q.getQUERY_INFO_NAME() + " Type:" + this.q.getQUERY_INFO_TYPE() + " Response_Code:" + this.q.getRESPONSE_CODE());

        StringBuilder response = new StringBuilder();
        response.append(this.q.getMESSAGE_ID()).append(',').append(this.q.getFLAGS()).append(',').append(this.q.getRESPONSE_CODE()).append(',');

        /*
        adiciona aos response_values os servidoresEmail que pertencem ao domínio que é enviado na query
         */
        this.q.setRESPONSE_VALUES(this.servidoresEmailAUX.stream().filter(line -> line.startsWith(this.q.getQUERY_INFO_NAME())).toList());
        this.q.setNUMBER_OF_VALUES(this.q.getRESPONSE_VALUES().size());

        /*
        constroi String dos servidoresEmail
         */
        StringBuilder stringMX = new StringBuilder();
        StringBuilder firstMX = new StringBuilder();
        stringMX.append(this.q.getNUMBER_OF_VALUES());

        for (String line : this.q.getRESPONSE_VALUES()) {
            if (i < this.q.getNUMBER_OF_VALUES() - 1) {
                firstMX.append(line).append(',').append('\n');
                i++;
            } else firstMX.append(line).append(';');
        }
        firstMX.append('\n');
        i = 0;


        /*
        adiciona aos authorities_values os servidoresEmail que pertencem ao domínio que é enviado na query
         */
        this.q.setAUTHORITIES_VALUES(this.servidoresAutoritativosAUX.stream().filter(line -> line.startsWith(this.q.getQUERY_INFO_NAME())).toList());
        this.q.setNUMBER_OF_AUTHORITIES(this.q.getAUTHORITIES_VALUES().size());

        /*
        constroi String dos servidoresAutoritativos
         */
        StringBuilder stringNS = new StringBuilder();
        StringBuilder firstNS = new StringBuilder();
        stringNS.append(this.q.getNUMBER_OF_AUTHORITIES());

        for (String line : this.q.getAUTHORITIES_VALUES()) {
            if (i < this.q.getNUMBER_OF_AUTHORITIES() - 1) {
                firstNS.append(line).append(',').append('\n');
                i++;
            } else firstNS.append(line).append(';');
        }
        firstNS.append('\n');
        i = 0;

        /*
        adiciona os IPAdresses que fazem match com os endereços dos servidoresAutoritativos
         */
        for (String a : this.IPAdressesAUX)
            for (String b : this.q.getAUTHORITIES_VALUES()) {
                String[] splitLine = a.split(" ");
                if (b.contains(splitLine[0])) aux.add(a);
            }

        /*
        adiciona os IPAdresses que fazem match com os endereços dos servidoresEmail
         */
        for (String a : this.IPAdressesAUX)
            for (String b : this.q.getRESPONSE_VALUES()) {
                String[] splitLine = a.split(" ");
                if (b.contains(splitLine[0])) aux.add(a);
            }

        /*
        adiciona aos extra_values os endereçosIP que pertencem ao domínio que é enviado na query
         */
        this.q.setEXTRA_VALUES(aux);
        this.q.setNUMBER_OF_EXTRA_VALUES(this.q.getEXTRA_VALUES().size());

        /*
        constroi String dos servidoresAutoritativos
         */
        StringBuilder stringA = new StringBuilder();
        StringBuilder firstA = new StringBuilder();
        stringA.append(this.q.getNUMBER_OF_EXTRA_VALUES());

        for (String line : this.q.getEXTRA_VALUES()) {
            if (i < this.q.getNUMBER_OF_EXTRA_VALUES() - 1) {
                firstA.append(line).append(',').append('\n');
                i++;
            } else firstA.append(line);
        }
        i = 0;

        switch (this.q.getQUERY_INFO_TYPE()) {
            case "MX":
                response.append(stringMX).append(',').append(stringNS).append(',').append(stringA).append(';').append(this.q.getQUERY_INFO_NAME())
                        .append(',').append(this.q.getQUERY_INFO_TYPE()).append(';').append('\n').append(firstMX).append(firstNS).append(firstA).append(';');
                break;
            case "NS":
                response.append(stringNS).append(',').append(stringMX).append(',').append(stringA).append(';').append(this.q.getQUERY_INFO_NAME())
                        .append(',').append(this.q.getQUERY_INFO_TYPE()).append(';').append('\n').append(firstNS).append(firstMX).append(firstA).append(';');
                break;
            case "A":
                response.append(stringA).append(',').append(stringNS).append(',').append(stringMX).append(';').append(this.q.getQUERY_INFO_NAME())
                        .append(',').append(this.q.getQUERY_INFO_TYPE()).append(';').append('\n').append(firstA).append(';').append('\n').append(firstNS).append(firstMX);
                break;
        }
        /*
        escrita no ficheiro de log - resposta enviada
         */
        this.lf.writeIntoLogFile(this.cf.getLogFile(), "RE " + this.q.getMESSAGE_ID() + " Domain:" + this.q.getQUERY_INFO_NAME() + " Type:" + this.q.getQUERY_INFO_TYPE() + " Response_Code:" + this.q.getRESPONSE_CODE());
        //System.out.println(response);
        return response.toString();
    }

    public int getPort() {
        return this.port;
    }

    public int getTime_out() {
        return this.time_out;
    }

    public String getModo_debug() {
        return this.modo_debug;
    }

    public String getPath() {
        return this.path;
    }

    public int getNumberOfDBLines() {
        return this.numberOfDBLines;
    }

    public void setNumberOfDBLines(int numberOfDBLines) {
        this.numberOfDBLines = numberOfDBLines;
    }

    public ConfigurationFile getConfigurationFile() {
        return this.cf;
    }

    public LogFile getLogFile() {
        return this.lf;
    }
}