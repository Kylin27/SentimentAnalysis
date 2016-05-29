package twitter.sentiment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import cmu.arktweetnlp.Tagger;
import cmu.arktweetnlp.Tagger.TaggedToken;
import cmu.arktweetnlp.io.JsonTweetReader;

public class AnalysisMapper extends Mapper<Object, Text, Text, IntWritable>  {
	private IntWritable sentiment = new IntWritable();
	private Text word = new Text();
	private Tagger tagger = new Tagger();
	
	// Static Counters
	private static IntWritable one = new IntWritable(1);
    
	// Dictionaries
	private ArrayList<String> negative = new ArrayList<String>();
	private ArrayList<String> positive = new ArrayList<String>();
	private ArrayList<String> neutral = new ArrayList<String>();
    
	private JsonTweetReader jsonTweetReader = new JsonTweetReader();
    
	public void map(Object key, Text value, Context context) throws IOException, InterruptedException  {
		
		List<TaggedToken> taggedTokens = null;
		ArrayList<TaggedToken> subjects = new ArrayList<TaggedToken>();
		String line = value.toString();
		if(!line.isEmpty())  {
			// Filter out tweet text
			if(line.indexOf("\"text\":") > 0)  {
				String tweet = jsonTweetReader.getText(line);
				taggedTokens = tagger.tokenizeAndTag(tweet);
							    
				// Extract the subjects
				for(TaggedToken token : taggedTokens)  { 
			    	if((token.tag.equals("N")|| token.tag.equals("^")))  {
			    			
			    		token.token = token.token.replaceAll("[-@%&#$'()?0-9://]","");
			    		boolean check = false;
			    		
			    		// Check if subject already in list
			    		for(TaggedToken sub : subjects)  {
			    			if(sub.token.equalsIgnoreCase(token.token))
			    				check = true;
			    			}
			    		
			    		if(!check)  {
			    			subjects.add(token);
			    		}
			    	}
			    		 
				}
			
		    
				// Calculate Sentiment
				int tweetSentiment = 0;
				for (TaggedToken token : taggedTokens) {
					token.token = token.token.replaceAll("[-@#$']","");
					System.err.println(token.token + "\t" + token.tag);
					if(token.tag.equals("V") || token.tag.equals("A") || token.tag.equals("R") || token.tag.equals("!") || token.tag.equals("E")  || token.tag.equals("M"))  {
						// Check positive Dictionaries
						if(positive.indexOf(token.token.toLowerCase()) > 0)  {
							tweetSentiment += 1;
						}
						// Check negative Dictionaries
						else if(negative.indexOf(token.token.toLowerCase()) > 0)  {
							tweetSentiment -= 1;
						}
					}
					
				}
				sentiment.set(tweetSentiment);
				for(TaggedToken sub : subjects)  {
					if(!sub.equals(""))  {
						if(sub.token.toLowerCase().equals("dog") || sub.token.toLowerCase().equals("puppy") || sub.token.toLowerCase().equals("cat") || sub.token.toLowerCase().equals("kitten") || sub.token.toLowerCase().equals("rabbit") || sub.token.toLowerCase().equals("bunny") || sub.token.toLowerCase().equals("fish"))  {
							word.set(sub.token.toLowerCase());
							context.write(new Text(sub.token.toLowerCase()+"-sentiment"), sentiment);
							context.write(new Text(sub.token.toLowerCase()+"-frequency"), one);
						}
					}
				}
			}
			//System.out.println("Dictionary Sanity Check:" + negativeEmotes.get(0));
		}
    }
    
	@Override
	// Runs once when the job starts
	// Loads tagger model and sentiment dictionaries
	protected void setup(Context context) throws IOException, InterruptedException {
		super.setup(context);
		// Load tagger model
		try  {
			tagger.loadModel("/s/chopin/k/grad/yadav/twitter/yarn/model");
		}
		catch (IOException e)  {
			System.out.println("Can't load model!");
		}
		
		// TODO: Load sentiment Dictionaries, modify paths as necessary
		fillDictionary(negative, "/s/bach/a/class/cs455/asg3/dictionary/negativeEmotes");
		fillDictionary(positive, "/s/bach/a/class/cs455/asg3/dictionary/positiveEmotes");
		fillDictionary(negative, "/s/bach/a/class/cs455/asg3/dictionary/negativeVerbs");
		fillDictionary(positive, "/s/bach/a/class/cs455/asg3/dictionary/positiveVerbs");
		fillDictionary(negative, "/s/bach/a/class/cs455/asg3/dictionary/negativeExclaim");
		fillDictionary(positive, "/s/bach/a/class/cs455/asg3/dictionary/positiveExclaim");
		fillDictionary(negative, "/s/bach/a/class/cs455/asg3/dictionary/negativeStopwords");
		fillDictionary(positive, "/s/bach/a/class/cs455/asg3/dictionary/positiveStopwords");
		fillDictionary(neutral, "/s/bach/a/class/cs455/asg3/dictionary/neutralEmotes");
		fillDictionary(neutral, "/s/bach/a/class/cs455/asg3/dictionary/neutralVerbs");
		fillDictionary(neutral, "/s/bach/a/class/cs455/asg3/dictionary/neutralExclaim");
		fillDictionary(neutral, "/s/bach/a/class/cs455/asg3/dictionary/neutralStopWords");

	}
    
	// Helper method to load dictionary items into ArrayLists
	public void fillDictionary(ArrayList<String> list, String filename)  {
		try  {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			while((line = br.readLine()) != null) {
				list.add(line.toLowerCase());
			}
			br.close();
		}
		catch(Exception e){}
	}
}