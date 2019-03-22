import com.google.gson.Gson;
import data.BillboardPlacement;
import data.ID;
import data.Song;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class BillboardScraperWorker implements Runnable {
    static final List<Integer> BILLBOARD_ERROR_YEARS = Arrays.asList(1966, 1977, 1983, 1994, 2005, 2011);

    static Lock jsoupLock = new ReentrantLock();

    private final int year;
    private List<Song> songs = new ArrayList<>();

    public BillboardScraperWorker(int year) {
        this.year = year;
    }

    @Override
    public void run() {
        System.out.println("Starting year " + year);
        try {
            Document yearListDocument = Jsoup.connect("https://www.billboard.com/archive/charts/" + year + "/hot-100").get();
            handleYearList(yearListDocument, year);
            Gson gson = new Gson();
            Path filePath = Paths.get("billboard_raws/" + year + ".txt");
            Files.createDirectories(filePath.getParent());
            BufferedWriter bw = new BufferedWriter(new FileWriter("billboard_raws/" + year + ".txt"));
            bw.write(gson.toJson(songs));
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Ending year " + year);
    }

    private void handleYearList(Document yearListDocument, int year) throws IOException {
        Elements weekLinks = yearListDocument.select("body > main > article > div > div > div > table > tbody > tr > td:nth-child(1) > a");

        /*
        Address Billboard issue - on common (non-leap) years where January 1st is on Saturday, the first chart is on January 1st.
         For these years, the Jan 1 chart is not listed on the archive listing for the year, resulting in the first
         chart being skipped. Example: https://www.billboard.com/archive/charts/1977/hot-100

         These years (1958-present): 1966, 1977, 1983, 1994, 2005, 2011
         List from: https://en.wikipedia.org/wiki/Common_year_starting_on_Saturday
        */
        if (BILLBOARD_ERROR_YEARS.contains(year)) {
            weekLinks.add(new Element("a").attr("href", "/charts/hot-100/" + year + "-01-01"));
        }

        System.out.println("Getting week chart documents");
        List<Document> weekChartDocuments = weekLinks.parallelStream().map(weekLink -> "https://www.billboard.com" + weekLink.attr("href")).map(url -> {
            Document weekChartDocument = null;
            try {
                weekChartDocument = Jsoup.connect(url).get();
            } catch (IOException e) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }

            while (weekChartDocument == null || weekChartDocument.getElementsByClass("chart-list-item").isEmpty()) {
                System.out.println("YES IT WAS NULL!!!");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                try {
                    weekChartDocument = Jsoup.connect(url).get();
                } catch (IOException e){
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            return weekChartDocument;

        }).filter(Objects::nonNull).collect(Collectors.toList());

        int week = 1;
        for (Document weekChartDocument : weekChartDocuments) {
            handleWeekChart(weekChartDocument, year, week);
            week++;
        }

//        for (Element weekLink : weekLinks) {
//            String linkRelURL = weekLink.attr("href");
//            String weekURL = "https://www.billboard.com" + linkRelURL;
//            Document weekChartDocument = getHTMLDocument(weekURL);
//
//            while (weekChartDocument == null || weekChartDocument.getElementsByClass("chart-list-item").isEmpty()) {
//                System.out.println("YES IT WAS NULL!!!");
//                try {
//                    Thread.sleep(10000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                weekChartDocument = getHTMLDocument(weekURL);
//            }
//
//            handleWeekChart(weekChartDocument, year, week);
//            week++;
//        }
    }

    private void handleWeekChart(Document weekChartDocument, int year, int week) {
        System.out.println("Year: " + year + " Week " + week);

        Elements chartListItems = weekChartDocument.getElementsByClass("chart-list-item");
        for (Element chartListItem : chartListItems) {
            int position = Integer.parseInt(chartListItem.getElementsByClass("chart-list-item__rank").first().text());
            String title = chartListItem.getElementsByClass("chart-list-item__title-text").first().text();
            String artist = chartListItem.getElementsByClass("chart-list-item__artist").first().text();

            Song song = new Song(new ID(title), new ID(artist));
            song.billboardPlacements = new LinkedList<>();
            song.billboardPlacements.add(new BillboardPlacement(year, week, position));
            songs.add(song);
        }
    }
}
