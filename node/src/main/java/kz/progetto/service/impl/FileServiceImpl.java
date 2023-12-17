package kz.progetto.service.impl;


import kz.progetto.dao.AppDocumentDAO;
import kz.progetto.dao.BinaryContentDAO;
import kz.progetto.entity.AppDocument;
import kz.progetto.entity.BinaryContent;
import kz.progetto.exception.UploadFileException;
import kz.progetto.service.FileService;
import lombok.extern.log4j.Log4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.print.DocFlavor;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

@Log4j
@Service
public class FileServiceImpl implements FileService {
    @Value("${token}")
    private String token;
    @Value("${service.file_info.url}")
    private String fileInfoUri;
    @Value("${service.file_storage.uri}")
    private String fileStorageUri;
    private final AppDocumentDAO appDocumentDAO;
    private final BinaryContentDAO binaryContentDAO;

    public FileServiceImpl(AppDocumentDAO appDocumentDAO, BinaryContentDAO binaryContentDAO) {
        this.appDocumentDAO = appDocumentDAO;
        this.binaryContentDAO = binaryContentDAO;
    }


    @Override
    public AppDocument processDoc(Message telegramMessage) {
        String fileId = telegramMessage.getDocument().getFileId();
        ResponseEntity<Spring> response = getFilePath(fileId);
        if (response.getStatusCode() == HttpStatus.OK){
            JSONObject jsonObject = new JSONObject(response.getBody());
            String filePath =String.valueOf(jsonObject
                    .getJSONObject("result")
                    .getString("file_path"));
            byte[] fileInByte = downloadFile(filePath);
            BinaryContent transientBinaryContent = BinaryContent.builder()
                    .fileAsArrayOfBytes(fileInByte)
                    .build();
            BinaryContent persistentBinaryContent = binaryContentDAO.save(transientBinaryContent);
            Document telegramDoc = telegramMessage.getDocument();
            AppDocument transientAppDoc = buildTransientAppDoc(telegramDoc,persistentBinaryContent);
            return appDocumentDAO.save(transientAppDoc);
        }else{
            throw new UploadFileException("Bad response from telegram service: " + response);
        }
    }


    private AppDocument buildTransientAppDoc(Document telegramDoc, BinaryContent persistentBinaryContent) {
        return AppDocument.builder()
                .telegramFileId(telegramDoc.getFileId())
                .docName(telegramDoc.getFileName())
                .binaryContent(persistentBinaryContent)
                .mimeType(telegramDoc.getMimeType())
                .fileSize(telegramDoc.getFileSize())
                .build();

    }

    private ResponseEntity<Spring> getFilePath(String fileId){
        RestTemplate restTomplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> request = new HttpEntity<>(headers);

        return restTomplate.exchange(
                fileInfoUri,
                HttpMethod.GET,
                request,
                Spring.class,
                token, fileId
        );
    }
    private byte[] downloadFile(String filePath) {
        String fullUri = fileStorageUri.replace("{token}",token)
                .replace("filePath",filePath);
                URL urlObj = null;
        try{
            urlObj = new URL(fullUri);
        }catch(MalformedURLException e){
            throw new UploadFileException(e);

        }
        try(InputStream is= urlObj.openStream()){
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UploadFileException(urlObj.toExternalForm(),e);
        }
    }

}
