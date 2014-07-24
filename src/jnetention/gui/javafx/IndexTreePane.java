/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.gui.javafx;

import com.google.common.base.Function;
import static com.google.common.collect.Iterables.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import jnetention.Core;
import jnetention.Core.NetworkUpdateEvent;
import jnetention.Core.SaveEvent;
import jnetention.EventEmitter.Observer;
import jnetention.NObject;
import jnetention.NTag;
import jnetention.gui.javafx.TaggerPane.TagReceiver;


/**
 *
 * @author me
 */
public class IndexTreePane extends BorderPane implements Observer {
    private final TreeView<NObject> tv;
    //http://docs.oracle.com/javafx/2/ui_controls/tree-view.htm
    private final Core core;
    private final TreeItem root;
    private final TagReceiver tagger;

    public IndexTreePane(Core core, TagReceiver tagger) {
        super();
        
        this.core = core;
        this.tagger = tagger;
        
        root = new TreeItem();        
                
        tv = new TreeView(root);
        tv.setShowRoot(false);
        tv.setEditable(false);

        tv.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override public void handle(MouseEvent mouseEvent)    {            
                if(mouseEvent.getClickCount() == 2) {
                    NObject item = tv.getSelectionModel().getSelectedItem().getValue();
                    onDoubleClick(item);
                }
            }
        });
        
        visibleProperty().addListener(new ChangeListener<Boolean>() {
            @Override public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (isVisible()) {
                    addHandlers();
                    update();
                }
                else {
                    core.off(SaveEvent.class, IndexTreePane.this);
                    core.off(NetworkUpdateEvent.class, IndexTreePane.this);
                }
            }
        });
    
        addHandlers();
        update();
        
        
        ScrollPane sp = new ScrollPane(tv);
        sp.setFitToWidth(true);
        sp.setFitToHeight(true);        
        setCenter(sp);
    }

    protected void addHandlers() {
        core.on(SaveEvent.class, IndexTreePane.this);
        core.on(NetworkUpdateEvent.class, IndexTreePane.this);        
    }
    
    @Override public void event(Object event) {
        Platform.runLater(new Runnable() {
            @Override public void run() {
                update();
            }            
        });
    }             
    
    
    protected void update() {        
        root.getChildren().clear();
        for (NObject t : core.getTagRoots()) {
            root.getChildren().add(newTagTree((NTag)t));
        }                
    }
    
    protected TreeItem newTagTree(final NTag t) {
        TreeItem<NObject> i = new TreeItem(t);
                              
        //add instances of the tag        
        addAll(i.getChildren(), transform(core.tagged(t.id), new Function<NObject,TreeItem<NObject>>() {
            @Override public TreeItem apply(final NObject f) {
                return newInstanceItem(f);
            }
        }));
        
        return i;        
    }
    
    protected TreeItem<NObject> newInstanceItem(NObject f) {
        TreeItem<NObject> t = new TreeItem<NObject>(f);        
        return t;
    }
    
    protected void onDoubleClick(NObject item) {
        if (tagger!=null) {
            tagger.onTagSelected(item.id);
        }        
    }
}
