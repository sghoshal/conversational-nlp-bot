package main.java.osn.info;

import main.java.osn.nlp.entity.Entity;
import main.java.osn.nlp.intent.Intent;

public class BotReplyInfo {
	public Intent intent;

	public Entity entityToBeMatchedByUser;

	public String entityIdToBeMatchedByUser;

	public String reply;

	@Override
	public String toString() {
		return "BotReplyInfo{" +
			   "intent=" + intent +
			   ", entityToBeMatchedByUser=" + entityToBeMatchedByUser +
			   ", reply='" + reply + '\'' +
			   '}';
	}
}
