import com.google.gson.reflect.TypeToken;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import data.ID;
import data.Song;
import data.SpotifyAttributes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpotifyScraper {

    private static List<Song> songs;

    public static void main(String[] args) throws IOException, SpotifyWebApiException {
        SpotifyUtils.setupApi();
        getSongs();
        getTracks();
        Main.writeJSONToFile(songs, "songs_list_after_track_search.txt");
        getAudioFeatures();

        System.out.println("Saving progress");
        Main.writeJSONToFile(songs, "song_list_with_spotify_attributes.txt");
    }

    private static void getAudioFeatures() throws IOException {
        String[][] ids = new String[songs.size() % 100 == 0 ? songs.size() / 100 : songs.size() / 100 + 1][100];
        System.out.println("Getting audio features. " + ids.length + " arrays of 100 ids each");

        int songNum = 0;
        outerFor:
        for (int arrayNum = 0; arrayNum < ids.length; arrayNum++) {
            for (int idNum = 0; idNum < ids[0].length; idNum++) {
                if (songNum >= songs.size()) {
                    break outerFor;
                }
                ids[arrayNum][idNum] = songs.get(songNum++).songID.spotifyId;
            }
        }

        List<Song> songsRemoved = new ArrayList<>();
        songNum = 0;
        for (int arrayNum = 0; arrayNum < ids.length; arrayNum++) {
            System.out.println("Retrieving features for array " + arrayNum);
            int finalArrayNum = arrayNum;
            AudioFeatures[] audioFeaturesList =
                    SpotifyUtils.requestAndRepeatIfTimeout(() -> SpotifyUtils.spotifyApi.getAudioFeaturesForSeveralTracks(ids[finalArrayNum])
                            .build().execute());
            try {
                for (AudioFeatures audioFeatures : audioFeaturesList) {
                    if (songs.size() <= songNum && audioFeatures == null) { // Hit the end of the list
                        break;
                    }
                    if (songs.get(songNum) != null && audioFeatures != null) {
                        songs.get(songNum).spotifyAttributes.setFeatureData(audioFeatures.getAcousticness(), audioFeatures.getDanceability(),
                                audioFeatures.getEnergy(), audioFeatures.getInstrumentalness(), audioFeatures.getKey(), audioFeatures.getLiveness()
                                , audioFeatures.getLoudness(), audioFeatures.getMode(), audioFeatures.getSpeechiness(), audioFeatures.getTempo(),
                                audioFeatures.getTimeSignature(), audioFeatures.getValence());
                    } else {
                        songsRemoved.add(songs.get(songNum));
                    }
                    songNum++;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        songs.removeAll(songsRemoved);
        System.out.println("Finished getting tracks. Songs removed because of lack of spotify audio feature data: " + songsRemoved.size());
        Main.writeJSONToFile(songsRemoved, "spotify_songs_removed_no_audio_feature_data.txt");
    }

    private static void getSongs() throws IOException {
        System.out.println("Getting songs");
        songs = Main.readJSONFromFile("billboard_deduped_list.txt", new TypeToken<List<Song>>() {}.getType());
    }

    private static void getTracks() throws IOException {
        System.out.println("Getting tracks");
        List<Song> songsRemoved = new ArrayList<>();

        int trackCount = 0;
        for (Song song : songs) {
            final Paging<Track> trackPaging =
                    SpotifyUtils.requestAndRepeatIfTimeout(() -> SpotifyUtils.spotifyApi.searchTracks(song.getSearchName()).limit(1).build()
                    .execute());
            if (trackPaging.getItems().length == 0) {
                songsRemoved.add(song);
                continue;
            }
            Track spotifyTrack = trackPaging.getItems()[0];
            song.artistIDs.clear();
            for (ArtistSimplified artist : spotifyTrack.getArtists()) {
                ID artistID = new ID(artist.getName());
                artistID.spotifyId = artist.getId();
                song.artistIDs.add(artistID);
            }
            song.songID.spotifyId = spotifyTrack.getId();
            song.spotifyAttributes = new SpotifyAttributes();
            song.spotifyAttributes.setTrackData(spotifyTrack.getDurationMs(), spotifyTrack.getIsExplicit(), spotifyTrack.getTrackNumber());

            trackCount++;
            if (trackCount % 100 == 0) {
                System.out.println("Count: " + trackCount);
            }
        }

        songs.removeAll(songsRemoved);
        System.out.println("Finished getting tracks. Songs removed because of lack of spotify track data: " + songsRemoved.size());
        Main.writeJSONToFile(songsRemoved, "spotify_songs_removed_no_track_data.txt");
    }

}
