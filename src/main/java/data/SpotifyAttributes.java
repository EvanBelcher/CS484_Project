package data;

import com.wrapper.spotify.enums.Modality;

import java.util.Objects;

public class SpotifyAttributes {
    public int durationMs; // Numerical
    public boolean explicit; // Binary
    public int trackNumber; // Numerical
    public double acousticness; // Numerical
    public double danceability; // Numerical
    public double energy; // Numerical
    public double instrumentalness; // Numerical
    public int key; // Nominal
    public double liveness; // Numerical
    public double loudness; // Numerical
    public Modality mode; // Categorical / Nominal
    public double speechiness; // Numerical
    public double tempo; // Numerical
    public int timeSignature; // Nominal
    public double valence; // Numerical

    public SpotifyAttributes() {}

    public SpotifyAttributes(int durationMs, boolean explicit, int trackNumber, double acousticness, double danceability, double energy,
                             double instrumentalness, int key, double liveness, double loudness, Modality mode, double speechiness, double tempo,
                             int timeSignature, double valence) {
        this.durationMs = durationMs;
        this.explicit = explicit;
        this.trackNumber = trackNumber;
        this.acousticness = acousticness;
        this.danceability = danceability;
        this.energy = energy;
        this.instrumentalness = instrumentalness;
        this.key = key;
        this.liveness = liveness;
        this.loudness = loudness;
        this.mode = mode;
        this.speechiness = speechiness;
        this.tempo = tempo;
        this.timeSignature = timeSignature;
        this.valence = valence;
    }

    public void setTrackData(int durationMs, boolean explicit, int trackNumber) {
        this.durationMs = durationMs;
        this.explicit = explicit;
        this.trackNumber = trackNumber;
    }

    public void setFeatureData(double acousticness, double danceability, double energy, double instrumentalness, int key, double liveness,
                               double loudness, Modality mode, double speechiness, double tempo, int timeSignature, double valence) {
        this.acousticness = acousticness;
        this.danceability = danceability;
        this.energy = energy;
        this.instrumentalness = instrumentalness;
        this.key = key;
        this.liveness = liveness;
        this.loudness = loudness;
        this.mode = mode;
        this.speechiness = speechiness;
        this.tempo = tempo;
        this.timeSignature = timeSignature;
        this.valence = valence;
    }

    @Override
    public String toString() {
        return "SpotifyAttributes{" +
                "durationMs=" + durationMs +
                ", explicit=" + explicit +
                ", trackNumber=" + trackNumber +
                ", acousticness=" + acousticness +
                ", danceability=" + danceability +
                ", energy=" + energy +
                ", instrumentalness=" + instrumentalness +
                ", key=" + key +
                ", liveness=" + liveness +
                ", loudness=" + loudness +
                ", mode=" + mode +
                ", speechiness=" + speechiness +
                ", tempo=" + tempo +
                ", timeSignature=" + timeSignature +
                ", valence=" + valence +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpotifyAttributes that = (SpotifyAttributes) o;
        return durationMs == that.durationMs &&
                explicit == that.explicit &&
                trackNumber == that.trackNumber &&
                Double.compare(that.acousticness, acousticness) == 0 &&
                Double.compare(that.danceability, danceability) == 0 &&
                Double.compare(that.energy, energy) == 0 &&
                Double.compare(that.instrumentalness, instrumentalness) == 0 &&
                key == that.key &&
                Double.compare(that.liveness, liveness) == 0 &&
                Double.compare(that.loudness, loudness) == 0 &&
                Double.compare(that.speechiness, speechiness) == 0 &&
                Double.compare(that.tempo, tempo) == 0 &&
                timeSignature == that.timeSignature &&
                Double.compare(that.valence, valence) == 0 &&
                mode == that.mode;
    }

    @Override
    public int hashCode() {

        return Objects.hash(durationMs, explicit, trackNumber, acousticness, danceability, energy, instrumentalness, key, liveness, loudness, mode,
                speechiness, tempo, timeSignature, valence);
    }
}
