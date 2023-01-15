package ru.kazberov.magentaTest.repos;

import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import ru.kazberov.magentaTest.models.Distance;

public interface DistanceRepo extends CrudRepository<Distance, Long> {
	
	@Query(" SELECT d "
			+ "FROM Distance d "
			+ "WHERE d.fromCity.id = :fromCityId "
			+ "	AND d.toCity.id = :toCityId")
			public Optional<Distance> findByCities(@Param("fromCityId") Long fromCityId,
													@Param("toCityId") Long toCityId);
	
	/**
	 * saves the distance without duplication
	 * if the distance already existed, updates the distance value
	 * ! it will not be saved if there are no two cities and a distance !
	 * @param distance
	 * @throws Exception if the object doesn't have all the properties
	 * @throws IllegalArgumentException if distance less than or equal to 0
	 */
	public default void saveItCorrectly(Distance distance) throws IllegalArgumentException, Exception {
		if ( !distance.ifCorrectDistance() ) {
			throw new IllegalArgumentException("Latitude or longitude are in invalid ranges");
		}
		Optional<Distance> optDistance = findByCities(distance.getFromCity().getId(),
														distance.getToCity().getId());
		if (optDistance.isEmpty()) {
			if (distance.getFromCity() != null 
							&& distance.getToCity() != null 
							&& distance.getDistance() != null) {
				save(distance);
			} else {
				throw new Exception("The object doesn't have all the properties!");
			}
		} else {
			Distance oldDistance = optDistance.get();
			if (distance.getDistance() != null) {
				oldDistance.setDistance(distance.getDistance());
			}
			save(oldDistance);
		}
	}
	
}

