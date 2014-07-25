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

package jnetention.run;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;
import jnetention.Core;
import jnetention.gui.javafx.NodeControlPane;
import org.jewelsea.willow.browser.BrowserTab;
import org.jewelsea.willow.browser.BrowserWindow;
import org.jewelsea.willow.browser.LoadingProgressDisplay;
import org.jewelsea.willow.browser.StatusDisplay;
import org.jewelsea.willow.browser.TabManager;
import org.jewelsea.willow.browser.UITab;
import org.jewelsea.willow.navigation.NavTools;
import org.jewelsea.willow.util.DebugUtil;
import org.jewelsea.willow.util.ResourceUtil;
import static org.jewelsea.willow.util.ResourceUtil.getString;

public class WebBrowser extends Application {
    final public static long start = System.currentTimeMillis();
    static {        
        System.out.println("Static start " + (System.currentTimeMillis() - start)/1000.0);
    }
    
    
    public static final String APPLICATION_ICON =
            "WillowTreeIcon.png";
    
    public static final String DEFAULT_HOME_LOCATION =
            "http://news.google.com";
    
    public static final String STYLESHEET =
            "org/jewelsea/willow/willow.css";
    public StringProperty homeLocationProperty = new SimpleStringProperty(DEFAULT_HOME_LOCATION);
    private static final double INITIAL_SCENE_HEIGHT = 600;
    private static final double INITIAL_SCENE_WIDTH = 1121;
    private Node sidebar;                              // sidebar for controlling the app.
    private TabManager tabManager;                        // tab manager for managing browser tabs.
    private BorderPane mainLayout = new BorderPane();     // layout of the browser application.
    private TextField chromeLocField = new TextField();   // current location of the current browser or a value being updated by the user to change the current browser's location.
    // change listeners to tie the location of the current browser to the chromeLocField and vice versa (binding cannot be used because both these values must be read/write).
    private ChangeListener<String> browserLocFieldChangeListener;
    private ChangeListener<String> chromeLocFieldChangeListener;

    private Core core = new Core();

    @Override
    public void start(final Stage stage) throws MalformedURLException, UnsupportedEncodingException {
        System.out.println("WebBrowser.start()" + (System.currentTimeMillis() - start)/1000.0);        
        
        // set the title bar to the title of the web page (if there is one).
        stage.setTitle(getString("browser.name"));

        // initialize the stuff which can't be initialized in the init method due to stupid threading issues.
        tabManager = new TabManager(core, chromeLocField);
        
        
        System.out.println("created tabs" + (System.currentTimeMillis() - start)/1000.0);        
        
        
        //sidebar = SideBar.createSidebar(this);        
        sidebar = new NodeControlPane(core);
        
        System.out.println("created sidebar" + (System.currentTimeMillis() - start)/1000.0);        

        // initialize the location field in the Chrome.
        chromeLocField.setStyle("-fx-font-size: 14;");
        chromeLocField.setPromptText(getString("location.prompt"));
        chromeLocField.setTooltip(new Tooltip(getString("location.tooltip")));
        chromeLocField.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode().equals(KeyCode.ENTER)) {
                go(chromeLocField.getText());
            }
        });

        // setup the main layout.
        HBox.setHgrow(chromeLocField, Priority.ALWAYS);
        final Pane navPane = NavTools.createNavPane(this);
        mainLayout.setTop(navPane);

        System.out.println("navpane added " + (System.currentTimeMillis() - start)/1000.0);

        // add an overlay layer over the main layout for effects and status messages.
        final AnchorPane overlayLayer = new AnchorPane();
        final StackPane overlaidLayout = new StackPane();
        overlaidLayout.getChildren().addAll(mainLayout, overlayLayer);
        overlayLayer.setPickOnBounds(false);

        // monitor the tab manager for a change in the browser window and update the display appropriately.
        tabManager.browserProperty().addListener((observableValue, oldBrowser, newBrowser) ->
                browserChanged(oldBrowser, newBrowser, stage, overlayLayer)
        );

        System.out.println("creating scene " + (System.currentTimeMillis() - start)/1000.0);


        // create the scene.
        final Scene scene = new Scene(
                overlaidLayout,
                INITIAL_SCENE_WIDTH,
                INITIAL_SCENE_HEIGHT
        );
        scene.getStylesheets().add(STYLESHEET);
        //overlaidLayout.setStyle("-fx-background: rgba(100, 0, 0, 0)");

        // set some sizing constraints on the scene.
        overlayLayer.prefHeightProperty().bind(scene.heightProperty());
        overlayLayer.prefWidthProperty().bind(scene.widthProperty());

        System.out.println("scene created " + (System.currentTimeMillis() - start)/1000.0);        
        
        ScrollPane sidebarScroll = new ScrollPane(sidebar);
        sidebarScroll.setFitToHeight(true);
        sidebarScroll.setFitToWidth(true);
        sidebarScroll.setMinWidth(250);
        mainLayout.setLeft(sidebarScroll);

        System.out.println("sidebar added " + (System.currentTimeMillis() - start)/1000.0);        

        // highlight the entire text if we click on the chromeLocField so that it can be easily changed.
        chromeLocField.focusedProperty().addListener((observableValue, from, to) -> {
            if (to) {
                // run later used here to override the default selection rules for the textfield.
                Platform.runLater(chromeLocField::selectAll);
            }
        });

        // make the chrome location field draggable.
        chromeLocField.getStyleClass().add("location-field");
        chromeLocField.setOnDragDetected(mouseEvent -> {
            Dragboard db = chromeLocField.startDragAndDrop(TransferMode.ANY);
            ClipboardContent content = new ClipboardContent();
            content.putString(chromeLocField.getText());
            db.setContent(content);
        });

        // automatically hide and show the sidebar and navbar as we transition in and out of fullscreen.
        /*
        final Button navPaneButton = createNavPaneButton(navPane);
        stage.fullScreenProperty().addListener((observableValue, oldValue, newValue) -> {
            if ((stage.isFullScreen() && getSidebar().getScroll().isVisible()) ||
                    (!stage.isFullScreen() && !getSidebar().getScroll().isVisible())) {
                ((Button) scene.lookup("#sidebarButton")).fire();
            }
            if ((stage.isFullScreen() && navPane.isVisible()) ||
                    (!stage.isFullScreen() && !navPane.isVisible())) {
                navPaneButton.fire();
            }
        });
        */

        // create a new tab when the user presses Ctrl+T
        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.isControlDown() && keyEvent.getCode().equals(KeyCode.T)) {
                tabManager.getNewTabButton().fire();
            }
        });

        //getSidebarDisplay().setMaxWidth(getSidebarDisplay().getWidth());

        // add an icon for the application.
        stage.getIcons().add(ResourceUtil.getImage(APPLICATION_ICON));

        //sidebar.getScroll().setPrefViewportWidth(sidebar.getBarDisplay().getWidth());

        // debugging routine.
        //debug(scene);
    
        // nav to the home location
        
        go(homeLocationProperty.get());
        
        // we need to manually handle the change from no browser at all to an initial browser.
        browserChanged(null, getBrowser(), stage, overlayLayer);

        System.out.println("WebBrowser.start() finished " + (System.currentTimeMillis() - start)/1000.0);        
    
        // show the scene.
        stage.setScene(scene);
        stage.show();

    }

    private void debug(final Scene scene) {
        System.getProperties().list(System.out);
        //ScenicView.show(scene);
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                DebugUtil.dump(scene.getRoot());
            }
        });
    }

    // creates a button to hide and show the navigation pane.
    private Button createNavPaneButton(final Pane navPane) {
        final Button navPaneButton = new Button();

        final DoubleProperty startHeight = new SimpleDoubleProperty();

        // todo java 8 has a weird background issue on resize.
        // hide sidebar.
        final Animation hideNavPane = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
            }

            protected void interpolate(double frac) {
                final double curHeight = startHeight.get() * (1.0 - frac);
                navPane.setPrefHeight(curHeight);   // todo resize a spacing underlay to allow the scene to adjust.
                navPane.setTranslateY(-startHeight.get() + curHeight);
            }
        };
        hideNavPane.onFinishedProperty().set(actionEvent -> navPane.setVisible(false));

        // show sidebar.
        final Animation showNavPane = new Transition() {
            {
                setCycleDuration(Duration.millis(250));
            }

            protected void interpolate(double frac) {
                final double curHeight = startHeight.get() * frac;
                navPane.setPrefHeight(curHeight);
                navPane.setTranslateY(-startHeight.get() + curHeight);
            }
        };

        navPaneButton.setOnAction(actionEvent -> {
            navPane.setMinHeight(Control.USE_PREF_SIZE);

            if (showNavPane.statusProperty().get().equals(Animation.Status.STOPPED) && hideNavPane.statusProperty().get().equals(Animation.Status.STOPPED)) {
                if (navPane.isVisible()) {
                    startHeight.set(navPane.getHeight());
                    hideNavPane.play();
                } else {
                    navPane.setVisible(true);
                    showNavPane.play();
                }
            }
        });

        return navPaneButton;
    }

    /**
     * Handler for when a new browser is switched into the chrome.
     *
     * @param oldBrowser   the old browser we were to displaying (or none if there is no such thing).
     * @param newBrowser   the new browser we are to display.
     * @param stage        the stage displaying the chrome.
     * @param overlayLayer the overlay layer for status and other information in the chrome.
     */
    private void browserChanged(final UITab oldTab, final UITab newTab, final Stage stage, AnchorPane overlayLayer) {

        
        if (oldTab instanceof BrowserTab) {
            BrowserTab oldBrowserTab = (BrowserTab)oldTab;
            BrowserWindow oldBrowser = oldBrowserTab.getBrowser();
            // cleanup the links between the chrome's location field and the old browser's location field.
            if (oldBrowser != null && browserLocFieldChangeListener != null) {
                oldBrowser.getLocField().textProperty().removeListener(browserLocFieldChangeListener);
            }
            if (chromeLocFieldChangeListener != null) {
                chromeLocField.textProperty().removeListener(chromeLocFieldChangeListener);
            }
        }

        
        if (newTab instanceof BrowserTab) {
            BrowserTab newBrowserTab = (BrowserTab)newTab;
            BrowserWindow newBrowser = newBrowserTab.getBrowser();

            // update the stage title to monitor the page displayed in the selected browser.
            // todo hmm I wonder how the listeners ever get removed...
            newBrowser.getView().getEngine().titleProperty().addListener((observableValue, oldTitle, newTitle) -> {
                if (newTitle != null && !"".equals(newTitle)) {
                    stage.setTitle(getString("browser.name") + " - " + newTitle);
                } else {
                    // necessary because when the browser is in the process of loading a new page, the title will be empty.  todo I wonder if the title would be reset correctly if the page has no title.
                    if (!newBrowser.getView().getEngine().getLoadWorker().isRunning()) {
                        stage.setTitle(getString("browser.name") );
                    }
                }
            });

            // monitor the status of the selected browser.
            overlayLayer.getChildren().clear();

            final StatusDisplay statusDisplay = new StatusDisplay(newBrowser.statusProperty());

            //statusDisplay.translateXProperty().bind(getSidebarDisplay().widthProperty().add(20).add(getSidebarDisplay().translateXProperty()));
            //statusDisplay.translateYProperty().bind(overlayLayer.heightProperty().subtract(50));
            //overlayLayer.getChildren().add(statusDisplay);

            // monitor the loading progress of the selected browser.

            statusDisplay.setLoadControl(
                new LoadingProgressDisplay(
                    newBrowser.getView().getEngine().getLoadWorker()
                )
            );

            // make the chrome's location field respond to changes in the new browser's location.
            browserLocFieldChangeListener = (observableValue, oldLoc, newLoc) -> {
                if (!chromeLocField.getText().equals(newLoc)) {
                    chromeLocField.setText(newLoc);
                }
            };
            newBrowser.getLocField().textProperty().addListener(browserLocFieldChangeListener);

            // make the new browser respond to changes the user makes to the chrome's location.
            chromeLocFieldChangeListener = (observableValue, oldLoc, newLoc) -> {
                if (!newBrowser.getLocField().getText().equals(newLoc)) {
                    newBrowser.getLocField().setText(newLoc);
                }
            };
            chromeLocField.textProperty().addListener(chromeLocFieldChangeListener);
            chromeLocField.setText(newBrowser.getLocField().getText());

            // enable forward and backward buttons as appropriate.
            Button forwardButton = (Button) mainLayout.lookup("#forwardButton");
            if (forwardButton != null) {
                forwardButton.disableProperty().unbind();
                forwardButton.disableProperty().bind(newBrowser.getHistory().canNavForwardProperty().not());
            }
            Button backButton = (Button) mainLayout.lookup("#backButton");
            if (forwardButton != null) {
                backButton.disableProperty().unbind();
                backButton.disableProperty().bind(newBrowser.getHistory().canNavBackwardProperty().not());
            }

            // display the selected browser.
            mainLayout.setCenter(newBrowser.getView());
            mainLayout.setBottom(statusDisplay);
        }
        else if (newTab instanceof UITab) {
            mainLayout.setCenter(newTab.content());            
        }
    }

    public UITab getBrowser() {
        return tabManager.getBrowser();
    }

    public Node getSidebar() {
        return sidebar;
    }

    public Node getSidebarDisplay() {
        return sidebar;
    }

    public TextField getChromeLocField() {
        return chromeLocField;
    }

    public TabManager getTabManager() {
        return tabManager;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void go(String url) {
        if (url.startsWith("about:")) {
            UITab u = new UITab(core, new Label("test"));
            getTabManager().addTab(u);
        }
        else { 
            if (getBrowser() instanceof BrowserTab)
                ((BrowserTab)getBrowser()).getBrowser().go(url);
            else {
                //create a new tab because we were in a  UITab (not BrowserTab)
                BrowserTab bt = new BrowserTab(core, tabManager);
                getTabManager().addTab(bt);
                bt.getBrowser().go(url);
            }
                
        }
    }

}
