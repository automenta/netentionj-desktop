/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.LinkedHashMultimap;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    public void add(String tag, Object v) {
        value.put(tag, v);
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
        //TODO optimize with iterator
        return getTags().contains(t);        
    }

    public boolean hasTag(final Tag t) {
        return hasTag(t.toString());
    }
    
    public Set<String> getTags() {
        Set<String> s = new HashSet();
        for (Map.Entry<String, Object> v : value.entries()) {
            if (v.getValue() instanceof Double) {
                s.add(v.getKey());
            }
        }
        return s;
        
    }

    @Override
    public String toString() {
        return id + "," + author + "," + createdAt + "=" + value;
    }
    
    public String toLongString() {
        return id + "," + author + "," + new Date(createdAt).toString() + "=" + value;
    }

    public <X> List<X> values(Class<X> c) {
        List<X> x = new ArrayList();
        for (Object o : value.values()) {
            if (c.isInstance(o))
                x.add((X)o);
        }
        return x;
    }

    public <X> X firstValue(Class<X> c) {
        for (Object o : value.values()) {
            if (c.isInstance(o))
                return (X)o;
        }        
        return null;        
    }

    public boolean isClass() {
        return hasTag(Tag.tag);
    }

    public Map<String, Double> getTagStrengths() {
        Map<String,Double> s = new HashMap();
        for (Map.Entry<String, Object> e: value.entries()) {
            if (e.getValue() instanceof Double) {
                //TODO calculate maximum value if repeating keys?
                s.put(e.getKey(), (Double)e.getValue());
            }
        }
        return s;
    }
    
    
    
    
}
