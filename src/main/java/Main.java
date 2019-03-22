import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import data.Song;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Main {

    public static void main(String[] args) throws IOException {

        Gson gson = new Gson();
        BufferedReader br = new BufferedReader(new FileReader("billboard_deduped_list.txt"));
        List<Song> songList = gson.fromJson(br, new TypeToken<List<Song>>(){}.getType());
        br.close();

        System.out.println("Size: " + songList.size());
        songList.sort((o1, o2) -> {
            if(o1.billboardPlacements.size() == o2.billboardPlacements.size()){
                return 0;
            }
            return o1.billboardPlacements.size() < o2.billboardPlacements.size() ? 1 : -1;
        });

        for(int i = 0; i < 11; i++){
            System.out.println(songList.get(i).songID.name + " - " + songList.get(i).billboardPlacements.size() + ". Most recent: " + songList.get(i).billboardPlacements.getLast());
        }

        System.out.println();
        System.out.println(songList.stream().collect(Collectors.summarizingInt(song -> song.billboardPlacements.size())));

        System.out.println();
        System.out.println(songList.stream().filter(song -> song.billboardPlacements.getFirst().placement == 1).sorted((o1, o2) -> {
            if(o1.billboardPlacements.getFirst().year == o2.billboardPlacements.getFirst().year){
                if(o1.billboardPlacements.getFirst().week == o2.billboardPlacements.getFirst().week){
                    return 0;
                }
                return o1.billboardPlacements.getFirst().week > o2.billboardPlacements.getFirst().week ? 1 : -1;
            }
           return o1.billboardPlacements.getFirst().year > o2.billboardPlacements.getFirst().year ? 1 : -1;
        }).map(song -> song.songID.name).collect(Collectors.toList()));
    }
}
