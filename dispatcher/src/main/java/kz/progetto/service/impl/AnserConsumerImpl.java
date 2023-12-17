package kz.progetto.service.impl;

import kz.progetto.controller.UpdateController;
import kz.progetto.service.AnswerConsumer;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import static kz.progetto.model.RabbitQueue.ANSWER_MESSAGE;

public class AnserConsumerImpl implements AnswerConsumer {
    private final UpdateController updateController;

    public AnserConsumerImpl(UpdateController updateController) {
        this.updateController = updateController;
    }

    @Override
    @RabbitListener(queues = ANSWER_MESSAGE)
    public void consume(SendMessage sendMessage) {
        updateController.setView(sendMessage);

    }
}
