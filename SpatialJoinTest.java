import org.junit.Test;

import static org.junit.Assert.*;

public class SpatialJoinTest {
    @Test
    public void debug() throws Exception {
        String[] input = new String[4];
        input[0] = "100,100,120,120"; //window
        input[1] = "hdfs://localhost:9000/cs585/P.csv";
        input[2] = "hdfs://localhost:9000/cs585/R.csv";
        input[3] = "hdfs://localhost:9000/cs585/SpatialJoin27.csv";
        SpatialJoin SJ = new SpatialJoin(input[0]);
        SJ.debug(input);
    }
}