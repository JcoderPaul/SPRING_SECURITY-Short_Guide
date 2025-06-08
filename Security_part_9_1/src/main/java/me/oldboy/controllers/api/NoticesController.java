package me.oldboy.controllers.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
public class NoticesController {
	@RequestMapping(value = "/notices", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<String>> getNotices() throws IOException {
		log.info("REST request health check");
		return new ResponseEntity<>(List.of("Here are the notices details from the DB"), HttpStatus.OK);
	}
}
