package jnetention.gui;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.swing.SwingUtilities;
import jnetention.Core;
import jnetention.NObject;
import jnetention.gui.swing.SwingMap;
import nars.gui.NARControls;
import org.jxmapviewer.viewer.GeoPosition;

/**
 *
 * @author me
 */
abstract public class NetentionJFX extends Application {


    public Core core;
    private NodeControlPane root;

    
    abstract protected Core newCore(Parameters p);
    
    @Override
    public void start(Stage primaryStage) {
        
        core = newCore(getParameters());
        
        root = new NodeControlPane(core);
        
        
        Scene scene = new Scene(root, 350, 650);
        
        primaryStage.setTitle(core.getMyself().id);
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                System.exit(0);
            }
        });
    }


    static void popupObjectView(Core core, NObject n) {
        Stage st = new Stage();

        BorderPane root = new BorderPane();
        
        WebView v = new WebView();
        v.getEngine().loadContent(ObjectEditPane.toHTML(n));
        
        root.setCenter(v);

        st.setTitle(n.id);
        st.setScene(new Scene(root));
        st.show();
    }
    
}
