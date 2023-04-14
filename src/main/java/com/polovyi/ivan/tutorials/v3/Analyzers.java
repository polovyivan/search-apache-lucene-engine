package com.polovyi.ivan.tutorials.v3;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

public class Analyzers {

    public static void main(String[] args) throws IOException {
        String text = "Lucene is a Java library that lets you add a search to the application";

        Set<String> usingWhiteSpaceAnalyzer = analyzeText(text, new WhitespaceAnalyzer());
        System.out.println("usingWhiteSpaceAnalyzer = " + usingWhiteSpaceAnalyzer);

        Set<String> usingSimpleAnalyzer = analyzeText(text, new SimpleAnalyzer());
        System.out.println("usingSimpleAnalyzer =     " + usingSimpleAnalyzer);

        CharArraySet stopWords = new CharArraySet(List.of("a", "the", "that", "is", "lets", "to", "you", "add"), true);
        Set<String> usingStopAnalyzer = analyzeText(text, new StopAnalyzer(stopWords));
        System.out.println("usingStopAnalyzer =       " + usingStopAnalyzer);

        Set<String> usingKeyWordAnalyzer = analyzeText(text, new KeywordAnalyzer());
        System.out.println("usingKeyWordAnalyzer =    " + usingKeyWordAnalyzer);

        Set<String> usingStandardAnalyzer = analyzeText(text, new StandardAnalyzer());
        System.out.println("usingStandardAnalyzer =   " + usingStandardAnalyzer);

        Analyzer customAnalyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Tokenizer tokenizer = new LetterTokenizer();
                return new Analyzer.TokenStreamComponents(tokenizer, new StopFilter(new LowerCaseFilter(tokenizer), stopWords));
            }
        };
        Set<String> usingCustomAnalyzer = analyzeText(text, customAnalyzer);
        System.out.println("usingCustomAnalyzer =     " + usingCustomAnalyzer);
    }

    private static Set<String> analyzeText(String text, Analyzer analyzer) throws IOException {
        Set<String> result = new HashSet<>();
        TokenStream tokenStream = analyzer.tokenStream("document-text", new StringReader(text));
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            result.add(charTermAttribute.toString());
        }
        tokenStream.end();
        tokenStream.close();
        return result;
    }
}
