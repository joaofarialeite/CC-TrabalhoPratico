import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Server sp = new SP(5555, 50000, "debug", "var/dns/configFiles/configurationFile.txt");
        //System.out.println(sp.getCache().getLines());
        sp.response("dnscl 193.136.130.250 example.com. MX R");
        //sp.response("3874,Q,0,0,0,0;example.com.,NS;");
        //Server ss = new SS(5555, 50000, "debug", "var/dns/configFiles/configurationFile.txt");

        /*SS ss = new SS(5555, 50000, "debug", "var/dns/configFiles/configurationFile.txt");
        ss.fillWithData("ns1 A 193.136.130.250 TTL");
        ss.inicializaServidor(ss.getCache().getLines());
        System.out.println(ss.getIPAdressesAUX());*/
    }
}