package com.master.rabbit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.master.rabbit.form.BaseSendMsgForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class RabbitService {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private RabbitSender rabbitSender;

    public <T> void handleSendMsg(String appName, String queueName, T data, String cmd, String token) {
        BaseSendMsgForm<T> form = new BaseSendMsgForm<>();
        form.setApp(appName);
        form.setCmd(cmd);
        form.setData(data);
        form.setToken(token);
        String msg;
        try {
            msg = objectMapper.writeValueAsString(form);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // create queue if existed
        createQueueIfNotExist(queueName);

        // push msg
        rabbitSender.send(queueName, msg);
    }

    private void createQueueIfNotExist(String queueName) {
        rabbitSender.createQueueIfNotExist(queueName);
    }
}
