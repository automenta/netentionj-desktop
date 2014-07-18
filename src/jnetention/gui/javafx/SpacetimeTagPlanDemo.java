/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.gui.javafx;

import java.util.ArrayList;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jnetention.NObject;
import jnetention.SpacePoint;
import jnetention.TimeRange;
import jnetention.possibility.SpacetimeTagPlan;

/**
 *
 * @author me
 */
public class SpacetimeTagPlanDemo extends Application {
    
    SpacetimeTagPlan p = newExampleSpacetimeTagPlan(10, 2, true);
    int numCentroids = 2;
    double fuzziness = 1.5;
    private BorderPane b;
    

    @Override
    public void start(Stage primaryStage) {
        b = new BorderPane();
        
        StackPane root = new StackPane();
        root.getChildren().add(b);
        
        Scene scene = new Scene(root, 700, 500);
        
        update();
        
        primaryStage.setTitle("Spacetime Tag Planning: Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
        
    public void update() {
        List<SpacetimeTagPlan.Possibility> possibilities = p.compute(numCentroids, fuzziness);        
                
        final NumberAxis xAxis = new NumberAxis(40, 41, 0.1);
        final NumberAxis yAxis = new NumberAxis(40, 41, 0.1);
        
        
        xAxis.setLabel("lon");
        yAxis.setLabel("lat");
       
        XYChart.Series series1 = new XYChart.Series();
        series1.setName("Goals (lat,lon)");
                
        XYChart.Series series2 = new XYChart.Series();
        series2.setName("Possibilities (lat,lon)");

        for (NObject n : p.objects) {
            SpacePoint s = SpacePoint.get(n);
            series1.getData().add(new XYChart.Data(s.lon, s.lat, 0.01));
        }        
        
        for (SpacetimeTagPlan.Possibility n : possibilities) {
            SpacePoint s = SpacePoint.get(n);
            series2.getData().add(new XYChart.Data(s.lon, s.lat, 0.05));
        }
        
        
        

        
        System.out.println(series1.getData());
        System.out.println(series2.getData());
        
        final BubbleChart<Number,Number> blc = new BubbleChart<Number,Number>(xAxis,yAxis);
        blc.getData().addAll(series1, series2);
        blc.setTitle("Spacetime Tag Planning");
        b.setCenter(blc);        
    }
    
    public static SpacetimeTagPlan newExampleSpacetimeTagPlan(int numObjects, int numTags, boolean space) {
        
        List<NObject> n = new ArrayList(numObjects);
        for (int i = 0; i < numObjects; i++) {
            NObject x = new NObject();
            
            //random time within the next few hours
            long now = System.currentTimeMillis();
            long from = (long)(Math.random() * 60 * 60 * 1000);
            long to = from + (long)(Math.random() * 60 * 60 * 1000);
            x.add("when", new TimeRange(now + from, now + to));
            
            if (space) {
                //random space location, with max variation of 1 degree lat/lng
                double lat = Math.random() + 40;
                double lon = Math.random() + 40;            
                x.add("where", new SpacePoint(lat, lon));
            }
            
            //random letters as tags, with random strength
            x.add(String.valueOf((char)('A' + (int)(Math.random()*numTags))), Math.random() );
            
            n.add(x);            
        }
        
        
        SpacetimeTagPlan s = new SpacetimeTagPlan(n, true, 10*60*1000, space, false);
        return s;
        
    }
    
    
}
