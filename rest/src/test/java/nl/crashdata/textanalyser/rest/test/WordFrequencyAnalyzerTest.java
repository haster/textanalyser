package nl.crashdata.textanalyser.rest.test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.assertj.core.util.Files;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;

import nl.crashdata.textanalyser.rest.model.WordFrequency;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WordFrequencyAnalyzerTest
{
	private static final Logger log = LoggerFactory.getLogger(WordFrequencyAnalyzerTest.class);
	
	private static final String QUICK_BROWN_FOX = """
			The quick brown fox jumps over the lazy horse.
			""";
	
	private static final String MULTILINE_MONKEYS = """
			Monkeys are said to be worshipped in Togo.
			Experiments on monkeys have, however, given negative results.
			
			Monkeys are numerous in the forests, and snakes are common.		
			Monkeys were commonly kept as pets;It may here be remembered that of the mammalia man and monkey alone are capable of convergence, and have a circumscribed macular area.
			""";
	
	private static final String ANCIENT_GREEK = """
			μῆνιν ἄειδε θεὰ Πηληϊάδεω Ἀχιλῆος
			οὐλομένην, ἣ μυρί᾽ Ἀχαιοῖς ἄλγε᾽ ἔθηκε,
			πολλὰς δ᾽ ἰφθίμους ψυχὰς Ἄϊδι προΐαψεν
			ἡρώων, αὐτοὺς δὲ ἑλώρια τεῦχε κύνεσσιν
			5οἰωνοῖσί τε πᾶσι, Διὸς δ᾽ ἐτελείετο βουλή,
			ἐξ οὗ δὴ τὰ πρῶτα διαστήτην ἐρίσαντε
			Ἀτρεΐδης τε ἄναξ ἀνδρῶν καὶ δῖος Ἀχιλλεύς.
			τίς τ᾽ ἄρ σφωε θεῶν ἔριδι ξυνέηκε μάχεσθαι;
			Λητοῦς καὶ Διὸς υἱός: ὃ γὰρ βασιλῆϊ χολωθεὶς
			10νοῦσον ἀνὰ στρατὸν ὄρσε κακήν, ὀλέκοντο δὲ λαοί,
			οὕνεκα τὸν Χρύσην ἠτίμασεν ἀρητῆρα
			Ἀτρεΐδης: ὃ γὰρ ἦλθε θοὰς ἐπὶ νῆας Ἀχαιῶν
			λυσόμενός τε θύγατρα φέρων τ᾽ ἀπερείσι᾽ ἄποινα,
			στέμματ᾽ ἔχων ἐν χερσὶν ἑκηβόλου Ἀπόλλωνος
			15χρυσέῳ ἀνὰ σκήπτρῳ, καὶ λίσσετο πάντας Ἀχαιούς,
			Ἀτρεΐδα δὲ μάλιστα δύω, κοσμήτορε λαῶν:
			Ἀτρεΐδαι τε καὶ ἄλλοι ἐϋκνήμιδες Ἀχαιοί,
			ὑμῖν μὲν θεοὶ δοῖεν Ὀλύμπια δώματ᾽ ἔχοντες
			ἐκπέρσαι Πριάμοιο πόλιν, εὖ δ᾽ οἴκαδ᾽ ἱκέσθαι:
			20παῖδα δ᾽ ἐμοὶ λύσαιτε φίλην, τὰ δ᾽ ἄποινα δέχεσθαι,
			ἁζόμενοι Διὸς υἱὸν ἑκηβόλον Ἀπόλλωνα.
			""";
	
	private static final String WILHELMUS = readWilhelmus();
	
	private static final String ATTA_ATTA = readAttaAtta();
	
	private static final String ONLY_WHITESPACE = """
			    
			    		
			    		  ␋
			""";
	
	private static final String PATH_HIGHEST_FREQ = "/analyze/highestfreq?text={text}";
	
	private static final String PATH_FREQ_FOR_WORD = "/analyze/frequency?text={text}&word={word}";
	
	private static final String PATH_TOPN_WORD_FREQ = "/analyze/calculateMostFrequent?text={text}&topN={topN}";
	
	@LocalServerPort
	private int localPort;
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	@Test
	public void testSimpleSentence()
	{
		Integer highestFrequency = calculateHighestFrequency(QUICK_BROWN_FOX);
		Assertions.assertThat(highestFrequency).isEqualTo(2);
		
		Integer countThe = calculateFrequencyOfWord(QUICK_BROWN_FOX, "the");
		Assertions.assertThat(countThe).isEqualTo(2);
		
		Integer countBrown = calculateFrequencyOfWord(QUICK_BROWN_FOX, "brown");
		Assertions.assertThat(countBrown).isEqualTo(1);
		
		List<WordFrequency> expectedFreqList = toWordFrequencyList("the", 2, "brown", 1, "fox", 1, "jumps", 1, "horse", 1);
		List<WordFrequency> actualFreqList = calculateTopNWordFrequencies(QUICK_BROWN_FOX, 5);
		Assertions.assertThat(actualFreqList).hasSize(5).hasSameElementsAs(expectedFreqList);
	}
	
	@Test
	public void testMultiLine()
	{
		Integer highestFrequency = calculateHighestFrequency(MULTILINE_MONKEYS);
		Assertions.assertThat(highestFrequency).isEqualTo(4);
		
		Integer countMonkeys = calculateFrequencyOfWord(MULTILINE_MONKEYS, "monkeys");
		Assertions.assertThat(countMonkeys).isEqualTo(4);
		
		Integer countPets = calculateFrequencyOfWord(MULTILINE_MONKEYS, "pets");
		Assertions.assertThat(countPets).isEqualTo(1);
		
		Integer countAre = calculateFrequencyOfWord(MULTILINE_MONKEYS, "are");
		Assertions.assertThat(countAre).isEqualTo(4);
		
		List<WordFrequency> expectedFreqList = toWordFrequencyList("are", 4, "monkeys", 4, "and", 3, "be", 2, "have", 2);
		List<WordFrequency> actualFreqList = calculateTopNWordFrequencies(MULTILINE_MONKEYS, 5);
		Assertions.assertThat(actualFreqList).hasSize(5).hasSameElementsAs(expectedFreqList);
	}
	
	@Test
	public void testUnicode()
	{
		Integer highestFrequency = calculateHighestFrequency(ANCIENT_GREEK);
		Assertions.assertThat(highestFrequency).isEqualTo(5);
		
		Integer countἄειδε = calculateFrequencyOfWord(ANCIENT_GREEK, "ἄειδε");
		Assertions.assertThat(countἄειδε).isEqualTo(1);
		
		Integer countδὲ = calculateFrequencyOfWord(ANCIENT_GREEK, "δὲ");
		Assertions.assertThat(countδὲ).isEqualTo(3);
		
		Integer countπολλὰς = calculateFrequencyOfWord(ANCIENT_GREEK, "πολλὰς");
		Assertions.assertThat(countπολλὰς).isEqualTo(1);
		
		List<WordFrequency> expectedFreqList = toWordFrequencyList("δ", 5, "καὶ", 4, "τε", 4, "διὸς", 3, "δὲ", 3);
		List<WordFrequency> actualFreqList = calculateTopNWordFrequencies(ANCIENT_GREEK, 5);
		Assertions.assertThat(actualFreqList).hasSize(5).hasSameElementsAs(expectedFreqList);
	}
	
	@Test
	public void testLongerText()
	{
		Integer highestFrequency = calculateHighestFrequency(WILHELMUS);
		Assertions.assertThat(highestFrequency).isEqualTo(22);
		
		Integer countEen = calculateFrequencyOfWord(WILHELMUS, "een");
		Assertions.assertThat(countEen).isEqualTo(9);
		
		Integer countGij = calculateFrequencyOfWord(WILHELMUS, "gij");
		Assertions.assertThat(countGij).isEqualTo(3);
		
		Integer countVan = calculateFrequencyOfWord(WILHELMUS, "van");
		Assertions.assertThat(countVan).isEqualTo(12);
		
		Integer countOranje = calculateFrequencyOfWord(WILHELMUS, "oranje");
		Assertions.assertThat(countOranje).isEqualTo(1);
		
		List<WordFrequency> expectedFreqList = toWordFrequencyList("ik", 22, "mijn", 19, "in", 15, "den", 14, "dat", 13);
		List<WordFrequency> actualFreqList = calculateTopNWordFrequencies(WILHELMUS, 5);
		Assertions.assertThat(actualFreqList).hasSize(5).hasSameElementsAs(expectedFreqList);
	}
	
	@Test
	public void testTextWithFewUniqueWords()
	{
		Integer highestFrequency = calculateHighestFrequency(ATTA_ATTA);
		Assertions.assertThat(highestFrequency).isEqualTo(18);
		
		Integer countAtta = calculateFrequencyOfWord(ATTA_ATTA, "atta");
		Assertions.assertThat(countAtta).isEqualTo(18);
		
		Integer countBubu = calculateFrequencyOfWord(ATTA_ATTA, "bubu");
		Assertions.assertThat(countBubu).isEqualTo(9);
		
		List<WordFrequency> expectedFreqList = toWordFrequencyList("atta", 18, "bubu", 9, "haja", 9);
		List<WordFrequency> actualFreqList = calculateTopNWordFrequencies(ATTA_ATTA, 5);
		Assertions.assertThat(actualFreqList).hasSize(3).hasSameElementsAs(expectedFreqList);
	}
	
	@Test
	public void testEmptyText()
	{
		Integer highestFrequency = calculateHighestFrequency("");
		Assertions.assertThat(highestFrequency).isEqualTo(0);
		
		Integer countWord = calculateFrequencyOfWord("", "aap");
		Assertions.assertThat(countWord).isEqualTo(0);
		
		Integer countEmpty = calculateFrequencyOfWord("", "");
		Assertions.assertThat(countEmpty).isEqualTo(0);
		
		List<WordFrequency> expectedFreqList = toWordFrequencyList();
		List<WordFrequency> actualFreqList = calculateTopNWordFrequencies("", 5);
		Assertions.assertThat(actualFreqList).hasSize(0).hasSameElementsAs(expectedFreqList);
	}
	
	@Test
	public void testNothingButWhitespace()
	{
		Integer highestFrequency = calculateHighestFrequency(ONLY_WHITESPACE);
		Assertions.assertThat(highestFrequency).isEqualTo(0);
		
		Integer countWord = calculateFrequencyOfWord(ONLY_WHITESPACE, "aap");
		Assertions.assertThat(countWord).isEqualTo(0);
		
		Integer countEmpty = calculateFrequencyOfWord(ONLY_WHITESPACE, "");
		Assertions.assertThat(countEmpty).isEqualTo(0);
		
		Integer countSpace = calculateFrequencyOfWord(ONLY_WHITESPACE, " ");
		Assertions.assertThat(countSpace).isEqualTo(0);
		
		Integer countTab = calculateFrequencyOfWord(ONLY_WHITESPACE, "\t");
		Assertions.assertThat(countTab).isEqualTo(0);
		
		Integer countNewline = calculateFrequencyOfWord(ONLY_WHITESPACE, "\t");
		Assertions.assertThat(countNewline).isEqualTo(0);
		
		List<WordFrequency> expectedFreqList = toWordFrequencyList();
		List<WordFrequency> actualFreqList = calculateTopNWordFrequencies("", 5);
		Assertions.assertThat(actualFreqList).hasSize(0).hasSameElementsAs(expectedFreqList);
	}
	
	@Test
	public void testIncorrectParameters()
	{
		Integer countEmpty = calculateFrequencyOfWord(MULTILINE_MONKEYS, "");
		Assertions.assertThat(countEmpty).isEqualTo(0);
		
		Integer countOranje = calculateFrequencyOfWord(MULTILINE_MONKEYS, "oranje");
		Assertions.assertThat(countOranje).isEqualTo(0);
		
		Integer countMultiWordInput = calculateFrequencyOfWord(MULTILINE_MONKEYS, MULTILINE_MONKEYS);
		Assertions.assertThat(countMultiWordInput).isEqualTo(0);
		
		Integer countOnePointFive = calculateFrequencyOfWord(MULTILINE_MONKEYS, "1.5");
		Assertions.assertThat(countOnePointFive).isEqualTo(0);
		
		Integer countZero = calculateFrequencyOfWord(MULTILINE_MONKEYS, "0");
		Assertions.assertThat(countZero).isEqualTo(0);
		
		Integer countStringNull = calculateFrequencyOfWord(MULTILINE_MONKEYS, "null");
		Assertions.assertThat(countStringNull).isEqualTo(0);
	}

	private Integer calculateHighestFrequency(String text)
	{
		Map<String, ?> params = Map.of("text", text);
		return this.restTemplate.getForObject(getBaseURL() + PATH_HIGHEST_FREQ, Integer.class, params);
	}
	
	private Integer calculateFrequencyOfWord(String text, String word)
	{
		Map<String, ?> params = Map.of("text", text, "word", word);
		return this.restTemplate.getForObject(getBaseURL() + PATH_FREQ_FOR_WORD, Integer.class, params);
	}
	
	private List<WordFrequency> calculateTopNWordFrequencies(String text, int topN)
	{
		Map<String, ?> params = Map.of("text", text, "topN", topN);
		return this.restTemplate.exchange(getBaseURL() + PATH_TOPN_WORD_FREQ, HttpMethod.GET, null, new ParameterizedTypeReference<List<WordFrequency>>() {
		}, params).getBody();
	}
	
	private String getBaseURL()
	{
		return "http://localhost:" + localPort;
	}
	
	private static final List<WordFrequency> toWordFrequencyList(Object... args)
	{
		List<WordFrequency> ret = new ArrayList<>();
		for (int i = 0; i < args.length-1; i+=2)
		{
			ret.add(new WordFrequency((String)args[i], (Integer)args[i+1]));
		}
		return ret;
	}
	
	private static final String readWilhelmus()
	{
		try
		{
			return Files.contentOf(new File(WordFrequencyAnalyzerTest.class.getResource("wilhelmus.txt").toURI()), StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private static final String readAttaAtta()
	{
		try
		{
			return Files.contentOf(new File(WordFrequencyAnalyzerTest.class.getResource("attaatta.txt").toURI()), StandardCharsets.UTF_8);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
}
