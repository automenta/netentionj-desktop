/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jnetention;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import nars.core.NAR;
import nars.core.Parameters;
import nars.io.TextInput;
import nars.io.TextOutput;
import net.tomp2p.connection.Bindings;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.storage.Data;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Unifies DB & P2P features
 */
public class Core {

    //LOGIC
    public final NAR logic;
    
    //DATABASE
    public final BTreeMap<String, NObject> data;
    public final DB db;
    
    //P2P
    public net.tomp2p.p2p.Peer net;
    private PeerDHT dht;
    private NObject myself;

    /** in-memory Database */
    public Core() {
        this(DBMaker.newMemoryDirectDB().make());        
        become(newUser("Anonymous"));
    }
    
    /** file Database */
    public Core(String filePath) {
        this(DBMaker.newFileDB(new File(filePath))
                .closeOnJvmShutdown()
                .transactionDisable()
                //.encryptionEnable("password")
                .make());
    }
    
    public Core(DB db) {
        logic = new NAR();
        new TextOutput(logic, System.out);
        
        
        this.db = db;
        // open existing an collection (or create new)
        data = db.getTreeMap("objects");
        
        //    map.put(1, "one");
        //    map.put(2, "two");
        //    // map.keySet() is now [1,2]
        //
        //    db.commit();  //persist changes into disk
        //
        //    map.put(3, "three");
        //    // map.keySet() is now [1,2,3]
        //    db.rollback(); //revert recent changes
        //    // map.keySet() is now [1,2]
        //
        //    db.close();
    }

    
    public Core online(int listenPort) throws IOException {
        
        Random r = new Random();
        Bindings b = new Bindings();
        
        
        net = new PeerBuilder(new Number160(r)).ports(listenPort).bindings(b).enableBroadcast(true).start();
        dht = new PeerBuilderDHT(net).start();
        
        //net.getConfiguration().setBehindFirewall(true);        
        
        
        return this;
    }
    
    public Core offline() {
        net.shutdown();
        return this;
    }
    
    public Collection<NObject> getUsers() {
        return Collections2.filter(data.values(), new Predicate<NObject>(){

            @Override
            public boolean apply(NObject t) {
                return t.getTags().contains(Tag.User.toString());
            }            
        });
    }
    
    public NObject newUser(String name) {
        NObject n = new NObject(name);
        n.author = n.id;
        n.add(Tag.User);
        n.add(Tag.Human);
        n.add("@", new SpacePoint(40, -80));
        save(n);
        return n;
    }
    
    /** creates a new anonymous object, but doesn't publish it yet */
    public NObject newAnonymousObject(String name) {
        NObject n = new NObject(name);
        return n;
    }
    
    /** creates a new object (with author = myself), but doesn't publish it yet */
    public NObject newObject(String name) {
        if (myself==null)
            throw new RuntimeException("Unidentified; can not create new object");
        
        NObject n = new NObject(name);                        
        n.author = myself.id;                
        return n;
    }
    
    public void become(NObject user) {
        myself = user;
    }
    
    /** save nobject to database */
    public void save(NObject x) {
        NObject removed = data.put(x.id, x);
        
        index(removed, x);
    }
    
    public void remove(String nobjectID) {
        data.remove(nobjectID);
    }
    
    public void remove(NObject x) {
        remove(x.id);        
    }

    public FutureDiscover connect(String host, int port) throws UnknownHostException {
        InetAddress address = Inet4Address.getByName(host);
        return net.discover().inetAddress(address).ports(port).start();
    }
    
   protected Object netGet(Number160 hash) throws ClassNotFoundException, IOException  {
        FutureGet g = dht.get(hash).start();
        g.awaitUninterruptibly();
        if (g.isSuccess()) {
            return g.data().object();
        }
        return null;
    }

    public Object netGet(String id) throws ClassNotFoundException, IOException  {
        return netGet(Number160.createHash(id));
    }
   
   protected boolean netPut(String id, Object o) throws IOException  {
       if (dht == null) return false;
       
        FuturePut p = dht.put(Number160.createHash(id)).object(o).start();
        p.awaitUninterruptibly();
        if (p.isSuccess()) {
            return true;
        }
        return false;
    }   
   protected boolean netPut(String id, Object key, Object value) throws IOException  {
        if (dht == null) return false;
       
        FuturePut p = dht.put(Number160.createHash(id)).data(Number160.createHash(key.toString()), new Data(value)).start();
        p.awaitUninterruptibly();
        if (p.isSuccess()) {
            return true;
        }
        
        return false;
    }     
   
    public List<NObject> netGetTagged(String tag) throws IOException, ClassNotFoundException {
        FutureGet g = dht.get(Number160.createHash(tag+".index")).all().start();

        // get can also be used with ranges for content keys
        /*
        FutureGet futureGet2 = peers[peerGet].get(locationKey).from(from).to(to).start();
        futureGet2.awaitUninterruptibly();
        System.out.println("row fetch [" + rowKey1 + "]");
        for (Map.Entry<Number640, Data> entry : futureGet2.dataMap().entrySet()) {
            System.out.println("multi fetch: " + entry.getValue().object());
        }
        */
        
        g.awaitUninterruptibly();
        if (g.isSuccess()) {
            final List<NObject> s = new ArrayList(g.dataMap().size());
            for (final Map.Entry<Number640, Data> entry : g.dataMap().entrySet()) {
                String nid = entry.getValue().object().toString();
                Object o = netGet(nid);
                if (o instanceof NObject)
                s.add((NObject)o);
            }            
            return s;
        }
        return null;
    }

    /** save to database and publish in DHT */
    public void publish(NObject x) {
        save(x);
        
        if (net!=null) {
            try {
                netPut(x.id, x);

                //add to tag index        
                for (String t : x.getTags()) {
                    netPut(t + ".index", getNetID()+x.id, x.id);
                }
            }
            catch (IOException e) {
                System.err.println("publish: " + e);
            }
        }
        
        //TODO save to geo-index
    }
    
    public int getNetID() {
        if (net == null)
            return -1;
        return net.p2pId();
    }

    public NObject getMyself() {
        return myself;
    }

    protected void index(NObject previous, NObject next) {
        if (previous!=null) {
            if (previous.isClass()) {
                
            }
        }
        
        if (next!=null) {
            if (next.isClass()) {
                String clas = next.id;
                for (Map.Entry<String, Object> e : next.value.entries()) {
                    if (!(e.getValue() instanceof Double))
                        continue;
                        
                    String superclass = e.getKey();
                    
                    Double strength = (Double)e.getValue();
                    double freq = (0.5 + strength/2.0) * (1.0);
                    double conf = 0.95;
                    
                    String s = "<" + n(clas) + " --> " + n(superclass) + ">. %" + freq + ";" + conf + "%";
                    
                    new TextInput(logic, s);
                    think();
                    
                }
                
            }
        }
        
    }

    public void knowSimilar(String a, String b, double freq, double conf) {
        String s = "<" + n(a) + " <-> " + n(b) + ">. %" + freq + ";" + conf + "%";
        new TextInput(logic, s);
        think();
    }
    public void knowProduct(String a, String b, String clas, double freq, double conf, double priority) {
        String s = "$" + priority + "$ <(*," + n(a) + "," + n(b) + ") --> " + clas + ">. %" + freq + ";" + conf + "%";
        new TextInput(logic, s);
        think();        
    }
    
    public void think() {
        logic.tick();
    }

    public static String n(String s) {
        if (s.indexOf('%')==-1) return s;
        return s.replaceAll("%", "__P");        
    }

    public Object getTag(String tagID) {
        NObject tag = data.get(tagID);
        if (tag!=null && tag.isClass())
            return tag;
        return null;
    }
    
}
