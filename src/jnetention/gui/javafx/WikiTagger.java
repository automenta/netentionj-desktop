/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jnetention.gui.javafx;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker.State;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

/**
 *
 * @author me
 */
public class WikiTagger extends BorderPane {

    public final int TIMEOUT_MS = 15 * 1000;
    private final WebView webview;
    private final WebEngine webEngine;

    public WikiTagger(String startURL) {
        super();

        webview = new WebView();
        webEngine = webview.getEngine();
        webEngine.getLoadWorker().stateProperty().addListener(new ChangeListener<State>() {

            @Override
            public void changed(ObservableValue<? extends State> ov, State oldState, State newState) {
                if (newState == State.SUCCEEDED) {
                    processPage();
                    webview.setVisible(true);
                }
            }
        });
        /*
         webEngine.locationProperty().addListener(new ChangeListener<String>() {
         @Override public void changed(ObservableValue<? extends String> ov, final String oldLoc, final String loc) {
         System.out.println(loc);
         }
         });
         */

        setCenter(webview);

        setTop(newControls());

        loadWikiPage(startURL);

    }

    static String readFile(URI uri, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(uri));
        return new String(encoded, encoding);
    }

    static String jquery = "";

    static {
        try {
            jquery = readFile(WikiTagger.class.getResource("minified-custom.js").toURI(), Charset.defaultCharset());
        } catch (Exception ex) {
            Logger.getLogger(WikiTagger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    protected void processPage() {
        //JSObject win =  (JSObject) webEngine.executeScript("window");
        //win.setMember("app", new JavaApp());


        /*
         webEngine.setJavaScriptEnabled(true);                
         webEngine.executeScript(jquery);
         webEngine.executeScript("var MINI = require('minified'); var _=MINI._, $=MINI.$, $$=MINI.$$, EE=MINI.EE, HTML=MINI.HTML;");
         */
        //String script = "$(function() {";
        String script = "";
        script += "$('body').after('<style>.crb { border: 1px solid #aaa; margin: 2px; padding: 1px; }</style>');";
        script += "$('head, .header, #page-actions, #jump-to-nav, .top-bar, .navigation-drawer, .edit-page').remove();";
        
        //Add tag button to each link
        script += "$('a').each(function() { var t = $(this); var h = t.attr('href'); if (h && h.indexOf('#')!==-1) return; t.addClass('crb'); t.after('<a class=\"crb\" href=\"tag:/' + h + '\">+</a>')});";
        
        //Add Tag button to H1 header of article
        script += "$('#section_0').each(function() { var t = $(this); t.append('<a class=\"crb\" href=\"tag://_\">+</a>')});";
        
        webEngine.executeScript(script);

        //webEngine.setJavaScriptEnabled(false);
        //if ((target.indexOf('Portal:') != 0) && (target.indexOf('Special:') != 0)) {
        //  t.after(newPopupButton(target));
        EventListener listener = new EventListener() {
            @Override
            public void handleEvent(Event ev) {
                String domEventType = ev.getType();
                //System.err.println("EventType: " + domEventType);
                if (domEventType.equals("click")) {
                    String href = ((Element) ev.getTarget()).getAttribute("href");
                                                 
                    if (href.startsWith("tag://")) {
                        tag(href);
                    }
                }
            }
        };

        Document doc = webEngine.getDocument();
        NodeList nodeList = doc.getElementsByTagName("a");
        for (int i = 0; i < nodeList.getLength(); i++) {
            ((EventTarget) nodeList.item(i)).addEventListener("click", listener, false);

        }

    }

//        
//        
//	protected class MyWebViewClient extends WebViewClient {
//
//
//		@Override
//		public boolean shouldOverrideUrlLoading(WebView view, String url) {
//			/*
//			 * if (Uri.parse(url).getHost().equals("www.example.com")) { // This
//			 * is my web site, so do not override; let my WebView load the page
//			 * return false; } // Otherwise, the link is not for a page on my
//			 * site, so launch another Activity that handles URLs Intent intent
//			 * = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
//			 * startActivity(intent); return true;
//			 */
//
//			final String originalURL = url;
//			String urlPrefix = "http://en.m.wikipedia.org/";
//			if (url.startsWith(urlPrefix)) {
//				url = url.substring(urlPrefix.length());
//				final String turl = urlPrefix + "wiki/" + url;
//
//				AlertDialog.Builder builder = new AlertDialog.Builder(TagActivity.this);
//			    builder.setTitle(url);
//			    CharSequence[] items = new CharSequence[8];
//			    items[0] = "Go...";
//			    items[1] = "Learner";
//			    items[2] = "Learner Collaborator";
//			    items[3] = "Collaborator Learner";
//			    items[4] = "Collaborator Teacher";
//			    items[5] = "Teacher Collaborator";
//			    items[6] = "Teacher";
//			    items[7] = "Cancel";
//
//
//			    builder.setItems(items, new DialogInterface.OnClickListener() {
//			           public void onClick(DialogInterface dialog, int which) {
//			        	   if (which == 0) {
//			        		   runOnUiThread(new Runnable() {
//			        			  @Override public void run() {
//					        		  loadWikiPage(turl);			        				
//			        			  } 
//			        		   });
//			        	   }
//			        	   else if (which < 7) {
//			        		   Toast.makeText(TagActivity.this, "Tagged.", Toast.LENGTH_SHORT).show();
//			        	   }
//
//		        		   dialog.dismiss();
//			           }
//			    });
//
//				AlertDialog dialog = builder.create();
//				dialog.show();
//
//				return true;
//			}
//
//			return false;
//
//		}
//	}
//
    public void loadWikiSearchPage(String query) {
        webview.setVisible(false);
        webEngine.load("http://en.m.wikipedia.org/w/index.php?search=" + URLEncoder.encode(query));
    }

    public void loadWikiPage(String urlOrTag) {
        webview.setVisible(false);
        if (urlOrTag.indexOf("/")==-1)
            urlOrTag = "http://en.m.wikipedia.org/wiki/" + urlOrTag;
        webEngine.load(urlOrTag);

		//String url = urlOrTag;
		//String[] sects = url.split("/");
        //String tag = sects[sects.length-1];
                // process page loading
//		try {
//			Document d = Jsoup.parse(new URL(url), TIMEOUT_MS);
//
//			d.select("head").after(
//					"<style>.crb { border: 1px solid gray; }</style>");
//
//			d.select(".header").remove();
//			d.select("#page-actions").remove();
//			//d.select("#contentSub").remove();
//			d.select("#jump-to-nav").remove();
//			//d.select(".IPA").remove();
//			//d.select("script").remove();
//
//			Elements links = d.select("a");
//			for (Element e : links) {
//				String href = e.attr("href");
//				if (href.startsWith("/wiki")) {
//					String target = href.substring(5);
//					e.attributes().put("href", target);
//					e.attributes().put("class", "crb");
//				}
//			}
//			Elements headings = d.select("#section_0");
//			for (Element e : headings) {
//				e.html("<a href='/" + tag + "' class='crb'>" + e.text() + "</a>");
//			}
//
//			webEngine.loadContent(d.html());
//                        
//		} catch (Exception e) {
//			webEngine.loadContent(e.toString());
//		}
    }

//        public void loadWikiPageOLD(String urlOrTag) {
//		webEngine.loadContent("Loading...");
//
//                
//		String url = urlOrTag;
//
//		String[] sects = url.split("/");
//		String tag = sects[sects.length-1];
//
//		try {
//			Document d = Jsoup.parse(new URL(url), TIMEOUT_MS);
//
//			d.select("head").after(
//					"<style>.crb { border: 1px solid gray; }</style>");
//
//			d.select(".header").remove();
//			d.select("#page-actions").remove();
//			//d.select("#contentSub").remove();
//			d.select("#jump-to-nav").remove();
//			//d.select(".IPA").remove();
//			//d.select("script").remove();
//
//			Elements links = d.select("a");
//			for (Element e : links) {
//				String href = e.attr("href");
//				if (href.startsWith("/wiki")) {
//					String target = href.substring(5);
//					e.attributes().put("href", target);
//					e.attributes().put("class", "crb");
//				}
//			}
//			Elements headings = d.select("#section_0");
//			for (Element e : headings) {
//				e.html("<a href='/" + tag + "' class='crb'>" + e.text() + "</a>");
//			}
//
//			webEngine.loadContent(d.html());
//                        
//		} catch (Exception e) {
//			webEngine.loadContent(e.toString());
//		}
//
//	}
//
//	@Override
//	protected void onStart() {
//		super.onStart();
//		   runOnUiThread(new Runnable() {
//			  @Override public void run() {
//				  loadWikiPage("http://en.m.wikipedia.org/wiki/Human");
//			  }
//		   });
//	}
//
//	@Override
//	protected void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.activity_tag);
//
//		// http://developer.android.com/guide/webapps/webview.html
//
//		webview = (WebView) findViewById(R.id.webview);
//		webview.setWebViewClient(new MyWebViewClient());
//
//		//WebSettings webSettings = webview.getSettings();
//		// webSettings.setJavaScriptEnabled(true);
//
//	}
//
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.tag, menu);
//		return true;
//	}
    
    
    public String getCurrentPageTag() {
        String l = webEngine.getLocation();
        int p = l.lastIndexOf('/');
        return l.substring(p+1);        
    }
    
    public void tag(String url) {
        String prefix = "tag://";
        if (!url.startsWith(prefix)) {
            return;
        }
        url = url.substring(prefix.length());

        String wikiPrefix = "wiki/";
        String tag;
        if (url.startsWith(wikiPrefix)) {
            tag = url.substring(wikiPrefix.length());
        }
        else if (url.startsWith("_")) {
            tag = getCurrentPageTag();
        }
        else {
            return;
        }
        
        int hashLocation = tag.indexOf('#');
        if (hashLocation!=-1) {
            tag = tag.substring(0, hashLocation);
        }
        /*
         Stage stage = new Stage();
         Parent root = new Tagger(url);
         stage.setScene(new Scene(root));
         stage.setTitle("My modal window");
         stage.initModality(Modality.WINDOW_MODAL);
         stage.initOwner( getScene().getWindow() );
         stage.showAndWait();    
         */
        setBottom(new OperatorTaggingPane(tag, this) {
            @Override
            public void onFinished(boolean save, String[] tags) {
                WikiTagger.this.setBottom(null);
            }
        });

    }

    protected Node newControls() {
        Button backButton = new Button("Back");
        Button searchButton = new Button("Search");
        TextField searchField = new TextField("");

        BorderPane p = new BorderPane();
        p.setCenter(searchField);
        p.setRight(searchButton);
        p.setLeft(backButton);

        EventHandler<ActionEvent> search = new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                loadWikiSearchPage(searchField.getText());
                searchField.setText("");
            }
        };

        searchButton.setOnAction(search);
        searchField.setOnAction(search);

        return p;
    }

}
