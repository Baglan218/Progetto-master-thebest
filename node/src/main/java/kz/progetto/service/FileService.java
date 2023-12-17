package kz.progetto.service;

import kz.progetto.entity.AppDocument;
import org.telegram.telegrambots.meta.api.objects.Message;

public interface FileService {
    AppDocument processDoc(Message externalMesssage);
}
