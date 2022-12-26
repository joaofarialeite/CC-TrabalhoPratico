import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClienteTesteTCPeTZ {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5555);

        Server ss = new SS(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);

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
                        ss.setNumberOfDBLines(Integer.parseInt(response));
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
            //socket.shutdownInput();
            while ((serverResponse = in.readLine()) != null) {
                //System.out.println(serverResponse);
                ss.fillWithData(serverResponse);
            }
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}