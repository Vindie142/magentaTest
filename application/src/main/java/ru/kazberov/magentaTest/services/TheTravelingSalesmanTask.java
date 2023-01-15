package ru.kazberov.magentaTest.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import ru.kazberov.magentaTest.models.City;
import ru.kazberov.magentaTest.models.Distance;
import ru.kazberov.magentaTest.repos.CityRepo;

public class TheTravelingSalesmanTask {
	
	/**
	 * looking for the minimum path from city to city. If there is no path, returns null
	 * @param fromCity
	 * @param toCity
	 * @param cityRepo
	 * @return
	 */
	public static BigDecimal calc(City fromCity, City toCity, CityRepo cityRepo) {
		if (fromCity.equals(toCity)) {
			new BigDecimal("0").setScale(Distance.NUMBER_OF_DECIMAL_OF_DIST, RoundingMode.HALF_DOWN);
		}

		count = new BigDecimal("0").setScale(Distance.NUMBER_OF_DECIMAL_OF_DIST, RoundingMode.HALF_DOWN);
		min = null;
		// the first loop to find the minimum path
		loop(cityRepo.findByName(fromCity.getName()).get(), toCity, cityRepo);
		return min;
	}
	
	private static BigDecimal count = new BigDecimal("0").setScale(Distance.NUMBER_OF_DECIMAL_OF_DIST, RoundingMode.HALF_DOWN);
	private static BigDecimal min = null;
	private static void loop(City fromCity, City toCity, CityRepo cityRepo) {
		List<Distance> distances = fromCity.getDistances();
		distances.sort(new Distance.DistanceComparatorByRandom());
		for (Distance dist : distances) {
			count = count.add(dist.getDistance());
			if (min != null && count.compareTo(min) > 0) {
				count = count.subtract(dist.getDistance());
				continue;
			}
			if (dist.getToCity().equals(toCity) ) {
				if (min == null || count.compareTo(min) < 0) {
					min = count;
				}
				count = count.subtract(dist.getDistance());
				continue;
			} else {
				loop(cityRepo.findByName(dist.getToCity().getName()).get(), toCity, cityRepo);
				count = count.subtract(dist.getDistance());
			}
		}
		return;
	}
}
