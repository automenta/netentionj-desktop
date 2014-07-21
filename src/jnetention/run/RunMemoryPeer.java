/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.run;

import javafx.application.Application;
import static javafx.application.Application.launch;
import jnetention.Core;
import jnetention.gui.javafx.NetentionJFX;
import net.tomp2p.futures.FutureDiscover;

/**
 *
 * @author me
 */
public class RunMemoryPeer extends NetentionJFX {

    @Override
    protected Core newCore(Application.Parameters p) {
        Core c = new Core();
        try {
            try {
                c.online();
            }
            catch (Exception e) {
                c.online(10000 + (int)(Math.random()*2000));
            }
            
            FutureDiscover fd = c.connect();
            fd.awaitUninterruptibly();
            //assert(fd.isSuccess());            

            /*while (true) {
                    for (PeerAddress pa : c.net.peerBean().peerMap().all()) {
                            System.out.println("PeerAddress: " + pa);
                    }
                    Thread.sleep(1500);
            } */
            
        }
        catch (Exception e) {
            System.err.println(e);
            System.exit(1);
        }
        return c;
    }
    
    public static void main(String[] args) {
        launch();        
    }    

}
