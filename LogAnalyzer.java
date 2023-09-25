import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class LogAnalyzer {
    public static void main(String[] args) {
        String fileName = "log.txt";

        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String line;
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");

            Map<String, Map<String, List<Integer>>> dateProcessMap = new HashMap<>();

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(" : ");
                if (parts.length == 2) {
                    String timestampStr = parts[0];
                    String logMessage = parts[1];
                    Date timestamp = dateFormat.parse(timestampStr);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(timestamp);

                    String dateKey = new SimpleDateFormat("dd/MM/yy").format(calendar.getTime());
                    int processId = Integer.parseInt(logMessage.split(" ")[1]);

                    dateProcessMap.putIfAbsent(dateKey, new HashMap<>());
                    Map<String, List<Integer>> processMap = dateProcessMap.get(dateKey);
                    processMap.putIfAbsent("success", new ArrayList<>());
                    processMap.putIfAbsent("timeouts", new ArrayList<>());

                    if (logMessage.contains("Start")) {
                        processMap.get("success").add(processId);
                    } else if (logMessage.contains("End")) {
                        processMap.get("timeouts").add(processId);
                    }
                }
            }

            // Output with Count
            System.out.println("Output with Count");
            List<Map<String, Map<String, Integer>>> countOutputList = new ArrayList<>();
            for (Map.Entry<String, Map<String, List<Integer>>> entry : dateProcessMap.entrySet()) {
                Map<String, Map<String, Integer>> dateMap = new HashMap<>();
                Map<String, List<Integer>> processMap = entry.getValue();
                Map<String, Integer> countMap = new HashMap<>();
                countMap.put("success", processMap.get("success").size());
                countMap.put("timeouts", processMap.get("timeouts").size());
                dateMap.put(entry.getKey(), countMap);
                countOutputList.add(dateMap);
            }
            System.out.println(countOutputList);

            // Output with Process Id
            System.out.println("\nOutput with Process Id");
            List<Map<String, Map<String, List<Integer>>>> processIdOutputList = new ArrayList<>();
            for (Map.Entry<String, Map<String, List<Integer>>> entry : dateProcessMap.entrySet()) {
                Map<String, Map<String, List<Integer>>> dateMap = new HashMap<>();
                dateMap.put(entry.getKey(), entry.getValue());
                processIdOutputList.add(dateMap);
            }
            System.out.println(processIdOutputList);

            br.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
}
