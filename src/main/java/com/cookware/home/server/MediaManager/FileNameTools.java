package com.cookware.home.server.MediaManager;

import com.bitlove.fnv.FNV;
import org.apache.log4j.Logger;

import java.math.BigInteger;

/**
 * Created by Kody on 19/09/2017.
 */
public class FileNameTools {
    private static final Logger log = Logger.getLogger(FileNameTools.class);
    private final FNV stringHasher;

    public FileNameTools() {
        stringHasher = new FNV();
    }

    public BigInteger generateHashFromMediaInfo(MediaInfo info){
        if((info.NAME != "")&&(info.RELEASED != null)) {
            String shortMediaName = info.NAME.replaceAll("\\s", "");
            shortMediaName += "(" + info.RELEASED.getYear() + ")";
            shortMediaName = removeSpecialCharactersFromString(shortMediaName);
            return this.stringHasher.fnv1a_32(shortMediaName.getBytes());
        }
        else{
            return BigInteger.ZERO;
        }
    }

    public String getFullFileNameFromMediaInfo(MediaInfo mediaInfo){
        String mediaFileName = "";
        if(mediaInfo.TYPE.equals(MediaType.EPISODE)){
            // TODO: check if directories exist and create them if not (maybe this should be done during the transfer)
            mediaFileName = String.format("%s - S%dE%d - %s (%d)",
                    mediaInfo.PARENTSHOWNAME,
                    (int) Math.floor(mediaInfo.EPISODE),
                    (int) ((mediaInfo.EPISODE-Math.floor(mediaInfo.EPISODE))*100),
                    mediaInfo.NAME,
                    mediaInfo.RELEASED.getYear());
        }else {
            // TODO: check if directories exist and create them if not (maybe this should be done during the transfer)
            mediaFileName = mediaFileName = String.format("%s (%d)",
                    mediaInfo.NAME,
                    mediaInfo.RELEASED.getYear());
        }
//        log.error(String.format("Issue creating file name for: %s", mediaInfo.toString()));
        log.info(mediaFileName);
        return removeSpecialCharactersFromString(mediaFileName);
    }

    public BigInteger generateHashFromFullFileName(String filePath){
        String[] seperatedFileName = filePath.split(" - ");
        String fileName = seperatedFileName[seperatedFileName.length-1];
        String fileNameWithoutExtension = fileName.substring(0,fileName.lastIndexOf('.'));
        log.debug(String.format("Generating Hash for: %s",fileNameWithoutExtension));

        return generateHashFromGeneralMediaName(fileNameWithoutExtension);
    }

    public BigInteger generateHashFromGeneralMediaName(String name){
        String nameWithoutSpaces = name.replaceAll("\\s", "");
        return this.stringHasher.fnv1a_32(nameWithoutSpaces.getBytes());
    }

    public String removeSpecialCharactersFromString(String inputString){
        return inputString.replaceAll("[\\/:*?\"<>|]","");
    }
}
