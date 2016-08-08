package main.java.osn.info;

import java.util.ArrayList;
import java.util.List;

public class ClassificationInfo {
	public String intent;
	public List<String> entities;

	public ClassificationInfo() {
		this.entities = new ArrayList<String>();
	}

	@Override
	public String toString() {
		return "[ Intent: '" + intent + "\t" + "Entities: " + entities + " ]";
	}
}
