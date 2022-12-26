import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SS extends Server {

    public SS(int port, int time_out, String modo_debug, String path) throws IOException {
        super(port, time_out, modo_debug, path);
    }

    /*public void fillWithData(String line) throws IOException {
        if (line.startsWith("#") || line.isBlank()) return;
        this.SPDataCopy.add(line);
        if (this.SPDataCopy.size() < super.getNumberOfDBLines())
            super.getLogFile().writeIntoLogFile(super.getConfigurationFile().getLogFile(), "ZT " + super.getConfigurationFile().getSP() + " Foram enviadas "
                    + this.SPDataCopy.size() + '/' + super.getNumberOfDBLines() + " linhas.");
        else if (this.SPDataCopy.size() == super.getNumberOfDBLines()) {
            super.inicializaServidor(this.SPDataCopy);
            super.getLogFile().writeIntoLogFile(super.getConfigurationFile().getLogFile(), "ZT " + super.getConfigurationFile().getSP() + " Transferência de zona concluída com sucesso");
        } else
            super.getLogFile().writeIntoLogFile(super.getConfigurationFile().getLogFile(), "EZ " + super.getConfigurationFile().getSP() + " Ocorreu um erro na Transferência de zona");
    }*/
}