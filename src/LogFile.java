import javax.xml.crypto.Data;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LogFile {
    public boolean validaLFValues(String v) {
        return v.equals("QR") || v.equals("QE") || v.equals("RE") || v.equals("RR") || v.equals("ZT") || v.equals("EV")
                || v.equals("ER") || v.equals("EZ") || v.equals("FL") || v.equals("TO") || v.equals("SP") || v.equals("ST");
    }

    public void writeIntoLogFile(String path, String line) throws IOException {
        String date = DateTimeFormatter.ofPattern("dd:MM:yyyy.hh:mm:ss:SSS").format(LocalDateTime.now());
        try {
            File file = new File(path);
            FileWriter writer = new FileWriter(file, true);
            BufferedWriter output = new BufferedWriter(writer);
            String[] splitLine = line.split(" ");
            if (validaLFValues(splitLine[0])) {
                if (file.length() != 0) output.newLine();
                output.write(date + " " + line);
            } else {
                if (file.length() != 0) output.newLine();
                output.write(date + " " + "não foi possível escrever no ficheiro de log. O formato da linha é inválido.");
            }
            output.close();
            writer.close();
        } catch (IOException e) {
            System.out.println("IOException" + e.getMessage());
        }
    }
}