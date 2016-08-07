package main.java.osn.nlp;

import main.java.osn.info.ClassificationInfo;
import main.java.osn.nlp.entity.Entity;
import main.java.osn.nlp.entity.EntityModel;
import main.java.osn.nlp.intent.Intent;
import main.java.osn.nlp.intent.IntentModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.*;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NLPModule {
	public static final String TRAINING_DATA_DIR = "res/train";
	public static final String TRAINED_MODEL_DIR = "models/trained";
	private static final Pattern START_TAG_PATTERN = Pattern.compile("<START(:([^:>\\s]*))?>");
	private static final String END_TAG = "<END>";

	private static NLPModule instance = null;

	private IntentModel intentModel;
	private EntityModel entityModel;

	private Map<Intent, Set<Entity>> intentRequiredEntities;
	private String trainingDirectoryPath;

	// Prevent instantiation from outside this class.
	private NLPModule() {
		intentRequiredEntities = new HashMap<Intent, Set<Entity>>();

		this.intentModel = new IntentModel();
		this.entityModel = new EntityModel();
	}

	public static NLPModule getInstance() {
		if (instance == null) {
			instance = new NLPModule();
		}

		return instance;
	}

	public void train(String trainingDirectoryPath) throws IOException {
		System.out.println("Path to training data: " + trainingDirectoryPath);

		File trainingDirectory = new File(trainingDirectoryPath);

		if (!trainingDirectory.isDirectory()) {
			throw new IllegalArgumentException("TrainingDirectory is not a directory: " + trainingDirectory.getAbsolutePath());
		}

		this.trainingDirectoryPath = trainingDirectoryPath;
		trainModelsAndCreateMappings(trainingDirectory);

		System.out.println("Training complete. Ready.");
	}

	public ClassificationInfo classifySentence(String input) {
		ClassificationInfo retVal = new ClassificationInfo();

		DocumentCategorizerME categorizer = intentModel.getCategorizer();

		double[] outcome = categorizer.categorize(input);
		retVal.intent = categorizer.getBestCategory(outcome);

		System.out.print("Intent = " + retVal.intent + " Entities = { ");

		NameFinderME nameFinder = entityModel.getNameFinder();

		String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(input);
		Span[] spans = nameFinder.find(tokens);
		String[] names = Span.spansToStrings(spans, tokens);

		for (int i = 0; i < spans.length; i++) {
			System.out.print(spans[i].getType() + ": " + names[i] + " ");
			retVal.entities.add(names[i]);
		}
//            nameFinderME.clearAdaptiveData();

		System.out.println("}");
		System.out.print(">");

		return retVal;
	}

	private void trainModelsAndCreateMappings(File trainingDirectory) throws IOException {
		intentModel.train(trainingDirectory);
		entityModel.train(trainingDirectory);

		createRequiredEntityMapping(trainingDirectory);
	}

	private void createRequiredEntityMapping(File trainingDirectory) throws IOException {
		FileReader fileReader = null;
		BufferedReader br = null;

		for (File trainingFile : trainingDirectory.listFiles()) {
			String intentStr = trainingFile.getName().replaceFirst("[.][^.]+$", "");
			Intent intent = new Intent(intentStr, "Sure!");

			Set<Entity> intentEntities = new LinkedHashSet<Entity>();			// Need to remember order of insertion.

			try {
				fileReader = new FileReader(trainingFile);
				br = new BufferedReader(fileReader);

				String line;

				while ((line = br.readLine()) != null) {
					List<Entity> entitiesInSentence = parseEntities(line);
					intentEntities.addAll(entitiesInSentence);
				}
			}
			catch (IOException e) {
				throw new IOException(e);
			}
			finally {
				if (fileReader != null) fileReader.close();
				if (br != null) br.close();
			}

			if (intentRequiredEntities.containsKey(intent)) {
				 intentRequiredEntities.get(intent).addAll(intentEntities);
			}
			else {
				intentRequiredEntities.put(intent, intentEntities);
			}
		}
	}

	public void addTrainingData(Intent intent, List<String> trainingSentences) throws IOException {
		saveNewTrainingData(intent, trainingSentences);
		train(this.trainingDirectoryPath);
	}

	private List<Entity> parseEntities(String line) throws IOException {
		List<Entity> result = new ArrayList<Entity>();

		if ((line == null) || (line.trim().isEmpty())) {
			return result;
		}

		// This tokenizer needs to be the same as the one used in entityModel training.

		String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(line);
		boolean encounteredStartTag = false;

		for (String token : tokens) {
			Matcher startMatcher = START_TAG_PATTERN.matcher(token);
			if (startMatcher.matches()) {
				if (encounteredStartTag) {
					throw new IOException("Encountered <START:*> again before closing the previous <START:*> tag");
				}
				encounteredStartTag = true;

				System.out.println(String.format("[Group0: %s\tGroup1: %s\tGroup2: %s]\n",
									startMatcher.group(0), startMatcher.group(1), startMatcher.group(2)));

				result.add(new Entity(startMatcher.group(2), true, startMatcher.group(2) + "-id"));
			}
			else if (token.equals(END_TAG)) {
				encounteredStartTag = false;
			}
		}

		return result;
	}

	private void saveNewTrainingData(Intent intent, List<String> trainingSentences) throws IOException {
		File destFile = new File(TRAINING_DATA_DIR + "/" + intent.getIntent() + ".txt");

		if (destFile.isDirectory()) {
			throw new RuntimeException("The destination file is a directory. Change intent argument. ");
		}

		// Need to check here if the sentence is already loaded in the file.

		StringBuilder sb = new StringBuilder();

		for (String sentence : trainingSentences) {
			sb.append(sentence);
			sb.append("\n");
		}

		FileUtils.writeStringToFile(destFile, sb.toString(), destFile.exists());
	}

	public Map<Intent, Set<Entity>> getIntentRequiredEntities() {
		return this.intentRequiredEntities;
	}
}

