/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jewelsea.willow.browser;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import jnetention.Core;
import org.jewelsea.willow.navigation.History;

/**
 *
 * @author me
 */
public class UITab<N extends Node> extends Tab {
    public final Core core;
    private final N contnt;
    

    public UITab(Core c, N content) {
        super();
        this.core = c;
        this.contnt = content;
        
    }
    
    public N content() {
        return contnt;
    }    
    
    protected void init() {   }
    
    public History getHistory() {
        return null;
    }
    public void go(String loc) {
        //...
    }

    public String getLocation() {
        return "about:?";
    }
    
    
}
