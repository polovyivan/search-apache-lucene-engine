package com.polovyi.ivan.tutorials.v2;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.StringField;
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
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.IOUtils;

public class AnalyzedVsNotAnalyzedFields {

    public static Directory directory;

    public static void main(String[] args) throws IOException, ParseException {
        Path indexPath = Files.createDirectories(Paths.get("test-index"));
        directory = FSDirectory.open(indexPath);
        IndexWriterConfig config = new IndexWriterConfig();
        IndexWriter indexWriter = new IndexWriter(directory, config);

        Document document1 = new Document();
        document1.add(new StringField("not-stored-not-analyzed", "Simple text 1", Store.NO));

        // Another way o do the same as StringField
        // FieldType type = new FieldType();
        // type.setIndexOptions(IndexOptions.DOCS);
        // type.setTokenized(false);
        // type.setStored(false);
        // document1.add(new Field("not-stored-not-analyzed", "Simple text 1", type));

        String document1Text = "Lucene is a Java library that lets you add a search to the application";
        document1.add(new TextField("stored-analyzed-field", document1Text, Store.YES));

        indexWriter.addDocument(document1);
        indexWriter.close();

        Query byId = new TermQuery(new Term("not-stored-not-analyzed", "Simple text 1"));
        Set<Document> documentsById = searchDocs(byId);
        assertEquals(1, documentsById.size());
        System.out.println("Documents By not-stored-not-analyzed field");
        printDocuments(documentsById);

        Query bySingleTerm = new TermQuery(new Term("not-stored-not-analyzed", "text"));
        Set<Document> documentsBySingleTerm = searchDocs(bySingleTerm);
        assertEquals(0, documentsBySingleTerm.size());

        Query byTerm = new TermQuery(new Term("stored-analyzed-field", "java"));
        Set<Document> documentsByTerm = searchDocs(byTerm);
        assertEquals(1, documentsByTerm.size());
        System.out.println("Documents By Term from stored-analyzed-field");
        printDocuments(documentsByTerm);

        directory.close();
        IOUtils.rm(indexPath);
    }

    private static void printDocuments(Set<Document> documents) {
        documents.forEach(doc -> {
            System.out.println("doc.not-stored-not-analyzed = " + doc.get("not-stored-not-analyzed"));
            System.out.println("doc.stored-analyzed-field = " + doc.get("stored-analyzed-field"));
            System.out.println("doc.analyzed-not-stored = " + doc.get("analyzed-not-stored"));
        });
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
