import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import data.Song;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

public class BillboardDeduper {

    static Gson gson = new Gson();

    public static void main(String[] args) throws Exception {
        checkData();

        List<Song> dedupedSongList = new ArrayList<>();


        int originalSongCount = 0;
        int duplicateCount = 0;
        for (int year = 1958; year <= 2019; year++) {
            System.out.println("Starting year " + year);
            BufferedReader br = new BufferedReader(new FileReader("billboard_raws/" + year + ".txt"));
            List<Song> songs = gson.fromJson(br, new TypeToken<List<Song>>() {
            }.getType());
            br.close();

            for (Song song : songs) {
                if (dedupedSongList.contains(song)) {
                    int index = dedupedSongList.indexOf(song);
                    Song existingSong = dedupedSongList.get(index);


                    Song newSong = new Song(existingSong.songID, existingSong.artistIDs);
                    newSong.billboardPlacements.addAll(existingSong.billboardPlacements);
                    newSong.billboardPlacements.addAll(song.billboardPlacements);

                    dedupedSongList.remove(index);
                    dedupedSongList.add(newSong);
                    duplicateCount++;
                } else {
                    dedupedSongList.add(song);
                }
                originalSongCount++;
            }
            System.out.println("Ending year " + year);
            System.out.println("Original songs: " + originalSongCount);
            System.out.println("Duplicates: " + duplicateCount);
            System.out.println("Unique songs: " + dedupedSongList.size());
            System.out.println();
        }

        BufferedWriter bw = new BufferedWriter(new FileWriter("billboard_deduped_list.txt"));
        bw.write(gson.toJson(dedupedSongList));
        bw.close();
    }

    private static void checkData() throws Exception {
        StringJoiner errors = new StringJoiner("\n");
        for (int year = 1958; year <= 2019; year++) {
            BufferedReader br = new BufferedReader(new FileReader("billboard_raws/" + year + ".txt"));
            List<Song> songs = gson.fromJson(br, new TypeToken<List<Song>>() {}.getType());
            br.close();

            boolean abnormalYear = Arrays.asList(1958, 1976, 1977, 2018, 2019).contains(year);

            if (year == 1958 && songs.size() != 22 * 100) { // First year
                errors.add("Year 1958 does not have 2200 entries. Actual: " + songs.size());
            } else if (year == 1976 && songs.size() != 5195) { // Abnormal year where the following weeks are each missing one entry: 48, 49, 50, 51, 52
                errors.add("Year 1976 does not have 5195 entries. Actual: " + songs.size());
            } else if (year == 1977 && songs.size() != 5190) { // Abnormal year where the following weeks are each missing one entry: 1, 2, 3, 4, 5, 6, 7, 8, 9, 52
                errors.add("Year 1977 does not have 5190 entries. Actual: " + songs.size());
            } else if (year == 2018 && songs.size() != 53 * 100) { // Abnormal year with 53 entries
                errors.add("Year 2018 does not have 5300 entries. Actual: " + songs.size());
            } else if (year == 2019 && songs.size() != 12 * 100) { // Current year
                errors.add("Year 2019 does not have 1200 entries. Actual: " + songs.size());
            } else if (!abnormalYear && songs.size() != 52 * 100) { // Every other year should have exactly 52*100 entries
                errors.add("Year " + year + " does not have 5200 entries. Actual: " + songs.size());
            }

            if (songs.stream().anyMatch(song -> song.billboardPlacements == null)) {
                errors.add("Year " + year + " does not have its billboard placements.");
            }

        }
        if (errors.length() > 0) {
            throw new Exception(errors.toString());
        }
    }

}
