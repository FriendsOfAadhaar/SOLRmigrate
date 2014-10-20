package com.aadhar.index;


import com.googlecode.jcsv.CSVStrategy;
import com.googlecode.jcsv.reader.CSVReader;
import com.googlecode.jcsv.reader.internal.CSVReaderBuilder;
import com.googlecode.jcsv.reader.internal.DefaultCSVEntryParser;
import org.apache.commons.io.IOUtils;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.SocketException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.rmi.ConnectException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IndexTool {
    static final Logger log = LoggerFactory.getLogger(IndexTool.class);
    private static final int BATCH_SIZE = 1000;


    private static final String ZK_HOST = "zkHost";
    private static final String FILE = "file";
    private static final String COLLECTION_NAME = "collection";

    private final String zkHost;
    private final String collectionName;
    private final String fileName;
    private final CloudSolrServer client;

    List<SolrInputDocument> sdocs;
    private final String[] columns;

    public IndexTool(String fileName, String zkHost, String collectionName) {
        this.fileName = fileName;
        this.zkHost = zkHost;
        this.collectionName = collectionName;

        client = new CloudSolrServer(this.zkHost);
        client.setDefaultCollection(this.collectionName);

        sdocs = new ArrayList<SolrInputDocument>(BATCH_SIZE);

        String columnStr = "id1,id2,id3,res_name,res_dob_ddmmyyyy,res_gender,res_mobile_num,res_email_id," +
                "res_lang_code,biometric_flag,info_shre_cnsnt,issue_date,res_addr_careof,res_addr_building," +
                "res_addr_street,res_addr_landmark,res_addr_locality,res_addr_vtc_name,res_addr_district_name," +
                "res_addr_subdistrict_name,res_addr_state_name,res_addr_po_name,res_addr_pincode";
        columns = columnStr.split(",");

    }

    /**
     *
     * java -jar aadhar-index-tool.jar -file data-part1.csv -zkHost localhost:2181
     */
    public static void main(String args[]) throws Exception {

        String zkHost = System.getProperty(ZK_HOST);
        String file = System.getProperty(FILE);
        String collectionName = System.getProperty(COLLECTION_NAME);
        //String threads = System.getProperty(THREADS);

        if (zkHost == null || file == null || collectionName == null) {
            throw new Exception("zkHost, file and collection params are mandatory");
        }

        IndexTool indexer = new IndexTool(file, zkHost, collectionName);
        indexer.startIndexing();
        indexer.cleanup();
    }

    private void cleanup() {
        client.shutdown();
    }

    private void startIndexing() throws IOException {
        Path path = FileSystems.getDefault().getPath(fileName);
        if (Files.isDirectory(path)) {
            List<String> files = new ArrayList<String>();
            listFiles(path, files);
            for(String file : files) {
                batchInput(file);
            }
        } else {
            batchInput(fileName);
        }

    }

    private void listFiles(Path path, List<String> files) throws IOException {
        DirectoryStream<Path> stream = Files.newDirectoryStream(path);
        for (Path entry : stream) {
            if (Files.isDirectory(entry)) {
                listFiles(entry, files);
            } else {
                files.add(path.toString());
            }
        }
    }

    private void batchInput(String file) {
        Reader reader = null;

        try {
            reader = new FileReader(file);
            CSVReader<String[]> csvPersonReader = new CSVReaderBuilder<String[]>(reader).
                    strategy(CSVStrategy.UK_DEFAULT).entryParser(new DefaultCSVEntryParser()).build();
            Iterator<String[]> iter = csvPersonReader.iterator();
            while (iter.hasNext()) {
                String[] values = iter.next();
                 if ( values.length == columns.length) { //TODO what to do otherwise
                    SolrInputDocument doc = new SolrInputDocument();

                     for(int i=0; i<values.length; i++) {
                        doc.addField(columns[i], values[i]);
                    }
                    transformDocument(doc);
                    sdocs.add(doc);
                    if ( sdocs.size() == BATCH_SIZE) {
                        sendBatch(sdocs, 10, 3);
                    }
                }
            }
            //Remaining docs
            if (sdocs.size() != 0) {
                sendBatch(sdocs, 10, 3);
            }
        } catch (FileNotFoundException e) {
            log.error("File " + file + " could not be found", e);
        } catch (Exception e) {
            log.error("Could not send bath ", e);
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }

    private void transformDocument(SolrInputDocument doc) {
        //noop. If you want to transform the document. Remove values which read "NULL" etc.
    }

    private void sendBatch(List<SolrInputDocument> batch, int waitBeforeRetry, int maxRetries) throws Exception {
        try {
            client.add(batch);
        } catch (Exception exc) {
            Throwable rootCause = SolrException.getRootCause(exc);
            boolean wasCommError = (rootCause instanceof ConnectException ||
                    rootCause instanceof ConnectTimeoutException ||
                    rootCause instanceof NoHttpResponseException ||
                    rootCause instanceof SocketException);

            if (wasCommError) {
                if (--maxRetries > 0) {
                    log.warn("ERROR: " + rootCause + " ... Sleeping for "
                            + waitBeforeRetry + " seconds before re-try ...");
                    Thread.sleep(waitBeforeRetry * 1000L);
                    sendBatch(batch, waitBeforeRetry, maxRetries);
                } else {
                    log.error("No more retries available! Add batch failed due to: " + rootCause);
                    throw exc;
                }
            }
        }
        batch.clear();
    }
}
