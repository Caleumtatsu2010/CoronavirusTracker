package com.example.coronavirustracker.services;

import com.example.coronavirustracker.models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class CoronaVirusDataService
{
    private static String  VIRUS_DATA_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    List<LocationStats> allStats = new ArrayList<>();

    @PostConstruct
    @Scheduled(cron = "* * 1 * * *")//scheule to run every 1 hour
    public void fetchVirusData() throws IOException, InterruptedException {
        List<LocationStats> newStats = new ArrayList<>();


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATA_URL)).build();
        HttpResponse <String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        for (CSVRecord record : records) {
            //initalize an object to store a record got from  csv file
            LocationStats locationStats = new LocationStats();

            //get data from Province/State collum and set to state in locationStats
            locationStats.setState(record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region"));

            int latestCases = Integer.parseInt(record.get(record.size() - 1));//get last collum in record
            int prevDayCases = Integer.parseInt(record.get(record.size() - 2));//get collum near the end of record

            locationStats.setLatestTotalCases(latestCases);
            locationStats.setDiffFromPrevDay(latestCases - prevDayCases);

            newStats.add(locationStats);//add to list of record objects
        }
        this.allStats = newStats;
    }

    public List<LocationStats> getAllStats()
    {
        return  allStats;
    }
}
