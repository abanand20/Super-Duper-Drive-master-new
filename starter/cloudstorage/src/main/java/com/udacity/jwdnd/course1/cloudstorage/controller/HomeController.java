package com.udacity.jwdnd.course1.cloudstorage.controller;

import com.udacity.jwdnd.course1.cloudstorage.model.File;
import com.udacity.jwdnd.course1.cloudstorage.model.User;
import com.udacity.jwdnd.course1.cloudstorage.model.FileForm;
import com.udacity.jwdnd.course1.cloudstorage.model.NoteForm;
import com.udacity.jwdnd.course1.cloudstorage.model.CredentialForm;
import com.udacity.jwdnd.course1.cloudstorage.services.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/home")
public class HomeController {

    private final FileService fileService;
    private final UserService userService;
    private final NoteService noteService;
    private final CredentialService credentialService;
    private final EncryptionService encryptionService;

    public HomeController(
            FileService fileService, UserService userService, NoteService noteService,
            CredentialService credentialService, EncryptionService encryptionService) {
        this.fileService = fileService;
        this.userService = userService;
        this.noteService = noteService;
        this.credentialService = credentialService;
        this.encryptionService = encryptionService;
    }

    @GetMapping
    public String getHomePage(
            Authentication authentication, @ModelAttribute("newFile") FileForm newFile,
            @ModelAttribute("newNote") NoteForm newNote, @ModelAttribute("newCredential") CredentialForm newCredential,
            Model model) {
        Integer userId = getUserId(authentication);
        model.addAttribute("files", this.fileService.getFiles(userId));
        model.addAttribute("notes", noteService.getNoteListings(userId));
        model.addAttribute("credentials", credentialService.getCredentialListings(userId));
        model.addAttribute("encryptionService", encryptionService);
        model.addAttribute("newFile", new FileForm());
        model.addAttribute("newNote", new NoteForm());
        model.addAttribute("newCredential", new CredentialForm());

        return "home";
    }

    @PostMapping("/search-files")
    public String searchFiles(Authentication authentication, @RequestParam("query") String query, Model model) {
        Integer userId = getUserId(authentication);
        List<File> allFiles = fileService.getFiles(userId);
        List<File> searchResults = allFiles.stream()
            .filter(file -> file.getFileName().toLowerCase().contains(query.toLowerCase()))
            .collect(Collectors.toList());
        model.addAttribute("searchResults", searchResults);
        model.addAttribute("files", this.fileService.getFiles(userId));
        model.addAttribute("notes", noteService.getNoteListings(userId));
        model.addAttribute("credentials", credentialService.getCredentialListings(userId));
        model.addAttribute("encryptionService", encryptionService);
        model.addAttribute("newFile", new FileForm());
        model.addAttribute("newNote", new NoteForm());
        model.addAttribute("newCredential", new CredentialForm());
        return "home";
    }

    private Integer getUserId(Authentication authentication) {
        String userName = authentication.getName();
        User user = userService.getUser(userName);
        return user.getUserId();
    }

    @PostMapping
    public String newFile(
            Authentication authentication, @ModelAttribute("newFile") FileForm newFile,
            @ModelAttribute("newNote") NoteForm newNote, @ModelAttribute("newCredential") CredentialForm newCredential, Model model) throws IOException {
        String userName = authentication.getName();
        User user = userService.getUser(userName);
        Integer userId = user.getUserId();
        String[] fileListings = fileService.getFileListings(userId);
        if(newFile.getFile().getOriginalFilename().trim().isEmpty()){
            model.addAttribute("result", "notSaved");
        } else {
//                model.addAttribute("result", "error");
//           }

            MultipartFile multipartFile = newFile.getFile();
            String fileName = multipartFile.getOriginalFilename();

            boolean fileIsDuplicate = false;
            for (String fileListing : fileListings) {
                if (fileListing.equals(fileName)) {
                    fileIsDuplicate = true;

                    break;
                }

            }

            if (!fileIsDuplicate) {
                fileService.addFile(multipartFile, userName);
                model.addAttribute("result", "success");
            } else {
                model.addAttribute("result", "error");
                model.addAttribute("message", "You have tried to add a duplicate file.");
            }

        }
        model.addAttribute("files", fileService.getFiles(userId));

        return "result";
    }

    @GetMapping(
            value = "/get-file/{fileName}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> getFile(@PathVariable String fileName, Authentication authentication) {
        Integer userId = getUserId(authentication);
        File file = fileService.getFile(fileName, userId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFileName() + "\"");
        headers.add(HttpHeaders.CONTENT_TYPE, file.getContentType());
        return ResponseEntity.ok()
                .headers(headers)
                .body(file.getFileData());
    }

    @GetMapping(
            value = "/view-file/{fileName}",
            produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<byte[]> viewFile(@PathVariable String fileName, Authentication authentication) {
        Integer userId = getUserId(authentication);
        File file = fileService.getFile(fileName, userId);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, file.getContentType());
        // Note: No Content-Disposition header for viewing - browser will display inline if supported
        return ResponseEntity.ok()
                .headers(headers)
                .body(file.getFileData());
    }

    @GetMapping(value = "/delete-file/{fileName}")
    public String deleteFile(
            Authentication authentication, @PathVariable String fileName, @ModelAttribute("newFile") FileForm newFile,
            @ModelAttribute("newNote") NoteForm newNote, @ModelAttribute("newCredential") CredentialForm newCredential,
            Model model) {
        Integer userId = getUserId(authentication);
        fileService.deleteFile(fileName, userId);
        model.addAttribute("files", fileService.getFiles(userId));
        model.addAttribute("result", "success");

        return "result";
    }
}
