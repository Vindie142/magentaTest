package ru.kazberov.magentaTest.services;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import ru.kazberov.magentaTest.models.City.DeserializableCity;
import ru.kazberov.magentaTest.models.Distance.DeserializableDistance;
import ru.kazberov.magentaTest.repos.CityRepo;

public class XMLhelper {

	/**
	 * returns a list of objects from an xml file
	 * @param file
	 * @return 
	 * @throws IOException
	 * @throws IllegalArgumentException
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
