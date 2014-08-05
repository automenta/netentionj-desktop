/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.nlp;

import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.ws.Service;

public class NLPClient {
    private final CoreNLPService impl;

    public NLPClient(String host, int port) throws Exception {
        URL url = new URL("http://" + host + ":" + port + "/corenlp/parse");
        
        QName qname = new QName("http://nlp.jnetention/", "CoreNLPImplService");
        Service service = Service.create(url, qname);
        
        impl = service.getPort(CoreNLPService.class);        
    }
    
   /** Read the object from Base64 string. */
   private static Object fromString( String s ) throws IOException, ClassNotFoundException {
        byte [] data = Base64.decode(s);
        ObjectInputStream ois = new ObjectInputStream( new ByteArrayInputStream(  data ) );
        Object o  = ois.readObject();
        ois.close();
        return o;
   }


    public NLParse parse(String input) {
        try {
            if (impl!=null)
                return (NLParse)fromString(impl.parse(input));
            return null;
        } catch (IOException ex) {
            Logger.getLogger(NLPClient.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(NLPClient.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new NLParse();
    }
    
    public static void main(String[] args) throws Exception {
        
        NLPClient client = new NLPClient("localhost", 8080);
        NLParse p = client.parse("This is NetBean's sentence. My sentences include another one.");
        System.out.println(p.annotation);
        System.out.println(p.getSentences());
        System.out.println(p.getNamedEntities());
        System.out.println(p.getVerbs());
        System.out.println(p.getDependencies(false).toFormattedString());
    }    
    
}