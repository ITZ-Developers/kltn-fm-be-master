package com.master.service;

import com.master.dto.ApiMessageDto;
import com.master.dto.UploadFileDto;
import com.master.exception.BadRequestException;
import com.master.form.UploadFileForm;
import com.master.model.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MasterApiService {
    static final String[] UPLOAD_TYPES = new String[]{"LOGO", "AVATAR","IMAGE", "DOCUMENT"};
    static final String[] AVATAR_EXTENSION = new String[]{"jpeg", "jpg", "gif", "bmp", "png"};
    @Value("${file.upload-dir}")
    String ROOT_DIRECTORY;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    MasterOTPService masterOTPService;
    @Autowired
    CommonAsyncService commonAsyncService;

    private Map<String, Long> storeQRCodeRandom = new ConcurrentHashMap<>();

    public ApiMessageDto<UploadFileDto> storeFile(UploadFileForm uploadFileForm) {
        ApiMessageDto<UploadFileDto> apiMessageDto = new ApiMessageDto<>();
        try {
            boolean contains = Arrays.stream(UPLOAD_TYPES).anyMatch(uploadFileForm.getType()::equalsIgnoreCase);
            if (!contains) {
                throw new BadRequestException("ERROR-UPLOAD-TYPE-INVALID", "Type is required in LOGO, AVATAR, IMAGE or DOCUMENT");
            }
            String fileName = StringUtils.cleanPath(uploadFileForm.getFile().getOriginalFilename());
            String extension = FilenameUtils.getExtension(fileName);
            if (uploadFileForm.getType().equals("AVATAR") && !Arrays.stream(AVATAR_EXTENSION).anyMatch(extension::equalsIgnoreCase)){
                throw new BadRequestException("ERROR-FILE-FORMAT-INVALID", "File format is invalid");
            }
            //upload to uploadFolder/TYPE/id
            String finalFile = uploadFileForm.getType() + "_" + RandomStringUtils.randomAlphanumeric(10) + "." + extension;
            String typeFolder = File.separator + uploadFileForm.getType();

            Path fileStorageLocation = Paths.get(ROOT_DIRECTORY + typeFolder).toAbsolutePath().normalize();
            Files.createDirectories(fileStorageLocation);
            Path targetLocation = fileStorageLocation.resolve(finalFile);
            Files.copy(uploadFileForm.getFile().getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            UploadFileDto uploadFileDto = new UploadFileDto();
            uploadFileDto.setFilePath(typeFolder + File.separator + finalFile);
            apiMessageDto.setData(uploadFileDto);
            apiMessageDto.setMessage("Upload file success");
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            apiMessageDto.setResult(false);
            apiMessageDto.setMessage("" + e.getMessage());
        }
        return apiMessageDto;
    }

    public void deleteFile(String filePath) {
        File file = new File(ROOT_DIRECTORY + filePath);
//        file.deleteOnExit();
        if(file.exists()) file.delete();
    }

    public Resource loadFileAsResource(String folder, String fileName) {

        try {
            Path fileStorageLocation = Paths.get(ROOT_DIRECTORY + File.separator + folder).toAbsolutePath().normalize();
            Path fP = fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(fP.toUri());
            if (resource.exists()) {
                return resource;
            }
        } catch (MalformedURLException ex) {
            log.error(ex.getMessage(), ex);

        }
        return null;
    }

    public InputStreamResource loadFileAsResourceExt(String folder, String fileName) {

        try {
            File file = new File(ROOT_DIRECTORY + File.separator + folder + File.separator + fileName);
            InputStreamResource inputStreamResource = new InputStreamResource(new FileInputStream(file));
            if (inputStreamResource.exists()) {
                return inputStreamResource;
            }
        } catch (FileNotFoundException ex) {
            log.error(ex.getMessage(), ex);

        }
        return null;
    }

    public String getOTPForgetPassword(){
        return masterOTPService.generate(4);
    }

    public synchronized Long getOrderHash(){
        return Long.parseLong(masterOTPService.generate(9))+System.currentTimeMillis();
    }

    public void sendEmail(String email, String msg, String subject, boolean html){
        commonAsyncService.sendEmail(email,msg,subject,html);
    }

    public String convertGroupToUri(List<Permission> permissions){
        if(permissions!=null){
            StringBuilder builderPermission = new StringBuilder();
            for(Permission p : permissions){
                builderPermission.append(p.getAction().trim().replace("/v1","")+",");
            }
            return  builderPermission.toString();
        }
        return null;
    }

    public String getOrderStt(Long storeId){
        return masterOTPService.orderStt(storeId);
    }

    public synchronized boolean checkCodeValid(String code){
        //delelete key has valule > 60s
        Set<String> keys = storeQRCodeRandom.keySet();
        Iterator<String> iterator = keys.iterator();
        while(iterator.hasNext()){
            String key = iterator.next();
            Long value = storeQRCodeRandom.get(key);
            if((System.currentTimeMillis() - value) > 60000){
                storeQRCodeRandom.remove(key);
            }
        }
        if(storeQRCodeRandom.containsKey(code)){
            return false;
        }
        storeQRCodeRandom.put(code,System.currentTimeMillis());
        return true;
    }
}
