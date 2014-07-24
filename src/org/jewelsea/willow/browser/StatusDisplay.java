/*
 * Copyright 2013 John Smith
 *
 * This file is part of Willow.
 *
 * Willow is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Willow is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Willow. If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact details: http://jewelsea.wordpress.com
 */

package org.jewelsea.willow.browser;

import javafx.beans.binding.StringExpression;
import javafx.geometry.Insets;
import javafx.scene.control.ProgressBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

/** a display to monitor status messages from the webview. */
public class StatusDisplay extends HBox {

    private final VBox progressHolder = new VBox();
    
    public StatusDisplay(StringExpression statusProperty) {
        Text statusText = new Text();
        statusText.textProperty().bind(statusProperty);
        HBox.setMargin(statusText, new Insets(1, 6, 3, 6));

        setEffect(new DropShadow());
        getStyleClass().add("status-background");
        
        setVisible(true);

        //statusText.textProperty().addListener((observableValue, oldValue, newValue) ->
                //setVisible(newValue != null && !newValue.equals(""))
        //);
        
        
        getChildren().addAll(statusText, progressHolder);
    }
    
    public void setLoadControl(ProgressBar loadControl) {
        VBox.setMargin(loadControl, new Insets(5, 5, 10, 5));
        progressHolder.getChildren().clear();
        progressHolder.getChildren().add(loadControl);
        
        
    }    

}
