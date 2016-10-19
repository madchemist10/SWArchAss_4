package gui;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import constants.Constants;
import org.apache.commons.io.IOUtils;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Command line application that allows the query of data from
 * the Movie database api.
 */
public class CommandLineApp {

    public static void main(String[] args) {
        //main menu loop
        boolean continueMainMenu = true;
        while(continueMainMenu){
            menuSeparator();
            displayMainMenu();
            try {
                continueMainMenu = handleMainMenuRequest(userInput());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Used at beginning of new menu to separate from previous menu.
     */
    private static void menuSeparator(){
        output("---------------------------------");
    }

    /**
     * Output a line of text to the user followed by newline char.
     * @param str string to display to the user.
     */
    private static void output(String str){
        System.out.println(str);
    }

    /**
     * Retrieve user input for a given task.
     * @return String representation of the user's input.
     */
    private static String userInput(){
        Scanner input = new Scanner(System.in);
        return input.nextLine();
    }

    /**
     * Display the main menu.
     */
    private static void displayMainMenu(){
        output("Welcome to Movie App.");
        output("0. Get Top Rated");
        output("1. Get Now Playing");
        output("2. Get Upcoming");
        output("Exit.");
    }

    /**
     * Handle main menu requests from the user.
     * @param choice from the user. Act only on valid choices.
     * @return true to continue main menu, false to stop and exit.
     * @throws Exception if json node parsing fails.
     */
    private static boolean handleMainMenuRequest(String choice) throws Exception{
        String url = null;
        switch(choice){
            case "Exit":
            case "exit":
                return false;
            case "0":
                url = buildSimpleMovieStatement(Constants.TOP_RATED);
                break;
            case "1":
                url = buildSimpleMovieStatement(Constants.NOW_PLAYING);
                break;
            case "2":
                url = buildSimpleMovieStatement(Constants.UPCOMING);
                break;
        }
        String result = getRequest(url);
        JsonNode rootNode = buildJsonNode(result);
        printJsonRootNodeMovies(rootNode);
        processSearch(rootNode);
        return true;
    }

    /**
     * Process a user's keyword to search for.
     * @throws Exception if the retrieval of nodes fails.
     */
    private static void processSearch(JsonNode rootNode) throws Exception{
        menuSeparator();
        output("Enter search term: ");
        String searchTerm = userInput();
        //retrieve list of overviews that have tag searched
        ArrayList<JsonNode> overviewList = parseTagFromResult(rootNode,searchTerm,Constants.OVERVIEW_JSON);
        //retrieve list of titles that have tag searched
        ArrayList<JsonNode> titleList = parseTagFromResult(rootNode,searchTerm,Constants.TITLE_JSON);
        ArrayList<JsonNode> combinedList = new ArrayList<>();
        //create the combined list to maintain order of the original search
        combinedList.addAll(titleList);
        for(JsonNode node: overviewList){
            if(!combinedList.contains(node)){
                combinedList.add(node);
            }
        }
        //display each json node.
        combinedList.forEach(CommandLineApp::printJsonNode);
    }

    /**
     * Print all movie titles for a given jsonRoot node.
     * @param rootNode to have all movies printed from.
     */
    private static void printJsonRootNodeMovies(JsonNode rootNode){
        JsonNode resultsNode = rootNode.get(Constants.RESULTS_JSON);
        for (JsonNode nextNode : resultsNode) {
            output(nextNode.get(Constants.TITLE_JSON).asText());
        }
    }

    /**
     * Print a movie's title, release date, and overview.
     * @param node that contains the movie's data.
     */
    private static void printJsonNode(JsonNode node){
        String title = node.get(Constants.TITLE_JSON).asText();
        String releaseDate = node.get(Constants.RELEASE_JSON).asText();
        String overview = node.get(Constants.OVERVIEW_JSON).asText();
        output(title+":\t"+releaseDate+":\t"+overview);
    }

    /**
     * Construct a simple movie statement for determining the url
     * for a given query type.
     * {@link Constants#NOW_PLAYING}
     * {@link Constants#UPCOMING}
     * {@link Constants#TOP_RATED}
     * @param queryType type of query to perform.
     * @return String format of the url to be used in the get request.
     */
    private static String buildSimpleMovieStatement(String queryType){
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
