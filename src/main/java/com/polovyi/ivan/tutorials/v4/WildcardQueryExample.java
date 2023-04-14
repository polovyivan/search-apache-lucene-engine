package com.polovyi.ivan.tutorials.v4;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

public class WildcardQueryExample {

    public static void main(String[] args) throws IOException, ParseException {
        Path indexPath = Files.createDirectories(Paths.get("test-index"));
        Directory directory = FSDirectory.open(indexPath);

        IndexWriterConfig config = new IndexWriterConfig();
        // Declare text to be added to an index
        String text1 = "Lucene is a Java library that lets you add a search to the application";
        String text2 = "Apache Lucene is an open-source, scalable, search storage engine";
        String text3 = "Two of the most popular search engines Elasticsearch and Apache Solr are built on top of Lucene";

//        *	matches any character sequence (including the empty one)
//        ?	matches any single character
//        '\'	escape character
//

        Document document1 = new Document();
        String fieldName = "document-text";
        document1.add(new TextField(fieldName, text1, Store.YES));
        Document document2 = new Document();
        document2.add(new TextField(fieldName, text2, Store.YES));
        Document document3 = new Document();
        document3.add(new TextField(fieldName, text3, Store.YES));
        IndexWriter indexWriter = new IndexWriter(directory, config);
        indexWriter.addDocument(document1);
        indexWriter.addDocument(document2);
        indexWriter.addDocument(document3);
        indexWriter.close();

        DirectoryReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);

        System.out.println("<< WildcardQuery a??lication >>");
        Query singleCharacterWildcardQuery = new WildcardQuery(new Term(fieldName, "a??lication"));
        Set<Document> documentsWithSingleCharacterWildcardQuery = searchDocs(indexSearcher, singleCharacterWildcardQuery);
        assertEquals(1, documentsWithSingleCharacterWildcardQuery.size());
        documentsWithSingleCharacterWildcardQuery.forEach(System.out::println);

        System.out.println("<< WildcardQuery * >>");
        Query onlyWildcardQuery = new WildcardQuery(new Term(fieldName, "*"));
        Set<Document> allDocuments = searchDocs(indexSearcher, onlyWildcardQuery);
        assertEquals(3, allDocuments.size());
        allDocuments.forEach(System.out::println);

        System.out.println("<< WildcardQuery ela* >>");
        Query righSideWildcardQuery = new WildcardQuery(new Term(fieldName, "ela*"));
        Set<Document> documentsWithRightSideWildcardQuery = searchDocs(indexSearcher, righSideWildcardQuery);
        assertEquals(1, documentsWithRightSideWildcardQuery.size());
        documentsWithRightSideWildcardQuery.forEach(System.out::println);

        System.out.println("<< WildcardQuery *ava>>");
        Query leftSideWildcardQuery = new WildcardQuery(new Term(fieldName, "*ava"));
        Set<Document> documentsWithLeftSideWildcardQuery = searchDocs(indexSearcher, leftSideWildcardQuery);
        assertEquals(1, documentsWithLeftSideWildcardQuery.size());
        documentsWithLeftSideWildcardQuery.forEach(System.out::println);

        System.out.println("<< WildcardQuery *bra*>>");
        Query wildcardQuery = new WildcardQuery(new Term(fieldName, "*bra*"));
        Set<Document> documentsWildcardQuery = searchDocs(indexSearcher, wildcardQuery);
        assertEquals(1, documentsWildcardQuery.size());
        documentsWildcardQuery.forEach(System.out::println);

        indexReader.close();
        directory.close();
        IOUtils.rm(indexPath);
    }

    private static Set<Document> searchDocs(IndexSearcher indexSearcher, Query query) throws IOException {
        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;
        StoredFields storedFields = indexSearcher.storedFields();
        Set<Document> documents = new HashSet<>();
        for (ScoreDoc hit : hits) {
            Document hitDoc = storedFields.document(hit.doc);
            documents.add(hitDoc);
        }
        return documents;
    }
}

