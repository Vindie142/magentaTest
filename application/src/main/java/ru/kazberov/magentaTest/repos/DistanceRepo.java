package ru.kazberov.magentaTest.repos;

import org.springframework.data.repository.CrudRepository;

import ru.kazberov.magentaTest.models.Distance;

public interface DistanceRepo extends CrudRepository<Distance, Long> {
	
	public default void saveItCorrectly(Distance distance) {
		save(distance);
	}
	
}

