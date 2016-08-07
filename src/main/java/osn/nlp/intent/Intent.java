package main.java.osn.nlp.intent;

public class Intent {

	private String intent;

	private String prompt;

	public Intent(String intent) {
		this(intent, null);
	}

	public Intent(String intent, String prompt) {
		this.intent = intent;
		this.prompt = prompt;
	}

	public String getPrompt() {
		return prompt;
	}

	public void setSpeech(String prompt) {
		this.prompt = prompt;
	}

	public String getIntent() {
		return intent;
	}

	public void setIntent(String intent) {
		this.intent = intent;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if ((o == null) || (getClass() != o.getClass())) return false;

		Intent xIntent = (Intent) o;
		return intent.equals(xIntent.intent);
	}

	@Override
	public String toString() {
		return "Intent{" +
				"intent='" + intent + '\'' +
				", prompt='" + prompt + '\'' +
				'}';
	}

	@Override
	public int hashCode() {
		return intent.hashCode();
	}
}
