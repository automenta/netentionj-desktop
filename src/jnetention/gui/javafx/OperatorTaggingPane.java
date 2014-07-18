/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.gui.javafx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

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
        label.setFont(label.getFont().font( Font.getDefault().getSize() * 1.4f ));
        label.setTextOverrun(OverrunStyle.CLIP);
        setTop(label);
        
        final ToggleGroup gk = new ToggleGroup();

        ToggleButton k1 = new ToggleButton("Learn");
        k1.setToggleGroup(gk);
        ToggleButton k2 = new ToggleButton("Do");
        k2.setToggleGroup(gk);
        ToggleButton k3 = new ToggleButton("Teach");
        k3.setToggleGroup(gk);
        
        TilePane k = new TilePane(k1,k2,k3);
        
        final ToggleGroup gn = new ToggleGroup();
        
        ToggleButton n1 = new ToggleButton("Can");
        n1.setToggleGroup(gn);
        ToggleButton n2 = new ToggleButton("Need");
        n2.setToggleGroup(gn);
        ToggleButton n3 = new ToggleButton("Not");
        n3.setToggleGroup(gn);
        
        TilePane n = new TilePane(n1,n2,n3);
        
        ComboBox<String> s = new ComboBox<String>();
        s.setTooltip(new Tooltip("Who?"));
        s.getItems().add("Myself");
        s.getSelectionModel().select(0);
        s.setEditable(false);
        
        VBox c = new VBox(k, n);
        c.setAlignment(Pos.CENTER);
        c.setPadding(new Insets(4,4,8,4));
        setCenter(c);
        
        Button cancelButton = new Button("Cancel");
        Button saveButton = new Button("Save");
        saveButton.setDefaultButton(true);
        saveButton.setOnAction(new EventHandler() {
            @Override public void handle(javafx.event.Event event) { 
                List<String> tags = new ArrayList();
                
                ToggleButton selectedK = (ToggleButton)gk.getSelectedToggle();
                ToggleButton selectedN = (ToggleButton)gn.getSelectedToggle();
                if (selectedK!=null)
                    tags.add(selectedK.getText());
                if (selectedN!=null)
                    tags.add(selectedN.getText());
                        
                onFinished(true, null, tags); 
            }
        });
        cancelButton.setOnAction(new EventHandler() {
            @Override public void handle(javafx.event.Event event) { onFinished(false, null, null); }
        });
        
        BorderPane b = new BorderPane();
        b.setLeft(s);
        
        FlowPane fp = new FlowPane(cancelButton, saveButton);
        fp.setAlignment(Pos.BOTTOM_RIGHT);
        
        b.setCenter(fp);
        setBottom(b);
    }
    
    

    public abstract void onFinished(boolean save, String subject, Collection<String>values);
    
}
