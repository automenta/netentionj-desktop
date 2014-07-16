package jnetention.gui.javafx;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javax.swing.SwingUtilities;
import jnetention.Core;
import jnetention.gui.swing.SwingMap;
import jnetention.gui.swing.TimePanel;
import nars.gui.NARControls;

/**
 *
 * @author me
 */
public class NetentionJFX extends Application {

    private Core core;

    
    
    @Override
    public void start(Stage primaryStage) {
        
        core = new Core();
        try {
            core.online(10001);            
        } catch (IOException ex) {
            Logger.getLogger(NetentionJFX.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        TabPane tab = new TabPane();
        tab.getTabs().add(newOptionsTab());        
        tab.getTabs().add(newSpacetimeTab());
        tab.getTabs().add(newSpaceTab());
        tab.getTabs().add(newTimeTab());
        tab.getTabs().add(newIndexTab());        
        tab.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tab.autosize();
        
        StackPane root = new StackPane();
        root.getChildren().add(tab);
        
        
        
        Scene scene = new Scene(root, 350, 650);
        
        primaryStage.setTitle("Netention (JavaFX GUI)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
    public Tab newSpaceTab() {
        Tab t = new Tab("Space");
        
        SwingNode swingMap = new SwingNode();
        swingMap.setContent(new SwingMap());
        t.setContent(swingMap);
        return t;
    }
    public Tab newTimeTab() {
        Tab tab = new Tab("Time");
        
        
        
        SwingNode timeNode = new SwingNode();        
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                timeNode.setContent(new TimePanel());
            }            
        });
        tab.setContent(timeNode);        
        
        
                
        return tab;
    }    
    public Tab newSpacetimeTab() {
        Tab t = new Tab("Spacetime");
        return t;
    }    
    public Tab newOptionsTab() {
        Tab t = new Tab("Options");
        
        
        Accordion a =new Accordion();
        
        a.getPanes().addAll(new TitledPane("Identity", newIdentityPanel()), new TitledPane("Network", newNetworkPanel()), new TitledPane("Logic", newLogicPanel()), new TitledPane("Database", newDatabasePanel()));
        
        for (TitledPane tp : a.getPanes())
            tp.setAnimated(false);
        
        t.setContent(a);
        return t;
    }    
    public Tab newIndexTab() {
        Tab t = new Tab("Index");
        return t;
    }    

    protected Node newNetworkPanel() {
        Pane p = new Pane();
        
        TextArea ta = new TextArea();
        if (core.net!=null) {
            ta.setText(core.net.getClass().getSimpleName());
        }
        else {
            ta.setText("Offline");
        }
        p.getChildren().add(ta);        
        
        return p;
    }
    protected Node newLogicPanel() {        

        
        
        final SwingNode s = new SwingNode();
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                s.setContent(new NARControls(core.logic));
            }            
        });

        ScrollPane x = new ScrollPane(s);
        
        
        BorderPane flow = new BorderPane(x);
        flow.setPadding(new Insets(5, 5, 5, 5));
        flow.autosize();
        
        
        return flow;
    }
    protected Node newIdentityPanel() {
        Pane p = new Pane();
        
        if (core.getMyself()!=null)
            p.getChildren().add(new ObjectCard(core.getMyself()));
                    
        return p;
    }
    protected Node newDatabasePanel() {
        Pane p = new Pane();
        
        TextArea ta = new TextArea();
        ta.setText(core.db.toString() + "\n" + core.db.getEngine().toString() );
        p.getChildren().add(ta);
        
        //core.data.sizeLong()
        
        return p;
    }
    
}
