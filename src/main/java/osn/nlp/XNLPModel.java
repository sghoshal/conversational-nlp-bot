package main.java.osn.nlp;

import opennlp.tools.doccat.DoccatModel;

import java.io.File;
import java.io.IOException;

public interface XNLPModel
{
	void train( File trainingDirectory ) throws IOException;

	void writeModel();

	DoccatModel readExistingModel() throws IOException;
}
