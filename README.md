# osn-nlp
Trainable NLP library that offers conversational interaction.

The underlying NLP engine used is OpenNLP.
With this library you can train 2 models:

1. Doccat model for Intent classification
2. Name Finder model - For Entity classification.

The training data needs to be provided by the user. Each file in the training directory should be a txt file with file name as the intent name.
For eg. In resources/training/ directoyr, the file could be play-music.txt

In the text file, each line should contain a sentence specifying the way the intent can be expressed by the user. Each line may or may not contain entities.
Entities are object types. For example, an artist can be an entity and Pink Floyd, Pearl Jam, Coldplay can refer to the artist entity.
In order to specify this, the format is: <START: artist> Coldplay <END>

There are some examples provided in res/training/ directory.
