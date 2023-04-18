package com.polovyi.ivan.tutorials.sort;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.SortedDocValuesField;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;
import org.apache.lucene.search.SortField.Type;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.IOUtils;

public class SortQueryResultExample {

    public static Directory directory;

    public static void main(String[] args) throws IOException {
        Path indexPath = Files.createDirectories(Paths.get("test-index"));
        directory = FSDirectory.open(indexPath);

        String fieldName = "document-text";
        String sortDateFieldName = "date";
        createDoc(fieldName, sortDateFieldName);

        Query matchAllDocsQuery = new MatchAllDocsQuery();

        Sort sort = new Sort(SortField.FIELD_SCORE,
                new SortField(sortDateFieldName, Type.STRING, false));

        Set<Document> documents = searchDocs(matchAllDocsQuery, sort);

        documents.forEach(doc -> {
            String textField = doc.get(fieldName);
            String data = doc.get(sortDateFieldName);
            System.out.println("doc = " + data + "-" + textField);
        });

        directory.close();
        IOUtils.rm(indexPath);
    }

    public static void createDoc(String fieldName, String sortDateFieldName) throws IOException {
        String text1 = "Lucene is a Java library that lets you add a search to the application";
        String text2 = "Apache Lucene is an open-source, scalable, search storage engine";
        String text3 = "Two of the most popular search engines Elasticsearch and Apache Solr are built on top of Lucene";

        Document document1 = new Document();
        document1.add(new TextField(fieldName, text1, Store.YES));

        document1.add(new SortedDocValuesField(sortDateFieldName, new BytesRef("2021-04-11")));
        document1.add(new StoredField(sortDateFieldName, "2021-04-11"));
        Document document2 = new Document();
        document2.add(new TextField(fieldName, text2, Store.YES));
        document2.add(new SortedDocValuesField(sortDateFieldName, new BytesRef("2022-04-11")));
        document2.add(new StoredField(sortDateFieldName, "2022-04-11"));
        Document document3 = new Document();
        document3.add(new TextField(fieldName, text3, Store.YES));
        document3.add(new SortedDocValuesField(sortDateFieldName, new BytesRef("2023-04-11")));
        document3.add(new StoredField(sortDateFieldName, "2023-04-11"));

        IndexWriter indexWriter = new IndexWriter(directory, new IndexWriterConfig());
        indexWriter.addDocument(document1);
        indexWriter.addDocument(document2);
        indexWriter.addDocument(document3);
        indexWriter.close();
    }

    private static Set<Document> searchDocs(Query query, Sort sort) throws IOException {
        DirectoryReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        ScoreDoc[] hits = indexSearcher.search(query, 10, sort, true).scoreDocs;
        StoredFields storedFields = indexSearcher.storedFields();
        Set<Document> documents = new HashSet<>();
        for (ScoreDoc hit : hits) {
            Document hitDoc = storedFields.document(hit.doc);
            documents.add(hitDoc);
        }
        return documents;
    }
}
