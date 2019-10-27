package kiarahmani.atropos.utils;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class Util {
	public static <T> List<T> getIntersectOfCollections(Collection<T> first, Collection<T> second) {
		return first.stream().filter(second::contains).collect(Collectors.toList());
	}
}
