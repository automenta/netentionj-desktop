package jnetention.nlp;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.AdjMarker;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IVerbFrame;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;


public class WordParse implements Serializable {

    static IDictionary dict = null;
    static {
        try {
            URL u = ClassLoader.getSystemResource("resources/wordnet/dict");
            dict = new Dictionary(u);        
            dict.open();
        } catch (IOException ex) {
            Logger.getLogger(WordParse.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }
    
    public final IWord word;
    public final String string;
    
    protected WordParse(String s, IWord word) {
        this.string = s;
        this.word = word;
    }

    /** translate CoreNLP POS string to Wordnet POS */
    public static WordParse getFirst(String wordString, String pos) {
        System.out.println(wordString + " POS: " + pos);
        if (pos.startsWith("N"))
            return getFirst(wordString, POS.NOUN);
        else if (pos.startsWith("V"))
            return getFirst(wordString, POS.VERB);
        else if (pos.equals("JJ"))
            return getFirst(wordString, POS.ADJECTIVE);
        else if (pos.equals("RB"))
            return getFirst(wordString, POS.ADVERB);
        return null;
    }
    
    public static List<WordParse> getAll(String wordString, POS pos) {
        //TODO
        return null;
    }
    
    public static WordParse getFirst(String wordString, POS pos) {
        if (dict == null) {
            return null;            
        }
        
        IIndexWord idxWord = dict.getIndexWord(wordString, pos);
        if (idxWord == null)
            return null;
        
        List<IWordID> words = idxWord.getWordIDs();
        if (words == null)
            return null;
        
        IWordID wordID = idxWord.getWordIDs().get(0);
        IWord word = dict.getWord(wordID);
//        System.out.println(word.toString());
//        System.out.println("Id = " + wordID);
//        System.out.println("Lemma = " + word.getLemma());
//        System.out.println("Synset words = " + word.getSynset().getWords());
//        System.out.println("Related Synsets = " + word.getSynset().getRelatedSynsets());
//        System.out.println("Gloss = " + word.getSynset().getGloss());
//        System.out.println("Verbframes = " + word.getVerbFrames());
//        System.out.println("POS = " + word.getPOS());
//        System.out.println("Related words = " + word.getRelatedMap());
//        Set<String> relatedWords = word.getRelatedWords().stream().map(
//                    (IWordID t) -> (String)dict.getWord(t).getLemma() ).collect(Collectors.toSet());
//        System.out.println("Related words = " + relatedWords);
//        System.out.println("Sensekey = " + word.getSenseKey());
//        System.out.println("AdjMarker = " + word.getAdjectiveMarker());
        
        return new WordParse(wordString, word);
    }

    public Set<String> getRelatedWords() {
        return word.getRelatedWords().stream().map(
                    (IWordID t) -> (String)dict.getWord(t).getLemma() ).collect(Collectors.toSet());
    }
    public Set<ISynset> getRelatedSynsets() {
        return word.getSynset().getRelatedSynsets().stream().map(
                (ISynsetID si) -> dict.getSynset(si) ).collect(Collectors.toSet());        
    }
    
    public String getLemma() {
        return word.getLemma();
    }

    public ISynset getSynset() {
        return word.getSynset();
    }

    public ISenseKey getSenseKey() {
        return word.getSenseKey();
    }

    public int getLexicalID() {
        return word.getLexicalID();
    }

    public Map<IPointer, List<IWordID>> getRelatedMap() {
        return word.getRelatedMap();
    }

    public List<IWordID> getRelatedWords(IPointer ip) {
        return word.getRelatedWords(ip);
    }
//
//    @Override
//    public List<IWordID> getRelatedWords() {
//        return word.getRelatedWords();
//    }

    public List<IVerbFrame> getVerbFrames() {
       return word.getVerbFrames();
    }

    public AdjMarker getAdjectiveMarker() {
        return word.getAdjectiveMarker();
    }

    public POS getPOS() {
        return word.getPOS();
    }

    public IWordID getID() {
        return word.getID();
    }

    @Override
    public String toString() {
        return string + " " + getLemma() + " " + getSenseKey() + " " + getRelatedWords() + " " + getRelatedSynsets();
    }
    
    
    
    
}
