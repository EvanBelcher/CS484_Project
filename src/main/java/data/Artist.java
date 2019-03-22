package data;

import java.util.Objects;
import java.util.Set;

public class Artist {
    ID artistID;
    Set<ID> songIds;

    @Override
    public String toString() {
        return "Artist{" +
                "artistIDs=" + artistID +
                ", songIds=" + songIds +
                '}';
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
}
