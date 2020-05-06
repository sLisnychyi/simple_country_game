package com.danit.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.experimental.Accessors;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ThreadLocalRandom;

@WebServlet(urlPatterns = "/game")
public class GameController extends HttpServlet {

    Map<String, String> values = new HashMap<>();

    public GameController() {
        ObjectMapper mapper = new ObjectMapper();
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpGet request = new HttpGet("https://raw.githubusercontent.com/samayo/country-json/master/src/country-by-capital-city.json");
        CloseableHttpResponse response = null;
        try {
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String result = EntityUtils.toString(entity);
            List<Data> data = mapper.readValue(result, new TypeReference<List<Data>>() {
            });
            for (Data datum : data) {
                values.put(datum.getCountry(), datum.getCity());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                response.close();
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Question> questions = new ArrayList<>();
        Set<Integer> integers = new HashSet<>();
        while (integers.size() < 3) {
            int i = ThreadLocalRandom.current().nextInt(values.size());
            if (integers.add(i)) {
                List<String> strings = new ArrayList<>(values.keySet());
                String key = strings.get(i);
                String value = values.get(key);
                StringBuilder anwers = new StringBuilder(value);
                for (int j = 0; j < 2; j++) {
                    int rnd = ThreadLocalRandom.current().nextInt(values.size());
                    List<String> values2 = new ArrayList<>(values.values());
                    anwers.append(", ").append(values2.get(rnd));
                }
                questions.add(new Question().setQuestion(key).setAnswers(anwers.toString()));
            }
        }
        String s = new ObjectMapper().writeValueAsString(questions);
        resp.getWriter().print(s);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}

@lombok.Data
class Data {
    private String country;
    private String city;
}

@lombok.Data
@Accessors(chain = true)
class Question {
    private String question;
    private String answers;
}