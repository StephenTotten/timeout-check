import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogAnalyzer {
    public static void main(String[] args) {
        // Define the log file path
        String logFilePath = "log.txt";

        // Define the timeout threshold (in seconds)
        int timeoutThreshold = 30;

        // Initialize data structures to store results
        Map<String, Map<String, List<Long>>> dateProcessMap = new TreeMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(logFilePath))) {
            String line;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts.length >= 2) {
                    String dateTimeString = parts[0];
                    String processInfo = parts[1];

                    try {
                        Date dateTime = dateFormat.parse(dateTimeString);
                        String dateKey = new SimpleDateFormat("dd/MM/yy").format(dateTime);

                        if (!dateProcessMap.containsKey(dateKey)) {
                            dateProcessMap.put(dateKey, new HashMap<>());
                        }

                        if (processInfo.contains("Start")) {
                            String processId = processInfo.split(" ")[1];
                            dateProcessMap.get(dateKey).putIfAbsent(processId, new ArrayList<>());
                            dateProcessMap.get(dateKey).get(processId).add(dateTime.getTime());
                        } else if (processInfo.contains("End")) {
                            String processId = processInfo.split(" ")[1];
                            if (dateProcessMap.get(dateKey).containsKey(processId)) {
                                List<Long> startTimes = dateProcessMap.get(dateKey).get(processId);
                                long elapsedTime = (dateTime.getTime() - startTimes.get(startTimes.size() - 1)) / 1000;
                                if (elapsedTime <= timeoutThreshold) {
                                    dateProcessMap.get(dateKey).putIfAbsent("success", new ArrayList<>());
                                    dateProcessMap.get(dateKey).get("success").add(Long.parseLong(processId));
                                } else {
                                    dateProcessMap.get(dateKey).putIfAbsent("timeouts", new ArrayList<>());
                                    dateProcessMap.get(dateKey).get("timeouts").add(Long.parseLong(processId));
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // Print the results
            System.out.println("Output with Count");
            System.out.println(dateProcessMap);

            System.out.println("\nOutput with Process Id");
            for (String dateKey : dateProcessMap.keySet()) {
                System.out.println("{");
                System.out.println("\"" + dateKey + "\":{");
                Map<String, List<Long>> processData = dateProcessMap.get(dateKey);
                for (String dataType : processData.keySet()) {
                    System.out.print(dataType + ": [");
                    List<Long> dataList = processData.get(dataType);
                    for (int i = 0; i < dataList.size(); i++) {
                        System.out.print(dataList.get(i));
                        if (i < dataList.size() - 1) {
                            System.out.print(", ");
                        }
                    }
                    System.out.print("]");
                    if (!dataType.equals("timeouts")) {
                        System.out.println(",");
                    }
                }
                System.out.println("\n}");
                System.out.println("}");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
