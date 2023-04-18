package com.polovyi.ivan.tutorials.firstindex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

public class FirstIndex {

    public static void main(String[] args) throws IOException, ParseException {
        // Get path
        Path indexPath = Files.createTempDirectory("test-index");
        // Create directory
        Directory directory = FSDirectory.open(indexPath);
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory, config);

        // Declare text to be added to an index
        String text1 = "Lucene is a Java library that lets you add a search to the application";
        String text2 = "Apache Lucene is an open-source, scalable, search storage engine";
        String text3 = "Two of the most popular search engines Elasticsearch and Apache Solr are built on top of Lucene";

        // Create documents
        Document document1 = new Document();
        String fieldName = "document-text";
        document1.add(new Field(fieldName, text1, TextField.TYPE_STORED));
        Document document2 = new Document();
        document2.add(new Field(fieldName, text2, TextField.TYPE_STORED));
        Document document3 = new Document();
        document3.add(new Field(fieldName, text3, TextField.TYPE_STORED));

        // Write documents to the index
        indexWriter.addDocument(document1);
        indexWriter.addDocument(document2);
        indexWriter.addDocument(document3);
        indexWriter.close();

        // Search the index
        DirectoryReader directoryReader = DirectoryReader.open(directory);
        //Create lucene searcher. It searches over a single DirectoryReader.
        IndexSearcher indexSearcher = new IndexSearcher(directoryReader);
        QueryParser parser = new QueryParser(fieldName, analyzer);
        Query query = parser.parse("Java");

        // retrieve top 10 search results
        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;
        StoredFields storedFields = indexSearcher.storedFields();

        // Process the result
        for (ScoreDoc hit : hits) {
            Document hitDoc = storedFields.document(hit.doc);
            System.out.println(fieldName + " = " + hitDoc.get(fieldName));
        }

        // Close directory reader
        directoryReader.close();
        // Close directory
        directory.close();
        // Remove directory with an index
        // In case of an error the directory won't be removed, so you have to do it manually
        IOUtils.rm(indexPath);
    }
}
