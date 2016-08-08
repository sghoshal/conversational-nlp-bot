package main.java.osn.conversation;

import java.util.ArrayList;
import java.util.List;

/**
 * A single Conversation specific Context Node.
 */
public class ConvContextNode {
	public static String OPTIONAL_ENTITY_ID = "optionalEntities";

	public String intent;
	public String entity;
	public String entityId;
	public String reply;
	public String optionalEntityPrompt;
	public List<ConvContextNode> requiredDependencies;
	public List<ConvContextNode> optionalDependencies;
	public List<ConvContextNode> selectedEntities;
	public String action;
	public boolean isEntityPlainText;
	public boolean isAsked;
	public boolean isAnswered;

	public ConvContextNode() {
		requiredDependencies = new ArrayList<ConvContextNode>();
		optionalDependencies = new ArrayList<ConvContextNode>();
		selectedEntities = new ArrayList<ConvContextNode>();
	}

	public ConvContextNode(String intent, String entity, String entityId, String reply) {
		this.intent = intent;
		this.entity = entity;
		this.entityId = entityId;
		this.reply = reply;

		this.isEntityPlainText = false;
		this.isAsked = false;
		this.isAnswered = false;

		this.requiredDependencies = new ArrayList<ConvContextNode>();
		this.optionalDependencies = new ArrayList<ConvContextNode>();
		this.selectedEntities = new ArrayList<ConvContextNode>();
	}

	public void addRequiredDependency(ConvContextNode dep) {
		if (dep == null)
			return;

		this.requiredDependencies.add(dep);
	}

	public void addOptionalDependency(ConvContextNode dep) {
		if (dep == null)
			return;

		this.optionalDependencies.add(dep);
	}

	@Override
	public String toString() {
		String retval = String.format("[Intent: %s | Entity: %s | EntityID: %s | Reply: %s\n" +
										"RequiredDependencies: %s | Optional Dependencies: %s\n" +
										"IsAsked: %s | IsAnswered: %s]\n",
										intent, entity, entityId, reply,
										requiredDependencies, optionalDependencies,
										isAsked, isAnswered);

		return retval;
	}

}
