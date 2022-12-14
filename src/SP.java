import java.io.IOException;

public class SP extends Server {
    public DataBaseFile dbf = new DataBaseFile(super.getConfigurationFile());

    public SP(int port, int time_out, String modo_debug, String path) throws IOException {
        super(port, time_out, modo_debug, path);
        this.dbf.readDataBaseFile(super.getConfigurationFile().getDB(), super.getCache());
    }
}