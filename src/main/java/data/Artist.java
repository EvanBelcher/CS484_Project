package data;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class Artist implements Comparable<Artist> {
    public ID artistID;
    public Set<ID> billboardSongIDs;
    public Set<ID> nonBillboardSongIDs;

    public Artist(ID artistID) {
        this.artistID = artistID;
        billboardSongIDs = new HashSet<>();
        nonBillboardSongIDs = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return Objects.equals(artistID, artist.artistID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artistID);
    }

    @Override
    public int compareTo(Artist o) {
        return artistID.spotifyId.compareTo(o.artistID.spotifyId);
    }
}
