package ru.kazberov.magentaTest.services;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.json.JSONObject;

import jakarta.servlet.http.HttpServletRequest;
import ru.kazberov.magentaTest.models.City;
import ru.kazberov.magentaTest.models.Distance;

public class Etc {
	/**
	 * standardizes latitude or longitude
	 * @param input
	 *				55,755831° OR -37,617673°
	 *			OR	N55.755831° OR E37.617673°
	 *			OR	55°45.35′N OR 37°37.06′E
	 *			OR	55°45′20.9916″N OR 37°37′3.6228″E
	 * @return String like -55,7558
	 * @throws IllegalArgumentException
	 */
	public static String standardizeLatOrLong(String input) throws IllegalArgumentException {
		if (input == null || input.length() == 0) {
			throw new IllegalArgumentException("Empty line");
		}
		input = input.replaceAll("(\\s|\\t|\\n|\\r|\\f)", "");
		input = input.replaceAll(",", ".");
		if ((input.matches("(.*)\\.(.*)\\.(.*)")) ) {
			throw new IllegalArgumentException("The entered string does not look like latitude or longitude (extraneous symbol)");
		}
		// has minutes and seconds
		else if (input.matches("(.*)(′|\')(.*)") && input.matches("(.*)(″|\")(.*)")) {
			if (!input.contains("°")) {
				throw new IllegalArgumentException("There is no degree symbol \"°\" ");
			}
			String negOrPositSign = negOrPositVal(input);
			input = input.replaceAll("[\\-&N&n&E&e&S&s&W&w]+", "");
			int degreeSymbol = input.indexOf("°");
			int minSymbol = input.contains("′") ? input.indexOf("′") : input.indexOf("\'");
			int secSymbol = input.contains("″") ? input.indexOf("″") : input.indexOf("\"");
			BigDecimal degrees = new BigDecimal( input.substring(0, degreeSymbol) );
			BigDecimal mins = new BigDecimal( input.substring(degreeSymbol+1, minSymbol) );
			BigDecimal secs = new BigDecimal( input.substring(minSymbol+1, secSymbol) );
			degrees = degrees.add( mins.divide(new BigDecimal("60"), City.NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN) )
								.add( secs.divide(new BigDecimal("3600"), City.NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN) )
								.setScale(City.NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN);
			return negOrPositSign + degrees.toPlainString();
		}
		// has minutes
		else if (input.matches("(.*)(′|\')(.*)")) {
			if (!input.contains("°")) {
				throw new IllegalArgumentException("There is no degree symbol \"°\" ");
			}
			String negOrPositSign = negOrPositVal(input);
			input = input.replaceAll("[\\-&N&n&E&e&S&s&W&w]+", "");
			int degreeSymbol = input.indexOf("°");
			int minSymbol = input.contains("′") ? input.indexOf("′") : input.indexOf("\'");
			BigDecimal degrees = new BigDecimal( input.substring(0, degreeSymbol) );
			BigDecimal mins = new BigDecimal( input.substring(degreeSymbol+1, minSymbol) );
			degrees = degrees.add( mins.divide(new BigDecimal("60"), City.NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN) )
								.setScale(City.NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN);
			return negOrPositSign + degrees.toPlainString();
		}
		// just degrees
		else {
			String negOrPositSign = negOrPositVal(input);
			input = input.replaceAll("[°&\\-&N&n&E&e&S&s&W&w]+", "");
			BigDecimal degrees = new BigDecimal(input).setScale(City.NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN);;
			return negOrPositSign + degrees.toPlainString();
		}
		
	}
	
	/**
	 * @param input of latitude or longitude
	 * @return "-" or ""
	 */
	private static String negOrPositVal(String input){
		return input.contains("-") || input.matches("(.*)[SsWw](.*)") ? "-" : "";
	}
	
	/**
	 * calculates the distance between two coordinates
	 * @param lat1BD
	 * @param lon1BD
	 * @param lat2BD
	 * @param lon2BD
	 * @return
	 * @throws IllegalArgumentException
	 * @throws NullPointerException
	 */
	public static BigDecimal calcDistanceBetweenTwoCoords(BigDecimal lat1BD, BigDecimal lon1BD,
															BigDecimal lat2BD, BigDecimal lon2BD)
															throws IllegalArgumentException, NullPointerException{
		if (lat1BD == null || lon1BD == null || lat2BD == null || lon2BD == null) {
			throw new NullPointerException("Еhere is not enough data in the data database");
		}
		if (!new City(lat1BD, lon1BD).ifCorrectLatAndLon() || !new City(lat2BD, lon2BD).ifCorrectLatAndLon()) {
			throw new IllegalArgumentException("Latitude or longitude are in invalid ranges");
		}
		
		double lat1 = lat1BD.doubleValue();
		double lon1 = lon1BD.doubleValue();
		double lat2 = lat2BD.doubleValue();
		double lon2 = lon2BD.doubleValue();
		
		// double result = 6372.795 * Math.atan(Math.sqrt(Math.pow(Math.cos(lat2)*Math.sin(lon2-lon1), 2) + Math.pow( (Math.cos(lat1)*Math.sin(lat2)) - (Math.sin(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1)), 2)) 
		// 										/ ( (Math.sin(lat1)*Math.sin(lat2)) + (Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1)) ) );
		double result = 6372.795 * 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(0.5*(lat2-lat1)), 2) + ( Math.cos(lat1)*Math.cos(lat2)*Math.pow(Math.sin(0.5*(lon2-lon1)), 2) ) ));
		// double result = 6372.795 * Math.acos(Math.sin(lat1)*Math.sin(lat2) + Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon1 - lon2) );
		// double result = 111.2 * Math.sqrt( (lon1 - lon2)*(lon1 - lon2) + (lat1 - lat2)*Math.cos(Math.PI*lon1/180)*(lat1 - lat2)*Math.cos(Math.PI*lon1/180));
		return new BigDecimal(result).setScale(Distance.NUMBER_OF_DECIMAL_OF_DIST, RoundingMode.HALF_DOWN);
	}
	
	/**
	 * finds the value by key by JSONObject
	 * @param string
	 * @param neededKey
	 * @return String
	 */
	public static String valFormJSON(String string, String neededKey) {
		JSONObject json = new JSONObject(string);
		return String.valueOf( json.get(neededKey) );
	}
	
	public static String domenFromRequest(HttpServletRequest sRequest) {
		String fullDomen = sRequest.getHeader("referer").trim();
		
		int firstDomenIndex = fullDomen.indexOf("://") + "://".length();
		int pastDomenIndex = fullDomen.indexOf('/', firstDomenIndex);
		return fullDomen.substring(firstDomenIndex, pastDomenIndex);
	}
	
}
