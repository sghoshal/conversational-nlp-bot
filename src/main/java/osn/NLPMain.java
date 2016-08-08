package main.java.osn;

import main.java.osn.conversation.ConvContextManager;
import main.java.osn.conversation.ConvContextNode;
import main.java.osn.info.ConvContextReplyInfo;
import main.java.osn.nlp.NLPManager;
import main.java.osn.utils.NLPUtils;

import java.io.IOException;
import java.util.*;

/**
 * Entry class.
 */
public class NLPMain {

	/**
	 * Create the intent dependency tree and seed it.
	 *
	 * @return
	 */
	private static ConvContextNode createConversationDependencies() {
		ConvContextNode root = new ConvContextNode();

		String intentJiraName = "updateJira";
		ConvContextNode updateJira = new ConvContextNode(intentJiraName, null, null, "What attribute would you like to update?");

		ConvContextNode jiraStory = new ConvContextNode(intentJiraName, "story", "storyId", "What story would you like to update?");
		ConvContextNode jiraSprint = new ConvContextNode(intentJiraName, "sprint", "sprintId", "What sprint would you like to update?");
		ConvContextNode jiraAssignee = new ConvContextNode(intentJiraName, "assignee", "assigneeId", "Who would you like to assign this to?");
		ConvContextNode jiraDescription = new ConvContextNode(intentJiraName, "description", "descriptionId", "Type the new description and hit enter.");

		updateJira.addRequiredDependency(jiraStory);
		updateJira.addRequiredDependency(jiraSprint);
		updateJira.addOptionalDependency(jiraAssignee);
		updateJira.addOptionalDependency(jiraDescription);

		String intentFollowUsersName = "follow-users";
		ConvContextNode followUsers = new ConvContextNode(intentFollowUsersName, null, null, "Sure!");
//		followUsers.optionalEnitityPrompt = "There are some optional entities as well bro.";

		ConvContextNode followUsersReqEntity1 = new ConvContextNode(intentFollowUsersName, "who", "reqEntity1", "Reply from req entity 1");
		ConvContextNode followUsersReqEntity2 = new ConvContextNode(intentFollowUsersName, "people", "reqEntity2", "Reply from req entity 2");
		ConvContextNode followUsersOptEntity3 = new ConvContextNode(intentFollowUsersName, "optEntity3", "optEntity3", "Reply from opt entity 3");
		ConvContextNode followUsersOptEntity4 = new ConvContextNode(intentFollowUsersName, "optEntity4", "optEntity4", "Reply from opt entity 4");

		followUsers.addRequiredDependency(followUsersReqEntity1);
		followUsers.addRequiredDependency(followUsersReqEntity2);
		followUsers.addOptionalDependency(followUsersOptEntity3);
		followUsers.addOptionalDependency(followUsersOptEntity4);

		String intentUpdateProfile = "update-profile";
		ConvContextNode updateProfile = new ConvContextNode(intentUpdateProfile, null, null, "Lets do it!");

		ConvContextNode profileDescription = new ConvContextNode(intentUpdateProfile, "description", "description-id", "Ok, give me a short description about thy self");
		ConvContextNode profileExpertise = new ConvContextNode(intentUpdateProfile, "expertise", "expertise-id", "Ok, what's your superpower. Can be 1 or many!");
		ConvContextNode profileOrganization = new ConvContextNode(intentUpdateProfile, "org", "org-id", "What organization do you belong to?");

		profileDescription.isEntityPlainText = true;
		profileExpertise.isEntityPlainText = true;
		profileOrganization.isEntityPlainText = true;

		updateProfile.addRequiredDependency(profileDescription);
		updateProfile.addRequiredDependency(profileExpertise);
		updateProfile.addRequiredDependency(profileOrganization);

		root.addRequiredDependency(updateJira);
		root.addRequiredDependency(followUsers);
		root.addRequiredDependency(updateProfile);

		return root;
	}

	private static void testExampleFlows(ConvContextManager convContextManager, NLPManager nlpManager)
	{
		// Test Conversation flow for Following users.

		String userInput = "Can you recommend me users to follow";
		System.out.println("User asked: " + userInput);
		System.out.println("Classification: " + nlpManager.classifySentence(userInput));
		System.out.println("Bot Reply: " + convContextManager.generateReply(userInput, null, null));
		System.out.println("-----\n");

		userInput = "Tell me who I can follow";
		System.out.println("User asked: " + userInput);
		System.out.println("Classification: " + nlpManager.classifySentence(userInput));
		System.out.println("Bot Reply: " + convContextManager.generateReply(userInput, "follow-users", "reqEntity1"));
		System.out.println("-----\n");

		userInput = "Any people I can follow";
		System.out.println("User asked: " + userInput);
		System.out.println("Classification: " + nlpManager.classifySentence(userInput));
		System.out.println("Bot Reply: " + convContextManager.generateReply(userInput, "follow-users", "reqEntity2"));
		System.out.println("-----\n");

		// Parse - Who to add to this conversation.

		userInput = "Who should I add to this conversation";
		System.out.println("User asked: " + userInput);
		System.out.println("Classification: " + nlpManager.classifySentence(userInput));
		System.out.println("Bot Reply: " + convContextManager.generateReply(userInput, null, null));
		System.out.println("-----\n");

		// Conversation flow for profile update.

		userInput = "Update profile";
		System.out.println("User asked: " + userInput);
		System.out.println("Classification: " + nlpManager.classifySentence(userInput));
		System.out.println("Bot Reply: " + convContextManager.generateReply(userInput, null, null));
		System.out.println("-----\n");

		userInput = "I am the ruler of bots";
		System.out.println("User asked: " + userInput);
		System.out.println("Bot Reply: " + convContextManager.generateReply(userInput, "update-profile", "description-id"));
		System.out.println("-----\n");

		userInput = "Being awesome at being awesome";
		System.out.println("User asked: " + userInput);
		System.out.println("Bot Reply: " + convContextManager.generateReply(userInput, "update-profile", "expertise-id"));
		System.out.println("-----\n");

		userInput = "Ministry of Awesomeness";
		System.out.println("User asked: " + userInput);
		System.out.println("Bot Reply: " + convContextManager.generateReply(userInput, "update-profile", "org-id"));
		System.out.println("-----\n");
	}

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		NLPManager nlpManager = NLPManager.getInstance();

		try {
			nlpManager.train( NLPManager.INTENT_TRAINING_DATA_DIR, NLPManager.ENTITY_TRAINING_DATA_DIR );
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		ConvContextNode conversationRoot = createConversationDependencies();
		ConvContextManager convContextManager = new ConvContextManager(nlpManager, conversationRoot);

		// testExampleFlows();

		// Get the input from user.

		Scanner scanner = new Scanner(System.in);
		System.out.println("\nStart a chat with Bot.\n" +
							"Enter Done to end current conversation.\n" +
							"Enter Exit to end Chat with the bot completely.\n");

		String cmdLineInput = "start";
		ConvContextReplyInfo botReply;
		String intent;
		String entityToBeMatched;

		while (!NLPUtils.isChatOver(cmdLineInput)) {
			intent = null;
			entityToBeMatched = null;
			cmdLineInput = "start";

			while (!NLPUtils.isConversationOver(cmdLineInput)) {
				System.out.print("You: ");
				cmdLineInput = scanner.nextLine();

				botReply = convContextManager.generateReply(cmdLineInput, intent, entityToBeMatched);
				intent = botReply.intent;
				entityToBeMatched = botReply.entityIdToBeMatchedByUser;

				System.out.println("BOT: " + botReply.reply + "\n");
			}

			System.out.println("Start a new conversation.");
		}
	}
}