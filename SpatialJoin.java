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
import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;

import java.io.IOException;
import java.util.*;

public class SpatialJoin {

    static Integer r = 1;
    static String window = "";

    public SpatialJoin(String w) {
        if (!w.isEmpty()) {
            window = w;
        }
    }

    public static class SpatialJoinMapper
            extends Mapper<LongWritable, Text, Text, Text> {
        @Override
        protected void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            FileSplit fileSplit = (FileSplit) context.getInputSplit(); //split file so we can see which one it is
            String filePath = fileSplit.getPath().getName(); //get the name of the filepath

            if (filePath.contains("P.csv")) {   //if p data
                String line = value.toString();
                String[] pieces = line.split(",");
                String x = pieces[0].replace("\"", ""); //remove quotes
                String y = pieces[1].replace("\"", ""); //remove quotes
                if (!window.isEmpty()){
                    String[] filterWindow = window.split(",");
                    String wX = filterWindow[0];
                    String wY = filterWindow[1];
                    String wX2 = filterWindow[2];
                    String wY2 = filterWindow[3];
                    if ((Integer.parseInt(x) >= Integer.parseInt(wX)) && (Integer.parseInt(x) <= Integer.parseInt(wX2))
                            && (Integer.parseInt(y) >= Integer.parseInt(wY)) && (Integer.parseInt(y) <= Integer.parseInt(wY2))){
                        context.write(new Text(x), new Text("point" + y));
                    }
                }
                else{
                    context.write(new Text(x), new Text("point" + y));
                    System.out.println("Mapped Point");
                }

            }
            else if (filePath.contains("R.csv")) {  //if rectangle data
                String line = value.toString();
                String[] pieces = line.split(",");
                String x = pieces[0].replace("\"", "");
                String y = pieces[1].replace("\"", "");
                String h = pieces[2].replace("\"", "");
                String w = pieces[3].replace("\"", "");
                Integer x1 = Integer.parseInt(x);
                Integer w1 = Integer.parseInt(w);
                Integer y1 = Integer.parseInt(y);
                Integer h1 = Integer.parseInt(h);
                if (!window.isEmpty()) {
                    String[] filterWindow = window.split(",");
                    String wX = filterWindow[0];
                    String wY = filterWindow[1];
                    String wX2 = filterWindow[2];
                    String wY2 = filterWindow[3];
                    for (Integer i = x1; i <= (x1 + w1); i++) {
                            if (((i >= Integer.parseInt(wX)) && (i <= Integer.parseInt(wX2)))      //if i,j is within the window...write that point with that rectangle # as the value
                                    && (((y1 >= Integer.parseInt(wY)) && (y1 <= Integer.parseInt(wY2))) ||
                                    ((y1 + h1 >= Integer.parseInt(wY)) && (y1 + h1 <= Integer.parseInt(wY2))))) {
                                context.write(new Text(String.valueOf(i)), new Text("rect" + r + "," + y
                                        + "," + h));
                        }
                    }
                    r++;  //next rectangle
                }
                else {
                    for (Integer i = x1; i <= (x1 + w1); i++) {
                            context.write(new Text(String.valueOf(i)), new Text("rect" + r + "," + y
                                    + "," + h));
                        System.out.println("Mapped Rectangle");
                    }
                    r++;
                }
            }
        }
    }

    public static class SpatialJoinReducer
            extends Reducer<Text, Text, Text, Text> {
        @Override
        protected void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {

            Map<Integer, LinkedList<String>> map = new HashMap<>();
            LinkedList<String> points = new LinkedList<>();
            for(Text text : values) {    //for each value given an x,y pair
                String value = text.toString();
                if (value.startsWith("point")) {  //if its from points
                    String pointInfo = value.substring(5);
                    points.add(pointInfo);
                    System.out.println("Reducing point");
                } else if (value.startsWith("rect")) {   //if its from rectangles
                    String rectInfo = value.substring(4);  //take the info
                    String[] split = rectInfo.split(",");
                    Integer yValue = Integer.parseInt(split[1]);
                    Integer hValue = Integer.parseInt(split[2]);
                    System.out.println("Reducing rect");
                    for (Integer i = yValue; i <= (yValue + hValue); i++) {
                        if (map.containsKey(i)){
                            LinkedList<String> mapValues = map.get(i);
                            mapValues.add(rectInfo);
                            map.put(i, mapValues);
                        }
                        else{
                            LinkedList<String> mapValues = new LinkedList<String>();
                            mapValues.add(rectInfo);
                            map.put(i, mapValues);
                        }

                    }
                }
            }
            if (!points.isEmpty()) {
                for (String aPoint: points) { //for each y value
                    if (map.containsKey(Integer.parseInt(aPoint))){
                        LinkedList<String> rectangles = map.get(Integer.parseInt(aPoint));
                        for (String aRect: rectangles){
                            System.out.println("Reduced");
                            context.write(new Text(aRect), new Text(key + ", " + aPoint));
                        }
                    }
                }
            }
        }
    }

    public void debug(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "SpatialJoin");
        job.setJarByClass(SpatialJoin.class);
        job.setMapperClass(SpatialJoinMapper.class);
        job.setReducerClass(SpatialJoinReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileInputFormat.addInputPath(job, new Path(args[2]));
        FileOutputFormat.setOutputPath(job, new Path(args[3]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "SpatialJoin");
        job.setJarByClass(SpatialJoin.class);
        job.setMapperClass(SpatialJoinMapper.class);
        job.setReducerClass(SpatialJoinReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[1]));
        FileInputFormat.addInputPath(job, new Path(args[2]));
        FileOutputFormat.setOutputPath(job, new Path(args[3]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}

