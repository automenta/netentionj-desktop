package jnetention.nlp.demo;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import static java.lang.System.out;
import java.util.List;
import java.util.Properties;


public class CoreNLPDemo {       
 
    public static void main(String[] args) {
        Properties props = new Properties();
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref, sentiment");
        
        /*
        boolean caseless = true;
        if (caseless) {
                props.put("","");
                props.put("pos.model","edu/stanford/nlp/models/pos-tagger/english-caseless-left3words-distsim.tagger");
                props.put("parse.model","edu/stanford/nlp/models/lexparser/englishPCFG.caseless.ser.gz ");
                props.put("ner.model","edu/stanford/nlp/models/ner/english.all.3class.caseless.distsim.crf.ser.gz edu/stanford/nlp/models/ner/english.muc.7class.caseless.distsim.crf.ser.gz edu/stanford/nlp/models/ner/english.conll.4class.caseless.distsim.crf.ser.gz ");
        }
                */

   
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);


        Annotation annotation;
        if (args.length > 0) {
            annotation = new Annotation(IOUtils.slurpFileNoExceptions(args[0]));
        } else {
            annotation = new Annotation("This is good.  I am parsing natural language now and can help people.");
        }

        pipeline.annotate(annotation);
        
        /*pipeline.prettyPrint(annotation, out);
        if (xmlOut != null) {
            pipeline.xmlPrint(annotation, xmlOut);
        }

        out.println(annotation.toShorterString());*/
        
        
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences == null) return;
        
        for (CoreMap sentence : sentences) {
      // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(NamedEntityTagAnnotation.class);
                System.out.println(word + " " + pos + " " + ne + " " + token);
            }

            System.out.println("sentiment: " + sentence.get(SentimentCoreAnnotations.AnnotatedTree.class));
            System.out.println("sentiment: " + sentence.get(SentimentCoreAnnotations.ClassName.class));
            
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            tree.pennPrint(out);
            System.out.println(sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class).toString("plain"));

            SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
            System.out.println(graph.toString("plain"));

        }

        
    }
}
