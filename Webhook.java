import javax.swing.plaf.basic.BasicPopupMenuSeparatorUI;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLOutput;
import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Webhook {
    public static void main(String[] args) {
        String prompt = System.getenv("LLM_PROMPT");
        String templates = System.getenv("LLM2_IMG_TEMPLATE");
        List<String> jokes = getEnvList("JOKES");
        List<String> imgs = getEnvList("IMGS");
        List<String> musics = getEnvList("MUSICS");

        // System.out.println("jokes = " + jokes);
        // System.out.println("images = " + imgs);
        // 랜덤 유머 선택
        Random random = new Random();
        String randomJoke = jokes.get(random.nextInt(jokes.size()));
        //System.out.println("randomJoke = " + randomJoke);
        String randomImage = imgs.get(random.nextInt(imgs.size()));
        //System.out.println("randomImage = " + randomImage);
        // Slack 메시지 전송
        String randomPlayList = musics.get(random.nextInt(musics.size()));


        int choice = random.nextInt(3); // 0 또는 1 생성
        if (choice == 0) {
            System.out.println("1번 선택됨");
            String llmTip = useLLM(prompt);
            String llmImgResult = useImg(templates.formatted(llmTip));
            sendMsg(llmTip, llmImgResult);
        } else if (choice == 1) {
            System.out.println("2번 선택됨");
            sendMsg1(randomImage, randomJoke);
        } else {
            System.out.println("3번 선택됨");
            sendMsg2(randomPlayList);
        }
    }

    private static List<String> getEnvList(String listName) {
        return Optional.ofNullable(System.getenv(listName))  // 환경변수 가져오기
                .map(value -> Arrays.asList(value.split(","))) // 쉼표로 나누어 리스트 변환
                .orElse(List.of()); // 환경변수가 없으면 빈 리스트 반환
    }

    // ====================== LLM 이미지 만들기
    public static String useImg(String prompt) {
        String apiUrl = System.getenv("LLM2_API_URL"); // 환경변수로 관리
        String apiKey = System.getenv("LLM2_API_KEY"); // 환경변수로 관리
        String model = System.getenv("LLM2_API_MODEL"); // 환경변수로 관리
        String payload = """
                {
                  "model": "%s",
                  "prompt": "%s",
                  "width": 1024,
                  "height": 768,
                  "steps": 1,
                  "n": 1
                }
                """.formatted(model, prompt);
        HttpClient client = HttpClient.newHttpClient(); // 새롭게 요청할 클라이언트 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl)) // URL을 통해서 어디로 요청을 보내는지 결정
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(); // 핵심
        String result = null; // return을 하려면 일단은 할당이 되긴 해야함
        try { // try
            /* =======toegether 파싱 ===========//
            "data": [
                {

                  }
                }
              ]
            }
            */
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body() = " + response.body());

            result = response.body()
                    .split("url\": \"")[1]
                    .split("\",")[0];

        } catch (Exception e) { // catch exception e
            throw new RuntimeException(e);
        }
        return result; // 앞뒤를 자르고 우리에게 필요한 내용만 리턴
    }

    // ================== LLM 내용 만들기
    public static String useLLM(String prompt) {
        String apiKey = System.getenv("LLM_API_KEY"); // 환경변수로 관리
        String apiUrl = System.getenv("LLM_API_URL"); // 환경변수로 관리
        // String model = System.getenv("LLM_API_MODEL"); // 환경변수로 관리

        if (!apiUrl.contains("?key=")) {
            apiUrl += "?key=" + apiKey;
        }
        String payload = String.format("""
                {
                    "contents": [
                        {
                            "role": "user",
                            "parts": [
                                {
                                    "text": "%s"
                                }
                            ]
                        }
                    ]
                }
                """, prompt);

        HttpClient client = HttpClient.newHttpClient(); // 새롭게 요청할 클라이언트 생성
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl)) // URL을 통해서 어디로 요청을 보내는지 결정
                .header("Content-Type", "application/json")
                //.header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build(); // 핵심
        try { // try
            HttpResponse<String> response = client.send(request,
                    HttpResponse.BodyHandlers.ofString());
            // System.out.println("response.statsusCode() = " + response.statusCode());
            // System.out.println("response.body() = " + response.body());

            String responseBody = response.body();
            String result = null;
            // content 값이 시작하는 위치

            // ============= Gemini 문자열 파싱 ================ //
            String patternString = "\"text\":\\s*\"([^\"]+)\"";
            Pattern pattern = Pattern.compile(patternString);
            Matcher matcher = pattern.matcher(responseBody);

            if (matcher.find()) {
                return matcher.group(1).trim(); // ✅ 찾은 값 반환 (앞뒤 공백 제거)
            } else {
                System.out.println("'text' 값을 찾을 수 없음!");
                return "⚠ API 응답에서 'text' 값을 찾을 수 없음!";
            }

            /* 지금 Gemini
            "candidates": [
            {
              "content": {
                "parts": [
                  {
                    "text": "무엇이든 기록하고 공유해봐요 📝.  코드는 간결하게, 주석은 명확하게!  괜찮아요, 질문 많이 하는 게 더 빨라요 👍.  그리고… 규칙적인 휴식 필수! ☕\n"
                  }
                ],
                "role": "model"
              },
             */
            //result = responseBody.split("\"text\":")[1].split("\"")[0];
            //System.out.println("result = " + result);
            //return result;
        } catch (Exception e) { // 예외 처리
            throw new RuntimeException(e);
        }
    }

    // ================= LLM 생성 이미지 전송 코드
    public static void sendMsg(String tip, String llmImgUrl) {
        String llmImgUrlWithCache = String.format("%s?cache_bypass=%d", llmImgUrl, Instant.now().getEpochSecond());

        String jsonPayload = String.format("""
                {
                    "attachments":[
                        {
                            "fallback": "오늘의 개발 팁",
                            "pretext": "☕오늘의 개발 팁 📲 \n\n %s",
                            "color": "#add8e6",
                            "image_url": "%s"
                        }
                   ]
                }
                """, tip, llmImgUrlWithCache);

        sendSlackMessage(jsonPayload);
    }

    // ============ 짤 이미지 코드
    public static void sendMsg1(String imgUrl, String text) {
        String imgUrlWithCache = imgUrl + "?cache_bypass=" + UUID.randomUUID();

        String jsonPayload = String.format("""
                {
                    "attachments":[
                        {
                            "fallback": "깔깔 유머",
                            "color": "#dda0dd",
                            "image_url": "%s",
                            "fields": [
                                {
                                    "title": "😄 아이고 배야, 깔깔 유-머 🤖",
                                    "value": "%s",
                                    "short": false
                                }
                            ]
                        }
                   ]
                }
                """, imgUrlWithCache, text);

        sendSlackMessage(jsonPayload);
    }

    // =========== PlayList 추천 코드
    public static void sendMsg2(String playUrl) {
        String jsonPayload = String.format("""
                {
                    "text": "🎵 코딩 플레이리스트 🎹 \\n 코딩할 때 들으면 좋은 유튜브 플레이리스트를 추천해 드립니다 \\n %s"
                }
                """, playUrl);

        sendSlackMessage(jsonPayload);
    }


    // ========== 각 함수에서 공통되는 부분인 HTTP 요청 코드
    private static void sendSlackMessage(String jsonPayload) {
        String slackUrl = System.getenv("SLACK_WEBHOOK_URL");

        // 브라우저나 유저인 척하는 것
        HttpClient client = HttpClient.newHttpClient();
        // 요청을 만들어보자 ! (fetch)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(slackUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        // 네트워크 과정에서 오류가 있을 수 있기에 선제적으로 예외처리가 필요
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body = " + response.body());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}