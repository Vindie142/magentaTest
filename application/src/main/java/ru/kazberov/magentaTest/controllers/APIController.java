package ru.kazberov.magentaTest.controllers;

import java.io.IOException;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import ru.kazberov.magentaTest.models.City;
import ru.kazberov.magentaTest.models.Distance;
import ru.kazberov.magentaTest.repos.CityRepo;
import ru.kazberov.magentaTest.repos.DistanceRepo;
import ru.kazberov.magentaTest.services.Etc;
import ru.kazberov.magentaTest.services.Etc.Deserializable;

@Controller
public class APIController {
	
	@Autowired
	private CityRepo cityRepo;
	@Autowired
	private DistanceRepo distanceRepo;
	
	
	
	
}
