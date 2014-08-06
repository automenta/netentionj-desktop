/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.nlp;


import edu.stanford.nlp.dcoref.CorefCoreAnnotations.CorefClusterAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author me
 */
public class TextParse implements Serializable {
    
    public Annotation annotation;

    public TextParse() {     }
    
    public TextParse(Annotation annotation) {
        this.annotation = annotation;
    }
    
    public List<CoreMap> getSentences() {
        List<CoreMap> l = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (l == null)
            return Collections.EMPTY_LIST;
        return l;
    }

    public static List<CoreLabel> getTokens(CoreMap sentence) {
        List<CoreLabel> c = sentence.get(CoreAnnotations.TokensAnnotation.class);
        if (c == null)
            return Collections.EMPTY_LIST;
        return c;
    }    
    
    public static Tree getTree(CoreMap sentence) {
        return sentence.get(TreeCoreAnnotations.TreeAnnotation.class);        
    }
    
    public static String getSentiment(CoreMap sentence) {       
        return sentence.get(SentimentCoreAnnotations.ClassName.class);
    }
    
    public Set<CoreLabel> getCorefCluster(CoreMap sentence) {
            return sentence.get(CorefClusterAnnotation.class);
    }
   
    public SemanticGraph getDependencies(CoreMap sentence, boolean collapsed) {
        if (collapsed)
            return sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);
        else
            return sentence.get(SemanticGraphCoreAnnotations.BasicDependenciesAnnotation.class);
    }
    
    public String getWord(CoreLabel token) {
        return token.get(CoreAnnotations.TextAnnotation.class);
    }
    public String getPOS(CoreLabel token) {
        return token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
    }
    public String getNamedEntity(CoreLabel token) {
        return token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
    }

    @Override
    public String toString() {
        return annotation.toShortString();
    }

    public SemanticGraph getDependencies(boolean b) {
        List<TypedDependency> l = new ArrayList();        
        for (CoreMap s : getSentences()) {
            SemanticGraph g = getDependencies(s, b);            
            l.addAll(g.typedDependencies());
        }
        SemanticGraph graph = new SemanticGraph(l);
        return graph;
    }
    public List<Set<CoreLabel>> getCorefCluster() {
        List<Set<CoreLabel>> l = new ArrayList();        
        for (CoreMap s : getSentences()) {
            Set<CoreLabel> g = getCorefCluster(s);
            l.add(g);
        }
        return l;
    }

    public List<CoreLabel> getTokens() {
        List<CoreLabel> c = new ArrayList();
        for (CoreMap s: getSentences()) {    
            List<CoreLabel> ts = getTokens(s);
            if (ts!=null)
                c.addAll(ts);
        }
        return c;        
    }
    
    public List<String> getNamedEntities() {
        List<String> a = new ArrayList();
        
        for (CoreLabel c : getTokens()) {                
            String e = getNamedEntity(c);
            if (e!=null)
                a.add(e);
        }       
        return a;
    }
    public List<String> getWords() {
        List<String> a = new ArrayList();
        
        for (CoreLabel c : getTokens())
            a.add(getWord(c));
        
        return a;            
    }
    public List<String> getVerbs() {
        List<String> a = new ArrayList();
        
        for (CoreLabel c : getTokens()) {
            String p = getPOS(c);
            if (p.startsWith("V"))
                a.add(getWord(c));
        }
        
        return a;            
    }

    public List<Tree> getTrees() {
        List<Tree> c = new ArrayList();
        for (CoreMap s: getSentences()) {    
            Tree ts = getTree(s);
            if (ts!=null)
                c.add(ts);
        }
        return c;   
    }



    public static Stream<Tree> getSubTrees(CoreMap sentence, Predicate<Tree> filter) {
        Tree t = getTree(sentence);
        if (t != null)
            return t.stream().filter(filter);                    
        else
            return Stream.empty();
    }
    
    public static Stream<Tree> getPhrases(CoreMap sentence, String label) {
        return getSubTrees(sentence, t -> t.label().value().equals(label));                
    }

    public List<Tree> getNounPhrases() {        
        return getSentences().stream().flatMap(s -> getPhrases(s, "NP")).collect(Collectors.toList());
    }    
    public List<Tree> getVerbPhrases() {        
        return getSentences().stream().flatMap(s -> getPhrases(s, "VP")).collect(Collectors.toList());
    }
    public List<Tree> getAdverbPhrases() {        
        return getSentences().stream().flatMap(s -> getPhrases(s, "ADVP")).collect(Collectors.toList());
    }
    public List<Tree> getBePhrases() {        
        return getSentences().stream().flatMap(s -> getPhrases(s, "ADVP")).collect(Collectors.toList());
    }
    
}
