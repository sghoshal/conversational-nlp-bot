package main.java.osn.nlp.entity;

import main.java.osn.nlp.NLPManager;
import main.java.osn.nlp.NLPModel;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.ObjectStreamUtils;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EntityModel implements NLPModel {
	private NameFinderME nameFinder;
	private TokenNameFinderModel tokenNameFinderModel;

	public void train() throws IOException {
		System.out.println("Path to training data: " + NLPManager.ENTITY_TRAINING_DATA_DIR);

		File trainingDirectory = new File(NLPManager.ENTITY_TRAINING_DATA_DIR);

		if (!trainingDirectory.isDirectory()) {
			throw new IllegalArgumentException("TrainingDirectory is not a directory: " + trainingDirectory.getAbsolutePath());
		}

		train(trainingDirectory);
	}

	@Override
	public void train(File trainingDirectory) throws IOException {
		List<ObjectStream<NameSample>> nameStreams = new ArrayList<ObjectStream<NameSample>>();

		for (File trainingFile : trainingDirectory.listFiles()) {
			ObjectStream<String> lineStream = new PlainTextByLineStream(new FileInputStream(trainingFile), "UTF-8");

			// Tokenizer used for training data and classification should be the same. Make sure that is the case.

			ObjectStream<NameSample> nameSampleStream = new NameSampleDataStream(lineStream);
			nameStreams.add(nameSampleStream);
		}

		ObjectStream<NameSample> combinedNameSampleStream = ObjectStreamUtils.createObjectStream(nameStreams.toArray(new ObjectStream[ 0 ]));

		this.tokenNameFinderModel = NameFinderME.train("en", null, combinedNameSampleStream, TrainingParameters.defaultParams(),
																		(AdaptiveFeatureGenerator) null, Collections.<String, Object>emptyMap());

		combinedNameSampleStream.close();
		this.nameFinder = new NameFinderME(tokenNameFinderModel);
	}

	public List<String> classify(String input) {
		List<String> result = new ArrayList<String>();

		String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(input);
		Span[] spans = nameFinder.find(tokens);
		String[] names = Span.spansToStrings(spans, tokens);

		for (int i = 0; i < spans.length; i++) {
			System.out.print("Entity: " + spans[i].getType() + "=" + names[i] + " ");
			result.add(names[i]);
		}
//            nameFinderME.clearAdaptiveData();

		return result;
	}

	@Override
	public void writeModel() {

	}

	@Override
	public DoccatModel readExistingModel() throws IOException {
		return null;
	}

	public NameFinderME getNameFinder() {
		return nameFinder;
	}

	public TokenNameFinderModel getTokenNameFinderModel() {
		return tokenNameFinderModel;
	}
}
