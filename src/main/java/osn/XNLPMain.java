package main.java.osn;

import main.java.osn.nlp.XNLPModule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class XNLPMain {
	public static void main( String[] args ) {
		XNLPModule nlpModule = XNLPModule.getInstance();

		try
		{
			nlpModule.trainWithInputFile( "res/train" );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		nlpModule.classifySentence("Can you recommend me users to follow");
		nlpModule.classifySentence("Tell me who I can follow");
		nlpModule.classifySentence("Who should I add to this conversation");
		nlpModule.classifySentence("Any people I can follow");

		// Add intents

		String playMusicIntent = "play-music";
		List<String> playMusicSentences = new ArrayList<String>();

		playMusicSentences.add( "Can you play <START:artist> Beatles <END>" );
		playMusicSentences.add( "I would like to listen to <START:artist> Pearl Jam <END>" );
		playMusicSentences.add( "Play <START:song> Hotel California <END> by <START:artist> Eagles <END>" );

		try
		{
			nlpModule.addTrainingData( playMusicIntent, playMusicSentences );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		nlpModule.classifySentence("I want to hear songs by Pearl Jam");
	}
}
