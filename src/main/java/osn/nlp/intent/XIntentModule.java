package main.java.osn.nlp.intent;

import main.java.osn.nlp.XNLPModel;
import main.java.osn.nlp.XNLPModule;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.PlainTextByLineStream;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class XIntentModule implements XNLPModel
{
	private DoccatModel doccatModel;
	private DocumentCategorizerME docCategorizer;

	public void train() throws IOException
	{
		System.out.println( "Path to training data: " + XNLPModule.TRAINING_DATA_DIR );

		File trainingDirectory = new File( XNLPModule.TRAINING_DATA_DIR );

		if ( !trainingDirectory.isDirectory() )
		{
			throw new IllegalArgumentException( "TrainingDirectory is not a directory: " + trainingDirectory.getAbsolutePath() );
		}

		train( trainingDirectory );
	}

	@Override
	public void train( File trainingDirectory ) throws IOException
	{
		List<ObjectStream<DocumentSample>> categoryStreams = new ArrayList<ObjectStream<DocumentSample>>();

		for ( File trainingFile : trainingDirectory.listFiles() )
		{
			String intent = trainingFile.getName().replaceFirst("[.][^.]+$", "");
			ObjectStream<String> lineStream = new PlainTextByLineStream( new FileInputStream(trainingFile), "UTF-8" );

			ObjectStream<DocumentSample> documentSampleStream = new IntentDocumentSampleStream( intent, lineStream );
			categoryStreams.add( documentSampleStream );
		}

		ObjectStream<DocumentSample> combinedDocumentSampleStream = ObjectStreamUtils.createObjectStream( categoryStreams.toArray( new ObjectStream[ 0 ] ) );

		this.doccatModel = DocumentCategorizerME.train( "en", combinedDocumentSampleStream, 0, 100 );
		combinedDocumentSampleStream.close();

		this.docCategorizer = new DocumentCategorizerME( doccatModel );
	}

	@Override
	public void writeModel()
	{
		OutputStream doccatModelOut = null;

		try
		{
			doccatModelOut = new BufferedOutputStream( new FileOutputStream( XNLPModule.TRAINED_MODEL_PATH ) );
			doccatModel.serialize( doccatModelOut );
		}
		catch ( IOException e )
		{
			e.printStackTrace();
			throw new RuntimeException( "Could not serialize trained model to file system." );
		}
		finally
		{
			if ( doccatModelOut != null )
			{
				try
				{
					doccatModelOut.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
					throw new RuntimeException( "Could not close doccatModelOut stream." );
				}
			}
		}
	}

	@Override
	public DoccatModel readExistingModel() throws IOException
	{
		DoccatModel model = null;
		InputStream doccatModelIn = new FileInputStream( XNLPModule.TRAINED_MODEL_PATH );

		if ( doccatModelIn != null )
		{
			model = new DoccatModel( doccatModelIn );
			doccatModelIn.close();
		}

		return model;
	}

	public void retrainUsingExistingModel() throws IOException
	{
		DoccatModel existingModel = readExistingModel();

		this.doccatModel = ( existingModel != null ) ? existingModel : this.doccatModel;

		docCategorizer = new DocumentCategorizerME( doccatModel );
	}

	public DoccatModel getModel()
	{
		return doccatModel;
	}

	public DocumentCategorizerME getCateogorizer()
	{
		return docCategorizer;
	}
}
