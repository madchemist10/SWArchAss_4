package gui;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Constants;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.ArrayList;

/**
 */
public class CommandLineApp {

    public static void main(String[] args) {
        try {
            String url = buildSimpleQueryStatement(Constants.UPCOMING);
            String body = getRequest(url);
            JsonNode node = buildJsonNode(body);
            ArrayList<JsonNode> overviewFound = parseTagFromResult(node, "reacher", Constants.OVERVIEW_JSON);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Construct a simple query statement for determining the url
     * for a given query type.
     * {@link Constants#NOW_PLAYING}
     * {@link Constants#UPCOMING}
     * {@link Constants#TOP_RATED}
     * @param queryType type of query to perform.
     * @return String format of the url to be used in the get request.
     */
    private static String buildSimpleQueryStatement(String queryType){
        return Constants.MOVIE_DB_ADR+"/3/movie/"+queryType+"?api_key="+ Constants.API_KEY;
    }

    /**
     * Perform the request for a given url.
     * @param url that is to be queried.
     * @return String representation of the body of the return from the
     *      get request.
     * @throws Exception if connection fails during connecting or transmission.
     */
    private static String getRequest(String url) throws Exception{
        //http://stackoverflow.com/questions/5769717/how-can-i-get-an-http-response-body-as-a-string-in-java
        URL myURL = new URL(url);
        URLConnection connection = myURL.openConnection();
        InputStream inputStream = connection.getInputStream();
        String encoding = connection.getContentEncoding();
        encoding = encoding == null ? "UTF-8" : encoding;
        return IOUtils.toString(inputStream, encoding);
    }

    /**
     * Construct a base level json node node from a given
     * result that is returned from the {@link #getRequest(String)}
     * method.
     * @param result returned from the {@link #getRequest(String)}
     * @return JsonNode build from the object mapper.
     * @throws Exception if the object mapper fails to read value from
     *      result.
     */
    private static JsonNode buildJsonNode(String result) throws Exception{
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(result,JsonNode.class);
    }

    /**
     * Parse a specific tag from a given result.
     * Attempting to retrieve the node that contains the movie
     * the user is wanting to view.
     * @param node retrieved from the {@link #buildJsonNode(String)} method.
     * @param search string to search through a given tag for a match.
     * @param tagToParse is the tag key that needs a value parsed.
     * @return JsonNode[] of all nodes that contain the search term.
     * @throws Exception if the retrieval of nodes fails.
     */
    private static ArrayList<JsonNode> parseTagFromResult(JsonNode node, String search, String tagToParse) throws Exception{
        ArrayList<JsonNode> jsonNodes = new ArrayList<>();
        JsonNode resultsNode = node.get(Constants.RESULTS_JSON);
        Iterator<JsonNode> results = resultsNode.elements();
        while(results.hasNext()){
            JsonNode nextResult = results.next();
            JsonNode overviewNode = nextResult.get(tagToParse);
            String overview = overviewNode.textValue();
            if(overview.toLowerCase().contains(search.toLowerCase())){
                jsonNodes.add(nextResult);
            }
        }
        return jsonNodes;
    }
}
