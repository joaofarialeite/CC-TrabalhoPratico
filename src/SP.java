import java.io.IOException;

public class SP extends Server {
    public ConfigurationFile cf = new ConfigurationFile();
    public DataBaseFile dbf = new DataBaseFile(this.cf);

    public SP(int port, int time_out, String modo_debug, String path) throws IOException {
        super(port, time_out, modo_debug, path);
        this.cf.readConfigurationFile(path);
        this.dbf.readDataBaseFile(this.cf.getDB());
        super.inicializaServidor(this.dbf.getDataForSP());
    }
}