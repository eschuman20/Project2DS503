import org.junit.Test;

import static org.junit.Assert.*;

public class KMeansTest {
    @Test
    public void debug() throws Exception {
        String[] input = new String[3];
        input[0] = "hdfs://localhost:9000/cs585/K.csv";
        input[1] = "hdfs://localhost:9000/cs585/P.csv";
        input[2] = "hdfs://localhost:9000/cs585/KMeans61";
        KMeans KMean = new KMeans();
        KMean.debug(input);
    }
}