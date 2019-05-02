import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import data.Artist;
import data.ID;
import data.Song;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
//        BufferedReader br = new BufferedReader(new FileReader("billboard_deduped_list.txt"));
//        List<Song> songList = gson.fromJson(br, new TypeToken<List<Song>>(){}.getType());
//        br.close();
//
//        System.out.println("Size: " + songList.size());
//        songList.sort((o1, o2) -> {
//            if(o1.billboardPlacements.size() == o2.billboardPlacements.size()){
//                return 0;
//            }
//            return o1.billboardPlacements.size() < o2.billboardPlacements.size() ? 1 : -1;
//        });
//
//        for(int i = 0; i < 11; i++){
//            System.out.println(songList.get(i).songID.name + " - " + songList.get(i).billboardPlacements.size() + ". Most recent: " + songList
//            .get(i).billboardPlacements.getLast());
//        }
//
//        System.out.println();
//        System.out.println(songList.stream().collect(Collectors.summarizingInt(song -> song.billboardPlacements.size())));
//
//        System.out.println();
//        System.out.println(songList.stream().filter(song -> song.billboardPlacements.getFirst().placement == 1).sorted((o1, o2) -> {
//            if(o1.billboardPlacements.getFirst().year == o2.billboardPlacements.getFirst().year){
//                if(o1.billboardPlacements.getFirst().week == o2.billboardPlacements.getFirst().week){
//                    return 0;
//                }
//                return o1.billboardPlacements.getFirst().week > o2.billboardPlacements.getFirst().week ? 1 : -1;
//            }
//           return o1.billboardPlacements.getFirst().year > o2.billboardPlacements.getFirst().year ? 1 : -1;
//        }).map(song -> song.songID.name).collect(Collectors.toList()));

//        List<Song> songs = Main.readJSONFromFile("song_list_with_spotify_attributes.txt", new TypeToken<List<Song>>(){}.getType());
//        System.out.println(songs.stream().filter(song -> song.songID.name.contains("7 Rings")).findFirst().get());

//        for(int year = 1958; year <= 2019; year++){
//            int finalYear = year;
//            System.out.println(year + ": " + songs.stream().mapToLong(song -> song.billboardPlacements.stream().filter(billboardPlacement ->
//            billboardPlacement.year == finalYear).count()).sum());
//        }

//        List<Song> songs = Main.readJSONFromFile("spotify_songs_removed_no_track_data.txt", new TypeToken<List<Song>>(){}.getType());
//        System.out.println(songs.stream().filter(song -> song.artistIDs.get(0).name.contains("feat")).collect(Collectors.toList()));

//        Document geniusPage = Jsoup.connect("https://www.genius.com/songs/1638578").get();
//        Document geniusPage = Jsoup.connect("https://genius.com/Ariana-grande-7-rings-lyrics").get();
//        System.out.println(geniusPage.getElementsByClass("lyrics").text());

        List<Song> billboardSongs = Main.readJSONFromFile("song_list_with_spotify_attributes.txt", new TypeToken<List<Song>>() {}.getType());
        List<Song> nonBillboardSongs = Main.readJSONFromFile("non_billboard_song_list_with_spotify_attributes.txt",
                new TypeToken<List<Song>>() {}.getType());
        ArrayList<Artist> artists = new ArrayList<>();
        for (Song song : billboardSongs) {
            for (ID artistID : song.artistIDs) {
                Artist artist = new data.Artist(artistID);
                if (artists.contains(artist)) {
                    int index = artists.indexOf(artist);
                    artists.get(index).billboardSongIDs.add(song.songID);
                } else {
                    artist.billboardSongIDs.add(song.songID);
                    artists.add(artist);
                }
            }
        }

        for (Song song : nonBillboardSongs) {
            for (ID artistID : song.artistIDs) {
                Artist artist = new data.Artist(artistID);
                if (artists.contains(artist)) {
                    int index = artists.indexOf(artist);
                    artists.get(index).nonBillboardSongIDs.add(song.songID);
                } else {
                    artist.nonBillboardSongIDs.add(song.songID);
                    artists.add(artist);
                }
            }
        }

        writeJSONToFile(artists, "artists.txt");
    }

    public static <T> T readJSONFromFile(String filepath, Type type) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        T object = gson.fromJson(br, type);
        br.close();
        return object;
    }

    public static void writeJSONToFile(Object object, String filepath) throws IOException {
        Path filePath = Paths.get(filepath);
        if (filePath.getParent() != null) {
            Files.createDirectories(filePath.getParent());
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter(filepath));
        bw.write(gson.toJson(object));
        bw.close();
    }
}
