/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.nlp;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 *
 * @author me
 */
@WebService
public interface CoreNLPService {

    @WebMethod
    public String parse(String input);
    
}
