import java.io.*;
import java.net.*;


class SSWorkerUDP implements Runnable {
    private DatagramPacket packet;

    private DatagramSocket socket;

    private SS ss;



    public SSWorkerUDP(DatagramSocket socket,DatagramPacket packet, SS ss) throws IOException {
        this.socket = socket;
        this.packet = packet;
        this.ss = ss;
    }

    public static String data(byte[] a) {
        if (a == null) return null;
        StringBuilder ret = new StringBuilder();
        int i = 0;
        while (a[i] != 0) {
            ret.append((char) a[i]);
            i++;
        }
        return ret.toString();
    }

    public void run() {

        try {

            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            byte[] bytes = (ss.responseQueryCliente(data(packet.getData()))).getBytes();
            DatagramPacket resposta = new DatagramPacket(bytes, bytes.length, address, port);

            socket.send(resposta);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class SSWorkerTCP implements Runnable {


    private Socket socket;
    private SS ss;


    public void enviarficheiro(BufferedReader in, PrintWriter out) throws IOException {

        FileReader file = new FileReader(ss.cf.getDB());
        BufferedReader buffer = new BufferedReader(file);
        String line;
        if (((line = in.readLine()) == null)) {
            while (buffer.ready()) {
                String linhadb = String.valueOf(buffer.readLine());
                out.println(linhadb);
                out.flush();
            }
        }
    }


    public SSWorkerTCP(Socket socket,SS ss) throws IOException {
        this.socket = socket;
        this.ss = ss;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());

            String line;

            //Na proxima fase ira ser implementado aqui outras coisas, se a base de dados
            //ainda esta atualizada ou não , neste momento para não estar vazio
            //responde a queries também por tcp.

            if ((line = in.readLine()) != null) {
                out.println(ss.responseQueryCliente(line));
                out.flush();
            }

            socket.shutdownOutput();
            socket.shutdownInput();
            socket.close();


            } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}


class SSWorkerTDZ implements Runnable {


    private Socket socket;
    private SS ss;


    public SSWorkerTDZ(Socket socket, SS ss) throws IOException {
        this.socket = socket;
        this.ss = ss;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());


            String serverResponse;
            int contador = 0;
            while ((contador <= 1)) {
                if (contador == 0) {
                    out.println(ss.cf.getDomain());
                    out.flush();
                    String response = in.readLine();
                    if (response != null) {
                        ss.setDBlines(Integer.parseInt(response));
                    } else {
                        break;
                    }

                }
                //Envia a segunda mensagem que escrevo que sera o "ok: 5"
                if (contador == 1) {

                    if(ss.getDBlines() <= 65535) { // tal como é pedido no relatorio
                        out.println(ss.getDBlines());
                        out.flush();
                        socket.shutdownOutput();
                        break;
                    }
                    else{
                        break;
                    }
                }
                contador++;
            }



            while ((serverResponse = in.readLine()) != null) {
                ss.fillWithData(serverResponse);
            }


            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


public class ServerTestarSS {

    // NAO ESQUECER DE MUDAR O CONFIGURATIONFILE PARA O SP PASSAR A SER O SERVIDOR1 E NAO O PCWINDOWS
    // E METER OS FICHEIROS CORE E DB TUDO NA MESMA PASTA AO TESTAR NO CORE (OS CAMINHOS LA DAO ERROS)

    //CORE -> java ServerTestarSS 5550 12345 bash configurationFile-cc-lei-ss.txt
    //PC -> 5550 12345 true var/dns/configFiles/configurationFile-cc-lei-ss.txt
    //porta , timeout ,debug ,path
    public static void main(String[] args) throws IOException {


        SS ss = new SS(Integer.parseInt(args[0]),Integer.parseInt(args[1]),(args[2]), args[3]);


        try {
            //Socket socketTCPTZ = new Socket("localhost", ss.cf.getPortSP()); // Aqui no core da erro
            Socket socketTCPTZ = new Socket("localhost", 5555);
            Thread workerTCPTZ = new Thread(new SSWorkerTDZ(socketTCPTZ, ss));
            workerTCPTZ.start();
            workerTCPTZ.join();
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // PARA RECEBER COMUNICACOES TCP
        new Thread(() -> {
            try (ServerSocket ssTCP = new ServerSocket(Integer.parseInt(args[0]))) {
                while (true) {
                    Socket socketTCP = ssTCP.accept();
                    Thread workerTCP = new Thread(new SSWorkerTCP(socketTCP, ss));
                    workerTCP.start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        // PARRA RECEBER MENSANGENS UDP
        new Thread(() -> {
            try (DatagramSocket socketUDP = new DatagramSocket(Integer.parseInt(args[0]))) {
                byte[] buf = new byte[65535];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socketUDP.receive(packet);
                    Thread workerUDP = new Thread(new SSWorkerUDP(socketUDP, packet, ss));
                    workerUDP.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}

