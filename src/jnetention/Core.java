/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jnetention;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.StandardProtocolFamily;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;
import nars.core.DefaultNARBuilder;
import nars.core.NAR;
import nars.io.Termize;
import nars.io.TextInput;
import nars.io.TextOutput;
import net.tomp2p.connection.Bindings;
import net.tomp2p.connection.DiscoverNetworks;
import net.tomp2p.dht.FutureGet;
import net.tomp2p.dht.FuturePut;
import net.tomp2p.dht.PeerBuilderDHT;
import net.tomp2p.dht.PeerDHT;
import net.tomp2p.futures.FutureDiscover;
import net.tomp2p.p2p.PeerBuilder;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.Number640;
import net.tomp2p.peers.PeerAddress;
import net.tomp2p.peers.PeerMapChangeListener;
import net.tomp2p.peers.PeerStatatistic;
import net.tomp2p.peers.PeerStatusListener;
import net.tomp2p.storage.Data;
import org.apache.commons.math3.stat.Frequency;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * Unifies DB & P2P features
 */
public class Core extends EventEmitter {
    private final static String Session_MYSELF = "myself";
    private final BTreeMap<Object, Object> session;
    private Bindings netBindings;

    public static class SaveEvent {
        public final NObject object;
        public SaveEvent(NObject object) { this.object = object;        }
    }
    public static class NetworkUpdateEvent {        
        private final PeerAddress pa;
        public NetworkUpdateEvent(PeerAddress p) { this.pa = p;        }
    }

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
        logic = new DefaultNARBuilder().
                setConceptBagLevels(400).
                setTaskLinkBagLevels(16).
                setTermLinkBagLevels(16).
                setConceptBagSize(4096).build();
        
        
        new TextOutput(logic, System.out).setErrors(true);
        
        
        this.db = db;
        // open existing an collection (or create new)
        data = db.getTreeMap("objects");
        session = db.getTreeMap("session");

        if (session.get(Session_MYSELF)==null) {            
            //first time user
            become(newUser("Anonymous " + NObject.UUID().substring(0,4)));
        }
        
        
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
        netBindings = new Bindings();
        
        if (listenPort == -1) {
            //netBindings
        }   
        else {
            netBindings.addProtocol(StandardProtocolFamily.INET).addAddress(InetAddress.getByName("127.0.0.1"));
            
        }
        
        net = new PeerBuilder(new Number160(r)).ports(listenPort).bindings(netBindings).enableBroadcast(true).start();
        dht = new PeerBuilderDHT(net).start();
        
        
        //net.getConfiguration().setBehindFirewall(true);                
        
        System.out.println("Server started listening to " + DiscoverNetworks.discoverInterfaces(netBindings));
	System.out.println("Accessible to outside networks at " + net.peerAddress());        
        
        emit(NetworkUpdateEvent.class, net.peerAddress());
        

        net.peerBean().addPeerStatusListeners(new PeerStatusListener() {

            @Override
            public boolean peerFailed(PeerAddress pa, PeerStatusListener.FailReason fr) {
                //System.out.println("peer FAILED: " + pa + " " + fr);
                return true;
            }

            @Override
            public boolean peerFound(PeerAddress pa, PeerAddress referrer) {
                //System.out.println("peer found: " + pa);
                emit(NetworkUpdateEvent.class, pa);
                
                /*if (!pa.equals(net.peerAddress()))
                    broadcast(myself);*/
                
                return true;
            }
            
        });
        
        net.peerBean().peerMap().addPeerMapChangeListener(new PeerMapChangeListener() {
            @Override
            public void peerInserted(PeerAddress pa, boolean verified) {
                if (verified) {
                    //System.out.println("peer inserted: " + pa);
                    emit(NetworkUpdateEvent.class, pa);
                }
            }

            @Override
            public void peerRemoved(PeerAddress pa, PeerStatatistic ps) {
                //System.out.println("peer removed: " + pa + " "  + ps);
                emit(NetworkUpdateEvent.class, pa);                
            }

            @Override
            public void peerUpdated(PeerAddress pa, PeerStatatistic ps) {
                //System.out.println("peer updated: " + pa + " "  + ps);
                //System.out.println("DHT size=" + dht.storageLayer().get().size());
                emit(NetworkUpdateEvent.class, pa);
            }
        });
        
        return this;
    }
    
    /** online, listening at the wildcard address (max 1 per host) */
    public Core online() throws IOException {
        return online(-1);
    }

    
    public FutureDiscover connect() throws UnknownHostException {        
        return net.discover().inetAddress(net.peerAddress().inetAddress()).start();
    }

    public FutureDiscover connect(String host, int port) throws UnknownHostException {
        InetAddress address = Inet4Address.getByName(host);
        return net.discover().inetAddress(address).ports(port).start();
    }
    
    public Core offline() {
        net.shutdown();
        return this;
    }

    public Iterable<NObject> netValues() {
        return Iterables.filter(Iterables.transform(dht.storageLayer().get().values(), 
            new Function<Data,NObject>() {
                @Override public NObject apply(final Data f) {
                    try {
                        final Object o = f.object();
                        if (o instanceof NObject) {
                            NObject n = (NObject)o;
                            if (data.containsKey(n.id))
                                return null;                                
                            return n;
                        }
                        return null;
                    } catch (Exception ex) {
                        return null;
                    }
                }                
        }), Predicates.notNull());        
    }
    
    public Iterable<NObject> allValues() {
        if (net!=null) {
            return Iterables.concat(data.values(), netValues());
        }
        else {
            return data.values();
        }
    }
    
    public Iterable<NObject> tagged(final String tagID) {
        return Iterables.filter(allValues(), new Predicate<NObject>(){
            @Override public boolean apply(final NObject o) {
                return o.hasTag(tagID);
            }            
        });        
    }    
    public Iterable<NObject> tagged(final Tag t) {
        return tagged(t.name());
    }
    
    public List<NObject> getUsers() {        
        return Lists.newArrayList(tagged(Tag.User));
    }
    
    public List<NObject> getTags() {         
        List<NObject> c = Lists.newArrayList(tagged(Tag.tag));
        
        for (Tag sysTag : Tag.values())
            c.add(NTag.asNObject(sysTag));
        
        return c;
    }
    
    public NObject newUser(String name) {
        NObject n = new NObject(name);
        n.author = n.id;
        n.add(Tag.User);
        n.add(Tag.Human);
        n.add("@", new SpacePoint(40, -80));
        publish(n);
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
        //System.out.println("Become: " + user);
        myself = user;
        session.put(Session_MYSELF, user.id);
    }

    
    public void remove(String nobjectID) {
        data.remove(nobjectID);
    }
    
    public void remove(NObject x) {
        remove(x.id);        
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
   
   protected FuturePut netPut(String id, Object o) throws IOException  {
        if (dht == null) return null;
       
        Number160 mkey = Number160.createHash(id);
        FuturePut p = dht.put(mkey).object(o).start();
        /*p.awaitUninterruptibly();
        if (p.isSuccess())*/ 
        return p;
    }   
   protected FuturePut netPut(String id, Object key, Object value) throws IOException  {
        if (dht == null) return null;
       
        Number160 mkey = Number160.createHash(id);
        FuturePut p = dht.put(mkey).data(Number160.createHash(key.toString()), new Data(value)).start();
        return p;
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

    
    /** save nobject to database */
    public void save(NObject x) {
        NObject removed = data.put(x.id, x);        
        index(removed, x);
        
        emit(SaveEvent.class, x);
    }
    
    /** batch save nobject to database */    
    public void save(Iterable<NObject> y) {
        for (NObject x : y) {
            NObject removed = data.put(x.id, x);
            index(removed, x);
        }            
        emit(SaveEvent.class, null);
    }

    
    public void broadcast(NObject x) {
        broadcast(x, false);
    }
    public void broadcast(NObject x, boolean block) {
        if (net!=null) {
            try {
                
                
                
                FuturePut fp = netPut(x.id, x);
                if (block)
                    fp.awaitUninterruptibly();

                //add to tag index        
                Collection<String> tags = x.getTags();
                for (String t : tags) {
                    FuturePut ft = netPut(t + ".index", getNetID()+x.id, x.id);
                    if (block)
                        ft.awaitUninterruptibly();
                }
                
                Number160 mkey = Number160.createHash(x.id);
                net.broadcast(mkey).start();
                    
            }
            catch (IOException e) {
                System.err.println("publish: " + e);
            }
        }        
    }
    
    /** save to database and publish in DHT */
    public void publish(NObject x, boolean block) {
        save(x);
    
        broadcast(x, block);
        
        
        //TODO save to geo-index
    }
    public void publish(NObject x) {
        publish(x, false);        
    }
    
    public int getNetID() {
        if (net == null)
            return -1;
        return net.p2pId();
    }

    public NObject getMyself() {
        return myself;
    }

    protected void index(NObject previous, NObject o) {
        if (previous!=null) {
            if (previous.isClass()) {
                
            }
        }
        
        if (o!=null) {
            
            if ((o.isClass()) || (o.isProperty())) {
                String clas = n(o.id);                
                for (Map.Entry<String, Object> e : o.value.entries()) {
                    String superclass = e.getKey();
                    if (superclass.equals("tag"))
                        continue;
                    
                    if (getTag(superclass)==null) {
                        save(new NTag(superclass));
                    }
                    
                    Double strength = (Double)e.getValue();
                    double freq = (0.5 + strength/2.0) * (1.0);
                    double conf = 0.95;
                    
                    String s = "<" + n(clas) + " --> " + n(superclass) + ">. %" + freq + ";" + conf + "%";
                    new TextInput(logic, s);
                    think();

                    if (o.isProperty()) {
                        if (o instanceof NProperty) {
                            NProperty p = (NProperty)o;
                            for (String d : p.domain) {
                                knowProduct(d, p.id, "property", freq, conf, 0.9);
                            }
                        }
                    }
                }
                
            }
            
        }
        
    }

    public void knowSimilar(String a, String b, double freq, double conf) {
        String s = "<" + n(a) + " <-> " + n(b) + ">. %" + freq + ";" + conf + "%";
        logic.addInput(s);
        think();
    }
    public void knowProduct(String a, String b, String clas, double freq, double conf, double priority) {
        String s = "$" + priority + "$ <(*," + n(a) + "," + n(b) + ") --> " + clas + ">. %" + freq + ";" + conf + "%";
        logic.addInput(s);
        think();        
    }
    public void knowInherit(String a, String b, double freq, double conf, double priority) {
        String s = "$" + priority + "$ <" + n(a) + " --> " + n(b) + ">. %" + freq + ";" + conf + "%";
        logic.addInput(s);
        think();        
    }
    
    public void think() {
        logic.step(1);
    }

    public static Frequency tokenBag(String x, int minLength, int maxTokenLength) {
        String[] tokens = tokenize(x);
        Frequency f = new Frequency();
        for (String t : tokens) {
            if (t==null) continue;
            if (t.length() < minLength) continue;
            if (t.length() > maxTokenLength) continue;
            t = t.toLowerCase();
            f.addValue(t);            
        }
        return f;
    }

    public static String[] tokenize(String value) {
            String v = value.replaceAll(","," \uFFEB ").
                        replaceAll("\\."," \uFFED").
                        replaceAll("\\!"," \uFFED").  //TODO alternate char
                        replaceAll("\\?"," \uFFED")   //TODO alternate char
                    ;
            return v.split(" ");
        }    
    
    public static String n(String s) {        
        return Termize.enterm(s);
    }

    public Object getTag(String tagID) {
        NObject tag = data.get(tagID);
        if (tag!=null && tag.isClass())
            return tag;
        return null;
    }

    public Iterable<NObject> getTagRoots() {
        return Iterables.filter(getTags(), new Predicate<NObject>() {
            @Override public boolean apply(NObject t) {
                try {
                    NTag tag = (NTag)t;
                    return tag.getSuperTags().isEmpty();
                }
                catch (Exception e) { }
                return false;
            }            
        });
    }
    
}
