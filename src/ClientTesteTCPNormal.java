import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientTesteTCPNormal {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 5555);

        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            BufferedReader systemIn = new BufferedReader(new InputStreamReader(System.in));

            String userInput;
            String serverResponse;
            int contador = 0;

            if((userInput = systemIn.readLine()) != null){
                out.println(userInput);
                out.flush();

                String response = in.readLine();
                while (response != null){
                    System.out.println(response);
                    response = in.readLine();
                }
            }


            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}