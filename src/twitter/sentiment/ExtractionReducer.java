package twitter.sentiment;

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class ExtractionReducer extends Reducer<Text, IntWritable, Text, IntWritable>  {
	private IntWritable occurrencesOfWord = new IntWritable();
	
	public void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException  {
		int sum = 0;
		for (IntWritable val : values) {
			sum += val.get();
		}
		occurrencesOfWord.set(sum);
		key.set(key.toString().toLowerCase());
		context.write(key, occurrencesOfWord);	 
		 
	}
}