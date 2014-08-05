/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.nlp;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 *
 * @author me
 */
@WebService(endpointInterface = "jnetention.nlp.CoreNLPService")
public class CoreNLPImpl implements CoreNLPService {
    private final StanfordCoreNLP pipeline;

    public CoreNLPImpl() {
           
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

           
        pipeline = new StanfordCoreNLP(props);
        
    }

        /** Write the object to a Base64 string. */
    private static String toString( Serializable o ) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream( baos );
        oos.writeObject( o );
        oos.close();
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }    
    
    @WebMethod
    @Override
    public String parse(String input) {
        
        System.out.println("in: " + input);

        Annotation annotation = new Annotation(input);
        
        pipeline.annotate(annotation);
        
        /*pipeline.prettyPrint(annotation, out);
        if (xmlOut != null) {
            pipeline.xmlPrint(annotation, xmlOut);
        }

        out.println(annotation.toShorterString());*/
        
        
        System.out.println("  out: " + annotation.toShortString());
        
        try {
            return toString(new TextParse(annotation));
        } catch (IOException ex) {
            Logger.getLogger(CoreNLPImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
}
