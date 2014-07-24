package jnetention.run;

import jnetention.Core;
import static javafx.application.Application.launch;
import javafx.stage.Stage;
import jnetention.gui.javafx.NetentionJFX;
import jnetention.gui.javafx.NodeControlPane;

/**
 * Runs in-memory only (no disk saving)
 */
public class RunMemory extends NetentionJFX {

    @Override
    protected Core newCore(Parameters p) {
        return new Core();
    }

    @Override
    public void start(Stage primaryStage) {
        super.start(primaryStage); //To change body of generated methods, choose Tools | Templates.
        NodeControlPane.popupObjectEdit(core, core.newObject("X"));
    }
    
    
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
