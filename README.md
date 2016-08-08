This is a command line app that works as a bot one can have a conversation with.
For it to work, the bot needs to be trained recognize sentences.
Also, a conversation context graph can be created that represents conversation flows.
When a chat is initiated, the sentence is parsed as an intent and the entity to be matched is returned along with the reply text.
In the subsequent chat message from the user, the same intent is considered and if the entity parsed matches the
entity to be matched, then the context node is considered as Answered and the next entity to be matched is returned along with
the reply text and this goes on. This is how the graph is traversed.

The underlying NLP engine used is OpenNLP.
With this library you can train 2 models:

1. Doccat model for Intent classification
2. Name Finder model - For Entity classification.

The training data needs to be provided by the user. Each file in the training directory should be a txt file with file name as the intent name.
For eg. In resources/training/ directory, the file could be play-music.txt

In the text file, each line should contain a sentence specifying the way the intent can be expressed by the user. Each line may or may not contain entities.
Entities are object types. For example, an artist can be an entity and Pink Floyd, Pearl Jam, Coldplay can refer to the artist entity.
In order to specify this, the format is: <START: artist> Coldplay <END>

There are some examples provided in res/training/ directory.
