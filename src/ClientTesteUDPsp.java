import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class ClientTesteUDPsp {
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

    // CORE -> java ClientTesteUDPsp dnscl 10.2.2.1 cc.lei. MX R
    //dnscl 127.0.0.1 cc.lei. MX
    //dnscl 127.0.0.1 cc.lei. MX
    public static void main(String[] args) {
        try {
            DatagramSocket socket = new DatagramSocket();
            InetAddress address = InetAddress.getByName(args[1]);

            // aqui falta por a ler a msg dos args e claro como é obvio falta depois trocar o argumento
            //adress pelo valor que se le dos argumentos , a porta vai ser sempre a mesma , vamos usar a 5000
            //byte[] buf;

            String msg = args[0] + " " + args[1] + " " + args[2] + " " + args[3];

            //String msg = "dnscl 10.2.2.1 example.com. MX R";
            byte[] buf = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buf, buf.length, address, 5555);
            socket.send(packet);
            byte[] bytes = new byte[65535];
            DatagramPacket resposta = new DatagramPacket(bytes, bytes.length);
            socket.receive(resposta);
            System.out.println(data(bytes));
            socket.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}