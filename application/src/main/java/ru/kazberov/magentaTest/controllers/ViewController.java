package ru.kazberov.magentaTest.controllers;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import ru.kazberov.magentaTest.models.City;
import ru.kazberov.magentaTest.repos.CityRepo;
import ru.kazberov.magentaTest.services.Etc;

@Controller
public class ViewController {
	
	private final HttpClient httpClient = HttpClient.newBuilder().version(HttpClient.Version.HTTP_2).build();
	@Autowired
	private CityRepo cityRepo;
	
	@GetMapping("/")
	public String getMain(HttpServletRequest request, Model model) {
		model.addAttribute("infoCalc", request.getParameter("infoCalc"));
		model.addAttribute("infoUpload", request.getParameter("infoUpload"));
		model.addAttribute("cities", cityRepo.findAll());
		return "main";
	}
	
	
	
	
	
	@GetMapping("/list-of-all-cities-in-the-db")
	public String getListOfAllCitiesInTheDB(Model model, HttpServletRequest sRequest) {
		final String DOMEN = Etc.domenFromRequest(sRequest);
		String urlString = "http://" + DOMEN + "/information-in-db";
		HttpRequest request = HttpRequest.newBuilder()
						.GET()
						.header("Accept", "application/json")
						.uri(URI.create(urlString))
						.build();
		
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				String info = "Code "
								+ Etc.valFormJSON(response.body(), "statusCode") 
								+ ": "
								+ Etc.valFormJSON(response.body(), "message");
				model.addAttribute("info", info);
				return "listOfCities";
			}
			List<City> cities = new ObjectMapper().readValue(new JsonFactory().createParser(response.body()),
																new TypeReference<List<City>>() { });
			model.addAttribute("cities", cities);
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("info", "Something went wrong =(");
		}
		return "listOfCities";
	}
	
	
	
	
	
	@GetMapping("/calculating")
	public RedirectView getCalculating(RedirectAttributes attributes, HttpServletRequest sRequest) {
		final String DOMEN = Etc.domenFromRequest(sRequest);
		String urlString = UriComponentsBuilder.fromUriString("http://" + DOMEN + "/calculate")
										.queryParam("calculation_type", sRequest.getParameter("calculationType"))
										.queryParam("from_city", sRequest.getParameter("fromCity"))
										.queryParam("to_city", sRequest.getParameter("toCity"))
										.build().toUriString().replaceAll(" ", "+");
		HttpRequest request = HttpRequest.newBuilder()
						.GET()
						.header("Accept", "application/json")
						.uri(URI.create(urlString))
						.build();
		
		try {
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				String info = "Code "
								+ Etc.valFormJSON(response.body(), "statusCode") 
								+ ": "
								+ Etc.valFormJSON(response.body(), "message");
				attributes.addAttribute("infoCalc", info);
				return new RedirectView("/");
			}
			RESTfulApiController.Response1 responsEntity = new ObjectMapper().readValue(new JsonFactory().createParser(response.body()),
																		new TypeReference<RESTfulApiController.Response1>() { });
			String crowflight = responsEntity.crowflight == null ? "" : "<br>Crowflight: " + responsEntity.crowflight + " km";
			String distanceMatrix = responsEntity.distance_matrix == null ? "" : "<br>Distance matrix: " + responsEntity.distance_matrix + " unit of measurement";
			attributes.addAttribute("infoCalc", "FtomTo: " + responsEntity.from_to
												+ crowflight
												+ distanceMatrix);
			return new RedirectView("/");
		} catch (Exception e) {
			e.printStackTrace();
			attributes.addAttribute("infoCalc", "Something went wrong =(");
			return new RedirectView("/");
		}
	}
	
	
	
	
	
	@PostMapping("/upload-data-to-the-db")
	public RedirectView postUploadDataToTheDB(@RequestParam("xmlFile") MultipartFile file, 
												RedirectAttributes attributes, HttpServletRequest sRequest) {
		final String DOMEN = Etc.domenFromRequest(sRequest);
		String urlString = "http://" + DOMEN + "/information-in-db";
		
		try {
			HttpRequest request = HttpRequest.newBuilder()
					.PUT(HttpRequest.BodyPublishers.ofByteArray(file.getBytes()))
					.header("Accept", "application/json")
					.uri(URI.create(urlString))
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() != 200) {
				String info = "Code "
								+ Etc.valFormJSON(response.body(), "statusCode") 
								+ ": "
								+ Etc.valFormJSON(response.body(), "message");
				attributes.addAttribute("infoUpload", info);
				return new RedirectView("/");
			} else {
				attributes.addAttribute("infoUpload", "Successfully");
				return new RedirectView("/");
			}
		} catch (Exception e) {
			e.printStackTrace();
			attributes.addAttribute("infoUpload", "Something went wrong =(");
			return new RedirectView("/");
		}
	}
	
}
