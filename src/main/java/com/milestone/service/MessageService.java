package com.milestone.service;

import com.milestone.entity.Message;
import com.milestone.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;

    //쪽지 전송
    public Message saveMessage(Message message) {
        return messageRepository.save(message);
    }


}