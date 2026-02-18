package com.Manupriya.bajaj.service;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.Manupriya.bajaj.Dto.FinalQueryRequest;
import com.Manupriya.bajaj.Dto.GenerateWebhookRequest;
import com.Manupriya.bajaj.Dto.GenerateWebhookResponse;

@Service
public class WebhookService {

    @Autowired
    private RestTemplate restTemplate;

    private final String GENERATE_URL =
            "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

    private final String regNo = "REG12347";

    public void startProcess() {
        try {
            GenerateWebhookResponse response = generateWebhook();
            String finalQuery = decideSqlQuery();
            submitFinalQuery(
                    response.getWebhook(),
                    response.getAccessToken(),
                    finalQuery
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private GenerateWebhookResponse generateWebhook() {

        GenerateWebhookRequest request =
                new GenerateWebhookRequest(
                        "John Doe",
                        regNo,
                        "john@example.com"
                );

        ResponseEntity<GenerateWebhookResponse> response =
                restTemplate.postForEntity(
                        GENERATE_URL,
                        request,
                        GenerateWebhookResponse.class
                );

        return response.getBody();
    }

    private String decideSqlQuery() {

        int lastTwoDigits =
                Integer.parseInt(regNo.substring(regNo.length() - 2));

        if (lastTwoDigits % 2 != 0) {
        	return "SELECT d.DEPARTMENT_NAME, " +
        		       "t.total_salary AS SALARY, " +
        		       "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS EMPLOYEE_NAME, " +
        		       "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE " +
        		       "FROM ( " +
        		       "SELECT e.EMP_ID, e.DEPARTMENT, SUM(p.AMOUNT) AS total_salary, " +
        		       "ROW_NUMBER() OVER (PARTITION BY e.DEPARTMENT ORDER BY SUM(p.AMOUNT) DESC) AS rn " +
        		       "FROM EMPLOYEE e " +
        		       "JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
        		       "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
        		       "GROUP BY e.EMP_ID, e.DEPARTMENT " +
        		       ") t " +
        		       "JOIN EMPLOYEE e ON t.EMP_ID = e.EMP_ID " +
        		       "JOIN DEPARTMENT d ON t.DEPARTMENT = d.DEPARTMENT_ID " +
        		       "WHERE t.rn = 1;";

        } else {
        	return "SELECT d.DEPARTMENT_NAME, " +
        		       "AVG(TIMESTAMPDIFF(YEAR, e.DOB, CURDATE())) AS AVERAGE_AGE, " +
        		       "SUBSTRING_INDEX(GROUP_CONCAT(CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) " +
        		       "ORDER BY e.FIRST_NAME SEPARATOR ', '), ', ', 10) AS EMPLOYEE_LIST " +
        		       "FROM ( " +
        		       "SELECT e.EMP_ID, e.DEPARTMENT, SUM(p.AMOUNT) AS total_salary " +
        		       "FROM EMPLOYEE e " +
        		       "JOIN PAYMENTS p ON e.EMP_ID = p.EMP_ID " +
        		       "GROUP BY e.EMP_ID, e.DEPARTMENT " +
        		       "HAVING SUM(p.AMOUNT) > 70000 " +
        		       ") t " +
        		       "JOIN EMPLOYEE e ON t.EMP_ID = e.EMP_ID " +
        		       "JOIN DEPARTMENT d ON t.DEPARTMENT = d.DEPARTMENT_ID " +
        		       "GROUP BY d.DEPARTMENT_ID, d.DEPARTMENT_NAME " +
        		       "ORDER BY d.DEPARTMENT_ID DESC;";

        }
    }

    private void submitFinalQuery(String webhookUrl,
                                  String accessToken,
                                  String finalQuery) {

        HttpHeaders headers = new HttpHeaders();
       headers.setContentType(MediaType.APPLICATION_JSON);
       headers.set("Authorization", accessToken);

        FinalQueryRequest request =
                new FinalQueryRequest(finalQuery);

        HttpEntity<FinalQueryRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(
                        webhookUrl,
                        HttpMethod.POST,
                        entity,
                        String.class
                );

        System.out.println("Submission Response: " + response.getBody());
    }
}
