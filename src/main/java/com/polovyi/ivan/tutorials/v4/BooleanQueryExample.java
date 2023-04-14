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
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

public class BooleanQueryExample {

    public static Directory directory;

    public static void main(String[] args) throws IOException {
        Path indexPath = Files.createDirectories(Paths.get("test-index"));
        directory = FSDirectory.open(indexPath);
        String fieldName = "document-text";
        createDoc(fieldName);

        TermQuery javaTermQuery = new TermQuery(new Term(fieldName, "java"));
        TermQuery applicationTermQuery = new TermQuery(new Term(fieldName, "application"));
        System.out.println("<< BooleanQuery Doc must contain 'java' and 'application'>>");
        Query booleanQuery1
                = new BooleanQuery.Builder()
                .add(javaTermQuery, BooleanClause.Occur.MUST)
                .add(applicationTermQuery, BooleanClause.Occur.MUST)
                .build();
        Set<Document> documents1 = searchDocs(booleanQuery1);
        assertEquals(1, documents1.size());
        documents1.forEach(System.out::println);

        System.out.println("<< BooleanQuery Doc must contain 'search' but not 'application'>>");
        TermQuery searchTermQuery = new TermQuery(new Term(fieldName, "search"));

        Query booleanQuery2
                = new BooleanQuery.Builder()
                .add(searchTermQuery, BooleanClause.Occur.MUST)
                .add(applicationTermQuery, Occur.MUST_NOT)
                .build();
        Set<Document> documents2 = searchDocs(booleanQuery2);
        assertEquals(2, documents2.size());
        documents2.forEach(System.out::println);

        System.out.println("<< BooleanQuery Doc must contain 'java' or 'elasticsearch'>>");
        TermQuery elasticSearchTermQuery = new TermQuery(new Term(fieldName, "elasticsearch"));

        Query booleanQuery3
                = new BooleanQuery.Builder()
                .add(elasticSearchTermQuery, Occur.SHOULD)
                .add(applicationTermQuery, Occur.SHOULD)
                .build();
        Set<Document> documents3 = searchDocs(booleanQuery3);
        assertEquals(2, documents3.size());
        documents3.forEach(System.out::println);

        directory.close();
        IOUtils.rm(indexPath);
    }

    public static void createDoc(String fieldName) throws IOException {
        // Declare text to be added to an index
        String text1 = "Lucene is a Java library that lets you add a search to the application";
        String text2 = "Apache Lucene is an open-source, scalable, search storage engine";
        String text3 = "Two of the most popular search engines Elasticsearch and Apache Solr are built on top of Lucene";

        Document document1 = new Document();
        document1.add(new TextField(fieldName, text1, Store.YES));
        Document document2 = new Document();
        document2.add(new TextField(fieldName, text2, Store.YES));
        Document document3 = new Document();
        document3.add(new TextField(fieldName, text3, Store.YES));
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig());
        indexWriter.addDocument(document1);
        indexWriter.addDocument(document2);
        indexWriter.addDocument(document3);
        indexWriter.close();
    }

    private static Set<Document> searchDocs(Query query) throws IOException {
        DirectoryReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        ScoreDoc[] hits = indexSearcher.search(query, 10).scoreDocs;
        StoredFields storedFields = indexSearcher.storedFields();
        Set<Document> documents = new HashSet<>();
        for (ScoreDoc hit : hits) {
            Document hitDoc = storedFields.document(hit.doc);
            documents.add(hitDoc);
        }
        indexReader.close();
        return documents;
    }
}

