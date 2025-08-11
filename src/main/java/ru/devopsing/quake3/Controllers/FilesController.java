package ru.devopsing.quake3.Controllers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.jboss.resteasy.annotations.providers.multipart.MultipartForm;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import ru.devopsing.quake3.Services.MatchLogParserService;

@Path("/files")
public class FilesController {

    @Inject
    MatchLogParserService matchLogParserService;

    // @Inject
    // MatchService matchService;

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public void uploadLog(@MultipartForm MultipartInput input) {
        for (InputPart part : input.getParts()) {
            try {
                // Extract InputStream from uploaded file
                InputStream inputStream = part.getBody(InputStream.class, null);
                
                // Read and process the file line by line
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    matchLogParserService.parseLog(reader);
                    // String line;
                    // while ((line = reader.readLine()) != null) {
                        // System.out.println(line);
                        // matchService.processLog(line);
                        // Thread.sleep(1);
                    // }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}