package com.polovyi.ivan.tutorials.queries;

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
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

public class TermQueryExample {

    public static Directory directory;

    public static void main(String[] args) throws IOException {
        Path indexPath = Files.createDirectories(Paths.get("test-index"));
        directory = FSDirectory.open(indexPath);
        String fieldName = "document-text";
        createDoc(fieldName);

        System.out.println("<< Search by Term lucene >>");
        Query termLuceneQuery = new TermQuery(new Term(fieldName, "lucene"));
        Set<Document> documentsWithLuceneTerm = searchDocs(termLuceneQuery);
        assertEquals(3, documentsWithLuceneTerm.size());
        documentsWithLuceneTerm.forEach(System.out::println);

        System.out.println("<< Search by Term apache >>");
        Query termApacheQuery = new TermQuery(new Term(fieldName, "apache"));
        Set<Document> documentsWithApacheTerm = searchDocs(termApacheQuery);
        assertEquals(2, documentsWithApacheTerm.size());
        documentsWithApacheTerm.forEach(System.out::println);

        System.out.println("<< Search by Term java >>");
        Query termJavaQuery = new TermQuery(new Term(fieldName, "java"));
        Set<Document> documentsWithJavaTerm = searchDocs(termJavaQuery);
        assertEquals(1, documentsWithJavaTerm.size());
        documentsWithJavaTerm.forEach(System.out::println);

        System.out.println("<< Search by non existent Term database >>");
        Query queryWithNonExistentTerm = new TermQuery(new Term(fieldName, "database"));
        assertEquals(0, searchDocs(queryWithNonExistentTerm).size());

        System.out.println("<< Search by more then one Term apache lucene>>");
        Query queryWithMultipleTerms = new TermQuery(new Term(fieldName, "apache lucene"));
        assertEquals(0, searchDocs(queryWithMultipleTerms).size());

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

