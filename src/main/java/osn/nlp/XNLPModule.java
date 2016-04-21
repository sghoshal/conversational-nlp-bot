package main.java.osn.nlp;

import main.java.osn.info.XClassificationInfo;
import main.java.osn.nlp.entity.XEntityModule;
import main.java.osn.nlp.intent.XIntentModule;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.List;

public class XNLPModule
{
	private static XNLPModule instance = null;

	private XIntentModule intentModule;
	private XEntityModule entityModule;

	public static final String TRAINING_DATA_DIR = "res/train";
	public static final String TRAINED_MODEL_PATH = "models/trained/doccat-trained.bin";

	// Prevent instantiation from outside this class.
	private XNLPModule()
	{
		this.intentModule = new XIntentModule();
		this.entityModule = new XEntityModule();
	}

	public static XNLPModule getInstance()
	{
		if (instance == null) {
			instance = new XNLPModule();
		}

		return instance;
	}

	public void trainWithInputFile( String path ) throws IOException
	{
		intentModule.train( path );
		entityModule.train( path );

		System.out.println("Training complete. Ready.");
	}

	public XClassificationInfo classifySentence( String input )
	{
		XClassificationInfo retVal = new XClassificationInfo();

		DocumentCategorizerME categorizer = intentModule.getCateogorizer();

		double[] outcome = categorizer.categorize(input);
		retVal.intent = categorizer.getBestCategory( outcome );
		System.out.print("action=" + retVal.intent + " args={ ");

		NameFinderME nameFinder = entityModule.getNameFinder();

		String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(input);
		Span[] spans = nameFinder.find( tokens );
		String[] names = Span.spansToStrings( spans, tokens );

		for ( int i = 0; i < spans.length; i++ )
		{
			System.out.print(spans[i].getType() + "=" + names[i] + " ");
			retVal.entities.add( names[i] );
		}
//            nameFinderME.clearAdaptiveData();

		System.out.println("}");
		System.out.print(">");

		return retVal;
	}

	public void addTrainingData( String intent, List<String> trainingSentences ) throws IOException
	{
		saveNewTrainingData(intent, trainingSentences);

		intentModule.train();
		entityModule.train();
	}

	private void saveNewTrainingData( String intent, List<String> trainingSentences ) throws IOException {
		File destFile = new File( TRAINING_DATA_DIR + "/" + intent + ".txt" );

		if ( destFile.isDirectory() )
		{
			throw new RuntimeException( "The destination file is a directory. Change intent argument. " );
		}

		StringBuilder sb = new StringBuilder();

		for ( String sentence : trainingSentences )
		{
			sb.append( sentence );
			sb.append( "\n" );
		}

		FileUtils.writeStringToFile(destFile, sb.toString(), destFile.exists());
	}
}

