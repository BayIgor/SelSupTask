package bay.selsup;

import com.google.gson.Gson;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import java.util.Objects;
import java.util.concurrent.*;

public class CrptApi {
    private final Semaphore semaphore;
    private final ScheduledExecutorService scheduler;

    public CrptApi(Long reqInterval, int reqCount) {
        this.semaphore = new Semaphore(reqCount, true);
        this.scheduler = Executors.newScheduledThreadPool(1);

        //запуск планировщика, который будет освобождать разрешения с интервалом reqInterval
        scheduler.scheduleAtFixedRate(this::releasePermit, 0, reqInterval, TimeUnit.SECONDS);
    }

    private void releasePermit() {
        semaphore.release();
    }

    public void documentCreation(Objects document, String signature){
        //создание http клиента
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            //получение разрешения от semaphore
            semaphore.acquire();

            //преобразование документа в JSON
            Gson gson = new Gson();
            String documentJson = gson.toJson(document);

            //создание HTTP POST запроса при необходимости
            HttpPost httpPost = new HttpPost(serverUrlOrSomethingElse);

            //установка JSON-сериализованного документа и подписи в теле запроса
            String requestBody = "{ \"document\": " + documentJson + ", \"signature\": \"" + signature + "\" }";
            httpPost.setEntity(new StringEntity(requestBody, ContentType.parse("UTF-8")));

            //выполнение запроса и получение ответа
            HttpEntity responseEntity = httpClient.execute(httpPost).getEntity();
            if (responseEntity != null) {
                //обработка ответа, если это необходимо
                String responseJson = EntityUtils.toString(responseEntity);
                System.out.println("Ответ от сервера: " + responseJson);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}