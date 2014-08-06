/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.nlp.demo;

import edu.stanford.nlp.trees.Tree;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import jnetention.nlp.NLPClient;
import jnetention.nlp.TextParse;

/**
 *
 * @author me
 */
public class NLPAnalyzer extends Application implements Runnable {
    private static NLPClient client;
    private TextArea input;
    private BorderPane output;
    boolean changed = false;
    
    
    
    int updatePeriodMS = 1000;
    
    @Override
    public void start(Stage primaryStage) {
        input = new TextArea();
        output = new BorderPane();
        
        SplitPane root = new SplitPane();
        root.setDividerPositions(0.5);
        root.getItems().add(input);
        root.getItems().add(output);
        
        input.textProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {                           changed = true;
            }           
        });
        
        Scene scene = new Scene(root, 700, 550);
        
        primaryStage.setScene(scene);
        primaryStage.show();
        
        new Thread(this).start();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        client = new NLPClient("localhost", 8080);
        launch(args);
    }

    @Override
    public void run() {
        while (true) {
            
            if (changed) {
                try {
                    update(input.getText());
                } catch (Exception ex) {
                    Logger.getLogger(NLPAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
                }
                changed = false;
            }
            
            
            try {
                Thread.sleep(updatePeriodMS);
            } catch (InterruptedException ex) {
                Logger.getLogger(NLPAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    protected void update(String text) throws Exception {
        
        TextParse p = client.parse(text);
        
        StringBuilder s = new StringBuilder();
        s.append(p.getDependencies(true).toString());
        s.append('\n');
        //s.append(p.getCorefCluster().toString());
        s.append(p.getNamedEntities());
        s.append('\n');
        s.append(p.getWords());
        s.append('\n');
        s.append(p.getNamedEntities());
        s.append('\n');
        s.append(p.getTrees().stream().map((Tree t) -> (String)(t.pennString()+'\n') ).collect(Collectors.toList()));
        s.append(p.getVerbPhrases());
        s.append(p.getNounPhrases());

                
        
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                
                TextArea ta = new TextArea(s.toString());
                ta.setMaxHeight(Double.MAX_VALUE);
                ta.setMaxWidth(Double.MAX_VALUE);
                
                ScrollPane sp = new ScrollPane(ta);
                sp.setMaxHeight(Double.MAX_VALUE);
                sp.setMaxWidth(Double.MAX_VALUE);
                
                output.setCenter(ta);
            }
            
        });
    }
}
