package com.polovyi.ivan.tutorials.manipulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

public class DeleteAllDocumentsExample {

    public static Directory directory;

    public static void main(String[] args) throws IOException {
        Path indexPath = Files.createDirectories(Paths.get("test-index"));
        directory = FSDirectory.open(indexPath);
        String fieldName = "document-text";
        createDoc(fieldName);

        Query matchAllDocsQuery = new MatchAllDocsQuery();
        System.out.println("Before deleting");
        searchDocs(matchAllDocsQuery).stream()
                .map(doc -> doc.get(fieldName))
                .forEach(System.out::println);

        deleteDocument();
        System.out.println("After deleting");
        searchDocs(matchAllDocsQuery).stream()
                .map(doc -> doc.get(fieldName))
                .forEach(System.out::println);

        directory.close();
        IOUtils.rm(indexPath);
    }

    public static void createDoc(String fieldName) throws IOException {
        String text1 = "Lucene is a Java library that lets you add a search to the application";
        String text2 = "Apache Lucene is an open-source, scalable, search storage engine";
        String text3 = "Two of the most popular search engines Elasticsearch and Apache Solr are built on top of Lucene";
        Document document1 = new Document();
        document1.add(new StringField(fieldName, text1, Store.YES));
        Document document2 = new Document();
        document2.add(new StringField(fieldName, text2, Store.YES));
        Document document3 = new Document();
        document3.add(new StringField(fieldName, text3, Store.YES));

        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig());
        indexWriter.addDocument(document1);
        indexWriter.addDocument(document2);
        indexWriter.addDocument(document3);
        indexWriter.close();
    }

    private static void deleteDocument() throws IOException {
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig());
        indexWriter.deleteAll();
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

