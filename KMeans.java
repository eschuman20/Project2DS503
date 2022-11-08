import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class KMeans {

    //number of centroids that did not change during the iteration
    static Integer kDidntChange = 0;

    //hashmap of the 30 centroids
    static Map<IntWritable, Text> centroids;

    static {
        try {
            centroids = getCentroids("hdfs://localhost:9000/cs585/K.csv");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

//create the hashmap of the centroids using the csv stored in hdfs
    public static Map<IntWritable, Text> getCentroids(String hdfsPath) throws IOException {
        Map<IntWritable, Text> map = new HashMap<>();

        Path path = new Path(hdfsPath);
        Configuration configuration = new Configuration();
        FileSystem fileSystem = path.getFileSystem(configuration);
        FSDataInputStream fsDataInputStream = fileSystem.open(path);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fsDataInputStream));

        String line;
        int centroid_id = 1;
        while ((line = bufferedReader.readLine()) != null) {
            String[] split = line.split(",");
            String xVal = split[0].replace("\"", ""); //remove quotes
            int x = Integer.parseInt(xVal);
            String yVal = split[1].replace("\"", ""); //remove quotes
            int y = Integer.parseInt(yVal);
            map.put(new IntWritable(centroid_id), new Text(x + "," + y));
            centroid_id++;
        }
        bufferedReader.close();
        fsDataInputStream.close();
        fileSystem.close();
        return map;
    }

    public static class KMeansMapper
            extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            FileSplit fileSplit = (FileSplit) context.getInputSplit();
            String filePath = fileSplit.getPath().getName();

            String line = value.toString();
            String[] split = line.split(",");
            if (filePath.contains("P.csv")) {                               //if from Points csv
                String x = split[0].replace("\"", "");
                String y = split[1].replace("\"", "");
                Text centroid_id = new Text();
                double min_distance = Double.POSITIVE_INFINITY;
                double distance;
                String id = "";
                for (IntWritable aKey : centroids.keySet()) {               //go through each centroid and find closest one
                    String[] centroid_coordinates = centroids.get(aKey).toString().split(",");
                    double x_centroid = Double.parseDouble(centroid_coordinates[0]);
                    double y_centroid = Double.parseDouble(centroid_coordinates[1]);
                    distance = Math.sqrt(Math.pow((Double.parseDouble(x) - x_centroid), 2) + Math.pow((Double.parseDouble(y) - y_centroid), 2));
                    if (distance < min_distance) {               //if centroid is minimum distance to point
                        min_distance = distance;
                        id = aKey.toString();                  //set the centroid id to that point
                    }
                }
                centroid_id.set(id);
                context.write(centroid_id, value);  //centroid ID corresponding to coordinate pair for point
            }
        }
    }

    //Need to fix this...not working correctly right now
//    public static class KMeansCombiner
//            extends Reducer<Text, Text, Text, Text> {
//        @Override
//        protected void reduce(Text key, Iterable<Text> values, Context context)
//                throws IOException, InterruptedException {
//            System.out.println("Combine");
//            Text partialSumsCount = new Text();
//            double x_sum = 0.0;
//            double y_sum = 0.0;
//            int count = 0;
//            for (Text val : values) {
//                String[] pointCoordinates = val.toString().split(",");
//                String x = pointCoordinates[0].replace("\"", "");
//                String y = pointCoordinates[1].replace("\"", "");
//                x_sum += Double.parseDouble(x);
//                y_sum += Double.parseDouble(y);
//                count ++;
//            }
//            partialSumsCount.set(x_sum + "," + y_sum + "," + count);
//            System.out.println(partialSumsCount);
//            context.write(key, partialSumsCount);
//        }
//    }
    public static class KMeansReducer
            extends Reducer<Text, Text, Text, NullWritable> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            System.out.println("Reduce");
            Text newCentroid = new Text();
            double x_sum = 0.0;
            double y_sum = 0.0;
            int count = 0;
            for (Text val : values) {      //go through each of the coordinates of the closest points to each centroid
                String[] partialSumsCount = val.toString().split(",");
                String x = partialSumsCount[0].replace("\"", "");
                String y = partialSumsCount[1].replace("\"", "");
                //String c = partialSumsCount[2].replace("\"", "");
                x_sum += Double.parseDouble(x);
                y_sum += Double.parseDouble(y);
                //count += Double.parseDouble(c);
                count ++;
            }
            System.out.println(x_sum + "," + y_sum + "," + count);
            String updatedCentroid =  x_sum/count + ", " + y_sum/count;  //takes average of x and y coordinates to make new center
            newCentroid.set(updatedCentroid);
            context.write(newCentroid, NullWritable.get());             //write new centroids position

            IntWritable centroidID = new IntWritable(Integer.parseInt(key.toString()));
            String[] oldCentroidCoordinates = centroids.get(centroidID).toString().split(",");
            double xOldCentroid = Double.parseDouble(oldCentroidCoordinates[0]);   //old coordinates of centroid
            double yOldCentroid = Double.parseDouble(oldCentroidCoordinates[1]);

            double updatedX = x_sum/count;       //new coordinates of centroid
            double updatedY = y_sum/count;

            if ((xOldCentroid == updatedX) && (yOldCentroid == updatedY)){      //see if centroids positions are still changing
                kDidntChange ++;
            }
            System.out.println(oldCentroidCoordinates[0] + ", " + oldCentroidCoordinates[1] + ", " + newCentroid);
            centroids.replace(centroidID,newCentroid);             //update hashmap to reflect new centroid position
        }
    }

    public void debug(String[] args) throws Exception {
        Configuration conf = new Configuration();
        for(int i = 1; i <= 6; i++){
            if (kDidntChange < 30){
                Job job = Job.getInstance(conf, "KMeans");
                job.setJarByClass(KMeans.class);
                job.setMapperClass(KMeansMapper.class);
                //job.setCombinerClass(KMeansCombiner.class);
                job.setReducerClass(KMeansReducer.class);
                job.setMapOutputKeyClass(Text.class);
                job.setMapOutputValueClass(Text.class);
                job.setOutputKeyClass(Text.class);
                job.setOutputValueClass(NullWritable.class);
                FileInputFormat.addInputPath(job, new Path(args[1]));
                FileOutputFormat.setOutputPath(job, new Path(args[2] + i + ".csv"));
                job.waitForCompletion(true);
            }
            System.out.println(kDidntChange + ", " + i);
            kDidntChange = 0;
        }
    }
}
