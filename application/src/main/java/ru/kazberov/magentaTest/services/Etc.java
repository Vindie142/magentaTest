package ru.kazberov.magentaTest.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import ru.kazberov.magentaTest.models.City;
import ru.kazberov.magentaTest.models.City.DeserializableCity;
import ru.kazberov.magentaTest.models.Distance.DeserializableDistance;
import ru.kazberov.magentaTest.repos.CityRepo;

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
		return input.contains("[//-SsWw]") ? "-" : "";
	}
	
	
	
	
	
	/**
	 * returns a list of objects from an xml file
	 * @param file
	 * @return List<Deserializable>
	 * @throws IOException, IllegalArgumentException
	 */
	public static List<Deserializable> objectsFromXMLfile(MultipartFile file) throws IOException, IllegalArgumentException {
		List<Deserializable> result = new ArrayList<Deserializable>();
		
		InputStream in = file.getInputStream();
		Scanner s = new Scanner(in).useDelimiter("\\A");
        String content = s.hasNext() ? s.next() : "";
        s.close();
        
        if (!content.contains("<")) {
        	throw new IllegalArgumentException("The file's content does not look like XML content!");
		}
        
        List<String> xmlStrings = new ArrayList<String>();
        while (content.contains("<")) {
        	int firstCharOfOpeningTag = content.indexOf("<");
        	String openingTag = content.substring(firstCharOfOpeningTag+1, content.indexOf(">", firstCharOfOpeningTag+1));
        	int lastCharOfClosingTag = content.indexOf("</"+openingTag+">") + openingTag.length()+3;
        	xmlStrings.add( content.substring(firstCharOfOpeningTag, lastCharOfClosingTag) );
        	
        	if (lastCharOfClosingTag+1 >= content.length()) {
				break;
			}
        	content = content.substring(lastCharOfClosingTag + 1);
		}
        
        XmlMapper xmlMapper = new XmlMapper();
        for (String string : xmlStrings) {
        	Deserializable deserializable = null;
        	if (string.contains("<City>")) {
        		deserializable = xmlMapper.readValue(string, DeserializableCity.class);
			} else if (string.contains("<Distance>")) {
				deserializable = xmlMapper.readValue(string, DeserializableDistance.class);
			} else {
				continue;
			}
        	
        	result.add(deserializable);
		} 

		return result;
	}
	/**
	 * for Etc.objectsFromXMLfile()
	 */
	public interface Deserializable {
		public Object toParentEntity(CityRepo cityRepo);
	}
	
}
