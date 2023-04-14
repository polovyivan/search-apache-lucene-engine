package com.polovyi.ivan.tutorials.v2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

public class AnalyzedVsNotAnalyzedFields {

    public static void main(String[] args) throws IOException, ParseException {

        Path indexPath = Files.createDirectories(Paths.get("tempIndex"));
        Directory directory = FSDirectory.open(indexPath);
        IndexWriterConfig config = new IndexWriterConfig();

        Document document1 = new Document();
        String document1Text = "Lucene is a Java library that lets you add a search to the application";
        document1.add(new StringField("document-text", document1Text, Store.YES));



        IndexWriter indexWriter = new IndexWriter(directory, config);
        indexWriter.addDocument(document1);
        indexWriter.close();

        DirectoryReader indexReader = DirectoryReader.open(directory);
        Query query = new QueryParser("document-text", new StandardAnalyzer())
                .parse("Lucene is a Java library that lets you add a search to the application");

        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;

        System.out.println("<< When searching for Lucene only a stored field is displayed >>");
        Arrays.stream(hits)
                .map(scoreDoc -> {
                    try {
                        return indexSearcher.doc(scoreDoc.doc);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).forEach(doc -> doc.getFields().forEach(System.out::println));

        indexReader.close();
        directory.close();
        IOUtils.rm(indexPath);
    }


}
