import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Query {
    private String QUERY;
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

    public Query() throws IOException {
        this.QUERY = "";
        this.MESSAGE_ID = 0;
        this.FLAGS = "";
        this.RESPONSE_CODE = 0;
        this.NUMBER_OF_VALUES = 0;
        this.NUMBER_OF_AUTHORITIES = 0;
        this.NUMBER_OF_EXTRA_VALUES = 0;
        this.QUERY_INFO_NAME = "";
        this.QUERY_INFO_TYPE = "";
        this.RESPONSE_VALUES = new ArrayList<>();
        this.AUTHORITIES_VALUES = new ArrayList<>();
        this.EXTRA_VALUES = new ArrayList<>();
    }

    public Query(String QUERY, int MESSAGE_ID, String FLAGS, int RESPONSE_CODE, int NUMBER_OF_VALUES, int NUMBER_OF_AUTHORITIES, int NUMBER_OF_EXTRA_VALUES,
                 String QUERY_INFO_NAME, String QUERY_INFO_TYPE, ArrayList<String> RESPONSE_VALUES, ArrayList<String> AUTHORITIES_VALUES, ArrayList<String> EXTRA_VALUES) throws IOException {
        this.QUERY = QUERY;
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