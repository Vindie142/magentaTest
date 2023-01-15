package ru.kazberov.magentaTest.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import ru.kazberov.magentaTest.repos.CityRepo;
import ru.kazberov.magentaTest.services.Etc;
import ru.kazberov.magentaTest.services.XMLhelper.Deserializable;

@Entity
@Table(name="cities")
public class City implements Comparable<City> {

	/**
	 * the number of decimal places of latitude and longitude
	 */
	public static int NUMBER_OF_DECIMAL_OF_LAT_AND_LONG = 6;
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column (length = 128, nullable = false)
	private String name;
	/**
	 * shirota
	 * the value is stored in degrees
	 * like 55,755831°
	 */
	@JsonIgnore
	@Column(nullable = true)
	private BigDecimal latitude; // shirota
	/**
	 * dolgota
	 * the value is stored in degrees
	 * like 55,755831°
	 */
	@JsonIgnore
	@Column(nullable = true)
	private BigDecimal longitude; // dolgota
	
	@JsonIgnore
	@OneToMany(mappedBy="fromCity", fetch=FetchType.LAZY, orphanRemoval = true, cascade = CascadeType.ALL)
	List<Distance> distances = new ArrayList<Distance>();
	
	
	public City() {}
	public City(String name) {
		this.name = name.trim();
	}
	public City(BigDecimal latitude, BigDecimal longitude) {
		this.latitude = latitude == null ? latitude : latitude.setScale(NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN);
		this.longitude = longitude == null ? longitude : longitude.setScale(NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN);
	}
	public City(String name, BigDecimal latitude, BigDecimal longitude) {
		this(name);
		this.latitude = latitude == null ? latitude : latitude.setScale(NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN);
		this.longitude = longitude == null ? longitude : longitude.setScale(NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN);
	}
	
	/**
	 * checks latitude and longitude for entering acceptable ranges
	 * latitude between −90° and +90°,
	 * longitude between −180° and +180°
	 * you can check only one by substituting null for the second value
	 * if both are null it returns true 
	 * @return true - correct, false - not correct
	 */
	public boolean ifCorrectLatAndLon(){
		if (latitude != null) {
			double latitudeD = latitude.doubleValue();
			if (latitudeD < -90 || latitudeD > 90) {
				return false;
			}
		}
		if (longitude != null) {
			double longitudeD = longitude.doubleValue();
			if (longitudeD < -180 || longitudeD > 180) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String toString() {
		String name = this.name == null ? "" : this.name;
		String latitude = this.latitude == null ? "" : this.latitude.toPlainString();
		String longitude = this.longitude == null ? "" : this.longitude.toPlainString();
		return  "City{"+
				"name="+name+", "+
				"latitude="+latitude+", "+
				"longitude="+longitude+"}";
	}
	
	@Override
    public boolean equals(Object obj) {
	    if (obj == this) {
	        return true;
	    }
	    if (obj == null || obj.getClass() != this.getClass()) {
	        return false;
	    }
	    
	    City mayBe = (City) obj;
	    if (id != null && mayBe.getId() !=null) {
	    	return id == mayBe.getId();
		}
	    return mayBe.toString().equals(this.toString());
    }
	
	@Override
    public int hashCode() {
		int result = 31;
		int a1 = id == null ? 1 : id.hashCode();
	    int a2 = name == null ? 1 : name.hashCode();
	    int a3 = latitude == null ? 1 : latitude.hashCode();
	    int a4 = longitude == null ? 1 : longitude.hashCode();
	    return result * (a1 + a2 + a3 + a4);
    }
	
	/**
	 * Comparison in alphabetical order by name
	 */
	@Override
	public int compareTo(City c) {
		return this.name.toLowerCase().compareTo(c.name.toLowerCase());
	}
	
	/**
	 * Shirota
	 */
	public static class CityComparatorByLatitude implements Comparator<City> {
		   @Override
		   public int compare(City c1, City c2) {
			   return c1.getLatitude().compareTo(c2.getLatitude());
		   }
	}
	/**
	 * Dolgota
	 */
	public static class CityComparatorByLongitude implements Comparator<City> {
		   @Override
		   public int compare(City c1, City c2) {
			   return c1.getLongitude().compareTo(c2.getLongitude());
		   }
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name.trim();
	}
	
	/**
	 * Shirota
	 */
	public BigDecimal getLatitude() {
		return latitude;
	}
	
	/**
	 * Shirota
	 */
	public void setLatitude(BigDecimal latitude) {
		this.latitude = latitude == null ? latitude : latitude.setScale(NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN);
	}
	
	/**
	 * Dolgota
	 */
	public BigDecimal getLongitude() {
		return longitude;
	}
	
	/**
	 * Dolgota
	 */
	public void setLongitude(BigDecimal longitude) {
		this.longitude = longitude == null ? longitude : longitude.setScale(NUMBER_OF_DECIMAL_OF_LAT_AND_LONG, RoundingMode.HALF_DOWN);
	}

	public List<Distance> getDistances() {
		return distances;
	}

	public void setDistances(List<Distance> distances) {
		this.distances = distances;
	}
	
	
	
	
	
	public static class DeserializableCity implements Deserializable {
		public Long id;
		public String name;
		public String latitude;
		public String longitude;
		public DeserializableCity() {}
		
		@Override
		public String toString() {
			String id = this.id == null ? "" : this.id.toString();
			String name = this.name == null ? "" : this.name;
			String latitude = this.latitude == null ? "" : this.latitude;
			String longitude = this.longitude == null ? "" : this.longitude;
			return  "City{"+
					"id="+id+", "+
					"name="+name+", "+
					"latitude="+latitude+", "+
					"longitude="+longitude+"}";
		}
		
		@Override
		public Object toParentEntity(CityRepo cityRepo) {
			City city = new City();
			// city.setId(id);
			city.setName(name);
			try {
				city.setLatitude( new BigDecimal(Etc.standardizeLatOrLong(latitude)) );
				city.setLongitude( new BigDecimal(Etc.standardizeLatOrLong(longitude)) );
			} catch (IllegalArgumentException e) {
				// we ignore the exception,
				// because we allow the possibility of entering incorrect data from the user
			}
			return city;
		}
	}
}
