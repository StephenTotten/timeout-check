import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LogAnalyzer {
    public static void main(String[] args) {
        String logFilePath = "log.txt"; // Specify the path to your log file
        int timeoutSeconds = 30;

        try {
            BufferedReader reader = new BufferedReader(new FileReader(logFilePath));
            String line;
            Map<String, Map<String, Map<String, Object>>> dateMap = new HashMap<>();
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length >= 6) {
                    String dateString = parts[0];
                    String timeString = parts[1];
                    String status = parts[5];
                    String processId = parts[7];

                    Date timestamp = dateFormat.parse(timeString);

                    if (!dateMap.containsKey(dateString)) {
                        dateMap.put(dateString, new HashMap<>());
                    }

                    Map<String, Map<String, Object>> dateInfo = dateMap.get(dateString);

                    if (!dateInfo.containsKey("success")) {
                        dateInfo.put("success", new HashMap<>());
                    }
                    if (!dateInfo.containsKey("timeouts")) {
                        dateInfo.put("timeouts", new HashMap<>());
                    }

                    if ("INFO".equals(status)) {
                        Map<String, Object> successInfo = dateInfo.get("success");
                        successInfo.put(processId, (int) successInfo.getOrDefault(processId, 0) + 1);
                    } else if ("Timeout".equals(status)) {
                        Map<String, Object> timeoutInfo = dateInfo.get("timeouts");
                        timeoutInfo.put(processId, (int) timeoutInfo.getOrDefault(processId, 0) + 1);
                        if (TimeUnit.MILLISECONDS.toSeconds(new Date().getTime() - timestamp.getTime()) > timeoutSeconds) {
                            timeoutInfo.put("timeoutCount", (int) timeoutInfo.getOrDefault("timeoutCount", 0) + 1);
                        }
                    }
                }
            }

            // Print the result with counts
            System.out.println(generateOutputWithCounts(dateMap));

            // Print the result with process IDs
            System.out.println(generateOutputWithProcessIds(dateMap));

            reader.close();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static Map<String, Map<String, Object>> generateOutputWithCounts(Map<String, Map<String, Map<String, Object>>> dateMap) {
        Map<String, Map<String, Object>> outputMap = new HashMap<>();
        dateMap.forEach((date, info) -> {
            Map<String, Object> dateResult = new HashMap<>();
            Map<String, Object> successInfo = info.getOrDefault("success", new HashMap<>());
            Map<String, Object> timeoutInfo = info.getOrDefault("timeouts", new HashMap<>());

            dateResult.put("success", sumCounts(successInfo));
            dateResult.put("timeouts", sumCounts(timeoutInfo));
            outputMap.put(date, dateResult);
        });
        return outputMap;
    }

    private static List<String> sumCounts(Map<String, Object> countMap) {
        List<String> result = new ArrayList<>();
        countMap.forEach((key, value) -> {
            int count = (int) value;
            for (int i = 0; i < count; i++) {
                result.add(key);
            }
        });
        return result;
    }

    private static Map<String, Map<String, Object>> generateOutputWithProcessIds(Map<String, Map<String, Map<String, Object>>> dateMap) {
        Map<String, Map<String, Object>> outputMap = new HashMap<>();
        dateMap.forEach((date, info) -> {
            Map<String, Object> dateResult = new HashMap<>();
            Map<String, Object> successInfo = info.getOrDefault("success", new HashMap<>());
            Map<String, Object> timeoutInfo = info.getOrDefault("timeouts", new HashMap<>());

            dateResult.put("success", new ArrayList<>(successInfo.keySet()));
            dateResult.put("timeouts", new ArrayList<>(timeoutInfo.keySet()));
            outputMap.put(date, dateResult);
        });
        return outputMap;
    }
}
