package jnetention.run;

import jnetention.Core;
import static javafx.application.Application.launch;
import jnetention.gui.javafx.NetentionJFX;

/**
 * Runs in-memory only (no disk saving)
 */
public class RunDisk extends NetentionJFX {

    @Override
    protected Core newCore(Parameters p) {
        String filePath = "database";
        return new Core(filePath);
    }
    
    public static void main(String[] args) {
        launch(args);
    }
    
}
