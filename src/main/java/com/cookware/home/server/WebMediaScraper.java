package com.cookware.home.server;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

public class WebMediaScraper {

    public WebMediaScraper() {

    }

    public Media findMedia(String urlToRead, ArrayList<HttpParameter> params) throws Exception{
        Scanner consoleScanner = new Scanner(System.in);

        String charset = "UTF-8";  // Or in Java 7 and later, use the constant: java.nio.charset.StandardCharsets.UTF_8.name()
        String query = "";
        for (int i = 0; i < params.size(); i++){
            query += params.get(i).key + '=' + params.get(i).value;
            if(i != params.size()-1){
                query += '&';
            }
        }
        String outUrl = urlToRead + "/index.php?" + query;

        System.out.println(outUrl);

        URLConnection connection = new URL(outUrl).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);
        InputStream response = connection.getInputStream();

        String html = "";
        try (Scanner scanner = new Scanner(response)) {
            html = scanner.useDelimiter("\\A").next();
        }

        Document document = Jsoup.parse(html);
        //System.out.println(document.toString());

        System.out.println("\n");

        Elements matchedMovies = document.getElementsByClass("index_item");

        if(matchedMovies.isEmpty()){
            System.out.println("No entries found, please try again!");

            return null;
        }

        int i = 1;
        String title;
        for (Element movie : matchedMovies) {
            title = movie.getElementsByAttribute("href").attr("title").substring(6);
            System.out.println(String.format("Op %d: %s", i, title));
            i++;
        }

        System.out.print("\nSelect an Option: Op ");
        String option = consoleScanner.next();

        title = matchedMovies.get(Integer.parseInt(option)-1).getElementsByAttribute("href").attr("title");
        title = title.substring(6, title.length()-7);

        String outUrl2 = urlToRead + matchedMovies.get(Integer.parseInt(option)-1).getElementsByAttribute("href").attr("href");
        System.out.println(outUrl2);

        connection = new URL(outUrl2).openConnection();
        connection.setRequestProperty("Accept-Charset", charset);
        response = connection.getInputStream();

        html = "";
        try (Scanner scanner = new Scanner(response)) {
            html = scanner.useDelimiter("\\A").next();
        }

        document = Jsoup.parse(html);
//        System.out.println(document.toString());

        Elements TvSeasons = document.getElementsByClass("tv_container");

        Media media;
        if(TvSeasons.isEmpty()){ //This means that this media is a Movie
            media = new Movie(title);
        }
        else {
            media = new TvShow(title);
            for(Element element : TvSeasons){
                Elements seasons = element.getElementsByClass("show_season");
                for(Element season : seasons){
                    if(Integer.parseInt(season.getElementsByAttribute("data-id").attr("data-id")) != 0){
                        TvShow.Season seasonObj = ((TvShow) media).addSeason();
                        Elements episodes = season.getElementsByClass("tv_episode_item");
                        for(Element episode : episodes){
                            if(Integer.parseInt(episode.getElementsByAttribute("href").text().substring(1,2)) != 0) {
                                TvShow.Episode episodeObj = seasonObj.addEpisode(episode.getElementsByClass("tv_episode_name").text().substring(2));
                                episodeObj.addLink(urlToRead + episode.getElementsByAttribute("href").attr("href"));
                            }
                        }
                    }
                }
            }
        }
        media.addLink(outUrl2);

        return media;
    }
}
