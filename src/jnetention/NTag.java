/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention;

import java.util.List;
import jnetention.NObject;
import jnetention.Tag;

/**
 * Tag = data class
 * @author me
 */
public class NTag extends NObject {
    
    String description;    

    public NTag(String id, String name, String extend) {
        super(name, id);
        

        add(Tag.tag.toString(), 1.0);
        if (extend!=null)
            add(extend, 1.0);
        
    }
        
    public NTag(String id, String name, List<String> extend) {
        this(id, name, (String)null);
        
        for (String c : extend)
            add(c, 1.0);
    }
    
    public void mergeFrom(NTag c) {
        //TODO
    }

    
}
