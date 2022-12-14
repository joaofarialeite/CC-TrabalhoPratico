import java.io.IOException;

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

    public Server() throws IOException {
    }

    public Server(int port, int time_out, String modo_debug, String path) throws IOException {
        this.port = port;
        this.time_out = time_out;
        this.modo_debug = modo_debug;
        this.path = path;
        this.cf.readConfigurationFile(path);
        this.lf.writeIntoLogFile(this.cf.getAllLogFile(), "ST " + this.cf.getDD() + " Port:" + this.port + " Time_Out:" + this.time_out + " Mode:" + this.modo_debug + " --- Servidor inicializado");
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