package com.polovyi.ivan.tutorials.manipulation;

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
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

public class UpdateDocumentUsingTermExample {

    public static Directory directory;

    public static void main(String[] args) throws IOException {
        Path indexPath = Files.createDirectories(Paths.get("test-index"));
        directory = FSDirectory.open(indexPath);
        String fieldName = "document-text";
        String text1 = "Lucene is a Jawa library that lets you add a search to the application";
        String text2 = "Apache Lucene is an open-source, scalable, search storage engine";
        createDoc(fieldName, text1);
        createDoc(fieldName, text2);

        Query matchAllDocsQuery = new MatchAllDocsQuery();
        System.out.println("<< Before updating by term >>");
        searchDocs(matchAllDocsQuery).stream()
                .map(doc -> doc.get(fieldName))
                .forEach(System.out::println);

        Document newDocument = new Document();
        newDocument.add(new TextField(fieldName, text1.replace("Jawa", "Java"), Store.YES));
        updateDocumentUsingTerm(new Term(fieldName, "jawa"), newDocument);

        System.out.println("<< After updating by term >>");
        searchDocs(matchAllDocsQuery).stream()
                .map(doc -> doc.get(fieldName))
                .forEach(System.out::println);

        directory.close();
        IOUtils.rm(indexPath);
    }

    public static void createDoc(String fieldName, String text) throws IOException {
        Document document = new Document();
        document.add(new TextField(fieldName, text, Store.YES));
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig());
        indexWriter.addDocument(document);
        indexWriter.close();
    }

    private static void updateDocumentUsingTerm(Term term, Document document) throws IOException {
        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig());
        indexWriter.updateDocument(term, document);
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

