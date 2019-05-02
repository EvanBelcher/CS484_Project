package data;

import java.util.Objects;

public class ID {
    public String name;
    public String spotifyId;
    public String geniusId;

    public ID(String name) {
        this.name = name;
    }

    public ID(String name, String spotifyId) {
        this.name = name;
        this.spotifyId = spotifyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ID id = (ID) o;
        return Objects.equals(name, id.name) &&
                Objects.equals(spotifyId, id.spotifyId) &&
                Objects.equals(geniusId, id.geniusId);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name, spotifyId, geniusId);
    }

    @Override
    public String toString() {
        return "ID{" +
                "name='" + name + '\'' +
                ", spotifyId='" + spotifyId + '\'' +
                ", geniusId='" + geniusId +
                "'}";
    }
}
