package ru.kazberov.magentaTest.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.kazberov.magentaTest.models.City;

public interface CityRepo extends JpaRepository<City, Long> {
	
	@Query(" SELECT c "
			+ "FROM City c LEFT JOIN FETCH c.distances cd "
			+ "WHERE c.name = :name ")
			public Optional<City> findByName(@Param("name") String name);
	
	@Query(" SELECT c "
			+ "FROM City c LEFT JOIN FETCH c.distances cd ")
			public List<City> findAll();
	
	/**
	 * saves taking into account the uniqueness of the name
	 * if the city already exists, updates the latitude and longitude value
	 * @param city
	 * @throws IllegalArgumentException
	 */
	public default void saveItCorrectly(City city) throws IllegalArgumentException {
		if ( !city.ifCorrectLatAndLon() ) {
			throw new IllegalArgumentException("Latitude or longitude are in invalid ranges");
		}
		Optional<City> optCity = findByName(city.getName());
		if (optCity.isEmpty()) {
			save(city);
		} else {
			City oldCity = optCity.get();
			if (city.getLatitude() != null) {
				oldCity.setLatitude(city.getLatitude());
			}
			if (city.getLongitude() != null) {
				oldCity.setLongitude(city.getLongitude());
			}
			save(oldCity);
		}
	}
	/**
	 * if a city with the same name exists, returns it, if not, then creates and returns
	 * @param name
	 */
	public default City findByNameOrCreate(String name) {
		Optional<City> optCity = findByName(name);
		City city;
		if (optCity.isEmpty()) {
			city = new City(name);
			save(city);
		} else {
			city = optCity.get();
		}
		return city;		
	}
}

