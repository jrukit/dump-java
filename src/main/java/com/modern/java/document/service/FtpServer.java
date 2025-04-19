package com.modern.java.document.service;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class FtpServer {
    public boolean uploadFile(String remotePath, String hireePath, MultipartFile file) {
        FTPClient ftpClient = new FTPClient();

        try {
            ftpClient.connect("localhost", 21);
            boolean login = ftpClient.login("user", "123");

            if (!login) {
                throw new RuntimeException("FTP login failed");
            }

            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

            // Change directory or create if doesn't exist
            if (!ftpClient.changeWorkingDirectory(hireePath)) {
                ftpClient.makeDirectory(hireePath);
                ftpClient.changeWorkingDirectory(hireePath);
            }

            boolean success = ftpClient.storeFile(remotePath, file.getInputStream());
            ftpClient.logout();
            ftpClient.disconnect();
            return success;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
