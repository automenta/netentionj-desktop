/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package jnetention.gui.javafx.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.chart.BubbleChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import jnetention.NObject;
import jnetention.SpacePoint;
import jnetention.TimePoint;
import jnetention.TimeRange;
import jnetention.possibility.SpacetimeTagPlan;

/**
 *
 * @author me
 */
public class SpacetimeTagPlanDemo extends Application {
    
    int numObjects = 16;
    int numTags = 4;
    SpacetimeTagPlan p = newExampleSpacetimeTagPlan(numObjects, numTags, true);
    int numCentroids = 2;
    double fuzziness = 1.5;
    final public static long timePeriod = 10*60*1000;
    
    private BorderPane b;
    private BubbleChart<Number, Number> blc;
    private NumberAxis tyAxis;
    private NumberAxis txAxis;
    private BubbleChart<Number, Number> tc;
    private BubbleChart<Number, Number> glc;
    private XYChart.Series series2;
    private XYChart.Series series1;
    private XYChart.Series tseries1;
    private XYChart.Series tseries2;
    private XYChart.Series gseries1;
    private XYChart.Series gseries2;
    private NumberAxis gxAxis;
    

    @Override
    public void start(Stage primaryStage) {
        b = new BorderPane();
        
        StackPane root = new StackPane();
        root.getChildren().add(b);
        
        Scene scene = new Scene(root, 700, 500);
        
        
        primaryStage.setTitle("Spacetime Tag Planning: Demo");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        
        
        //controls
        Slider centroidSlider = new Slider(0, 2.0, 0.3);
        centroidSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                numCentroids = (int)(centroidSlider.getValue() * numObjects);
                numCentroids = Math.max(1, numCentroids);
                
                update();
            }            
        });

        Slider fuzzySlider = new Slider(1.01, 4.0, 1.25);        
        fuzzySlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                fuzziness = fuzzySlider.getValue();
                update();
            }            
        });
        
        Slider spaceSlider = new Slider(0.0, 10.0, 1.0);
        spaceSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                p.setSpaceWeight(spaceSlider.getValue());
                update();
            }            
        });
        Slider timeSlider = new Slider(0.0, 10.0, 1.0);
        timeSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                p.setTimeWeight(timeSlider.getValue());
                update();
            }            
        });
        Slider tagSlider = new Slider(0.0, 10.0, 1.0);
        tagSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                p.setTagWeight(tagSlider.getValue());
                update();
            }            
        });
        
        
        
        FlowPane controls = new FlowPane(
                new Label("Centroids:"), centroidSlider, 
                new Label("Fuzziness:"), fuzzySlider,
                new Label("SpaceWeight:"), spaceSlider,
                new Label("TimeWeight:"), timeSlider,                     
                new Label("TagWeight:"), tagSlider
        );
        b.setBottom(controls);
    
        newCharts();
        update();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
        
    public void newCharts() {
        final NumberAxis xAxis = new NumberAxis(40, 41, 0.1);
        xAxis.setLabel("lon");
        final NumberAxis yAxis = new NumberAxis(40, 41, 0.1);                
        yAxis.setLabel("lat");        
        blc = new BubbleChart<Number,Number>(xAxis,yAxis);
        
        tyAxis = new NumberAxis(0,1,0.1);
        txAxis = new NumberAxis(0,1,0.1);        
        txAxis.setLabel("time");
        tc = new BubbleChart<Number,Number>(txAxis,tyAxis);
        
        
        gxAxis = new NumberAxis(-1, 1 + numObjects + numCentroids, 1);
        gxAxis.setLabel("goal | possibility");
        final NumberAxis gyAxis = new NumberAxis(-1, 1 + numTags, 1);
        gyAxis.setLabel("tag");      
        glc = new BubbleChart<Number,Number>(gxAxis,gyAxis);
        
        HBox charts = new HBox(blc, tc, glc);
        b.setCenter(charts);        
        
        series1 = new XYChart.Series();
        series1.setName("Goals(lat,lon)");
        series2 = new XYChart.Series();
        series2.setName("Possibilities(lat,lon)");       
        blc.getData().setAll(series1, series2);
        
        tseries1 = new XYChart.Series();
        tseries1.setName("Goals(t)");                        
        tseries2 = new XYChart.Series();
        tseries2.setName("Possibilities(t)");       
        tc.getData().setAll(tseries1, tseries2);
        
        gseries1 = new XYChart.Series();
        gseries1.setName("Goals(tag)");
        gseries2 = new XYChart.Series();
        gseries2.setName("Possibilities(tag)");       
        glc.getData().setAll(gseries1, gseries2);        
        
        
    }
    
    public void update() {
        
        List<SpacetimeTagPlan.Possibility> possibilities = p.compute(numCentroids, fuzziness);        
                
        //space chart             
        if (series1.getData().size() == 0) {        
            for (NObject n : p.objects) {
                SpacePoint s = SpacePoint.get(n);
                series1.getData().add(new XYChart.Data(s.lon, s.lat, 0.03));
            }
        }

        series2.getData().clear();
        for (SpacetimeTagPlan.Possibility n : possibilities) {
            SpacePoint s = SpacePoint.get(n);
            series2.getData().add(new XYChart.Data(s.lon, s.lat, 0.05));            
        }
                

        
        //time chart        
        double maxT = -1, minT = Double.MAX_VALUE;
        
        
        if (tseries1.getData().size() == 0) {
            for (NObject n : p.objects) {
                TimeRange s = TimeRange.get(n);
                for (TimePoint tp : s.discretize(timePeriod)) {
                    double a = tp.at;
                    if (a < minT) minT = a;
                    if (a > maxT) maxT = a;
                    tseries1.getData().add(new XYChart.Data(a, timePeriod, timePeriod/8));
                }
            }
            tyAxis.setLowerBound(-timePeriod*2);
            tyAxis.setUpperBound(timePeriod*2);
            tyAxis.setTickUnit(timePeriod/4);

            txAxis.setLowerBound(minT);
            txAxis.setUpperBound(maxT);
            txAxis.setTickUnit((maxT-minT)/10.0);
            
        }
        
        tseries2.getData().clear();
        for (SpacetimeTagPlan.Possibility n : possibilities) {
            TimePoint s = TimePoint.get(n);
            tseries2.getData().add(new XYChart.Data(s.at, -timePeriod, timePeriod/2));
        }


        
                
        

        //tag chart       
        gxAxis.setUpperBound(1 + numObjects + numCentroids);
        
        int goal = 0;
        if (gseries1.getData().size() == 0) {
            for (NObject n : p.objects) {
                Map<String, Double> ts = n.getTagStrengths();
                for (String s : ts.keySet()) {
                    if (s.length() == 1) {
                        int index = s.charAt(0)-'A';
                        gseries1.getData().add(new XYChart.Data(goal, index, ts.get(s)/4.0));
                    }
                }
                goal++;            
            }        
        }
        else {
            goal = p.objects.size();
        }
        
        gseries2.getData().clear();
        for (SpacetimeTagPlan.Possibility n : possibilities) {
            Map<String, Double> ts = n.getTagStrengths();
            for (String s : ts.keySet()) {
                if (s.length() == 1) {
                    int index = s.charAt(0)-'A';
                    gseries2.getData().add(new XYChart.Data(goal, index, ts.get(s)/4.0));
                }
            }
            goal++;
        }
                
        
        
        //--------------
        
        
        b.setTop(new Label("numCentroids=" + numCentroids + ", fuzziness=" + fuzziness + 
                " weight=" + p.getSpaceWeight() + "," + p.getTimeWeight() + "," + p.getTagWeight() ));
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
        
        
        SpacetimeTagPlan s = new SpacetimeTagPlan(n, true, SpacetimeTagPlanDemo.timePeriod, space, false);
        return s;
        
    }
    
    
}
