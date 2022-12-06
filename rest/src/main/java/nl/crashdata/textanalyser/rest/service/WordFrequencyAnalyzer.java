package nl.crashdata.textanalyser.rest.service;

import java.util.List;

import nl.crashdata.textanalyser.rest.model.WordFrequency;

public interface WordFrequencyAnalyzer
{
	int calculateHighestFrequency(String text);
	int calculateFrequencyForWord (String text, String word);
	List<WordFrequency> calculateNMostFrequentWords (String text, int n);
}
