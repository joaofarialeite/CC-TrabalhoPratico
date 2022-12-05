import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;


class ServerWorkerUDP implements Runnable {
    private DatagramPacket packet;
    //private byte[] buf = new byte[65535];
    private DatagramSocket socket;

    private SP sp;

/*    public ServerWorkerUDP(DatagramSocket socket, DatagramPacket packet,int port, int timeout, String debug, String path ) throws IOException {
        this.socket = socket;
        this.packet = packet;
        this.sp = new Joel.Server(port, timeout, debug, path);
    }*/

    public ServerWorkerUDP(DatagramSocket socket, DatagramPacket packet, SP sp) throws IOException {
        this.socket = socket;
        this.packet = packet;
        this.sp = sp;
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
        //DatagramPacket packet = new DatagramPacket(buf,buf.length);
        //DatagramPacket packet = new DatagramPacket(buf,buf.length);
        //DatagramSocket s = new DatagramSocket(null);  s.bind(new InetSocketAddress(8888))
        try {
            //this.socket.receive(packet);
            //SP sp = new SP(5555, 0, "debug", "var/dns/configFiles/configurationFile.txt");
            //Joel.Server sp = new Joel.Server(1);
            //Resposta
            InetAddress address = packet.getAddress();
            int port = packet.getPort();
            //byte[] bytes = ("ola").getBytes();
            //byte[] bytes = (sp.responseQueryCliente("dnscl 10.2.2.1 example.com. MX")).getBytes();
            byte[] bytes = (sp.responseQueryCliente(data(packet.getData()))).getBytes();
            DatagramPacket resposta = new DatagramPacket(bytes, bytes.length, address, port);
            //DatagramSocket novoSocket = new DatagramSocket(port);
            socket.send(resposta);
            //System.out.println(data(buf));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

class ServerWorkerTCP implements Runnable {
    //private final Register register;
    private Socket socket;
    private ArrayList<String> linhasdodb;
    private int linhas_enviadas = 0;
    private SP sp;

    public int numerodelinhas() {
        return this.sp.dbf.getDBLines();
    }


    // vai ao cf e verifica o dominio
    public boolean confirmadominio(String line) {
        if (this.sp.getConfigurationFile().getDomain().equals(line)) return true;
        else return false;
    }

    public void enviarficheiro(BufferedReader in, PrintWriter out) throws IOException {
        FileReader file = new FileReader(sp.getConfigurationFile().getDB());
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

    public static void printMap(Map mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry) it.next();
            System.out.println(pairs.getKey() + " = " + pairs.getValue());
            it.remove();
        }
    }

    public ServerWorkerTCP(Socket socket, SP sp) throws IOException {
        this.socket = socket;
        this.sp = sp;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream());
            String line;
            int contador = 0;
            // Confirma se o SS esta no seu CF ou seja se é
            if ((sp.getConfigurationFile().getServerPortAndAddress().containsKey(socket.getInetAddress().getHostAddress()))) {
                //PRIMEIRO PASSO
                //RECEBE O DOMINIO CONFIRMA E lanca o numero de linhas
                while (!(socket.isClosed())) {
                    if ((line = in.readLine()) != null) {
                        if (confirmadominio(line)) {
                            out.println(numerodelinhas());
                            out.flush();
                            contador++;
                        } else {
                            socket.shutdownOutput();
                            socket.shutdownInput();
                            socket.close();
                        }
                        //System.out.println(line);
                    }
                    ;
                    //System.out.println(line);
                    if ((line = in.readLine()) != null) {
                        if (Integer.parseInt(line) == numerodelinhas()) {
                            //out.println("Tudo certo, vou comecar a transferir");
                            //System.out.println("Tudo certo, vou comecar a transferir");
                            //out.flush();
                        } else {
                            socket.close();
                            //break;
                        }
                    }
                    enviarficheiro(in, out);

                    Thread.sleep(200);

                    if ((line = in.readLine()) == null) {
                        socket.shutdownOutput();
                        socket.shutdownInput();
                        socket.close();
                    }
                    if (!socket.isClosed()) {
                        socket.shutdownOutput();
                        socket.shutdownInput();
                        socket.close();
                    }
                }
            } else {
                // caso nao seja uma conexão para transferencia de zona funciona como um echo servidor responde a queries por tcp
                if ((line = in.readLine()) != null) {
                    out.println(sp.responseQueryCliente(line));
                    out.flush();
                }
                socket.shutdownOutput();
                socket.shutdownInput();
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}


public class ServerTestarSP {
    // E METER OS FICHEIROS CORE E DB TUDO NA MESMA PASTA AO TESTAR NO CORE (OS CAMINHOS LA DAO ERROS)

    //CORE -> java ServerTestarSP 5555 12345 bash configurationFile-cc-lei-sp.txt
    //PC -> 5555 12345 true var/dns/configFiles/configurationFile-cc-lei-sp.txt
    //porta , timeout ,debug ,path
    public static void main(String[] args) throws IOException {
        SP sp = new SP(Integer.parseInt(args[0]), Integer.parseInt(args[1]), args[2], args[3]);

        new Thread(() -> {
            try (ServerSocket ssTCP = new ServerSocket(Integer.parseInt(args[0]))) {
                while (true) {
                    Socket socketTCP = ssTCP.accept();
                    //Thread workerTCP = new Thread(new ServerWorkerTCP(socketTCP, Integer.parseInt(args[0]),Integer.parseInt(args[1]),(args[2]), args[3]));
                    Thread workerTCP = new Thread(new ServerWorkerTCP(socketTCP, sp));
                    workerTCP.start();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(() -> {
            try (DatagramSocket socketUDP = new DatagramSocket(Integer.parseInt(args[0]))) {
                byte[] buf = new byte[65535];
                while (true) {
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    socketUDP.receive(packet);
                    //Thread workerUDP = new Thread(new ServerWorkerUDP(socketUDP, packet, Integer.parseInt(args[0]),Integer.parseInt(args[1]),(args[2]), args[3]));
                    Thread workerUDP = new Thread(new ServerWorkerUDP(socketUDP, packet, sp));
                    workerUDP.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        //sp.getLogFile().writeIntoLogFile(sp.getConfigurationFile().getAllLogFile(), "ST " + sp.getConfigurationFile().getDD() + " Port:" + args[0] + " Time_Out:" + args[1] + " Mode:" + args[2] + " --- Servidor desligado");
    }

}