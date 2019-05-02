import com.wrapper.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

interface SpotifyRequestRunnable<T> {
    T run() throws SpotifyWebApiException, IOException;
}