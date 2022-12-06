package nl.crashdata.textanalyser.rest.model;

import java.util.Comparator;
import java.util.Objects;

public record WordFrequency(String word, int frequency) implements Comparable<WordFrequency>
{
	@Override
	public int compareTo(WordFrequency other) {
		return Objects.compare(this, other,
				Comparator.comparing(WordFrequency::frequency).reversed()
				    .thenComparing(WordFrequency::word));
	}
};
