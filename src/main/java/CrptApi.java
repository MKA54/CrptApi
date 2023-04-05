import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public class CrptApi {
    private final long duration;
    private final int requestLimit;
    private final String uri;
    private final Object lock = new Object();

    public CrptApi(long duration, int requestLimit) {
        this.duration = duration;

        if (requestLimit < 1) {
            throw new IllegalArgumentException("Request count < 1: " + requestLimit);
        }

        this.requestLimit = requestLimit;
        uri = "/api/v3/lk/documents/send";
    }

    public void addItemToCirculation(DocumentGoodsProducedInRussianFederation document,
                                     String signature) throws InterruptedException, IOException {
        long start = System.currentTimeMillis();

        long end = start + duration;
        int i = 0;

        synchronized (lock) {
            while (i >= requestLimit || System.currentTimeMillis() >= end) {
                lock.wait();
            }

            File file = null;

            if (document.doc_type.equals("LP_INTRODUCE_GOODS")) {
                ObjectMapper mapper = new ObjectMapper();
                file = new File("document.json");
                mapper.writeValue(file, document);
            }

            if (document.doc_type.equals("LP_INTRODUCE_GOODS_CSV")) {
                CsvMapper csvMapper = new CsvMapper();
                file = new File("document.csv");
                csvMapper.writeValue(file, document);
            }

            if (document.doc_type.equals("LP_INTRODUCE_GOODS_XML")) {
                XmlMapper xmlMapper = new XmlMapper();
                file = new File("document.xml");
                xmlMapper.writeValue(file, document);
            }

            assert file != null;
            HttpEntity entity = MultipartEntityBuilder.create()
                    .addPart("file", new FileBody(file))
                    .build();

            HttpPost request = new HttpPost("https://ismp.crpt.ru" + uri);
            request.setEntity(entity);

            i++;
        }

        lock.notifyAll();
    }

    class DocumentGoodsProducedInRussianFederation {
        private final Description description;
        private final String doc_id;
        private final String doc_status;
        private final String doc_type;
        private final String importRequest;
        private final String owner_inn;
        private final String participant_inn;
        private final String producer_inn;
        private final Date production_date;
        private final String production_type;
        private final List<Product> products;
        private final Date reg_date;
        private final String reg_number;

        public DocumentGoodsProducedInRussianFederation(Description description, String doc_id,
                                                        String doc_status, String doc_type,
                                                        String importRequest, String owner_inn,
                                                        String participant_inn, String producer_inn,
                                                        Date production_date, String production_type,
                                                        List<Product> products, Date reg_date,
                                                        String reg_number) {
            this.description = description;
            this.doc_id = doc_id;
            this.doc_status = doc_status;
            this.doc_type = doc_type;
            this.importRequest = importRequest;
            this.owner_inn = owner_inn;
            this.participant_inn = participant_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.production_type = production_type;
            this.products = products;
            this.reg_date = reg_date;
            this.reg_number = reg_number;
        }

        public Description getDescription() {
            return description;
        }

        public String getDoc_id() {
            return doc_id;
        }

        public String getDoc_status() {
            return doc_status;
        }

        public String getDoc_type() {
            return doc_type;
        }

        public String getImportRequest() {
            return importRequest;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public String getParticipant_inn() {
            return participant_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public Date getProduction_date() {
            return production_date;
        }

        public String getProduction_type() {
            return production_type;
        }

        public List<Product> getProducts() {
            return products;
        }

        public Date getReg_date() {
            return reg_date;
        }

        public String getReg_number() {
            return reg_number;
        }
    }

    class Description {
        private final String participantInn;

        public Description(String participantInn) {
            this.participantInn = participantInn;
        }

        public String getParticipantInn() {
            return participantInn;
        }
    }

    class Product {
        private final String certificate_document;
        private final Date certificate_document_date;
        private final String certificate_document_number;
        private final String owner_inn;
        private final String producer_inn;
        private final Date production_date;
        private final String tnved_code;
        private final String uit_code;
        private final String uitu_code;

        public Product(String certificate_document, Date certificate_document_date,
                       String certificate_document_number, String owner_inn,
                       String producer_inn, Date production_date,
                       String tnved_code, String uit_code, String uitu_code) {
            this.certificate_document = certificate_document;
            this.certificate_document_date = certificate_document_date;
            this.certificate_document_number = certificate_document_number;
            this.owner_inn = owner_inn;
            this.producer_inn = producer_inn;
            this.production_date = production_date;
            this.tnved_code = tnved_code;
            this.uit_code = uit_code;
            this.uitu_code = uitu_code;
        }

        public String getCertificate_document() {
            return certificate_document;
        }

        public Date getCertificate_document_date() {
            return certificate_document_date;
        }

        public String getCertificate_document_number() {
            return certificate_document_number;
        }

        public String getOwner_inn() {
            return owner_inn;
        }

        public String getProducer_inn() {
            return producer_inn;
        }

        public Date getProduction_date() {
            return production_date;
        }

        public String getTnved_code() {
            return tnved_code;
        }

        public String getUit_code() {
            return uit_code;
        }

        public String getUitu_code() {
            return uitu_code;
        }
    }
}
