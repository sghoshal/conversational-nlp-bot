package main.java.osn.conversation;

import java.util.ArrayList;
import java.util.List;

public class IntentEntityNode {
	public static String OPTIONAL_ENTITY_ID = "optionalEntities";

	public String intent;

	public String entity;

	public String entityId;

	public boolean isEntityPlainText;

	public String reply;

	public String optionalEnitityPrompt;

	public List<IntentEntityNode> requiredDependencies;

	public List<IntentEntityNode> optionalDependencies;

	public boolean isAsked;

	public List<IntentEntityNode> selectedEntities;

	public boolean isAnswered;

	public String action;

	public IntentEntityNode() {
		requiredDependencies = new ArrayList<IntentEntityNode>();
		optionalDependencies = new ArrayList<IntentEntityNode>();
		selectedEntities = new ArrayList<IntentEntityNode>();
	}

	public IntentEntityNode(String intent, String entity, String entityId, String reply) {
		this.intent = intent;
		this.entity = entity;
		this.entityId = entityId;
		this.isEntityPlainText = false;
		this.reply = reply;

		this.isAsked = false;
		this.isAnswered = false;

		this.requiredDependencies = new ArrayList<IntentEntityNode>();
		this.optionalDependencies = new ArrayList<IntentEntityNode>();
		this.selectedEntities = new ArrayList<IntentEntityNode>();
	}

	public void addRequiredDependency(IntentEntityNode dep) {
		if (dep == null) {
			return;
		}

		this.requiredDependencies.add(dep);
	}

	public void addOptionalDependency(IntentEntityNode dep) {
		if (dep == null) {
			return;
		}

		this.optionalDependencies.add(dep);
	}

	public static boolean isStringNotBlank(String input) {
		return ((input != null) && (!input.trim().isEmpty()));
	}

	public static boolean isStringBlank(String input) {
		return (!isStringNotBlank(input));
	}
}
