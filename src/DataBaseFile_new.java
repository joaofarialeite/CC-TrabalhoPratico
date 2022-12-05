import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBaseFile_new {
    private ConfigurationFile cf;
    private List<String> dataForSP = new ArrayList<>();
    private int lines_of_filedb = 0;

    public DataBaseFile_new(ConfigurationFile cf) {
        this.cf = cf;
    }

    public boolean validaDBValues(String v) {
        return v.equals("DEFAULT") || v.equals("SOASP") || v.equals("SOAADMIN") || v.equals("SOASERIAL") || v.equals("SOAREFRESH") || v.equals("SOARETRY")
                || v.equals("SOAEXPIRE") || v.equals("NS") || v.equals("MX") || v.equals("A") || v.equals("CNAME");
    }

    public void readDataBaseFile(String filePath) throws IOException {
        FileReader file = new FileReader(filePath);
        BufferedReader buffer = new BufferedReader(file);

        while (buffer.ready()) {
            String line = String.valueOf(buffer.readLine());
            String[] splitLine = line.split(" ");
            if (line.startsWith("#") || line.isBlank()) continue;
            if (validaDBValues(splitLine[1]) && splitLine.length >= 4 || validaDBValues(splitLine[1]) && splitLine[1].equals("DEFAULT") && splitLine.length >= 3)
                this.dataForSP.add(line);
            else {
                System.out.println("Existem linhas inválidas no ficheiro de base de dados.");
                continue;
            }
            lines_of_filedb++;
        }
        // escreve no ficheiro de log a entrada que corresponde à leitura da base de dados do servidor
        this.cf.getLf().writeIntoLogFile(this.cf.getLogFile(), "EV " + this.cf.getDD() + " db-file-read " + this.cf.getDB());
        //System.out.println("Tudo válido!");
        buffer.close();
        file.close();
    }

    public List<String> getDataForSP() {
        return this.dataForSP;
    }

    public int getLines_of_filedb() {
        return this.lines_of_filedb;
    }

    public void setLines_of_filedb(int lines_of_filedb) {
        this.lines_of_filedb = lines_of_filedb;
    }
}