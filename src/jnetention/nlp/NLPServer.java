/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.nlp;

import jnetention.nlp.CoreNLPService;
import jnetention.nlp.CoreNLPImpl;
import com.google.common.collect.Lists;
import java.net.URL;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.xml.namespace.QName;
import javax.xml.ws.Endpoint;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceRef;

/**
 *
 * @author me
 */
public class NLPServer {



    
    public NLPServer(String host, int port) throws Exception {
        String url = "http://" + host + ":" + port + "/corenlp/parse";
        Endpoint.publish(url, new CoreNLPImpl());         
        System.out.println(this + " ready: " + url);       
    }
    public static void main(String[] args) throws Exception {
        new NLPServer("localhost", 8080);
    }        
    
}
