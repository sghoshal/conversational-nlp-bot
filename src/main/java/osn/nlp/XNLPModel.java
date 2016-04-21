package main.java.osn.nlp;

import opennlp.tools.doccat.DoccatModel;

import java.io.IOException;

public interface XNLPModel
{
	void train( String pathToTrainingData ) throws IOException;

	void writeModel();

	DoccatModel readExistingModel() throws IOException;
}
