import com.google.gson.reflect.TypeToken;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import data.Song;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.Normalizer;
import java.util.List;

public class GeniusScraper {

    private static final String BASE_URL = "http://api.genius.com";

    private static List<Song> songs;


    public static void main(String[] args) throws IOException {
        setupApi();
        getSongs();
        getGeniusIDs();
        getLyrics();
//        JSONObject resultObject = getGeniusResult("7 Rings");

//        System.out.println(resultObject);
//        int songID = resultObject.getInt("id");
//        int artistID = resultObject.getJSONObject("primary_artist").getInt("id");
//
//        System.out.println(songID + " " + artistID);
    }

    private static void getLyrics() throws IOException {
        System.out.println("Getting lyrics");
        songs.forEach(song -> {
            if (song.geniusAttributes == null || song.geniusAttributes.rawLyrics == null || song.geniusAttributes.rawLyrics.isEmpty()) {
                Document geniusPage = getDocument("https://genius.com/songs/" + song.songID.geniusId);
                song.geniusAttributes.rawLyrics = geniusPage.getElementsByClass("lyrics").text();
            }
        });
        Main.writeJSONToFile(songs, "songs_with_genius_info.txt");
    }

    private static Document getDocument(String url) {
        for (int i = 0; i < 100; i++) { // Try 100 times, or throw an exception
            try {
                Document document = Jsoup.connect(url).get();
                if (document != null) {
                    return document;
                }
            } catch (IOException e) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
        throw new RuntimeException("The document was never found: " + url);
    }

    private static void getGeniusIDs() throws IOException {
        System.out.println("Getting genius IDs");
        int count = 0;
        int failureCount = 0;
        for (Song song : songs) {
            if (count++ % 100 == 0) {
                System.out.println("Song " + count);
            }
            JSONObject geniusResult = getGeniusResult(song.getSearchName(), song.songID.name, song.artistIDs.get(0).name);
            if (geniusResult == null) {
//                System.out.println(song);
                failureCount++;
                continue;
            }
//            if(!basicallyEqual(geniusResult.getJSONObject("primary_artist").getString("name"), song.artistIDs.get(0).name)){
//                System.out.println(geniusResult);
//            }

            song.songID.geniusId = String.valueOf(geniusResult.getInt("id"));
            song.artistIDs.get(0).geniusId = String.valueOf(geniusResult.getJSONObject("primary_artist").getInt("id"));
        }

        System.out.println("Total: " + songs.size() + ". Failures: " + failureCount);

        Main.writeJSONToFile(songs, "songs_with_genius_ids.txt");
    }

    private static void getSongs() throws IOException {
        System.out.println("Getting songs");
        songs = Main.readJSONFromFile("song_list_with_spotify_attributes.txt", new TypeToken<List<Song>>() {}.getType());
    }

    private static void setupApi() throws IOException {
        BufferedReader br = new BufferedReader(new FileReader("genius_api_key.txt"));
        String accessToken = br.readLine();
        br.close();
        Unirest.setDefaultHeader("Authorization", "Bearer " + accessToken);
    }

    private static JSONObject getGeniusResult(String query, String songName, String artist) {
        while (true) {
            try {
                JSONArray hits = getGeniusSearchHits(query);
//                System.out.println(query + " " + hits.length());

                for (int i = 0; i < hits.length(); i++) {
                    JSONObject result = hits.getJSONObject(i).getJSONObject("result");
                    if (basicallyEqual(result.getJSONObject("primary_artist").getString("name"), artist)) {
                        return result;
                    }
                }

                hits = getGeniusSearchHits(songName);
                for (int i = 0; i < hits.length(); i++) {
                    JSONObject result = hits.getJSONObject(i).getJSONObject("result");
                    if (basicallyEqual(result.getJSONObject("primary_artist").getString("name"), artist)) {
                        return result;
                    }
                }

                return null;
            } catch (UnirestException e) {
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                System.out.println("Error song: " + query);
                return null;
            }
        }
    }

    private static JSONArray getGeniusSearchHits(String query) throws UnirestException {
        HttpResponse<JsonNode> response = Unirest.get(BASE_URL + "/search").queryString("q", query).asJson();
        JsonNode jsonNode = response.getBody();
        return jsonNode.getObject().getJSONObject("response").getJSONArray("hits");
    }

    private static boolean basicallyEqual(String s1, String s2) {
        s1 = Normalizer.normalize(s1.trim().toLowerCase(), Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]", "").replaceAll("&", "and");
        s2 = Normalizer.normalize(s2.trim().toLowerCase(), Normalizer.Form.NFKD).replaceAll("[^\\p{ASCII}]", "").replaceAll("&", "and");
        boolean result = s1.equalsIgnoreCase(s2) || s1.contains(s2) || s2.contains(s1);
//        if(!result){
//            System.out.println(s1 + " " + s2);
//        }
        return result;
    }


}
