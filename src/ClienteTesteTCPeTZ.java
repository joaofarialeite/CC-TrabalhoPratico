import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteTesteTCPeTZ {
/*    static int Numerodelinhas;


    public void setNumerodelinhas(int numerodelinhas) {
        this.Numerodelinhas = numerodelinhas;
    }*/

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5555);

        SS ss =  new SS(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);
  /*      ss.fillWithData("cc.lei. SOAADMIN dns.admin.example.com. 86400");
        //ss.fillWithData("cc.lei. SOAADMIN dns.admin.example.com. 86400");
        System.out.println("SOAADMIN" + ss.getSOAADMIN());*/

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput;
            String serverResponse;
            int contador = 0;
            while ((userInput = systemIn.readLine()) != null && (contador <= 1)) {
                // envia a primeira mensagem que escrevo que sera o "example.com"
                if (contador == 0) {
                    out.println(userInput);
                    out.flush();
                    String response = in.readLine();
                    if (response != null) {
                        System.out.println(response);
                        //Numerodelinhas = Integer.parseInt(response);
                        ss.setDBlines(Integer.parseInt(response));
                    } else {
                        break;
                    }

                }
                //Envia a segunda mensagem que escrevo que sera o "ok: 5"
                if (contador == 1) {
                    //System.out.println("1221");
                    out.println(userInput);
                    out.flush();
                    socket.shutdownOutput();
                    break;
                }
                //if( (!socket.isClosed())) return;
                contador++;
            }

            //System.out.println("1221");
            //socket.shutdownInput();
            while ((serverResponse = in.readLine()) != null) {
                //System.out.println(serverResponse);
                ss.fillWithData(serverResponse);
            }

            //ss.readSSData();

            System.out.println(ss.responseQueryCliente("dnscl 10.2.2.1 example.com. MX"));
/*            int i = 0;
            for(String c : ss.getSPDataCopy()){
                System.out.println( i + c);
                i++;
            }*/

            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}