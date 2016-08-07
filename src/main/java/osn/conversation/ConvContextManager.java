package main.java.osn.conversation;

import java.util.ArrayList;
import java.util.List;

import main.java.osn.info.ConvContextReplyInfo;
import main.java.osn.nlp.NLPManager;

public class ConvContextManager {
	private ConvContextNode conversationRoot;
	private NLPManager nlpManager;

	/**
	 * Allow instantiation from within this class only.
	 */
	public ConvContextManager(NLPManager nlpManager, ConvContextNode conversationRoot) {
		this.conversationRoot = conversationRoot;
		this.nlpManager = nlpManager;
	}

	/**
	 *
	 * @param chatText
	 * @param chatIntent
	 * @param entityIdToMatch
	 * @return
	 */
	public ConvContextReplyInfo generateReply(String chatText, String chatIntent, String entityIdToMatch) {
		ConvContextReplyInfo result = new ConvContextReplyInfo();
		ConvContextNode intentNode = null;

		if (ConvContextNode.isStringBlank(chatIntent)) {
			chatIntent = nlpManager.getIntentModel().classify(chatText);
			intentNode = getIntentNode(chatIntent);

			result = getRequiredEntityOrOptionalPromptReply(intentNode);
		}
		else {
			// If chatIntent and the entityIdToMatch is present, then try finding matches.

			intentNode = getIntentNode(chatIntent);

			if (ConvContextNode.isStringNotBlank(entityIdToMatch)) {
				List<String> classifiedEntities = nlpManager.getEntityModel().classify(chatText);

				// If the entityId to be matched is an optional entity, then:
				// check if the classified entities form the sentence matches any of the predefined ones.

				if (entityIdToMatch.equals(ConvContextNode.OPTIONAL_ENTITY_ID)) {
					result.reply = "";

					for (String ce : classifiedEntities) {
						for (ConvContextNode optDepNode : intentNode.optionalDependencies) {
							if (optDepNode.entity.equals(ce)) {
								String nodeReply = "Found a match: " + optDepNode.entity + "->" + optDepNode.action;
								result.reply += nodeReply;
							}
						}
					}

					result.intent = chatIntent;
					result.entityIdToBeMatchedByUser = null;
				}
				else {
					// This is for required entities.

					ConvContextNode lastAskedEntity = getLastAskedRequiredEntityNode(intentNode);

					if (lastAskedEntity.isEntityPlainText) {
						lastAskedEntity.isAnswered = true;
						result = getRequiredEntityOrOptionalPromptReply(intentNode);
						result.reply = "Got it. Lets move on. " + result.reply;
					}
					else {
						for (String ce : classifiedEntities) {
							if (lastAskedEntity.entity.equals(ce) && lastAskedEntity.entityId.equals(entityIdToMatch)) {
								lastAskedEntity.isAnswered = true;
								result = getRequiredEntityOrOptionalPromptReply(intentNode);
								result.reply = "Got it. Lets move on. " + result.reply;
								// break;
							}
						}
					}
				}
			}
			else {
				// When chatIntent is present and entityIdToMatch is null

				result = new ConvContextReplyInfo(chatIntent, entityIdToMatch, "Could not match any entities as entityIdToBeMatched field is null.");
			}
		}

		return result;
	}

	public void closeChatContext() {
		closeChatContextHelper(conversationRoot);
	}

	/**
	 * Get the next required entity node. If the entity has been asked and not answered, return that node.
	 * Else return the next unasked entity node.
	 *
	 * @param intentNode
	 * @return
	 */
	private ConvContextNode getNextRequiredEntityNode(ConvContextNode intentNode) {
		if ((intentNode == null) || (intentNode.requiredDependencies == null)) {
			return null;
		}

		for (ConvContextNode dep : intentNode.requiredDependencies) {
			if (dep.isAsked && !dep.isAnswered) {
				return dep;
			}

			if (!dep.isAsked) {
				return dep;
			}
		}

		return null;
	}

	private ConvContextNode getIntentNode(String intentInput) {
		if (ConvContextNode.isStringBlank(intentInput)) {
			return null;
		}

		List<ConvContextNode> intents = conversationRoot.requiredDependencies;

		for (ConvContextNode dep : intents) {
			if (intentInput.equals(dep.intent)) {
				return dep;
			}
		}

		return null;
	}

	/**
	 *
	 * @param intentNode
	 * @return
	 */
	private ConvContextReplyInfo getRequiredEntityOrOptionalPromptReply(ConvContextNode intentNode) {
		if (intentNode == null) {
			return new ConvContextReplyInfo(null, null, "Could not calculate the intent.");
		}

		ConvContextNode entityNode = getNextRequiredEntityNode(intentNode);
		ConvContextReplyInfo result = null;

		if (entityNode != null) {
			entityNode.isAsked = true;
			result = new ConvContextReplyInfo(intentNode.intent, entityNode.entityId, entityNode.reply);
		}
		else if (ConvContextNode.isStringNotBlank(intentNode.optionalEntityPrompt)) {
			// If all the required entities have been asked, ask for optional entities if prompt present.

			result = new ConvContextReplyInfo(intentNode.intent,
										ConvContextNode.OPTIONAL_ENTITY_ID,
										intentNode.optionalEntityPrompt);
		}
		else {
			// We have asked all the required and optional entities (if any).

			result = new ConvContextReplyInfo(intentNode.intent, null, "Thanks for providing all the information!");
			closeChatContext();
		}

		return result;
	}

	private ConvContextNode getLastAskedRequiredEntityNode(ConvContextNode current) {
		if (current == null) {
			return null;
		}

		for (ConvContextNode reqDep : current.requiredDependencies) {
			if (reqDep.isAsked && !reqDep.isAnswered) {
				return reqDep;
			}
		}

		return null;
	}

	private void closeChatContextHelper(ConvContextNode current) {
		if (current == null) {
			return;
		}

		if (current.requiredDependencies != null) {
			for (ConvContextNode dep : current.requiredDependencies) {
				closeChatContextHelper(dep);

				dep.isAsked = false;
				dep.isAnswered = false;
				dep.selectedEntities = new ArrayList<ConvContextNode>();
			}
		}

		if (current.optionalDependencies != null) {
			for (ConvContextNode dep : current.optionalDependencies) {
				closeChatContextHelper(dep);

				dep.isAsked = false;
				dep.isAnswered = false;
				dep.selectedEntities = new ArrayList<ConvContextNode>();
			}
		}
	}

}
