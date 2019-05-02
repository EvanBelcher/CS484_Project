import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.exceptions.detailed.UnauthorizedException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

class SpotifyUtils {

    static SpotifyApi spotifyApi;

    static void setupApi() throws IOException, SpotifyWebApiException {
        System.out.println("Setting up api");
        BufferedReader br = new BufferedReader(new FileReader("spotify_api_key.txt"));
        String clientID = br.readLine();
        String clientSecret = br.readLine();
        br.close();

        spotifyApi = new SpotifyApi.Builder().setClientId(clientID).setClientSecret(clientSecret).build();
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());

        System.out.println("Spotify api access token expires in: " + clientCredentials.getExpiresIn());
    }

    static <T> T requestAndRepeatIfTimeout(SpotifyRequestRunnable<T> runnable) {
        while (true) {
            try {
                return runnable.run();
            } catch (SpotifyWebApiException e) {
                if (e instanceof TooManyRequestsException) {
                    System.out.println("Rate limit hit - sleeping");
                    try {
                        Thread.sleep((((TooManyRequestsException) e).getRetryAfter() + 1) * 1000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } else if (e instanceof UnauthorizedException) {
                    try {
                        SpotifyUtils.setupApi();
                    } catch (IOException | SpotifyWebApiException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    e.printStackTrace();
                }
            } catch (IOException | NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

}
