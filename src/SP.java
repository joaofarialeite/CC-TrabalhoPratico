import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class SP {
    private int port;
    private int time_out;
    private String modoDebug;
    private String path;
    public ConfigurationFile cf = new ConfigurationFile();
    public DataBaseFile dbf = new DataBaseFile(this.cf);
    private LogFile lf = new LogFile();
    private Query q = new Query();


    public SP(int port, int time_out, String modo_debug, String path) throws IOException {
        this.port = port;
        this.time_out = time_out;
        this.modoDebug = modo_debug;
        this.path = path;
        this.cf.readConfigurationFile(this.path);
        this.dbf.readDataBaseFile(this.cf.getDB());
        //Server.execute(port);

        // escreve no ficheiro de log a entrada que indica que o componente foi iniciado
        this.lf.writeIntoLogFile(this.cf.getLogFile(), "ST".concat(" ").concat(this.cf.getDD()).concat(" ").concat(String.valueOf(this.port)).concat(" ").concat(String.valueOf(this.time_out)).concat(" ").concat(String.valueOf(this.modoDebug)));
    }

    public String responseQueryCliente(String query) throws IOException {
        String response = null;
        //System.out.println("OLA");

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

            this.q.setRESPONSE_VALUES(this.dbf.getServidoresEmail().get(this.q.getQUERY_INFO_NAME()));
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

            this.q.setAUTHORITIES_VALUES(this.dbf.getServidoresAutoritativos().get(this.q.getQUERY_INFO_NAME()));
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
            for (String a : this.dbf.getIPAdressesAUX())
                for (String b : this.dbf.getServidoresAutoritativos().get(this.q.getQUERY_INFO_NAME())) {
                    String[] splitAddress = a.split(" ");
                    if (b.contains(splitAddress[0])) aux.add(a);
                }

            // verifica nos servidores de email se faz algum match com um endereço IP
            for (String a : this.dbf.getIPAdressesAUX())
                for (String b : this.dbf.getServidoresEmail().get(this.q.getQUERY_INFO_NAME())) {
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
                case "MX" : response = string.append(stringMX.append(',').append(stringNS.append(',')
                        .append(stringA.append(';').append(this.q.getQUERY_INFO_NAME()).append(',').append(this.q.getQUERY_INFO_TYPE())
                                .append(';').append('\n').append(firstMX.append(firstNS.append(firstA.append(';'))))))).toString();
                    break;
                case "NS" : response = string.append(stringNS.append(',').append(stringMX.append(',')
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
                //System.out.println(this.cf.getDomain() + splitAgain[1]);
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
                //System.out.println(this.cf.getDomain()+ "." + splitQuery2[2]);
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

