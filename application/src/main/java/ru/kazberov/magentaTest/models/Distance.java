package ru.kazberov.magentaTest.models;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.Random;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import ru.kazberov.magentaTest.repos.CityRepo;
import ru.kazberov.magentaTest.services.XMLhelper.Deserializable;



@Entity
@Table(name="distances")
public class Distance implements Comparable<Distance> {

	/**
	 * the number of decimal places of latitude and longitude
	 */
	public static int NUMBER_OF_DECIMAL_OF_DIST = 1;

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne (optional=false)
    @JoinColumn (name="from_city_id")
	private City fromCity;
	@ManyToOne (optional=false)
    @JoinColumn (name="to_city_id")
	private City toCity;
	@Column(nullable = false)
	private BigDecimal distance;
	
	public Distance() {}
	public Distance(City fromCity, City toCity, BigDecimal distance) {
		this.fromCity = fromCity;
		this.toCity = toCity;
		this.distance = distance == null ? distance : distance.setScale(NUMBER_OF_DECIMAL_OF_DIST, RoundingMode.HALF_DOWN);
	}
	
	/**
	 * returns an entity with the opposite direction
	 * For example, it was from A to B, it became from B to A with the same distance
	 * @return reversed Distance
	 */
	public Distance reverse() {
		return new Distance(toCity, fromCity, distance);
	}
	
	/**
	 * checks distance > 0
	 * if the distance == null it returns true 
	 * @return true - correct, false - not correct
	 */
	public boolean ifCorrectDistance(){
		if (distance != null && distance.compareTo(new BigDecimal("0")) <= 0) {
			return false;
		}
		return true;
	}
	
	public static class DistanceComparatorByRandom implements Comparator<Distance> {
		   @Override
		   public int compare(Distance d1, Distance d2) {
			   return 100 - new Random().nextInt(201);
		   }
	}
	
	@Override
	public int compareTo(Distance a) {
		return this.fromCity.compareTo(a.fromCity);
	}
	
	@Override
	public String toString() {
		String fromCity = this.fromCity == null ? "" : this.fromCity.getName();
		String toCity = this.toCity == null ? "" : this.toCity.getName();
		String distance = this.distance == null ? "" : this.distance.toPlainString();
		return  "Distance{"+
				"fromCity="+fromCity+", "+
				"toCity="+toCity+", "+
				"distance="+distance+"}";
	}
	
	@Override
    public boolean equals(Object obj) {
	    if (obj == this) {
	        return true;
	    }
	    if (obj == null || obj.getClass() != this.getClass()) {
	        return false;
	    }
	    Distance mayBe = (Distance) obj;
	    if (id != null && mayBe.getId() !=null) {
	    	return id == mayBe.getId();
		}
	    return mayBe.toString().equals(this.toString());
    }
	
	@Override
    public int hashCode() {
		int result = 31;
		int a1 = id == null ? 1 : id.hashCode();
	    int a2 = fromCity == null ? 1 : fromCity.hashCode();
	    int a3 = toCity == null ? 1 : toCity.hashCode();
	    int a4 = distance == null ? 1 : distance.hashCode();
	    return result * (a1 + a2 + a3 + a4);
    }
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public City getFromCity() {
		return fromCity;
	}
	public void setFromCity(City fromCity) {
		this.fromCity = fromCity;
	}
	public City getToCity() {
		return toCity;
	}
	public void setToCity(City toCity) {
		this.toCity = toCity;
	}
	public BigDecimal getDistance() {
		return distance;
	}
	public void setDistance(BigDecimal distance) {
		this.distance = distance == null ? distance : distance.setScale(NUMBER_OF_DECIMAL_OF_DIST, RoundingMode.HALF_DOWN);
	}
	
	
	
	
	
	public static class DeserializableDistance implements Deserializable {
		public Long id;
		public String fromCity;
		public String toCity;
		public BigDecimal distance;
		public DeserializableDistance() {}
		
		@Override
		public String toString() {
			String id = this.id == null ? "" : this.id.toString();
			String fromCity = this.fromCity == null ? "" : this.fromCity;
			String toCity = this.toCity == null ? "" : this.toCity;
			String distance = this.distance == null ? "" : this.distance.toPlainString();
			return  "Distance{"+
					"id="+id+", "+
					"fromCity="+fromCity+", "+
					"toCity="+toCity+", "+
					"distance="+distance+"}";
		}

		@Override
		public Object toParentEntity(CityRepo cityRepo) {
			Distance dist = new Distance();
			// dist.setId(id);
			dist.setDistance(distance);
			dist.setFromCity(cityRepo.findByNameOrCreate(fromCity));
			dist.setToCity(cityRepo.findByNameOrCreate(toCity));
			return dist;
		}
	}
}
