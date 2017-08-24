package com.cookware.home.server.WebMediaServer;

import java.util.ArrayList;

/**
 * Created by Kody on 13/08/2017.
 */
public class TvShow extends Media{
    private ArrayList<Season> seasons;

    public TvShow(String mName) {
        super(mName);
        seasons = new ArrayList<Season>();
    }

    public Season addSeason(){
        this.seasons.add(new Season(String.format("Season %d", seasons.size()+1), seasons.size()+1));
        return this.seasons.get(this.seasons.size()-1);
    }

    public ArrayList<Season> getSeasons(){
        return this.seasons;
    }

    public String toString(){
        String output = String.format("TV Show: %s (%s) -> %s\n\n", super.name, "XXXX", super.primewireUrl);
        for(Season season : seasons){
            output += season.toString() + '\n';
        }
        return output;
    }

    public class Season {
        String name;
        int seasonNumber;
        DownloadState state;
        ArrayList<Episode> episodes;

        public Season(String mName, int mSeasonNumber){
            this.name = mName;
            this.seasonNumber = mSeasonNumber;
            this.state = DownloadState.PENDING;
            this.episodes = new ArrayList<Episode>();
        }

        public Episode addEpisode(String name){
            episodes.add(new Episode(name, this.seasonNumber, episodes.size() + 1));
            return this.episodes.get(this.episodes.size()-1);
        }

        public ArrayList<TvShow.Episode> getEpisodes(){
            return this.episodes;
        }

        public String toString(){
            String output = String.format("\t%s\n", this.name);
            for (Episode episode : episodes) {
                output += String.format("\t\t%s\n", episode.toString());
            }
            return output;
        }
    }

    public class Episode extends Media{
        String name;
        int seasonNumber;
        int episodeNumber;
        DownloadState state;
        private String primewireUrl;
        //TODO: Add Date Release and Downloaded (To The TVShow Object and Movie objects as well
//        date dateReleased;
//        date dateDownloaded;

        public Episode(String mName, int mSeasonNumber, int mEpisodeNumber){
            super();
            this.name = mName;
            this.seasonNumber = mSeasonNumber;
            this.episodeNumber = mEpisodeNumber;
            this.state = DownloadState.PENDING;
        }

        public void addLink(String link){
            this.primewireUrl = link;
        }

        public String toString(){
            return String.format("S%02dE%02d - %s -> %s", this.seasonNumber, this.episodeNumber, this.name, this.primewireUrl);
        }
    }
}

