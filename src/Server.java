import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Server {
    private int port;
    private int time_out;
    private String modo_debug;
    private String path;
    private LogFile lf = new LogFile();
    private ConfigurationFile cf = new ConfigurationFile();
    private int numberOfDBLines = 0;
    private Cache cache = new Cache();
    private Query query = new Query();
    public DataBaseFile dbf = new DataBaseFile(this.cf);

    public Server() throws IOException {
    }

    /*
    public Server(int port, int time_out, String modo_debug, String path) throws IOException {
        this.port = port;
        this.time_out = time_out;
        this.modo_debug = modo_debug;
        this.path = path;
        this.cf.readConfigurationFile(path);
        this.lf.writeIntoLogFile(this.cf.getAllLogFile(), "ST " + this.cf.getDD() + " Port:" + this.port + " Time_Out:" + this.time_out + " Mode:" + this.modo_debug + " --- Servidor inicializado");
    }*/

    public Server(int port, int time_out, String modo_debug, String path) throws IOException {
        this.port = port;
        this.time_out = time_out;
        this.modo_debug = modo_debug;
        this.path = path;
        this.cf.readConfigurationFile(path);
        this.lf.writeIntoLogFile(this.cf.getAllLogFile(), "ST " + this.cf.getDD() + " Port:" + this.port + " Time_Out:" + this.time_out + " Mode:" + this.modo_debug + " --- Servidor inicializado");
        //Verificar se é SP (se nao existir o type_value SP é porque é um SP)
        if (this.cf.getSP() == null)
            //se for um SP, SDT ou ST tem de fazer a leitura do ficheiro de base de dados.
            this.dbf.readDataBaseFile(this.cf.getDB(), this.cache);

        //Se o SS implementar um ficheiro de dados com os rootServers, esta linha passa para fora do if
        //Tanto o SP como o SS tem de fazer a leitura do ficheiro de base de dados dos rootservers.
        this.dbf.readDataBaseFile(this.cf.getST(), this.cache);
    }

    public void fillWithData(String line) throws IOException {
        String[] splitLine = line.split(" ");
        if (line.startsWith("#") || line.isBlank() || splitLine[1].equals("DEFAULT")) return;
        this.cache.addEntryToCache(line, "SP", Timestamp.valueOf(LocalDateTime.now()));
        if (this.cache.getLines().size() < this.numberOfDBLines)
            this.lf.writeIntoLogFile(this.cf.getLogFile(), "ZT " + this.cf.getSP() + " Foram enviadas "
                    + this.cache.getLines().size() + '/' + this.numberOfDBLines + " linhas.");
        else if (this.cache.getLines().size() == this.numberOfDBLines) {
            this.lf.writeIntoLogFile(this.cf.getLogFile(), "ZT " + this.cf.getSP() + " Transferência de zona concluída com sucesso");
        } else
            this.lf.writeIntoLogFile(this.cf.getLogFile(), "EZ " + this.cf.getSP() + " Ocorreu um erro na Transferência de zona");
    }

    public String response(String query) throws IOException {
        this.lf.writeIntoLogFile(this.cf.getLogFile(), "QR " + this.query.getMESSAGE_ID() + " Domain:" + this.query.getQUERY_INFO_NAME() + " Type:" + this.query.getQUERY_INFO_TYPE() + " Response_Code:" + this.query.getRESPONSE_CODE());
        this.lf.writeIntoLogFile(this.cf.getLogFile(), "RE " + this.query.getMESSAGE_ID() + " Domain:" + this.query.getQUERY_INFO_NAME() + " Type:" + this.query.getQUERY_INFO_TYPE() + " Response_Code:" + this.query.getRESPONSE_CODE());
        return this.getQuery().response(query, this.cache);
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

    public Cache getCache() {
        return this.cache;
    }

    public Query getQuery() {
        return this.query;
    }
}