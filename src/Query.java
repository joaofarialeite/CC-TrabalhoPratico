import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Query {
    private int MESSAGE_ID;
    private String FLAGS;
    private int RESPONSE_CODE;
    private int NUMBER_OF_VALUES;
    private int NUMBER_OF_AUTHORITIES;
    private int NUMBER_OF_EXTRA_VALUES;
    private String QUERY_INFO_NAME;
    private String QUERY_INFO_TYPE;
    private List<String> RESPONSE_VALUES;
    private List<String> AUTHORITIES_VALUES;
    private List<String> EXTRA_VALUES;
    private LogFile lf = new LogFile();

    public Query() {
    }

    public Query(int MESSAGE_ID, String FLAGS, int RESPONSE_CODE, int NUMBER_OF_VALUES, int NUMBER_OF_AUTHORITIES, int NUMBER_OF_EXTRA_VALUES,
                 String QUERY_INFO_NAME, String QUERY_INFO_TYPE, ArrayList<String> RESPONSE_VALUES, ArrayList<String> AUTHORITIES_VALUES, ArrayList<String> EXTRA_VALUES) throws IOException {
        this.MESSAGE_ID = MESSAGE_ID;
        this.FLAGS = FLAGS;
        this.RESPONSE_CODE = RESPONSE_CODE;
        this.NUMBER_OF_VALUES = NUMBER_OF_VALUES;
        this.NUMBER_OF_AUTHORITIES = NUMBER_OF_AUTHORITIES;
        this.NUMBER_OF_EXTRA_VALUES = NUMBER_OF_EXTRA_VALUES;
        this.QUERY_INFO_NAME = QUERY_INFO_NAME;
        this.QUERY_INFO_TYPE = QUERY_INFO_TYPE;
        this.RESPONSE_VALUES = new ArrayList<>(RESPONSE_VALUES);
        this.AUTHORITIES_VALUES = new ArrayList<>(AUTHORITIES_VALUES);
        this.EXTRA_VALUES = new ArrayList<>(EXTRA_VALUES);
    }

    public String response(String query, Cache cache) throws IOException {
        List<String> servidoresAutoritativosAUX = new ArrayList<>();
        List<String> servidoresEmailAUX = new ArrayList<>();
        List<String> IPAdressesAUX = new ArrayList<>();
        boolean existeDomain = false;
        int i = 0;

        if (query.contains(",")) {
            String[] splitQuery = query.split(",");
            this.setMESSAGE_ID(Integer.parseInt(splitQuery[0]));
            this.setFLAGS(splitQuery[1]);
            this.setQUERY_INFO_TYPE(splitQuery[6].substring(0, splitQuery[6].length() - 1));
            String[] splitByComma = splitQuery[5].split(";");
            this.setQUERY_INFO_NAME(splitByComma[1]);
        } else {
            String[] splitQuery = query.split(" ");
            this.setMESSAGE_ID(ThreadLocalRandom.current().nextInt(1, 65535));
            this.setQUERY_INFO_NAME(splitQuery[2]);
            this.setQUERY_INFO_TYPE(splitQuery[3]);
            this.setFLAGS("R+A");
        }

        for (String line : cache.getLines()) {
            String[] splitLine = line.split(" ");
            String newLine = splitLine[0] + " " + splitLine[1] + " " + splitLine[2] + " " + splitLine[3] + " " + splitLine[4];

            if (splitLine[0].equals(this.getQUERY_INFO_NAME())) {
                existeDomain = true;
                if (splitLine[1].equals("MX") && splitLine[9].equals("VALID"))
                    servidoresEmailAUX.add(newLine);
                else if (splitLine[1].equals("NS") && splitLine[9].equals("VALID"))
                    servidoresAutoritativosAUX.add(newLine);
            } else if (splitLine[1].equals("A") && splitLine[9].equals("VALID")) {
                for (String servidorEmail : servidoresEmailAUX)
                    if (servidorEmail.contains(splitLine[0]))
                        IPAdressesAUX.add(newLine);
                for (String servidorAutoritativo : servidoresAutoritativosAUX)
                    if (servidorAutoritativo.contains(splitLine[0]))
                        IPAdressesAUX.add(newLine);
            }
        }


        if (existeDomain && QUERY_INFO_TYPE.equals("MX") && servidoresEmailAUX.size() == 0) {
            servidoresAutoritativosAUX.clear();
            IPAdressesAUX.clear();
            setRESPONSE_CODE(1);
            for (String line : cache.getLines()) {
                String[] splitLine = line.split(" ");
                String newLine = splitLine[0] + " " + splitLine[1] + " " + splitLine[2] + " " + splitLine[3] + " " + splitLine[4];
                if (splitLine[0].contains(QUERY_INFO_NAME) && splitLine[1].equals("NS") && splitLine[9].equals("VALID")) {
                    servidoresAutoritativosAUX.add(newLine);
                } else if (splitLine[0].contains(QUERY_INFO_NAME) && splitLine[1].equals("A") && splitLine[9].equals("VALID"))
                    IPAdressesAUX.add(newLine);
            }
        } else if (existeDomain && QUERY_INFO_TYPE.equals("NS") && servidoresEmailAUX.size() == 0) {
            IPAdressesAUX.clear();
            setRESPONSE_CODE(1);
            for (String line : cache.getLines()) {
                String[] splitLine = line.split(" ");
                String newLine = splitLine[0] + " " + splitLine[1] + " " + splitLine[2] + " " + splitLine[3] + " " + splitLine[4];
                if (splitLine[0].contains(QUERY_INFO_NAME) && splitLine[1].equals("A") && splitLine[9].equals("VALID"))
                    IPAdressesAUX.add(newLine);
            }
        } else if (existeDomain && QUERY_INFO_TYPE.equals("A") && servidoresEmailAUX.size() == 0) {
            servidoresAutoritativosAUX.clear();
            setRESPONSE_CODE(1);
            for (String line : cache.getLines()) {
                String[] splitLine = line.split(" ");
                String newLine = splitLine[0] + " " + splitLine[1] + " " + splitLine[2] + " " + splitLine[3] + " " + splitLine[4];
                if (splitLine[0].contains(QUERY_INFO_NAME) && splitLine[1].equals("NS") && splitLine[9].equals("VALID"))
                    servidoresAutoritativosAUX.add(newLine);
            }
        } else if (!existeDomain) {
            setRESPONSE_CODE(2);
            for (String line : cache.getLines()) {
                String[] splitLine = line.split(" ");
                String newLine = splitLine[0] + " " + splitLine[1] + " " + splitLine[2] + " " + splitLine[3] + " " + splitLine[4];
                if ((splitLine[5].equals("SP") || splitLine[5].equals("FILE")) && splitLine[9].equals("VALID")) {
                    if (splitLine[1].equals("NS"))
                        servidoresAutoritativosAUX.add(newLine);
                    else if (splitLine[1].equals("A"))
                        IPAdressesAUX.add(newLine);
                }
            }
        }

        /*
        adiciona aos response_values os servidoresEmail que pertencem ao domínio que é enviado na query
         */
        this.setRESPONSE_VALUES(servidoresEmailAUX);
        this.setNUMBER_OF_VALUES(this.getRESPONSE_VALUES().size());

        /*
        adiciona aos authorities_values os servidoresEmail que pertencem ao domínio que é enviado na query
         */
        this.setAUTHORITIES_VALUES(servidoresAutoritativosAUX);
        this.setNUMBER_OF_AUTHORITIES(this.getAUTHORITIES_VALUES().size());

        /*
        adiciona aos extra_values os endereçosIP que pertencem ao domínio que é enviado na query
         */
        this.setEXTRA_VALUES(IPAdressesAUX);
        this.setNUMBER_OF_EXTRA_VALUES(this.getEXTRA_VALUES().size());

        StringBuilder response = new StringBuilder();
        response.append(this.getMESSAGE_ID()).append(',').append(this.getFLAGS()).append(',').append(this.getRESPONSE_CODE()).append(',');

        /*
        constroi String dos servidoresEmail
         */
        StringBuilder stringMX = new StringBuilder();
        StringBuilder firstMX = new StringBuilder();
        stringMX.append(this.getNUMBER_OF_VALUES());

        for (String line : this.getRESPONSE_VALUES()) {
            if (i < this.getNUMBER_OF_VALUES() - 1) {
                firstMX.append(line).append(',').append('\n');
                i++;
            } else firstMX.append(line).append(';').append('\n');
            ;
        }
        i = 0;

        /*
        constroi String dos servidoresAutoritativos
         */
        StringBuilder stringNS = new StringBuilder();
        StringBuilder firstNS = new StringBuilder();
        stringNS.append(this.getNUMBER_OF_AUTHORITIES());

        for (String line : this.getAUTHORITIES_VALUES()) {
            if (i < this.getNUMBER_OF_AUTHORITIES() - 1) {
                firstNS.append(line).append(',').append('\n');
                i++;
            } else firstNS.append(line).append(';');
        }
        firstNS.append('\n');
        i = 0;

        /*
        constroi String dos servidoresAutoritativos
         */
        StringBuilder stringA = new StringBuilder();
        StringBuilder firstA = new StringBuilder();
        stringA.append(this.getNUMBER_OF_EXTRA_VALUES());

        for (String line : this.getEXTRA_VALUES()) {
            if (i < this.getNUMBER_OF_EXTRA_VALUES() - 1) {
                firstA.append(line).append(',').append('\n');
                i++;
            } else firstA.append(line).append(';').append('\n');
        }
        i = 0;

        switch (this.getQUERY_INFO_TYPE()) {
            case "MX":
                response.append(stringMX).append(',').append(stringNS).append(',').append(stringA).append(';').append(this.getQUERY_INFO_NAME())
                        .append(',').append(this.getQUERY_INFO_TYPE()).append(';').append('\n').append(firstMX).append(firstNS).append(firstA);
                break;
            case "NS":
                response.append(stringNS).append(',').append(stringMX).append(',').append(stringA).append(';').append(this.getQUERY_INFO_NAME())
                        .append(',').append(this.getQUERY_INFO_TYPE()).append(';').append('\n').append(firstNS).append(firstMX).append(firstA);
                break;
            case "A":
                response.append(stringA).append(',').append(stringNS).append(',').append(stringMX).append(';').append(this.getQUERY_INFO_NAME())
                        .append(',').append(this.getQUERY_INFO_TYPE()).append(';').append('\n').append(firstA).append(firstNS).append(firstMX);
                break;
        }
        System.out.println(response);
        return response.toString();
    }

    public int getMESSAGE_ID() {
        return this.MESSAGE_ID;
    }

    public void setMESSAGE_ID(int MESSAGE_ID) {
        this.MESSAGE_ID = MESSAGE_ID;
    }

    public String getFLAGS() {
        return this.FLAGS;
    }

    public void setFLAGS(String FLAGS) {
        this.FLAGS = FLAGS;
    }

    public int getRESPONSE_CODE() {
        return this.RESPONSE_CODE;
    }

    public void setRESPONSE_CODE(int RESPONSE_CODE) {
        this.RESPONSE_CODE = RESPONSE_CODE;
    }

    public int getNUMBER_OF_VALUES() {
        return this.NUMBER_OF_VALUES;
    }

    public void setNUMBER_OF_VALUES(int NUMBER_OF_VALUES) {
        this.NUMBER_OF_VALUES = NUMBER_OF_VALUES;
    }

    public int getNUMBER_OF_AUTHORITIES() {
        return this.NUMBER_OF_AUTHORITIES;
    }

    public void setNUMBER_OF_AUTHORITIES(int NUMBER_OF_AUTHORITIES) {
        this.NUMBER_OF_AUTHORITIES = NUMBER_OF_AUTHORITIES;
    }

    public int getNUMBER_OF_EXTRA_VALUES() {
        return this.NUMBER_OF_EXTRA_VALUES;
    }

    public void setNUMBER_OF_EXTRA_VALUES(int NUMBER_OF_EXTRA_VALUES) {
        this.NUMBER_OF_EXTRA_VALUES = NUMBER_OF_EXTRA_VALUES;
    }

    public String getQUERY_INFO_NAME() {
        return this.QUERY_INFO_NAME;
    }

    public void setQUERY_INFO_NAME(String QUERY_INFO_NAME) {
        this.QUERY_INFO_NAME = QUERY_INFO_NAME;
    }

    public String getQUERY_INFO_TYPE() {
        return this.QUERY_INFO_TYPE;
    }

    public void setQUERY_INFO_TYPE(String QUERY_INFO_TYPE) {
        this.QUERY_INFO_TYPE = QUERY_INFO_TYPE;
    }

    public List<String> getRESPONSE_VALUES() {
        return new ArrayList<>(this.RESPONSE_VALUES);
    }

    public void setRESPONSE_VALUES(List<String> RESPONSE_VALUES) {
        this.RESPONSE_VALUES = new ArrayList<>(RESPONSE_VALUES);
    }

    public List<String> getAUTHORITIES_VALUES() {
        return new ArrayList<>(this.AUTHORITIES_VALUES);
    }

    public void setAUTHORITIES_VALUES(List<String> AUTHORITIES_VALUES) {
        this.AUTHORITIES_VALUES = new ArrayList<>(AUTHORITIES_VALUES);
    }

    public List<String> getEXTRA_VALUES() {
        return new ArrayList<>(this.EXTRA_VALUES);
    }

    public void setEXTRA_VALUES(List<String> EXTRA_VALUES) {
        this.EXTRA_VALUES = new ArrayList<>(EXTRA_VALUES);
    }
}