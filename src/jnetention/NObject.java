/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention;

import com.google.common.collect.LinkedHashMultimap;
import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author me
 */
public class NObject extends Value implements Serializable, Comparable {
        
    public long createdAt;
    //public long modifiedAt;
    public String name;
    public String author;
    
    public Set<String> getTags() {
        return new HashSet(value.keys());
    }

    public NObject() {
        this("");
    }
    
    public NObject(String name) {
        this(name, UUID.randomUUID().toString());
    }
    
    public NObject(String name, String id) {
        this.name = name;
        this.id = id;
        this.createdAt = System.currentTimeMillis();
        this.value = LinkedHashMultimap.create();        
    }

    public void add(Tag tag) {
        add(tag.toString());
    }
    
    public void add(String tag) {
        add(tag, 1.0);
    }
    
    public void add(String tag, double strength) {
        value.put(tag, strength);
    }

    @Override
    public int compareTo(final Object o) {
        if (o instanceof NObject) {
            NObject n = (NObject)o;
            return id.compareTo(n.id);
        }
        return -1;
    }

    @Override
    public int hashCode() {
        return id.hashCode(); 
    }

    
    public boolean hasTag(String t) {
        return getTags().contains(t);        
    }

    @Override
    public String toString() {
        return id + "," + author + "," + createdAt + "=" + value;
    }
    
    public String toLongString() {
        return id + "," + author + "," + new Date(createdAt).toString() + "=" + value;
    }
    
    
    
    
    
}
