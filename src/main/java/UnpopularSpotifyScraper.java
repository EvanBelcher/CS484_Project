import com.google.gson.reflect.TypeToken;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.specification.Artist;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.Track;
import data.ID;
import data.Song;
import data.SpotifyAttributes;

import java.io.IOException;
import java.util.*;

public class UnpopularSpotifyScraper {

    private static List<Song> billboardSongs;
    private static TreeMap<data.Artist, Artist> artists = new TreeMap<>();
    private static List<Song> nonBillboardSongs = new ArrayList<>();

    public static void main(String[] args) throws IOException, SpotifyWebApiException {
        SpotifyUtils.setupApi();
        getSongs();
        getArtists();
        System.out.println("Billboard songs: " + billboardSongs.size() + ", Artists: " + artists.size());


        getNonBillboardTracks();
        Main.writeJSONToFile(nonBillboardSongs, "non_billboard_songs_list_after_track_search.txt");


        getAudioFeatures();
        System.out.println("Saving progress");
        Main.writeJSONToFile(nonBillboardSongs, "non_billboard_song_list_with_spotify_attributes.txt");
        Main.writeJSONToFile(new ArrayList<>(artists.navigableKeySet()), "artists.txt");
    }

    private static void getArtists() {
        LinkedList<data.Artist> artistList = new LinkedList<>();

        // Get artists (data.Artist) who have Billboard songs
        for (Song song : billboardSongs) {
            for (ID artistID : song.artistIDs) {
                data.Artist artist = new data.Artist(artistID);
                if (artistList.contains(artist)) {
                    int index = artistList.indexOf(artist);
                    artistList.get(index).billboardSongIDs.add(song.songID);
                } else {
                    artist.billboardSongIDs.add(song.songID);
                    artistList.add(artist);
                }
            }
        }

        // Get spotify artists
        for (int i = 0; i < artistList.size(); i += 50) { // Get several artists takes batches of 50 artists.
            int lowerBound = i;
            int upperBound = Math.min(lowerBound + 50, artistList.size());
            System.out.println("Retrieving artists " + lowerBound + " thru " + upperBound);

            final Artist[] spotifyArtists =
                    SpotifyUtils.requestAndRepeatIfTimeout(() -> SpotifyUtils.spotifyApi.getSeveralArtists(artistList.subList(lowerBound,
                            upperBound).stream().map(artist -> artist.artistID.spotifyId).toArray(String[]::new)).build().execute());
            for (int j = 0; j < spotifyArtists.length; j++) {
                artists.put(artistList.get(i + j), spotifyArtists[j]);
            }
        }
    }

    private static void getNonBillboardTracks() {
        System.out.println("Getting non-Billboard tracks");

        int trackCount = 0;
        for (data.Artist billboardArtist : artists.navigableKeySet()) {
            Artist spotifyArtist = artists.get(billboardArtist);
            final Paging<Track> trackPaging =
                    SpotifyUtils.requestAndRepeatIfTimeout(() -> SpotifyUtils.spotifyApi.searchTracks(spotifyArtist.getName()).limit(50).build().execute());

            for (Track track : trackPaging.getItems()) {
                if (++trackCount % 100 == 0) {
                    System.out.println("Getting non-Billboard track " + trackCount);
                }
                // Check that we have the right artist
                if (Arrays.stream(track.getArtists()).noneMatch(artistSimplified -> artistSimplified.getId().equals(billboardArtist.artistID.spotifyId))) {
                    continue;
                }

                // Check that the song is not on the billboard charts
                if (billboardSongs.stream().map(song -> song.songID.spotifyId).anyMatch(spotifyId -> spotifyId.equals(track.getId()))) {
                    continue;
                }

                Song song = new Song(new ID(track.getName(), track.getId()), new ID(spotifyArtist.getName(), spotifyArtist.getId()));
                song.spotifyAttributes = new SpotifyAttributes();
                song.spotifyAttributes.setTrackData(track.getDurationMs(), track.getIsExplicit(), track.getTrackNumber());
                nonBillboardSongs.add(song);

                billboardArtist.nonBillboardSongIDs.add(song.songID);
            }
        }
    }

    private static void getSongs() throws IOException {
        System.out.println("Getting billboardSongs");
        billboardSongs = Main.readJSONFromFile("song_list_with_spotify_attributes.txt", new TypeToken<List<Song>>() {}.getType());
    }

    private static void getAudioFeatures() throws IOException {
        String[][] ids = new String[nonBillboardSongs.size() % 100 == 0 ? nonBillboardSongs.size() / 100 : nonBillboardSongs.size() / 100 + 1][100];
        System.out.println("Getting audio features. " + ids.length + " arrays of 100 ids each");

        int songNum = 0;
        outerFor:
        for (int arrayNum = 0; arrayNum < ids.length; arrayNum++) {
            for (int idNum = 0; idNum < ids[0].length; idNum++) {
                if (songNum >= nonBillboardSongs.size()) {
                    break outerFor;
                }
                ids[arrayNum][idNum] = nonBillboardSongs.get(songNum++).songID.spotifyId;
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
                    if (nonBillboardSongs.size() <= songNum && audioFeatures == null) { // Hit the end of the list
                        break;
                    }
                    if (nonBillboardSongs.get(songNum) != null && audioFeatures != null) {
                        nonBillboardSongs.get(songNum).spotifyAttributes.setFeatureData(audioFeatures.getAcousticness(),
                                audioFeatures.getDanceability(),
                                audioFeatures.getEnergy(), audioFeatures.getInstrumentalness(), audioFeatures.getKey(), audioFeatures.getLiveness()
                                , audioFeatures.getLoudness(), audioFeatures.getMode(), audioFeatures.getSpeechiness(), audioFeatures.getTempo(),
                                audioFeatures.getTimeSignature(), audioFeatures.getValence());
                    } else {
                        songsRemoved.add(nonBillboardSongs.get(songNum));
                    }
                    songNum++;
                }
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        nonBillboardSongs.removeAll(songsRemoved);
        System.out.println("Finished getting tracks. Songs removed because of lack of spotify audio feature data: " + songsRemoved.size());
        Main.writeJSONToFile(songsRemoved, "spotify_non-billboard_songs_removed_no_audio_feature_data.txt");
    }

}
