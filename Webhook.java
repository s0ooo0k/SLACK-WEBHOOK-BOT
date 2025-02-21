import javax.swing.plaf.basic.BasicPopupMenuSeparatorUI;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLOutput;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Webhook {
    public static void main(String[] args) {
        String prompt = "너는 시니어 개발자야. 주니어 개발자들에게 전하는 개발 팁을 100글자 이내로 작성해줘. 개발자 팁이라는 제목도 없어도 되고, 앞 뒤 내용 없이 팁만 100글자 이내의 자연스러운 한글 평문으로 작성해줘. 100글자 이내야 꼭. 한자나 일본어 등 한글이 아닌 문자는 꼭 제외해줘, 명심해.  *같은 마크다운이나 강조 문법은 모두 생략해줘";
        String llmTip = useLLM(prompt);
        String templates = System.getenv("LLM2_IMG_TEMPLATE");
        String llmImgResult = useImg(templates.formatted(llmTip));
        List<String> jokes = List.of(
                "개발자가 제일 좋아하는 과일은? \n => '바나나(BaNaNa)' 🍌 ",
                "`==` 와 `===`이 싸웠어. \n => `==`가 말했지: \"넌 너무 엄격해!\" 🤨",
                "왜 `NaN === NaN`이 `false`일까? \n => JavaScript도 자기 자신을 이해 못 해서! 🤯",
                "왜 `typeof null`이 `object`일까? \n => JavaScript도 실수할 때가 있지! 🫠",
                "에러를 만나면 외쳐라 \n=> 내 잘못은 없다 🫠",
                "shell의 종류에 뭐가 있는지 아시나요? \n => 정답 : 몽쉘! 🍫",
                "Q. HTML이 JavaScript에게 사랑을 고백했어. JavaScript의 반응은? => A. undefined 😭",
                "나는 짠 코드에 반 만 가져가.\n => 난 싱거운 코드",
                "난 익명을 JAVA낸다"
        );

        List<String> images = List.of(
                "https://github.com/user-attachments/assets/d7f929c9-6602-4d26-9a0a-d09149567108",
                "https://github.com/user-attachments/assets/262f3fb5-1017-4a36-ae37-5ac2129b53a4",
                "https://github.com/user-attachments/assets/476403d0-a814-4482-8713-bbad44ec9708",
                "https://github.com/user-attachments/assets/ed11bd93-0c2a-4ace-9845-605cf36a178b",
                "https://github.com/user-attachments/assets/99a0927d-fa6d-4c86-b68f-73b58973d052",
                "https://github.com/user-attachments/assets/0ae5ed34-c8e2-4227-a04f-7e5f40032f1e",
                "https://github.com/user-attachments/assets/c1892c96-174d-4a2b-8883-991e63f95574",
                "https://github.com/user-attachments/assets/82781e95-4027-4dbc-8b7c-78dddddcf095"
        );
        // 랜덤 유머 선택
        Random random = new Random();
        String randomJoke = jokes.get(random.nextInt(jokes.size()));
        String randomImage = images.get(random.nextInt(images.size()));

        System.out.println("llmImageResult = " + llmImgResult); // 발송은 안함
        // Slack 메시지 전송
        sendMsg(llmTip, llmImgResult, randomJoke);


    }

    // 이미지 만들기
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
                  "index": 0,
                  "url": "https://api.together.ai/imgproxy/JttWVsqNRjYmWzesmSG8Mul4f4EoMtIn_ycVYcEmr5Y/format:jpeg/aHR0cHM6Ly90b2dldGhlci1haS1iZmwtaW1hZ2VzLXByb2QuczMudXMtd2VzdC0yLmFtYXpvbmF3cy5jb20vaW1hZ2VzLzNlNzZjNTY2MjcwODY5NDViOTRjMzVkYTc3MDc1NWIwYmM3YjNhM2Y4NTA3OTQ5MzEwY2VmYzdmOGZiYzBlNmM_WC1BbXotQWxnb3JpdGhtPUFXUzQtSE1BQy1TSEEyNTYmWC1BbXotQ29udGVudC1TaGEyNTY9VU5TSUdORUQtUEFZTE9BRCZYLUFtei1DcmVkZW50aWFsPUFTSUFZV1pXNEhWQ05PS0tYU0JJJTJGMjAyNTAyMjElMkZ1cy13ZXN0LTIlMkZzMyUyRmF3czRfcmVxdWVzdCZYLUFtei1EYXRlPTIwMjUwMjIxVDA2MzIyNlomWC1BbXotRXhwaXJlcz0zNjAwJlgtQW16LVNlY3VyaXR5LVRva2VuPUlRb0piM0pwWjJsdVgyVmpFS2YlMkYlMkYlMkYlMkYlMkYlMkYlMkYlMkYlMkYlMkZ3RWFDWFZ6TFhkbGMzUXRNaUpITUVVQ0lBWXlLdU9HbHlGVXNtQ09sTDYzZ0oyUlYyZW11c0VyNU1sRGdXYTg5WVF4QWlFQTFsZDFaSWFvWjkySmpQcmxZZnVrYWF6dFBPbG0ycjNpQmxGb1Z6UVl4YmtxbVFVSTBQJTJGJTJGJTJGJTJGJTJGJTJGJTJGJTJGJTJGJTJGQVJBQUdndzFPVGczTWpZeE5qTTNPREFpRE9KclExN0xyMUhZY25rQ1JpcnRCQ2Z1RExqVFZHZW9hQXpqaWVveE9oTWszdlhCMVk1UzRNalhSQm1DVjZnamkxRzFjU1dkSW5Va1NwWHl0UmglMkJLJTJGQ3hFTEZwak5jQ3olMkY2cmJHM3R6Yks3NmxHREpHa1prbWY2Y3hDJTJGbDNTRGlDZEpFZkNRdDVkc0VXWW5oYjAlMkIyR1BGdHVDYlBGV2dXV09FMW91akxNbjMwQzh0cHhkTW1uM2RmMGZaJTJGZWszdXZidFFuRkdXJTJGbUZkblIlMkJtWkpOSHo2JTJCbVN5T2NvRnNwNTVPdUMycDhXUzZGbDEzelFTSGVjJTJCSFBXWE1IUiUyRlYlMkJLZU16c0djUDg1a0FSN1pWR2Nva1ZzS3IzRUh3NzZvZ3N5enVPVUtJZ0NyNDhmZHVuU25vekN5amxxd00wTVV3OUxoUHNXQTFPdElDMGN5QkJ3ZWdCYmJpVFhDNU5TWkFqakJENEQ3NGVwQ3BXb28lMkIzVk9PNlRDJTJCYmlySnVySWRxaWZRQjd6Um4yRjVmVFA3bE5RWGVUR3BjJTJGSmdaWktDTUFEb1RhVGs1NjdMUDZrTCUyRjR6b2UzS1ppdkpSSTlzNGZUZldqVVZNS2pXNXJwJTJGaTFsSEZsbjZRTWZsMGZ2Qkt1JTJGWEpabFFTMkVPRlU1MjZTdTFOUnh1VVV4JTJCeFpyQ0w2MG9BczF2WWdjUzBGVXJWWFBVU2d5WXE3b29hMjZQaXM0eHQ0Rk1IRlFBN1lSekFHUDlZTFp2NlB1OHZPb2slMkI5SXRaR2R3R1Q4UFl5QXd0R1RqT3NnVjE1a0l2dEFSZ0dCTkljcWNzWG1HJTJGNyUyRjlPTlhsNEd1Ukc0S2k4TzZPb1BJSTdkMVJscTN2anVTMDBuRmFLUEZYeEx2QnBOOWUzeG1XbEk2TVo3S1ZQZ1V5RlVaJTJCaWwyYXFvbm1jdEVLRWhsOTY5aEFPTWxHeUlIWEtGYmFPTUFYSHNZTGZjY09yaDBjMnh3MDl2bEVIUFk4SHFZeEZIRnNzc1VPMzYlMkJFYTJiV3N6MTYzbkVZbXMxWFZiJTJGdVVJb0lqMVRrdkVWOEJndkxtRUVQS3d3eTlIaWR4V3FTalY1TmlnSlhUSXJmMXlmNXd1emZ3ckdSMjNNbThERDV1JTJCQzlCanFiQVdzenI2TlpJSjNEQVVDaklycVlYWnJhTHVpbk1ZekNCcVA4dUZxVWlQSDlvTUlzb0cwUXNiOSUyRlY2M2cwdVljWjZPZ1RST05wY2ZRN2J5bWFFTDVqaXZ3WE1DVFpoQzliNFBEYllJY1JMS1pwNU5MQlRnbUR6SUJRUFQ0dWF6MEZzSjdSY0FueFIyNlhmWDUzWUhmZ0hGY2dKSGY0cWJOU3cwM0Rud3BQeEFhb2ZWU05oRW45VE0zRmMwMXhIQU9DdU5RNVRzVFlQSGgzUnJJJlgtQW16LVNpZ25hdHVyZT00MmQ5NjcwMjFiZmFlMTFlZmIyNjZkMTFmNDhiMWQ3N2I4MjA5MDgyNWQ2YjNlZjYzNmI2NjcxMDQxZmMxZDVlJlgtQW16LVNpZ25lZEhlYWRlcnM9aG9zdCZ4LWlkPUdldE9iamVjdA",
                  "timings": {
                    "inference": 0.605654388666153
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
                    .split("\",")[0];;

        } catch (Exception e) { // catch exception e
            throw new RuntimeException(e);
        }
        return result; // 앞뒤를 자르고 우리에게 필요한 내용만 리턴
    }

    // LLM 내용 만들기
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

    public static void sendMsg(String tip, String llmImgUrl, String text, String imgUrl) {
        // String slackUrl = "https://hooks.slack.com/services/";
        String slackUrl = System.getenv("SLACK_WEBHOOK_URL");
        String jsonPayload = String.format("""
        {
           "attachments":[
                                           {
                                               "text": "☕ 오늘의 개발 팁 📲 \\n %s \\n",
                                               "image_url": "%s"
                                           },
                                           {
                                               "text": "😄 아이고 배야, 깔깔 유-머 🤖\\n %s \\n",
                                               "image_url": "%s"
                                           }
                                       ]
        }
        """, tip, llmImgUrl, text, imgUrl);

        // 브라우저나 유저인 척하는 것
        HttpClient client = HttpClient.newHttpClient();
        // 요청을 만들어보자 ! (fetch)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(slackUrl)) // URL을 통해서 어디로 요청을 보내는지 결정
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build(); // 핵심


        // 네트워크 과정에서 오류가 있을 수 있기에 선제적으로 예외처리가 필요
        try {
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());
            // statusCode 2 -> ok / 4, 5 -> issue
            System.out.println("response.statusCode() = " + response.statusCode());
            System.out.println("response.body = " +response.body());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
