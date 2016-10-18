package gui;


import constants.Constants;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 */
public class CommandLineApp {

    public static void main(String[] args) {
        try {
            String url = buildSimpleQueryStatement(Constants.UPCOMING);
            String body = getRequest(url);
            System.out.println(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String buildSimpleQueryStatement(String queryType){
        return Constants.MOVIE_DB_ADR+"/3/movie/"+queryType+"?api_key="+ Constants.API_KEY;
    }

    private static String getRequest(String url) throws Exception{
        URL myURL = new URL(url);
        URLConnection connection = myURL.openConnection();
        InputStream inputStream = connection.getInputStream();
        String encoding = connection.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        return IOUtils.toString(inputStream, encoding);
    }

    private static String parseOverviewFromResult(String result){
        return null;
    }

    private static String parseTitleFromResult(String result){
        return null;
    }
}
