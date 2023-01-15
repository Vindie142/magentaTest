package ru.kazberov.magentaTest.controllers;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import ru.kazberov.magentaTest.models.City;
import ru.kazberov.magentaTest.models.Distance;
import ru.kazberov.magentaTest.repos.CityRepo;
import ru.kazberov.magentaTest.repos.DistanceRepo;
import ru.kazberov.magentaTest.services.Etc;
import ru.kazberov.magentaTest.services.TheTravelingSalesmanTask;
import ru.kazberov.magentaTest.services.XMLhelper;
import ru.kazberov.magentaTest.services.XMLhelper.Deserializable;

@RestController
public class RESTfulApiController {
	
	@Autowired
	private CityRepo cityRepo;
	@Autowired
	private DistanceRepo distanceRepo;
	
	
	@GetMapping("/information-in-db")
	public ResponseEntity<?> getListOfAllCities() {
		try {
			List<City> cities = cityRepo.findAll();
            return new ResponseEntity<>(cities, HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>(new HttpError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()),
            							HttpStatus.INTERNAL_SERVER_ERROR);
        }
	}
	
	@GetMapping("/calculate")
	public ResponseEntity<?> getCalculateDistance(HttpServletRequest request) {
		String calculationType = request.getParameter("calculation_type");
		String fromCityName = request.getParameter("from_city");
		String toCityName = request.getParameter("to_city");
		if (calculationType == null || fromCityName == null || toCityName == null) {
			return new ResponseEntity<>(new HttpError(HttpStatus.BAD_REQUEST.value(), "Insufficient input parameters"),
										HttpStatus.BAD_REQUEST);
		}
		calculationType = calculationType.trim();
		Optional<City> optFromCity = cityRepo.findByName(fromCityName.trim());
		Optional<City> optToCity = cityRepo.findByName(toCityName.trim());
		if (optFromCity.isEmpty()) {
			return new ResponseEntity<>(new HttpError(HttpStatus.NOT_FOUND.value(), "The city " + fromCityName + " does not exist"),
										HttpStatus.NOT_FOUND);
		}
		if (optToCity.isEmpty()) {
			return new ResponseEntity<>(new HttpError(HttpStatus.NOT_FOUND.value(), "The city " + toCityName + " does not exist"),
										HttpStatus.NOT_FOUND);
		}
		City fromCity = optFromCity.get();
		City toCity = optToCity.get();
		
		if ( !calculationType.equals("Crowflight") 
							&& !calculationType.equals("Distance Matrix")
							&& !calculationType.equals("All") ) {
			return new ResponseEntity<>(new HttpError(HttpStatus.BAD_REQUEST.value(), "Not acceptable calculation type"),
										HttpStatus.BAD_REQUEST);
		}
		
		Response1 response = new Response1(fromCity.getName() +" -> " + toCity.getName());
		switch (calculationType) {
		case "Crowflight" :
			try {
				BigDecimal result = Etc.calcDistanceBetweenTwoCoords(fromCity.getLatitude(), fromCity.getLongitude(),
						toCity.getLatitude(), toCity.getLongitude());
				response.crowflight = result;
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<>(new HttpError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()),
											HttpStatus.INTERNAL_SERVER_ERROR);
			}
			break;
		case "Distance Matrix" :
			try {
				BigDecimal result2 = TheTravelingSalesmanTask.calc(fromCity, toCity, cityRepo);
				if (result2 == null) {
					return new ResponseEntity<>(new HttpError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Couldn't find the route"),
																HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					response.distance_matrix = result2;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<>(new HttpError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()),
											HttpStatus.INTERNAL_SERVER_ERROR);
			}
			break;
		case "All" :
			try {
				BigDecimal result = Etc.calcDistanceBetweenTwoCoords(fromCity.getLatitude(), fromCity.getLongitude(),
						toCity.getLatitude(), toCity.getLongitude());
				response.crowflight = result;
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<>(new HttpError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()),
											HttpStatus.INTERNAL_SERVER_ERROR);
			}
			try {
				BigDecimal result2 = TheTravelingSalesmanTask.calc(fromCity, toCity, cityRepo);
				if (result2 == null) {
					return new ResponseEntity<>(new HttpError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Couldn't find the route"),
																HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					response.distance_matrix = result2;
				}
			} catch (Exception e) {
				e.printStackTrace();
				return new ResponseEntity<>(new HttpError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()),
											HttpStatus.INTERNAL_SERVER_ERROR);
			}
			break;
		}
		return new ResponseEntity<>(response, HttpStatus.OK);
	}
	
	@PostMapping("/information-in-db")
	public ResponseEntity<?> putUploadData (@RequestParam(value="xml_file", required=false) MultipartFile file) {
		if (file == null) {
			return new ResponseEntity<>(new HttpError(HttpStatus.BAD_REQUEST.value(), "Insufficient input parameters"),
										HttpStatus.BAD_REQUEST);
		}
		try {
			List<Deserializable> result = XMLhelper.objectsFromXMLfile(file);
			int count = 0;
			for (Deserializable deserializable : result) {
				try {
					Object parentEntity = deserializable.toParentEntity(cityRepo);
					if (parentEntity.getClass() == new City().getClass()) {
						City city = (City) parentEntity;
						cityRepo.saveItCorrectly(city);
					} else if (parentEntity.getClass() == new Distance().getClass()) {
						Distance distance = (Distance) parentEntity;
						distanceRepo.saveItCorrectly(distance.reverse());
						distanceRepo.saveItCorrectly(distance);
					} else {
						throw new Exception("Unsuitable class");
					}
					count++;
				} catch (DataAccessException e) {
					// we ignore it because one or more entries are allowed to be erroneous
					e.printStackTrace();
				} catch (Exception e) {
					// we ignore it because one or more entries are allowed to be erroneous
					e.printStackTrace();
				}
			}
			if (count == result.size()) {
				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				return new ResponseEntity<>(new HttpError(HttpStatus.PARTIAL_CONTENT.value(), "Successfully only " + count + " /" + result.size()),
											HttpStatus.PARTIAL_CONTENT);
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return new ResponseEntity<>(new HttpError(HttpStatus.BAD_REQUEST.value(), e.getMessage()),
										HttpStatus.BAD_REQUEST);
		} catch (IOException e) {
			e.printStackTrace();
			return new ResponseEntity<>(new HttpError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Input Output Exception"),
										HttpStatus.INTERNAL_SERVER_ERROR);
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
	}
	
	
	
	
	
	
	/**
	 * Class for sending error information
	 */
	public static class HttpError {
		public int statusCode;
		public String message;
		
	    public HttpError() {}
	    public HttpError(int statusCode, String message) {
	        this.statusCode = statusCode;
	        this.message = message;
	    }
	}
	
	/**
	 * the class for sending the response1
	 */
	public static class Response1 {
		public BigDecimal crowflight;
		public BigDecimal distance_matrix;
		public String from_to;
		
	    public Response1() {}
	    public Response1(String from_to) {
	    	this.from_to = from_to;
	    }
	}
	
}
