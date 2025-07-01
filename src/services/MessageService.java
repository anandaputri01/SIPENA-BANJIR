package services;

import models.Message;
import repositories.MessageRepository;

import java.util.List;

public class MessageService {
    private final MessageRepository messageRepository;

    public MessageService() {
        this.messageRepository = new MessageRepository();
    }

    public boolean sendMessage(Message message) {
        return messageRepository.create(message);
    }

    public List<Message> getMessagesByReportId(int reportId) {
        return messageRepository.findByReportId(reportId);
    }
}