package com.udacity.jwdnd.course1.cloudstorage;

import com.udacity.jwdnd.course1.cloudstorage.model.File;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.file.Files;
import java.nio.file.Path;
import org.openqa.selenium.By;

/**
 * Tests for File Upload, View, Download, and Deletion.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FileTests extends CloudStorageApplicationTests {

    private final String testFileName = "testFile.txt";
    private final String testFileContent = "This is a test file content.";

    /**
     * Test that uploads a file and verifies it is displayed.
     */
    @Test
    public void testFileUploadAndDisplay() throws Exception {
        // Create a temporary test file
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, testFileContent.getBytes());
        String testFileName = tempFile.getFileName().toString();

        HomePage homePage = signUpAndLogin();

        // Debug: Check current page
        System.out.println("Current URL: " + driver.getCurrentUrl());
        System.out.println("Page title: " + driver.getTitle());

        // Wait for the page to load
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Upload the file
        homePage.uploadFile(tempFile.toString());

        // Wait for page to reload after upload
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify the file is displayed in the table
        Assertions.assertTrue(homePage.isFileDisplayed(testFileName), "File should be displayed after upload");

        // Clean up
        deleteFile(homePage, testFileName);
        Files.deleteIfExists(tempFile);
    }

    /**
     * Test that verifies a file can be viewed/downloaded.
     */
    @Test
    public void testFileView() throws Exception {
        // Create a temporary test file
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, testFileContent.getBytes());
        String testFileName = tempFile.getFileName().toString();

        HomePage homePage = signUpAndLogin();

        // Upload the file
        homePage.uploadFile(tempFile.toString());
        Assertions.assertTrue(homePage.isFileDisplayed(testFileName), "File should be displayed after upload");

        // View the file (this should open in a new tab/window)
        homePage.viewFile(testFileName);

        // Note: In a real test, we would need to handle the new window/tab
        // For now, we'll just verify the file exists

        // Clean up
        deleteFile(homePage, testFileName);
        Files.deleteIfExists(tempFile);
    }

    /**
     * Test that verifies a file can be deleted.
     */
    @Test
    public void testFileDelete() throws Exception {
        // Create a temporary test file
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, testFileContent.getBytes());
        String testFileName = tempFile.getFileName().toString();

        HomePage homePage = signUpAndLogin();

        // Upload the file
        homePage.uploadFile(tempFile.toString());
        Assertions.assertTrue(homePage.isFileDisplayed(testFileName), "File should be displayed after upload");

        // Delete the file
        deleteFile(homePage, testFileName);

        // Verify the file is no longer displayed
        Assertions.assertFalse(homePage.isFileDisplayed(testFileName), "File should not be displayed after deletion");

        Files.deleteIfExists(tempFile);
    }

    /**
     * Test that verifies duplicate file names are not allowed.
     */
    @Test
    public void testDuplicateFileUpload() throws Exception {
        // Create a temporary test file
        Path tempFile = Files.createTempFile("test", ".txt");
        Files.write(tempFile, testFileContent.getBytes());
        String testFileName = tempFile.getFileName().toString();

        HomePage homePage = signUpAndLogin();

        // Upload the file first time
        homePage.uploadFile(tempFile.toString());
        Assertions.assertTrue(homePage.isFileDisplayed(testFileName), "File should be displayed after first upload");

        // Try to upload the same file again
        homePage.uploadFile(tempFile.toString());

        // Check if error message is displayed
        Assertions.assertTrue(homePage.isErrorMessageDisplayed(), "Error message should be displayed for duplicate file");

        // Clean up
        deleteFile(homePage, testFileName);
        Files.deleteIfExists(tempFile);
    }

    private void deleteFile(HomePage homePage, String fileName) {
        // If we're currently on the result page (e.g., after a failed upload),
        // click the result link to return to the home page first.
        ResultPage resultPage = new ResultPage(driver);
        boolean resultLinkPresent = !driver.findElements(By.id("aResultSuccess")).isEmpty()
                || !driver.findElements(By.id("aResultNotSaved")).isEmpty()
                || !driver.findElements(By.id("aResultFailure")).isEmpty();

        if (resultLinkPresent) {
            if (!driver.findElements(By.id("aResultSuccess")).isEmpty()) {
                driver.findElement(By.id("aResultSuccess")).click();
            } else if (!driver.findElements(By.id("aResultNotSaved")).isEmpty()) {
                driver.findElement(By.id("aResultNotSaved")).click();
            } else if (!driver.findElements(By.id("aResultFailure")).isEmpty()) {
                driver.findElement(By.id("aResultFailure")).click();
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Now perform delete from the home page and acknowledge the result.
        homePage.deleteFile(fileName);
        resultPage.clickOk();
    }
}