package main.java.osn.nlp;

import main.java.osn.conversation.IntentEntityNode;
import main.java.osn.info.BotReplyInfo;
import main.java.osn.info.ClassificationInfo;
import main.java.osn.nlp.entity.Entity;
import main.java.osn.nlp.entity.EntityModel;
import main.java.osn.nlp.intent.Intent;
import main.java.osn.nlp.intent.IntentModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import opennlp.tools.util.*;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NLPManager {
	public static final String INTENT_TRAINING_DATA_DIR = "res/train/intent";
	public static final String ENTITY_TRAINING_DATA_DIR = "res/train/entity";
	public static final String TRAINED_MODEL_DIR = "models/trained";
	private static final Pattern START_TAG_PATTERN = Pattern.compile("<START(:([^:>\\s]*))?>");
	private static final String END_TAG = "<END>";

	private static NLPManager instance = null;

	private IntentModel intentModel;
	private EntityModel entityModel;

	private Map<Intent, LinkedHashSet<Entity>> intentRequiredEntities;
	private String intentTrainingDirectoryPath;
	private String entityTrainingDirectoryPath;

	private IntentEntityNode conversationRoot;

	/**
	 * Private constructor. Singleton pattern - Prevent instantiation from outside this class.
	 */
	private NLPManager() {
		intentRequiredEntities = new HashMap<Intent, LinkedHashSet<Entity>>();

		this.intentModel = new IntentModel();
		this.entityModel = new EntityModel();
	}

	/**
	 * Get the only instance of this class.
	 *
	 * @return
	 */
	public static NLPManager getInstance() {
		if (instance == null) {
			instance = new NLPManager();
		}

		return instance;
	}

	public void train(String intentTrainingDirPath, String entityTrainingDirPath) throws IOException {
		trainModelsAndCreateMappings(getDirectoryObject(intentTrainingDirPath), getDirectoryObject(entityTrainingDirPath));
	}

	/**
	 * Train the Intent classifier = Doccat model.
	 *
	 * @param trainingDirectoryPath
	 * @throws IOException
	 */
	public void trainIntentModel(String trainingDirectoryPath) throws IOException {
		File trainingDirectory = getDirectoryObject(trainingDirectoryPath);
		this.intentTrainingDirectoryPath = trainingDirectoryPath;

		intentModel.train(trainingDirectory);
		createRequiredEntityMapping(trainingDirectory);

		System.out.println("Intent Training complete.");
	}

	/**
	 * Train the Entity classifier = Name Finder model.
	 *
	 * @param trainingDirectoryPath
	 * @throws IOException
	 */
	public void trainEntityModel(String trainingDirectoryPath) throws IOException {
		File trainingDirectory = getDirectoryObject(trainingDirectoryPath);
		this.entityTrainingDirectoryPath = trainingDirectoryPath;

		entityModel.train(trainingDirectory);

		System.out.println("Entity Training complete.");
	}

	private void trainModelsAndCreateMappings(File intentTrainingDirectory, File entityTrainingDirectory) throws IOException {
		intentModel.train(intentTrainingDirectory);
		entityModel.train(entityTrainingDirectory);

		createRequiredEntityMapping(intentTrainingDirectory);
	}

	private void createRequiredEntityMapping(File trainingDirectory) throws IOException {
		FileReader fileReader = null;
		BufferedReader br = null;

		for (File trainingFile : trainingDirectory.listFiles()) {
			String intentStr = trainingFile.getName().replaceFirst("[.][^.]+$", "");

			// Intent specific speech reply is hardcoded for now.

			Intent intent = new Intent(intentStr, "Sure!");

			// Need to remember order of insertion and also should be unique entities.
			LinkedHashSet<Entity> intentEntities = new LinkedHashSet<Entity>();

			try {
				fileReader = new FileReader(trainingFile);
				br = new BufferedReader(fileReader);

				String line;

				while ((line = br.readLine()) != null) {
					// Parse the line and extract entities.

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

//	/**
//	 * Save new training data as text files in the same directory original training data is present.
//	 *
//	 * @param intent
//	 * @param trainingSentences
//	 * @throws IOException
//	 */
//	public void addTrainingData(Intent intent, List<String> trainingSentences) throws IOException
//	{
//		saveNewTrainingData(intent, trainingSentences);
//		train(this.trainingDirectoryPath);
//	}

	// TODO: 1. When to consider end of conversation?
	// TODO: 2. Traversing up the decision tree.
//	public XNLPConvReplyInfo parseUserChatConvWithBot(String chatText, String chatIntent, String entityIdToMatch)
//	{
//		XNLPConvReplyInfo result = new XNLPConvReplyInfo();
//
//		// If the chat intent is not known already, it must be the first chat with the bot in the conversation.
//		// Find the intent.
//		if ((chatIntent == null) || (chatIntent.trim().isEmpty()))
//		{
//			String intent = intentModel.classify(chatText);
//			IntentEntityNode intentNode = getIntentNode(intent);
//
//			// TODO Instead of getFirstUnaksedNode, it should be a generic method that
//			// calculates the next valid node. If the required nodes are all answered,
//
//			IntentEntityNode entityNode = getFirstUnaskedRequiredNode(intentNode);
//
//			entityNode.isAsked = true;
//
//			result.reply = entityNode.reply;
//			result.intent = intent;
//			result.entityIdToBeMatchedByUser = entityNode.entityId;
//
//			return result;
//		}
//		else
//		{
//			// The chat intent is known already, we need to check:
//			// - If the entity is matched by the user and proceed to the next entity.
//			if ((entityIdToMatch != null) && (!entityIdToMatch.trim().isEmpty()))
//			{
//				// TODO if entity is "|optionalEntity"
//
//				IntentEntityNode intentNode = getIntentNode(chatIntent);
//				IntentEntityNode lastAskedEntityNode = getLastAskedNode(conversationRoot, entityIdToMatch);
//
//				List<String> parsedEntities = entityModel.classify(chatText);
//
//				if (parsedEntities == null)
//				{
//					result.reply = "Parsed entity list = null. Was trying to match: " + lastAskedEntityNode.entity;
//					return result;
//				}
//
//				for (String ent : parsedEntities)
//				{
//					if (ent.equals(lastAskedEntityNode.entity))
//					{
//						lastAskedEntityNode.isAnswered = true;
//						IntentEntityNode nextRequiredNode = getUnaskedRequiredNode(conversationRoot);
//
//						// If the next required node is null, then provide the optional parameter prompt
//						// present in this node.
//
//						if (nextRequiredNode == null)
//						{
//							if (IntentEntityNode.isStringNotBlank(lastAskedEntityNode.optionalEnitityPrompt))
//							{
//								result.reply = lastAskedEntityNode.optionalEnitityPrompt;
//								result.intent = chatIntent;
//								result.entityIdToBeMatchedByUser = lastAskedEntityNode.entityId + IntentEntityNode.optionalEntityIdSuffix;
//							}
//							else
//							{
//								result.reply = "Thanks for providing all the information."
//								result.intent = chatIntent;
//							}
//
//							return result;
//						}
//						else
//						{
//							// There are more required entities for which the user needs to provide info.
//
//						}
//
//						// TODO
//						// We found a match
//						// This is where the builder is supposed to add the action associated with this node.
//
//					}
//				}
//
//			}
//		}
//	}

//	private IntentEntityNode getLastAskedNode(IntentEntityNode current, String entityIdToBeMatched)
//	{
//		if (current == null)
//		{
//			return null;
//		}
//
//		List<Dependency> entityDeps = current.dependencies;
//
//		for (Dependency dep : entityDeps)
//		{
//			if ((dep.node.isAsked) && (!dep.node.isAnswered))
//			{
//				if (entityIdToBeMatched.equals(dep.node.entityId))
//				{
//					return dep.node;
//				}
//			}
//
//			return getLastAskedNode(dep.node, entityIdToBeMatched);
//		}
//
//		return null;
//	}

//	private IntentEntityNode getUnaskedRequiredNode(IntentEntityNode current)
//	{
//		if (current == null)
//		{
//			return null;
//		}
//
//		List<Dependency> entityDeps = current.dependencies;
//
//		for (Dependency dep : entityDeps)
//		{
//			if (dep.isRequired)
//			{
//				if (!dep.node.isAsked)
//				{
//					return dep.node;
//				}
//
//				return getUnaskedRequiredNode(dep.node);
//			}
//		}
//
//		return null;
//	}

	/**
	 * Parse the user chat message to bot and return the bot reply as string.
	 *
	 * @param
	 * @return
	 */
	public BotReplyInfo parseUserChatToBot(String chatText, String chatIntent, String entityIdToMatch) {
		BotReplyInfo result = new BotReplyInfo();

		// If chatIntent is not known by the client, we need to match the sentence with our Doccat mode to get
		// the intent. The entity to be matched will be the first entry in the map of intent to required entities.

		if ((chatIntent == null) || (chatIntent.trim().isEmpty())) {
			// Get the intent if it is not already passed in the user chat to bot.

			DocumentCategorizerME categorizer = intentModel.getCategorizer();
			double[] outcome = categorizer.categorize(chatText);
			String intentStr = categorizer.getBestCategory(outcome);

			for (Intent xi : intentRequiredEntities.keySet()) {
				if (xi.getIntent().equals(intentStr)) {
					result.intent = xi;
					result.reply = xi.getPrompt();

					LinkedHashSet<Entity> reqEntities = intentRequiredEntities.get(xi);

					Iterator<Entity> it = reqEntities.iterator();

					if (it.hasNext()) {
						result.entityToBeMatchedByUser = it.next();
					}

					return result;
				}
			}

			result.reply = "Sorry, couldn't figure out what you meant.";
			return result;
		}
		// If we come here we know that the client has sent the intent info already.
		// We need to check if the text sent by user now matches the classified entity.

		for (Intent xi : intentRequiredEntities.keySet()) {
			if (xi.getIntent().equals(chatIntent)) {
				result.intent = xi;
				LinkedHashSet<Entity> reqEntities = intentRequiredEntities.get(xi);

//				System.out.print("action=" + retVal.intent + " args={ ");

				// Get the entity(ies) from our Name Finder Model.

				NameFinderME nameFinder = entityModel.getNameFinder();

				String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(chatText);
				Span[] spans = nameFinder.find(tokens);
				String[] names = Span.spansToStrings(spans, tokens);

//				nameFinder.clearAdaptiveData();

				for (int i = 0; i < spans.length; i++) {
					System.out.print(spans[ i ].getType() + "=" + names[ i ] + " ");

					String classifiedEntity = names[ i ];
					Iterator<Entity> it = reqEntities.iterator();

					// Iterate through the required entities for the give intent.
					// If there is an entity that matches our model classified entity and the IDs are same as well,
					// 			then we have completed processing this entity node - Now move on to next.
					while (it.hasNext()) {
						Entity xe = it.next();

						if ((classifiedEntity.equals(xe.getEntity())) && (xe.getEntityId().equals(entityIdToMatch))) {
							// If there are more required entities to be matched, send that in the bot result.

							if (it.hasNext()) {
								result.entityToBeMatchedByUser = it.next();
								result.reply = result.entityToBeMatchedByUser.getPrompt();
							}
							else {
								result.reply = "You have provided all the required information.";
							}

							return result;
						}
					}
				}

				// If we come here -> None of the entities parsed by our model for the chat Text matched the entity Id that was to be matched.
				// This means that the user has replied with some chat that we don't recognize with any of the entities stored for the intent.

				result.reply = "I could not process this info. [Bot should ask the user the last question in a different way]";
				return result;
			}
		}

		// If we reach here, the intent specified by the client is something we don't understand.
		// Throw an exception!

		//		throw new RuntimeException("Invalid intent specified in the request.");
		BotReplyInfo invalidResult = new BotReplyInfo();

		invalidResult.reply = "Invalid intent specified in the request.";

		return invalidResult;
	}

	public ClassificationInfo classifySentence(String input) {
		ClassificationInfo retVal = new ClassificationInfo();

		retVal.intent = intentModel.classify(input);
		List<String> classifiedEntities = entityModel.classify(input);

		if ((classifiedEntities != null) && (!classifiedEntities.isEmpty())) {
			retVal.entities = classifiedEntities;
		}

		return retVal;
	}

	/**
	 * Parse entities in a sentence.
	 *
	 * @param line
	 * @return
	 * @throws IOException
	 */
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

				result.add(new Entity(startMatcher.group(2), true, startMatcher.group(2) + "-id"));
			}
			else if (token.equals(END_TAG)) {
				encounteredStartTag = false;
			}
		}

		return result;
	}

//	/**
//	 * Save new training sentences for an intent to disk.
//	 *
//	 * TODO FIX BUG - Need to check if the new sentences are not already present in the intent.txt file.
//	 *
//	 * @param intent
//	 * @param trainingSentences
//	 * @throws IOException
//	 */
//	private void saveNewTrainingData(Intent intent, List<String> trainingSentences) throws IOException {
//		File destFile = new File(this.intentTrainingDirectoryPath + "/" + intent.getIntent() + ".txt");
//
//		if (destFile.isDirectory())
//		{
//			throw new RuntimeException("The destination file is a directory. Change intent argument. ");
//		}
//
//		// Need to check here if the sentence is already loaded in the file.
//
//		StringBuilder sb = new StringBuilder();
//
//		for (String sentence : trainingSentences)
//		{
//			sb.append(sentence);
//			sb.append("\n");
//		}
//
//		FileUtils.writeStringToFile(destFile, sb.toString(), destFile.exists());
//	}

	private File getDirectoryObject (String path) {
		System.out.println("Path to training data: " + path);
		File trainingDirectory = new File(path);

		if (!trainingDirectory.isDirectory()) {
			throw new IllegalArgumentException("TrainingDirectory is not a directory: " + trainingDirectory.getAbsolutePath());
		}

		return trainingDirectory;
	}

	public EntityModel getEntityModel() {
		return this.entityModel;
	}

	public IntentModel getIntentModel() {
		return this.intentModel;
	}

	public Map<Intent, LinkedHashSet<Entity>> getIntentRequiredEntities() {
		return this.intentRequiredEntities;
	}
}

