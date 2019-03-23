package data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Song {
    public ID songID;
    public List<ID> artistIDs;

    public LinkedList<BillboardPlacement> billboardPlacements;
    public SpotifyAttributes spotifyAttributes;
    public GeniusAttributes geniusAttributes;

    public Song(ID songID) {
        this.songID = songID;
        this.billboardPlacements = new LinkedList<>();
    }

    public Song(ID songID, ID artistID) {
        this.songID = songID;
        this.artistIDs = new ArrayList<>();
        this.artistIDs.add(artistID);
        this.billboardPlacements = new LinkedList<>();
    }

    public Song(ID songID, List<ID> artistIDs) {
        this.songID = songID;
        this.artistIDs = artistIDs;
        this.billboardPlacements = new LinkedList<>();
    }

    public Song(ID songID, List<ID> artistIDs, LinkedList<BillboardPlacement> billboardPlacements, SpotifyAttributes spotifyAttributes,
                GeniusAttributes geniusAttributes) {
        this.songID = songID;
        this.artistIDs = artistIDs;
        this.billboardPlacements = billboardPlacements;
        this.spotifyAttributes = spotifyAttributes;
        this.geniusAttributes = geniusAttributes;
        this.billboardPlacements = new LinkedList<>();
    }

    public String getSearchName() {
        return this.songID.name + " " + this.artistIDs.get(0).name.toLowerCase().replaceAll("featuring |feat\\. |and ", "");
    }

    @Override
    public String toString() {
        return "Song{" +
                "songID=" + songID +
                ", artistIDs=" + artistIDs +
                ", billboardPlacements=" + billboardPlacements +
                ", spotifyAttributes=" + spotifyAttributes +
                ", geniusAttributes=" + geniusAttributes +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(songID, song.songID) &&
                Objects.equals(artistIDs, song.artistIDs);
    }

    @Override
    public int hashCode() {

        return Objects.hash(songID, artistIDs);
    }
}
