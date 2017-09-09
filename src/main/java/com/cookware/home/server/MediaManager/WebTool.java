package MediaManager;

import org.apache.log4j.Logger;

/**
 * Created by Kody on 9/09/2017.
 * The Web Tool package takes a single url and provides an array of capabilities (GET/POST requests)
 */
public class WebTool {
    private String url;
    private Logger log;

    public WebTool(String mUrl){
        this.url = mUrl;
    }


}
