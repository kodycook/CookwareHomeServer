package com.cookware.home.server.WebMediaServer;

/**
 * Created by Kody on 13/08/2017.
 */
public class Movie extends Media{

    public Movie(String mName) {
        super(mName);
    }

    public String toString(){
        return String.format("Movie: %s (%s) -> %s\n\n", super.name, "XXXX", super.primewireUrl);
    }

}
