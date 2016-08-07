package main.java.osn.info;

public class ConvContextReplyInfo {

	public String intent;
	public String entityIdToBeMatchedByUser;
	public String reply;

	public ConvContextReplyInfo(String intent, String entityIdToBeMatchedByUser, String reply) {
		this.intent = intent;
		this.entityIdToBeMatchedByUser = entityIdToBeMatchedByUser;
		this.reply = reply;
	}

	public ConvContextReplyInfo() {
	}

	@Override
	public String toString() {
		return "NLPConvReplyInfo{" +
			   "intent='" + intent + '\'' +
			   ", entityIdToBeMatchedByUser='" + entityIdToBeMatchedByUser + '\'' +
			   ", reply='" + reply + '\'' +
			   '}';
	}
}
