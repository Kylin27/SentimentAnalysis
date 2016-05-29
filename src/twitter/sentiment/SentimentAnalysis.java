package twitter.sentiment;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


public class SentimentAnalysis {
	
	public static void main(String[] args) 
	throws Exception 
	{
		// Extraction Job  
		Job job = Job.getInstance(new Configuration());
		job.setJarByClass(SentimentAnalysis.class);
		job.setMapperClass(ExtractionMapper.class);
		job.setCombinerClass(ExtractionReducer.class);
		job.setReducerClass(ExtractionReducer.class);
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job, new Path(args[0]));
		FileOutputFormat.setOutputPath(job, new Path(args[1]));
		
		// Analysis Job
		Job job2 = Job.getInstance(new Configuration());
		job2.setJarByClass(SentimentAnalysis.class);
		job2.setMapperClass(AnalysisMapper.class);
		job2.setCombinerClass(AnalysisReducer.class);
		job2.setReducerClass(AnalysisReducer.class);
		job2.setOutputKeyClass(Text.class);
		job2.setOutputValueClass(IntWritable.class);
		FileInputFormat.addInputPath(job2, new Path(args[0]));
		FileOutputFormat.setOutputPath(job2, new Path(args[2]));
		
		// Analysis 
		if(job.waitForCompletion(true))  {
			System.exit(job2.waitForCompletion(true) ? 0 : 1);
		}
	}
}

