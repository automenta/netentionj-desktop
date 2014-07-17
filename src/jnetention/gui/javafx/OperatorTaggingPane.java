/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.gui.javafx;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 *
 * @author me
 */
public abstract class OperatorTaggingPane extends BorderPane {
    private final WikiTagger outer;

    public OperatorTaggingPane(String tag, final WikiTagger outer) {
        super();
        this.outer = outer;
        autosize();
        
        setPadding(new Insets(4,4,4,4));
        
        Label label = new Label(tag);
        label.setTextOverrun(OverrunStyle.CLIP);
        setTop(label);
        
        /*
        //label.setFont(Font.getDefault().font(24));
        getChildren().add(label);
        String[] tags = new String[]{"Learn", "Do", "Teach", "Can", "Need", "Not"};
        for (String s : tags) {
            CheckBox c = new CheckBox(s);
            getChildren().add(c);
        }
                */
        
        final ToggleGroup gk = new ToggleGroup();

        ToggleButton k1 = new ToggleButton("Learn");
        k1.setToggleGroup(gk);
        ToggleButton k2 = new ToggleButton("Do");
        k2.setToggleGroup(gk);
        ToggleButton k3 = new ToggleButton("Teach");
        k3.setToggleGroup(gk);
        
        HBox k = new HBox(k1,k2,k3);
        
        final ToggleGroup gn = new ToggleGroup();
        
        ToggleButton n1 = new ToggleButton("Can");
        n1.setToggleGroup(gn);
        ToggleButton n2 = new ToggleButton("Need");
        n2.setToggleGroup(gn);
        ToggleButton n3 = new ToggleButton("Not");
        n3.setToggleGroup(gn);
        
        HBox n = new HBox(n1,n2,n3);
        
        VBox c = new VBox(k, n);
        c.setFillWidth(true);                
        setCenter(c);
        
        Button cancelButton = new Button("Cancel");
        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(new EventHandler() {
            @Override public void handle(javafx.event.Event event) { onFinished(true, null); }
        });
        cancelButton.setOnAction(new EventHandler() {
            @Override public void handle(javafx.event.Event event) { onFinished(false, null); }
        });
        FlowPane b = new FlowPane(cancelButton, saveButton);
        b.setAlignment(Pos.BOTTOM_RIGHT);
        setBottom(b);
    }
    
    

    public abstract void onFinished(boolean save, String[] values);
    
}
