package MediaManager;

import java.util.ArrayList;
import com.bitlove.fnv.FNV;
import org.apache.log4j.Logger;


/**
 * Created by Kody on 5/09/2017.
 */
public class MediaManager extends Thread{
    private static ArrayList<QueuedMedia> mediaQueue;
    private static DataBaseManager dataBaseManager;
    private FNV stringHasher;
    private Logger log;


    public MediaManager(){
        log = Logger.getLogger(this.getClass());
        stringHasher = new FNV();
        mediaQueue = new ArrayList<QueuedMedia>();
        dataBaseManager = new DataBaseManager("media.db");
    }

    @Override
    public void run()
    {
        // TODO: Add method to load up queue from database

        while(true)
        {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void add(String url, int priority, String quality){


//        log.info("MADE IT");
//        log.info(this.stringHasher.fnv1a_64("blah".getBytes()));

        dataBaseManager.addMedia((int) Math.round(Math.random()*1000), "Kody", url, 240, 0, 1, "01/02/1992", "09/09/2017");

        // TODO: Add item to queue
    }

    public class QueuedMedia {
        int databaseId;
        String name;
        String url;

        public QueuedMedia(){

        }

    }
}
