import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {
        Server sp = new Server(5555, 50000, "debug", "var/dns/configFiles/configurationFile-cc-lei-sp1.txt");

        //System.out.println(sp.getCache().findIPAdresses());
        //System.out.println(sp.getCache().getLines());
        System.out.println(sp.getCache().findEntry(1, "cc.lei.", "MX"));
        System.out.println(sp.getCache().getSOAEXPIRE());
        //System.out.println(sp.getCache().findEntries(1, "cc.lei.", "MX"));
        //System.out.println(sp.getCache().getLines().get(10));
        //sp.response("dnscl 193.136.130.250 cc.lei. MX R");
        //sp.response("3874,Q,0,0,0,0;cc.lei.,MX;");


//        Server ss = new Server(5550, 50000, "debug", "var/dns/configFiles/configurationFile-cc-lei-ss1.txt");
//        System.out.println(ss.getCache().getLines());
//        ss.fillWithData("ns1 A 193.136.130.250 TTl");
//        System.out.println(ss.getCache().findIPAdresses());
//
//        Server st = new Server(5600, 50000, "debug", "var/dns/configFiles/configurationFile-st1.txt");
//        System.out.println(st.getCache().findServidoresAutoritativos());
//        System.out.println(st.getCache().findIPAdresses());
    }
}