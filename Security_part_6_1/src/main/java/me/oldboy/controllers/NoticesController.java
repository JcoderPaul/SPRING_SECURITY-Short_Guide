package me.oldboy.controllers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
public class NoticesController {
	@GetMapping(value = "/notices", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> getNotices() throws IOException {
		log.info("REST request health check");
		return new ResponseEntity<>("Here are the notices details from the DB", HttpStatus.OK);
	}
}
