import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SS extends Server {
    public ConfigurationFile cf = new ConfigurationFile();
    private List<String> SPDataCopy = new ArrayList<>();

    public SS(int port, int time_out, String modo_debug, String path) throws IOException {
        super(port, time_out, modo_debug, path);
        this.cf.readConfigurationFile(path);
    }

    public void fillWithData(String line) {
        if (line.startsWith("#") || line.isBlank()) return;
        this.SPDataCopy.add(line);
        if (this.SPDataCopy.size() == super.getNumberOfDBLines() ) {
            super.inicializaServidor(this.SPDataCopy);
        }
    }
}