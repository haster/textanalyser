package nl.crashdata.textanalyser.rest.service.resource;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import nl.crashdata.textanalyser.rest.model.WordFrequency;
import nl.crashdata.textanalyser.rest.service.WordFrequencyAnalyzer;

@RestController
@RequestMapping("analyze")
public class WordFrequencyAnalyzerService implements WordFrequencyAnalyzer
{
	@GetMapping("highestfreq")
	@Override
	public int calculateHighestFrequency(@RequestParam("text") String text)
	{
		Map<String, Integer> freqCount = getFrequencyCount(text);
		
		return freqCount.values().stream().max(Comparator.naturalOrder()).orElse(0);
	}

	@GetMapping("frequency")
	@Override
	public int calculateFrequencyForWord(@RequestParam("text") String text, @RequestParam("word") String word)
	{
		Map<String, Integer> freqCount = getFrequencyCount(text);
		
		return freqCount.getOrDefault(word, 0);
	}

	@GetMapping("calculateMostFrequent")
	@Override
	public List<WordFrequency> calculateNMostFrequentWords(@RequestParam("text") String text, @RequestParam("topN") int topN)
	{
		Map<String, Integer> freqCount = getFrequencyCount(text);
		List<Map.Entry<String, Integer>> sortedFreqCounts = new ArrayList<>(freqCount.entrySet());
		sortedFreqCounts.sort(Comparator.<Map.Entry<String, Integer>>comparingInt(e -> e.getValue().intValue()).reversed().thenComparing(Map.Entry::getKey));
		
		List<Entry<String, Integer>> shrunkSortedFreqCounts = sortedFreqCounts.subList(0,  Math.min(sortedFreqCounts.size(), topN));
		return shrunkSortedFreqCounts.stream().map(e -> new WordFrequency(e.getKey(), e.getValue())).collect(Collectors.toList());
	}
	
	private Map<String, Integer> getFrequencyCount(String text)
	{
		Map<String, Integer> freqCount = new HashMap<>();
		if (text.length() == 0)
		{
			// String.split has a special case for empty input strings where it returns an array with that empty string, which screws up the count.
			// It was kept in the JDK due to internal dependencies, see https://bugs.openjdk.org/browse/JDK-8028321
			return freqCount;
		}
		for (String word : text.split("[^\\p{L}]+"))
		{
			freqCount.merge(word.toLowerCase(), 1, (i,j) -> i + j);
		}
		return freqCount;
	}
}
