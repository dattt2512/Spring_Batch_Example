package com.tdt.springbatch.service.impl;

import com.tdt.springbatch.repository.OrderRepository;
import com.tdt.springbatch.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRCsvDataSource;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final String TEMP_FILE = "tempOrders.csv";

    private static final Logger logger = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private OrderRepository orderRepo;

    @Override
    public int[] batchInsert(int recordsStart, int recordsStop) {
        return orderRepo.batchInsert(recordsStart, recordsStop);
    }

    @Override
//    @Scheduled(cron = "0 */30 * * * *")
    public void exportDBToCsv() {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("startAt", System.currentTimeMillis()).toJobParameters();

        try {
            jobLauncher.run(job, jobParameters);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss");
            var dateTimeNow = LocalDateTime.now().format(formatter);

            File sourceFile = new File(TEMP_FILE);

            OutputStream os = new FileOutputStream(sourceFile);
            for (int i = 1; i < 9; i++) {
                File file = new File("orders" + i + ".csv");
                InputStream is = new FileInputStream(file);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) > 0) {
                    os.write(buffer, 0, length);
                }
                file.delete();
            }

            String fileName = this.genderFileName();
            File reportCSV = new File(fileName + ".csv");

            this.copyFile(sourceFile, reportCSV);

        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException | IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void copyFile(File source, File dest) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            is.close();
            os.close();
        }
    }

    @Override
    public void reportPdf() throws JRException {
        JasperPrint jasperPrint = this.configJasperPrint("PDF");
        String fileName = this.genderFileName();

        JRPdfExporter exporter = new JRPdfExporter();
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileName + ".pdf"));
        exporter.exportReport();
    }

    @Override
    public void reportExcel() throws JRException {
        JasperPrint jasperPrint = this.configJasperPrint("EXCEL");
        String fileName = this.genderFileName();

        JRXlsxExporter exporter = new JRXlsxExporter();
        SimpleXlsxReportConfiguration reportConfigXLS = new SimpleXlsxReportConfiguration();
        reportConfigXLS.setDetectCellType(true);
        reportConfigXLS.setCollapseRowSpan(false);
        exporter.setConfiguration(reportConfigXLS);
        exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
        exporter.setExporterOutput(new SimpleOutputStreamExporterOutput(fileName + ".xlsx"));
        exporter.exportReport();
    }

    private JasperPrint configJasperPrint(String exportType) throws JRException {
        String titleTransactionBy = "Transactions Report";

        String[] columnNames = new String[] {"id", "code", "stockCode", "price", "quantity", "total", "orderDate"};
        JRCsvDataSource jrCsvDataSource = new JRCsvDataSource("/home/eco1027_dattt/workspace/spring-batch/" + TEMP_FILE);
        jrCsvDataSource.setColumnNames(columnNames);
        jrCsvDataSource.setFieldDelimiter(';');
        jrCsvDataSource.setDateFormat(new SimpleDateFormat("yyyy-MM-dd"));

        HashMap<String, Object> parameters = new HashMap();
        parameters.put("title", titleTransactionBy);

        InputStream transactionReportStream =
                getClass()
                        .getResourceAsStream(
                                "/transaction_report_" + exportType.toString().toLowerCase() + ".jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(transactionReportStream);

        JasperPrint jasperPrint =
                JasperFillManager.fillReport(jasperReport, parameters, jrCsvDataSource);

        return jasperPrint;
    }

    private String genderFileName() {
        String titleTransactionBy = "Transactions Report";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd.HH.mm.ss");
        var dateTimeNow = LocalDateTime.now().format(formatter);
        var fileName = titleTransactionBy.replace(" ", "") + dateTimeNow;

        return fileName;
    }
}
