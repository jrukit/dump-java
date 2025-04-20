package com.modern.java.document.service;

import org.apache.commons.net.ftp.FTPClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import static org.assertj.core.api.Assertions.assertThat;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.never;
import static org.mockito.BDDMockito.times;

@ExtendWith(MockitoExtension.class)
class FtpServerTest {
    @InjectMocks
    @Spy
    private FtpServer ftpServer;
    @Mock
    private FTPClient ftpClient;
    @Test
    void shouldBeTrue_WhenCallUploadFile() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "document",
                "dump.jpg",
                "image/jpeg",
                "Dummy file content".getBytes(StandardCharsets.UTF_8)
        );

        BDDMockito.willReturn(ftpClient).given(ftpServer).getFtpClient();
        BDDMockito.given(ftpClient.login("user", "123")).willReturn(true);
        BDDMockito.given(ftpClient.storeFile(eq("/remote-path"), any())).willReturn(true);

        Assertions.assertTrue(ftpServer
                .uploadFile("/remote-path", "/hiree-path", mockFile));
        Mockito.verify(ftpClient).enterLocalPassiveMode();
        Mockito.verify(ftpClient).setFileType(2);
        Mockito.verify(ftpServer).createWorkingDir("/hiree-path", ftpClient);
        Mockito.verify(ftpClient).logout();
        Mockito.verify(ftpClient).disconnect();
    }

    @Test
    void shouldBeFalse_WhenCallUploadFile_WithLoginFailed() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "document",
                "dump.jpg",
                "image/jpeg",
                "Dummy file content".getBytes(StandardCharsets.UTF_8)
        );

        BDDMockito.willReturn(ftpClient).given(ftpServer).getFtpClient();
        BDDMockito.given(ftpClient.login("user", "123")).willReturn(false);

        Assertions.assertFalse(ftpServer
                .uploadFile("/remote-path", "/hiree-path", mockFile));
        Mockito.verify(ftpClient, never()).enterLocalPassiveMode();
        Mockito.verify(ftpClient, never()).setFileType(anyInt());
        Mockito.verify(ftpClient, never()).storeFile(anyString(), any());
        Mockito.verify(ftpServer, never()).createWorkingDir(anyString(), any());
        Mockito.verify(ftpClient, never()).logout();
        Mockito.verify(ftpClient, never()).disconnect();
    }

    @Test
    void shouldBeFalse_WhenCallUploadFile_WithStoreFileFailed() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile(
                "document",
                "dump.jpg",
                "image/jpeg",
                "Dummy file content".getBytes(StandardCharsets.UTF_8)
        );

        BDDMockito.willReturn(ftpClient).given(ftpServer).getFtpClient();
        BDDMockito.given(ftpClient.login("user", "123")).willReturn(true);
        BDDMockito.given(ftpClient.storeFile(eq("/remote-path"), any())).willReturn(false);

        Assertions.assertFalse(ftpServer
                .uploadFile("/remote-path", "/hiree-path", mockFile));
        BDDMockito.verify(ftpClient).enterLocalPassiveMode();
        BDDMockito.verify(ftpClient).setFileType(anyInt());
        BDDMockito.verify(ftpServer).createWorkingDir(anyString(), any());
        BDDMockito.verify(ftpClient).logout();
        BDDMockito.verify(ftpClient).disconnect();
    }

    @Test
    void shouldBeFTPClient_WhenCallGetFtpClient() {
        assertThat(ftpServer.getFtpClient()).isInstanceOf(FTPClient.class);
    }

    @Test
    void shouldDoNothing_WhenCallCreateWorkingDir_WithDirectoryExist() throws IOException {
        BDDMockito.given(ftpClient.changeWorkingDirectory("/hiree-path")).willReturn(true);
        Assertions.assertDoesNotThrow(() -> ftpServer.createWorkingDir("/hiree-path", ftpClient));
    }

    @Test
    void shouldCreateDirectory_WhenCallCreateWorkingDir_WithDirectoryDoesNotExist() throws IOException {
        BDDMockito.given(ftpClient.changeWorkingDirectory("/hiree-path")).willReturn(false);
        Assertions.assertDoesNotThrow(() -> ftpServer.createWorkingDir("/hiree-path", ftpClient));

        BDDMockito.verify(ftpClient).makeDirectory("/hiree-path");
        BDDMockito.verify(ftpClient, times(2)).changeWorkingDirectory("/hiree-path");
    }
}