package com.cookware.home.server.WebMediaServer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Hello world!
 *
 */
public class WebMediaDownloader
{
    String moviePath;
    String tvPath;

    public WebMediaDownloader(){
        this.moviePath = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\Movies";
        this.tvPath = "C:\\Users\\maste\\IdeaProjects\\CookwareHomeServer\\TV";
    }

    public WebMediaDownloader(String mTvPath, String mMoviePath){
        this.moviePath = mMoviePath;
        this.tvPath = mTvPath;
    }

    public void newDownload(MediaType type, String downloadUrl, String downloadFilename){
        String downloadFilepath = "";
        if(type == MediaType.MOVIE) {
            downloadFilepath = this.moviePath;
        }
        else if(type == MediaType.TV){
            downloadFilepath = this.tvPath;
        }
        File output = new File(downloadFilepath, downloadFilename);
        try {
            downloadWithHttpClient(downloadUrl, output);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private static void downloadWithHttpClient(String downloadUrl, File outputfile) throws Throwable {
        HttpGet httpget2 = new HttpGet(downloadUrl);
        long startTime = System.currentTimeMillis();
        //httpget2.setHeader("User-Agent", userAgent);

        System.out.println("Executing " + httpget2.getURI());
        HttpClient httpclient2 = new DefaultHttpClient();
        HttpResponse response2 = httpclient2.execute(httpget2);
        HttpEntity entity2 = response2.getEntity();
        if (entity2 != null && response2.getStatusLine().getStatusCode() == 200) {
            long length = entity2.getContentLength();
            InputStream instream2 = entity2.getContent();
            System.out.println("Writing " + length + " bytes to " + outputfile);
            if (outputfile.exists()) {
                outputfile.delete();
            }
            FileOutputStream outstream = new FileOutputStream(outputfile);
            int i = 1;
            try {
                byte[] buffer = new byte[2048];
                int count = -1;
                while ((count = instream2.read(buffer)) != -1) {
                    printProgress(startTime, (int) length/2048+1, i);
                    i++;
                    outstream.write(buffer, 0, count);
                }
                outstream.flush();
            } finally {
                outstream.close();
            }
        }
    }
    private static void printProgress(long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / (current);


        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);
        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [", percent))
                .append(String.join("", Collections.nCopies(percent, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(100 - percent, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies((int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %.2f/%.2fMB ", ((double) current)/512, ((double) total)/512))
                .append(String.format("(%.2fMB/s), ", ((double) current)/512/TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)))
                .append(String.format("ETA: %s", etaHms));

        System.out.print(string);
    }
}



