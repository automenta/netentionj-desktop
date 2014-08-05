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

/**
 *
 * @author me
 */
public class NLParse implements Serializable {
    
    public Annotation annotation;

    public NLParse() {     }
    
    public NLParse(Annotation annotation) {
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
        //TODO: System.out.println("sentiment: " + sentence.get(SentimentCoreAnnotations.AnnotatedTree.
        
        return sentence.get(SentimentCoreAnnotations.ClassName.class);
    }
    
   public Set<CoreLabel> getCorefGraph(CoreMap sentence, boolean collapsed) {
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

    public List<String> getVerbs() {
        List<String> a = new ArrayList();
        
        for (CoreLabel c : getTokens()) {
            String p = getPOS(c);
            if (p.startsWith("V"))
                a.add(getWord(c));
        }
        
        return a;            
    }

    
}
