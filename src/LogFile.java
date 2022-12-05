import javax.xml.crypto.Data;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogFile {
    enum validLFValues {
        QR, //foi recebida uma query do endereço indicado
        QE, //foi enviada uma query para o endereço indicado
        RE, //foi enviada uma resposta a uma query para o endereço indicado
        RR, //foi recebida uma resposta a uma query do endereço indicado
        ZT, //foi iniciado e concluido corretamente um processo de transferencia de zona
        // O endereço deve indicar o servidor na outra ponta da trasnferencia
        EV, //foi detetado um evento/atividade interna no componente
        //o endereço deve indicar 127.0.0.1
        ER, //foi recebido um PDU do endereço indicado que não foi possivel descodificar corretamente
        EZ, //foi detetado um erro num processo de transferência de zona que não foi concluida corretamente
        FL, //foi detetado um erro no funcionamento interno do componente
        TO, //foi detetado um timeout na interação com o servidor no endereço indicado
        SP, //a execução do componente foi parada
        ST  //a execução do componente foi iniciada
    }

    public boolean validLFString(String line) {
        String[] parts = line.split(" ");
        for (LogFile.validLFValues v : LogFile.validLFValues.values()) {
            if (v.name().equals(parts[0])) return true;
        }
        return false;
    }

    public void writeIntoLogFile(String path, String line) throws IOException {
        String date = DateTimeFormatter.ofPattern("dd:MM:yyyy.hh:mm:ss:SSS").format(LocalDateTime.now());
        try {
            File file = new File(path);
            if (validLFString(line)) {
                FileWriter writer = new FileWriter(file, true);
                BufferedWriter output = new BufferedWriter(writer);
                if (file.length() != 0) output.newLine();
                output.write(date + " " + line);
                output.close();
                writer.close();
            }
        } catch (IOException e) {
            System.out.println("IOException" + e.getMessage());
        }
    }
}