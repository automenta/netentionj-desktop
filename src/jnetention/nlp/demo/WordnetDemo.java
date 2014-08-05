package jnetention.nlp.demo;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @see http://projects.csail.mit.edu/jwi/api/index.html
 */
public class WordnetDemo {

    public static void main(String[] args) throws Exception {


        // construct the dictionary object and open it
        URL u = ClassLoader.getSystemResource("resources/wordnet/dict");
        IDictionary dict = new Dictionary(u);
        dict.open();
        
        // look up first sense of the word "dog"
        IIndexWord idxWord = dict.getIndexWord("computer", POS.NOUN);
        IWordID wordID = idxWord.getWordIDs().get(0);
        IWord word = dict.getWord(wordID);
        System.out.println(word.toString());
        System.out.println("Id = " + wordID);
        System.out.println("Lemma = " + word.getLemma());
        System.out.println("Synset words = " + word.getSynset().getWords());
        System.out.println("Related Synsets = " + word.getSynset().getRelatedSynsets());
        System.out.println("Gloss = " + word.getSynset().getGloss());
        System.out.println("Verbframes = " + word.getVerbFrames());
        System.out.println("POS = " + word.getPOS());
        System.out.println("Related words = " + word.getRelatedMap());
        List<String> relatedWords = word.getRelatedWords().stream().map(
                    (IWordID t) -> (String)dict.getWord(t).getLemma() ).collect(Collectors.toList());
        System.out.println("Related words = " + relatedWords);
        System.out.println("Sensekey = " + word.getSenseKey());
        System.out.println("AdjMarker = " + word.getAdjectiveMarker());

    }
}
