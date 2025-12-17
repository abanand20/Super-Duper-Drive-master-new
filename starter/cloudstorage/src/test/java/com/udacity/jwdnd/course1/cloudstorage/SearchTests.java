package com.udacity.jwdnd.course1.cloudstorage;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Tests for File Search functionality.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SearchTests extends CloudStorageApplicationTests {

    /**
     * Test that verifies the search functionality UI is present and accessible.
     */
    @Test
    public void testSearchFiles() {
        // Sign up and login to access the home page
        HomePage homePage = signUpAndLogin();

        // Check if search tab link exists
        Assertions.assertTrue(homePage.isSearchTabDisplayed(), "Search tab link should be displayed");

        // Navigate to the search tab to make search elements visible
        homePage.navToSearchTab();

        // Wait a bit for the tab to switch
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Check if search input is present and displayed after clicking tab
        Assertions.assertTrue(homePage.isSearchInputDisplayed(), "Search input should be displayed after clicking search tab");

        // Check if search button is present and displayed after clicking tab
        Assertions.assertTrue(homePage.isSearchButtonDisplayed(), "Search button should be displayed after clicking search tab");

        // Logout
        homePage.logout();
    }
}