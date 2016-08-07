package main.java.osn.nlp.entity;

public class Entity {
	private String entity;

	private boolean isRequired;

	private String entityId;

	private String action;

	private String prompt;

	public Entity(String entity, boolean isRequired, String entityId) {
		this(entity, isRequired, entityId, null);
	}

	public Entity(String entity, boolean isRequired, String entityId, String prompt) {
		this.entity = entity;
		this.entityId = entityId;
		this.isRequired = isRequired;
		this.prompt = prompt;
	}

	public void setEntity(String entity) {
		this.entity = entity;
	}

	public void setIsRequired(boolean isRequired) {
		this.isRequired = isRequired;
	}

	public void setEntityId(String entityId) {
		this.entityId = entityId;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getEntity() {
		return entity;
	}

	public boolean isRequired() {
		return isRequired;
	}

	public String getEntityId() {
		return entityId;
	}

	public String getAction() {
		return action;
	}

	public String getPrompt() {
		return prompt;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if ((o == null) || (getClass() != o.getClass())) {
			return false;
		}

		Entity entity = (Entity) o;

		return entityId.equals(entity.entityId);
	}

	@Override
	public int hashCode() {
		return entityId.hashCode();
	}

	@Override
	public String toString() {
		return "Entity{" +
			   "entity='" + entity + '\'' +
			   ", isRequired=" + isRequired +
			   ", entityId='" + entityId + '\'' +
			   ", action='" + action + '\'' +
			   ", prompt='" + prompt + '\'' +
			   '}';
	}
}
