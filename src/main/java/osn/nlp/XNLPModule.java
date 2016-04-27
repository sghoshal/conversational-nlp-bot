package main.java.osn.nlp;

import main.java.osn.info.XClassificationInfo;
import main.java.osn.nlp.entity.XEntity;
import main.java.osn.nlp.entity.XEntityModel;
import main.java.osn.nlp.intent.XIntentModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XNLPModule
{
	private static XNLPModule instance = null;

	private XIntentModel intentModel;
	private XEntityModel entityModel;

	private Map<String, XEntity> intentRequiredEntities;
	private String trainingDirectoryPath;

	public static final String TRAINING_DATA_DIR = "res/train";
	public static final String TRAINED_MODEL_DIR = "models/trained";

	// Prevent instantiation from outside this class.
	private XNLPModule()
	{
		intentRequiredEntities = new HashMap<String, XEntity>();

		this.intentModel = new XIntentModel();
		this.entityModel = new XEntityModel();
	}

	public static XNLPModule getInstance()
	{
		if ( instance == null )
		{
			instance = new XNLPModule();
		}

		return instance;
	}

	public void train( String trainingDirectoryPath ) throws IOException
	{
		System.out.println( "Path to training data: " + trainingDirectoryPath );

		File trainingDirectory = new File( trainingDirectoryPath );

		if ( !trainingDirectory.isDirectory() )
		{
			throw new IllegalArgumentException( "TrainingDirectory is not a directory: " + trainingDirectory.getAbsolutePath() );
		}

		this.trainingDirectoryPath = trainingDirectoryPath;
		trainModelsAndCreateMappings( trainingDirectory );

		System.out.println("Training complete. Ready.");
	}

	public XClassificationInfo classifySentence( String input )
	{
		XClassificationInfo retVal = new XClassificationInfo();

		DocumentCategorizerME categorizer = intentModel.getCateogorizer();

		double[] outcome = categorizer.categorize(input);
		retVal.intent = categorizer.getBestCategory( outcome );
		System.out.print("action=" + retVal.intent + " args={ ");

		NameFinderME nameFinder = entityModel.getNameFinder();

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

	private void trainModelsAndCreateMappings( File trainingDirectory ) throws IOException
	{
		intentModel.train( trainingDirectory );
		entityModel.train( trainingDirectory );

		createRequiredEntityMapping( trainingDirectory );
	}

	private void createRequiredEntityMapping( File trainingDirectory ) throws IOException
	{
		FileReader fileReader = null;
		BufferedReader br = null;

		for ( File trainingFile : trainingDirectory.listFiles() )
		{
			String intent = trainingFile.getName().replaceFirst("[.][^.]+$", "");

			try
			{
				fileReader = new FileReader( trainingFile );
				br = new BufferedReader( fileReader );

				String line;

				while ( ( line = br.readLine() ) != null )
				{
					System.out.println( "Line: " + line );
				}
			}
			catch ( IOException e )
			{
				throw new IOException( e );
			}
			finally
			{
				if ( fileReader != null ) fileReader.close();
				if ( br != null ) br.close();
			}
		}
	}


	public void addTrainingData( String intent, List<String> trainingSentences ) throws IOException
	{
		saveNewTrainingData(intent, trainingSentences);
		train( this.trainingDirectoryPath );
	}

	private void saveNewTrainingData( String intent, List<String> trainingSentences ) throws IOException {
		File destFile = new File( TRAINING_DATA_DIR + "/" + intent + ".txt" );

		if ( destFile.isDirectory() )
		{
			throw new RuntimeException( "The destination file is a directory. Change intent argument. " );
		}

		// Need to check here if the sentence is already loaded in the file.

		StringBuilder sb = new StringBuilder();

		for ( String sentence : trainingSentences )
		{
			sb.append( sentence );
			sb.append( "\n" );
		}

		FileUtils.writeStringToFile(destFile, sb.toString(), destFile.exists());
	}
}

