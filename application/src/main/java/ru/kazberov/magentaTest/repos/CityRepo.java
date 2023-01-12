package ru.kazberov.magentaTest.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ru.kazberov.magentaTest.models.City;

public interface CityRepo extends JpaRepository<City, Long> {
	
	@Query(" SELECT c "
			+ "FROM City c "
			+ "WHERE c.name = :name ")
			public Optional<City> findByName(@Param("name") String name);
	
	@Query(" SELECT c "
			+ "FROM City c ")
			public List<City> findAll();
	
	/**
	 * saves taking into account the uniqueness of the name
	 * @param city
	 */
	public default void saveItCorrectly(City city) {
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
}

