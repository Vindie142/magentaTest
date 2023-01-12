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
public class ViewController {
	
	@Autowired
	private CityRepo cityRepo;
	@Autowired
	private DistanceRepo distanceRepo;
	
	@GetMapping("/")
	public String getMain(HttpServletRequest request, Model model) {
		model.addAttribute("infoUpload", request.getParameter("infoUpload"));
		return "main";
	}
	
	@GetMapping("/list-of-all-cities-in-the-db")
	public String getListOfAllCitiesInTheDB(Model model) {
		model.addAttribute("cities", cityRepo.findAll());
		return "listOfCities";
	}
	
	@PostMapping("/upload-data-to-the-db")
	public RedirectView postUploadDataToTheDB(@RequestParam("xmlFile") MultipartFile file, 
												RedirectAttributes attributes) {
		try {
			List<Deserializable> result = Etc.objectsFromXMLfile(file);
			for (Deserializable deserializable : result) {
				Object parentEntity = deserializable.toParentEntity(cityRepo);
				if (parentEntity.getClass() == new City().getClass()) {
					City city = (City) parentEntity;
					cityRepo.saveItCorrectly(city);
				} if (parentEntity.getClass() == new Distance().getClass()) {
					Distance distance = (Distance) parentEntity;
					distanceRepo.saveItCorrectly(distance);
				} else {

				}
			}
			attributes.addAttribute("infoUpload", "Successfully");
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			attributes.addAttribute("infoUpload", e.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
			attributes.addAttribute("infoUpload", "Input Output Exception");
		} catch (Exception e) {
			e.printStackTrace();
			attributes.addAttribute("infoUpload", e.getStackTrace());
		}
		
		return new RedirectView("/");
	}
	
}
