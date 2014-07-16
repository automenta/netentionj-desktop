/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention;

import com.google.common.collect.LinkedHashMultimap;
import java.io.Serializable;

/**
 *
 * @author me
 */
public class Value implements Serializable {
    
    public String id;
    public LinkedHashMultimap<String, Object> value;
    
}
